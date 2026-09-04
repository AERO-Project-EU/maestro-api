package eu.orchestrator.backend.service.component;

import eu.orchestrator.backend.transfer.DockerTO;
import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.common.exception.NotAuthorizedException;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.transfer.DashboardTO;
import eu.orchestrator.backend.transfer.TOConverter;
import eu.orchestrator.backend.util.ConstantsUtil;
import eu.orchestrator.backend.util.Util;
import eu.orchestrator.common.util.NullCheckUtil;
import eu.orchestrator.elasticity.spi.adapter.ElasticityFrameworkBackend;
import eu.orchestrator.repository.dao.*;
import eu.orchestrator.repository.domain.*;
import eu.orchestrator.backend.service.application.ApplicationService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;

import static eu.orchestrator.repository.domain.QComponent.component;

@Service
@Transactional(rollbackOn = Exception.class)
public class ComponentNodeService {

    private static final Logger logger = Logger.getLogger(ComponentNodeService.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    ComponentDAO componentDAO;

    @Autowired
    ComponentNodeDAO componentNodeDAO;

    @Autowired
    PluginDAO pluginDAO;

    @Autowired
    LabelDAO labelDAO;

    @Autowired
    HealthCheckDAO healthCheckDAO;

    @Autowired
    RequirementDAO requirementDAO;

    @Autowired
    ServerlessPropertiesDAO serverlessPropertiesDAO;

    @Autowired
    InterfaceDAO interfaceDAO;

    @Autowired
    GraphLinkDAO graphLinkDAO;

    @Autowired
    VolumeDAO volumeDAO;

    @Autowired
    DeviceDAO deviceDAO;

    @Autowired
    EnvironmentalVariableDAO environmentalVariableDAO;

    @Autowired
    VulnerabilityDAO vulnerabilityDAO;

    @Autowired
    ApplicationService applicationService;

    @Autowired
    InterfaceService interfaceService;

    @Autowired
    SimpMessagingTemplate wsTemplate;

    @Resource(name = "elasticityFrameworkAdapters")
    private List elasticityFrameworkAdapters;

    @Value("${token.signer.secret}")
    private String secretToken;


    public Component fetchComponentById(Long id) {
        Optional<Component> componentOptional = componentDAO.findById(id);
        return componentOptional.orElse(null);
    }

    public Component fetchComponentByHexId(String hexId) {
        Optional<Component> componentOptional = componentDAO.findByHexID(hexId);
        return componentOptional.orElse(null);
    }

    public ComponentNode fetchComponentNodeById(Long id) {
        Optional<ComponentNode> componentNodeOptional = componentNodeDAO.findById(id);
        return componentNodeOptional.orElse(null);
    }

    public ComponentNode fetchComponentNodeByHexId(String hexId) {
        Optional<ComponentNode> componentNodeOptional = componentNodeDAO.findByHexID(hexId);
        return componentNodeOptional.orElse(null);
    }

    public List<Component> fetchAllComponents(String filters, Component fComponent, User authenticatedUser) {
        return (List<Component>) componentDAO.findAll(fetchPredicates(filters, fComponent, authenticatedUser));
    }

    public Page<Component> fetchComponents(Pageable pageable, String filters, Component fComponent, User authenticatedUser) {
        BooleanExpression predicate = fetchPredicates(filters, fComponent, authenticatedUser);
        Page<Component> page;
        if (pageable.getPageSize() > 100) {
            page = componentDAO.findAll(predicate, PageRequest.of(pageable.getPageNumber(), 100, pageable.getSort()));
        } else {
            page = componentDAO.findAll(predicate, pageable);
        }
        return new TOConverter(Component.class.getName(), page, pageable, authenticatedUser).convertToTOWithPermissions();
    }

    public List<Component> fetchAllByOrganizationOrPublicComponent(User authenticatedUser) {
        return componentDAO.findAllByOrganizationOrPublicComponent(authenticatedUser.getOrganization(), true);
    }

    public List<Component> fetchAllByUserOrPublicComponentOrderByNameAsc(User authenticatedUser) {
        return componentDAO.findAllByUserOrPublicComponentOrderByNameAsc(authenticatedUser, true);
    }

    public Page<Component> fetchFilteredComponents(Pageable pageable, Component fComponent, User authenticatedUser) {
        BooleanExpression predicate = component.eq(component);

        if (null != fComponent && null != fComponent.getName() && !fComponent.getName().isEmpty()) {
            predicate = predicate.and(component.name.containsIgnoreCase(fComponent.getName()));
        }

        // Filter out Traefik and LambdaProxy
        predicate = predicate.and(component.name.notEqualsIgnoreCase("Traefik"))
                .and(component.name.notEqualsIgnoreCase("LambdaProxy"));

        if (!authenticatedUser.isAdmin()) {
            predicate = predicate.and(component.organization.eq(authenticatedUser.getOrganization())
                    .or(component.publicComponent.eq(true)));
        }

        Page<Component> page;

        if (pageable.getPageSize() > 30) {
            page = componentDAO
                    .findAll(predicate, PageRequest.of(pageable.getPageNumber(), 30, pageable.getSort()));
        } else {
            page = componentDAO.findAll(predicate, pageable);
        }

        return page;

    }

    public List<Component> fetchCandidatesByInterfaceId(Long id, Long interfaceID, String name, User authenticatedUser) {
        try {
            Component component = fetchComponentById(id);
            Interface maestroInterface = interfaceService.fetchInterfaceById(interfaceID);

            if (NullCheckUtil.isNotEmpty(component) && NullCheckUtil.isEmpty(maestroInterface) &&
                    (authenticatedUser.isAdmin() || (component.getPublicComponent()) ||
                            (component.getOrganization().equals(authenticatedUser.getOrganization())))) {

                if (null == name) {
                    name = "";
                }

                return componentDAO.findAllByNameContainingIgnoreCaseAndExposedInterfacesContains(name, maestroInterface);
            } else {
                throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
        }
    }

    public DockerTO fetchDockerCredentials(Long id, User authenticatedUser) {
        try {
            Component component = fetchComponentById(id);

            if (NullCheckUtil.isNotEmpty(component) && Boolean.TRUE.equals(component.hasEditAllowance(authenticatedUser))) {
                return new DockerTO(component.getDockerImage(), component.getDockerRegistry(),
                    component.getDockerUsername(), Util.decrypt(component.getDockerPassword(), secretToken));
            } else {
                throw new NotAuthorizedException(GenericMessage.COMPONENT_FETCH_NOT_ALLOWED.getCode(), GenericMessage.COMPONENT_FETCH_NOT_ALLOWED);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
        }
    }

    public Component fetchById(Long id, User authenticatedUser) {
        try {
            Component exComponent = fetchComponentById(id);

            if (NullCheckUtil.isNotEmpty(exComponent) && Boolean.TRUE.equals(exComponent.hasEditAllowance(authenticatedUser))) {
                Component component = new Component();
                BeanUtils.copyProperties(exComponent, component);
                // filter plugins
                if (null != component.getPlugins()) {
                    List<Plugin> plugins = component.getPlugins().stream().filter(plugin -> !plugin.getDefaultPlugin()).collect(Collectors.toList());
                    component.setPlugins(new TreeSet<>(plugins));
                }
                return component;
            } else {
                throw new NotAuthorizedException(GenericMessage.COMPONENT_FETCH_NOT_ALLOWED.getCode(), GenericMessage.COMPONENT_FETCH_NOT_ALLOWED);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
        }
    }

    public void create(Component component, User authenticatedUser) {
        if (component.getPublicComponent() && !authenticatedUser.isAdmin()) {
            throw new NotAuthorizedException(GenericMessage.PUBLIC_COMPONENT_NOT_AUTHORIZED.getCode(), GenericMessage.PUBLIC_COMPONENT_NOT_AUTHORIZED);
        }

        try {
            // Check if component already exists
            if (authenticatedUser.isAdmin()) {
                //The component name must be unique cross platform for the Admin
                if (componentDAO.findByName(component.getName()).isPresent()) {
                    throw new GenericBusinessException(GenericMessage.COMPONENT_ALREADY_EXISTS.getCode(), GenericMessage.COMPONENT_ALREADY_EXISTS);
                }
            } else {
                //The component name must be unique in the organization AND in the public components
                if (componentDAO.findByNameAndOrganization(component.getName(), authenticatedUser.getOrganization()).isPresent()
                        || componentDAO.findByNameAndPublicComponent(component.getName(), true).isPresent()) {
                    throw new GenericBusinessException(GenericMessage.COMPONENT_ALREADY_EXISTS.getCode(), GenericMessage.COMPONENT_ALREADY_EXISTS);
                }
            }

            if (null != component.getExposedInterfaces() && !component.getExposedInterfaces().isEmpty()) {
                for (Interface inFace : component.getExposedInterfaces()) {
                    if (null != inFace.getName() && !inFace.getName().isEmpty()) {
                        if (NullCheckUtil.isNotEmpty(interfaceService.fetchInterfaceByName(inFace.getName()))) {
                            throw new GenericBusinessException(GenericMessage.INTERFACE_ALREADY_EXISTS.getCode(), GenericMessage.INTERFACE_ALREADY_EXISTS);
                        }
                    } else {
                        throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(), GenericMessage.REQUIRED_FIELDS_MISSING);
                    }
                }
            }

            // check if mode selected
            for (ElasticityFrameworkBackend elasticityAdapter : (List<ElasticityFrameworkBackend>) elasticityFrameworkAdapters) {
                if (elasticityAdapter.getElasticityType().getName().equals(component.getElasticityController())) {
                    if (null != elasticityAdapter.getElasticityType().getElasticityMode() && (null == component.getElasticityControllerMode() || component
                            .getElasticityControllerMode().isEmpty())) {
                        throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(), GenericMessage.REQUIRED_FIELDS_MISSING);
                    }
                }
            }

            component.setUser(authenticatedUser);
            component.setOrganization(authenticatedUser.getOrganization());

            if (null == component.getUlimitMemlockHard() || component.getUlimitMemlockHard().isEmpty()) {
                component.setUlimitMemlockHard(null);
            } else {
//                try {
                int num = Integer.parseInt(component.getUlimitMemlockHard());
                component.setUlimitMemlockHard(component.getUlimitMemlockHard());
//                } catch (NumberFormatException e) {
//                    throw new RequiredFieldsMissingException(GenericMessage.REQUIRED_FIELDS_WITH_PROPER_VALUES.getCode(),
//                            GenericMessage.REQUIRED_FIELDS_WITH_PROPER_VALUES);
//                }
            }

            if (null == component.getUlimitMemlockSoft() || component.getUlimitMemlockSoft().isEmpty()) {
                component.setUlimitMemlockSoft(null);
            } else {
//               try {
                int num = Integer.parseInt(component.getUlimitMemlockSoft());
                component.setUlimitMemlockSoft(component.getUlimitMemlockSoft());
//                } catch (NumberFormatException e) {
//                    throw new RequiredFieldsMissingException(GenericMessage.REQUIRED_FIELDS_WITH_PROPER_VALUES.getCode(),
//                            GenericMessage.REQUIRED_FIELDS_WITH_PROPER_VALUES);
//                }
            }

            if (null == component.getDockerExecutionUser() || component.getDockerExecutionUser().isEmpty()) {
                component.setDockerExecutionUser(null);
            } else {
                component.setDockerExecutionUser(component.getDockerExecutionUser());
            }

            // Runtime Class Name for K8S based deployments
            component.setKubernetesRuntimeClassName(component.getKubernetesRuntimeClassName());

            component.setDateCreated(new Date());

            // Store temp POJOs

            // Minimum Execution Requirements
            Requirement newRequirement = component.getRequirement();
            component.setRequirement(null);

            // Serverless Properties
            ServerlessProperties newServerlessProperties = component.getServerlessProperties();
            component.setServerlessProperties(null);

            // Health Check
            HealthCheck newHealth = component.getHealthCheck();
            component.setHealthCheck(null);

            // Environmental Variables
            List<EnvironmentalVariable> newEnvironmentalVariables = null;
            if (null != component.getEnvironmentalVariables() && !component.getEnvironmentalVariables()
                    .isEmpty()) {
                newEnvironmentalVariables = component.getEnvironmentalVariables();
                component.setEnvironmentalVariables(null);
            }

            // Exposed Interfaces
            List<Interface> newExposedInterfaces = null;
            if (null != component.getExposedInterfaces() && !component.getExposedInterfaces()
                    .isEmpty()) {
                newExposedInterfaces = component.getExposedInterfaces();
                component.setExposedInterfaces(null);

            }

            // Required Interfaces
            List<GraphLink> newGraphLinks = null;
            if (null != component.getRequiredInterfaces() && !component.getRequiredInterfaces()
                    .isEmpty()) {
                newGraphLinks = component.getRequiredInterfaces();
                component.setRequiredInterfaces(null);
            }

            // Plugins
            List<Plugin> newPlugins = new ArrayList<>();
            if (null != component.getPlugins() && !component.getPlugins().isEmpty()) {
                newPlugins.addAll(component.getPlugins());
            }

            List<Plugin> defaultPlugins = pluginDAO.findAllByDefaultPlugin(true);

            if (null != defaultPlugins && !defaultPlugins.isEmpty()) {

                defaultPlugins.stream().forEach(defaultPlugin -> {

                    if (newPlugins.stream()
                            .filter(plugin -> plugin.getPluginID().equals(defaultPlugin.getPluginID())).collect(
                                    Collectors.toList()).isEmpty()) {
                        newPlugins.add(defaultPlugin);
                    }

                });

            }

            component.setPlugins(new TreeSet<>(newPlugins));

            // Volumes
            List<Volume> newVolumes = null;
            if (null != component.getVolumes() && !component.getVolumes().isEmpty()) {
                newVolumes = component.getVolumes();
                component.setVolumes(null);
                boolean flagContainsNullFile = newVolumes.stream().anyMatch(volume -> null == volume.getFile());
                if (flagContainsNullFile) {
                    throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(), GenericMessage.REQUIRED_FIELDS_MISSING);
                }
            }

            // Devices
            List<Device> newDevices = null;
            if (null != component.getDevices() && !component.getDevices().isEmpty()) {
                newDevices = component.getDevices();
                component.setDevices(null);
            }

            // Labels
            List<Label> newLabels = new ArrayList<>();
            if (null != component.getLabels() && !component.getLabels().isEmpty()) {

                component.getLabels().stream().forEach(label -> {

                    if (null != label.getLabelID() && label.getLabelID() > 0) {

                        newLabels.add(labelDAO.findById(label.getLabelID()).get());

                    } else {

                        boolean rF = null != label.getName() && !label.getName().isEmpty();

                        if (rF) {

                            if (!labelDAO.findByName(label.getName()).isPresent()) {

                                label.setDateCreated(new Date());
                                label.setLastModified(new Date());

                                labelDAO.save(label);

                                newLabels.add(label);


                            } else {

                                newLabels.add(labelDAO.findByName(label.getName()).get());

                            }

                        }
                    }

                });

            }

            component.setLabels(new TreeSet<>(newLabels));

            component.setHexID(Util.createRandomHEXString());

            if (component.getDockerCredentialsUsing()) {

                if (component.getDockerUsername() == null ||
                        component.getDockerUsername().isEmpty()) {
                    throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(), GenericMessage.REQUIRED_FIELDS_MISSING);
                }

                if (component.getDockerCustomRegistry() && (component.getDockerRegistry() == null ||
                        component.getDockerRegistry().isEmpty())) {
                    throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(), GenericMessage.REQUIRED_FIELDS_MISSING);
                }

                if (!component.getDockerCustomRegistry()) {
                    component.setDockerRegistry("");
                }

                if (component.getDockerPassword() == null ||
                        component.getDockerPassword().isEmpty()) {
                    throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(), GenericMessage.REQUIRED_FIELDS_MISSING);

                } else {
                    component.setDockerPassword(Util.encrypt(component.getDockerPassword(), secretToken));

                }

            } else {
                component.setDockerUsername(null);
                component.setDockerRegistry(null);
                component.setDockerPassword(null);
            }

            component.setLastModified(new Date());
            componentDAO.save(component);

            // Minimum Execution Requirements
            newRequirement.setComponent(component);
            newRequirement.setDateCreated(new Date());
            newRequirement.setLastModified(new Date());
            requirementDAO.save(newRequirement);

            component.setRequirement(newRequirement);

            // Minimum Execution Requirements
            newServerlessProperties.setComponent(component);
            newServerlessProperties.setDateCreated(new Date());
            newServerlessProperties.setLastModified(new Date());
            serverlessPropertiesDAO.save(newServerlessProperties);

            // Health Check
            newHealth.setComponent(component);
            newHealth.setDateCreated(new Date());
            newHealth.setLastModified(new Date());
            healthCheckDAO.save(newHealth);

            component.setHealthCheck(newHealth);

            // Environmental Variables
            if (null != newEnvironmentalVariables && !newEnvironmentalVariables.isEmpty()) {
                List<EnvironmentalVariable> saveEnvs = new ArrayList<>();

                newEnvironmentalVariables.stream().forEach(envVar -> {

                    if (null != envVar.getKey() && !envVar.getKey().isEmpty() && null != envVar.getValue()
                            && !envVar.getValue().isEmpty()) {

                        envVar.setComponent(component);
                        envVar.setDateCreated(new Date());
                        envVar.setLastModified(new Date());
                        environmentalVariableDAO.save(envVar);
                        saveEnvs.add(envVar);
                    } else {
                        // TODO Throw exception and rollback
                        logger.log(Level.SEVERE, "Missing Required Fields");
                        throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(), GenericMessage.REQUIRED_FIELDS_MISSING);
                    }

                });

                if (null != saveEnvs && !saveEnvs.isEmpty()) {
                    component.setEnvironmentalVariables(saveEnvs);
                }
            }

            // Exposed Interfaces
            if (null != newExposedInterfaces && !newExposedInterfaces.isEmpty()) {

                ArrayList<Interface> saveInterfaces = new ArrayList<>();

                newExposedInterfaces.stream().forEach(expInterface -> {

                    if (null != expInterface.getName() && !expInterface.getName().isEmpty()
                            && null != expInterface.getPort() && !expInterface.getPort().isEmpty()
                            && null != expInterface.getTransmissionProtocol() && !expInterface
                            .getTransmissionProtocol().isEmpty()
                            && null != expInterface.getInterfaceType() && !expInterface.getInterfaceType()
                            .isEmpty()) {

                        expInterface.setComponent(component);
                        expInterface.setDateCreated(new Date());
                        expInterface.setLastModified(new Date());
                        interfaceDAO.save(expInterface);
                        saveInterfaces.add(expInterface);

                    } else {
                        logger.log(Level.SEVERE, GenericMessage.REQUIRED_FIELDS_MISSING.getMessageEN());
                        throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(), GenericMessage.REQUIRED_FIELDS_MISSING);
                    }

                });

                if (null != saveInterfaces && !saveInterfaces.isEmpty()) {
                    component.setExposedInterfaces(saveInterfaces);
                }
            }

            // Required Interfaces
            if (null != newGraphLinks && !newGraphLinks.isEmpty()) {

                ArrayList<GraphLink> saveGraphLinks = new ArrayList<>();
                newGraphLinks.stream().forEach(graphLink -> {

                    if (null != graphLink.getInterfaceObj()) {

                        graphLink.setInterfaceObj(
                                interfaceDAO.findById(graphLink.getInterfaceObj().getInterfaceID()).get());
                        graphLink.setComponent(component);
                        graphLink.setDateCreated(new Date());
                        graphLink.setLastModified(new Date());
                        graphLinkDAO.save(graphLink);
                        saveGraphLinks.add(graphLink);

                    } else {
                        logger.log(Level.SEVERE, GenericMessage.REQUIRED_FIELDS_MISSING.getMessageEN());
                        throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(), GenericMessage.REQUIRED_FIELDS_MISSING);
                    }

                });

                if (null != saveGraphLinks && !saveGraphLinks.isEmpty()) {
                    component.setRequiredInterfaces(saveGraphLinks);
                }
            }

            // Volumes
            if (null != newVolumes && !newVolumes.isEmpty()) {

                List<Volume> saveVolume = new ArrayList<>();

                newVolumes.stream().forEach(volume -> {

                    if (null != volume.getDockerPath() && !volume.getDockerPath().isEmpty()) {

                        if (volume.getDockerPath().charAt(0) != '/') {
                            volume.setDockerPath("/" + volume.getDockerPath());
                        }
                        volume.setComponent(component);
                        volume.setDateCreated(new Date());
                        volume.setLastModified(new Date());
                        volumeDAO.save(volume);
                        saveVolume.add(volume);
                    } else {
                        logger.log(Level.SEVERE, GenericMessage.REQUIRED_FIELDS_MISSING.getMessageEN());
                        throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(), GenericMessage.REQUIRED_FIELDS_MISSING);
                    }

                });

                if (null != saveVolume && !saveVolume.isEmpty()) {
                    component.setVolumes(saveVolume);
                }
            }

            // Devices
            if (null != newDevices && !newDevices.isEmpty()) {
                ArrayList<Device> saveDevices = new ArrayList<>();
                newDevices.stream().forEach(device -> {

                    if (null != device.getKey() && !device.getKey().isEmpty() && null != device.getValue()
                            && !device.getValue().isEmpty()) {

                        device.setComponent(component);
                        device.setDateCreated(new Date());
                        device.setLastModified(new Date());
                        deviceDAO.save(device);
                        saveDevices.add(device);

                    } else {
                        logger.log(Level.SEVERE, GenericMessage.REQUIRED_FIELDS_MISSING.getMessageEN());
                        throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(), GenericMessage.REQUIRED_FIELDS_MISSING);
                    }

                });

                if (null != saveDevices && !saveDevices.isEmpty()) {
                    component.setDevices(saveDevices);
                }

            }

            try {
                DashboardTO dashboardTO = new DashboardTO();
                dashboardTO.setOverview(true);
                String notificationAsString = null;
                notificationAsString = objectMapper.writeValueAsString(dashboardTO);
                wsTemplate.convertAndSend(ConstantsUtil.DASHBOARD_TOPIC, notificationAsString);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
        }
    }

    public boolean updateWithInterfaces(Component component, User authenticatedUser) {


        if (null!=component.getRequiredInterfaces() && !component.getRequiredInterfaces().isEmpty()){

            List<Long> interfaceIDsTemp = component.getRequiredInterfaces().stream().
                    map(x -> x.getInterfaceObj().getInterfaceID()).collect(Collectors.toList());

            boolean hasItsOwnInterfacesAsRequired = interfaceIDsTemp.stream()
                    .anyMatch(element -> interfaceDAO.findById(element).get().getComponent().getId() == component.getId());

            if (hasItsOwnInterfacesAsRequired) {
                throw new GenericBusinessException(GenericMessage.COMPONENT_HAS_ITS_OWN_INTERFACE_AS_REQUIRED.getCode(),
                        GenericMessage.COMPONENT_HAS_ITS_OWN_INTERFACE_AS_REQUIRED);
            }
        }


        boolean changeInterfaces = false;

        // Check if component already exists or not
        Optional<Component> existingComponentOP;
        if (null!=component.getId()) {
            existingComponentOP = componentDAO.findById(component.getId());
        } else {
            throw new GenericBusinessException(GenericMessage.COMPONENT_ID_NULL.getCode(),
                    GenericMessage.COMPONENT_ID_NULL);
        }

        if (existingComponentOP.isPresent() && existingComponentOP.get().hasEditAllowance(authenticatedUser)) {

            Component existingComponent = existingComponentOP.get();

            if (!existingComponent.getName().equals(component.getName())) {

                // Check if component already exists
                if (authenticatedUser.isAdmin()) {
                    //The component name must be unique cross platform for the Admin
                    if (componentDAO.findByName(component.getName()).isPresent()) {
                        throw new GenericBusinessException(GenericMessage.COMPONENT_ALREADY_EXISTS.getCode(), GenericMessage.COMPONENT_ALREADY_EXISTS);
                    }
                } else {
                    //The component name must be unique in the organization AND in the public components
                    if (componentDAO.findByNameAndOrganization(component.getName(), authenticatedUser.getOrganization()).isPresent()
                            || componentDAO.findByNameAndPublicComponent(component.getName(), true).isPresent()) {
                        throw new GenericBusinessException(GenericMessage.COMPONENT_ALREADY_EXISTS.getCode(), GenericMessage.COMPONENT_ALREADY_EXISTS);
                    }
                }

            }

            if (null != component.getExposedInterfaces() && !component.getExposedInterfaces().isEmpty()) {
                for (Interface inFace : component.getExposedInterfaces()) {
                    if (null != inFace.getName() && !inFace.getName().isEmpty()) {
                        if (null == inFace.getInterfaceID() && interfaceDAO.findByName(inFace.getName()).isPresent()) {
                            throw new GenericBusinessException(GenericMessage.INTERFACE_ALREADY_EXISTS.getCode(), GenericMessage.INTERFACE_ALREADY_EXISTS);
                        }
                    } else {
                        throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(), GenericMessage.REQUIRED_FIELDS_MISSING);
                    }
                }
            }

            // check if mode selected
            for (ElasticityFrameworkBackend elasticityAdapter : (List<ElasticityFrameworkBackend>) elasticityFrameworkAdapters) {
                if (elasticityAdapter.getElasticityType().getName().equals(component.getElasticityController())) {
                    if (null != elasticityAdapter.getElasticityType().getElasticityMode() && (null == component.getElasticityControllerMode()
                            || component.getElasticityControllerMode().isEmpty())) {
                        throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(), GenericMessage.REQUIRED_FIELDS_MISSING);
                    }
                }
            }

            existingComponent.setName(component.getName());
            existingComponent.setArchitecture(component.getArchitecture());
            existingComponent.setElasticityController(component.getElasticityController());
            existingComponent.setElasticityControllerMode(component.getElasticityControllerMode());

            existingComponent.setDockerImage(component.getDockerImage());

            //check if isPublic
            if (!existingComponent.getPublicComponent().equals(component.getPublicComponent())) {
                if (authenticatedUser.isAdmin() && existingComponent.getOrganization().getName()
                        .equals("Admin_Organization")) {
                    existingComponent.setPublicComponent(component.getPublicComponent());
                } else {
                    throw new GenericBusinessException(GenericMessage.PUBLIC_PLUGIN_NOT_AUTHORIZED.getCode(), GenericMessage.PUBLIC_PLUGIN_NOT_AUTHORIZED);
                }
            }

            existingComponent.setLastModified(new Date());

            if (null != component.getLabels() && !component.getLabels().isEmpty()) {

                List<Label> newLabels = new ArrayList<>();

                component.getLabels().stream().forEach(label -> {

                    if (null != label.getLabelID() && label.getLabelID() > 0) {

                        newLabels.add(labelDAO.findById(label.getLabelID()).get());

                    } else {

                        boolean rF = null != label.getName() && !label.getName().isEmpty();

                        if (rF) {

                            if (!labelDAO.findByName(label.getName()).isPresent()) {

                                label.setDateCreated(new Date());
                                label.setLastModified(new Date());

                                labelDAO.save(label);

                                newLabels.add(label);


                            } else {

                                newLabels.add(labelDAO.findByName(label.getName()).get());

                            }

                        }
                    }

                });

                existingComponent.setLabels(new TreeSet<>(newLabels));
            } else {
                existingComponent.setLabels(null);
            }

            List<Plugin> newPlugins = new ArrayList<>();
            if (null != component.getPlugins() && !component.getPlugins().isEmpty()) {

                component.getPlugins().stream().forEach(plugin -> {

                    if (null != plugin.getPluginID() && plugin.getPluginID() > 0) {

                        newPlugins.add(pluginDAO.findById(plugin.getPluginID()).get());

                    }

                });

                List<Plugin> defaultPlugins = pluginDAO.findAllByDefaultPlugin(true);

                if (null != defaultPlugins && !defaultPlugins.isEmpty()) {

                    defaultPlugins.stream().forEach(defaultPlugin -> {

                        if (newPlugins.stream()
                                .filter(plugin -> plugin.getPluginID().equals(defaultPlugin.getPluginID()))
                                .collect(Collectors.toList()).isEmpty()) {
                            newPlugins.add(defaultPlugin);
                        }

                    });

                }

            } else {

                List<Plugin> defaultPlugins = pluginDAO.findAllByDefaultPlugin(true);

                if (null != defaultPlugins && !defaultPlugins.isEmpty()) {

                    newPlugins.addAll(defaultPlugins);

                }
            }

            existingComponent.setPlugins(new TreeSet<>(newPlugins));

            if (component.getDockerCredentialsUsing()) {

                // username is null or `` store null into database
                if (component.getDockerUsername() == null ||
                        component.getDockerUsername().isEmpty()) {
                    existingComponent.setDockerUsername(null);

                } else {
                    existingComponent.setDockerUsername(component.getDockerUsername());
                }

                // registry is null or `` store null into database
                // else store encrypt password
                if (component.getDockerRegistry() == null && component.getDockerCustomRegistry()) {
                    existingComponent.setDockerRegistry(null);

                } else {
                    if (component.getDockerRegistry().isEmpty()) {
                        existingComponent.setDockerRegistry("");

                    } else {
                        existingComponent.setDockerRegistry(component.getDockerRegistry());

                    }
                }

                // password is null or `` store null into database
                if (component.getDockerPassword() == null ||
                        component.getDockerPassword().isEmpty()) {
                    existingComponent.setDockerPassword(null);

                } else {
                    if (null == existingComponent.getDockerPassword() || !existingComponent.getDockerPassword().equals(component.getDockerPassword())) {
                        existingComponent.setDockerPassword(Util.encrypt(component.getDockerPassword(), secretToken));
                    }
                }

                //check if something is missing
                if ((existingComponent.getDockerPassword() == null && (existingComponent.getDockerUsername() != null
                        || existingComponent.getDockerRegistry() != null)) ||
                        (existingComponent.getDockerRegistry() == null && (existingComponent.getDockerUsername() != null
                                || existingComponent.getDockerPassword() != null)) ||
                        (existingComponent.getDockerUsername() == null && (existingComponent.getDockerRegistry() != null
                                || existingComponent.getDockerPassword() != null))) {
                    throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(), GenericMessage.REQUIRED_FIELDS_MISSING);
                }

            }

            existingComponent.setCapabilityAdds(component.getCapabilityAdds());
            existingComponent.setCapabilityDrops(component.getCapabilityDrops());
            existingComponent.setNetworkModeHost(component.getNetworkModeHost());
            existingComponent.setHostname(component.getHostname());
            existingComponent.setPrivilege(component.getPrivilege());
            existingComponent.setSharedMemorySize(component.getSharedMemorySize());
            existingComponent.setCommand(component.getCommand());

            if (null == component.getUlimitMemlockHard() || component.getUlimitMemlockHard().isEmpty()) {
                existingComponent.setUlimitMemlockHard(null);
            } else {

                //try {
                int num = Integer.parseInt(component.getUlimitMemlockHard());
                existingComponent.setUlimitMemlockHard(component.getUlimitMemlockHard());
                //                    } catch (NumberFormatException e) {
                //                        handleException(request);
                //                    }
            }

            if (null == component.getUlimitMemlockSoft() || component.getUlimitMemlockSoft().isEmpty()) {
                existingComponent.setUlimitMemlockSoft(null);
            } else {
                //                    try {
                int num = Integer.parseInt(component.getUlimitMemlockSoft());
                existingComponent.setUlimitMemlockSoft(component.getUlimitMemlockSoft());
                //                    } catch (NumberFormatException e) {
                //                        handleException(request);
                //                    }

            }

            if (null == component.getDockerExecutionUser() || component.getDockerExecutionUser().isEmpty()) {
                existingComponent.setDockerExecutionUser(null);
            } else {
                existingComponent.setDockerExecutionUser(component.getDockerExecutionUser());
            }

            existingComponent.setKubernetesRuntimeClassName(component.getKubernetesRuntimeClassName());

            componentDAO.save(existingComponent);

            // Minimum Execution Requirements
            Optional<Requirement> exRequirementOP = requirementDAO.findByComponent(existingComponent);
            if (exRequirementOP.isPresent()) {

                Requirement exRequirement = exRequirementOP.get();
                exRequirement.setvCPUs(component.getRequirement().getvCPUs());
                exRequirement.setRam(component.getRequirement().getRam());
                exRequirement.setStorage(component.getRequirement().getStorage());
                exRequirement.setHypervisorType(component.getRequirement().getHypervisorType());
                exRequirement.setGpuRequired(
                        null != component.getRequirement().getGpuRequired() ? component.getRequirement()
                                .getGpuRequired().booleanValue() : null);
                exRequirement.setServerlessEnabled(
                        null != component.getRequirement().getServerlessEnabled() ? component.getRequirement()
                                .getServerlessEnabled().booleanValue() : null);
                exRequirement.setLastModified(new Date());
                requirementDAO.save(exRequirement);
            }


            // Serverless Properties
            Optional<ServerlessProperties> exServerlessPropertiesOP = serverlessPropertiesDAO.findByComponent(existingComponent);
            if (exServerlessPropertiesOP.isPresent()) {

                ServerlessProperties exServerlessProperties = exServerlessPropertiesOP.get();


                exServerlessProperties.setAutoscaler(null != component.getServerlessProperties().getAutoscaler() && !component.getServerlessProperties()
                        .getAutoscaler().isEmpty()? component.getServerlessProperties().getAutoscaler() : null);

                exServerlessProperties.setMetric(null != component.getServerlessProperties().getMetric() && !component.getServerlessProperties()
                        .getMetric().isEmpty()? component.getServerlessProperties().getMetric() : null);

                exServerlessProperties.setTargetValue(null != component.getServerlessProperties().getTargetValue() && !component.getServerlessProperties()
                        .getTargetValue().isEmpty()? component.getServerlessProperties().getTargetValue() : null);

                exServerlessProperties.setMinScale(null != component.getServerlessProperties().getMinScale() && !component.getServerlessProperties()
                        .getMinScale().isEmpty()? component.getServerlessProperties().getMinScale() : null);

                exServerlessProperties.setMaxScale(null != component.getServerlessProperties().getMaxScale() && !component.getServerlessProperties()
                        .getMaxScale().isEmpty()? component.getServerlessProperties().getMaxScale() : null);

                exServerlessProperties.setWindowSize(null != component.getServerlessProperties().getWindowSize() && !component.getServerlessProperties()
                        .getWindowSize().isEmpty()? component.getServerlessProperties().getWindowSize() : null);

                exServerlessProperties.setLastModified(new Date());

                serverlessPropertiesDAO.save(exServerlessProperties);
            }
            // Health Checks
            Optional<HealthCheck> exHealthCheckOP = healthCheckDAO.findByComponent(existingComponent);
            if (exHealthCheckOP.isPresent()) {

                HealthCheck exHealthCheck = exHealthCheckOP.get();
                exHealthCheck.setInterval(component.getHealthCheck().getInterval());
                exHealthCheck.setHttpURL(
                        null != component.getHealthCheck().getHttpURL() && !component.getHealthCheck()
                                .getHttpURL().isEmpty() ? component.getHealthCheck().getHttpURL() : null);
                exHealthCheck.setArgs(
                        null != component.getHealthCheck().getArgs() && !component.getHealthCheck()
                                .getArgs().isEmpty() ? component.getHealthCheck().getArgs() : null);
                exHealthCheck.setLastModified(new Date());
                healthCheckDAO.save(exHealthCheck);
            }

            // Environmental Variables
            List<Long> existingEnvironmentalVariableIDs = existingComponent
                    .getEnvironmentalVariableIDs();
            List<EnvironmentalVariable> newEnvironmentalVariables = component
                    .getEnvironmentalVariables();

            List<EnvironmentalVariable> allExistingEnvironmentalVariable = new ArrayList<>();
            if (null != newEnvironmentalVariables && !newEnvironmentalVariables.isEmpty()) {
                List<Long> newEnvironmentalVariablesIDs = newEnvironmentalVariables.stream()
                        .filter(newenv -> null != newenv.getEnvironmentalVariableID())
                        .map(newenv -> newenv.getEnvironmentalVariableID())
                        .collect(Collectors.toList());


                for (Long existingEnvironmentalVariableID : existingEnvironmentalVariableIDs) {
                    if (newEnvironmentalVariablesIDs.contains(existingEnvironmentalVariableID)) {
                        allExistingEnvironmentalVariable.add(environmentalVariableDAO.findById(existingEnvironmentalVariableID).get());
                    }
                }
            }

            existingComponent.setEnvironmentalVariables(allExistingEnvironmentalVariable);

            if (null != newEnvironmentalVariables && !newEnvironmentalVariables.isEmpty()) {

                newEnvironmentalVariables.stream().forEach(newEnvironmentalVariable -> {

                    if (null != newEnvironmentalVariable.getEnvironmentalVariableID()
                            && newEnvironmentalVariable.getEnvironmentalVariableID() != 0) {

                        // Existing, needs to be updated
                        Optional<EnvironmentalVariable> exEnvironmentalVariableOP = environmentalVariableDAO
                                .findById(newEnvironmentalVariable.getEnvironmentalVariableID());

                        if (exEnvironmentalVariableOP.isPresent()) {

                            EnvironmentalVariable exEnvironmentalVariable = exEnvironmentalVariableOP.get();
                            exEnvironmentalVariable.setDateCreated(new Date());
                            exEnvironmentalVariable.setComponent(existingComponent);
                            exEnvironmentalVariable.setKey(newEnvironmentalVariable.getKey());
                            exEnvironmentalVariable.setValue(newEnvironmentalVariable.getValue());
                            exEnvironmentalVariable.setLastModified(new Date());
                            environmentalVariableDAO.save(exEnvironmentalVariable);

                            existingEnvironmentalVariableIDs
                                    .remove(newEnvironmentalVariable.getEnvironmentalVariableID());

                        }

                    } else {

                        // New, needs to be added
                        if (null != newEnvironmentalVariable.getKey() && !newEnvironmentalVariable.getKey()
                                .isEmpty()
                                && null != newEnvironmentalVariable.getValue() && !newEnvironmentalVariable
                                .getValue()
                                .isEmpty()) {
                            newEnvironmentalVariable.setLastModified(new Date());
                            newEnvironmentalVariable.setDateCreated(new Date());
                            newEnvironmentalVariable.setComponent(existingComponent);
                            environmentalVariableDAO.save(newEnvironmentalVariable);

                        } else {
                            logger.log(Level.SEVERE, GenericMessage.REQUIRED_FIELDS_MISSING.getMessageEN());
                            throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(), GenericMessage.REQUIRED_FIELDS_MISSING);

                        }

                    }

                });

            }

            if (!existingEnvironmentalVariableIDs.isEmpty()) {

                existingEnvironmentalVariableIDs.stream().forEach(existingEnvironmentalVariableID -> {
                    environmentalVariableDAO.deleteById(existingEnvironmentalVariableID);
                });

            }

            // Exposed Interfaces
            Long componentNodeExposedInterfacesCount = componentNodeDAO.findAllByComponent(existingComponent).stream().count();

            if (componentNodeExposedInterfacesCount == 0) {

                changeInterfaces = true;

                List<Long> existingInterfaceIDs = existingComponent
                        .getInterfaceIDs();
                List<Interface> newInterfaces = component
                        .getExposedInterfaces();
                List<Interface> allExistingInterfaces = new ArrayList<>();

                if (null != newInterfaces && !newInterfaces.isEmpty()) {

                    List<Long> newInterfaceIDs = newInterfaces.stream()
                            .filter(newInterface -> null != newInterface.getInterfaceID())
                            .map(newInterface -> newInterface.getInterfaceID())
                            .collect(Collectors.toList());


                    for (Long existingInterfceID : existingInterfaceIDs) {
                        if (null != newInterfaceIDs && !newInterfaceIDs.isEmpty() && newInterfaceIDs.contains(existingInterfceID)) {
                            allExistingInterfaces.add(interfaceDAO.findById(existingInterfceID).get());
                        }
                    }
                }
                existingComponent.setExposedInterfaces(allExistingInterfaces);

                if (null != newInterfaces && !newInterfaces.isEmpty()) {

                    newInterfaces.stream().forEach(newInterface -> {

                        if (null != newInterface.getInterfaceID()
                                && newInterface.getInterfaceID() != 0) {

                            // Existing, needs to be updated
                            Optional<Interface> exInterfaceOP = interfaceDAO
                                    .findById(newInterface.getInterfaceID());

                            if (exInterfaceOP.isPresent()) {

                                Interface exInterface = exInterfaceOP.get();
                                exInterface.setDateCreated(new Date());
                                exInterface.setComponent(existingComponent);
                                exInterface.setInterfaceType(newInterface.getInterfaceType());
                                exInterface.setName(newInterface.getName());
                                exInterface.setPort(newInterface.getPort());
                                exInterface.setTransmissionProtocol(newInterface.getTransmissionProtocol());
                                exInterface.setVna(
                                        null != newInterface.getVna() && !newInterface.getVna().isEmpty()
                                                ? newInterface.getVna() : "VNA0");
                                exInterface.setLastModified(new Date());
                                interfaceDAO.save(exInterface);

                                existingInterfaceIDs.remove(newInterface.getInterfaceID());

                            }

                        } else {

                            // New, needs to be added
                            if (null != newInterface.getName() && !newInterface.getName().isEmpty()
                                    && null != newInterface.getPort() && !newInterface.getPort().isEmpty()
                                    && null != newInterface.getTransmissionProtocol() && !newInterface
                                    .getTransmissionProtocol().isEmpty()
                                    && null != newInterface.getInterfaceType() && !newInterface.getInterfaceType()
                                    .isEmpty()) {

                                newInterface.setLastModified(new Date());
                                newInterface.setDateCreated(new Date());
                                newInterface.setComponent(existingComponent);
                                interfaceDAO.save(newInterface);

                            } else {
                                logger.log(Level.SEVERE, GenericMessage.REQUIRED_FIELDS_MISSING.getMessageEN());
                                throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(), GenericMessage.REQUIRED_FIELDS_MISSING);

                            }

                        }

                    });

                }

                if (!existingInterfaceIDs.isEmpty()) {

                    existingInterfaceIDs.stream().forEach(existingInterfaceID -> {

                        Optional<Interface> existingInterface = interfaceDAO.findById(existingInterfaceID);

                        if (existingInterface.isPresent()) {

                            List<GraphLink> graphLinkOfCurrentInterface = graphLinkDAO.findByInterfaceObj(existingInterface.get());

                            graphLinkOfCurrentInterface.stream().forEach(graphLink -> {
                                graphLink.setInterfaceObj(null);
                                graphLinkDAO.delete(graphLink);
                            });

                            Long graphLinkInterfaceCounter = graphLinkDAO.countAllByInterfaceObj(existingInterface.get());
                            if (graphLinkInterfaceCounter == 0) {
                                interfaceDAO.deleteById(existingInterfaceID);
                            }
                        }
                    });

                }
            }

            // Required Interfaces
            Long componentNodeRequiredInterfacesCount = componentNodeDAO.findAllByComponent(existingComponent).stream().count();

            if (componentNodeRequiredInterfacesCount == 0) {

                changeInterfaces = true;

                List<Long> existingGraphLinkIDs = existingComponent
                        .getGraphLinkIDs();
                List<GraphLink> newGraphLinks = component
                        .getRequiredInterfaces();

                if (null != newGraphLinks && !newGraphLinks.isEmpty()) {

                    List<Long> newGraphLinksIDs = newGraphLinks.stream()
                            .filter(newGraph -> null != newGraph.getGraphLinkID())
                            .map(newGraph -> newGraph.getGraphLinkID())
                            .collect(Collectors.toList());

                    List<GraphLink> allExistingGraphLinks = new ArrayList<>();

                    for (Long existingGraphLinkID : existingGraphLinkIDs) {
                        if (null != newGraphLinksIDs && !newGraphLinksIDs.isEmpty() && newGraphLinksIDs
                                .contains(existingGraphLinkID)) {
                            allExistingGraphLinks.add(graphLinkDAO.findById(existingGraphLinkID).get());
                        }
                    }

                    existingComponent.setRequiredInterfaces(allExistingGraphLinks);
                }

                if (null != newGraphLinks && !newGraphLinks.isEmpty()) {

                    newGraphLinks.stream().forEach(newGraphLink -> {

                        if (null != newGraphLink.getGraphLinkID()
                                && newGraphLink.getGraphLinkID() != 0) {

                            // Existing, needs to be updated
                            Optional<GraphLink> exGraphLinkOP = graphLinkDAO
                                    .findById(newGraphLink.getGraphLinkID());

                            if (exGraphLinkOP.isPresent()) {

                                GraphLink exGraphLink = exGraphLinkOP.get();
                                exGraphLink.setDateCreated(new Date());
                                exGraphLink.setComponent(existingComponent);
                                exGraphLink.setFriendlyName(newGraphLink.getFriendlyName());
                                exGraphLink.setInterfaceObj(
                                        interfaceDAO.findById(newGraphLink.getInterfaceObj().getInterfaceID())
                                                .get());
                                exGraphLink.setLastModified(new Date());
                                graphLinkDAO.save(exGraphLink);

                                existingGraphLinkIDs.remove(newGraphLink.getGraphLinkID());

                            }

                        } else {

                            if (null != newGraphLink.getInterfaceObj()) {

                                // New, needs to be added
                                newGraphLink.setLastModified(new Date());
                                newGraphLink.setDateCreated(new Date());
                                newGraphLink.setInterfaceObj(
                                        interfaceDAO.findById(newGraphLink.getInterfaceObj().getInterfaceID())
                                                .get());
                                newGraphLink.setComponent(existingComponent);
                                graphLinkDAO.save(newGraphLink);

                            } else {
                                logger.log(Level.SEVERE, GenericMessage.REQUIRED_FIELDS_MISSING.getMessageEN());
                                throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(), GenericMessage.REQUIRED_FIELDS_MISSING);
                            }

                        }

                    });

                }

                if (!existingGraphLinkIDs.isEmpty()) {

                    existingGraphLinkIDs.stream().forEach(existingGraphLinkID -> {
                        GraphLink temp = graphLinkDAO.findById(existingGraphLinkID).get();
                        temp.setInterfaceObj(null);
                        graphLinkDAO.save(temp);
                        graphLinkDAO.deleteById(existingGraphLinkID);
                    });

                }

            }
            // Volumes
            List<Long> existingVolumeIDs = existingComponent
                    .getVolumeIDs();
            List<Volume> newVolumes = component
                    .getVolumes();

            List<Long> newVolumeIDs = null;
            if(null != newVolumes && !newVolumes.isEmpty() ) {
                newVolumeIDs = newVolumes.stream()
                        .filter(volume -> null != volume.getVolumeID())
                        .map(newVolume -> newVolume.getVolumeID())
                        .collect(Collectors.toList());
            }

            List<Volume> allExistingVolumes = new ArrayList<>();

            for (Long existingVolumeID : existingVolumeIDs) {
                if (null != newVolumeIDs && !newVolumeIDs.isEmpty() && newVolumeIDs.contains(existingVolumeID)) {
                    allExistingVolumes.add(volumeDAO.findById(existingVolumeID).get());
                }
            }

            existingComponent.setVolumes(allExistingVolumes);

            if (null != newVolumes && !newVolumes.isEmpty()) {

                newVolumes.stream().forEach(newVolume -> {

                    if (null != newVolume.getVolumeID()
                            && newVolume.getVolumeID() != 0) {

                        // Existing, needs to be updated
                        Optional<Volume> exVolumeOP = volumeDAO.findById(newVolume.getVolumeID());

                        if (exVolumeOP.isPresent()) {

                            Volume exVolume = exVolumeOP.get();
                            exVolume.setDateCreated(new Date());
                            exVolume.setComponent(existingComponent);
                            if (newVolume.getDockerPath().charAt(0) != '/') {
                                newVolume.setDockerPath("/" + newVolume.getDockerPath());
                            }
                            exVolume.setDockerPath(newVolume.getDockerPath());
                            exVolume.setFile(newVolume.getFile());
                            exVolume.setLastModified(new Date());
                            volumeDAO.save(exVolume);

                            existingVolumeIDs.remove(newVolume.getVolumeID());

                        }

                    } else {

                        if (null != newVolume.getDockerPath() && !newVolume.getDockerPath().isEmpty()) {

                            // New, needs to be added
                            newVolume.setLastModified(new Date());
                            newVolume.setDateCreated(new Date());
                            if (newVolume.getDockerPath().charAt(0) != '/') {
                                newVolume.setDockerPath("/" + newVolume.getDockerPath());
                            }
                            newVolume.setComponent(existingComponent);
                            volumeDAO.save(newVolume);

                        } else {
                            logger.log(Level.SEVERE, GenericMessage.REQUIRED_FIELDS_MISSING.getMessageEN());
                            throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(), GenericMessage.REQUIRED_FIELDS_MISSING);

                        }

                    }

                });

            }

            if (!existingVolumeIDs.isEmpty()) {
                existingVolumeIDs.stream().forEach(existingVolumeID -> {
                    volumeDAO.deleteById(existingVolumeID);
                });
            }

            // Devices
            List<Long> existingDeviceIDs = existingComponent
                    .getDeviceIDs();
            List<Device> newDevices = component
                    .getDevices();

            List<Long> newDevicesIDs = null;

            if(null != newDevices && !newDevices.isEmpty()) {
                newDevicesIDs = newDevices.stream()
                        .filter(newDevice -> null != newDevice.getDeviceID())
                        .map(newDevice -> newDevice.getDeviceID())
                        .collect(Collectors.toList());
            }
            List<Device> allDevices = new ArrayList<>();

            for (Long existingDeviceID : existingDeviceIDs) {
                if (null != newDevicesIDs && !newDevicesIDs.isEmpty() && newDevicesIDs.contains(existingDeviceID)) {
                    allDevices.add(deviceDAO.findById(existingDeviceID).get());
                }
            }

            existingComponent.setDevices(allDevices);

            if (null != newDevices && !newDevices.isEmpty()) {

                newDevices.stream().forEach(newDevice -> {

                    if (null != newDevice.getDeviceID()
                            && newDevice.getDeviceID() != 0) {

                        // Existing, needs to be updated
                        Optional<Device> exDeviceOP = deviceDAO.findById(newDevice.getDeviceID());

                        if (exDeviceOP.isPresent()) {

                            Device exDevice = exDeviceOP.get();
                            exDevice.setDateCreated(new Date());
                            exDevice.setComponent(existingComponent);
                            exDevice.setKey(newDevice.getKey());
                            exDevice.setValue(newDevice.getValue());
                            exDevice.setLastModified(new Date());
                            deviceDAO.save(exDevice);

                            existingDeviceIDs.remove(newDevice.getDeviceID());

                        }

                    } else {

                        // New, needs to be added
                        if (null != newDevice.getKey() && !newDevice.getKey().isEmpty() && null != newDevice
                                .getValue() && !newDevice.getValue().isEmpty()) {
                            newDevice.setLastModified(new Date());
                            newDevice.setDateCreated(new Date());
                            newDevice.setComponent(existingComponent);
                            deviceDAO.save(newDevice);

                        } else {
                            logger.log(Level.SEVERE, GenericMessage.REQUIRED_FIELDS_MISSING.getMessageEN());
                            throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(), GenericMessage.REQUIRED_FIELDS_MISSING);
                        }

                    }

                });

            }

            if (!existingDeviceIDs.isEmpty()) {

                existingDeviceIDs.stream().forEach(existingDeviceID -> {
                    deviceDAO.deleteById(existingDeviceID);
                });

            }

            return changeInterfaces;

        } else {
            throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
        }

    }

    public void delete(Long id, User authenticatedUser) {
        // Check if Component exists or not
        Component existingComponent = fetchComponentById(id);

        if (NullCheckUtil.isNotEmpty(existingComponent) && existingComponent.hasDeleteAllowance(authenticatedUser)) {

            // Check if this component is used in application graph
            List<ComponentNode> componentNodes = componentNodeDAO
                    .findAllByComponent(existingComponent);

            if (null != componentNodes && !componentNodes.isEmpty()) {

                // Check if it is contained in applications
                List<Application> applications = applicationService.fetchAllByComponentNodesIsIn(componentNodes);

                if (NullCheckUtil.isNotEmpty(applications)) {
                    throw new GenericBusinessException(GenericMessage.COMPONENT_HAS_APPLICATIONS.getCode(), GenericMessage.COMPONENT_HAS_APPLICATIONS);
                }

            }

            // Check if exposedInterfacesUsed is using
            Long exposedInterfacesUsed = existingComponent.getExposedInterfaces().stream()
                    .filter(interfaceObj -> graphLinkDAO.countAllByInterfaceObj(interfaceObj) > 0).count();

            if (exposedInterfacesUsed > 0) {
                throw new GenericBusinessException(GenericMessage.COMPONENT_EXPOSED_INTERFACE_IS_USING.getCode(),
                        GenericMessage.COMPONENT_EXPOSED_INTERFACE_IS_USING);
            }

            // Check minimum execution requirements
            Optional<Requirement> exRequirement = requirementDAO.findByComponent(existingComponent);
            if (exRequirement.isPresent()) {
                requirementDAO.delete(exRequirement.get());
            }

            // Check Serverless Properties
            Optional<ServerlessProperties> exServerlessProperties = serverlessPropertiesDAO.findByComponent(existingComponent);
            if (exServerlessProperties.isPresent()) {
                serverlessPropertiesDAO.delete(exServerlessProperties.get());
            }

            // Check health checks
            Optional<HealthCheck> exHealthCheck = healthCheckDAO.findByComponent(existingComponent);
            if (exHealthCheck.isPresent()) {
                healthCheckDAO.delete(exHealthCheck.get());
            }

            // Check environmental variables
            List<EnvironmentalVariable> environmentalVariables = environmentalVariableDAO
                    .findAllByComponentOrderByDateCreatedAsc(existingComponent);

            if (null != environmentalVariables && !environmentalVariables.isEmpty()) {
                environmentalVariableDAO.deleteAll(environmentalVariables);
            }

            // Check devices
            List<Device> devices = deviceDAO
                    .findAllByComponentOrderByDateCreatedAsc(existingComponent);
            if (null != devices && !devices.isEmpty()) {
                deviceDAO.deleteAll(devices);
            }

            // Check graph links
            List<GraphLink> graphLinks = graphLinkDAO.findAllByComponentOrderByDateCreatedAsc(existingComponent);

            if (null != graphLinks && !graphLinks.isEmpty()) {
                graphLinks.forEach(graphLink -> {
                    graphLink.setInterfaceObj(null);
                    graphLinkDAO.save(graphLink);
                });
                graphLinkDAO.deleteAll(graphLinks);
            }

            // Check volumes
            List<Volume> volumes = volumeDAO
                    .findAllByComponentOrderByDateCreatedAsc(existingComponent);

            if (null != volumes && !volumes.isEmpty()) {
                volumeDAO.deleteAll(volumes);
            }

            // Check labels
            if (null != existingComponent.getLabels() && !existingComponent.getLabels().isEmpty()) {
                existingComponent.setLabels(null);
            }

            // Check Plugins
            if (null != existingComponent.getPlugins() && !existingComponent.getPlugins().isEmpty()) {
                existingComponent.setPlugins(null);
            }

            // Check exposed interfaces
            List<Interface> exposedInterfaces = interfaceDAO.findByComponent(existingComponent);
            if (null != exposedInterfaces && !exposedInterfaces.isEmpty()) {
                interfaceDAO.deleteAll(exposedInterfaces);
            }

            // Check Vulnerabilities
            List<Vulnerability> vulnerabilities = vulnerabilityDAO.findByComponent(existingComponent);
            if (null != vulnerabilities && !vulnerabilities.isEmpty()) {
                vulnerabilityDAO.deleteAll(vulnerabilities);
            }

            componentDAO.delete(existingComponent);

            try {
                DashboardTO dashboardTO = new DashboardTO();
                dashboardTO.setOverview(true);
                String notificationAsString = null;
                notificationAsString = objectMapper.writeValueAsString(dashboardTO);
                wsTemplate.convertAndSend(ConstantsUtil.DASHBOARD_TOPIC, notificationAsString);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

        } else {
            throw new NotAuthorizedException(GenericMessage.NOT_AUTHORIZED.getCode(), GenericMessage.NOT_AUTHORIZED);
        }

    }

    public Long count(User authenticatedUser) {
        return componentDAO.calculateComponentsAndPublicComponent(authenticatedUser.getOrganization().getId(), true);
    }

    /*
     * privates
     * */

    private BooleanExpression fetchPredicates(String filters, Component fComponent, User authenticatedUser) {
        BooleanExpression predicate = component.eq(component);

        boolean checkRequestParam = null != filters && !filters.isEmpty();
        boolean proceedWithRequestBody = true;

        if (checkRequestParam) {
            try {
                Component filterComponent = new Gson().fromJson(new String(Base64.decodeBase64(filters)), Component.class);
                if (null != filterComponent) {
                    proceedWithRequestBody = false;
                    if (null != filterComponent.getName() && !filterComponent.getName().isEmpty()) {
                        predicate = predicate.and(component.name.containsIgnoreCase(filterComponent.getName()));
                    }
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }

        if (proceedWithRequestBody && null != fComponent && null != fComponent.getName() && !fComponent.getName().isEmpty()) {
            predicate = predicate.and(component.name.containsIgnoreCase(fComponent.getName()));
        }

        if (!authenticatedUser.isAdmin()) {
            predicate = predicate.and(component.organization.eq(authenticatedUser.getOrganization())
                    .or(component.publicComponent.eq(true)));
        }

        return predicate;
    }

    public void deleteComponentNode(ComponentNode componentNode) {
        componentNodeDAO.delete(componentNode);
    }

}
