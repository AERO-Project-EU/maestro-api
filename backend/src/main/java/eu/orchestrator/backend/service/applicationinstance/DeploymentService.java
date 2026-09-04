package eu.orchestrator.backend.service.applicationinstance;

import eu.orchestrator.backend.config.KafkaTopicConfig;
import eu.orchestrator.backend.service.k8s.KnativeService;
import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.backend.kafka.Sender;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.service.elasticity.ElasticityService;
import eu.orchestrator.backend.service.k8s.KubernetesService;
import eu.orchestrator.backend.service.k8s.RainbowService;
import eu.orchestrator.backend.service.oss.OssKubernetesService;
import eu.orchestrator.backend.service.oss.OssService;
import eu.orchestrator.backend.service.oss.ProviderNameReducer;
import eu.orchestrator.backend.service.resourceprovider.ProviderService;
import eu.orchestrator.backend.service.security.ExpertSystemService;
import eu.orchestrator.backend.service.util.CleanUpService;
import eu.orchestrator.backend.service.util.CommonService;
import eu.orchestrator.backend.transfer.ApplicationInstanceGraphTO;
import eu.orchestrator.backend.transfer.DashboardTO;
import eu.orchestrator.common.util.NullCheckUtil;
import eu.orchestrator.backend.util.ConstantsUtil;
import eu.orchestrator.backend.util.GrafanaUtil;
import eu.orchestrator.backend.util.OSSUtil;
import eu.orchestrator.backend.util.OrchestratorUtil;
import eu.orchestrator.backend.util.Util;
import eu.orchestrator.document.repository.dao.OrchestratorApplicationInstanceDAO;
import eu.orchestrator.repository.dao.ApplicationDAO;
import eu.orchestrator.repository.dao.ApplicationInstanceDAO;
import eu.orchestrator.repository.dao.ComponentDAO;
import eu.orchestrator.repository.dao.ComponentNodeDAO;
import eu.orchestrator.repository.dao.ComponentNodeInstanceAlertDAO;
import eu.orchestrator.repository.dao.ComponentNodeInstanceDAO;
import eu.orchestrator.repository.dao.ComponentNodeInstanceIPDAO;
import eu.orchestrator.repository.dao.ComponentNodeInstanceStatusDAO;
import eu.orchestrator.repository.dao.ConstraintDAO;
import eu.orchestrator.repository.dao.CountryDAO;
import eu.orchestrator.repository.dao.DeviceDAO;
import eu.orchestrator.repository.dao.DeviceInstanceDAO;
import eu.orchestrator.repository.dao.ElasticityHistoryDAO;
import eu.orchestrator.repository.dao.EnvironmentalVariableInstanceDAO;
import eu.orchestrator.repository.dao.FlavorInstanceDAO;
import eu.orchestrator.repository.dao.GraphLinkDAO;
import eu.orchestrator.repository.dao.GraphLinkNodeDAO;
import eu.orchestrator.repository.dao.GraphLinkNodeInstanceDAO;
import eu.orchestrator.repository.dao.HealthCheckDAO;
import eu.orchestrator.repository.dao.HealthCheckInstanceDAO;
import eu.orchestrator.repository.dao.IDRuleSetDAO;
import eu.orchestrator.repository.dao.IDRuleSetInstanceDAO;
import eu.orchestrator.repository.dao.InterfaceDAO;
import eu.orchestrator.repository.dao.InterfaceInstanceDAO;
import eu.orchestrator.repository.dao.LocationInstanceDAO;
import eu.orchestrator.repository.dao.PluginDAO;
import eu.orchestrator.repository.dao.PluginInstanceDAO;
import eu.orchestrator.repository.dao.ProviderDAO;
import eu.orchestrator.repository.dao.ProviderTypeDAO;
import eu.orchestrator.repository.dao.QIDAO;
import eu.orchestrator.repository.dao.RequirementDAO;
import eu.orchestrator.repository.dao.RuntimePolicyDAO;
import eu.orchestrator.repository.dao.SecurityConfigurationDAO;
import eu.orchestrator.repository.dao.SecurityConfigurationResultDAO;
import eu.orchestrator.repository.dao.SliceConstraintSatisfactionDAO;
import eu.orchestrator.repository.dao.SliceDAO;
import eu.orchestrator.repository.dao.SlicePlacementAttachmentPointDAO;
import eu.orchestrator.repository.dao.SlicePlacementDAO;
import eu.orchestrator.repository.dao.SliceProviderDAO;
import eu.orchestrator.repository.dao.VolumeInstanceDAO;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.ApplicationInstance.ApplicationInstanceStatus;
import eu.orchestrator.repository.domain.ComponentNode;
import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.repository.domain.ComponentNodeInstance.SecurityEnablers;
import eu.orchestrator.repository.domain.ComponentNodeInstanceStatus;
import eu.orchestrator.repository.domain.Constraint;
import eu.orchestrator.repository.domain.DeviceInstance;
import eu.orchestrator.repository.domain.ElasticityHistory;
import eu.orchestrator.repository.domain.EnvironmentalVariableInstance;
import eu.orchestrator.repository.domain.GraphLinkNodeInstance;
import eu.orchestrator.repository.domain.InterfaceInstance;
import eu.orchestrator.repository.domain.LocationInstance;
import eu.orchestrator.repository.domain.PluginInstance;
import eu.orchestrator.repository.domain.Provider;
import eu.orchestrator.repository.domain.ProviderType;
import eu.orchestrator.repository.domain.ProviderType.ProviderName;
import eu.orchestrator.repository.domain.SecurityConfiguration;
import eu.orchestrator.repository.domain.SecurityConfigurationResult;
import eu.orchestrator.repository.domain.SecurityConfigurationResult.SecurityConfigurationResultStatus;
import eu.orchestrator.repository.domain.Slice;
import eu.orchestrator.repository.domain.SliceConstraintSatisfaction;
import eu.orchestrator.repository.domain.SlicePlacement;
import eu.orchestrator.repository.domain.SlicePlacementAttachmentPoint;
import eu.orchestrator.repository.domain.SliceProvider;
import eu.orchestrator.repository.domain.User;
import eu.orchestrator.repository.domain.VolumeInstance;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorApplicationInstance;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorChangedStatusNotification;
import eu.orchestrator.transfer.entities.policyEngine.PolicyModel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackOn = Exception.class)
public class DeploymentService {

    private static final Logger logger = Logger.getLogger(DeploymentService.class.getName());
    private static final RestTemplate restTemplate = new RestTemplate();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ui.server.url}")
    String uiURL;

    @Value("${orchestrator.server.url}")
    String orchestratorURL;

    @Value("${oss.server.slice-endpoint}")
    String ossSliceEndpoint;

    @Value("${consul.server.url}")
    String consulURL;

    @Value("${consul.server.ipv6}")
    String consulURLIPv6;

    @Value("${oss.tac.url}")
    String ossTacURL;

    @Value("${oss.server.url}")
    String ossURL;

    @Autowired
    CommonService commonService;

    @Autowired
    RainbowService rainbowService;

    @Autowired
    KubernetesService kubernetesService;

    @Autowired
    KnativeService knativeService;

    @Autowired
    CleanUpService cleanUpService;

    @Autowired
    ApplicationInstanceService applicationInstanceService;

    @Autowired
    ElasticityService elasticityService;

    @Autowired
    ProviderService providerService;

    @Autowired
    ApplicationInstanceDAO applicationInstanceDAO;

    @Autowired
    ProviderDAO providerDAO;

    @Autowired
    ProviderTypeDAO providerTypeDAO;

    @Autowired
    ApplicationDAO applicationDAO;

    @Autowired
    ConstraintDAO constraintDAO;

    @Autowired
    CountryDAO countryDAO;

    @Autowired
    ComponentNodeDAO componentNodeDAO;

    @Autowired
    ComponentDAO componentDAO;

    @Autowired
    ComponentNodeInstanceDAO componentNodeInstanceDAO;

    @Autowired
    ComponentNodeInstanceIPDAO componentNodeInstanceIPDAO;

    @Autowired
    GraphLinkNodeInstanceDAO graphLinkNodeInstanceDAO;

    @Autowired
    QIDAO qciDAO;

    @Autowired
    InterfaceInstanceDAO interfaceInstanceDAO;

    @Autowired
    EnvironmentalVariableInstanceDAO environmentalVariableInstanceDAO;

    @Autowired
    VolumeInstanceDAO volumeInstanceDAO;

    @Autowired
    LocationInstanceDAO locationInstanceDAO;

    @Autowired
    FlavorInstanceDAO flavorInstanceDAO;

    @Autowired
    RequirementDAO requirementDAO;

    @Autowired
    GraphLinkNodeDAO graphLinkNodeDAO;

    @Autowired
    RuntimePolicyDAO runtimePolicyDAO;

    @Autowired
    ComponentNodeInstanceStatusDAO componentNodeInstanceStatusDAO;

    @Autowired
    ComponentNodeInstanceAlertDAO componentNodeInstanceAlertDAO;

    @Autowired
    HealthCheckInstanceDAO healthCheckInstanceDAO;

    @Autowired
    HealthCheckDAO healthCheckDAO;

    @Autowired
    DeviceInstanceDAO deviceInstanceDAO;

    @Autowired
    PluginInstanceDAO pluginInstanceDAO;

    @Autowired
    PluginDAO pluginDAO;

    @Autowired
    DeviceDAO deviceDAO;

    @Autowired
    IDRuleSetInstanceDAO idRuleSetInstanceDAO;

    @Autowired
    IDRuleSetDAO idRuleSetDAO;

    // Optional: present only when mongo.enabled=true. Passed to OSSUtil, which guards its use.
    @Autowired(required = false)
    OrchestratorApplicationInstanceDAO orchestratorApplicationInstanceDAO;

    @Autowired
    EntityManager entityManager;

    @Autowired
    GraphLinkDAO graphLinkDAO;

    @Autowired
    InterfaceDAO interfaceDAO;

    @Autowired
    SliceDAO sliceDAO;

    @Autowired
    SliceProviderDAO sliceProviderDAO;

    @Autowired
    SliceConstraintSatisfactionDAO sliceConstraintSatisfactionDAO;

    @Autowired
    SlicePlacementDAO slicePlacementDAO;

    @Autowired
    SlicePlacementAttachmentPointDAO slicePlacementAttachmentPointDAO;

    @Autowired
    ElasticityHistoryDAO elasticityHistoryDAO;

    @Autowired
    SecurityConfigurationDAO securityConfigurationDAO;

    @Autowired
    SecurityConfigurationResultDAO securityConfigurationResultDAO;
    @Autowired
    SimpMessagingTemplate wsTemplate;
    @Autowired
    private KafkaTopicConfig kafkaTopicConfig;
    @Autowired
    private Sender sender;
    @Autowired
    private GrafanaUtil grafanaUtil;
    @Autowired
    private OssService ossService;
    @Autowired
    private OssKubernetesService ossKubernetesService;

    @Autowired
    private ExpertSystemService externalSystemService;


    public List<SlicePlacement> requestPlacement(Long applicationInstanceID, User authenticatedUser) {
        ApplicationInstance existingApplicationInstance = applicationInstanceService.fetchApplicationInstanceById(applicationInstanceID);

        boolean isSuccess = false;

        if (NullCheckUtil.isNotEmpty(existingApplicationInstance)
                && existingApplicationInstance.hasStatus(ApplicationInstanceStatus.PENDING)
                && (authenticatedUser.getRole().equals(User.RoleName.ADMIN.name())
                || existingApplicationInstance.getUser().getId().equals(authenticatedUser.getId())
                || existingApplicationInstance.getOrganization().getId()
                .equals(authenticatedUser.getOrganization().getId()))) {

            if (existingApplicationInstance.hasProvider(ProviderName.POLICY_DEFINED)) {
                isSuccess = true;

            } else {
                throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
            }

            if (isSuccess) {

                Slice slice = OrchestratorUtil
                        .requestPlacement(existingApplicationInstance, sliceDAO, sliceConstraintSatisfactionDAO,
                                sliceProviderDAO, slicePlacementDAO, slicePlacementAttachmentPointDAO,
                                applicationInstanceDAO, applicationDAO, componentNodeInstanceDAO,
                                componentNodeInstanceStatusDAO, componentNodeDAO, componentDAO,
                                interfaceInstanceDAO, flavorInstanceDAO, healthCheckDAO, healthCheckInstanceDAO,
                                deviceInstanceDAO, deviceDAO, environmentalVariableInstanceDAO, volumeInstanceDAO,
                                locationInstanceDAO, graphLinkNodeInstanceDAO, requirementDAO, providerDAO,
                                constraintDAO, runtimePolicyDAO, pluginInstanceDAO, pluginDAO, idRuleSetInstanceDAO,
                                idRuleSetDAO, entityManager, restTemplate, objectMapper, orchestratorURL, uiURL,
                                consulURL, consulURLIPv6, elasticityService, kafkaTopicConfig);

                if (null != slice) {

                    try {

                        existingApplicationInstance.setSlice(slice);
                        existingApplicationInstance.setLastModified(new Date());
                        existingApplicationInstance.setStatus(ApplicationInstanceStatus.WAITING_CONFIRMATION.name());
                        applicationInstanceDAO.save(existingApplicationInstance);

                        return slicePlacementDAO.findAllBySlice(slice);

                    } catch (Exception e) {
                        logger.log(Level.SEVERE, e.getMessage(), e);
                        throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
                    }

                } else {

                    existingApplicationInstance.setLastModified(new Date());
                    existingApplicationInstance.setStatus(ApplicationInstanceStatus.ERROR_OCCURRED.name());
                    applicationInstanceDAO.save(existingApplicationInstance);

                    return new ArrayList<>();
                }

            }

        }
        throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
    }

    public ApplicationInstanceGraphTO requestSliceIntent(Long applicationInstanceID, User authenticatedUser) {
        ApplicationInstance existingApplicationInstance = applicationInstanceService.fetchApplicationInstanceById(applicationInstanceID);

        boolean isSuccess = false;

        if (NullCheckUtil.isNotEmpty(existingApplicationInstance)
                && existingApplicationInstance.hasStatus(ApplicationInstanceStatus.PENDING)
                && (authenticatedUser.getRole().equals(User.RoleName.ADMIN.name())
                || existingApplicationInstance.getUser().getId().equals(authenticatedUser.getId())
                || existingApplicationInstance.getOrganization().getId()
                .equals(authenticatedUser.getOrganization().getId()))) {

            // Checks if provider is 5G or not
            boolean ossDeployment = false;

            if (!existingApplicationInstance.hasProvider(ProviderName.POLICY_DEFINED) &&
                    !existingApplicationInstance.hasProvider(ProviderName.USER_DEFINED) &&
                    existingApplicationInstance.hasProvider(ProviderName.FIFTH_GENERATION_TELCO_PROVIDER)) {
                ossDeployment = true;

            } else if (existingApplicationInstance.hasProvider(ProviderName.USER_DEFINED)) {

                List<ComponentNodeInstance> componentNodeInstances = componentNodeInstanceDAO
                        .findAllByApplicationInstance(existingApplicationInstance, null).getContent();

                if (NullCheckUtil.isNotEmpty(componentNodeInstances)) {

                    List<String> providerNames = new ArrayList<>();

                    componentNodeInstances.forEach(componentNodeInstance -> {
                        if (!providerNames.contains(componentNodeInstance.getProvider().getName())) {
                            providerNames.add(componentNodeInstance.getProvider().getName());
                        }
                    });

                    if (providerNames.size() == 1) {
                        Provider provider = providerService.findByName(providerNames.get(0));
                        if (NullCheckUtil.isNotEmpty(provider) && provider.hasType(ProviderName.FIFTH_GENERATION_TELCO_PROVIDER)) {
                            ossDeployment = true;
                        }
                    }


                }

            } else {
                throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
            }

            if (ossDeployment) {

                isSuccess = OSSUtil
                        .requestSlice(existingApplicationInstance, applicationInstanceDAO, applicationDAO,
                                componentNodeInstanceDAO, componentNodeInstanceStatusDAO,
                                componentNodeInstanceAlertDAO, componentNodeDAO, componentDAO, interfaceInstanceDAO,
                                flavorInstanceDAO, healthCheckDAO, healthCheckInstanceDAO, deviceInstanceDAO,
                                deviceDAO, environmentalVariableInstanceDAO, volumeInstanceDAO, locationInstanceDAO,
                                graphLinkNodeInstanceDAO, graphLinkNodeDAO, requirementDAO, constraintDAO,
                                runtimePolicyDAO, pluginInstanceDAO, pluginDAO, idRuleSetInstanceDAO, idRuleSetDAO,
                                orchestratorApplicationInstanceDAO, entityManager, restTemplate, ossURL, ossSliceEndpoint, uiURL,
                                consulURL, consulURLIPv6, ossTacURL, elasticityService, kafkaTopicConfig);

                if (isSuccess) {

                    existingApplicationInstance.setLastModified(new Date());
                    existingApplicationInstance.setStatus(ApplicationInstanceStatus.WAITING_OSS.name());
                    applicationInstanceDAO.save(existingApplicationInstance);

                    // Return application instance TO
                    return commonService.convertApplicationInstanceToTO(existingApplicationInstance);

                } else {

                    existingApplicationInstance.setLastModified(new Date());
                    existingApplicationInstance.setStatus(ApplicationInstanceStatus.ERROR_OCCURRED.name());
                    applicationInstanceDAO.save(existingApplicationInstance);

                    return null;
                }

            } else {

                // Proceed with actual deployment to orchestrator
                isSuccess = OrchestratorUtil.executeDeployment(existingApplicationInstance, applicationDAO, componentNodeInstanceStatusDAO, componentNodeDAO,
                        componentNodeInstanceDAO, graphLinkNodeInstanceDAO, pluginDAO, restTemplate, orchestratorURL,
                        uiURL, elasticityService, kafkaTopicConfig, sender);

                if (isSuccess) {
                    existingApplicationInstance.setLastModified(new Date());
                    existingApplicationInstance.setStatus(ApplicationInstanceStatus.WAITING_ORCHESTRATOR.name());
                    applicationInstanceDAO.save(existingApplicationInstance);

                    // Return application instance TO
                    return commonService.convertApplicationInstanceToTO(existingApplicationInstance);

                } else {

                    existingApplicationInstance.setLastModified(new Date());
                    existingApplicationInstance.setStatus(ApplicationInstanceStatus.ERROR_OCCURRED.name());
                    applicationInstanceDAO.save(existingApplicationInstance);

                    return null;
                }

            }

        }
        throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
    }

    public ApplicationInstanceGraphTO requestDeployment(Long applicationInstanceID, User authenticatedUser) {
        ApplicationInstance existingApplicationInstance = applicationInstanceService.fetchApplicationInstanceById(applicationInstanceID);

        boolean isSuccess = false;

        ApplicationInstanceGraphTO applicationInstanceGraphTO = null;

        if (NullCheckUtil.isNotEmpty(existingApplicationInstance)
                && (existingApplicationInstance.hasStatus(ApplicationInstanceStatus.PENDING)
                || existingApplicationInstance.hasStatus(ApplicationInstanceStatus.WAITING_CONFIRMATION))
                && (authenticatedUser.getRole().equals(User.RoleName.ADMIN.name())
                || existingApplicationInstance.getUser().getId().equals(authenticatedUser.getId())
                || existingApplicationInstance.getOrganization().getId().equals(authenticatedUser.getOrganization().getId()))) {

            if (existingApplicationInstance.hasStatus(ApplicationInstanceStatus.PENDING)) {

                // FOR K8S PROVIDERS
                if (existingApplicationInstance.hasProvider(ProviderName.RAINBOW_KUBERNETES)) {

                    isSuccess = rainbowService.rainbowDeployment(existingApplicationInstance.getApplicationInstanceID(),
                            existingApplicationInstance.getProvider().getNetworkModeHost());
                    if (isSuccess && NullCheckUtil.isNotEmpty(existingApplicationInstance.getComponentNodeInstances())) {
                        createInitialComponentNodeInstancesStatus(existingApplicationInstance);
                    }
                } else if (existingApplicationInstance.hasProvider(ProviderName.KUBERNETES) ||
                        existingApplicationInstance.hasProvider(ProviderName.FIVE_G_INDUCE_SLICE)) {

                    isSuccess = kubernetesService.deployment(existingApplicationInstance.getApplicationInstanceID(),
                            existingApplicationInstance.getProvider().getNetworkModeHost());
                    if (isSuccess && NullCheckUtil.isNotEmpty(existingApplicationInstance.getComponentNodeInstances())) {
                        createInitialComponentNodeInstancesStatus(existingApplicationInstance);
                    }
                }  else if (existingApplicationInstance.hasProvider(ProviderName.KUBERNETES_KNATIVE)) {
                    isSuccess = knativeService.knativeDeployment(existingApplicationInstance.getApplicationInstanceID(),
                            existingApplicationInstance.getProvider().getNetworkModeHost());
                    if (isSuccess && NullCheckUtil.isNotEmpty(existingApplicationInstance.getComponentNodeInstances())) {
                        createInitialComponentNodeInstancesStatus(existingApplicationInstance);
                    }
                }  else if (ossKubernetesService.isSupportedOssProvider(existingApplicationInstance.getProvider())) {
                    final Provider applicationProvider = existingApplicationInstance.getProvider();
                    logger.log(Level.INFO,
                            "Forwarding deployment request for application [id={0}] to OSS for provider [{1}]",
                            new Object[]{existingApplicationInstance.getApplicationInstanceID(),
                                    applicationProvider.getProviderType()});
                    final boolean isIntentSubmittedSuccessfully = ossService.requestSlice(existingApplicationInstance);

                    final ApplicationInstanceStatus status =
                            isIntentSubmittedSuccessfully ? ApplicationInstanceStatus.WAITING_OSS : ApplicationInstanceStatus.ERROR_OCCURRED;
                    existingApplicationInstance.setStatus(status.name());
                    existingApplicationInstance.setLastModified(new Date());
                    applicationInstanceDAO.save(existingApplicationInstance);

                    logger.log(Level.INFO, "Slice submission for application [id={0}] {1}",
                            new Object[]{existingApplicationInstance.getApplicationInstanceID(), isIntentSubmittedSuccessfully ? "succeeded" : "failed"});

                    if (isIntentSubmittedSuccessfully) {
                        return commonService.convertApplicationInstanceToTO(existingApplicationInstance);
                    } else {
                        return null;
                    }
                } else {
                    // END OF - FOR K8S PROVIDERS SECTION

                    // Proceed with actual deployment to orchestrator

                    isSuccess = OrchestratorUtil.executeDeployment(existingApplicationInstance, applicationDAO, componentNodeInstanceStatusDAO,
                            componentNodeDAO, componentNodeInstanceDAO, graphLinkNodeInstanceDAO, pluginDAO, restTemplate, orchestratorURL,
                            uiURL, elasticityService, kafkaTopicConfig, sender);


                }
                if (isSuccess) {

                    existingApplicationInstance.setLastModified(new Date());
                    existingApplicationInstance.setStatus(ApplicationInstanceStatus.WAITING_ORCHESTRATOR.name());

                    String output = grafanaUtil.createDashboard(existingApplicationInstance);
                    if (!output.isEmpty()) {
                        existingApplicationInstance.setGrafanaDashboardUUID(output);
                    }

                    applicationInstanceDAO.save(existingApplicationInstance);

                    // Return application instance TO
                    applicationInstanceGraphTO = commonService.convertApplicationInstanceToTO(existingApplicationInstance);

                    return applicationInstanceGraphTO;

                } else {

                    existingApplicationInstance.setLastModified(new Date());
                    existingApplicationInstance.setStatus(ApplicationInstanceStatus.ERROR_OCCURRED.name());
                    applicationInstanceDAO.save(existingApplicationInstance);

                    return null;
                }

            } else if (existingApplicationInstance.hasStatus(ApplicationInstanceStatus.WAITING_CONFIRMATION)) {

                // Check if the deployment refers to 5G provider or not
                if (existingApplicationInstance.hasProvider(ProviderName.POLICY_DEFINED)) {

                    // Policy-defined
                    if (null != existingApplicationInstance.getSlice()) {

                        isSuccess = OrchestratorUtil.executePolicyDefinedDeployment(existingApplicationInstance,
                                existingApplicationInstance.getSlice(), sliceDAO, sliceProviderDAO,
                                slicePlacementDAO, slicePlacementAttachmentPointDAO, componentNodeInstanceDAO,
                                providerDAO, providerTypeDAO, graphLinkNodeInstanceDAO, entityManager, objectMapper,
                                restTemplate, orchestratorURL);

                        if (isSuccess) {

                            existingApplicationInstance.setLastModified(new Date());
                            existingApplicationInstance.setStatus(ApplicationInstanceStatus.WAITING_ORCHESTRATOR.name());

                            String output = grafanaUtil.createDashboard(existingApplicationInstance);
                            if (!output.isEmpty()) {
                                existingApplicationInstance.setGrafanaDashboardUUID(output);
                            }

                            applicationInstanceDAO.save(existingApplicationInstance);

                            // Return application instance TO
                            applicationInstanceGraphTO = commonService.convertApplicationInstanceToTO(existingApplicationInstance);

                            return applicationInstanceGraphTO;

                        } else {

                            existingApplicationInstance.setLastModified(new Date());
                            existingApplicationInstance.setStatus(ApplicationInstanceStatus.ERROR_OCCURRED.name());
                            applicationInstanceDAO.save(existingApplicationInstance);

                            return null;
                        }

                    }


                } else {

                    // OSS
                    if (existingApplicationInstance.getSlice() == null) {
                        logger.log(Level.SEVERE,
                                "Reached an OSS deployment path but slice of application instance [id={0}] is empty",
                                applicationInstanceID);
                        throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(),
                                GenericMessage.GENERIC_ERROR);
                    }

                    final ProviderName applicationInstanceProvider = getProviderName(existingApplicationInstance.getProvider());
                    ProviderNameReducer providerNameReducer = new ProviderNameReducer();

                    if (providerNameReducer.isOpenStackBasedProvider(applicationInstanceProvider)) {
                        logger.log(Level.INFO,
                                "Issuing an OSS based deployment on an OpenStack based provider for application instance [id={0}]",
                                applicationInstanceID);
                        isSuccess = OSSUtil.executeDeploymentFromOSS(existingApplicationInstance,
                                existingApplicationInstance.getSlice(), sliceDAO, sliceProviderDAO,
                                slicePlacementDAO, slicePlacementAttachmentPointDAO, componentNodeInstanceDAO,
                                providerDAO, providerTypeDAO, graphLinkNodeInstanceDAO, graphLinkNodeDAO,
                                graphLinkDAO, interfaceInstanceDAO, interfaceDAO, entityManager, objectMapper,
                                restTemplate, orchestratorURL);

                        existingApplicationInstance.setLastModified(new Date());

                        if (!isSuccess) {
                            existingApplicationInstance.setStatus(ApplicationInstanceStatus.ERROR_OCCURRED.name());
                            applicationInstanceDAO.save(existingApplicationInstance);
                            return null;
                        }

                        final String grafanaDashboardUuid = grafanaUtil.createDashboard(existingApplicationInstance);
                        if (!grafanaDashboardUuid.isEmpty()) {
                            existingApplicationInstance.setGrafanaDashboardUUID(grafanaDashboardUuid);
                        }

                        existingApplicationInstance.setStatus(ApplicationInstanceStatus.WAITING_ORCHESTRATOR.name());
                        applicationInstanceDAO.save(existingApplicationInstance);
                        // Return application instance TO
                        applicationInstanceGraphTO = commonService.convertApplicationInstanceToTO(existingApplicationInstance);

                    } else if (providerNameReducer.isKubernetesBasedProvider(applicationInstanceProvider)) {
                        logger.log(Level.INFO, "Issuing an OSS-Kubernetes based deployment for application instance [id={0}]", applicationInstanceID);

                        isSuccess = ossKubernetesService.deployment(existingApplicationInstance.getApplicationInstanceID(),
                                existingApplicationInstance.getProvider().getNetworkModeHost());

                        if (!isSuccess) {
                            existingApplicationInstance.setLastModified(new Date());
                            existingApplicationInstance.setStatus(ApplicationInstanceStatus.ERROR_OCCURRED.name());
                            applicationInstanceDAO.save(existingApplicationInstance);

                            throw new GenericBusinessException(
                                    String.format("OSS-Kubernetes based deployment failed for application instance [id=%d]", applicationInstanceID),
                                    GenericMessage.GENERIC_ERROR);
                        }

                        if (NullCheckUtil.isNotEmpty(existingApplicationInstance.getComponentNodeInstances())) {
                            createInitialComponentNodeInstancesStatus(existingApplicationInstance);
                        }

                        existingApplicationInstance.setLastModified(new Date());
                        existingApplicationInstance.setStatus(ApplicationInstanceStatus.WAITING_ORCHESTRATOR.name());
                        final String grafanaDashboardUuid = grafanaUtil.createDashboard(existingApplicationInstance);
                        if (!grafanaDashboardUuid.isEmpty()) {
                            existingApplicationInstance.setGrafanaDashboardUUID(grafanaDashboardUuid);
                        }
                        applicationInstanceDAO.save(existingApplicationInstance);

                        logger.log(Level.INFO, "Successfully issued OSS-Kubernetes based deployment for application instance [id={0}]", applicationInstanceID);
                        return commonService.convertApplicationInstanceToTO(existingApplicationInstance);

                    } else {
                        logger.log(Level.SEVERE,
                                "Non supported provider type ({0} => reduced to {1}) for OSS based deployments. Aborting...",
                                new Object[]{applicationInstanceProvider,
                                        providerNameReducer.reduce(applicationInstanceProvider)});

                        throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(),
                                GenericMessage.GENERIC_ERROR);
                    }

                }

            }

            if (isSuccess) {
                Optional<ApplicationInstance> applicationInstanceStartedOp = applicationInstanceDAO
                        .findById(existingApplicationInstance.getApplicationInstanceID());

                if (applicationInstanceStartedOp.isPresent()) {
                    ApplicationInstance applicationInstanceStarted = applicationInstanceStartedOp.get();
                    //Key = ComponentNodeName, Value = entry
                    Map<String, ElasticityHistory> elasticityHistoryMap = new HashMap<>();

                    List<ComponentNodeInstance> componentNodeInstanceList = componentNodeInstanceDAO.findAllByApplicationInstance(existingApplicationInstance);
                    componentNodeInstanceList.forEach(componentNodeInstance -> {
//          applicationInstanceStarted.getComponentNodeInstances().forEach(componentNodeInstance -> {

                        if (Boolean.TRUE.equals(elasticityService.requiresElasticity(componentNodeInstance))) {

                            ComponentNode componentNodeStarted = componentNodeInstance.getComponentNode();
                            if (!elasticityHistoryMap.containsKey(componentNodeStarted.getName())) {
                                ElasticityHistory elasticityHistory = new ElasticityHistory();

                                elasticityHistory.setApplicationInstance(applicationInstanceStarted);
                                elasticityHistory.setComponentNode(componentNodeStarted);

                                elasticityHistory.setActiveWorkers((long) 0);
                                elasticityHistory.setWorkersCount((long) 1);

                                elasticityHistory.setStatus(ElasticityHistory.ElasticityStatus.DEPLOYING.name());
                                elasticityHistory.setDateCreated(new Date());
                                elasticityHistory.setLastModified(new Date());

                                elasticityHistoryMap.put(componentNodeStarted.getName(), elasticityHistory);
                            } else {
                                ElasticityHistory elasticityHistory = elasticityHistoryMap.get(componentNodeStarted.getName());

                                elasticityHistory.setWorkersCount(elasticityHistory.getWorkersCount() + 1);

//                  elasticityHistoryMap.replace(componentNodeStarted.getName(),elasticityHistory);
                            }
                        }

                    });

                    if (!elasticityHistoryMap.isEmpty()) {
                        elasticityHistoryMap.keySet().forEach(key -> {
                            ElasticityHistory elasticityHistory = elasticityHistoryMap.get(key);
                            elasticityHistoryDAO.save(elasticityHistory);
                        });
                    }

                    //TODo astrid security configuration
                    List<ComponentNodeInstance> componentNodeInstanceListWithConfiguration = componentNodeInstanceList.stream()
                            .filter(componentNodeInstance -> null != componentNodeInstance.getSecurityEnablers()
                                    && !componentNodeInstance.getSecurityEnablers().isEmpty()
                                    && componentNodeInstance.getSecurityEnablers().contains(SecurityEnablers.CONFIGURATION_INTEGRITY_VERIFICATION))
                            .collect(Collectors.toList());

                    if (!componentNodeInstanceListWithConfiguration.isEmpty()) {
                        SecurityConfiguration securityConfiguration = new SecurityConfiguration();

                        securityConfiguration.setApplicationInstance(existingApplicationInstance);
                        securityConfiguration.setName("Default Configuration integrity verification");
                        securityConfiguration.setUser(authenticatedUser);
                        securityConfiguration.setHexID(Util.createRandomHEXString());
                        securityConfiguration.setDateCreated(new Date());
                        securityConfiguration.setLastModified(new Date());
                        securityConfiguration.setSecurityConfigurationType(SecurityEnablers.CONFIGURATION_INTEGRITY_VERIFICATION.name());
                        securityConfiguration = securityConfigurationDAO.save(securityConfiguration);

                        SecurityConfiguration finalSecurityConfiguration = securityConfiguration;

                        componentNodeInstanceListWithConfiguration.stream().forEach(componentNodeInstance -> {
                            SecurityConfigurationResult securityConfigurationResult = new SecurityConfigurationResult();
                            securityConfigurationResult.setComponentNodeInstance(componentNodeInstance);
                            securityConfigurationResult.setDateCreated(new Date());
                            securityConfigurationResult.setLastModified(new Date());
                            securityConfigurationResult.setSecurityConfiguration(finalSecurityConfiguration);
                            securityConfigurationResult.setStatus(SecurityConfigurationResultStatus.process.name());
                            securityConfigurationResult.setHexID(Util.createRandomHEXString());
                            securityConfigurationResultDAO.save(securityConfigurationResult);
                        });
                    }

                }

                try {
                    DashboardTO dashboardTO = new DashboardTO();
                    dashboardTO.setOverview(true);
                    dashboardTO.setElasticity(true);
                    dashboardTO.setIdsSecurity(true);
                    dashboardTO.setIpsSecurity(true);
                    String notificationAsString = null;
                    notificationAsString = objectMapper.writeValueAsString(dashboardTO);
                    wsTemplate.convertAndSend(ConstantsUtil.DASHBOARD_TOPIC, notificationAsString);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

                return applicationInstanceGraphTO;
            }

        }
        throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
    }

    private void createInitialComponentNodeInstancesStatus(ApplicationInstance existingApplicationInstance) {
        for (ComponentNodeInstance cni : existingApplicationInstance.getComponentNodeInstances()) {
            ComponentNodeInstanceStatus componentNodeInstanceStatus = new ComponentNodeInstanceStatus();

            componentNodeInstanceStatus.setReportedChange(
                    OrchestratorChangedStatusNotification.ChangeType.GraphStatusChange.name());
            final Date now = new Date();
            componentNodeInstanceStatus.setLastModified(now);
            componentNodeInstanceStatus.setDateCreated(now);
            componentNodeInstanceStatus.setComponentNodeInstance(cni);
            componentNodeInstanceStatus.setMessage("The Component is spawning...");
            componentNodeInstanceStatus.setStatus("INFO");
            componentNodeInstanceStatus.setApplicationInstance(existingApplicationInstance);

            componentNodeInstanceStatusDAO.save(componentNodeInstanceStatus);
        }
    }

    private ProviderName getProviderName(Provider provider) {
        final ProviderType providerType = provider.getProviderType();
        if (providerType == null) {
            throw new IllegalArgumentException("Cannot infer provider name with null provider type");
        }

        return providerType.getProviderName();
    }

    public void requestUndeployment(Long applicationInstanceID, User authenticatedUser) {
        ApplicationInstance existingApplicationInstance = applicationInstanceService.fetchApplicationInstanceById(applicationInstanceID);

        if (NullCheckUtil.isNotEmpty(existingApplicationInstance) &&
                (authenticatedUser.getRole().equals(User.RoleName.ADMIN.name())
                        || existingApplicationInstance.getUser().getId()
                        .equals(authenticatedUser.getId()) || authenticatedUser.getOrganization().getId()
                        .equals(existingApplicationInstance.getOrganization().getId()))
                && (existingApplicationInstance.hasStatus(ApplicationInstanceStatus.DEPLOYED)
                || existingApplicationInstance.hasStatus(ApplicationInstanceStatus.WAITING_ORCHESTRATOR))) {

            //TODO PROVIDER
            if (existingApplicationInstance.hasProvider(ProviderName.RAINBOW_KUBERNETES)) {
                // Update Status of Application Instance
                existingApplicationInstance.setStatus(ApplicationInstanceStatus.UNDEPLOYING.name());
                applicationInstanceDAO.save(existingApplicationInstance);
                boolean successUndeployment = rainbowService.rainbowUndeployment(applicationInstanceID);
                cleanUpService.applicationInstanceRemoveStatuses(applicationInstanceID, successUndeployment);
            } else if (existingApplicationInstance.hasProvider(ProviderName.KUBERNETES) ||
                    existingApplicationInstance.hasProvider(ProviderName.FIVE_G_INDUCE_SLICE)) {
                // Update Status of Application Instance
                existingApplicationInstance.setStatus(ApplicationInstanceStatus.UNDEPLOYING.name());
                applicationInstanceDAO.save(existingApplicationInstance);
                boolean successUndeployment = kubernetesService.undeployment(applicationInstanceID);
                cleanUpService.applicationInstanceRemoveStatuses(applicationInstanceID, successUndeployment);
            } else if (existingApplicationInstance.hasProvider(ProviderName.KUBERNETES_KNATIVE)) {
                // Update Status of Application Instance
                existingApplicationInstance.setStatus(ApplicationInstanceStatus.UNDEPLOYING.name());
                applicationInstanceDAO.save(existingApplicationInstance);
                boolean successUndeployment = knativeService.knativeUndeployment(applicationInstanceID);
                cleanUpService.applicationInstanceRemoveStatuses(applicationInstanceID, successUndeployment);
            }

            //TODO K8s
            else {

                OrchestratorApplicationInstance orchestratorApplicationInstance = new OrchestratorApplicationInstance();
                orchestratorApplicationInstance
                        .setGraphID(existingApplicationInstance.getApplication().getId() + "");
                orchestratorApplicationInstance
                        .setGraphHexID(existingApplicationInstance.getApplication().getHexID());
                orchestratorApplicationInstance
                        .setGraphName(existingApplicationInstance.getApplication().getName());
                orchestratorApplicationInstance
                        .setGraphInstanceHexID(existingApplicationInstance.getHexID());
                orchestratorApplicationInstance
                        .setGraphInstanceID(existingApplicationInstance.getApplicationInstanceID() + "");
                orchestratorApplicationInstance.setGraphInstanceName(existingApplicationInstance.getName());
                orchestratorApplicationInstance.setCallbackURL(
                        uiURL + "/api/v1/callback/undeployment/" + existingApplicationInstance
                                .getApplicationInstanceID());

                try {
                    String orchestratorAppInstanceAsString = objectMapper
                            .writeValueAsString(orchestratorApplicationInstance);
                    logger.log(Level.INFO, "Orchestrator AppInstance: {0}", orchestratorAppInstanceAsString);
                    sender.sendToOrchestrator(orchestratorAppInstanceAsString);
                } catch (JsonProcessingException ex) {
                    logger.log(Level.SEVERE, ex.getMessage());
                }
                // Update Status of Application Instance
                existingApplicationInstance.setStatus(ApplicationInstanceStatus.UNDEPLOYING.name());
                applicationInstanceDAO.save(existingApplicationInstance);

                // Notify also Policy Engine
                PolicyModel policyModel = new PolicyModel();
                policyModel.setGraphHexID(existingApplicationInstance.getApplication().getHexID());
                policyModel.setGraphInstanceHexID(existingApplicationInstance.getHexID());
                policyModel.setCreation(false);

                try {
                    String policyModelAsString = objectMapper.writeValueAsString(policyModel);
                    sender.sendElasticityPolicy(policyModelAsString);
                } catch (JsonProcessingException ex) {
                    logger.log(Level.SEVERE, ex.getMessage());
                }
            }

            // Delete also runtime policies of application instance from DB
            if (null != existingApplicationInstance.getRuntimePolicies() && !existingApplicationInstance
                    .getRuntimePolicies().isEmpty()) {

                runtimePolicyDAO.deleteAll(existingApplicationInstance.getRuntimePolicies());

            }

            grafanaUtil.deleteDashboard(existingApplicationInstance.getGrafanaDashboardUUID());

            List<ElasticityHistory> elasticityHistoryList = elasticityHistoryDAO.findAllByApplicationInstance(existingApplicationInstance);
            if (null != elasticityHistoryList && !elasticityHistoryList.isEmpty()) {
                elasticityHistoryDAO.deleteAll(elasticityHistoryList);
            }

            // TODO remove after spider, delete spider rules
            if (null != existingApplicationInstance.getSpiderRules() && !existingApplicationInstance.getSpiderRules().isEmpty()) {
                externalSystemService.deleteRulesFromExpertSystem(existingApplicationInstance);
            }

            try {
                DashboardTO dashboardTO = new DashboardTO();
                dashboardTO.setOverview(true);
                dashboardTO.setElasticity(true);
                dashboardTO.setIpsSecurity(true);
                dashboardTO.setIdsSecurity(true);
                String notificationAsString = null;
                notificationAsString = objectMapper.writeValueAsString(dashboardTO);
                wsTemplate.convertAndSend(ConstantsUtil.DASHBOARD_TOPIC, notificationAsString);
            } catch (JsonProcessingException ex) {
                logger.log(Level.SEVERE, ex.getMessage());
                ex.printStackTrace();
            }

        }

    }

    public void requestCancellation(Long applicationInstanceID, User authenticatedUser) {
        ApplicationInstance existingApplicationInstance = applicationInstanceService.fetchApplicationInstanceById(applicationInstanceID);

        if (NullCheckUtil.isNotEmpty(existingApplicationInstance) &&
                (authenticatedUser.getRole().equals(User.RoleName.ADMIN.name())
                        || existingApplicationInstance.getUser().getId()
                        .equals(authenticatedUser.getId()) || existingApplicationInstance
                        .getOrganization().getId().equals(authenticatedUser.getOrganization().getId()))
                && existingApplicationInstance.hasStatus(ApplicationInstanceStatus.WAITING_CONFIRMATION)) {

            // TODO Delete all extra component node instances along with Slice

            Slice slice = sliceDAO.findByApplicationInstance(existingApplicationInstance).get();

            if (null != slice) {

                // Delete

                List<SlicePlacement> slicePlacements = slicePlacementDAO.findAllBySlice(slice);

                if (null != slicePlacements && !slicePlacements.isEmpty()) {

                    slicePlacements.stream().forEach(slicePlacement -> {

                        List<SlicePlacementAttachmentPoint> slicePlacementAttachmentPoints = slicePlacementAttachmentPointDAO
                                .findAllBySlicePlacement(slicePlacement);

                        if (null != slicePlacementAttachmentPoints && !slicePlacementAttachmentPoints
                                .isEmpty()) {

                            slicePlacementAttachmentPointDAO.deleteAll(slicePlacementAttachmentPoints);

                        }

                        slicePlacementDAO.delete(slicePlacement);


                    });


                }

                List<SliceProvider> sliceProviders = sliceProviderDAO.findAllBySlice(slice);

                if (null != sliceProviders && !sliceProviders.isEmpty()) {

                    sliceProviderDAO.deleteAll(sliceProviders);

                }

                List<SliceConstraintSatisfaction> sliceConstraintSatisfactions = sliceConstraintSatisfactionDAO
                        .findAllBySlice(slice);

                if (null != sliceConstraintSatisfactions && !sliceConstraintSatisfactions.isEmpty()) {

                    sliceConstraintSatisfactionDAO.deleteAll(sliceConstraintSatisfactions);

                }

                sliceDAO.delete(slice);

                // Delete extra component node instances

                List<ComponentNodeInstance> componentNodeInstances = componentNodeInstanceDAO
                        .findAllByApplicationInstance(existingApplicationInstance, null).getContent();

                if (null != componentNodeInstances && !componentNodeInstances.isEmpty()) {

                    componentNodeInstances.stream().forEach(componentNodeInstance -> {

                        boolean toBeDeleted = false;

                        if (componentNodeInstance.getLoadBalancer().booleanValue()) {

                            toBeDeleted = true;

                        } else if (null != componentNodeInstance.getLoadBalancedBy()) {

                            if (componentNodeInstance.getName().contains("Worker")) {

                                toBeDeleted = true;

                            } else {

                                componentNodeInstance.setLoadBalancedBy(null);
                                componentNodeInstance.setLastModified(new Date());
                                componentNodeInstance.setProvider(existingApplicationInstance.getProvider());
                                componentNodeInstanceDAO.save(componentNodeInstance);

                            }

                        } else {

                            componentNodeInstance.setLastModified(new Date());
                            componentNodeInstance.setProvider(existingApplicationInstance.getProvider());
                            componentNodeInstanceDAO.save(componentNodeInstance);

                        }

                        if (toBeDeleted) {

                            List<GraphLinkNodeInstance> graphLinkNodeInstancesFrom = graphLinkNodeInstanceDAO
                                    .findAllByApplicationInstanceAndComponentNodeInstanceFrom(
                                            existingApplicationInstance, componentNodeInstance);

                            if (null != graphLinkNodeInstancesFrom && !graphLinkNodeInstancesFrom.isEmpty()) {

                                // TODO Check if we have constraints on this GLNI
                                List<Constraint> constraints = constraintDAO
                                        .findAllByApplicationInstance(existingApplicationInstance, null).getContent();

                                if (null != constraints && !constraints.isEmpty()) {

                                    constraints.stream()
                                            .filter(constraint -> null != constraint.getGraphLinkNodeInstance())
                                            .forEach(constraint ->

                                                    graphLinkNodeInstancesFrom.stream().forEach(graphLinkNodeInstance -> {

                                                        if (constraint.getGraphLinkNodeInstance().getGraphLinkNodeInstanceID()
                                                                .equals(graphLinkNodeInstance.getGraphLinkNodeInstanceID())) {

                                                            constraintDAO.delete(constraint);

                                                        }

                                                    })

                                            );

                                }

                                graphLinkNodeInstanceDAO.deleteAll(graphLinkNodeInstancesFrom);

                            }

                            List<GraphLinkNodeInstance> graphLinkNodeInstancesTo = graphLinkNodeInstanceDAO
                                    .findAllByApplicationInstanceAndComponentNodeInstanceTo(
                                            existingApplicationInstance, componentNodeInstance);

                            if (null != graphLinkNodeInstancesTo && !graphLinkNodeInstancesTo.isEmpty()) {

                                // TODO Check if we have constraints on this GLNI

                                List<Constraint> constraints = constraintDAO
                                        .findAllByApplicationInstance(existingApplicationInstance, null).getContent();

                                if (null != constraints && !constraints.isEmpty()) {

                                    constraints.stream()
                                            .filter(constraint -> null != constraint.getGraphLinkNodeInstance())
                                            .forEach(constraint ->

                                                    graphLinkNodeInstancesTo.stream().forEach(graphLinkNodeInstance -> {

                                                        if (constraint.getGraphLinkNodeInstance().getGraphLinkNodeInstanceID()
                                                                .equals(graphLinkNodeInstance.getGraphLinkNodeInstanceID())) {
                                                            constraintDAO.delete(constraint);

                                                        }
                                                    })
                                            );

                                }

                                graphLinkNodeInstanceDAO.deleteAll(graphLinkNodeInstancesTo);

                            }

                            // Delete this worker
                            List<VolumeInstance> volumeInstances = volumeInstanceDAO
                                    .findAllByComponentNodeInstanceOrderByDateCreatedDesc(componentNodeInstance,
                                            null).getContent();

                            if (null != volumeInstances && !volumeInstances.isEmpty()) {

                                volumeInstanceDAO.deleteAll(volumeInstances);

                            }

                            List<InterfaceInstance> interfaceInstances = interfaceInstanceDAO
                                    .findAllByComponentNodeInstanceOrderByDateCreatedDesc(componentNodeInstance,
                                            null).getContent();

                            if (null != interfaceInstances && !interfaceInstances.isEmpty()) {

                                // TODO Check if we have constraints on this II
                                List<Constraint> constraints = constraintDAO
                                        .findAllByApplicationInstance(existingApplicationInstance, null).getContent();

                                if (null != constraints && !constraints.isEmpty()) {

                                    constraints.stream()
                                            .filter(constraint -> null != constraint.getInterfaceInstance())
                                            .forEach(constraint -> {

                                                interfaceInstances.stream().forEach(interfaceInstance -> {

                                                    if (constraint.getInterfaceInstance().getInterfaceInstanceID()
                                                            .equals(interfaceInstance.getInterfaceInstanceID())) {

                                                        constraintDAO.delete(constraint);

                                                    }

                                                });

                                            });

                                }

                                interfaceInstanceDAO.deleteAll(interfaceInstances);

                            }

                            List<EnvironmentalVariableInstance> environmentalVariableInstances = environmentalVariableInstanceDAO
                                    .findAllByComponentNodeInstanceOrderByDateCreatedDesc(componentNodeInstance,
                                            null).getContent();

                            if (null != environmentalVariableInstances && !environmentalVariableInstances
                                    .isEmpty()) {

                                environmentalVariableInstanceDAO.deleteAll(environmentalVariableInstances);

                            }

                            List<PluginInstance> pluginInstances = pluginInstanceDAO
                                    .findAllByComponentNodeInstanceOrderByDateCreatedDesc(componentNodeInstance,
                                            null).getContent();

                            if (null != pluginInstances && !pluginInstances.isEmpty()) {

                                pluginInstanceDAO.deleteAll(pluginInstances);

                            }

                            List<DeviceInstance> deviceInstances = deviceInstanceDAO
                                    .findAllByComponentNodeInstanceOrderByDateCreatedDesc(componentNodeInstance,
                                            null).getContent();

                            if (null != deviceInstances && !deviceInstances.isEmpty()) {

                                deviceInstanceDAO.deleteAll(deviceInstances);

                            }

                            List<LocationInstance> locationInstances = locationInstanceDAO
                                    .findAllByComponentNodeInstance(componentNodeInstance, null).getContent();

                            if (null != locationInstances && !locationInstances.isEmpty()) {

                                locationInstanceDAO.deleteAll(locationInstances);

                            }

                            if (flavorInstanceDAO.findByComponentNodeInstance(componentNodeInstance)
                                    .isPresent()) {

//                            FlavorInstance flavorInstance = flavorInstanceDAO.findByComponentNodeInstance(componentNodeInstance).get();

                                Query q = entityManager.createNativeQuery(
                                        "DELETE FROM flavor_instance WHERE component_node_instance = ?");
                                q.setParameter(1, componentNodeInstance.getComponentNodeInstanceID());
                                entityManager.joinTransaction();
                                q.executeUpdate();

//                            flavorInstanceDAO.delete(flavorInstance);

                            }

                            if (healthCheckInstanceDAO.findByComponentNodeInstance(componentNodeInstance)
                                    .isPresent()) {

//                            HealthCheckInstance healthCheckInstance = healthCheckInstanceDAO.findByComponentNodeInstance(componentNodeInstance).get();

                                Query q = entityManager.createNativeQuery(
                                        "DELETE FROM health_check_instance WHERE component_node_instance = ?");
                                q.setParameter(1, componentNodeInstance.getComponentNodeInstanceID());
                                entityManager.joinTransaction();
                                q.executeUpdate();

//                            healthCheckInstanceDAO.delete(healthCheckInstance);

                            }

                            List<ComponentNodeInstanceStatus> componentNodeInstanceStatuses = componentNodeInstanceStatusDAO
                                    .findAllByComponentNodeInstanceOrderByDateCreatedDesc(componentNodeInstance,
                                            null).getContent();

                            if (null != componentNodeInstanceStatuses && !componentNodeInstanceStatuses
                                    .isEmpty()) {

                                componentNodeInstanceStatuses.stream().forEach(componentNodeInstanceStatus -> {

                                    componentNodeInstanceStatus.setComponentNodeInstance(null);
                                    componentNodeInstanceStatusDAO.save(componentNodeInstanceStatus);

                                });

                            }

                            List<Constraint> constraints = constraintDAO
                                    .findAllByApplicationInstance(existingApplicationInstance, null).getContent();

                            if (null != constraints && !constraints.isEmpty()) {

                                constraints.stream().filter(
                                                constraint -> null != constraint.getComponentNodeInstance() && constraint
                                                        .getComponentNodeInstance().getComponentNodeInstanceID()
                                                        .equals(componentNodeInstance.getComponentNodeInstanceID()))
                                        .forEach(constraint -> constraintDAO.delete(constraint));

                            }

                            Query q = entityManager
                                    .createNativeQuery("DELETE FROM component_node_instance WHERE id = ?");
                            q.setParameter(1, componentNodeInstance.getComponentNodeInstanceID());
                            entityManager.joinTransaction();
                            q.executeUpdate();

                        }

                    });

                }

                existingApplicationInstance.setSlice(null);
                existingApplicationInstance.setStatus(ApplicationInstanceStatus.PENDING.name());
                existingApplicationInstance.setLastModified(new Date());
                applicationInstanceDAO.save(existingApplicationInstance);

            }

        }
    }

}
