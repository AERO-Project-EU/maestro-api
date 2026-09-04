package eu.orchestrator.backend.service.applicationinstance;

import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.common.exception.NotAuthorizedException;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.service.resourceprovider.ProviderService;
import eu.orchestrator.backend.service.util.CommonService;
import eu.orchestrator.backend.util.ConstantsUtil;
import eu.orchestrator.backend.util.Util;
import eu.orchestrator.repository.dao.*;
import eu.orchestrator.repository.domain.*;
import eu.orchestrator.repository.domain.Component.CapabilityAdd;
import eu.orchestrator.backend.transfer.ApplicationInstanceGraphTO;
import eu.orchestrator.backend.transfer.DashboardTO;
import eu.orchestrator.backend.transfer.ProviderTO;
import eu.orchestrator.backend.transfer.ProviderTypeTO;
import eu.orchestrator.backend.transfer.TOConverter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import jakarta.transaction.Transactional;

import static eu.orchestrator.repository.domain.QApplicationInstance.applicationInstance;

@Service
@Transactional(rollbackOn = Exception.class)
public class ApplicationInstanceService {

    private static final Logger logger = Logger.getLogger(ApplicationInstanceService.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    CommonService commonService;

    @Autowired
    ProviderService providerService;

    @Autowired
    ApplicationInstanceDAO applicationInstanceDAO;

    @Autowired
    ApplicationDAO applicationDAO;

    @Autowired
    ProviderDAO providerDAO;

    @Autowired
    ComponentNodeDAO componentNodeDAO;

    @Autowired
    ComponentNodeInstanceDAO componentNodeInstanceDAO;

    @Autowired
    EnvironmentalVariableInstanceDAO environmentalVariableInstanceDAO;

    @Autowired
    ComponentDAO componentDAO;

    @Autowired
    PluginInstanceDAO pluginInstanceDAO;

    @Autowired
    DeviceInstanceDAO deviceInstanceDAO;

    @Autowired
    RequirementDAO requirementDAO;

    @Autowired
    SSHKeyDAO sshKeyDAO;

    @Autowired
    ConstraintDAO constraintDAO;

    @Autowired
    FlavorInstanceDAO flavorInstanceDAO;

    @Autowired
    HealthCheckDAO healthCheckDAO;

    @Autowired
    ServerlessPropertiesDAO serverlessPropertiesDAO;

    @Autowired
    LocationInstanceDAO locationInstanceDAO;

    @Autowired
    HealthCheckInstanceDAO healthCheckInstanceDAO;

    @Autowired
    VolumeInstanceDAO volumeInstanceDAO;

    @Autowired
    InterfaceInstanceDAO interfaceInstanceDAO;

    @Autowired
    GraphLinkNodeInstanceDAO graphLinkNodeInstanceDAO;

    @Autowired
    SimpMessagingTemplate wsTemplate;
    @Autowired
    private ServerlessPropertiesInstanceDAO serverlessPropertiesInstanceDAO;


    public ApplicationInstance fetchApplicationInstanceById(Long id) {
        Optional<ApplicationInstance> existingApplicationInstanceOP = applicationInstanceDAO.findById(id);
        return existingApplicationInstanceOP.orElse(null);
    }

    public List<ApplicationInstance> fetchAllByOrganization(User authenticatedUser) {
        return applicationInstanceDAO.findAllByOrganization(authenticatedUser.getOrganization());
    }

    public List<ApplicationInstance> fetchAllApplicationInstances() {
        return applicationInstanceDAO.findAll();
    }

    public ApplicationInstance fetchApplicationInstanceByHexId(String applicationInstanceHexID) {
        Optional<ApplicationInstance> existingApplicationInstanceOP = applicationInstanceDAO.findByHexID(applicationInstanceHexID);
        return existingApplicationInstanceOP.orElse(null);
    }

    public ApplicationInstance fetchApplicationInstanceByName(String name) {
        Optional<ApplicationInstance> applicationInstanceOP = applicationInstanceDAO.findByName(name);
        return applicationInstanceOP.orElse(null);
    }

    public ApplicationInstance fetchApplicationInstanceByNameAndOrganization(String name, Organization organization) {
        Optional<ApplicationInstance> applicationInstanceOP = applicationInstanceDAO.findByNameAndOrganization(name, organization);
        return applicationInstanceOP.orElse(null);
    }

    public List<ApplicationInstance> fetchApplicationInstancesByStatus(String status) {
        return applicationInstanceDAO.findAllByStatus(status);
    }

    public Page fetchApplicationInstanceList(Pageable pageable, ApplicationInstance fApplicationInstance, User authenticatedUser) {
        BooleanExpression predicate = applicationInstance.eq(applicationInstance);
        if (null != fApplicationInstance) {
            if (null != fApplicationInstance.getName() && !fApplicationInstance.getName().isEmpty()) {
                predicate = predicate.and(applicationInstance.name.containsIgnoreCase(fApplicationInstance.getName()));
            }
            if (null != fApplicationInstance.getStatus() && !fApplicationInstance.getStatus().isEmpty()) {
                predicate = predicate.and(applicationInstance.status.containsIgnoreCase(fApplicationInstance.getStatus()));
            }
            if (null != fApplicationInstance.getApplication() && fApplicationInstance.getApplication().getId() > 0) {
                predicate = predicate.and(applicationInstance.application.id.eq(fApplicationInstance.getApplication().getId()));
            }
        }
        if (!authenticatedUser.isAdmin()) {
            predicate = predicate.and(applicationInstance.organization.eq(authenticatedUser.getOrganization()));
        }
        Page<ApplicationInstance> page;
        if (pageable.getPageSize() > 100) {
            page = applicationInstanceDAO.findAll(predicate, PageRequest.of(pageable.getPageNumber(), 100, pageable.getSort()));
        } else {
            page = applicationInstanceDAO.findAll(predicate, pageable);
        }
        return new TOConverter(ApplicationInstance.class.getName(), page, pageable, authenticatedUser).convertToTOWithPermissions();
    }

    public List<ApplicationInstance> fetchAllByUser(User authenticatedUser) {
        return applicationInstanceDAO.findAllByUser(authenticatedUser);
    }

    public ApplicationInstanceGraphTO fetchProviderByApplicationInstanceId(Long applicationInstanceId, User authenticatedUser) {
        ApplicationInstance applicationInstance = fetchApplicationInstanceById(applicationInstanceId);
        if (applicationInstance == null) {
            throw new GenericBusinessException(GenericMessage.APPLICATION_INSTANCE_NOT_EXIST.getCode(), GenericMessage.APPLICATION_INSTANCE_NOT_EXIST);
        }
        if (authenticatedUser.isAdmin() || applicationInstance.getOrganization().getId().equals(authenticatedUser.getOrganization().getId())) {
            // Create ApplicationInstanceTO Object for updating graph
            ApplicationInstanceGraphTO applicationInstanceGraphTO = new ApplicationInstanceGraphTO();
            applicationInstanceGraphTO.setName(applicationInstance.getName());
            applicationInstanceGraphTO.setApplicationInstanceID(applicationInstance.getApplicationInstanceID());
            applicationInstanceGraphTO.setHexID(applicationInstance.getHexID());
            // ProviderTO
            ProviderTO providerTO = new ProviderTO();
            providerTO.setProviderID(applicationInstance.getProvider().getProviderID());
            providerTO.setName(applicationInstance.getProvider().getName());
            providerTO.setDefaultProvider(applicationInstance.getProvider().getDefaultProvider());
            providerTO.setProviderType(new ProviderTypeTO(applicationInstance.getProvider().getProviderType().getId(),
                    applicationInstance.getProvider().getProviderType().getName(), applicationInstance.getProvider().getProviderType().getFriendlyName()));
            applicationInstanceGraphTO.setProvider(providerTO);
            applicationInstanceGraphTO.setStatus(ApplicationInstance.ApplicationInstanceStatus.valueOf(applicationInstance.getStatus()).getFriendlyName());
            return applicationInstanceGraphTO;
        } else {
            throw new NotAuthorizedException(GenericMessage.APPLICATION_INSTANCE_NOT_AUTHORIZED.getCode(), GenericMessage.APPLICATION_INSTANCE_NOT_AUTHORIZED);
        }
    }

    public boolean checkIfApplicationInstanceNameExists(String name, User authenticatedUser) {
        if (authenticatedUser.isAdmin()) {
            ApplicationInstance applicationInstance = fetchApplicationInstanceByName(name);
            if (applicationInstance != null && applicationInstance.getName().equals(name)) {
                return Boolean.TRUE;
            }
        } else {
            ApplicationInstance applicationInstance = fetchApplicationInstanceByNameAndOrganization(name, authenticatedUser.getOrganization());
            if (applicationInstance != null && applicationInstance.getName().equals(name)) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    public ApplicationInstanceGraphTO fetchApplicationInstanceGraphTO(ApplicationInstance applicationInstance, User authenticatedUser) {
        if (authenticatedUser.isAdmin() || applicationInstance.getOrganization().getId().equals(authenticatedUser.getOrganization().getId())) {
            // Create ApplicationInstanceTO Object for showing graph
            return commonService.convertApplicationInstanceToTO(applicationInstance);
        }
        return null;
    }

    public void saveApplicationInstance(ApplicationInstance applicationInstance) {
        applicationInstanceDAO.save(applicationInstance);
    }

    public ApplicationInstanceGraphTO create(ApplicationInstance applicationInstance, User authenticatedUser) {
        // Check if Application Instance name already exists
        if (checkIfApplicationInstanceNameExists(applicationInstance.getName(), authenticatedUser)) {
            throw new GenericBusinessException(GenericMessage.APPLICATION_INSTANCE_ALREADY_EXISTS.getCode(),
                    GenericMessage.APPLICATION_INSTANCE_ALREADY_EXISTS);
        }
        // Check if application exists and belongs to the same organization
        Optional<Application> existingApplicationOP = applicationDAO.findById(applicationInstance.getApplication().getId());

        if (existingApplicationOP.isPresent() && (existingApplicationOP.get().getOrganization().getId().equals(authenticatedUser.getOrganization().getId())
                || authenticatedUser.isAdmin() || existingApplicationOP.get().getPublicApplication())) {
            Application existingApplication = existingApplicationOP.get();
            applicationInstance.setApplication(existingApplication);
            applicationInstance.setHexID(Util.createRandomHEXString());
            applicationInstance.setUser(authenticatedUser);
            applicationInstance.setOrganization(authenticatedUser.getOrganization());
            applicationInstance.setDateCreated(new Date());
            applicationInstance.setLastModified(new Date());
            applicationInstance.setStatus(ApplicationInstance.ApplicationInstanceStatus.PENDING.name());

            // Check provider existence and set default
            if (null != applicationInstance.getProvider()) {
                applicationInstance.setProvider(providerService.findById(applicationInstance.getProvider().getProviderID()));
            } else {
                // Check if this organization has default provider
                if (providerDAO.findByOrganizationAndDefaultProvider(authenticatedUser.getOrganization(), true).isPresent()) {
                    applicationInstance
                            .setProvider(providerDAO.findByOrganizationAndDefaultProvider(authenticatedUser.getOrganization(), true).get());
                } else {
                    throw new GenericBusinessException(GenericMessage.DEFAULT_PROVIDER_MISSING.getCode(), GenericMessage.DEFAULT_PROVIDER_MISSING);
                }
            }
            applicationInstanceDAO.save(applicationInstance);
            // Create CNI
            List<ComponentNode> componentNodes = componentNodeDAO.findAllByApplication(existingApplication);
            if (null != componentNodes && !componentNodes.isEmpty()) {
                createComponentNodeInstance(componentNodes, applicationInstance, existingApplication);
            } else {
                // Application without component nodes
                throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
            }
        }
        // Create ApplicationInstanceTO Object for showing graph
        return commonService.convertApplicationInstanceToTO(applicationInstance);
    }

    public ApplicationInstanceGraphTO update(ApplicationInstance applicationInstance, User authenticatedUser) {
        // Check if application instance already exists or not
        Optional<ApplicationInstance> existingApplicationInstanceOP = applicationInstanceDAO.findById(applicationInstance.getApplicationInstanceID());
        if (!existingApplicationInstanceOP.isPresent()) {
            throw new GenericBusinessException(GenericMessage.APPLICATION_INSTANCE_NOT_EXIST.getCode(), GenericMessage.APPLICATION_INSTANCE_NOT_EXIST);
        }

        if (Boolean.TRUE.equals(existingApplicationInstanceOP.get().hasEditAllowance(authenticatedUser))) {
            ApplicationInstance existingApplicationInstance = existingApplicationInstanceOP.get();
            if (null != applicationInstance.getName() && !applicationInstance.getName().isEmpty()) {
                ApplicationInstance applicationInstanceByNameAndOrganization = fetchApplicationInstanceByNameAndOrganization(applicationInstance.getName(),
                        authenticatedUser.getOrganization());
                if (existingApplicationInstance.getName().equals(applicationInstance.getName())
                        && applicationInstanceByNameAndOrganization != null
                        && applicationInstanceByNameAndOrganization.getName().equals(applicationInstance.getName())) {
                    throw new GenericBusinessException(GenericMessage.APPLICATION_INSTANCE_ALREADY_EXISTS.getCode(),
                            GenericMessage.APPLICATION_INSTANCE_ALREADY_EXISTS);
                }
                existingApplicationInstance.setName(applicationInstance.getName());
            }

            if (null != applicationInstance.getProvider() && null != applicationInstance.getProvider().getProviderID()
                    && applicationInstance.getProvider().getProviderID() != 0
                    && null != providerService.findById(applicationInstance.getProvider().getProviderID())) {

                Provider newProv = providerService.findById(applicationInstance.getProvider().getProviderID());
                // Update provider to componentNodeInstances
                List<ComponentNodeInstance> componentNodeInstances = new ArrayList<>(existingApplicationInstance.getComponentNodeInstances());
                if (null != componentNodeInstances && !componentNodeInstances.isEmpty()) {
                    for (ComponentNodeInstance componentNodeInstance : componentNodeInstances) {
                        if (newProv.getProviderType().getName().equals(ProviderType.ProviderName.POLICY_DEFINED.name())) {
                            componentNodeInstance.setProvider(newProv);
                            componentNodeInstance.setLastModified(new Date());
                            componentNodeInstanceDAO.save(componentNodeInstance);
                        } else if (newProv.getProviderType().getName().equals(ProviderType.ProviderName.USER_DEFINED.name())) {
                            // Do nothing
                        } else {
                            if (!componentNodeInstance.getProvider().getProviderID().equals(newProv.getProviderID())) {
                                componentNodeInstance.setProvider(newProv);
                                componentNodeInstance.setLastModified(new Date());
                                componentNodeInstanceDAO.save(componentNodeInstance);
                            }
                        }
                    }
                }
                existingApplicationInstance.setProvider(newProv);
            }
            if (null != applicationInstance.getOverlay()) {
                existingApplicationInstance.setOverlay(applicationInstance.getOverlay());
            }
            existingApplicationInstance.setLastModified(new Date());
            applicationInstanceDAO.save(existingApplicationInstance);
            return commonService.convertApplicationInstanceToTO(existingApplicationInstance);
        } else {
            throw new NotAuthorizedException(GenericMessage.APPLICATION_INSTANCE_NOT_AUTHORIZED.getCode(),
                    GenericMessage.APPLICATION_INSTANCE_NOT_AUTHORIZED);
        }

    }

    public void delete(Long id, User authenticatedUser) {
        // Check if application instance already exists or not
        Optional<ApplicationInstance> existingApplicationInstanceOP = applicationInstanceDAO.findById(id);
        if (!existingApplicationInstanceOP.isPresent()) {
            throw new GenericBusinessException(GenericMessage.APPLICATION_INSTANCE_NOT_EXIST.getCode(), GenericMessage.APPLICATION_INSTANCE_NOT_EXIST);
        }
        if (Boolean.TRUE.equals(existingApplicationInstanceOP.get().hasDeleteAllowance(authenticatedUser))) {
            ApplicationInstance existingApplicationInstance = existingApplicationInstanceOP.get();
            // Check if application instance exists or not
            if (existingApplicationInstance.getStatus().equals(ApplicationInstance.ApplicationInstanceStatus.PENDING.name())
                    || existingApplicationInstance.getStatus().equals(ApplicationInstance.ApplicationInstanceStatus.ERROR_OCCURRED.name())
                    || existingApplicationInstance.getStatus().equals(ApplicationInstance.ApplicationInstanceStatus.UNDEPLOYED.name())) {
                applicationInstanceDAO.delete(existingApplicationInstance);
                applicationInstanceDAO.flush();
                try {
                    DashboardTO dashboardTO = new DashboardTO();
                    dashboardTO.setOverview(true);
                    String notificationAsString;
                    notificationAsString = objectMapper.writeValueAsString(dashboardTO);
                    wsTemplate.convertAndSend(ConstantsUtil.DASHBOARD_TOPIC, notificationAsString);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            } else {
                throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
            }
        } else {
            throw new NotAuthorizedException(GenericMessage.APPLICATION_INSTANCE_NOT_AUTHORIZED.getCode(), GenericMessage.APPLICATION_INSTANCE_NOT_AUTHORIZED);
        }
    }

    public Long count(User authenticatedUser) {
        return applicationInstanceDAO.calculateApplicationInstances(authenticatedUser.getOrganization().getId());
    }

    public void deleteApplicationInstance(ApplicationInstance applicationInstance) {
        applicationInstanceDAO.delete(applicationInstance);
    }

    /*
     * privates
     */

    private void createComponentNodeInstance(List<ComponentNode> componentNodes, ApplicationInstance applicationInstance, Application existingApplication) {

        SSHKey defaultSSHKey =
                sshKeyDAO.findByUserAndDefaultSSH(applicationInstance.getUser(), true).isPresent()
                        ? sshKeyDAO.findByUserAndDefaultSSH(applicationInstance.getUser(), true).get()
                        : null;

        List<ComponentNodeInstance> componentNodeInstances = new ArrayList<>();

        for (ComponentNode componentNode : componentNodes) {

            ComponentNodeInstance componentNodeInstance = new ComponentNodeInstance();
            componentNodeInstance.setName(componentNode.getName());
            componentNodeInstance.setHexID(Util.createRandomHEXString());
            componentNodeInstance.setApplicationInstance(applicationInstance);
            componentNodeInstance.setProvider(applicationInstance.getProvider());
            componentNodeInstance.setComponentNode(componentNode);
            componentNodeInstance.setDateCreated(new Date());
            componentNodeInstance.setLastModified(new Date());
            componentNodeInstance.setMinimumWorkers(1);
            componentNodeInstance.setMaximumWorkers(1);
            componentNodeInstance.setStatusIDS(false);
            componentNodeInstance.setStatusIPS(false);
            componentNodeInstance.setLoadBalancer(false);
            componentNodeInstance.setLoadBalancedBy(null);

            componentNodeInstance.setCommand(componentNode.getComponent().getCommand());
            componentNodeInstance.setNetworkModeHost(componentNode.getComponent().getNetworkModeHost());
            componentNodeInstance.setPrivilege(componentNode.getComponent().getPrivilege());
            componentNodeInstance.setHostname(componentNode.getComponent().getHostname());
            componentNodeInstance.setSharedMemorySize(componentNode.getComponent().getSharedMemorySize());
            componentNodeInstance.setDnsEntry(null);

            componentNodeInstance.setSshKey(defaultSSHKey);

            // Capability Add
            if (componentNode.getComponent().getCapabilityAdds() != null) {
                Collection<CapabilityAdd> capabilityAdds = new ArrayList<>();
                componentNode.getComponent().getCapabilityAdds().forEach(
                        capabilityAdd -> capabilityAdds.add(capabilityAdd));
                componentNodeInstance.setCapabilityAdds(capabilityAdds);
            } else {
                componentNodeInstance.setCapabilityAdds(null);
            }

            // Capability Drop
            if (componentNode.getComponent().getCapabilityDrops() != null) {
                Collection<Component.CapabilityDrop> capabilityDrops = new ArrayList<>();
                componentNode.getComponent().getCapabilityDrops().forEach(
                        capabilityDrop -> capabilityDrops.add(capabilityDrop));
                componentNodeInstance.setCapabilityDrops(capabilityDrops);
            } else {
                componentNodeInstance.setCapabilityDrops(null);
            }

            // Kubernetes Runtime Class Name
            if (componentNode.getComponent().getKubernetesRuntimeClassName() != null) {
                componentNodeInstance.setKubernetesRuntimeClassName(componentNode.getComponent().getKubernetesRuntimeClassName());
            } else {
                componentNodeInstance.setKubernetesRuntimeClassName(null);
            }

            componentNodeInstanceDAO.save(componentNodeInstance);

            // Environmental variables
            if (null != componentNode.getComponent().getEnvironmentalVariables()
                    && !componentNode.getComponent().getEnvironmentalVariables().isEmpty()) {
                componentNodeInstance.setEnvironmentalVariableInstances(
                        createEnvironmentalVariables(componentNode, componentNodeInstance, existingApplication));
            } else {
                componentNodeInstance.setEnvironmentalVariableInstances(null);
            }

            // Plugins
            if (null != componentNode.getComponent().getPlugins() && !componentNode
                    .getComponent().getPlugins().isEmpty()) {
                componentNodeInstance.setPluginInstances(createPluginInstances(componentNode, componentNodeInstance));
            } else {
                componentNodeInstance.setPluginInstances(null);
            }

            // Devices
            if (null != componentNode.getComponent().getDevices() && !componentNode
                    .getComponent().getDevices().isEmpty()) {
                componentNodeInstance.setDeviceInstances(createDeviceInstances(componentNode, componentNodeInstance));
            } else {
                componentNodeInstance.setDeviceInstances(null);
            }

            // Minimum Execution Requirements
            if (requirementDAO.findByComponent(componentNode.getComponent()).isPresent()) {
                Requirement requirement = requirementDAO.findByComponent(componentNode.getComponent()).get();
                createMinimumExecutionRequirements(componentNode, componentNodeInstance, applicationInstance, requirement);
            }

            // Serverless Properties
            if (serverlessPropertiesDAO.findByComponent(componentNode.getComponent()).isPresent()) {
                ServerlessProperties serverlessProperties = serverlessPropertiesDAO.findByComponent(componentNode.getComponent()).get();
                createServerlessPropertiesInstance(serverlessProperties, componentNodeInstance);
            }

            // Health Checks
            if (healthCheckDAO.findByComponent(componentNode.getComponent()).isPresent()) {
                HealthCheck healthCheck = healthCheckDAO.findByComponent(componentNode.getComponent()).get();
                createHealthCheckInstance(healthCheck, componentNodeInstance);
            }

            Provider provider = providerDAO
                    .findById(applicationInstance.getProvider().getProviderID()).get();

            // Regions TODO
            if (null != provider.getRegions() && !provider.getRegions().isEmpty()) {
                componentNodeInstance.setLocationInstances(createRegions(componentNodeInstance, applicationInstance, provider));
            } else {
                componentNodeInstance.setLocationInstances(null);
            }

            // Volumes
            if (null != componentNode.getComponent().getVolumes() && !componentNode
                    .getComponent().getVolumes().isEmpty()) {
                componentNodeInstance.setVolumeInstances(createVolumeInstances(componentNode, componentNodeInstance));
            } else {
                componentNodeInstance.setVolumeInstances(null);
            }

            // Exposed Interfaces
            if (null != componentNode.getComponent().getExposedInterfaces() && !componentNode
                    .getComponent().getExposedInterfaces().isEmpty()) {
                componentNodeInstance.setInterfaceInstances(createInterfaceInstances(componentNode, componentNodeInstance));
            } else {
                componentNodeInstance.setInterfaceInstances(null);
            }

            componentNodeInstanceDAO.save(componentNodeInstance);
            componentNodeInstances.add(componentNodeInstance);
        }

        applicationInstance.setComponentNodeInstances(new TreeSet<>(componentNodeInstances));

        // Create Graph Link Node Instances
        if (null != existingApplication.getGraphLinkNodes() && !existingApplication.getGraphLinkNodes().isEmpty()) {
            createGraphLinkNodeInstances(existingApplication, applicationInstance);
        }

        applicationInstanceDAO.save(applicationInstance);
        // TODO Send Push Notification

        try {
            DashboardTO dashboardTO = new DashboardTO();
            dashboardTO.setOverview(true);
            String notificationAsString;
            notificationAsString = objectMapper.writeValueAsString(dashboardTO);
            wsTemplate.convertAndSend(ConstantsUtil.DASHBOARD_TOPIC, notificationAsString);
        } catch (JsonProcessingException e) {
            logger.info(e.getMessage());
            e.printStackTrace();
        }

    }

    private List<EnvironmentalVariableInstance> createEnvironmentalVariables(ComponentNode componentNode, ComponentNodeInstance componentNodeInstance,
            Application existingApplication) {

        List<EnvironmentalVariableInstance> environmentalVariableInstances = new ArrayList<>();

        for (EnvironmentalVariable environmentalVariable : componentNode.getComponent()
                .getEnvironmentalVariables()) {

            EnvironmentalVariableInstance environmentalVariableInstance = new EnvironmentalVariableInstance();
            environmentalVariableInstance
                    .setComponentNodeInstance(componentNodeInstance);
            environmentalVariableInstance.setDateCreated(new Date());
            environmentalVariableInstance.setLastModified(new Date());
            environmentalVariableInstance
                    .setEnvironmentalVariable(environmentalVariable);
            environmentalVariableInstance.setKey(environmentalVariable.getKey());

            if (environmentalVariable.getValue().startsWith("@") || environmentalVariable.getValue().startsWith("#")) {

                String componentName = environmentalVariable.getValue().substring(1);

                if (componentDAO.findByNameAndOrganization(componentName, componentNode.getComponent().getOrganization()).isPresent()) {

                    Component component = componentDAO.findByNameAndOrganization(componentName, componentNode.getComponent().getOrganization()).get();
                    if (!componentNodeDAO.findAllByApplication(existingApplication).stream()
                            .filter(compNode -> compNode.getComponent().getId()
                                    .equals(component.getId())).collect(
                                    Collectors.toList()).isEmpty()) {
                        String prefix = String.valueOf(environmentalVariable.getValue().charAt(0));
                        environmentalVariableInstance.setValue(
                                prefix + componentNodeDAO.findAllByApplication(existingApplication)
                                        .stream().filter(compNode -> compNode.getComponent().getId()
                                                .equals(component.getId())).collect(Collectors.toList())
                                        .get(0).getName());
                    }

                } else {
                    environmentalVariableInstance
                            .setValue(environmentalVariable.getValue());
                }

            } else {
                environmentalVariableInstance.setValue(environmentalVariable.getValue());
            }

            environmentalVariableInstanceDAO.save(environmentalVariableInstance);
            environmentalVariableInstances.add(environmentalVariableInstance);

        }
        return environmentalVariableInstances;
    }

    private List<PluginInstance> createPluginInstances(ComponentNode componentNode, ComponentNodeInstance componentNodeInstance) {

        List<PluginInstance> pluginInstances = new ArrayList<>();

        for (Plugin plugin : componentNode.getComponent().getPlugins()) {

            PluginInstance pluginInstance = new PluginInstance();
            pluginInstance.setComponentNodeInstance(componentNodeInstance);
            pluginInstance.setDateCreated(new Date());
            pluginInstance.setLastModified(new Date());
            pluginInstance.setPlugin(plugin);
            pluginInstance.setName(plugin.getName());
            pluginInstance.setModuleName(
                    null != plugin.getModuleName() && !plugin.getModuleName().isEmpty()
                            ? plugin.getModuleName() : null);
            pluginInstance.setImmutablePlugin(plugin.getImmutablePlugin());
            pluginInstance.setDeletedPlugin(false);

            pluginInstanceDAO.save(pluginInstance);
            pluginInstances.add(pluginInstance);

        }

        return pluginInstances;
    }

    private List<DeviceInstance> createDeviceInstances(ComponentNode componentNode, ComponentNodeInstance componentNodeInstance) {
        List<DeviceInstance> deviceInstances = new ArrayList<>();

        for (Device device : componentNode.getComponent().getDevices()) {
            DeviceInstance deviceInstance = new DeviceInstance();
            deviceInstance.setComponentNodeInstance(componentNodeInstance);
            deviceInstance.setDateCreated(new Date());
            deviceInstance.setLastModified(new Date());
            deviceInstance.setDevice(device);
            deviceInstance.setKey(device.getKey());
            deviceInstance.setValue(device.getValue());

            deviceInstanceDAO.save(deviceInstance);
            deviceInstances.add(deviceInstance);
        }

        return deviceInstances;
    }

    private void createMinimumExecutionRequirements(ComponentNode componentNode, ComponentNodeInstance componentNodeInstance,
            ApplicationInstance applicationInstance, Requirement requirement) {

        FlavorInstance flavorInstance = new FlavorInstance();
        flavorInstance.setComponentNodeInstance(componentNodeInstance);
        flavorInstance.setDateCreated(new Date());
        flavorInstance.setLastModified(new Date());
        flavorInstance.setRam(requirement.getRam());
        flavorInstance.setStorage(requirement.getStorage());
        flavorInstance.setvCPUs(requirement.getvCPUs());
        flavorInstance.setServerlessEnabled(requirement.getServerlessEnabled());
        flavorInstanceDAO.save(flavorInstance);

        if (null != requirement.getvCPUs() && requirement.getvCPUs() != 0) {

            Constraint cpuConstraint = new Constraint();
            cpuConstraint.setComponentNodeInstance(componentNodeInstance);
            cpuConstraint.setConstraintCategory(
                    Constraint.ConstraintCategory.COMPONENT_HOSTING.name());
            cpuConstraint.setConstraintType(Constraint.ConstraintType.HARD.name());
            cpuConstraint
                    .setConstraintMetric(Constraint.ConstraintMetric.MIN_V_CPU.name());
            cpuConstraint.setDeletableConstraint(false);
            cpuConstraint.setConstraintUnit(ConstantsUtil.AMOUNT);
            cpuConstraint.setConstraintValue(requirement.getvCPUs() + "");
            cpuConstraint.setCountry(null);
            cpuConstraint.setGraphLinkNodeInstance(null);
            cpuConstraint.setInterfaceInstance(null);
            cpuConstraint.setQi(null);
            cpuConstraint.setDateCreated(new Date());
            cpuConstraint.setLastModified(new Date());
            cpuConstraint.setApplicationInstance(applicationInstance);
            constraintDAO.save(cpuConstraint);

        }

        if (null != requirement.getRam() && requirement.getRam() != 0) {

            Constraint ramConstraint = new Constraint();
            ramConstraint.setComponentNodeInstance(componentNodeInstance);
            ramConstraint.setConstraintCategory(
                    Constraint.ConstraintCategory.COMPONENT_HOSTING.name());
            ramConstraint.setConstraintType(Constraint.ConstraintType.HARD.name());
            ramConstraint.setConstraintMetric(Constraint.ConstraintMetric.MIN_RAM.name());
            ramConstraint.setDeletableConstraint(false);
            ramConstraint.setConstraintUnit("mb");
            ramConstraint.setConstraintValue(requirement.getRam() + "");
            ramConstraint.setCountry(null);
            ramConstraint.setGraphLinkNodeInstance(null);
            ramConstraint.setInterfaceInstance(null);
            ramConstraint.setQi(null);
            ramConstraint.setDateCreated(new Date());
            ramConstraint.setLastModified(new Date());
            ramConstraint.setApplicationInstance(applicationInstance);
            constraintDAO.save(ramConstraint);

        }

        if (null != requirement.getStorage() && requirement.getStorage() != 0) {

            Constraint storageConstraint = new Constraint();
            storageConstraint.setComponentNodeInstance(componentNodeInstance);
            storageConstraint.setConstraintCategory(
                    Constraint.ConstraintCategory.COMPONENT_HOSTING.name());
            storageConstraint.setConstraintType(Constraint.ConstraintType.HARD.name());
            storageConstraint
                    .setConstraintMetric(Constraint.ConstraintMetric.MIN_STORAGE.name());
            storageConstraint.setDeletableConstraint(false);
            storageConstraint.setConstraintValue(requirement.getStorage() + "");
            storageConstraint.setConstraintUnit("gb");
            storageConstraint.setCountry(null);
            storageConstraint.setGraphLinkNodeInstance(null);
            storageConstraint.setInterfaceInstance(null);
            storageConstraint.setQi(null);
            storageConstraint.setDateCreated(new Date());
            storageConstraint.setLastModified(new Date());
            storageConstraint.setApplicationInstance(applicationInstance);
            constraintDAO.save(storageConstraint);

        }

        // Elasticity Profile Default Constraints
        if (null != componentNode.getComponent().getElasticityController() &&
                (componentNode.getComponent().getElasticityController().equals(
                        "HORIZONTAL") ||
                        componentNode.getComponent().getElasticityController().equals(
                                "LAMBDA_FUNCTION"))) {

            Constraint minWorkersConstraint = new Constraint();
            minWorkersConstraint.setComponentNodeInstance(componentNodeInstance);
            minWorkersConstraint.setConstraintCategory(
                    Constraint.ConstraintCategory.COMPONENT_HOSTING.name());
            minWorkersConstraint.setConstraintType(Constraint.ConstraintType.HARD.name());
            minWorkersConstraint
                    .setConstraintMetric(Constraint.ConstraintMetric.MIN_WORKERS.name());
            minWorkersConstraint.setDeletableConstraint(false);
            minWorkersConstraint
                    .setConstraintValue(componentNodeInstance.getMinimumWorkers() + "");
            minWorkersConstraint.setConstraintUnit(ConstantsUtil.AMOUNT);
            minWorkersConstraint.setCountry(null);
            minWorkersConstraint.setGraphLinkNodeInstance(null);
            minWorkersConstraint.setInterfaceInstance(null);
            minWorkersConstraint.setQi(null);
            minWorkersConstraint.setDateCreated(new Date());
            minWorkersConstraint.setLastModified(new Date());
            minWorkersConstraint.setApplicationInstance(applicationInstance);
            constraintDAO.save(minWorkersConstraint);

            Constraint maxWorkersConstraint = new Constraint();
            maxWorkersConstraint.setComponentNodeInstance(componentNodeInstance);
            maxWorkersConstraint.setConstraintCategory(
                    Constraint.ConstraintCategory.COMPONENT_HOSTING.name());
            maxWorkersConstraint.setConstraintType(Constraint.ConstraintType.HARD.name());
            maxWorkersConstraint
                    .setConstraintMetric(Constraint.ConstraintMetric.MAX_WORKERS.name());
            maxWorkersConstraint.setDeletableConstraint(false);
            maxWorkersConstraint
                    .setConstraintValue(componentNodeInstance.getMaximumWorkers() + "");
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

    private void createServerlessPropertiesInstance(ServerlessProperties serverlessProperties, ComponentNodeInstance componentNodeInstance) {

        ServerlessPropertiesInstance serverlessPropertiesInstance = new ServerlessPropertiesInstance();
        serverlessPropertiesInstance.setServerlessProperties(serverlessProperties);
        serverlessPropertiesInstance.setComponentNodeInstance(componentNodeInstance);
        serverlessPropertiesInstance.setDateCreated(new Date());
        serverlessPropertiesInstance.setLastModified(new Date());

        serverlessPropertiesInstance.setAutoscaler(serverlessProperties.getAutoscaler());
        serverlessPropertiesInstance.setMetric(serverlessProperties.getMetric());
        serverlessPropertiesInstance.setTargetValue(serverlessProperties.getTargetValue());
        serverlessPropertiesInstance.setWindowSize(serverlessProperties.getWindowSize());
        serverlessPropertiesInstance.setMinScale(serverlessProperties.getMinScale());
        serverlessPropertiesInstance.setMaxScale(serverlessProperties.getMaxScale());

        serverlessPropertiesInstanceDAO.save(serverlessPropertiesInstance);

    }

    private void createHealthCheckInstance(HealthCheck healthCheck, ComponentNodeInstance componentNodeInstance) {

        HealthCheckInstance healthCheckInstance = new HealthCheckInstance();
        healthCheckInstance.setHealthCheck(healthCheck);
        healthCheckInstance.setComponentNodeInstance(componentNodeInstance);
        healthCheckInstance.setDateCreated(new Date());
        healthCheckInstance.setLastModified(new Date());
        healthCheckInstance.setName(healthCheck.getName());
        healthCheckInstance.setInterval(healthCheck.getInterval());

        healthCheckInstance.setArgs(
                null != healthCheck.getArgs() && !healthCheck.getArgs().isEmpty()
                        ? healthCheck.getArgs() : null);
        healthCheckInstance.setHttpURL(
                null != healthCheck.getHttpURL() && !healthCheck.getHttpURL().isEmpty()
                        ? healthCheck.getHttpURL() : null);

        healthCheckInstanceDAO.save(healthCheckInstance);

    }

    private List<LocationInstance> createRegions(ComponentNodeInstance componentNodeInstance, ApplicationInstance applicationInstance, Provider provider) {

        List<LocationInstance> locationInstances = new ArrayList<>();

        LocationInstance locationInstance = new LocationInstance();
        locationInstance
                .setRegion(new ArrayList<>(provider.getRegions()).get(0).getName());
        locationInstance.setCountry(null);
        locationInstance.setComponentNodeInstance(componentNodeInstance);
        locationInstance.setDateCreated(new Date());
        locationInstance.setLastModified(new Date());
        locationInstanceDAO.save(locationInstance);
        locationInstances.add(locationInstance);

        Constraint regionConstraint = new Constraint();
        regionConstraint.setComponentNodeInstance(componentNodeInstance);
        regionConstraint.setConstraintCategory(
                Constraint.ConstraintCategory.COMPONENT_HOSTING.name());
        regionConstraint.setConstraintType(Constraint.ConstraintType.HARD.name());
        regionConstraint.setConstraintMetric(Constraint.ConstraintMetric.REGION.name());
        regionConstraint.setDeletableConstraint(false);
        regionConstraint.setConstraintValue(locationInstance.getRegion());
        regionConstraint.setConstraintUnit("region");
        regionConstraint.setCountry(null);
        regionConstraint.setGraphLinkNodeInstance(null);
        regionConstraint.setInterfaceInstance(null);
        regionConstraint.setQi(null);
        regionConstraint.setDateCreated(new Date());
        regionConstraint.setLastModified(new Date());
        regionConstraint.setApplicationInstance(applicationInstance);
        constraintDAO.save(regionConstraint);

        return locationInstances;

    }

    private List<VolumeInstance> createVolumeInstances(ComponentNode componentNode, ComponentNodeInstance componentNodeInstance) {

        List<VolumeInstance> volumeInstances = new ArrayList<>();

        for (Volume volume : componentNode.getComponent().getVolumes()) {
            VolumeInstance volumeInstance = new VolumeInstance();
            volumeInstance.setComponentNodeInstance(componentNodeInstance);
            volumeInstance.setDateCreated(new Date());
            volumeInstance.setLastModified(new Date());
            volumeInstance.setVolume(volume);
            volumeInstance.setDockerPath(volume.getDockerPath());
            volumeInstance.setFile(volume.getFile());
            //TODO pparthenis
            volumeInstance.setHostPath("");
            volumeInstanceDAO.save(volumeInstance);
            volumeInstances.add(volumeInstance);
        }

        return volumeInstances;
    }

    private List<InterfaceInstance> createInterfaceInstances(ComponentNode componentNode, ComponentNodeInstance componentNodeInstance) {

        List<InterfaceInstance> interfaceInstances = new ArrayList<>();

        for (Interface exposedInterface : componentNode.getComponent().getExposedInterfaces()) {

            InterfaceInstance interfaceInstance = new InterfaceInstance();
            interfaceInstance.setComponentNodeInstance(componentNodeInstance);
            interfaceInstance.setDateCreated(new Date());
            interfaceInstance.setLastModified(new Date());
            interfaceInstance.setInterfaceObj(exposedInterface);
            interfaceInstance.setName(exposedInterface.getName());
            interfaceInstance.setPort(exposedInterface.getPort());
            interfaceInstance.setInterfaceType(exposedInterface.getInterfaceType());
            interfaceInstanceDAO.save(interfaceInstance);
            interfaceInstances.add(interfaceInstance);

        }

        return interfaceInstances;

    }

    private void createGraphLinkNodeInstances(Application existingApplication, ApplicationInstance applicationInstance) {

        for (GraphLinkNode graphLinkNode : existingApplication.getGraphLinkNodes()) {
            GraphLinkNodeInstance graphLinkNodeInstance = new GraphLinkNodeInstance();
            graphLinkNodeInstance.setApplicationInstance(applicationInstance);
            graphLinkNodeInstance.setGraphLinkNode(graphLinkNode);

            graphLinkNodeInstance.setComponentNodeInstanceFrom(componentNodeInstanceDAO
                    .findByComponentNodeAndApplicationInstance(
                            graphLinkNode.getComponentNodeFrom(), applicationInstance).get());
            graphLinkNodeInstance.setComponentNodeInstanceTo(componentNodeInstanceDAO
                    .findByComponentNodeAndApplicationInstance(
                            graphLinkNode.getComponentNodeTo(),
                            applicationInstance).get());

            graphLinkNodeInstance.setDateCreated(new Date());
            graphLinkNodeInstance.setLastModified(new Date());
            graphLinkNodeInstanceDAO.save(graphLinkNodeInstance);


        }

    }

}
