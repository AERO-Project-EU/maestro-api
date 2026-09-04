package eu.orchestrator.backend.service.applicationinstance;

import com.querydsl.core.types.dsl.BooleanExpression;
import eu.orchestrator.backend.kafka.Sender;
import eu.orchestrator.backend.service.component.ComponentNodeService;
import eu.orchestrator.backend.service.k8s.KnativeService;
import eu.orchestrator.backend.service.k8s.PrometheusService;
import eu.orchestrator.backend.service.resourceprovider.ProviderService;
import eu.orchestrator.backend.service.sshkey.SSHKeyService;
import eu.orchestrator.backend.service.util.CommonService;
import eu.orchestrator.backend.transfer.*;
import eu.orchestrator.backend.util.ConstantsUtil;
import eu.orchestrator.backend.util.PrometheusUtil;
import eu.orchestrator.backend.util.Util;
import eu.orchestrator.backend.util.KubernetesUtil;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.common.exception.NotAuthorizedException;
import eu.orchestrator.common.util.NullCheckUtil;
import eu.orchestrator.repository.dao.*;
import eu.orchestrator.repository.domain.*;
import eu.orchestrator.repository.domain.ComponentNodeInstance.SecurityEnablers;
import eu.orchestrator.transfer.entities.agent.IDSConfiguration;
import eu.orchestrator.transfer.entities.backend.repository.component.ComponentNodeInstanceAffinityDto;
import eu.orchestrator.backend.transfer.TOConverter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import jakarta.transaction.Transactional;

import static eu.orchestrator.repository.domain.QApplicationInstance.applicationInstance;

@Service
@Transactional(rollbackOn = Exception.class)
public class ComponentNodeInstanceService {

    private static final Logger logger = Logger.getLogger(ComponentNodeInstanceService.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Base URL of the registry search API, used to resolve the digest of a component image.
    @Value("${elasticity.registry.search.url:}")
    private String registrySearchUrl;

    @Value("${prometheus.server.url}")
    String prometheusServerURL;

    @Value("${token.signer.secret}")
    private String secretToken;

    @Value("${nfs.server.path}")
    private String rootPathOnServer;

    @Value("${nfs.path}")
    private String rootPathMaster;

    @Autowired
    private CommonService commonService;

    @Autowired
    private ApplicationInstanceService applicationInstanceService;

    @Autowired
    private ComponentNodeService componentNodeService;

    @Autowired
    private SSHKeyService sshKeyService;

    @Autowired
    private ProviderService providerService;

    @Autowired
    private Sender kafkaSender;

    @Autowired
    private ApplicationInstanceDAO applicationInstanceDAO;

    @Autowired
    private ComponentNodeInstanceDAO componentNodeInstanceDAO;

    @Autowired
    private ComponentNodeInstanceStatusDAO componentNodeInstanceStatusDAO;

    @Autowired
    private IDRuleSetInstanceDAO idRuleSetInstanceDAO;

    @Autowired
    private IDRuleSetDAO idRuleSetDAO;

    @Autowired
    private IDRuleDAO idRuleDAO;

    @Autowired
    private ComponentNodeDAO componentNodeDAO;

    @Autowired
    private PluginDAO pluginDAO;

    @Autowired
    private PluginInstanceDAO pluginInstanceDAO;

    @Autowired
    private MetricDAO metricDAO;

    @Autowired
    private ConstraintDAO constraintDAO;

    @Autowired
    private LocationInstanceDAO locationInstanceDAO;

    @Autowired
    private CountryDAO countryDAO;

    @Autowired
    private ComponentNodeInstanceHashDAO componentNodeInstanceHashDAO;

    @Autowired
    private ComponentNodeInstanceAlertDAO componentNodeInstanceAlertDAO;

    @Autowired
    private ComponentNodeInstanceIPDAO componentNodeInstanceIPDAO;

    @Autowired
    private ComponentNodeInstanceAffinityDAO componentNodeInstanceAffinityDAO;

    @Autowired
    private FlavorInstanceDAO flavorInstanceDAO;

    @Autowired
    private ServerlessPropertiesDAO serverlessPropertiesDAO;

    @Autowired
    private ServerlessPropertiesInstanceDAO serverlessPropertiesInstanceDAO;

    @Autowired
    private RequirementDAO requirementDAO;

    @Autowired
    private HealthCheckInstanceDAO healthCheckInstanceDAO;

    @Autowired
    private HealthCheckDAO healthCheckDAO;

    @Autowired
    private InterfaceInstanceDAO interfaceInstanceDAO;

    @Autowired
    private EnvironmentalVariableInstanceDAO environmentalVariableInstanceDAO;

    @Autowired
    private DeviceInstanceDAO deviceInstanceDAO;

    @Autowired
    private VolumeInstanceDAO volumeInstanceDAO;

    @Autowired
    private PrometheusService prometheusService;
    @Autowired
    private KnativeService knativeService;


    public ComponentNodeInstance fetchComponentNodeInstanceById(Long id) {
        Optional<ComponentNodeInstance> componentNodeInstanceOptional = componentNodeInstanceDAO.findById(id);
        return componentNodeInstanceOptional.orElse(null);
    }

    public ComponentNodeInstance fetchComponentNodeInstanceByHexId(String hexId) {
        Optional<ComponentNodeInstance> componentNodeInstanceOptional = componentNodeInstanceDAO.findByHexID(hexId);
        return componentNodeInstanceOptional.orElse(null);
    }

    public List<ComponentNodeInstance> fetchAllComponentNodeInstancesByApplicationInstance(ApplicationInstance applicationInstance) {
        return componentNodeInstanceDAO.findAllByApplicationInstance(applicationInstance);
    }

    public List<ComponentNodeInstance> fetchAllComponentNodeInstances() {
        return componentNodeInstanceDAO.findAll();
    }

    public List<ComponentNodeInstance> fetchAllByApplicationInstanceAndComponentNode(ApplicationInstance applicationInstance, ComponentNode componentNode) {
        return componentNodeInstanceDAO.findAllByApplicationInstanceAndComponentNode(applicationInstance, componentNode);
    }

    public void saveComponentNodeInstance(ComponentNodeInstance componentNodeInstance) {
        componentNodeInstanceDAO.save(componentNodeInstance);
    }

    public void deleteComponentNodeInstance(ComponentNodeInstance componentNodeInstance) {
        componentNodeInstanceDAO.delete(componentNodeInstance);
    }

    public List<ComponentNodeInstance> fetchAllComponentNodeInstancesBySshKey(SSHKey sshKey) {
        return componentNodeInstanceDAO.findAllBySshKey(sshKey);
    }

    // TODO move to security
    public List<IDRuleSetInstanceTO> fetchIDRuleSetInstancesByComponentNodeAndId(Long id, Long componentNodeID, User authenticatedUser) {
        ApplicationInstance existingApplicationInstance = applicationInstanceService.fetchApplicationInstanceById(id);

        List<IDRuleSetInstanceTO> idRuleSetInstanceTOs = new ArrayList<>();

        if (NullCheckUtil.isNotEmpty(existingApplicationInstance) &&
                null != componentNodeID && componentNodeID > 0 &&
                existingApplicationInstance.getStatus()
                        .equals(ApplicationInstance.ApplicationInstanceStatus.DEPLOYED.name()) &&
                (authenticatedUser.isAdmin()
                        || existingApplicationInstance.getOrganization().getId()
                        .equals(authenticatedUser.getOrganization().getId()))) {

            List<ComponentNodeInstance> componentNodeInstances = componentNodeInstanceDAO
                    .findAllByApplicationInstance(existingApplicationInstance);

            if (null != componentNodeInstances && !componentNodeInstances.isEmpty()) {

                List<ComponentNodeInstance> customComponentNodeInstances = componentNodeInstances.stream()
                        .filter(cni -> cni.getComponentNode().getComponentNodeID().equals(componentNodeID))
                        .collect(Collectors.toList());

                if (null != customComponentNodeInstances && !customComponentNodeInstances.isEmpty()) {

                    Map<Integer, IDRuleSet> idRuleSets = new HashMap<>();

                    customComponentNodeInstances.stream().forEach(cCNI -> {

                        List<IDRuleSetInstance> idRuleSetInstances = idRuleSetInstanceDAO
                                .findAllByComponentNodeInstanceOrderByDateCreatedDesc(cCNI);

                        if (null != idRuleSetInstances && !idRuleSetInstances.isEmpty()) {

                            idRuleSetInstances.stream().forEach(idRuleSetInstance -> {

                                IDRuleSet idRuleSet = idRuleSetDAO
                                        .findById(idRuleSetInstance.getIdRuleSet().getId()).get();

                                if (!idRuleSets.containsKey(idRuleSet.getId().intValue())) {
                                    idRuleSets.put(idRuleSet.getId().intValue(), idRuleSet);

                                    IDRuleSetInstanceTO idRuleSetInstanceTO = new IDRuleSetInstanceTO();
                                    idRuleSetInstanceTO
                                            .setRuleSetInstanceID(idRuleSetInstance.getRuleSetInstanceID());
                                    idRuleSetInstanceTO.setName(idRuleSetInstance.getName());
                                    idRuleSetInstanceTO.setIdRuleSetID(idRuleSet.getId());
                                    IDRuleSetTO idRuleSetTO = new IDRuleSetTO();
                                    idRuleSetTO.setId(idRuleSet.getId());
                                    idRuleSetTO.setName(idRuleSet.getName());
                                    idRuleSetTO.setPublicIDRuleSet(idRuleSet.getPublicIDRuleSet());
                                    idRuleSetInstanceTO.setIdRuleSet(idRuleSetTO);
                                    idRuleSetInstanceTOs.add(idRuleSetInstanceTO);

                                }

                            });

                        }

                    });
                }
            }
        }
        return idRuleSetInstanceTOs;
    }

    // TODO move to security
    public void updateIDRuleSetInstancesByComponentNodeAndId(Long id, Long componentNodeID, List<IDRuleSetInstance> idRuleSetInstances,
            User authenticatedUser) {

        ApplicationInstance existingApplicationInstance = applicationInstanceService.fetchApplicationInstanceById(id);

        if (NullCheckUtil.isNotEmpty(existingApplicationInstance) &&
                null != componentNodeID && componentNodeID > 0 &&
                existingApplicationInstance.getStatus()
                        .equals(ApplicationInstance.ApplicationInstanceStatus.DEPLOYED.name()) &&
                (Boolean.TRUE.equals(existingApplicationInstance.hasEditAllowance(authenticatedUser)))) {

            List<ComponentNodeInstance> componentNodeInstances = componentNodeInstanceDAO
                    .findAllByApplicationInstance(existingApplicationInstance);

            if (null != componentNodeInstances && !componentNodeInstances.isEmpty()) {

                List<IDRuleSetInstance> newIDRuleSetInstances = new ArrayList<>();

                List<ComponentNodeInstance> customComponentNodeInstances = componentNodeInstances.stream()
                        .filter(cni -> cni.getComponentNode().getComponentNodeID().equals(componentNodeID))
                        .collect(Collectors.toList());

                if (null != customComponentNodeInstances && !customComponentNodeInstances.isEmpty()) {

                    for (ComponentNodeInstance existingComponentNodeInstance : customComponentNodeInstances) {

                        boolean notifyAgent = false;
                        if (null != existingComponentNodeInstance.getStatusIDS()
                                && existingComponentNodeInstance.getStatusIDS().booleanValue()) {
                            if (null != idRuleSetInstances && !idRuleSetInstances.isEmpty()) {
                                idRuleSetInstances.stream().forEach(idRuleSetInstance -> {
                                    if (null != idRuleSetInstance.getIdRuleSet()
                                            && idRuleSetInstance.getIdRuleSet().getId() >= 0L) {
                                        IDRuleSet idRuleSet = idRuleSetDAO
                                                .findById(idRuleSetInstance.getIdRuleSet().getId()).get();
                                        if (idRuleSetInstanceDAO
                                                .findByNameAndComponentNodeInstance(idRuleSet.getName(),
                                                        existingComponentNodeInstance).isPresent()) {
                                            IDRuleSetInstance existingIDRuleSetInstance = idRuleSetInstanceDAO
                                                    .findByNameAndComponentNodeInstance(idRuleSet.getName(),
                                                            existingComponentNodeInstance).get();
                                            newIDRuleSetInstances.add(existingIDRuleSetInstance);
                                        } else {
                                            IDRuleSetInstance newIdRuleSetInstance = new IDRuleSetInstance();

                                            newIdRuleSetInstance
                                                    .setComponentNodeInstance(existingComponentNodeInstance);
                                            newIdRuleSetInstance.setName(idRuleSet.getName());
                                            newIdRuleSetInstance.setDateCreated(new Date());
                                            newIdRuleSetInstance.setLastModified(new Date());
                                            newIdRuleSetInstance.setIdRuleSet(idRuleSet);

                                            idRuleSetInstanceDAO.save(newIdRuleSetInstance);
                                            newIDRuleSetInstances.add(newIdRuleSetInstance);
                                        }
                                    }
                                });
                                List<IDRuleSetInstance> existingIDRuleSetInstances = idRuleSetInstanceDAO
                                        .findAllByComponentNodeInstanceOrderByDateCreatedDesc(
                                                existingComponentNodeInstance);
                                if (null != existingIDRuleSetInstances && !existingIDRuleSetInstances.isEmpty()) {
                                    existingIDRuleSetInstances.stream().forEach(existingIDRuleSetInstance -> {
                                        if (newIDRuleSetInstances.stream().filter(
                                                        idRuleSetInstance -> idRuleSetInstance.getRuleSetInstanceID()
                                                                .equals(existingIDRuleSetInstance.getRuleSetInstanceID()))
                                                .collect(Collectors.toList()).isEmpty()) {
                                            idRuleSetInstanceDAO.delete(existingIDRuleSetInstance);
                                        }
                                    });
                                }
                                notifyAgent = true;

                            } else {
                                // Delete all id rule set instances
                                List<IDRuleSetInstance> existingIDRuleSetInstances = idRuleSetInstanceDAO
                                        .findAllByComponentNodeInstanceOrderByDateCreatedDesc(existingComponentNodeInstance);
                                if (null != existingIDRuleSetInstances && !existingIDRuleSetInstances.isEmpty()) {
                                    existingIDRuleSetInstances.forEach(existingIDRuleSetInstance -> idRuleSetInstanceDAO.delete(existingIDRuleSetInstance));
                                }
                                notifyAgent = true;
                            }

                        } else {
                            // IDS disabled, delete all id rule set instances
                            List<IDRuleSetInstance> existingIDRuleSetInstances = idRuleSetInstanceDAO
                                    .findAllByComponentNodeInstanceOrderByDateCreatedDesc(existingComponentNodeInstance);
                            if (null != existingIDRuleSetInstances && !existingIDRuleSetInstances.isEmpty()) {
                                existingIDRuleSetInstances.forEach(existingIDRuleSetInstance -> idRuleSetInstanceDAO.delete(existingIDRuleSetInstance));
                            }
                            existingComponentNodeInstance.setLastModified(new Date());
                            componentNodeInstanceDAO.save(existingComponentNodeInstance);
                            notifyAgent = true;
                        }
                        // Notify Agent that rules have been updated
                        if (notifyAgent) {
                            try {
                                // Send new ID Rule Set Instances to Agent
                                IDSConfiguration idsConfigurationMSG = new IDSConfiguration();
                                idsConfigurationMSG.setGraphInstanceHexId(existingApplicationInstance.getHexID());
                                idsConfigurationMSG
                                        .setGraphHexId(existingApplicationInstance.getApplication().getHexID());
                                idsConfigurationMSG
                                        .setComponentNodeInstanceHexId(existingComponentNodeInstance.getHexID());
                                idsConfigurationMSG.setComponentNodeHexId(
                                        existingComponentNodeInstance.getComponentNode().getHexID());

                                List<String> rules = new ArrayList<>();

                                if (!newIDRuleSetInstances.isEmpty()) {
                                    newIDRuleSetInstances.stream().forEach(iRSI -> {
                                        IDRuleSet idRuleSet = idRuleSetDAO
                                                .findById(iRSI.getIdRuleSet().getId())
                                                .get();
                                        List<IDRule> idRules = idRuleDAO
                                                .findAllByIdRuleSetOrderByDateCreated(idRuleSet, null).getContent();
                                        if (null != idRules && !idRules.isEmpty()) {
                                            idRules.stream().forEach(idRule -> {
                                                String rule = idRule.getName();
                                                if (!rules.contains(rule)) {
                                                    rules.add(rule);
                                                }
                                            });
                                        }
                                    });
                                }
                                idsConfigurationMSG.setRules(rules);
                                kafkaSender.sendIDRulesToAgent(objectMapper.writeValueAsString(idsConfigurationMSG));
                            } catch (Exception e) {
                                e.printStackTrace();
                                logger.log(Level.SEVERE, e.getMessage(), e);
                            }
                        }
                    }
                }
            }
        }
    }

    // TODO move to elasticity
    public List<String> fetchMetricsByComponentNodeHexID(Long id, String componentNodeHexID, User authenticatedUser) {
        ApplicationInstance existingApplicationInstance = applicationInstanceService.fetchApplicationInstanceById(id);

        List<String> metrics = new ArrayList<>();

        if (NullCheckUtil.isNotEmpty(existingApplicationInstance) &&
                null != componentNodeHexID && !componentNodeHexID.isEmpty() &&
                existingApplicationInstance.getStatus()
                        .equals(ApplicationInstance.ApplicationInstanceStatus.DEPLOYED.name()) &&
                (authenticatedUser.isAdmin()
                        || existingApplicationInstance.getOrganization().getId()
                        .equals(authenticatedUser.getOrganization().getId()))) {

            Optional<ComponentNode> componentNodeOP = componentNodeDAO.findByHexID(componentNodeHexID);

            if (componentNodeOP.isPresent()) {

                ComponentNode componentNode = componentNodeOP.get();

                // K8s metrics START
                if (NullCheckUtil.isNotEmpty(existingApplicationInstance.getProvider())
                        && NullCheckUtil.isNotEmpty(existingApplicationInstance.getProvider().getProviderType())
                        && ProviderType.ProviderName.KUBERNETES.name()
                        .equalsIgnoreCase(existingApplicationInstance.getProvider().getProviderType().getName())) {
                    Optional<ComponentNodeInstance> componentNodeInstanceOptional = componentNodeInstanceDAO.
                            findByComponentNodeAndApplicationInstance(componentNode, existingApplicationInstance);
                    if (componentNodeInstanceOptional.isPresent()) {
                        ComponentNodeInstance cni = componentNodeInstanceOptional.get();
                        metrics.addAll(prometheusService.retrieveMetricsForCNI(cni.getHexID()));
                    }
                }
                // k8s metrics END
                else {
                    List<ComponentNodeInstance> componentNodeInstances = componentNodeInstanceDAO
                            .findAllByApplicationInstance(existingApplicationInstance);

                    if (null != componentNodeInstances && !componentNodeInstances.isEmpty()
                            && !componentNodeInstances.stream().filter(cni -> cni.getComponentNode().getComponentNodeID()
                            .equals(componentNode.getComponentNodeID())).collect(Collectors.toList()).isEmpty()) {

                        List<ComponentNodeInstance> filteredCNIs = componentNodeInstances.stream().filter(
                                cni -> cni.getComponentNode().getComponentNodeID()
                                        .equals(componentNode.getComponentNodeID())).collect(Collectors.toList());
                        //TODO for Matilda 5G
                        boolean hasPublicInterface = filteredCNIs.get(0).getInterfaceInstances().stream()
                                .anyMatch(interfaceInstance -> interfaceInstance.getInterfaceType().compareTo("ACCESS") == 0);
                        boolean isMatildaProvider = existingApplicationInstance.getProvider().getName().compareTo("MATILDA-enabled Telco Provider") == 0;
                        if (isMatildaProvider && hasPublicInterface) {
                            metrics.add("inin_ping_rtt_ms");
                        }

                        filteredCNIs.stream().forEach(cni -> {
                            List<PluginInstance> pluginInstances = pluginInstanceDAO
                                    .findAllByComponentNodeInstanceOrderByDateCreatedDesc(cni);
                            if (null != pluginInstances && !pluginInstances.isEmpty()) {
                                pluginInstances.stream().forEach(pluginInstance -> {
                                    if (!pluginInstance.getDeletedPlugin().booleanValue()) {
                                        Plugin plugin = pluginDAO.findById(pluginInstance.getPlugin().getPluginID())
                                                .get();
                                        List<Metric> pluginMetrics = metricDAO
                                                .findAllByPluginOrderByDateCreatedDesc(plugin);
                                        if (null != pluginMetrics && !pluginMetrics.isEmpty()) {
                                            pluginMetrics.stream().forEach(mt -> {
                                                if (!metrics
                                                        .contains(mt.getName().concat(" (").concat(mt.getUnit()).concat(")"))) {
                                                    metrics.add(mt.getName().concat(" (").concat(mt.getUnit()).concat(")"));
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        });
                    }
                }
                metrics.sort((o1, o2) -> o1.compareTo(o2));
            }
        }

        return metrics;
    }

    // TODO move to elasticity
    public List<String> fetchDimensionsByComponentNodeHexIDAndMetricNameAndId(Long id, String componentNodeHexID, String metricName, User authenticatedUser) {
        ApplicationInstance existingApplicationInstance = applicationInstanceService.fetchApplicationInstanceById(id);

        List<String> dimensions = new ArrayList<>();

        if (NullCheckUtil.isNotEmpty(existingApplicationInstance) &&
                null != componentNodeHexID && !componentNodeHexID.isEmpty() &&
                existingApplicationInstance.getStatus()
                        .equals(ApplicationInstance.ApplicationInstanceStatus.DEPLOYED.name()) &&
                (authenticatedUser.isAdmin()
                        || existingApplicationInstance.getOrganization().getId()
                        .equals(authenticatedUser.getOrganization().getId()))) {

            Optional<ComponentNode> componentNodeOP = componentNodeDAO.findByHexID(componentNodeHexID);

            if (componentNodeOP.isPresent()) {
                ComponentNode componentNode = componentNodeOP.get();
                // K8s dimensions START
                if (NullCheckUtil.isNotEmpty(existingApplicationInstance.getProvider())
                        && NullCheckUtil.isNotEmpty(existingApplicationInstance.getProvider().getProviderType())
                        && ProviderType.ProviderName.KUBERNETES.name()
                        .equalsIgnoreCase(existingApplicationInstance.getProvider().getProviderType().getName())) {

                    Optional<ComponentNodeInstance> componentNodeInstanceOptional = componentNodeInstanceDAO.
                            findByComponentNodeAndApplicationInstance(componentNode, existingApplicationInstance);
                    if (componentNodeInstanceOptional.isPresent()) {
                        ComponentNodeInstance cni = componentNodeInstanceOptional.get();
                        dimensions.addAll(prometheusService.retrieveDimensionsForMetricAndCNI(metricName, cni.getHexID()));
                    }
                }// k8s dimensions END
                else {

                    Application existingApplication = existingApplicationInstance.getApplication();
                    //TODO for Matilda 5G
                    boolean isMatildaProvider = existingApplicationInstance.getProvider().getName().compareTo("MATILDA-enabled Telco Provider") == 0;
                    boolean isMatildaMetric = metricName.compareTo("inin_ping_rtt_ms") == 0;
                    if (isMatildaProvider && isMatildaMetric) {
                        //String[] temp = {"hash='ad54ebdb1c60292b931e82c04f59928cf8ace0b7'"};
                        return Arrays.asList("hash='ad54ebdb1c60292b931e82c04f59928cf8ace0b7'");
                    }
                    dimensions = PrometheusUtil
                            .retrieveDimensions(existingApplication.getHexID(),
                                    existingApplicationInstance.getHexID(), componentNode.getHexID(), metricName,
                                    prometheusServerURL);

                }
            }
        }

        if (!dimensions.isEmpty()) {
            dimensions.sort(Comparator.naturalOrder());
        }

        return dimensions;
    }


    public ComponentNodeInstanceTO fetchComponentNodeInstanceInfoById(Long id, Long cniID, User authenticatedUser) {
        ApplicationInstance existingApplicationInstance = applicationInstanceService.fetchApplicationInstanceById(id);
        ComponentNodeInstance componentNodeInstance = fetchComponentNodeInstanceById(cniID);

        if (NullCheckUtil.isEmpty(existingApplicationInstance) || NullCheckUtil.isEmpty(componentNodeInstance)) {
            throw new GenericBusinessException(GenericMessage.APPLICATION_INSTANCE_NOT_EXIST.getCode(), GenericMessage.APPLICATION_INSTANCE_NOT_EXIST);
        }
        // Check if CNI belongs to this Application Instance and to this organization
        if ((authenticatedUser.isAdmin() || existingApplicationInstance.getOrganization().getId().equals(authenticatedUser.getOrganization().getId()))
                && componentNodeInstance.getApplicationInstance().getApplicationInstanceID().equals(existingApplicationInstance.getApplicationInstanceID())) {
            // Create ComponentNodeInstanceTO Object for showing modal
            return commonService.convertComponentNodeInstanceToTO(componentNodeInstance);
        } else {
            throw new NotAuthorizedException(GenericMessage.APPLICATION_INSTANCE_NOT_AUTHORIZED.getCode(), GenericMessage.APPLICATION_INSTANCE_NOT_AUTHORIZED);
        }
    }

    public List<ComponentNodeInstance> retrieveAllComponentNodeInstances(Long id) {
        ApplicationInstance existingApplicationInstance = applicationInstanceService.fetchApplicationInstanceById(id);
        List<ComponentNodeInstance> componentNodeInstanceList = new ArrayList<>();

        if (NullCheckUtil.isNotEmpty(existingApplicationInstance)) {
            componentNodeInstanceList = componentNodeInstanceDAO.findAllByApplicationInstance(existingApplicationInstance);
        }
        return componentNodeInstanceList;
    }

    public Page fetchComponentNodeInstanceList(Pageable pageable, ApplicationInstance fApplicationInstance, Long id, User authenticatedUser) {

        Page<ComponentNodeInstance> page;
        Optional<ApplicationInstance> applicationInstance1 = applicationInstanceDAO.findById(id);

        if (applicationInstance1.isPresent()) {
            if (pageable.getPageSize() > 100) {
                page = componentNodeInstanceDAO.findAllByApplicationInstance(applicationInstance1.get(), PageRequest.of(pageable.getPageNumber(), 100, pageable.getSort()));
            } else {
                page = componentNodeInstanceDAO.findAllByApplicationInstance(applicationInstance1.get(), pageable);
            }
            return new TOConverter(ComponentNodeInstance.class.getName(), page, pageable, authenticatedUser).convertToTOWithPermissions();
        } else {
            logger.log(Level.WARNING, "Application instance not found, not able to retrieve component node instances" );
            return null;
        }
    }


    public void updateComponentNodeInstanceInfoById(Long id, Long cniID, ComponentNodeInstanceTO componentNodeInstanceTO, User authenticatedUser) {
        ApplicationInstance applicationInstance = applicationInstanceService.fetchApplicationInstanceById(id);
        ComponentNodeInstance componentNodeInstance = fetchComponentNodeInstanceById(cniID);

        if (NullCheckUtil.isEmpty(applicationInstance) || NullCheckUtil.isEmpty(componentNodeInstance)) {
            throw new GenericBusinessException(GenericMessage.APPLICATION_INSTANCE_NOT_EXIST.getCode(), GenericMessage.APPLICATION_INSTANCE_NOT_EXIST);
        }
        // Check if CNI belongs to this Application Instance and to this organization
        if (Boolean.TRUE.equals(applicationInstance.hasEditAllowance(authenticatedUser))
                && componentNodeInstance.getApplicationInstance().getApplicationInstanceID().equals(applicationInstance.getApplicationInstanceID())) {

            List<Constraint> applicationInstanceConstraints = constraintDAO.findAllByApplicationInstance(applicationInstance);
            List<Constraint> componentNodeInstanceConstraints
                    = constraintDAO.findAllByApplicationInstanceAndComponentNodeInstance(applicationInstance, componentNodeInstance);

            // SSH Key
            if (null != componentNodeInstanceTO.getSshKey() && null != componentNodeInstanceTO.getSshKey().getId()
                    && componentNodeInstanceTO.getSshKey().getId() > 0
                    && NullCheckUtil.isNotEmpty(sshKeyService.fetchSshKeyById(componentNodeInstanceTO.getSshKey().getId()))) {
                componentNodeInstance.setSshKey(sshKeyService.fetchSshKeyById(componentNodeInstanceTO.getSshKey().getId()));
            }

            // Provider
            if (null != componentNodeInstanceTO.getProvider() && null != componentNodeInstanceTO.getProvider().getProviderID()
                    && componentNodeInstanceTO.getProvider().getProviderID() > 0
                    && NullCheckUtil.isNotEmpty(providerService.findById(componentNodeInstanceTO.getProvider().getProviderID()))) {

                Provider newProvider = providerService.findById(componentNodeInstanceTO.getProvider().getProviderID());
                componentNodeInstance.setProvider(newProvider);

                ApplicationInstance existingApplicationInstance = applicationInstanceService.fetchApplicationInstanceById(
                        componentNodeInstance.getApplicationInstance().getApplicationInstanceID());

        /*if (newProvider.getProviderType().getName()
            .equals(ProviderType.ProviderName.USER_DEFINED.name())
            || newProvider.getProviderID() != existingApplicationInstance.getProvider()
            .getProviderID()) {

          existingApplicationInstance
              .setProvider(providerDAO.findByName("User-defined").get());
          existingApplicationInstance.setLastModified(new Date());
          applicationInstanceDAO.save(existingApplicationInstance);


        } else if (newProvider.getProviderType().getName()
            .equals(ProviderType.ProviderName.POLICY_DEFINED.name())) {

          existingApplicationInstance
              .setProvider(providerDAO.findByName("Policy-defined").get());
          existingApplicationInstance.setLastModified(new Date());
          applicationInstanceDAO.save(existingApplicationInstance);

        }*/

            } else if (NullCheckUtil.isNotEmpty(componentNodeInstanceTO.getProvider()) && NullCheckUtil.isNotEmpty(
                    componentNodeInstanceTO.getProvider().getProviderID()) &&
                    (componentNodeInstanceTO.getProvider().getProviderID().equals(providerService.findByName("User-defined").getProviderID())
                            || componentNodeInstanceTO.getProvider().getProviderID().equals(providerService.findByName("Policy-defined").getProviderID()))) {

                Provider newProvider = providerService.findById(componentNodeInstanceTO.getProvider().getProviderID());
                componentNodeInstance.setProvider(newProvider);

                ApplicationInstance existingApplicationInstance = applicationInstanceService
                        .fetchApplicationInstanceById(componentNodeInstance.getApplicationInstance().getApplicationInstanceID());
                existingApplicationInstance.setProvider(componentNodeInstance.getProvider());
                applicationInstanceService.saveApplicationInstance(applicationInstance);

            } else {
                throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(), GenericMessage.REQUIRED_FIELDS_MISSING);
            }
            // Regions
            if (null != componentNodeInstanceTO.getLocationInstances() && !componentNodeInstanceTO.getLocationInstances().isEmpty()) {
                // New Location Instance
                LocationInstanceTO locationInstance = componentNodeInstanceTO.getLocationInstances().get(0);
                List<LocationInstance> locationInstances = locationInstanceDAO.findAllByComponentNodeInstance(componentNodeInstance);
                if (null != locationInstances && !locationInstances.isEmpty()) {

                    LocationInstance existingLocationInstance = locationInstances.get(0);

                    boolean rF = null != locationInstance.getCountry() || (null != locationInstance.getRegion() && !locationInstance.getRegion().isEmpty());
                    if (rF) {
                        existingLocationInstance.setLastModified(new Date());
                        existingLocationInstance.setRegion(locationInstance.getRegion());
                        locationInstanceDAO.save(existingLocationInstance);
                        // Update region constraints
                        if (null != applicationInstanceConstraints && !applicationInstanceConstraints.isEmpty()) {
                            applicationInstanceConstraints.stream().filter(constraint -> null != constraint.getComponentNodeInstance()
                                    && constraint.getComponentNodeInstance().getComponentNodeInstanceID()
                                    .equals(componentNodeInstance.getComponentNodeInstanceID())
                                    && constraint.getConstraintMetric()
                                    .equals(Constraint.ConstraintMetric.REGION.name())).forEach(constraint -> {
                                constraint.setConstraintValue(locationInstance.getRegion());
                                constraint.setConstraintUnit(ConstantsUtil.REGION);
                                constraint.setLastModified(new Date());
                                constraintDAO.save(constraint);
                                // TODO Handle exception
                            });
                        } else {
                            Constraint regionConstraint = new Constraint();
                            regionConstraint.setComponentNodeInstance(componentNodeInstance);
                            regionConstraint.setConstraintCategory(Constraint.ConstraintCategory.COMPONENT_HOSTING.name());
                            regionConstraint.setConstraintType(Constraint.ConstraintType.HARD.name());
                            regionConstraint.setConstraintMetric(Constraint.ConstraintMetric.REGION.name());
                            regionConstraint.setDeletableConstraint(false);
                            regionConstraint.setConstraintValue(locationInstance.getRegion() + "");
                            regionConstraint.setConstraintUnit(ConstantsUtil.REGION);
                            regionConstraint.setCountry(null);
                            regionConstraint.setGraphLinkNodeInstance(null);
                            regionConstraint.setInterfaceInstance(null);
                            regionConstraint.setQi(null);
                            regionConstraint.setDateCreated(new Date());
                            regionConstraint.setLastModified(new Date());
                            regionConstraint.setApplicationInstance(applicationInstance);
                            constraintDAO.save(regionConstraint);
                        }
                    } else {
                        throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(), GenericMessage.REQUIRED_FIELDS_MISSING);
                    }
                } else {
                    boolean rF = null != locationInstance.getCountry() || (null != locationInstance.getRegion() && !locationInstance.getRegion().isEmpty());
                    if (rF) {
                        LocationInstance newLocationInstance = new LocationInstance();
                        newLocationInstance.setComponentNodeInstance(componentNodeInstance);
                        newLocationInstance.setCountry(null != locationInstance.getCountry() ? countryDAO
                                .findById(locationInstance.getCountry().getId()).get() : null);
                        newLocationInstance.setDateCreated(new Date());
                        newLocationInstance.setLastModified(new Date());
                        locationInstanceDAO.save(newLocationInstance);

                        if (null != applicationInstanceConstraints && !applicationInstanceConstraints.isEmpty()) {
                            applicationInstanceConstraints.stream().filter(constraint -> null != constraint.getComponentNodeInstance()
                                    && constraint.getComponentNodeInstance().getComponentNodeInstanceID()
                                    .equals(componentNodeInstance.getComponentNodeInstanceID())
                                    && constraint.getConstraintMetric()
                                    .equals(Constraint.ConstraintMetric.REGION.name())).forEach(constraint -> {
                                constraint.setConstraintValue(locationInstance.getRegion());
                                constraint.setConstraintUnit(ConstantsUtil.REGION);
                                constraint.setLastModified(new Date());
                                constraintDAO.save(constraint);
                                // TODO Handle exception
                            });
                        } else {
                            Constraint regionConstraint = new Constraint();
                            regionConstraint.setComponentNodeInstance(componentNodeInstance);
                            regionConstraint.setConstraintCategory(Constraint.ConstraintCategory.COMPONENT_HOSTING.name());
                            regionConstraint.setConstraintType(Constraint.ConstraintType.HARD.name());
                            regionConstraint.setConstraintMetric(Constraint.ConstraintMetric.REGION.name());
                            regionConstraint.setDeletableConstraint(false);
                            regionConstraint.setConstraintValue(locationInstance.getRegion() + "");
                            regionConstraint.setConstraintUnit(ConstantsUtil.REGION);
                            regionConstraint.setCountry(null);
                            regionConstraint.setGraphLinkNodeInstance(null);
                            regionConstraint.setInterfaceInstance(null);
                            regionConstraint.setQi(null);
                            regionConstraint.setDateCreated(new Date());
                            regionConstraint.setLastModified(new Date());
                            regionConstraint.setApplicationInstance(applicationInstance);
                            constraintDAO.save(regionConstraint);
                        }
                    }
                }
            } else {
                throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(), GenericMessage.REQUIRED_FIELDS_MISSING);
            }

            // Minimum/Maximum Workers
            if (null != componentNodeInstanceTO.getMinimumWorkers() && componentNodeInstanceTO.getMinimumWorkers() >= 1
                    && null != componentNodeInstanceTO.getMaximumWorkers() && componentNodeInstanceTO.getMaximumWorkers() >= 1) {
                if (componentNodeInstanceTO.getMinimumWorkers() > componentNodeInstanceTO.getMaximumWorkers()) {
                    throw new GenericBusinessException(GenericMessage.MINIMUM_WORKERS_GREATER_THAN_MAXIMUM_WORKERS.getCode(),
                            GenericMessage.MINIMUM_WORKERS_GREATER_THAN_MAXIMUM_WORKERS);
                }
                if (!componentNodeInstanceTO.getMinimumWorkers().equals(componentNodeInstance.getMinimumWorkers())) {
                    componentNodeInstance.setMinimumWorkers(componentNodeInstanceTO.getMinimumWorkers());
                    if (null != componentNodeInstanceConstraints && !componentNodeInstanceConstraints.isEmpty()) {

                        componentNodeInstanceConstraints.forEach(constraint -> {
                            if (constraint.getConstraintMetric().equals(Constraint.ConstraintMetric.MIN_WORKERS.name())) {
                                constraint.setConstraintValue(componentNodeInstance.getMinimumWorkers() + "");
                                constraint.setConstraintUnit(ConstantsUtil.AMOUNT);
                                constraint.setLastModified(new Date());
                                constraintDAO.save(constraint);
                            }
                        });
                    } else {
                        Constraint minWorkersConstraint = new Constraint();
                        minWorkersConstraint.setComponentNodeInstance(componentNodeInstance);
                        minWorkersConstraint.setConstraintCategory(Constraint.ConstraintCategory.COMPONENT_HOSTING.name());
                        minWorkersConstraint.setConstraintType(Constraint.ConstraintType.HARD.name());
                        minWorkersConstraint.setConstraintMetric(Constraint.ConstraintMetric.MIN_WORKERS.name());
                        minWorkersConstraint.setDeletableConstraint(false);
                        minWorkersConstraint.setConstraintValue(componentNodeInstance.getMinimumWorkers() + "");
                        minWorkersConstraint.setConstraintUnit(ConstantsUtil.AMOUNT);
                        minWorkersConstraint.setCountry(null);
                        minWorkersConstraint.setGraphLinkNodeInstance(null);
                        minWorkersConstraint.setInterfaceInstance(null);
                        minWorkersConstraint.setQi(null);
                        minWorkersConstraint.setDateCreated(new Date());
                        minWorkersConstraint.setLastModified(new Date());
                        minWorkersConstraint.setApplicationInstance(applicationInstance);
                        constraintDAO.save(minWorkersConstraint);
                    }
                }

                if (!componentNodeInstanceTO.getMaximumWorkers().equals(componentNodeInstance.getMaximumWorkers())) {
                    componentNodeInstance.setMaximumWorkers(componentNodeInstanceTO.getMaximumWorkers());

                    if (null != componentNodeInstanceConstraints && !componentNodeInstanceConstraints.isEmpty()) {

                        componentNodeInstanceConstraints.stream().forEach(constraint -> {
                            if (constraint.getConstraintMetric().equals(Constraint.ConstraintMetric.MAX_WORKERS.name())) {
                                constraint.setConstraintValue(componentNodeInstance.getMaximumWorkers() + "");
                                constraint.setConstraintUnit(ConstantsUtil.AMOUNT);
                                constraint.setLastModified(new Date());
                                constraintDAO.save(constraint);
                            }
                        });
                    } else {
                        Constraint maxWorkersConstraint = new Constraint();
                        maxWorkersConstraint.setComponentNodeInstance(componentNodeInstance);
                        maxWorkersConstraint.setConstraintCategory(Constraint.ConstraintCategory.COMPONENT_HOSTING.name());
                        maxWorkersConstraint.setConstraintType(Constraint.ConstraintType.HARD.name());
                        maxWorkersConstraint.setConstraintMetric(Constraint.ConstraintMetric.MAX_WORKERS.name());
                        maxWorkersConstraint.setDeletableConstraint(false);
                        maxWorkersConstraint.setConstraintValue(componentNodeInstance.getMaximumWorkers() + "");
                        maxWorkersConstraint.setConstraintUnit(ConstantsUtil.AMOUNT);
                        maxWorkersConstraint.setCountry(null);
                        maxWorkersConstraint.setGraphLinkNodeInstance(null);
                        maxWorkersConstraint.setInterfaceInstance(null);
                        maxWorkersConstraint.setQi(null);
                        maxWorkersConstraint.setDateCreated(new Date());
                        maxWorkersConstraint.setLastModified(new Date());
                        maxWorkersConstraint.setApplicationInstance(applicationInstance);
                        constraintDAO.save(maxWorkersConstraint);
                    }
                }
            }
            // IPS
            if (null != componentNodeInstanceTO.getStatusIPS()) {
                componentNodeInstance.setStatusIPS(componentNodeInstanceTO.getStatusIPS());
            } else {
                componentNodeInstance.setStatusIPS(componentNodeInstance.getStatusIPS());
            }
            // SOC
            if (null != componentNodeInstanceTO.getStatusSOC()) {
                componentNodeInstance.setStatusSOC(componentNodeInstanceTO.getStatusSOC());
            } else {
                componentNodeInstance.setStatusSOC(componentNodeInstance.getStatusSOC());
            }
            // IDS
            if (null != componentNodeInstanceTO.getStatusIDS() || !componentNodeInstance.getStatusIDS().booleanValue()) {
                componentNodeInstance.setStatusIDS(componentNodeInstanceTO.getStatusIDS());

                if (null != componentNodeInstanceTO.getiDRuleSetInstances() && !componentNodeInstanceTO.getiDRuleSetInstances().isEmpty()) {

                    List<IDRuleSetInstance> newIDRuleSetInstances = new ArrayList<>();
                    componentNodeInstanceTO.getiDRuleSetInstances().stream().forEach(idRuleSetInstance -> {
                        if (null != idRuleSetInstance.getIdRuleSet() && idRuleSetInstance.getIdRuleSet().getId() > 0L) {

                            IDRuleSet idRuleSet = idRuleSetDAO.findById(idRuleSetInstance.getIdRuleSet().getId()).get();

                            if (idRuleSetInstanceDAO.findByNameAndComponentNodeInstance(idRuleSet.getName(), componentNodeInstance).isPresent()) {
                                IDRuleSetInstance existingIDRuleSetInstance
                                        = idRuleSetInstanceDAO.findByNameAndComponentNodeInstance(idRuleSet.getName(), componentNodeInstance).get();
                                newIDRuleSetInstances.add(existingIDRuleSetInstance);
                            } else {
                                IDRuleSetInstance newIdRuleSetInstance = new IDRuleSetInstance();
                                newIdRuleSetInstance.setComponentNodeInstance(componentNodeInstance);
                                newIdRuleSetInstance.setName(idRuleSet.getName());
                                newIdRuleSetInstance.setDateCreated(new Date());
                                newIdRuleSetInstance.setLastModified(new Date());
                                newIdRuleSetInstance.setIdRuleSet(idRuleSet);
                                idRuleSetInstanceDAO.save(newIdRuleSetInstance);
                                newIDRuleSetInstances.add(newIdRuleSetInstance);
                            }
                        }
                    });

                    List<IDRuleSetInstance> existingIDRuleSetInstances = idRuleSetInstanceDAO
                            .findAllByComponentNodeInstanceOrderByDateCreatedDesc(componentNodeInstance);

                    if (null != existingIDRuleSetInstances && !existingIDRuleSetInstances.isEmpty()) {

                        existingIDRuleSetInstances.forEach(existingIDRuleSetInstance -> {
                            if (newIDRuleSetInstances.stream().filter(idRuleSetInstance -> idRuleSetInstance.getRuleSetInstanceID()
                                    .equals(existingIDRuleSetInstance.getRuleSetInstanceID())).collect(Collectors.toList()).isEmpty()) {
                                idRuleSetInstanceDAO.delete(existingIDRuleSetInstance);
                            }
                        });
                    }
                } else {
                    List<IDRuleSetInstance> existingIDRuleSetInstances = idRuleSetInstanceDAO
                            .findAllByComponentNodeInstanceOrderByDateCreatedDesc(componentNodeInstance);
                    if (null != existingIDRuleSetInstances && !existingIDRuleSetInstances.isEmpty()) {
                        existingIDRuleSetInstances.forEach(existingIDRuleSetInstance -> idRuleSetInstanceDAO.delete(existingIDRuleSetInstance));
                    }
                }
            } else {
                componentNodeInstance.setStatusIDS(false);
                List<IDRuleSetInstance> existingIDRuleSetInstances = idRuleSetInstanceDAO
                        .findAllByComponentNodeInstanceOrderByDateCreatedDesc(componentNodeInstance);
                if (null != existingIDRuleSetInstances && !existingIDRuleSetInstances.isEmpty()) {
                    existingIDRuleSetInstances.forEach(existingIDRuleSetInstance -> idRuleSetInstanceDAO.delete(existingIDRuleSetInstance));
                }
            }
            // Command
            if (null != componentNodeInstanceTO.getCommand() && !componentNodeInstanceTO.getCommand().isEmpty()) {
                componentNodeInstance.setCommand(componentNodeInstanceTO.getCommand());
            } else {
                componentNodeInstance.setCommand(componentNodeInstance.getCommand());
            }
            // NetworkModeHost
            if (null != componentNodeInstanceTO.getNetworkModeHost()) {
                componentNodeInstance.setNetworkModeHost(componentNodeInstanceTO.getNetworkModeHost());
            } else {
                componentNodeInstance.setNetworkModeHost(componentNodeInstance.getNetworkModeHost());
            }
            // Privilege
            if (null != componentNodeInstanceTO.getPrivilege()) {
                componentNodeInstance.setPrivilege(componentNodeInstanceTO.getPrivilege());
            } else {
                componentNodeInstance.setPrivilege(componentNodeInstance.getPrivilege());
            }
            // Hostname
            if (null != componentNodeInstanceTO.getHostname() && !componentNodeInstanceTO.getHostname().isEmpty()) {
                componentNodeInstance.setHostname(componentNodeInstanceTO.getHostname());
            } else {
                componentNodeInstance.setHostname(componentNodeInstance.getHostname());
            }
            // sharedMemorySize
            if (null != componentNodeInstanceTO.getSharedMemorySize() && !componentNodeInstanceTO.getSharedMemorySize().isEmpty()) {
                componentNodeInstance.setSharedMemorySize(componentNodeInstanceTO.getSharedMemorySize());
            } else {
                componentNodeInstance.setSharedMemorySize(componentNodeInstance.getSharedMemorySize());
            }
            // DnsEntry
            if (null != componentNodeInstanceTO.getDnsEntry() && !componentNodeInstanceTO.getDnsEntry().isEmpty()) {
                componentNodeInstance.setDnsEntry(componentNodeInstanceTO.getDnsEntry());
            } else {
                componentNodeInstance.setDnsEntry(null);
            }
            // K8s runtime class
            if (null != componentNodeInstanceTO.getKubernetesRuntimeClassName() && !componentNodeInstanceTO.getKubernetesRuntimeClassName().isEmpty()) {
                componentNodeInstance.setKubernetesRuntimeClassName(componentNodeInstanceTO.getKubernetesRuntimeClassName());
            } else {
                componentNodeInstance.setKubernetesRuntimeClassName(componentNodeInstance.getKubernetesRuntimeClassName());
            }

            //TODO for astrid
            // security enablers
            if (null != componentNodeInstanceTO.getSecurityEnablers() && !componentNodeInstanceTO.getSecurityEnablers().isEmpty()) {
                Collection<SecurityEnablers> securityEnablersCollection = new ArrayList<>();
                securityEnablersCollection.clear();
                componentNodeInstanceHashDAO.deleteAllByComponentNodeInstance(componentNodeInstance);
                componentNodeInstanceTO.getSecurityEnablers().forEach(securityEnabler -> {
                    if ((ComponentNodeInstance.SecurityEnablers.valueOf(securityEnabler)
                            .equals(ComponentNodeInstance.SecurityEnablers.CONFIGURATION_INTEGRITY_VERIFICATION)
                            || ComponentNodeInstance.SecurityEnablers.valueOf(securityEnabler)
                            .equals(ComponentNodeInstance.SecurityEnablers.RUNTIME_FILE_INTEGRITY))
                            && componentNodeInstanceHashDAO.countByComponentNodeInstance(componentNodeInstance) == 0) {

                        JsonObject dockerCredential = new JsonObject();
                        dockerCredential.addProperty("dockerRegistry", componentNodeInstance.getComponentNode().getComponent().getDockerRegistry());
                        dockerCredential.addProperty("dockerUsername", componentNodeInstance.getComponentNode().getComponent().getDockerUsername());
                        dockerCredential.addProperty("dockerPassword",
                                Util.decrypt(componentNodeInstance.getComponentNode().getComponent().getDockerPassword(), secretToken));
                        String dockerCredentialAsString = dockerCredential.toString();
                        String dockerCredentialHashing = Util.stringHash(dockerCredentialAsString);

                        ComponentNodeInstanceHash componentNodeInstanceHash = new ComponentNodeInstanceHash();
                        componentNodeInstanceHash.setDateCreated(new Date());
                        componentNodeInstanceHash.setLastModified(new Date());
                        componentNodeInstanceHash.setComponentNodeInstance(componentNodeInstance);
                        componentNodeInstanceHash.setValue(Util.encrypt(dockerCredentialHashing, secretToken));
                        componentNodeInstanceHash.setType(ComponentNodeInstanceHash.HashType.DOCKER_CREDENTIALS.name());
                        //componentNodeInstanceHashDAO.save(componentNodeInstanceHash);

                        String dockerImageHash = "";
                        if (null != componentNodeInstance.getComponentNode().getComponent().getDockerRegistry()) {
                            String username = componentNodeInstance.getComponentNode().getComponent().getDockerUsername();
                            String password = Util.decrypt(componentNodeInstance.getComponentNode().getComponent().getDockerPassword(), secretToken);
                            String registry = componentNodeInstance.getComponentNode().getComponent().getDockerRegistry();
                            String[] temp = registry.split("-");
                            registry = temp[0] + "-docker-" + temp[1];
                            String image = componentNodeInstance.getComponentNode().getComponent().getDockerImage();
                            String[] tempImage = image.split(":");
                            String imageName = tempImage[0];
                            String imageTag = tempImage[1];
                            String url = registrySearchUrl + "/service/rest/v1/search?repository=" + registry + "&name=*" + imageName;
                            RestTemplate restTemplate = new RestTemplate();
                            restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(username, password));
                            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

                            if (response.getStatusCode().is2xxSuccessful()) {

                                Gson gson = new Gson();
                                JsonObject jsonObjectBase = gson.fromJson(response.getBody(), JsonObject.class);
                                JsonArray jsonArrayOfDockerImages = jsonObjectBase.get("items").getAsJsonArray();
                                for (JsonElement dockerImage : jsonArrayOfDockerImages) {
                                    JsonObject jsonObject = dockerImage.getAsJsonObject();
                                    if ((jsonObject.get("name").getAsString().compareTo(imageName) == 0
                                            || jsonObject.get("name").getAsString().compareTo("library/" + imageName) == 0)
                                            && jsonObject.get("version").getAsString().compareTo(imageTag) == 0) {

                                        JsonArray jsonArrayAssets = jsonObject.get("assets").getAsJsonArray();
                                        JsonObject jsonObjectAssets0 = jsonArrayAssets.get(0).getAsJsonObject();
                                        JsonObject jsonObjectAssets0Checksum = jsonObjectAssets0.get("checksum").getAsJsonObject();
                                        dockerImageHash = jsonObjectAssets0Checksum.get("sha256").getAsString();
                                        break;
                                    }
                                }
                            }
                        }

                        componentNodeInstanceHash = new ComponentNodeInstanceHash();
                        componentNodeInstanceHash.setDateCreated(new Date());
                        componentNodeInstanceHash.setLastModified(new Date());
                        componentNodeInstanceHash.setComponentNodeInstance(componentNodeInstance);
                        componentNodeInstanceHash.setValue(Util.encrypt(dockerImageHash, secretToken));
                        componentNodeInstanceHash.setType(ComponentNodeInstanceHash.HashType.DOCKER_IMAGE.name());
                        //componentNodeInstanceHashDAO.save(componentNodeInstanceHash);
                    }
                    securityEnablersCollection.add(ComponentNodeInstance.SecurityEnablers.valueOf(securityEnabler));
                });
                componentNodeInstance.setSecurityEnablers(securityEnablersCollection);
            } else {
                Collection<ComponentNodeInstance.SecurityEnablers> securityEnablersCollection = new ArrayList<>();
                securityEnablersCollection.clear();
                componentNodeInstance.setSecurityEnablers(securityEnablersCollection);
            }

            // Capability Add
            if (null != componentNodeInstanceTO.getCapabilityAdds() && !componentNodeInstanceTO.getCapabilityAdds().isEmpty()) {
                //Check if there are already some add capabilities
                if (null != componentNodeInstance.getCapabilityAdds()) {
                    Collection<String> newCapabilityAdds = componentNodeInstanceTO.getCapabilityAdds();
                    Collection<Component.CapabilityAdd> exCapabilityAdds = componentNodeInstance.getCapabilityAdds();
                    exCapabilityAdds.clear();
                    newCapabilityAdds.forEach(capabilityAdd -> exCapabilityAdds.add(Component.CapabilityAdd.valueOf(capabilityAdd)));
                    componentNodeInstance.setCapabilityAdds(exCapabilityAdds);
                } else {
                    Collection<Component.CapabilityAdd> newCapabilityAdds = new ArrayList<>();
                    newCapabilityAdds.clear();
                    componentNodeInstanceTO.getCapabilityAdds().forEach(capabilityAdd -> newCapabilityAdds.add(Component.CapabilityAdd.valueOf(capabilityAdd)));
                    componentNodeInstance.setCapabilityAdds(newCapabilityAdds);
                }
            } else {
                componentNodeInstance.setCapabilityAdds(componentNodeInstance.getCapabilityAdds());
            }

            // Capability Drop
            if (null != componentNodeInstanceTO.getCapabilityDrops() && !componentNodeInstanceTO.getCapabilityDrops().isEmpty()) {
                //Check if there are already some drop capabilities
                if (null != componentNodeInstance.getCapabilityDrops()) {
                    Collection<String> newCapabilityDrops = componentNodeInstanceTO.getCapabilityDrops();
                    Collection<Component.CapabilityDrop> exCapabilityDrops = componentNodeInstance.getCapabilityDrops();
                    exCapabilityDrops.clear();
                    newCapabilityDrops.stream().forEach(capabilityDrop -> exCapabilityDrops.add(Component.CapabilityDrop.valueOf(capabilityDrop)));
                    componentNodeInstance.setCapabilityDrops(exCapabilityDrops);
                } else {
                    Collection<Component.CapabilityDrop> newCapabilityDrops = new ArrayList<>();
                    newCapabilityDrops.clear();
                    componentNodeInstanceTO.getCapabilityDrops().stream().forEach(capabilityDrop ->
                            newCapabilityDrops.add(Component.CapabilityDrop.valueOf(capabilityDrop))
                    );
                    componentNodeInstance.setCapabilityDrops(newCapabilityDrops);
                }
            } else {
                componentNodeInstance.setCapabilityDrops(componentNodeInstance.getCapabilityDrops());
            }
            // Flavor Instance
            if (null != componentNodeInstanceTO.getFlavorInstance() && flavorInstanceDAO.findByComponentNodeInstance(componentNodeInstance).isPresent()) {
                FlavorInstance existingFlavorInstance = flavorInstanceDAO.findByComponentNodeInstance(componentNodeInstance).get();
                boolean rF = null != componentNodeInstanceTO.getFlavorInstance().getRam()
                        && componentNodeInstanceTO.getFlavorInstance().getRam() > 0
                        && null != componentNodeInstanceTO.getFlavorInstance().getStorage()
                        && componentNodeInstanceTO.getFlavorInstance().getStorage() > 0
                        && null != componentNodeInstanceTO.getFlavorInstance().getvCPUs()
                        && componentNodeInstanceTO.getFlavorInstance().getvCPUs() > 0;

                if (rF) {
                    ComponentNode componentNode = componentNodeService.fetchComponentNodeById(componentNodeInstance.getComponentNode().getComponentNodeID());
                    Component component = componentNodeService.fetchComponentById(componentNode.getComponent().getId());
                    Requirement requirement = requirementDAO.findByComponent(component).get();
                    if (componentNodeInstanceTO.getFlavorInstance().getRam() < requirement.getRam()) {
                        throw new GenericBusinessException(GenericMessage.FLAVOR_LOWER_THAN_MINIMUM_EXECUTION_REQUIREMENTS.getCode(),
                                GenericMessage.FLAVOR_LOWER_THAN_MINIMUM_EXECUTION_REQUIREMENTS);
                    }
                    if (componentNodeInstanceTO.getFlavorInstance().getStorage() < requirement.getStorage()) {
                        throw new GenericBusinessException(GenericMessage.FLAVOR_LOWER_THAN_MINIMUM_EXECUTION_REQUIREMENTS.getCode(),
                                GenericMessage.FLAVOR_LOWER_THAN_MINIMUM_EXECUTION_REQUIREMENTS);
                    }
                    if (componentNodeInstanceTO.getFlavorInstance().getvCPUs() < requirement.getvCPUs()) {
                        throw new GenericBusinessException(GenericMessage.FLAVOR_LOWER_THAN_MINIMUM_EXECUTION_REQUIREMENTS.getCode(),
                                GenericMessage.FLAVOR_LOWER_THAN_MINIMUM_EXECUTION_REQUIREMENTS);
                    }
                    existingFlavorInstance.setComponentNodeInstance(componentNodeInstance);
                    existingFlavorInstance.setLastModified(new Date());
                    existingFlavorInstance.setRam(componentNodeInstanceTO.getFlavorInstance().getRam());
                    existingFlavorInstance.setStorage(componentNodeInstanceTO.getFlavorInstance().getStorage());
                    existingFlavorInstance.setvCPUs(componentNodeInstanceTO.getFlavorInstance().getvCPUs());
                    existingFlavorInstance.setServerlessEnabled(componentNodeInstanceTO.getFlavorInstance().getServerlessEnabled());
                    flavorInstanceDAO.save(existingFlavorInstance);

                    if (null != applicationInstanceConstraints && !applicationInstanceConstraints.isEmpty()) {
                        applicationInstanceConstraints.stream().filter(constraint -> null != constraint.getComponentNodeInstance() && constraint
                                        .getComponentNodeInstance().getComponentNodeInstanceID().equals(componentNodeInstance.getComponentNodeInstanceID()))
                                .forEach(constraint -> {
                                    if (constraint.getConstraintMetric().equals(Constraint.ConstraintMetric.MIN_V_CPU.name())) {
                                        constraint.setConstraintValue(existingFlavorInstance.getvCPUs() + "");
                                    } else if (constraint.getConstraintMetric().equals(Constraint.ConstraintMetric.MIN_STORAGE.name())) {
                                        constraint.setConstraintValue(existingFlavorInstance.getStorage() + "");
                                    } else if (constraint.getConstraintMetric().equals(Constraint.ConstraintMetric.MIN_RAM.name())) {
                                        constraint.setConstraintValue(existingFlavorInstance.getRam() + "");
                                    }
                                    constraint.setLastModified(new Date());
                                    constraintDAO.save(constraint);
                                    // TODO Handle generic exception
                                });
                    }
                } else {
                    throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(), GenericMessage.REQUIRED_FIELDS_MISSING);
                }
            } else {
                boolean rF = null != componentNodeInstanceTO.getFlavorInstance().getRam() && componentNodeInstanceTO.getFlavorInstance().getRam() > 0
                        && null != componentNodeInstanceTO.getFlavorInstance().getStorage() && componentNodeInstanceTO.getFlavorInstance().getStorage() > 0
                        && null != componentNodeInstanceTO.getFlavorInstance().getvCPUs() && componentNodeInstanceTO.getFlavorInstance().getvCPUs() > 0;
                if (rF) {
                    ComponentNode componentNode = componentNodeService.fetchComponentNodeById(componentNodeInstance.getComponentNode().getComponentNodeID());
                    Component component = componentNodeService.fetchComponentById(componentNode.getComponent().getId());
                    Requirement requirement = requirementDAO.findByComponent(component).get();
                    if (componentNodeInstanceTO.getFlavorInstance().getRam() < requirement.getRam()) {
                        throw new GenericBusinessException(GenericMessage.FLAVOR_LOWER_THAN_MINIMUM_EXECUTION_REQUIREMENTS.getCode(),
                                GenericMessage.FLAVOR_LOWER_THAN_MINIMUM_EXECUTION_REQUIREMENTS);
                    }
                    if (componentNodeInstanceTO.getFlavorInstance().getStorage() < requirement.getStorage()) {
                        throw new GenericBusinessException(GenericMessage.FLAVOR_LOWER_THAN_MINIMUM_EXECUTION_REQUIREMENTS.getCode(),
                                GenericMessage.FLAVOR_LOWER_THAN_MINIMUM_EXECUTION_REQUIREMENTS);
                    }
                    if (componentNodeInstanceTO.getFlavorInstance().getvCPUs() < requirement.getvCPUs()) {
                        throw new GenericBusinessException(GenericMessage.FLAVOR_LOWER_THAN_MINIMUM_EXECUTION_REQUIREMENTS.getCode(),
                                GenericMessage.FLAVOR_LOWER_THAN_MINIMUM_EXECUTION_REQUIREMENTS);
                    }
                    FlavorInstance flavorInstance = new FlavorInstance();
                    flavorInstance.setComponentNodeInstance(componentNodeInstance);
                    flavorInstance.setLastModified(new Date());
                    flavorInstance.setRam(componentNodeInstanceTO.getFlavorInstance().getRam());
                    flavorInstance.setStorage(componentNodeInstanceTO.getFlavorInstance().getStorage());
                    flavorInstance.setvCPUs(componentNodeInstanceTO.getFlavorInstance().getvCPUs());
                    flavorInstance.setServerlessEnabled(componentNodeInstanceTO.getFlavorInstance().getServerlessEnabled());
                    flavorInstanceDAO.save(flavorInstance);

                    if (null != applicationInstanceConstraints && !applicationInstanceConstraints.isEmpty()) {
                        applicationInstanceConstraints.stream().filter(constraint -> null != constraint.getComponentNodeInstance()
                                        && constraint.getComponentNodeInstance().getComponentNodeInstanceID()
                                        .equals(componentNodeInstance.getComponentNodeInstanceID()))
                                .forEach(constraint -> {
                                    if (constraint.getConstraintMetric().equals(Constraint.ConstraintMetric.MIN_V_CPU.name())) {
                                        constraint.setConstraintValue(flavorInstance.getvCPUs() + "");
                                    } else if (constraint.getConstraintMetric().equals(Constraint.ConstraintMetric.MIN_STORAGE.name())) {
                                        constraint.setConstraintValue(flavorInstance.getStorage() + "");
                                    } else if (constraint.getConstraintMetric().equals(Constraint.ConstraintMetric.MIN_RAM.name())) {
                                        constraint.setConstraintValue(flavorInstance.getRam() + "");
                                    }
                                    constraint.setLastModified(new Date());
                                    constraintDAO.save(constraint);
                                });
                    }
                } else {
                    throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(), GenericMessage.REQUIRED_FIELDS_MISSING);
                }
            }

            // Serverless Properties Instance

            if (null != componentNodeInstanceTO.getServerlessPropertiesInstance()
                    && serverlessPropertiesInstanceDAO.findByComponentNodeInstance(componentNodeInstance).isPresent()) {
                ServerlessPropertiesInstance existingServerlessPropertiesInstance = serverlessPropertiesInstanceDAO.findByComponentNodeInstance(componentNodeInstance).get();

                existingServerlessPropertiesInstance.setComponentNodeInstance(componentNodeInstance);
                existingServerlessPropertiesInstance.setLastModified(new Date());
                existingServerlessPropertiesInstance.setAutoscaler(componentNodeInstanceTO.getServerlessPropertiesInstance().getAutoscaler());

                existingServerlessPropertiesInstance.setMetric(null != componentNodeInstanceTO.getServerlessPropertiesInstance().getMetric()
                        && !componentNodeInstanceTO.getServerlessPropertiesInstance()
                        .getMetric().isEmpty()? componentNodeInstanceTO.getServerlessPropertiesInstance().getMetric() : null);

                existingServerlessPropertiesInstance.setWindowSize(null != componentNodeInstanceTO.getServerlessPropertiesInstance().getWindowSize()
                        && !componentNodeInstanceTO.getServerlessPropertiesInstance()
                        .getWindowSize().isEmpty()? componentNodeInstanceTO.getServerlessPropertiesInstance().getWindowSize() : null);

                existingServerlessPropertiesInstance.setTargetValue(null != componentNodeInstanceTO.getServerlessPropertiesInstance().getMetric()
                        && !componentNodeInstanceTO.getServerlessPropertiesInstance()
                        .getTargetValue().isEmpty()? componentNodeInstanceTO.getServerlessPropertiesInstance().getTargetValue() : null);


                existingServerlessPropertiesInstance.setMinScale(null != componentNodeInstanceTO.getServerlessPropertiesInstance().getMinScale()
                        && !componentNodeInstanceTO.getServerlessPropertiesInstance()
                        .getMinScale().isEmpty()? componentNodeInstanceTO.getServerlessPropertiesInstance().getMinScale() : null);

                existingServerlessPropertiesInstance.setMaxScale(null != componentNodeInstanceTO.getServerlessPropertiesInstance().getMaxScale()
                        && !componentNodeInstanceTO.getServerlessPropertiesInstance()
                        .getMaxScale().isEmpty()? componentNodeInstanceTO.getServerlessPropertiesInstance().getMaxScale() : null);


                serverlessPropertiesInstanceDAO.save(existingServerlessPropertiesInstance);

                // TODO (ideally) Wrap in if applicationInstance.getStatus() == DEPLOYED condition
                // ------------------------------------------------------------ //
                Map<String, String> scalingPropertiesAnnotations = new HashMap<>();
                String knativeScalingPrefix = "autoscaling.knative.dev/";
                scalingPropertiesAnnotations.put(knativeScalingPrefix + "class" , existingServerlessPropertiesInstance.getAutoscaler().toLowerCase() + ".autoscaling.knative.dev");
                scalingPropertiesAnnotations.put(knativeScalingPrefix + "metric" , existingServerlessPropertiesInstance.getMetric());
                scalingPropertiesAnnotations.put(knativeScalingPrefix + "target" , existingServerlessPropertiesInstance.getTargetValue());
                scalingPropertiesAnnotations.put(knativeScalingPrefix + "min-scale" , existingServerlessPropertiesInstance.getMinScale());
                scalingPropertiesAnnotations.put(knativeScalingPrefix + "max-scale" , existingServerlessPropertiesInstance.getMaxScale());
                scalingPropertiesAnnotations.put(knativeScalingPrefix + "window" , existingServerlessPropertiesInstance.getWindowSize() + "s");

                String knativeServiceName = KubernetesUtil.componentInstanceNameProvider(componentNodeInstance) + "-deployment";
                knativeService.updateKnativeServiceScalingProperties(applicationInstance, knativeServiceName, scalingPropertiesAnnotations);
                // ------------------------------------------------------------- //

            } else {

                ServerlessPropertiesInstance serverlessPropertiesInstance = new ServerlessPropertiesInstance();
                serverlessPropertiesInstance.setServerlessProperties(serverlessPropertiesDAO.findById(componentNodeInstanceTO.getServerlessPropertiesInstance().getServerlessProperties()
                            .getServerlessPropertiesID()).get());
                serverlessPropertiesInstance.setComponentNodeInstance(componentNodeInstance);
                serverlessPropertiesInstance.setLastModified(new Date());
                serverlessPropertiesInstance.setDateCreated(new Date());

                serverlessPropertiesInstance.setAutoscaler(componentNodeInstanceTO.getServerlessPropertiesInstance().getAutoscaler());

                serverlessPropertiesInstance.setMetric(null != componentNodeInstanceTO.getServerlessPropertiesInstance().getMetric()
                        && !componentNodeInstanceTO.getServerlessPropertiesInstance()
                        .getMetric().isEmpty()? componentNodeInstanceTO.getServerlessPropertiesInstance().getMetric() : null);

                serverlessPropertiesInstance.setWindowSize(null != componentNodeInstanceTO.getServerlessPropertiesInstance().getWindowSize()
                        && !componentNodeInstanceTO.getServerlessPropertiesInstance()
                        .getWindowSize().isEmpty()? componentNodeInstanceTO.getServerlessPropertiesInstance().getWindowSize() : null);

                serverlessPropertiesInstance.setTargetValue(null != componentNodeInstanceTO.getServerlessPropertiesInstance().getMetric()
                        && !componentNodeInstanceTO.getServerlessPropertiesInstance()
                        .getTargetValue().isEmpty()? componentNodeInstanceTO.getServerlessPropertiesInstance().getTargetValue() : null);


                serverlessPropertiesInstance.setMinScale(null != componentNodeInstanceTO.getServerlessPropertiesInstance().getMinScale()
                        && !componentNodeInstanceTO.getServerlessPropertiesInstance()
                        .getMinScale().isEmpty()? componentNodeInstanceTO.getServerlessPropertiesInstance().getMinScale() : null);

                serverlessPropertiesInstance.setMaxScale(null != componentNodeInstanceTO.getServerlessPropertiesInstance().getMaxScale()
                        && !componentNodeInstanceTO.getServerlessPropertiesInstance()
                        .getMaxScale().isEmpty()? componentNodeInstanceTO.getServerlessPropertiesInstance().getMaxScale() : null);

                serverlessPropertiesInstanceDAO.save(serverlessPropertiesInstance);
            }


            // Health Check Instance
            if (null != componentNodeInstanceTO.getHealthCheckInstance()
                    && healthCheckInstanceDAO.findByComponentNodeInstance(componentNodeInstance).isPresent()) {
                HealthCheckInstance existingHealthCheckInstance = healthCheckInstanceDAO.findByComponentNodeInstance(componentNodeInstance).get();

                boolean rF = null != componentNodeInstanceTO.getHealthCheckInstance().getInterval()
                        && (componentNodeInstanceTO.getHealthCheckInstance().getInterval().longValue() > 0)
                        && ((null != componentNodeInstanceTO.getHealthCheckInstance().getHttpURL()
                        && !componentNodeInstanceTO.getHealthCheckInstance().getHttpURL().isEmpty()) || (
                        null != componentNodeInstanceTO.getHealthCheckInstance().getArgs()
                                && !componentNodeInstanceTO.getHealthCheckInstance().getArgs().isEmpty()));
                if (rF) {
                    existingHealthCheckInstance.setComponentNodeInstance(componentNodeInstance);
                    existingHealthCheckInstance.setLastModified(new Date());
                    existingHealthCheckInstance.setInterval(componentNodeInstanceTO.getHealthCheckInstance().getInterval());
                    existingHealthCheckInstance.setHttpURL(null != componentNodeInstanceTO.getHealthCheckInstance().getHttpURL()
                            && !componentNodeInstanceTO.getHealthCheckInstance().getHttpURL().isEmpty()
                            ? componentNodeInstanceTO.getHealthCheckInstance().getHttpURL() : null);
                    existingHealthCheckInstance.setArgs(null != componentNodeInstanceTO.getHealthCheckInstance().getArgs()
                            && !componentNodeInstanceTO.getHealthCheckInstance().getArgs().isEmpty()
                            ? componentNodeInstanceTO.getHealthCheckInstance().getArgs() : null);
                    healthCheckInstanceDAO.save(existingHealthCheckInstance);
                } else {
                    throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(), GenericMessage.REQUIRED_FIELDS_MISSING);
                }
            } else {
                boolean rF = null != componentNodeInstanceTO.getHealthCheckInstance().getInterval()
                        && (componentNodeInstanceTO.getHealthCheckInstance().getInterval().longValue() > 0)
                        && ((null != componentNodeInstanceTO.getHealthCheckInstance().getHttpURL()
                        && !componentNodeInstanceTO.getHealthCheckInstance().getHttpURL().isEmpty())
                        || (null != componentNodeInstanceTO.getHealthCheckInstance().getArgs()
                        && !componentNodeInstanceTO.getHealthCheckInstance().getArgs().isEmpty()));
                if (rF) {
                    HealthCheckInstance healthCheckInstance = new HealthCheckInstance();
                    healthCheckInstance.setHealthCheck(healthCheckDAO.findById(componentNodeInstanceTO.getHealthCheckInstance().getHealthCheck()
                            .getHealthCheckID()).get());
                    healthCheckInstance.setComponentNodeInstance(componentNodeInstance);
                    healthCheckInstance.setLastModified(new Date());
                    healthCheckInstance.setDateCreated(new Date());
                    healthCheckInstance.setArgs(null != componentNodeInstanceTO.getHealthCheckInstance().getHttpURL()
                            && !componentNodeInstanceTO.getHealthCheckInstance().getHttpURL().isEmpty()
                            ? componentNodeInstanceTO.getHealthCheckInstance().getHttpURL() : null);
                    healthCheckInstance.setHttpURL(null != componentNodeInstanceTO.getHealthCheckInstance().getArgs()
                            && !componentNodeInstanceTO.getHealthCheckInstance().getArgs().isEmpty()
                            ? componentNodeInstanceTO.getHealthCheckInstance().getArgs() : null);
                    healthCheckInstanceDAO.save(healthCheckInstance);
                }
            }
            // Interface Instances
            if (null != componentNodeInstanceTO.getInterfaceInstances() && !componentNodeInstanceTO.getInterfaceInstances().isEmpty()) {
                List<InterfaceInstance> newInterfaceInstances = new ArrayList<>();
                for (InterfaceInstanceTO interfaceInstance : componentNodeInstanceTO.getInterfaceInstances()) {
                    boolean rF = null != interfaceInstance.getInterfaceInstanceID() && interfaceInstance.getInterfaceInstanceID() > 0
                            && null != interfaceInstance.getPort() && !interfaceInstance.getPort().isEmpty();
                    if (rF) {
                        // Existing interface instances
                        Optional<InterfaceInstance> existingInterfaceInstanceOP = interfaceInstanceDAO
                                .findByInterfaceInstanceID(interfaceInstance.getInterfaceInstanceID());
                        if (existingInterfaceInstanceOP.isPresent()) {
                            InterfaceInstance existingInterfaceInstance = existingInterfaceInstanceOP.get();
                            existingInterfaceInstance.setPort(interfaceInstance.getPort());
                            interfaceInstanceDAO.save(existingInterfaceInstance);
                            newInterfaceInstances.add(existingInterfaceInstance);
                        }
                    } else {
                        logger.warning("Missing interfaceInstanceID or port");
                        throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(),
                                GenericMessage.REQUIRED_FIELDS_MISSING);
                    }
                }
            }
            // Environmental Variables
            if (null != componentNodeInstanceTO.getEnvironmentalVariableInstances()
                    && !componentNodeInstanceTO.getEnvironmentalVariableInstances().isEmpty()) {
                List<EnvironmentalVariableInstance> newEnvironmentalVariableInstances = new ArrayList<>();
                for (EnvironmentalVariableInstanceTO environmentalVariableInstance : componentNodeInstanceTO.getEnvironmentalVariableInstances()) {
                    boolean rF = null != environmentalVariableInstance.getEnvironmentalVariableInstanceID()
                            && environmentalVariableInstance.getEnvironmentalVariableInstanceID() > 0
                            && null != environmentalVariableInstance.getValue()
                            && !environmentalVariableInstance.getValue().isEmpty();
                    if (rF) {
                        EnvironmentalVariableInstance existingEnvironmentalVariableInstance = environmentalVariableInstanceDAO
                                .findByEnvironmentalVariableInstanceID(environmentalVariableInstance.getEnvironmentalVariableInstanceID()).get();
                        existingEnvironmentalVariableInstance.setValue(environmentalVariableInstance.getValue());
                        environmentalVariableInstanceDAO.save(existingEnvironmentalVariableInstance);
                        newEnvironmentalVariableInstances.add(existingEnvironmentalVariableInstance);
                    } else {
                        logger.warning(ConstantsUtil.MISSING_REQUIRED_FIELDS);
                        throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(),
                                GenericMessage.REQUIRED_FIELDS_MISSING);
                    }
                }
            }
            // Device Instances
            if (null != componentNodeInstanceTO.getDeviceInstances() && !componentNodeInstanceTO.getDeviceInstances().isEmpty()) {
                List<DeviceInstance> newDeviceInstances = new ArrayList<>();
                for (DeviceInstanceTO deviceInstance : componentNodeInstanceTO.getDeviceInstances()) {
                    boolean rF = null != deviceInstance.getDeviceInstanceID() && deviceInstance.getDeviceInstanceID() > 0L
                            && null != deviceInstance.getValue() && !deviceInstance.getValue().isEmpty();
                    if (rF) {
                        DeviceInstance existingDeviceInstance = deviceInstanceDAO.findById(deviceInstance.getDeviceInstanceID()).get();
                        existingDeviceInstance.setValue(deviceInstance.getValue());
                        deviceInstanceDAO.save(existingDeviceInstance);
                        newDeviceInstances.add(existingDeviceInstance);
                    } else {
                        logger.warning(ConstantsUtil.MISSING_REQUIRED_FIELDS);
                        throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(), GenericMessage.REQUIRED_FIELDS_MISSING);
                    }
                }
            }

            // Volume Instances
            if (null != componentNodeInstanceTO.getVolumeInstances() && !componentNodeInstanceTO.getVolumeInstances().isEmpty()) {
                List<VolumeInstance> newVolumeInstances = new ArrayList<>();
                for (VolumeInstanceTO volumeInstance : componentNodeInstanceTO.getVolumeInstances()) {
                    //no volume set
                    if (volumeInstance.getHostPath().isEmpty()) {
                        continue;
                    }
                    boolean rF = null != volumeInstance.getVolumeInstanceID() && volumeInstance.getVolumeInstanceID() > 0L
                            && null != volumeInstance.getHostPath() && !volumeInstance.getHostPath().isEmpty();
                    if (rF) {
                        // Existing volume instance
                        VolumeInstance existingVolumeInstance = volumeInstanceDAO.findByVolumeInstanceID(volumeInstance.getVolumeInstanceID()).get();
                        existingVolumeInstance.setDockerPath(volumeInstance.getDockerPath());
                        String hostWithOutSpaces = volumeInstance.getHostPath().trim();
                        if (volumeInstance.getHostPath().startsWith("/")) {
                            existingVolumeInstance.setHostPath(rootPathOnServer + "/" + authenticatedUser.getOrganization().getName() + hostWithOutSpaces);
                        } else {
                            existingVolumeInstance
                                    .setHostPath(rootPathOnServer + "/" + authenticatedUser.getOrganization().getName() + "/" + hostWithOutSpaces);
                        }

                        String hostVolumePathString = existingVolumeInstance.getHostPath().replace(rootPathOnServer, rootPathMaster);
                        Path hostVolumePath = Paths.get(hostVolumePathString);

                        if (!Files.exists(hostVolumePath) || !Files.isDirectory(hostVolumePath)) {
                            throw new GenericBusinessException(GenericMessage.ERROR_ON_VOLUME_MAPPING.getCode(), GenericMessage.ERROR_ON_VOLUME_MAPPING);
                        }
                        volumeInstanceDAO.save(existingVolumeInstance);
                        newVolumeInstances.add(existingVolumeInstance);
                    } else {
                        logger.warning(ConstantsUtil.MISSING_REQUIRED_FIELDS);
                        throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(), GenericMessage.REQUIRED_FIELDS_MISSING);
                    }
                }
            }
            // Plugins
            if (null != componentNodeInstanceTO.getPluginInstances() && !componentNodeInstanceTO.getPluginInstances().isEmpty()) {
                List<PluginInstance> newPluginInstances = new ArrayList<>();
                for (PluginInstanceTO pluginInstance : componentNodeInstanceTO.getPluginInstances()) {
                    boolean rF = null != pluginInstance.getPluginInstanceID() && pluginInstance.getPluginInstanceID() > 0;
                    if (rF) {
                        PluginInstance existingPluginInstance = pluginInstanceDAO.findById(pluginInstance.getPluginInstanceID()).get();
                        existingPluginInstance.setDeletedPlugin(false);
                        newPluginInstances.add(existingPluginInstance);
                    } else {
                        logger.warning(ConstantsUtil.MISSING_REQUIRED_FIELDS);
                        throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(), GenericMessage.REQUIRED_FIELDS_MISSING);
                    }
                }
                List<PluginInstance> existingPluginInstances = pluginInstanceDAO.findAllByComponentNodeInstanceOrderByDateCreatedDesc(componentNodeInstance);

                if (null != existingPluginInstances && !existingPluginInstances.isEmpty()) {
                    existingPluginInstances.forEach(existingPluginInstance -> {
                        if (newPluginInstances.stream().filter(pluginInstance -> pluginInstance.getPluginInstanceID()
                                .equals(existingPluginInstance.getPluginInstanceID())).collect(Collectors.toList()).isEmpty()) {
                            if (!existingPluginInstance.getImmutablePlugin().booleanValue()) {
                                existingPluginInstance.setDeletedPlugin(true);
                                pluginInstanceDAO.save(existingPluginInstance);
                            }
                        }
                    });
                }
            } else {
                List<PluginInstance> existingPluginInstances = pluginInstanceDAO.findAllByComponentNodeInstanceOrderByDateCreatedDesc(componentNodeInstance);
                if (null != existingPluginInstances && !existingPluginInstances.isEmpty()) {
                    existingPluginInstances.forEach(existingPluginInstance -> {
                        if (!existingPluginInstance.getImmutablePlugin().booleanValue()) {
                            existingPluginInstance.setDeletedPlugin(true);
                            pluginInstanceDAO.save(existingPluginInstance);
                        }
                    });
                }
            }
            componentNodeInstance.setLastModified(new Date());
            componentNodeInstanceDAO.save(componentNodeInstance);
        } else {
            throw new NotAuthorizedException(GenericMessage.APPLICATION_INSTANCE_NOT_AUTHORIZED.getCode(), GenericMessage.APPLICATION_INSTANCE_NOT_AUTHORIZED);
        }
    }

    public List<ComponentNodeInstanceStatus> fetchCNIStatusesByCNIOrderByDateCreatedDesc(ComponentNodeInstance componentNodeInstance) {
        return componentNodeInstanceStatusDAO.findAllByComponentNodeInstanceOrderByDateCreatedDesc(componentNodeInstance);
    }

    public void deleteAllCNIInstanceStatuses(List<ComponentNodeInstanceStatus> componentNodeInstanceStatuses) {
        componentNodeInstanceStatusDAO.deleteAll(componentNodeInstanceStatuses);
    }

    public void saveCNIInstanceStatus(ComponentNodeInstanceStatus componentNodeInstanceStatus) {
        componentNodeInstanceStatusDAO.save(componentNodeInstanceStatus);
    }

    public List<ComponentNodeInstanceAlert> fetchAllCNIAlertsByComponentNodeInstanceOrderByDateCreatedDesc(ComponentNodeInstance componentNodeInstance) {
        return componentNodeInstanceAlertDAO.findAllByComponentNodeInstanceOrderByDateCreatedDesc(componentNodeInstance);
    }

    public void deleteAllCNIInstanceAlerts(List<ComponentNodeInstanceAlert> componentNodeInstanceAlerts) {
        componentNodeInstanceAlertDAO.deleteAll(componentNodeInstanceAlerts);
    }

    public List<ComponentNodeInstanceIP> fetchAllCNIIpsByComponentNodeInstanceOrderByDateCreatedDesc(ComponentNodeInstance componentNodeInstance) {
        return componentNodeInstanceIPDAO.findAllByComponentNodeInstanceOrderByDateCreatedDesc(componentNodeInstance);
    }

    public void deleteAllCNIIps(List<ComponentNodeInstanceIP> componentNodeInstanceIPS) {
        componentNodeInstanceIPDAO.deleteAll(componentNodeInstanceIPS);
    }

    public void saveCNIIp(ComponentNodeInstanceIP componentNodeInstanceIP) {
        componentNodeInstanceIPDAO.save(componentNodeInstanceIP);
    }

    public void saveCNIInterfaceInstance(InterfaceInstance cniInterfaceInstance) {
        interfaceInstanceDAO.save(cniInterfaceInstance);
    }

    public void saveCNIHash(ComponentNodeInstanceHash cniEnvsHash) {
        componentNodeInstanceHashDAO.save(cniEnvsHash);
    }

    public boolean createCNIAffinity(ComponentNodeInstanceAffinityDto cniAffinityDto, User authenticatedUser){

        ApplicationInstance applicationInstance = applicationInstanceService.fetchApplicationInstanceById(cniAffinityDto.getApplicationInstanceId());
        ComponentNodeInstance componentNodeInstance = fetchComponentNodeInstanceById(cniAffinityDto.getComponentNodeInstanceId());
        if(  applicationInstance != null && componentNodeInstance != null){
            if (Boolean.TRUE.equals(applicationInstance.hasEditAllowance(authenticatedUser))
                    && componentNodeInstance.getApplicationInstance().getApplicationInstanceID().equals(applicationInstance.getApplicationInstanceID())) {

                try {
                    ComponentNodeInstanceAffinity cniAffinity = new ComponentNodeInstanceAffinity();
                    cniAffinity.setApplicationInstance(applicationInstance);
                    cniAffinity.setComponentNodeInstance(componentNodeInstance);
                    cniAffinity.setAffinityLabels(cniAffinityDto.getAffinityLabels());
                    cniAffinity.setDateCreated(new Date());
                    cniAffinity.setLastModified(new Date());

                    componentNodeInstanceAffinityDAO.save(cniAffinity);
                    return true;
                }catch (Exception e){
                    logger.severe("Exception while creating Component Node Instance Affinity for appInstanceID: " + cniAffinityDto.getApplicationInstanceId()
                            + " cniID: " + cniAffinityDto.getComponentNodeInstanceId() + " with error: " + e.getMessage());
                    throw e;
                }
            } else {
                throw new NotAuthorizedException(GenericMessage.APPLICATION_INSTANCE_NOT_AUTHORIZED.getCode(), GenericMessage.APPLICATION_INSTANCE_NOT_AUTHORIZED);
            }
        }
        logger.severe("Failed to create Component Node Instance Affinity for appInstanceID: " + cniAffinityDto.getApplicationInstanceId() +
                " cniID: " + cniAffinityDto.getComponentNodeInstanceId());
        return false;
    }

    public Map<Long, ComponentNodeInstanceAffinity> fetchCNIAffinityMapByApplicationInstance(ApplicationInstance applicationInstance){
        Map<Long, ComponentNodeInstanceAffinity> cniAffinityMap = new HashMap<>();

        List<ComponentNodeInstanceAffinity> cniAffinityList = componentNodeInstanceAffinityDAO.findAllByApplicationInstance(applicationInstance);
        if (cniAffinityList != null && !cniAffinityList.isEmpty()){
            cniAffinityList.forEach( affinity -> {
                cniAffinityMap.put(affinity.getComponentNodeInstance().getComponentNodeInstanceID(), affinity);
            });
        }

        return cniAffinityMap;
    }

}
