package eu.orchestrator.backend.util;

import eu.orchestrator.repository.dao.ApplicationDAO;
import eu.orchestrator.repository.dao.ApplicationInstanceDAO;
import eu.orchestrator.repository.dao.ComponentDAO;
import eu.orchestrator.repository.dao.ComponentNodeDAO;
import eu.orchestrator.repository.dao.ComponentNodeInstanceDAO;
import eu.orchestrator.repository.dao.ComponentNodeInstanceStatusDAO;
import eu.orchestrator.repository.dao.ConstraintDAO;
import eu.orchestrator.repository.dao.DeviceInstanceDAO;
import eu.orchestrator.repository.dao.EnvironmentalVariableInstanceDAO;
import eu.orchestrator.repository.dao.FlavorInstanceDAO;
import eu.orchestrator.repository.dao.GraphLinkNodeInstanceDAO;
import eu.orchestrator.repository.dao.HealthCheckDAO;
import eu.orchestrator.repository.dao.HealthCheckInstanceDAO;
import eu.orchestrator.repository.dao.IDRuleSetDAO;
import eu.orchestrator.repository.dao.IDRuleSetInstanceDAO;
import eu.orchestrator.repository.dao.InterfaceInstanceDAO;
import eu.orchestrator.repository.dao.LocationInstanceDAO;
import eu.orchestrator.repository.dao.PluginDAO;
import eu.orchestrator.repository.dao.PluginInstanceDAO;
import eu.orchestrator.repository.dao.RequirementDAO;
import eu.orchestrator.repository.dao.RuntimePolicyDAO;
import eu.orchestrator.repository.dao.VolumeInstanceDAO;
import eu.orchestrator.repository.domain.Application;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.Component;
import eu.orchestrator.repository.domain.ComponentNode;
import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.repository.domain.ComponentNodeInstanceStatus;
import eu.orchestrator.repository.domain.Constraint;
import eu.orchestrator.repository.domain.DeviceInstance;
import eu.orchestrator.repository.domain.EnvironmentalVariableInstance;
import eu.orchestrator.repository.domain.FlavorInstance;
import eu.orchestrator.repository.domain.GraphLinkNodeInstance;
import eu.orchestrator.repository.domain.HealthCheck;
import eu.orchestrator.repository.domain.HealthCheckInstance;
import eu.orchestrator.repository.domain.IDRuleSetInstance;
import eu.orchestrator.repository.domain.Interface;
import eu.orchestrator.repository.domain.InterfaceInstance;
import eu.orchestrator.repository.domain.LocationInstance;
import eu.orchestrator.repository.domain.Plugin;
import eu.orchestrator.repository.domain.PluginInstance;
import eu.orchestrator.repository.domain.Requirement;
import eu.orchestrator.repository.domain.VolumeInstance;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorChangedStatusNotification;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorComponentNodeInstance;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorDependency;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorElasticity;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorFlavor;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorHealthCheck;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorLocation;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorPlugin;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorPort;
import eu.orchestrator.transfer.entities.oss.ComponentPlacement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import jakarta.persistence.EntityManager;

public class BalancerUtil {

    private static final Logger logger = Logger.getLogger(BalancerUtil.class.getName());

    public static String addLoadBalancerCNIAndMinimumWorkers(
            Map<Long, ComponentNodeInstance> mapOfNeededChanges,
            Map<String, OrchestratorComponentNodeInstance> services,
            ComponentPlacement componentPlacement, Application application,
            ApplicationInstance applicationInstance, ComponentNodeInstance componentNodeInstance,
            String vimID, ApplicationDAO applicationDAO, ApplicationInstanceDAO applicationInstanceDAO,
            ComponentDAO componentDAO, ComponentNodeDAO componentNodeDAO,
            ComponentNodeInstanceDAO componentNodeInstanceDAO,
            ComponentNodeInstanceStatusDAO componentNodeInstanceStatusDAO,
            InterfaceInstanceDAO interfaceInstanceDAO,
            EnvironmentalVariableInstanceDAO environmentalVariableInstanceDAO,
            HealthCheckDAO healthCheckDAO, HealthCheckInstanceDAO healthCheckInstanceDAO,
            DeviceInstanceDAO deviceInstanceDAO, FlavorInstanceDAO flavorInstanceDAO,
            LocationInstanceDAO locationInstanceDAO, VolumeInstanceDAO volumeInstanceDAO,
            GraphLinkNodeInstanceDAO graphLinkNodeInstanceDAO, RequirementDAO requirementDAO,
            ConstraintDAO constraintDAO, RuntimePolicyDAO runtimePolicyDAO,
            PluginInstanceDAO pluginInstanceDAO, PluginDAO pluginDAO,
            IDRuleSetInstanceDAO idRuleSetInstanceDAO, IDRuleSetDAO idRuleSetDAO,
            EntityManager entityManager, String consulURL, String consulURLIPv6) {

        logger.info("Adding load balancer for CNI: " + componentNodeInstance.getName());

        // STEP0: Initialize workers map
        Map<Long, ComponentNodeInstance> workers = new HashMap<>();
        workers.put(componentNodeInstance.getComponentNodeInstanceID(), componentNodeInstance);

        // STEP1: Create component node instance for Load Balancer
        Component traefikComponent = componentDAO.findByName("Traefik").get();
        ComponentNode traefikComponentNode = new ComponentNode();
        traefikComponentNode.setComponent(traefikComponent);
        traefikComponentNode.setHexID(Util.createRandomHEXString());
        traefikComponentNode.setDateCreated(new Date());
        traefikComponentNode.setLastModified(new Date());
        traefikComponentNode.setName(componentNodeInstance.getName() + "LB");
        componentNodeDAO.save(traefikComponentNode);

        ComponentNodeInstance traefikComponentNodeInstance = new ComponentNodeInstance();
        traefikComponentNodeInstance.setSshKey(componentNodeInstance.getSshKey());
        traefikComponentNodeInstance.setHexID(Util.createRandomHEXString());
        traefikComponentNodeInstance.setLoadBalancer(true);
        traefikComponentNodeInstance.setLoadBalancedBy(null);
        traefikComponentNodeInstance.setVolumeInstances(null);
        traefikComponentNodeInstance.setLocationInstances(null);
        //TODO rethink the placement of the lb
        traefikComponentNodeInstance.setProvider(componentNodeInstance.getProvider());
        traefikComponentNodeInstance.setEnvironmentalVariableInstances(null);
        traefikComponentNodeInstance.setDeviceInstances(null);
        //TODO check what the balanced component node instace has
        traefikComponentNodeInstance.setStatusIPS(false);
        traefikComponentNodeInstance.setStatusIDS(false);
        traefikComponentNodeInstance.setNetworkModeHost(false);
        traefikComponentNodeInstance.setPrivilege(false);
        traefikComponentNodeInstance.setHostname(null);
        traefikComponentNodeInstance.setDnsEntry(null);
        traefikComponentNodeInstance.setSharedMemorySize(null);
        traefikComponentNodeInstance.setCapabilityAdds(null);
        traefikComponentNodeInstance.setCapabilityDrops(null);

        traefikComponentNodeInstance.setMinimumWorkers(1);
        traefikComponentNodeInstance.setMaximumWorkers(1);
        traefikComponentNodeInstance.setLastModified(new Date());
        traefikComponentNodeInstance.setDateCreated(new Date());
        traefikComponentNodeInstance.setComponentNode(traefikComponentNode);
        traefikComponentNodeInstance.setName(traefikComponentNode.getName());
        traefikComponentNodeInstance.setApplicationInstance(applicationInstance);

        String command;
        if (applicationInstance.getOverlay()) {

            command = "-c,--api,--consul.prefix=" + application.getHexID() + "/" + applicationInstance
                    .getHexID() + "/" + traefikComponentNode.getHexID()
                    + "/lbConfiguration,--consul.endpoint=" + consulURLIPv6;
        } else {
            command = "-c,--api,--consul.prefix=" + application.getHexID() + "/" + applicationInstance
                    .getHexID() + "/" + traefikComponentNode.getHexID()
                    + "/lbConfiguration,--consul.endpoint=" + consulURL;
        }

        traefikComponentNodeInstance.setCommand(command);

        componentNodeInstanceDAO.save(traefikComponentNodeInstance);

        SortedSet<ComponentNodeInstance> currentComponentsNodeInstances = applicationInstance.getComponentNodeInstances();
        currentComponentsNodeInstances.add(traefikComponentNodeInstance);
        applicationInstance.setComponentNodeInstances(currentComponentsNodeInstances);

        ComponentNodeInstanceStatus componentNodeInstanceStatus = new ComponentNodeInstanceStatus();
        componentNodeInstanceStatus.setReportedChange(
                OrchestratorChangedStatusNotification.ChangeType.GraphStatusChange.name());
        componentNodeInstanceStatus.setLastModified(new Date());
        componentNodeInstanceStatus.setDateCreated(new Date());
        componentNodeInstanceStatus.setComponentNodeInstance(traefikComponentNodeInstance);
        componentNodeInstanceStatus.setMessage("Component is loading...");
        componentNodeInstanceStatus.setStatus("LOADING");
        componentNodeInstanceStatus.setApplicationInstance(applicationInstance);
        componentNodeInstanceStatusDAO.save(componentNodeInstanceStatus);

        Requirement requirement = requirementDAO.findByComponent(traefikComponent).get();

        FlavorInstance flavorInstance = new FlavorInstance();
        flavorInstance.setComponentNodeInstance(traefikComponentNodeInstance);
        flavorInstance.setvCPUs(requirement.getvCPUs());
        flavorInstance.setStorage(requirement.getStorage());
        flavorInstance.setRam(requirement.getRam());
        flavorInstance.setLastModified(new Date());
        flavorInstance.setDateCreated(new Date());
        flavorInstanceDAO.save(flavorInstance);
        traefikComponentNodeInstance.setFlavorInstance(flavorInstance);

        HealthCheck healthCheck = healthCheckDAO.findByComponent(traefikComponent).get();

        HealthCheckInstance healthCheckInstance = new HealthCheckInstance();
        healthCheckInstance.setHealthCheck(healthCheck);
        healthCheckInstance.setComponentNodeInstance(traefikComponentNodeInstance);
        healthCheckInstance.setName(healthCheck.getName());
        healthCheckInstance.setInterval(healthCheck.getInterval());
        healthCheckInstance.setArgs(
                null != healthCheck.getArgs() && !healthCheck.getArgs().isEmpty() ? healthCheck.getArgs()
                        : null);
        healthCheckInstance.setHttpURL(
                null != healthCheck.getHttpURL() && !healthCheck.getHttpURL().isEmpty() ? healthCheck
                        .getHttpURL() : null);

        healthCheckInstance.setLastModified(new Date());
        healthCheckInstance.setDateCreated(new Date());
        healthCheckInstanceDAO.save(healthCheckInstance);
        traefikComponentNodeInstance.setHealthCheckInstance(healthCheckInstance);

        logger.info("Size of plugins: " + traefikComponent.getPlugins().size());

        List<Plugin> traefikPlugins = new ArrayList<>(traefikComponent.getPlugins());

        logger.info("Size of plugins: " + traefikPlugins.size());

        // Plugins
        if (null != traefikPlugins && !traefikPlugins.isEmpty()) {

            List<PluginInstance> pluginInstances = new ArrayList<>();

            traefikPlugins.stream().forEach(plugin -> {

                try {

                    Plugin lPlugin = pluginDAO.findById(plugin.getPluginID()).get();

                    PluginInstance pluginInstance = new PluginInstance();
                    pluginInstance.setComponentNodeInstance(traefikComponentNodeInstance);
                    pluginInstance.setDateCreated(new Date());
                    pluginInstance.setLastModified(new Date());
                    pluginInstance.setPlugin(lPlugin);
                    pluginInstance.setName(lPlugin.getName());
                    pluginInstance.setModuleName(
                            null != lPlugin.getModuleName() && !lPlugin.getModuleName().isEmpty() ? lPlugin
                                    .getModuleName() : null);
                    pluginInstance.setImmutablePlugin(lPlugin.getImmutablePlugin());
                    pluginInstance.setDeletedPlugin(false);

                    pluginInstanceDAO.save(pluginInstance);
                    pluginInstances.add(pluginInstance);

                } catch (Exception e) {
                    e.printStackTrace();
                    logger.log(Level.SEVERE, e.getMessage(), e);
                }

            });

            traefikComponentNodeInstance.setPluginInstances(pluginInstances);

        } else {
            traefikComponentNodeInstance.setPluginInstances(null);
        }

        // Expose Interface of traefic ui
        List<InterfaceInstance> exposedInterfaces = new ArrayList<>();

        Interface exposedInterface = traefikComponent.getExposedInterfaces().get(0);
        InterfaceInstance uiInterfaceInstance = new InterfaceInstance();
        uiInterfaceInstance.setPort(exposedInterface.getPort());
        uiInterfaceInstance.setName(exposedInterface.getName());
        uiInterfaceInstance.setInterfaceObj(exposedInterface);
        uiInterfaceInstance.setInterfaceType(exposedInterface.getInterfaceType());
        uiInterfaceInstance.setComponentNodeInstance(traefikComponentNodeInstance);
        uiInterfaceInstance.setLastModified(new Date());
        uiInterfaceInstance.setDateCreated(new Date());
        interfaceInstanceDAO.save(uiInterfaceInstance);

        exposedInterfaces.add(uiInterfaceInstance);

        // Exposed interfaces of components
        if (null != componentNodeInstance.getInterfaceInstances() && !componentNodeInstance
                .getInterfaceInstances().isEmpty()) {

            componentNodeInstance.getInterfaceInstances().stream().forEach(interfaceInstance -> {

                try {
                    InterfaceInstance componentNodeInstanceExposedInterface = new InterfaceInstance();
                    componentNodeInstanceExposedInterface.setPort(interfaceInstance.getPort());
                    componentNodeInstanceExposedInterface.setName(interfaceInstance.getName());
                    componentNodeInstanceExposedInterface
                            .setInterfaceObj(interfaceInstance.getInterfaceObj());
                    componentNodeInstanceExposedInterface
                            .setComponentNodeInstance(traefikComponentNodeInstance);
                    componentNodeInstanceExposedInterface.setInterfaceType(interfaceInstance.getInterfaceType());
                    componentNodeInstanceExposedInterface.setLastModified(new Date());
                    componentNodeInstanceExposedInterface.setDateCreated(new Date());
                    interfaceInstanceDAO.save(componentNodeInstanceExposedInterface);
                    exposedInterfaces.add(componentNodeInstanceExposedInterface);

                    // Check type of interface
                    if (interfaceInstance.getInterfaceObj().getInterfaceType()
                            .equals(Interface.InterfaceType.CORE.name())) {

                        if (!mapOfNeededChanges
                                .containsKey(componentNodeInstance.getComponentNodeInstanceID())) {
                            mapOfNeededChanges.put(componentNodeInstance.getComponentNodeInstanceID(),
                                    traefikComponentNodeInstance);
                        }

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            });

        }

        traefikComponentNodeInstance.setInterfaceInstances(exposedInterfaces);

//        componentNodeInstanceDAO.save(traefikComponentNodeInstance);

//        ApplicationInstance existingApplicationInstance = applicationInstanceDAO.findById(applicationInstance.getId()).get();
//        existingApplicationInstance.getComponentNodeInstances().add(traefikComponentNodeInstance);
//        existingApplicationInstance.setLastModified(new Date());
//        applicationInstanceDAO.save(existingApplicationInstance);

        // TODO STEP5: Have to move constraints of Component Node Instance to Traefik Component Node Instance

        if (null != applicationInstance.getConstraints() && !applicationInstance.getConstraints()
                .isEmpty()) {

            applicationInstance.getConstraints().stream().filter(constraint ->
                    (null != constraint.getComponentNodeInstance() && constraint.getComponentNodeInstance()
                            .getComponentNodeInstanceID()
                            .equals(componentNodeInstance.getComponentNodeInstanceID()))
                            || (null != constraint.getInterfaceInstance() && constraint.getInterfaceInstance()
                            .getInterfaceInstanceID().equals(
                                    componentNodeInstance.getInterfaceInstances().get(0).getInterfaceInstanceID()))
                            || null != constraint.getGraphLinkNodeInstance()).forEach(constraint -> {

                // Found constraints that should be moved to LB

                try {

                    if (null != constraint.getComponentNodeInstance()) {

                        // Component Constraint
                        Constraint componentConstraint = new Constraint();
                        componentConstraint.setComponentNodeInstance(traefikComponentNodeInstance);
                        componentConstraint.setConstraintCategory(constraint.getConstraintCategory());
                        componentConstraint.setConstraintType(constraint.getConstraintType());
                        componentConstraint.setConstraintMetric(constraint.getConstraintMetric());
                        componentConstraint.setDeletableConstraint(constraint.getDeletableConstraint());
                        componentConstraint.setConstraintValue(constraint.getConstraintValue());
                        componentConstraint.setConstraintUnit(constraint.getConstraintUnit());
                        componentConstraint.setCountry(null);
                        componentConstraint.setGraphLinkNodeInstance(null);
                        componentConstraint.setInterfaceInstance(null);
                        componentConstraint.setQi(null);
                        componentConstraint.setDateCreated(new Date());
                        componentConstraint.setLastModified(new Date());
                        componentConstraint.setApplicationInstance(applicationInstance);
                        constraintDAO.save(componentConstraint);

                    } else if (null != constraint.getGraphLinkNodeInstance()) {

                        if (constraint.getGraphLinkNodeInstance().getComponentNodeInstanceTo()
                                .getComponentNodeInstanceID().equals(componentNodeInstance.getComponentNode())) {

                            // Graph Link Constraint
                            GraphLinkNodeInstance graphLinkNodeInstance = graphLinkNodeInstanceDAO
                                    .findById(constraint.getGraphLinkNodeInstance().getGraphLinkNodeInstanceID())
                                    .get();
                            graphLinkNodeInstance.setComponentNodeInstanceTo(traefikComponentNodeInstance);
                            graphLinkNodeInstance.setLastModified(new Date());
                            graphLinkNodeInstanceDAO.save(graphLinkNodeInstance);

                            constraint.setGraphLinkNodeInstance(graphLinkNodeInstance);
                            constraint.setLastModified(new Date());
                            constraintDAO.save(constraint);

                        }

                    } else {

                        InterfaceInstance interfaceInstance = interfaceInstanceDAO
                                .findAllByComponentNodeInstanceOrderByDateCreatedDesc(traefikComponentNodeInstance,
                                        null).getContent().stream().filter(
                                        iInstance -> iInstance.getInterfaceObj().getInterfaceID().equals(
                                                componentNodeInstance.getInterfaceInstances().get(0).getInterfaceObj()
                                                        .getInterfaceID())).collect(Collectors.toList()).get(0);

                        // Access Constraint
                        Constraint constraintAccessInterface = new Constraint();
                        constraintAccessInterface.setGraphLinkNodeInstance(null);
                        constraintAccessInterface
                                .setInterfaceInstance(interfaceInstance);  //uiInterfaceInstance ???
                        constraintAccessInterface.setApplicationInstance(applicationInstance);
                        constraintAccessInterface.setComponentNodeInstance(null);
                        constraintAccessInterface.setConstraintCategory(constraint.getConstraintCategory());
                        constraintAccessInterface.setConstraintMetric(null);
                        constraintAccessInterface.setConstraintType(constraint.getConstraintType());
                        constraintAccessInterface.setConstraintValue(null);
                        constraintAccessInterface.setCountry(null);
                        constraintAccessInterface.setQi(constraint.getQi());
                        constraintAccessInterface.setRadioServiceType(constraint.getRadioServiceType());
                        constraintAccessInterface.setResourceType(constraint.getResourceType());
                        constraintAccessInterface.setAllocationRetentionPriorityProfile(
                                constraint.getAllocationRetentionPriorityProfile());
                        constraintAccessInterface
                                .setMinimumGuaranteedBandwidth(constraint.getMinimumGuaranteedBandwidth());
                        constraintAccessInterface
                                .setMaximumRequiredBandwidth(constraint.getMaximumRequiredBandwidth());
                        constraintAccessInterface.setDeletableConstraint(constraint.getDeletableConstraint());
                        constraintAccessInterface.setDateCreated(new Date());
                        constraintAccessInterface.setLastModified(new Date());
                        constraintDAO.save(constraintAccessInterface);

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            });

        }

        // Change the ACCESS interfaces of the components instances to CORE
        if (null != componentNodeInstance.getInterfaceInstances() && !componentNodeInstance
                .getInterfaceInstances().isEmpty()) {

            componentNodeInstance.getInterfaceInstances().stream().forEach(interfaceInstance -> {
                if (interfaceInstance.getInterfaceType().equals(Interface.InterfaceType.ACCESS.name())) {
                    interfaceInstance.setInterfaceType(Interface.InterfaceType.CORE.name());

                    interfaceInstanceDAO.save(interfaceInstance);
                }
            });
        }

        // STEP3: Check if we need more than one workers
        if (componentNodeInstance.getMinimumWorkers() > 1
                && componentNodeInstance.getMaximumWorkers() > 1) {

            // Calculate Workers
            int numOfWorkers = componentNodeInstance.getMinimumWorkers();

            for (int i = 1; i < numOfWorkers; i++) {

                // Replicate Component Node Instance
                ComponentNodeInstance workerI = new ComponentNodeInstance();
                workerI.setName(componentNodeInstance.getName() + "Worker" + i);
                workerI.setLoadBalancer(false);
                workerI.setLoadBalancedBy(traefikComponentNodeInstance);
                workerI.setHexID(Util.createRandomHEXString());
                workerI.setCommand(
                        null != componentNodeInstance.getCommand() && !componentNodeInstance.getCommand()
                                .isEmpty() ? componentNodeInstance.getCommand() : null);
                workerI.setSshKey(
                        null != componentNodeInstance.getSshKey() ? componentNodeInstance.getSshKey() : null);
                workerI.setDateCreated(new Date());
                workerI.setProvider(componentNodeInstance.getProvider());
                workerI.setLastModified(new Date());
                workerI.setComponentNode(componentNodeInstance.getComponentNode());
                workerI.setApplicationInstance(applicationInstance);
                workerI.setStatusIDS(
                        null != componentNodeInstance.getStatusIDS() && componentNodeInstance.getStatusIDS()
                                .booleanValue());
                workerI.setStatusIPS(
                        null != componentNodeInstance.getStatusIPS() && componentNodeInstance.getStatusIPS()
                                .booleanValue());

                workerI.setNetworkModeHost(
                        null != componentNodeInstance.getNetworkModeHost() && componentNodeInstance.getNetworkModeHost()
                                .booleanValue());
                workerI.setPrivilege(
                        null != componentNodeInstance.getPrivilege() && componentNodeInstance.getPrivilege()
                                .booleanValue());
                workerI.setHostname(
                        null != componentNodeInstance.getHostname() && !componentNodeInstance.getHostname()
                                .isEmpty() ? componentNodeInstance.getHostname() : null);
                workerI.setDnsEntry(
                        null != componentNodeInstance.getDnsEntry() && !componentNodeInstance.getDnsEntry()
                                .isEmpty() ? componentNodeInstance.getDnsEntry() : null);
                workerI.setSharedMemorySize(
                        null != componentNodeInstance.getSharedMemorySize() && !componentNodeInstance.getSharedMemorySize()
                                .isEmpty() ? componentNodeInstance.getSharedMemorySize() : null);

                if (null != componentNodeInstance.getCapabilityAdds() && !componentNodeInstance.getCapabilityAdds().isEmpty()) {
                    Collection<Component.CapabilityAdd> newCapabilities = new ArrayList<>();
                    Collection<Component.CapabilityAdd> exCapabilities = componentNodeInstance.getCapabilityAdds();
                    exCapabilities.stream().forEach(capability -> {
                        newCapabilities.add(capability);
                    });

                    workerI.setCapabilityAdds(newCapabilities);
                } else {
                    workerI.setCapabilityAdds(null);
                }
                if (null != componentNodeInstance.getCapabilityDrops() && !componentNodeInstance.getCapabilityDrops().isEmpty()) {
                    Collection<Component.CapabilityDrop> newCapabilities = new ArrayList<>();
                    Collection<Component.CapabilityDrop> exCapabilities = componentNodeInstance.getCapabilityDrops();
                    exCapabilities.stream().forEach(capability -> {
                        newCapabilities.add(capability);
                    });

                    workerI.setCapabilityDrops(newCapabilities);
                } else {
                    workerI.setCapabilityDrops(null);
                }

                workerI.setMaximumWorkers(componentNodeInstance.getMaximumWorkers());
                workerI.setMinimumWorkers(componentNodeInstance.getMinimumWorkers());
                componentNodeInstanceDAO.save(workerI);

                ComponentNodeInstanceStatus componentNodeInstanceStatusWorkerI = new ComponentNodeInstanceStatus();
                componentNodeInstanceStatusWorkerI.setReportedChange(
                        OrchestratorChangedStatusNotification.ChangeType.GraphStatusChange.name());
                componentNodeInstanceStatusWorkerI.setLastModified(new Date());
                componentNodeInstanceStatusWorkerI.setDateCreated(new Date());
                componentNodeInstanceStatusWorkerI.setComponentNodeInstance(workerI);
                componentNodeInstanceStatusWorkerI.setMessage("Component is loading...");
                componentNodeInstanceStatusWorkerI.setStatus("LOADING");
                componentNodeInstanceStatusWorkerI.setApplicationInstance(applicationInstance);
                componentNodeInstanceStatusDAO.save(componentNodeInstanceStatusWorkerI);

                Constraint minWorkersConstraint = new Constraint();
                minWorkersConstraint.setComponentNodeInstance(workerI);
                minWorkersConstraint
                        .setConstraintCategory(Constraint.ConstraintCategory.COMPONENT_HOSTING.name());
                minWorkersConstraint.setConstraintType(Constraint.ConstraintType.HARD.name());
                minWorkersConstraint.setConstraintMetric(Constraint.ConstraintMetric.MIN_WORKERS.name());
                minWorkersConstraint.setDeletableConstraint(false);
                minWorkersConstraint.setConstraintValue(workerI.getMinimumWorkers() + "");
                minWorkersConstraint.setConstraintUnit("amount");
                minWorkersConstraint.setCountry(null);
                minWorkersConstraint.setGraphLinkNodeInstance(null);
                minWorkersConstraint.setInterfaceInstance(null);
                minWorkersConstraint.setQi(null);
                minWorkersConstraint.setDateCreated(new Date());
                minWorkersConstraint.setLastModified(new Date());
                minWorkersConstraint.setApplicationInstance(applicationInstance);
                constraintDAO.save(minWorkersConstraint);

                Constraint maxWorkersConstraint = new Constraint();
                maxWorkersConstraint.setComponentNodeInstance(workerI);
                maxWorkersConstraint
                        .setConstraintCategory(Constraint.ConstraintCategory.COMPONENT_HOSTING.name());
                maxWorkersConstraint.setConstraintType(Constraint.ConstraintType.HARD.name());
                maxWorkersConstraint.setConstraintMetric(Constraint.ConstraintMetric.MAX_WORKERS.name());
                maxWorkersConstraint.setDeletableConstraint(false);
                maxWorkersConstraint.setConstraintValue(workerI.getMaximumWorkers() + "");
                maxWorkersConstraint.setConstraintUnit("amount");
                maxWorkersConstraint.setCountry(null);
                maxWorkersConstraint.setGraphLinkNodeInstance(null);
                maxWorkersConstraint.setInterfaceInstance(null);
                maxWorkersConstraint.setQi(null);
                maxWorkersConstraint.setDateCreated(new Date());
                maxWorkersConstraint.setLastModified(new Date());
                maxWorkersConstraint.setApplicationInstance(applicationInstance);
                constraintDAO.save(maxWorkersConstraint);

                // Check required interfaces
                List<GraphLinkNodeInstance> graphLinkNodeInstances = graphLinkNodeInstanceDAO
                        .findAllByApplicationInstanceAndComponentNodeInstanceFrom(applicationInstance,
                                componentNodeInstance);

                if (null != graphLinkNodeInstances && !graphLinkNodeInstances.isEmpty()) {

                    graphLinkNodeInstances.stream().forEach(graphLinkNodeInstance -> {

                        GraphLinkNodeInstance graphLNI = new GraphLinkNodeInstance();
                        graphLNI.setGraphLinkNode(graphLinkNodeInstance.getGraphLinkNode());
                        graphLNI.setApplicationInstance(applicationInstance);
                        graphLNI.setComponentNodeInstanceTo(graphLinkNodeInstance.getComponentNodeInstanceTo());
                        graphLNI.setComponentNodeInstanceFrom(workerI);
                        graphLNI.setDateCreated(new Date());
                        graphLNI.setLastModified(new Date());
                        graphLinkNodeInstanceDAO.save(graphLNI);


                    });
                }

                // Check interface instances
                if (null != componentNodeInstance.getInterfaceInstances() && !componentNodeInstance
                        .getInterfaceInstances().isEmpty()) {
                    List<InterfaceInstance> interfaceInstances = new ArrayList<>();

                    componentNodeInstance.getInterfaceInstances().stream().forEach(interfaceInstance -> {

                        try {

                            InterfaceInstance interfaceInstanceNew = new InterfaceInstance();
                            interfaceInstanceNew.setDateCreated(new Date());
                            interfaceInstanceNew.setLastModified(new Date());
                            interfaceInstanceNew.setComponentNodeInstance(workerI);
                            interfaceInstanceNew.setInterfaceObj(interfaceInstance.getInterfaceObj());
                            interfaceInstanceNew.setName(interfaceInstance.getName());
                            interfaceInstanceNew.setPort(interfaceInstance.getPort());
                            interfaceInstanceNew.setInterfaceType(interfaceInstance.getInterfaceType());
                            interfaceInstanceDAO.save(interfaceInstanceNew);
                            interfaceInstances.add(interfaceInstanceNew);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    });

                    workerI.setInterfaceInstances(interfaceInstances);

                } else {

                    workerI.setInterfaceInstances(null);

                }

                // Check environmental variables
                if (null != componentNodeInstance.getEnvironmentalVariableInstances()
                        && !componentNodeInstance.getEnvironmentalVariableInstances().isEmpty()) {
                    List<EnvironmentalVariableInstance> environmentalVariableInstances = new ArrayList<>();

                    componentNodeInstance.getEnvironmentalVariableInstances().stream()
                            .forEach(environmentalVariableInstance -> {

                                try {

                                    EnvironmentalVariableInstance environmentalVariableInstanceNew = new EnvironmentalVariableInstance();
                                    environmentalVariableInstanceNew
                                            .setValue(environmentalVariableInstance.getValue());
                                    environmentalVariableInstanceNew.setKey(environmentalVariableInstance.getKey());
                                    environmentalVariableInstanceNew.setLastModified(new Date());
                                    environmentalVariableInstanceNew.setDateCreated(new Date());
                                    environmentalVariableInstanceNew.setEnvironmentalVariable(
                                            environmentalVariableInstance.getEnvironmentalVariable());
                                    environmentalVariableInstanceNew.setComponentNodeInstance(workerI);
                                    environmentalVariableInstanceDAO.save(environmentalVariableInstanceNew);
                                    environmentalVariableInstances.add(environmentalVariableInstanceNew);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            });

                    workerI.setEnvironmentalVariableInstances(environmentalVariableInstances);
                } else {

                    workerI.setEnvironmentalVariableInstances(null);

                }

                // Check plugin instances
                if (null != componentNodeInstance.getPluginInstances() && !componentNodeInstance
                        .getPluginInstances().isEmpty()) {
                    List<PluginInstance> pluginInstances = new ArrayList<>();

                    componentNodeInstance.getPluginInstances().stream().forEach(pluginInstance -> {

                        try {

                            PluginInstance pluginInstanceNew = new PluginInstance();
                            pluginInstanceNew.setComponentNodeInstance(workerI);
                            pluginInstanceNew.setDateCreated(new Date());
                            pluginInstanceNew.setLastModified(new Date());
                            pluginInstanceNew.setPlugin(pluginInstance.getPlugin());
                            pluginInstanceNew.setName(pluginInstance.getName());
                            pluginInstanceNew.setModuleName(
                                    null != pluginInstance.getModuleName() && !pluginInstance.getModuleName()
                                            .isEmpty() ? pluginInstance.getModuleName() : null);
                            pluginInstanceNew.setImmutablePlugin(pluginInstance.getImmutablePlugin());
                            pluginInstanceNew.setDeletedPlugin(false);

                            pluginInstanceDAO.save(pluginInstanceNew);
                            pluginInstances.add(pluginInstanceNew);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    });

                    workerI.setPluginInstances(pluginInstances);
                } else {

                    workerI.setPluginInstances(null);

                }

                if (null != componentNodeInstance.getiDRuleSetInstances() && !componentNodeInstance
                        .getiDRuleSetInstances().isEmpty()) {
                    List<IDRuleSetInstance> idRuleSetInstances = new ArrayList<>();

                    componentNodeInstance.getiDRuleSetInstances().stream().forEach(idRuleSetInstance -> {

                        try {

                            IDRuleSetInstance idRuleSetInstanceNew = new IDRuleSetInstance();
                            idRuleSetInstanceNew.setComponentNodeInstance(workerI);
                            idRuleSetInstanceNew.setDateCreated(new Date());
                            idRuleSetInstanceNew.setLastModified(new Date());
                            idRuleSetInstanceNew.setIdRuleSet(idRuleSetInstance.getIdRuleSet());
                            idRuleSetInstanceNew.setName(idRuleSetInstance.getName());

                            idRuleSetInstanceDAO.save(idRuleSetInstanceNew);
                            idRuleSetInstances.add(idRuleSetInstanceNew);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    });

                    workerI.setiDRuleSetInstances(idRuleSetInstances);
                } else {

                    workerI.setiDRuleSetInstances(null);

                }

                // Check devices
                if (null != componentNodeInstance.getDeviceInstances() && !componentNodeInstance
                        .getDeviceInstances().isEmpty()) {
                    List<DeviceInstance> deviceInstances = new ArrayList<>();

                    componentNodeInstance.getDeviceInstances().stream().forEach(deviceInstance -> {

                        try {

                            DeviceInstance deviceInstanceNew = new DeviceInstance();
                            deviceInstanceNew.setValue(deviceInstance.getValue());
                            deviceInstanceNew.setKey(deviceInstance.getKey());
                            deviceInstanceNew.setLastModified(new Date());
                            deviceInstanceNew.setDateCreated(new Date());
                            deviceInstanceNew.setDevice(deviceInstance.getDevice());
                            deviceInstanceNew.setComponentNodeInstance(workerI);
                            deviceInstanceDAO.save(deviceInstanceNew);
                            deviceInstances.add(deviceInstanceNew);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    });

                    workerI.setDeviceInstances(deviceInstances);
                } else {

                    workerI.setDeviceInstances(null);

                }

                // Flavors
                if (flavorInstanceDAO.findByComponentNodeInstance(componentNodeInstance).isPresent()) {

                    FlavorInstance oldFlavorInstance = flavorInstanceDAO
                            .findByComponentNodeInstance(componentNodeInstance).get();

                    FlavorInstance flavorInstanceNew = new FlavorInstance();
                    flavorInstanceNew.setDateCreated(new Date());
                    flavorInstanceNew.setLastModified(new Date());
                    flavorInstanceNew.setRam(oldFlavorInstance.getRam());
                    flavorInstanceNew.setStorage(oldFlavorInstance.getStorage());
                    flavorInstanceNew.setvCPUs(oldFlavorInstance.getvCPUs());
                    flavorInstanceNew.setComponentNodeInstance(workerI);
                    flavorInstanceDAO.save(flavorInstanceNew);
                    workerI.setFlavorInstance(flavorInstanceNew);

                    Constraint cpuConstraint = new Constraint();
                    cpuConstraint.setComponentNodeInstance(workerI);
                    cpuConstraint
                            .setConstraintCategory(Constraint.ConstraintCategory.COMPONENT_HOSTING.name());
                    cpuConstraint.setConstraintType(Constraint.ConstraintType.HARD.name());
                    cpuConstraint.setConstraintMetric(Constraint.ConstraintMetric.MIN_V_CPU.name());
                    cpuConstraint.setDeletableConstraint(false);
                    cpuConstraint.setConstraintValue(flavorInstanceNew.getvCPUs() + "");
                    cpuConstraint.setConstraintUnit("amount");
                    cpuConstraint.setCountry(null);
                    cpuConstraint.setGraphLinkNodeInstance(null);
                    cpuConstraint.setInterfaceInstance(null);
                    cpuConstraint.setQi(null);
                    cpuConstraint.setDateCreated(new Date());
                    cpuConstraint.setLastModified(new Date());
                    cpuConstraint.setApplicationInstance(applicationInstance);
                    constraintDAO.save(cpuConstraint);

                    Constraint ramConstraint = new Constraint();
                    ramConstraint.setComponentNodeInstance(workerI);
                    ramConstraint
                            .setConstraintCategory(Constraint.ConstraintCategory.COMPONENT_HOSTING.name());
                    ramConstraint.setConstraintType(Constraint.ConstraintType.HARD.name());
                    ramConstraint.setConstraintMetric(Constraint.ConstraintMetric.MIN_RAM.name());
                    ramConstraint.setDeletableConstraint(false);
                    ramConstraint.setConstraintValue(flavorInstanceNew.getRam() + "");
                    ramConstraint.setConstraintUnit("mb");
                    ramConstraint.setCountry(null);
                    ramConstraint.setGraphLinkNodeInstance(null);
                    ramConstraint.setInterfaceInstance(null);
                    ramConstraint.setQi(null);
                    ramConstraint.setDateCreated(new Date());
                    ramConstraint.setLastModified(new Date());
                    ramConstraint.setApplicationInstance(applicationInstance);
                    constraintDAO.save(ramConstraint);

                    Constraint storageConstraint = new Constraint();
                    storageConstraint.setComponentNodeInstance(workerI);
                    storageConstraint
                            .setConstraintCategory(Constraint.ConstraintCategory.COMPONENT_HOSTING.name());
                    storageConstraint.setConstraintType(Constraint.ConstraintType.HARD.name());
                    storageConstraint.setConstraintMetric(Constraint.ConstraintMetric.MIN_STORAGE.name());
                    storageConstraint.setDeletableConstraint(false);
                    storageConstraint.setConstraintValue(flavorInstanceNew.getStorage() + "");
                    storageConstraint.setConstraintUnit("gb");
                    storageConstraint.setCountry(null);
                    storageConstraint.setGraphLinkNodeInstance(null);
                    storageConstraint.setInterfaceInstance(null);
                    storageConstraint.setQi(null);
                    storageConstraint.setDateCreated(new Date());
                    storageConstraint.setLastModified(new Date());
                    storageConstraint.setApplicationInstance(applicationInstance);
                    constraintDAO.save(storageConstraint);

                } else {
                    workerI.setFlavorInstance(null);
                }

                // Health Checks
                if (healthCheckInstanceDAO.findByComponentNodeInstance(componentNodeInstance).isPresent()) {

                    HealthCheckInstance oldHealthCheckInstance = healthCheckInstanceDAO
                            .findByComponentNodeInstance(componentNodeInstance).get();

                    HealthCheckInstance healthCheckInstanceNew = new HealthCheckInstance();
                    healthCheckInstanceNew.setHealthCheck(oldHealthCheckInstance.getHealthCheck());
                    healthCheckInstanceNew.setDateCreated(new Date());
                    healthCheckInstanceNew.setLastModified(new Date());

                    healthCheckInstanceNew.setInterval(oldHealthCheckInstance.getInterval());
                    healthCheckInstanceNew.setArgs(
                            null != oldHealthCheckInstance.getArgs() && !oldHealthCheckInstance.getArgs()
                                    .isEmpty() ? oldHealthCheckInstance.getArgs() : null);
                    healthCheckInstanceNew.setHttpURL(
                            null != oldHealthCheckInstance.getHttpURL() && !oldHealthCheckInstance.getHttpURL()
                                    .isEmpty() ? oldHealthCheckInstance.getHttpURL() : null);

                    healthCheckInstanceNew.setComponentNodeInstance(workerI);
                    healthCheckInstanceDAO.save(healthCheckInstanceNew);
                    workerI.setHealthCheckInstance(healthCheckInstanceNew);

                } else {
                    workerI.setHealthCheckInstance(null);
                }

                // Locations
                if (null != componentNodeInstance.getLocationInstances() && !componentNodeInstance
                        .getLocationInstances().isEmpty()) {
                    List<LocationInstance> locationInstances = new ArrayList<>();

                    componentNodeInstance.getLocationInstances().stream().forEach(locationInstance -> {

                        try {

                            LocationInstance locationInstanceNew = new LocationInstance();
                            locationInstanceNew.setRegion(locationInstance.getRegion());
                            locationInstanceNew.setComponentNodeInstance(workerI);
                            locationInstanceNew.setCountry(
                                    null != locationInstance.getCountry() ? locationInstance.getCountry() : null);
                            locationInstanceNew.setDateCreated(new Date());
                            locationInstanceNew.setLastModified(new Date());
                            locationInstanceDAO.save(locationInstanceNew);
                            locationInstances.add(locationInstanceNew);

                            Constraint regionConstraint = new Constraint();
                            regionConstraint.setComponentNodeInstance(workerI);
                            regionConstraint
                                    .setConstraintCategory(Constraint.ConstraintCategory.COMPONENT_HOSTING.name());
                            regionConstraint.setConstraintType(Constraint.ConstraintType.HARD.name());
                            regionConstraint.setConstraintMetric(Constraint.ConstraintMetric.REGION.name());
                            regionConstraint.setDeletableConstraint(false);
                            regionConstraint.setConstraintValue(locationInstance.getRegion() + "");
                            regionConstraint.setConstraintUnit("region");
                            regionConstraint.setCountry(null);
                            regionConstraint.setGraphLinkNodeInstance(null);
                            regionConstraint.setInterfaceInstance(null);
                            regionConstraint.setQi(null);
                            regionConstraint.setDateCreated(new Date());
                            regionConstraint.setLastModified(new Date());
                            regionConstraint.setApplicationInstance(applicationInstance);
                            constraintDAO.save(regionConstraint);


                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    });

                    workerI.setLocationInstances(locationInstances);

                } else {

                    workerI.setLocationInstances(null);

                }

                // Volumes
                if (null != componentNodeInstance.getVolumeInstances() && !componentNodeInstance
                        .getVolumeInstances().isEmpty()) {
                    List<VolumeInstance> volumeInstances = new ArrayList<>();

                    componentNodeInstance.getVolumeInstances().stream().forEach(volumeInstance -> {

                        try {

                            VolumeInstance volumeInstanceNew = new VolumeInstance();
                            volumeInstanceNew.setDockerPath(volumeInstance.getDockerPath());
                            volumeInstanceNew.setHostPath(volumeInstance.getHostPath());
                            volumeInstanceNew.setVolume(volumeInstance.getVolume());
                            volumeInstanceNew.setLastModified(new Date());
                            volumeInstanceNew.setDateCreated(new Date());
                            volumeInstanceNew.setComponentNodeInstance(workerI);
                            volumeInstanceDAO.save(volumeInstanceNew);
                            volumeInstances.add(volumeInstanceNew);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    });

                    workerI.setVolumeInstances(volumeInstances);

                } else {

                    workerI.setVolumeInstances(null);

                }

                componentNodeInstanceDAO.save(workerI);

//                ApplicationInstance existingApplicationInstance = applicationInstanceDAO.findById(applicationInstance.getId()).get();
//                existingApplicationInstance.getComponentNodeInstances().add(workerI);
//                existingApplicationInstance.setLastModified(new Date());
//                applicationInstanceDAO.save(existingApplicationInstance);

                workers.put(workerI.getComponentNodeInstanceID(), workerI);

                // TODO Convert workers to orchestrator component node instances;
                OrchestratorComponentNodeInstance workerOrchestratorCNI = new OrchestratorComponentNodeInstance();
                workerOrchestratorCNI.setComponentNodeInstanceHexID(workerI.getHexID());
                workerOrchestratorCNI.setComponentNodeInstanceID(workerI.getComponentNodeInstanceID() + "");
                workerOrchestratorCNI.setComponentNodeInstanceName(workerI.getName());
                workerOrchestratorCNI
                        .setComponentNodeID(workerI.getComponentNode().getComponentNodeID() + "");
                workerOrchestratorCNI.setComponentNodeHexID(workerI.getComponentNode().getHexID());
                workerOrchestratorCNI.setComponentNodeName(workerI.getComponentNode().getName());
                workerOrchestratorCNI.setProviderID(vimID);
                workerOrchestratorCNI.setMinimumWorkers(workerI.getMinimumWorkers());
                workerOrchestratorCNI.setMaximumWorkers(workerI.getMaximumWorkers());
                workerOrchestratorCNI.setStatusIPS(
                        null != workerI.getStatusIPS() && workerI.getStatusIPS().booleanValue());
                workerOrchestratorCNI.setStatusIDS(
                        null != workerI.getStatusIDS() && workerI.getStatusIDS().booleanValue());

                workerOrchestratorCNI.setNetworkModeHost(
                        null != workerI.getNetworkModeHost() && workerI.getNetworkModeHost()
                                .booleanValue());
                workerOrchestratorCNI.setPrivilege(
                        null != workerI.getPrivilege() && workerI.getPrivilege()
                                .booleanValue());
                workerOrchestratorCNI.setHostname(
                        null != workerI.getHostname() && !workerI.getHostname()
                                .isEmpty() ? workerI.getHostname() : null);
                workerOrchestratorCNI.setDnsEntry(
                        null != workerI.getDnsEntry() && !workerI.getDnsEntry()
                                .isEmpty() ? workerI.getDnsEntry() : null);
                workerOrchestratorCNI.setSharedMemorySize(
                        null != workerI.getSharedMemorySize() && !workerI.getSharedMemorySize()
                                .isEmpty() ? workerI.getSharedMemorySize() : null);
                workerOrchestratorCNI.setDockerUsername(
                        null != workerI.getComponentNode().getComponent().getDockerUsername()
                                && !workerI.getComponentNode().getComponent().getDockerUsername()
                                .isEmpty() ? workerI.getComponentNode().getComponent().getDockerUsername() : null);
                workerOrchestratorCNI.setDockerPassword(
                        null != workerI.getComponentNode().getComponent().getDockerPassword()
                                && !workerI.getComponentNode().getComponent().getDockerPassword()
                                .isEmpty() ? workerI.getComponentNode().getComponent().getDockerPassword() : null);

                if (null != workerI.getCapabilityAdds() && !workerI.getCapabilityAdds().isEmpty()) {
                    Collection<String> newCapabilities = new ArrayList<>();
                    Collection<Component.CapabilityAdd> exCapabilities = workerI.getCapabilityAdds();
                    exCapabilities.stream().forEach(capability -> {
                        newCapabilities.add(capability.getFriendlyName());
                    });

                    workerOrchestratorCNI.setCapabilityAdds(newCapabilities);
                } else {
                    workerOrchestratorCNI.setCapabilityAdds(null);
                }
                if (null != workerI.getCapabilityDrops() && !workerI.getCapabilityDrops().isEmpty()) {
                    Collection<String> newCapabilities = new ArrayList<>();
                    Collection<Component.CapabilityDrop> exCapabilities = workerI.getCapabilityDrops();
                    exCapabilities.stream().forEach(capability -> {
                        newCapabilities.add(capability.getFriendlyName());
                    });

                    workerOrchestratorCNI.setCapabilityDrops(newCapabilities);
                } else {
                    workerOrchestratorCNI.setCapabilityDrops(null);
                }

                workerOrchestratorCNI.setCommand(
                        null != workerI.getCommand() && !workerI.getCommand().isEmpty() ? Arrays
                                .asList(workerI.getCommand()) : null);

                workerOrchestratorCNI.setImage(workerI.getComponentNode().getComponent().getDockerImage());
                workerOrchestratorCNI
                        .setRegistry(workerI.getComponentNode().getComponent().getDockerRegistry());

                if (null != workerI.getSshKey()) {
                    workerOrchestratorCNI.setSshKey(workerI.getSshKey().getSshKey());
                } else {
                    workerOrchestratorCNI.setSshKey(null);
                }

                // Scaling
                OrchestratorElasticity orchestratorElasticity = new OrchestratorElasticity();
                orchestratorElasticity.setProfile(workerI.getComponentNode().getComponent().getElasticityController());
                orchestratorElasticity
                        .setType(workerI.getComponentNode().getComponent().getElasticityControllerMode());
                workerOrchestratorCNI.setMonitoringElasticity(orchestratorElasticity);

                // Required Interfaces
                List<GraphLinkNodeInstance> graphLinkNodeInstancesOfWorkerI = graphLinkNodeInstanceDAO
                        .findAllByApplicationInstanceAndComponentNodeInstanceFrom(applicationInstance, workerI);

                if (null != graphLinkNodeInstancesOfWorkerI && !graphLinkNodeInstancesOfWorkerI.isEmpty()) {

                    List<OrchestratorDependency> dependsOn = new ArrayList<>();

                    graphLinkNodeInstancesOfWorkerI.stream()
                            .filter(graphLinkNodeInstance -> graphLinkNodeInstance.getComponentNodeInstanceFrom()
                                    .getComponentNodeInstanceID().equals(workerI.getComponentNodeInstanceID()))
                            .forEach(graphLinkNodeInstance -> {

                                try {

                                    if (dependsOn.stream().filter(dep -> dep.getDependency()
                                                    .equals(graphLinkNodeInstance.getComponentNodeInstanceTo().getHexID()))
                                            .collect(Collectors.toList()).isEmpty()) {

                                        OrchestratorDependency orchestratorDependency = new OrchestratorDependency();
                                        orchestratorDependency.setDependency(
                                                graphLinkNodeInstance.getComponentNodeInstanceTo().getHexID());
                                        orchestratorDependency
                                                .setNetworkAttachmentPoint(workerI.getProvider().getNetworkID());
                                        orchestratorDependency.setVna(
                                                graphLinkNodeInstance.getGraphLinkNode().getGraphLink().getInterfaceObj()
                                                        .getVna());

                                        // TODO CHECK
                                        dependsOn.add(orchestratorDependency);

                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();

                                }

                            });

                    workerOrchestratorCNI.setDependsOn(dependsOn);

                }

                // Exposed Interfaces
                if (null != workerI.getInterfaceInstances() && !workerI.getInterfaceInstances().isEmpty()) {

                    List<OrchestratorPort> ports = new ArrayList<>();

                    workerI.getInterfaceInstances().stream().forEach(interfaceInstance -> {

                        OrchestratorPort orchestratorPort = new OrchestratorPort();
                        orchestratorPort.setTarget(interfaceInstance.getInterfaceObj().getPort());
                        orchestratorPort.setPublished(interfaceInstance.getPort());
                        orchestratorPort
                                .setProtocol(interfaceInstance.getInterfaceObj().getTransmissionProtocol());
                        orchestratorPort.setVna(interfaceInstance.getInterfaceObj().getVna());
                        orchestratorPort.setType(interfaceInstance.getInterfaceType());
                        orchestratorPort.setNetworkAttachmentPoint(workerI.getProvider().getNetworkID());
                        ports.add(orchestratorPort);

                    });

                    workerOrchestratorCNI.setPorts(ports);

                }

                if (null != workerI.getEnvironmentalVariableInstances() && !workerI
                        .getEnvironmentalVariableInstances().isEmpty()) {

                    // Environmental Variables
                    Map<String, String> orchestratorEnvironmentalVariables = new HashMap<>();
                    workerI.getEnvironmentalVariableInstances().stream()
                            .forEach(environmentalVariableInstance -> {

//                        if (environmentalVariableInstance.getValue().startsWith("@")) {
//
//                            // TODO CHECK
//                            // Find @ComponentNodeName in order to replace it with @ComponentNodeInstanceName
//                            ComponentNode componentNode = componentNodeDAO.findByName(environmentalVariableInstance.getValue().substring(1)).get();
//                            List<ComponentNodeInstance> cniList = existingApplicationInstance.getComponentNodeInstances().stream().filter(cNI -> cNI.getComponentNode().getComponentNodeID().equals(componentNode.getComponentNodeID())).collect(Collectors.toList());
//
//                            if (null != componentNode && null != cniList && !cniList.isEmpty()) {
//
//                                String componentNodeInstanceName = cniList.get(0).getName();
//
//                                orchestratorEnvironmentalVariables.put(environmentalVariableInstance.getKey(), "@" + componentNodeInstanceName);
//
//                            }
//
//                        } else {

                                orchestratorEnvironmentalVariables.put(environmentalVariableInstance.getKey(),
                                        environmentalVariableInstance.getValue());

//                        }

                            });

                    workerOrchestratorCNI.setEnvironmentalVariables(orchestratorEnvironmentalVariables);

                }

                if (null != workerI.getPluginInstances() && !workerI.getPluginInstances().isEmpty()) {

                    List<OrchestratorPlugin> orchestratorPlugins = new ArrayList<>();

                    workerI.getPluginInstances().stream().forEach(pluginInstance -> {

                        Plugin plugin = pluginDAO.findById(pluginInstance.getPlugin().getPluginID()).get();

                        OrchestratorPlugin orchestratorPlugin = new OrchestratorPlugin();
                        orchestratorPlugin.setId(pluginInstance.getPluginInstanceID() + "");
                        orchestratorPlugin.setName(plugin.getName());
                        orchestratorPlugin.setModuleName(
                                null != plugin.getModuleName() && !plugin.getModuleName().isEmpty() ? plugin
                                        .getModuleName() : null);
                        orchestratorPlugin.setDefaultPlugin(plugin.getDefaultPlugin().booleanValue());
                        orchestratorPlugin.setImmutablePlugin(plugin.getImmutablePlugin().booleanValue());
                        orchestratorPlugin.setDownloadURL(
                                null != plugin.getDownloadURL() && !plugin.getDownloadURL().isEmpty() ? plugin
                                        .getDownloadURL() : null);
                        orchestratorPlugin.setPluginType(
                                null != plugin.getPluginType() && !plugin.getPluginType().isEmpty()
                                        ? Plugin.PluginType.valueOf(plugin.getPluginType()).name() : null);
                        orchestratorPlugin.setPort(
                                null != plugin.getPort() && !plugin.getPort().isEmpty() ? plugin.getPort() : null);
                        orchestratorPlugin.setEndpoint(
                                null != plugin.getEndpoint() && !plugin.getEndpoint().isEmpty() ? plugin
                                        .getEndpoint() : null);
                        orchestratorPlugin.setDisabledPlugin(pluginInstance.getDeletedPlugin().booleanValue());
                        orchestratorPlugins.add(orchestratorPlugin);

                    });

                    workerOrchestratorCNI.setPlugins(orchestratorPlugins);

                }

                if (null != workerI.getDeviceInstances() && !workerI.getDeviceInstances().isEmpty()) {

                    // Devices
                    Map<String, String> orchestratorDevices = new HashMap<>();
                    workerI.getDeviceInstances().stream().forEach(deviceInstance -> {

                        orchestratorDevices.put(deviceInstance.getKey(), deviceInstance.getValue());

                    });

                    workerOrchestratorCNI.setDevices(orchestratorDevices);

                }

                if (null != workerI.getVolumeInstances() && !workerI.getVolumeInstances().isEmpty()) {

                    // Volumes
                    Map<String, String> orchestratorVolumes = new HashMap<>();
                    workerI.getVolumeInstances().stream().filter(volumeInstance -> !volumeInstance.getHostPath().isEmpty()).forEach(volumeInstance -> {

                        orchestratorVolumes.put(volumeInstance.getHostPath(), volumeInstance.getDockerPath());

                    });

                    workerOrchestratorCNI.setVolumes(orchestratorVolumes);

                }

                if (null != workerI.getFlavorInstance()) {

                    OrchestratorFlavor orchestratorFlavor = new OrchestratorFlavor();
                    orchestratorFlavor.setRam(workerI.getFlavorInstance().getRam());
                    orchestratorFlavor.setvCPUs(workerI.getFlavorInstance().getvCPUs());
                    orchestratorFlavor.setStorage(workerI.getFlavorInstance().getStorage());
                    workerOrchestratorCNI.setFlavor(orchestratorFlavor);

                }

                if (null != workerI.getHealthCheckInstance()) {

                    OrchestratorHealthCheck orchestratorHealthCheck = new OrchestratorHealthCheck();

                    orchestratorHealthCheck.setInterval(workerI.getHealthCheckInstance().getInterval().toString());
                    orchestratorHealthCheck.setArgs(
                            null != workerI.getHealthCheckInstance().getArgs() && !workerI
                                    .getHealthCheckInstance().getArgs().isEmpty() ? workerI.getHealthCheckInstance()
                                    .getArgs() : null);
                    orchestratorHealthCheck.setHttpURL(
                            null != workerI.getHealthCheckInstance().getHttpURL() && !workerI
                                    .getHealthCheckInstance().getHttpURL().isEmpty() ? workerI
                                    .getHealthCheckInstance().getHttpURL() : null);

                    workerOrchestratorCNI.setHealthCheck(orchestratorHealthCheck);

                }

                if (null != workerI.getLocationInstances() && !workerI.getLocationInstances().isEmpty()) {

                    List<OrchestratorLocation> locations = new ArrayList<>();

                    workerI.getLocationInstances().stream().forEach(locationInstance -> {

                        OrchestratorLocation orchestratorLocation = new OrchestratorLocation();
                        orchestratorLocation.setRegion(locationInstance.getRegion());
                        orchestratorLocation.setCountry(null);
                        locations.add(orchestratorLocation);

                    });

                    workerOrchestratorCNI.setLocations(locations);
                }

                workerOrchestratorCNI.setLoadBalancer(false);
                workerOrchestratorCNI.setLambdaProxy(false);

                // TODO

                services.put(workerOrchestratorCNI.getComponentNodeInstanceID(), workerOrchestratorCNI);

            }
        }

        // STEP3b: Update also component node instance that it is load balanced.
        componentNodeInstance.setLoadBalancedBy(traefikComponentNodeInstance);
        componentNodeInstanceDAO.save(componentNodeInstance);

        // STEP3c: Create GraphLinkNodeInstances between LB and Workers
        if (!workers.isEmpty()) {

            workers.entrySet().stream().forEach(entry -> {

                ComponentNodeInstance worker = entry.getValue();
                ComponentNodeInstance loadBalancer = worker.getLoadBalancedBy();

                GraphLinkNodeInstance graphLinkNodeInstance = new GraphLinkNodeInstance();
                graphLinkNodeInstance.setComponentNodeInstanceFrom(loadBalancer);
                graphLinkNodeInstance.setComponentNodeInstanceTo(worker);
                graphLinkNodeInstance.setDateCreated(new Date());
                graphLinkNodeInstance.setLastModified(new Date());
                graphLinkNodeInstance.setApplicationInstance(applicationInstance);
                graphLinkNodeInstance.setGraphLinkNode(null);
                graphLinkNodeInstanceDAO.save(graphLinkNodeInstance);

            });

        }

        // STEP4: Convert Traefik Component Node Instance to Orchestrator Component Node Instance
        OrchestratorComponentNodeInstance loadBalancer = new OrchestratorComponentNodeInstance();
        loadBalancer.setComponentNodeInstanceHexID("" + traefikComponentNodeInstance.getHexID());
        loadBalancer
                .setComponentNodeInstanceID("" + traefikComponentNodeInstance.getComponentNodeInstanceID());
        loadBalancer.setComponentNodeInstanceName(traefikComponentNodeInstance.getName());
        loadBalancer.setComponentNodeHexID("" + traefikComponentNode.getHexID());
        loadBalancer.setComponentNodeID("" + traefikComponentNode.getComponentNodeID());
        loadBalancer.setComponentNodeName(traefikComponentNode.getName());
        loadBalancer.setProviderID(vimID);

        OrchestratorElasticity orchestratorElasticity = new OrchestratorElasticity();
        orchestratorElasticity.setProfile(traefikComponent.getElasticityController());
        loadBalancer.setMonitoringElasticity(orchestratorElasticity);

        List<String> commands = new ArrayList<>();
        for (String subCommand : traefikComponentNodeInstance.getCommand().split("\\,")) {

            if (!commands.contains(subCommand)) {
                commands.add(subCommand);
            }
        }

        loadBalancer.setCommand(commands);

        loadBalancer.setImage(traefikComponent.getDockerImage());
        loadBalancer.setRegistry(traefikComponent.getDockerRegistry());
        loadBalancer.setDockerUsername(traefikComponent.getDockerUsername());
        loadBalancer.setDockerPassword(traefikComponent.getDockerPassword());
        loadBalancer.setLoadBalancer(true);
        loadBalancer.setLambdaProxy(false);

        if (null != traefikComponentNodeInstance.getSshKey()) {
            loadBalancer.setSshKey(traefikComponentNodeInstance.getSshKey().getSshKey());
        } else {
            loadBalancer.setSshKey(null);
        }

        // TODO What if initial component has more than one exposed interfaces ????

        OrchestratorPort orchestratorPortA = new OrchestratorPort();
        orchestratorPortA.setProtocol(Interface.TransmissionProtocol.TCP.name());
        orchestratorPortA.setTarget(uiInterfaceInstance.getPort());
        orchestratorPortA.setPublished(uiInterfaceInstance.getPort());
        orchestratorPortA.setVna(uiInterfaceInstance.getInterfaceObj().getVna());
        orchestratorPortA.setType(uiInterfaceInstance.getInterfaceType());
        orchestratorPortA.setNetworkAttachmentPoint(componentNodeInstance.getProvider().getNetworkID());

        OrchestratorPort orchestratorPortB = new OrchestratorPort();
        orchestratorPortB.setProtocol(
                componentNodeInstance.getInterfaceInstances().get(0).getInterfaceObj()
                        .getTransmissionProtocol());
        orchestratorPortB.setTarget(componentNodeInstance.getInterfaceInstances().get(0).getPort());
        orchestratorPortB.setPublished(componentNodeInstance.getInterfaceInstances().get(0).getPort());
        orchestratorPortB
                .setVna(componentNodeInstance.getInterfaceInstances().get(0).getInterfaceObj().getVna());
        orchestratorPortB.setType(componentNodeInstance.getInterfaceInstances().get(0).getInterfaceObj().getInterfaceType());
        orchestratorPortB.setNetworkAttachmentPoint(componentNodeInstance.getProvider().getNetworkID());

        loadBalancer.setPorts(Arrays.asList(orchestratorPortA, orchestratorPortB));
        loadBalancer.setDependsOn(Arrays.asList(
                new OrchestratorDependency(componentNodeInstance.getComponentNode().getHexID(),
                        componentNodeInstance.getProvider().getNetworkID(),
                        componentNodeInstance.getInterfaceInstances().get(0).getInterfaceObj().getVna())));

        if (null != traefikComponentNodeInstance.getFlavorInstance()) {

            OrchestratorFlavor orchestratorFlavor = new OrchestratorFlavor();
            orchestratorFlavor.setRam(traefikComponentNodeInstance.getFlavorInstance().getRam());
            orchestratorFlavor.setvCPUs(traefikComponentNodeInstance.getFlavorInstance().getvCPUs());
            orchestratorFlavor.setStorage(traefikComponentNodeInstance.getFlavorInstance().getStorage());
            loadBalancer.setFlavor(orchestratorFlavor);

        }

        if (null != traefikComponentNodeInstance.getHealthCheckInstance()) {

            OrchestratorHealthCheck orchestratorHealthCheck = new OrchestratorHealthCheck();
            orchestratorHealthCheck
                    .setInterval(traefikComponentNodeInstance.getHealthCheckInstance().getInterval().toString());
            orchestratorHealthCheck.setArgs(
                    null != traefikComponentNodeInstance.getHealthCheckInstance().getArgs()
                            && !traefikComponentNodeInstance.getHealthCheckInstance().getArgs().isEmpty()
                            ? traefikComponentNodeInstance.getHealthCheckInstance().getArgs() : null);
            orchestratorHealthCheck.setHttpURL(
                    null != traefikComponentNodeInstance.getHealthCheckInstance().getHttpURL()
                            && !traefikComponentNodeInstance.getHealthCheckInstance().getHttpURL().isEmpty()
                            ? traefikComponentNodeInstance.getHealthCheckInstance().getHttpURL() : null);
            loadBalancer.setHealthCheck(orchestratorHealthCheck);

        }

        if (null != traefikComponentNodeInstance.getPluginInstances() && !traefikComponentNodeInstance
                .getPluginInstances().isEmpty()) {

            List<OrchestratorPlugin> orchestratorPlugins = new ArrayList<>();

            traefikComponentNodeInstance.getPluginInstances().stream().forEach(pluginInstance -> {

                Plugin plugin = pluginDAO.findById(pluginInstance.getPlugin().getPluginID()).get();

                OrchestratorPlugin orchestratorPlugin = new OrchestratorPlugin();
                orchestratorPlugin.setId(pluginInstance.getPluginInstanceID() + "");
                orchestratorPlugin.setName(plugin.getName());
                orchestratorPlugin.setModuleName(
                        null != plugin.getModuleName() && !plugin.getModuleName().isEmpty() ? plugin
                                .getModuleName() : null);
                orchestratorPlugin.setDefaultPlugin(plugin.getDefaultPlugin().booleanValue());
                orchestratorPlugin.setImmutablePlugin(plugin.getImmutablePlugin().booleanValue());
                orchestratorPlugin.setDownloadURL(
                        null != plugin.getDownloadURL() && !plugin.getDownloadURL().isEmpty() ? plugin
                                .getDownloadURL() : null);
                orchestratorPlugin.setPluginType(
                        null != plugin.getPluginType() && !plugin.getPluginType().isEmpty() ? Plugin.PluginType
                                .valueOf(plugin.getPluginType()).name() : null);
                orchestratorPlugin.setPort(
                        null != plugin.getPort() && !plugin.getPort().isEmpty() ? plugin.getPort() : null);
                orchestratorPlugin.setEndpoint(
                        null != plugin.getEndpoint() && !plugin.getEndpoint().isEmpty() ? plugin.getEndpoint()
                                : null);
                orchestratorPlugin.setDisabledPlugin(pluginInstance.getDeletedPlugin().booleanValue());
                orchestratorPlugins.add(orchestratorPlugin);

            });

            loadBalancer.setPlugins(orchestratorPlugins);

        }

        services.put(loadBalancer.getComponentNodeInstanceID(), loadBalancer);

        return traefikComponentNodeInstance.getHexID();

    }

    public static String addLambdaProxyLoadBalancerCNIAndMinimumWorkers(
            Map<Long, ComponentNodeInstance> lambdaProxyLoadBalancers,
            Map<Long, OrchestratorComponentNodeInstance> lambdaProxyOrchestratorLoadBalancers,
            Map<Long, ComponentNodeInstance> mapOfNeededChanges,
            Map<String, OrchestratorComponentNodeInstance> services,
            ComponentPlacement componentPlacement, Application application,
            ApplicationInstance applicationInstance, ComponentNodeInstance componentNodeInstance,
            String vimID, ApplicationDAO applicationDAO, ApplicationInstanceDAO applicationInstanceDAO,
            ComponentDAO componentDAO, ComponentNodeDAO componentNodeDAO,
            ComponentNodeInstanceDAO componentNodeInstanceDAO,
            ComponentNodeInstanceStatusDAO componentNodeInstanceStatusDAO,
            InterfaceInstanceDAO interfaceInstanceDAO,
            EnvironmentalVariableInstanceDAO environmentalVariableInstanceDAO,
            HealthCheckDAO healthCheckDAO, HealthCheckInstanceDAO healthCheckInstanceDAO,
            DeviceInstanceDAO deviceInstanceDAO, FlavorInstanceDAO flavorInstanceDAO,
            LocationInstanceDAO locationInstanceDAO, VolumeInstanceDAO volumeInstanceDAO,
            GraphLinkNodeInstanceDAO graphLinkNodeInstanceDAO, RequirementDAO requirementDAO,
            ConstraintDAO constraintDAO, RuntimePolicyDAO runtimePolicyDAO,
            PluginInstanceDAO pluginInstanceDAO, PluginDAO pluginDAO,
            IDRuleSetInstanceDAO idRuleSetInstanceDAO, IDRuleSetDAO idRuleSetDAO,
            EntityManager entityManager, String consulURL, String consulURLIPv6) {

        logger.info("Adding LambdaProxy balancer for CNI: " + componentNodeInstance.getName());

        // STEP0: Initialize workers map
        Map<Long, ComponentNodeInstance> workers = new HashMap<>();
        workers.put(componentNodeInstance.getComponentNodeInstanceID(), componentNodeInstance);

        ComponentNodeInstance lambdaProxyLoadBalancer = null;

        // STEP1: Create component node instance for LambdaProxy Load Balancer
        List<InterfaceInstance> exposedInterfaces = null;
        if (lambdaProxyLoadBalancers == null || lambdaProxyLoadBalancers.isEmpty()) {

            logger.info("Lambda Proxy is null, creating new one...");

            Component lambdaProxyComponent = componentDAO.findByName("Traefik").get();
            ComponentNode lambdaProxyComponentNode = new ComponentNode();
            lambdaProxyComponentNode.setComponent(lambdaProxyComponent);
            lambdaProxyComponentNode.setDateCreated(new Date());
            lambdaProxyComponentNode.setLastModified(new Date());
            lambdaProxyComponentNode.setName("LambdaProxy");
            lambdaProxyComponentNode.setHexID(Util.createRandomHEXString());
            componentNodeDAO.save(lambdaProxyComponentNode);

            lambdaProxyLoadBalancer = new ComponentNodeInstance();
            lambdaProxyLoadBalancer.setHexID(Util.createRandomHEXString());
            lambdaProxyLoadBalancer.setSshKey(componentNodeInstance.getSshKey());
            lambdaProxyLoadBalancer.setLoadBalancer(true);
            lambdaProxyLoadBalancer.setLoadBalancedBy(null);
            lambdaProxyLoadBalancer.setVolumeInstances(null);
            lambdaProxyLoadBalancer.setProvider(componentNodeInstance.getProvider());
            lambdaProxyLoadBalancer.setLocationInstances(null);
            lambdaProxyLoadBalancer.setEnvironmentalVariableInstances(null);
            lambdaProxyLoadBalancer.setDeviceInstances(null);
            lambdaProxyLoadBalancer.setStatusIDS(false);
            lambdaProxyLoadBalancer.setStatusIPS(false);

            lambdaProxyLoadBalancer.setNetworkModeHost(false);
            lambdaProxyLoadBalancer.setPrivilege(false);
            lambdaProxyLoadBalancer.setHostname(null);
            lambdaProxyLoadBalancer.setDnsEntry(null);
            lambdaProxyLoadBalancer.setSharedMemorySize(null);

            if (null != componentNodeInstance.getCapabilityAdds() && !componentNodeInstance.getCapabilityAdds().isEmpty()) {
                Collection<Component.CapabilityAdd> newCapabilities = new ArrayList<>();
                Collection<Component.CapabilityAdd> exCapabilities = componentNodeInstance.getCapabilityAdds();
                exCapabilities.stream().forEach(capability -> {
                    newCapabilities.add(capability);
                });

                lambdaProxyLoadBalancer.setCapabilityAdds(newCapabilities);
            } else {
                lambdaProxyLoadBalancer.setCapabilityAdds(null);
            }
            if (null != componentNodeInstance.getCapabilityDrops() && !componentNodeInstance.getCapabilityDrops().isEmpty()) {
                Collection<Component.CapabilityDrop> newCapabilities = new ArrayList<>();
                Collection<Component.CapabilityDrop> exCapabilities = componentNodeInstance.getCapabilityDrops();
                exCapabilities.stream().forEach(capability -> {
                    newCapabilities.add(capability);
                });

                lambdaProxyLoadBalancer.setCapabilityDrops(newCapabilities);
            } else {
                lambdaProxyLoadBalancer.setCapabilityDrops(null);
            }

            lambdaProxyLoadBalancer.setMinimumWorkers(1);
            lambdaProxyLoadBalancer.setMaximumWorkers(1);
            lambdaProxyLoadBalancer.setLastModified(new Date());
            lambdaProxyLoadBalancer.setDateCreated(new Date());
            lambdaProxyLoadBalancer.setComponentNode(lambdaProxyComponentNode);
            lambdaProxyLoadBalancer.setName(lambdaProxyComponentNode.getName());
            lambdaProxyLoadBalancer.setApplicationInstance(applicationInstance);

            String command;
            if (applicationInstance.getOverlay()) {
                command = "-c,--api,--consul.prefix=" + application.getHexID() + "/" + applicationInstance
                        .getHexID() + "/" + lambdaProxyComponentNode.getHexID()
                        + "/lbConfiguration,--consul.endpoint=" + consulURLIPv6;
            } else {
                command = "-c,--api,--consul.prefix=" + application.getHexID() + "/" + applicationInstance
                        .getHexID() + "/" + lambdaProxyComponentNode.getHexID()
                        + "/lbConfiguration,--consul.endpoint=" + consulURL;
            }
            lambdaProxyLoadBalancer.setCommand(command);

            componentNodeInstanceDAO.save(lambdaProxyLoadBalancer);

            SortedSet<ComponentNodeInstance> currentComponentsNodeInstances = applicationInstance.getComponentNodeInstances();
            currentComponentsNodeInstances.add(lambdaProxyLoadBalancer);
            applicationInstance.setComponentNodeInstances(currentComponentsNodeInstances);

            ComponentNodeInstanceStatus componentNodeInstanceStatus = new ComponentNodeInstanceStatus();
            componentNodeInstanceStatus.setReportedChange(
                    OrchestratorChangedStatusNotification.ChangeType.GraphStatusChange.name());
            componentNodeInstanceStatus.setLastModified(new Date());
            componentNodeInstanceStatus.setDateCreated(new Date());
            componentNodeInstanceStatus.setComponentNodeInstance(lambdaProxyLoadBalancer);
            componentNodeInstanceStatus.setMessage("Component is loading...");
            componentNodeInstanceStatus.setStatus("LOADING");
            componentNodeInstanceStatus.setApplicationInstance(applicationInstance);
            componentNodeInstanceStatusDAO.save(componentNodeInstanceStatus);

            Requirement requirement = requirementDAO.findByComponent(lambdaProxyComponent).get();

            FlavorInstance flavorInstance = new FlavorInstance();
            flavorInstance.setComponentNodeInstance(lambdaProxyLoadBalancer);
            flavorInstance.setvCPUs(requirement.getvCPUs());
            flavorInstance.setStorage(requirement.getStorage());
            flavorInstance.setRam(requirement.getRam());
            flavorInstance.setLastModified(new Date());
            flavorInstance.setDateCreated(new Date());
            flavorInstanceDAO.save(flavorInstance);
            lambdaProxyLoadBalancer.setFlavorInstance(flavorInstance);

            HealthCheck healthCheck = healthCheckDAO.findByComponent(lambdaProxyComponent).get();

            HealthCheckInstance healthCheckInstance = new HealthCheckInstance();
            healthCheckInstance.setHealthCheck(healthCheck);
            healthCheckInstance.setComponentNodeInstance(lambdaProxyLoadBalancer);
            healthCheckInstance.setName(healthCheck.getName());
            healthCheckInstance.setInterval(healthCheck.getInterval());

            healthCheckInstance.setArgs(
                    null != healthCheck.getArgs() && !healthCheck.getArgs().isEmpty() ? healthCheck.getArgs()
                            : null);
            healthCheckInstance.setHttpURL(
                    null != healthCheck.getHttpURL() && !healthCheck.getHttpURL().isEmpty() ? healthCheck
                            .getHttpURL() : null);

            healthCheckInstance.setLastModified(new Date());
            healthCheckInstance.setDateCreated(new Date());
            healthCheckInstanceDAO.save(healthCheckInstance);
            lambdaProxyLoadBalancer.setHealthCheckInstance(healthCheckInstance);

            // Expose Interface of traefic ui
            exposedInterfaces = new ArrayList<>();

            Interface exposedInterface = lambdaProxyComponent.getExposedInterfaces().get(0);
            InterfaceInstance uiInterfaceInstance = new InterfaceInstance();
            uiInterfaceInstance.setPort(exposedInterface.getPort());
            uiInterfaceInstance.setName(exposedInterface.getName());
            uiInterfaceInstance.setInterfaceObj(exposedInterface);
            uiInterfaceInstance.setComponentNodeInstance(lambdaProxyLoadBalancer);
            uiInterfaceInstance.setInterfaceType(exposedInterface.getInterfaceType());
            uiInterfaceInstance.setLastModified(new Date());
            uiInterfaceInstance.setDateCreated(new Date());
            interfaceInstanceDAO.save(uiInterfaceInstance);

            exposedInterfaces.add(uiInterfaceInstance);

            List<Plugin> lambdaProxyPlugins = new ArrayList<>(lambdaProxyComponent.getPlugins());

            // Plugins
            if (null != lambdaProxyPlugins && !lambdaProxyPlugins.isEmpty()) {

                List<PluginInstance> pluginInstances = new ArrayList<>();

                for (Plugin plugin : lambdaProxyPlugins) {

                    try {

                        Plugin lPlugin = pluginDAO.findById(plugin.getPluginID()).get();

                        PluginInstance pluginInstance = new PluginInstance();
                        pluginInstance.setComponentNodeInstance(lambdaProxyLoadBalancer);
                        pluginInstance.setDateCreated(new Date());
                        pluginInstance.setLastModified(new Date());
                        pluginInstance.setPlugin(lPlugin);
                        pluginInstance.setName(lPlugin.getName());
                        pluginInstance.setModuleName(
                                null != lPlugin.getModuleName() && !lPlugin.getModuleName().isEmpty() ? lPlugin
                                        .getModuleName() : null);
                        pluginInstance.setImmutablePlugin(lPlugin.getImmutablePlugin());
                        pluginInstance.setDeletedPlugin(false);

                        pluginInstanceDAO.save(pluginInstance);
                        pluginInstances.add(pluginInstance);

                    } catch (Exception e) {
                        e.printStackTrace();
                        logger.log(Level.SEVERE, e.getMessage(), e);
                    }

                }

                lambdaProxyLoadBalancer.setPluginInstances(pluginInstances);

            } else {
                lambdaProxyLoadBalancer.setPluginInstances(null);
            }

        } else {

            logger.info("Lambda Proxy is not null, fetching existing one...");

            lambdaProxyLoadBalancer = lambdaProxyLoadBalancers.get(1L);

            exposedInterfaces = lambdaProxyLoadBalancer.getInterfaceInstances();
        }

        // Exposed interfaces of components
        if (null != componentNodeInstance.getInterfaceInstances() && !componentNodeInstance
                .getInterfaceInstances().isEmpty()) {

            for (InterfaceInstance interfaceInstance : componentNodeInstance.getInterfaceInstances()) {

                InterfaceInstance componentNodeInstanceExposedInterface = new InterfaceInstance();
                componentNodeInstanceExposedInterface.setPort(interfaceInstance.getPort());
                componentNodeInstanceExposedInterface.setName(interfaceInstance.getName());
                componentNodeInstanceExposedInterface.setInterfaceObj(interfaceInstance.getInterfaceObj());
                componentNodeInstanceExposedInterface.setComponentNodeInstance(lambdaProxyLoadBalancer);
                componentNodeInstanceExposedInterface.setInterfaceType(interfaceInstance.getInterfaceType());
                componentNodeInstanceExposedInterface.setLastModified(new Date());
                componentNodeInstanceExposedInterface.setDateCreated(new Date());
                interfaceInstanceDAO.save(componentNodeInstanceExposedInterface);
                exposedInterfaces.add(componentNodeInstanceExposedInterface);

                // Check type of interface
                if (interfaceInstance.getInterfaceObj().getInterfaceType()
                        .equals(Interface.InterfaceType.CORE.name())) {

                    if (!mapOfNeededChanges.containsKey(componentNodeInstance.getComponentNodeInstanceID())) {
                        mapOfNeededChanges
                                .put(componentNodeInstance.getComponentNodeInstanceID(), lambdaProxyLoadBalancer);
                    }

                }

            }

        }

        lambdaProxyLoadBalancer.setInterfaceInstances(exposedInterfaces);

        // TODO STEP5: Have to move constraints of Component Node Instance to Traefik Component Node Instance
        // TODO CHANGE NAME OF TRAEFIC UI
        InterfaceInstance uiInterfaceInstance = interfaceInstanceDAO
                .findByNameAndComponentNodeInstance("traefikUI", lambdaProxyLoadBalancer).get();

        if (null != applicationInstance.getConstraints() && !applicationInstance.getConstraints()
                .isEmpty()) {

            // TODO Fix filters
            ComponentNodeInstance finalLambdaProxyLoadBalancer = lambdaProxyLoadBalancer;
            applicationInstance.getConstraints().stream().filter(constraint ->
                    (null != constraint.getComponentNodeInstance() && constraint.getComponentNodeInstance()
                            .getComponentNodeInstanceID()
                            .equals(componentNodeInstance.getComponentNodeInstanceID()))
                            || (null != constraint.getInterfaceInstance() && constraint.getInterfaceInstance()
                            .getInterfaceInstanceID().equals(
                                    componentNodeInstance.getInterfaceInstances().get(0).getInterfaceInstanceID()))
                            || null != constraint.getGraphLinkNodeInstance()).forEach(constraint -> {

                // Found constraints that should be moved to LB

                if (null != constraint.getComponentNodeInstance()) {

                    // Component Constraint
                    Constraint componentConstraint = new Constraint();
                    componentConstraint.setComponentNodeInstance(finalLambdaProxyLoadBalancer);
                    componentConstraint.setConstraintCategory(constraint.getConstraintCategory());
                    componentConstraint.setConstraintType(constraint.getConstraintType());
                    componentConstraint.setConstraintMetric(constraint.getConstraintMetric());
                    componentConstraint.setDeletableConstraint(constraint.getDeletableConstraint());
                    componentConstraint.setConstraintValue(constraint.getConstraintValue());
                    componentConstraint.setConstraintUnit(constraint.getConstraintUnit());
                    componentConstraint.setCountry(null);
                    componentConstraint.setGraphLinkNodeInstance(null);
                    componentConstraint.setInterfaceInstance(null);
                    componentConstraint.setQi(null);
                    componentConstraint.setDateCreated(new Date());
                    componentConstraint.setLastModified(new Date());
                    componentConstraint.setApplicationInstance(applicationInstance);
                    constraintDAO.save(componentConstraint);

                } else if (null != constraint.getGraphLinkNodeInstance()) {

                    if (constraint.getGraphLinkNodeInstance().getComponentNodeInstanceTo()
                            .getComponentNodeInstanceID().equals(componentNodeInstance.getComponentNode())) {

                        // Graph Link Constraint
                        GraphLinkNodeInstance graphLinkNodeInstance = graphLinkNodeInstanceDAO
                                .findById(constraint.getGraphLinkNodeInstance().getGraphLinkNodeInstanceID()).get();
                        graphLinkNodeInstance.setComponentNodeInstanceTo(finalLambdaProxyLoadBalancer);
                        graphLinkNodeInstance.setLastModified(new Date());
                        graphLinkNodeInstanceDAO.save(graphLinkNodeInstance);

                        constraint.setGraphLinkNodeInstance(graphLinkNodeInstance);
                        constraint.setLastModified(new Date());
                        constraintDAO.save(constraint);

                    }

                } else {

                    // Access Constraint

                    InterfaceInstance interfaceInstance = interfaceInstanceDAO
                            .findAllByComponentNodeInstanceOrderByDateCreatedDesc(finalLambdaProxyLoadBalancer,
                                    null).getContent().stream().filter(
                                    iInstance -> iInstance.getInterfaceObj().getInterfaceID().equals(
                                            componentNodeInstance.getInterfaceInstances().get(0).getInterfaceObj()
                                                    .getInterfaceID())).collect(Collectors.toList()).get(0);

                    Constraint constraintAccessInterface = new Constraint();
                    constraintAccessInterface.setGraphLinkNodeInstance(null);
                    constraintAccessInterface.setInterfaceInstance(interfaceInstance);
                    constraintAccessInterface.setApplicationInstance(applicationInstance);
                    constraintAccessInterface.setComponentNodeInstance(null);
                    constraintAccessInterface.setConstraintCategory(constraint.getConstraintCategory());
                    constraintAccessInterface.setConstraintMetric(null);
                    constraintAccessInterface.setConstraintType(constraint.getConstraintType());
                    constraintAccessInterface.setConstraintValue(null);
                    constraintAccessInterface.setCountry(null);
                    constraintAccessInterface.setQi(constraint.getQi());
                    constraintAccessInterface.setRadioServiceType(constraint.getRadioServiceType());
                    constraintAccessInterface.setResourceType(constraint.getResourceType());
                    constraintAccessInterface.setAllocationRetentionPriorityProfile(
                            constraint.getAllocationRetentionPriorityProfile());
                    constraintAccessInterface
                            .setMinimumGuaranteedBandwidth(constraint.getMinimumGuaranteedBandwidth());
                    constraintAccessInterface
                            .setMaximumRequiredBandwidth(constraint.getMaximumRequiredBandwidth());
                    constraintAccessInterface.setDeletableConstraint(constraint.getDeletableConstraint());
                    constraintAccessInterface.setDateCreated(new Date());
                    constraintAccessInterface.setLastModified(new Date());
                    constraintDAO.save(constraintAccessInterface);

                }

            });

        }

        // Change the ACCESS interfaces of the components instances to CORE
        if (null != componentNodeInstance.getInterfaceInstances() && !componentNodeInstance
                .getInterfaceInstances().isEmpty()) {

            componentNodeInstance.getInterfaceInstances().stream().forEach(interfaceInstance -> {
                if (interfaceInstance.getInterfaceType().equals(Interface.InterfaceType.ACCESS.name())) {
                    interfaceInstance.setInterfaceType(Interface.InterfaceType.CORE.name());

                    interfaceInstanceDAO.save(interfaceInstance);
                }
            });
        }

        // STEP3: Check if we need more than one workers
        if (componentNodeInstance.getMinimumWorkers() > 1
                && componentNodeInstance.getMaximumWorkers() > 1) {

            // Calculate Workers
            int numOfWorkers = componentNodeInstance.getMinimumWorkers();

            for (int i = 1; i < numOfWorkers; i++) {

                // Replicate Component Node Instance
                ComponentNodeInstance workerI = new ComponentNodeInstance();
                workerI.setName(componentNodeInstance.getName() + "Worker" + i);
                workerI.setLoadBalancer(false);
                workerI.setLoadBalancedBy(lambdaProxyLoadBalancer);
                workerI.setHexID(Util.createRandomHEXString());
                workerI.setCommand(
                        null != componentNodeInstance.getCommand() && !componentNodeInstance.getCommand()
                                .isEmpty() ? componentNodeInstance.getCommand() : null);
                workerI.setSshKey(
                        null != componentNodeInstance.getSshKey() ? componentNodeInstance.getSshKey() : null);
                workerI.setDateCreated(new Date());
                workerI.setProvider(componentNodeInstance.getProvider());
                workerI.setLastModified(new Date());
                workerI.setComponentNode(componentNodeInstance.getComponentNode());
                workerI.setApplicationInstance(applicationInstance);
                workerI.setStatusIDS(
                        null != componentNodeInstance.getStatusIDS() && componentNodeInstance.getStatusIDS()
                                .booleanValue());
                workerI.setStatusIPS(
                        null != componentNodeInstance.getStatusIPS() && componentNodeInstance.getStatusIPS()
                                .booleanValue());

                workerI.setNetworkModeHost(
                        null != componentNodeInstance.getNetworkModeHost() && componentNodeInstance.getNetworkModeHost()
                                .booleanValue());
                workerI.setPrivilege(
                        null != componentNodeInstance.getPrivilege() && componentNodeInstance.getPrivilege()
                                .booleanValue());
                workerI.setHostname(
                        null != componentNodeInstance.getHostname() && !componentNodeInstance.getHostname()
                                .isEmpty() ? componentNodeInstance.getHostname() : null);
                workerI.setDnsEntry(
                        null != componentNodeInstance.getDnsEntry() && !componentNodeInstance.getDnsEntry()
                                .isEmpty() ? componentNodeInstance.getDnsEntry() : null);
                workerI.setSharedMemorySize(
                        null != componentNodeInstance.getSharedMemorySize() && !componentNodeInstance.getSharedMemorySize()
                                .isEmpty() ? componentNodeInstance.getSharedMemorySize() : null);

                if (null != componentNodeInstance.getCapabilityAdds() && !componentNodeInstance.getCapabilityAdds().isEmpty()) {
                    Collection<Component.CapabilityAdd> newCapabilities = new ArrayList<>();
                    Collection<Component.CapabilityAdd> exCapabilities = componentNodeInstance.getCapabilityAdds();
                    exCapabilities.stream().forEach(capability -> {
                        newCapabilities.add(capability);
                    });

                    workerI.setCapabilityAdds(newCapabilities);
                } else {
                    workerI.setCapabilityAdds(null);
                }
                if (null != componentNodeInstance.getCapabilityDrops() && !componentNodeInstance.getCapabilityDrops().isEmpty()) {
                    Collection<Component.CapabilityDrop> newCapabilities = new ArrayList<>();
                    Collection<Component.CapabilityDrop> exCapabilities = componentNodeInstance.getCapabilityDrops();
                    exCapabilities.stream().forEach(capability -> {
                        newCapabilities.add(capability);
                    });

                    workerI.setCapabilityDrops(newCapabilities);
                } else {
                    workerI.setCapabilityDrops(null);
                }

                workerI.setMaximumWorkers(componentNodeInstance.getMaximumWorkers());
                workerI.setMinimumWorkers(componentNodeInstance.getMinimumWorkers());
                componentNodeInstanceDAO.save(workerI);

                ComponentNodeInstanceStatus componentNodeInstanceStatusWorkerI = new ComponentNodeInstanceStatus();
                componentNodeInstanceStatusWorkerI.setReportedChange(
                        OrchestratorChangedStatusNotification.ChangeType.GraphStatusChange.name());
                componentNodeInstanceStatusWorkerI.setLastModified(new Date());
                componentNodeInstanceStatusWorkerI.setDateCreated(new Date());
                componentNodeInstanceStatusWorkerI.setComponentNodeInstance(workerI);
                componentNodeInstanceStatusWorkerI.setMessage("Component is loading...");
                componentNodeInstanceStatusWorkerI.setStatus("LOADING");
                componentNodeInstanceStatusWorkerI.setApplicationInstance(applicationInstance);
                componentNodeInstanceStatusDAO.save(componentNodeInstanceStatusWorkerI);

                Constraint minWorkersConstraint = new Constraint();
                minWorkersConstraint.setComponentNodeInstance(workerI);
                minWorkersConstraint
                        .setConstraintCategory(Constraint.ConstraintCategory.COMPONENT_HOSTING.name());
                minWorkersConstraint.setConstraintType(Constraint.ConstraintType.HARD.name());
                minWorkersConstraint.setConstraintMetric(Constraint.ConstraintMetric.MIN_WORKERS.name());
                minWorkersConstraint.setDeletableConstraint(false);
                minWorkersConstraint.setConstraintValue(workerI.getMinimumWorkers() + "");
                minWorkersConstraint.setConstraintUnit("amount");
                minWorkersConstraint.setCountry(null);
                minWorkersConstraint.setGraphLinkNodeInstance(null);
                minWorkersConstraint.setInterfaceInstance(null);
                minWorkersConstraint.setQi(null);
                minWorkersConstraint.setDateCreated(new Date());
                minWorkersConstraint.setLastModified(new Date());
                minWorkersConstraint.setApplicationInstance(applicationInstance);
                constraintDAO.save(minWorkersConstraint);

                Constraint maxWorkersConstraint = new Constraint();
                maxWorkersConstraint.setComponentNodeInstance(workerI);
                maxWorkersConstraint
                        .setConstraintCategory(Constraint.ConstraintCategory.COMPONENT_HOSTING.name());
                maxWorkersConstraint.setConstraintType(Constraint.ConstraintType.HARD.name());
                maxWorkersConstraint.setConstraintMetric(Constraint.ConstraintMetric.MAX_WORKERS.name());
                maxWorkersConstraint.setDeletableConstraint(false);
                maxWorkersConstraint.setConstraintValue(workerI.getMaximumWorkers() + "");
                maxWorkersConstraint.setConstraintUnit("amount");
                maxWorkersConstraint.setCountry(null);
                maxWorkersConstraint.setGraphLinkNodeInstance(null);
                maxWorkersConstraint.setInterfaceInstance(null);
                maxWorkersConstraint.setQi(null);
                maxWorkersConstraint.setDateCreated(new Date());
                maxWorkersConstraint.setLastModified(new Date());
                maxWorkersConstraint.setApplicationInstance(applicationInstance);
                constraintDAO.save(maxWorkersConstraint);

                // Check required interfaces
                List<GraphLinkNodeInstance> graphLinkNodeInstances = graphLinkNodeInstanceDAO
                        .findAllByApplicationInstanceAndComponentNodeInstanceFrom(applicationInstance,
                                componentNodeInstance);

                if (null != graphLinkNodeInstances && !graphLinkNodeInstances.isEmpty()) {

                    graphLinkNodeInstances.stream().forEach(graphLinkNodeInstance -> {

                        GraphLinkNodeInstance graphLNI = new GraphLinkNodeInstance();
                        graphLNI.setGraphLinkNode(graphLinkNodeInstance.getGraphLinkNode());
                        graphLNI.setApplicationInstance(applicationInstance);
                        graphLNI.setComponentNodeInstanceTo(graphLinkNodeInstance.getComponentNodeInstanceTo());
                        graphLNI.setComponentNodeInstanceFrom(workerI);
                        graphLNI.setDateCreated(new Date());
                        graphLNI.setLastModified(new Date());
                        graphLinkNodeInstanceDAO.save(graphLNI);


                    });
                }

                // Check interface instances
                if (null != componentNodeInstance.getInterfaceInstances() && !componentNodeInstance
                        .getInterfaceInstances().isEmpty()) {
                    List<InterfaceInstance> interfaceInstances = new ArrayList<>();

                    componentNodeInstance.getInterfaceInstances().stream().forEach(interfaceInstance -> {

                        try {

                            InterfaceInstance interfaceInstanceNew = new InterfaceInstance();
                            interfaceInstanceNew.setDateCreated(new Date());
                            interfaceInstanceNew.setLastModified(new Date());
                            interfaceInstanceNew.setComponentNodeInstance(workerI);
                            interfaceInstanceNew.setInterfaceObj(interfaceInstance.getInterfaceObj());
                            interfaceInstanceNew.setName(interfaceInstance.getName());
                            interfaceInstanceNew.setPort(interfaceInstance.getPort());
                            interfaceInstanceNew.setInterfaceType(interfaceInstance.getInterfaceType());
                            interfaceInstanceDAO.save(interfaceInstanceNew);
                            interfaceInstances.add(interfaceInstanceNew);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    });

                    workerI.setInterfaceInstances(interfaceInstances);

                } else {

                    workerI.setInterfaceInstances(null);

                }

                // Check environmental variables
                if (null != componentNodeInstance.getEnvironmentalVariableInstances()
                        && !componentNodeInstance.getEnvironmentalVariableInstances().isEmpty()) {
                    List<EnvironmentalVariableInstance> environmentalVariableInstances = new ArrayList<>();

                    componentNodeInstance.getEnvironmentalVariableInstances().stream()
                            .forEach(environmentalVariableInstance -> {

                                try {

                                    EnvironmentalVariableInstance environmentalVariableInstanceNew = new EnvironmentalVariableInstance();
                                    environmentalVariableInstanceNew
                                            .setValue(environmentalVariableInstance.getValue());
                                    environmentalVariableInstanceNew.setKey(environmentalVariableInstance.getKey());
                                    environmentalVariableInstanceNew.setLastModified(new Date());
                                    environmentalVariableInstanceNew.setDateCreated(new Date());
                                    environmentalVariableInstanceNew.setEnvironmentalVariable(
                                            environmentalVariableInstance.getEnvironmentalVariable());
                                    environmentalVariableInstanceNew.setComponentNodeInstance(workerI);
                                    environmentalVariableInstanceDAO.save(environmentalVariableInstanceNew);
                                    environmentalVariableInstances.add(environmentalVariableInstanceNew);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            });

                    workerI.setEnvironmentalVariableInstances(environmentalVariableInstances);
                } else {

                    workerI.setEnvironmentalVariableInstances(null);

                }

                // Check plugin instances
                if (null != componentNodeInstance.getPluginInstances() && !componentNodeInstance
                        .getPluginInstances().isEmpty()) {
                    List<PluginInstance> pluginInstances = new ArrayList<>();

                    componentNodeInstance.getPluginInstances().stream().forEach(pluginInstance -> {

                        try {

                            PluginInstance pluginInstanceNew = new PluginInstance();
                            pluginInstanceNew.setComponentNodeInstance(workerI);
                            pluginInstanceNew.setDateCreated(new Date());
                            pluginInstanceNew.setLastModified(new Date());
                            pluginInstanceNew.setPlugin(pluginInstance.getPlugin());
                            pluginInstanceNew.setName(pluginInstance.getName());
                            pluginInstanceNew.setModuleName(
                                    null != pluginInstance.getModuleName() && !pluginInstance.getModuleName()
                                            .isEmpty() ? pluginInstance.getModuleName() : null);
                            pluginInstanceNew.setImmutablePlugin(pluginInstance.getImmutablePlugin());
                            pluginInstanceNew.setDeletedPlugin(false);

                            pluginInstanceDAO.save(pluginInstanceNew);
                            pluginInstances.add(pluginInstanceNew);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    });

                    workerI.setPluginInstances(pluginInstances);
                } else {

                    workerI.setPluginInstances(null);

                }

                // Check id rule sets instances
                if (null != componentNodeInstance.getiDRuleSetInstances() && !componentNodeInstance
                        .getiDRuleSetInstances().isEmpty()) {
                    List<IDRuleSetInstance> idRuleSetInstances = new ArrayList<>();

                    componentNodeInstance.getiDRuleSetInstances().stream().forEach(idRuleSetInstance -> {

                        try {

                            IDRuleSetInstance idRuleSetInstanceNew = new IDRuleSetInstance();
                            idRuleSetInstanceNew.setComponentNodeInstance(workerI);
                            idRuleSetInstanceNew.setDateCreated(new Date());
                            idRuleSetInstanceNew.setLastModified(new Date());
                            idRuleSetInstanceNew.setIdRuleSet(idRuleSetInstance.getIdRuleSet());
                            idRuleSetInstanceNew.setName(idRuleSetInstance.getName());

                            idRuleSetInstanceDAO.save(idRuleSetInstanceNew);
                            idRuleSetInstances.add(idRuleSetInstanceNew);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    });

                    workerI.setiDRuleSetInstances(idRuleSetInstances);
                } else {

                    workerI.setiDRuleSetInstances(null);

                }

                // Check devices
                if (null != componentNodeInstance.getDeviceInstances() && !componentNodeInstance
                        .getDeviceInstances().isEmpty()) {
                    List<DeviceInstance> deviceInstances = new ArrayList<>();

                    componentNodeInstance.getDeviceInstances().stream().forEach(deviceInstance -> {

                        try {

                            DeviceInstance deviceInstanceNew = new DeviceInstance();
                            deviceInstanceNew.setValue(deviceInstance.getValue());
                            deviceInstanceNew.setKey(deviceInstance.getKey());
                            deviceInstanceNew.setLastModified(new Date());
                            deviceInstanceNew.setDateCreated(new Date());
                            deviceInstanceNew.setDevice(deviceInstance.getDevice());
                            deviceInstanceNew.setComponentNodeInstance(workerI);
                            deviceInstanceDAO.save(deviceInstanceNew);
                            deviceInstances.add(deviceInstanceNew);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    });

                    workerI.setDeviceInstances(deviceInstances);
                } else {

                    workerI.setDeviceInstances(null);

                }

                // Flavors
                if (flavorInstanceDAO.findByComponentNodeInstance(componentNodeInstance).isPresent()) {

                    FlavorInstance oldFlavorInstance = flavorInstanceDAO
                            .findByComponentNodeInstance(componentNodeInstance).get();

                    FlavorInstance flavorInstanceNew = new FlavorInstance();
                    flavorInstanceNew.setDateCreated(new Date());
                    flavorInstanceNew.setLastModified(new Date());
                    flavorInstanceNew.setRam(oldFlavorInstance.getRam());
                    flavorInstanceNew.setStorage(oldFlavorInstance.getStorage());
                    flavorInstanceNew.setvCPUs(oldFlavorInstance.getvCPUs());
                    flavorInstanceNew.setComponentNodeInstance(workerI);
                    flavorInstanceDAO.save(flavorInstanceNew);
                    workerI.setFlavorInstance(flavorInstanceNew);

                    Constraint cpuConstraint = new Constraint();
                    cpuConstraint.setComponentNodeInstance(workerI);
                    cpuConstraint
                            .setConstraintCategory(Constraint.ConstraintCategory.COMPONENT_HOSTING.name());
                    cpuConstraint.setConstraintType(Constraint.ConstraintType.HARD.name());
                    cpuConstraint.setConstraintMetric(Constraint.ConstraintMetric.MIN_V_CPU.name());
                    cpuConstraint.setDeletableConstraint(false);
                    cpuConstraint.setConstraintUnit("amount");
                    cpuConstraint.setConstraintValue(flavorInstanceNew.getvCPUs() + "");
                    cpuConstraint.setCountry(null);
                    cpuConstraint.setGraphLinkNodeInstance(null);
                    cpuConstraint.setInterfaceInstance(null);
                    cpuConstraint.setQi(null);
                    cpuConstraint.setDateCreated(new Date());
                    cpuConstraint.setLastModified(new Date());
                    cpuConstraint.setApplicationInstance(applicationInstance);
                    constraintDAO.save(cpuConstraint);

                    Constraint ramConstraint = new Constraint();
                    ramConstraint.setComponentNodeInstance(workerI);
                    ramConstraint
                            .setConstraintCategory(Constraint.ConstraintCategory.COMPONENT_HOSTING.name());
                    ramConstraint.setConstraintType(Constraint.ConstraintType.HARD.name());
                    ramConstraint.setConstraintMetric(Constraint.ConstraintMetric.MIN_RAM.name());
                    ramConstraint.setDeletableConstraint(false);
                    ramConstraint.setConstraintUnit("mb");
                    ramConstraint.setConstraintValue(flavorInstanceNew.getRam() + "");
                    ramConstraint.setCountry(null);
                    ramConstraint.setGraphLinkNodeInstance(null);
                    ramConstraint.setInterfaceInstance(null);
                    ramConstraint.setQi(null);
                    ramConstraint.setDateCreated(new Date());
                    ramConstraint.setLastModified(new Date());
                    ramConstraint.setApplicationInstance(applicationInstance);
                    constraintDAO.save(ramConstraint);

                    Constraint storageConstraint = new Constraint();
                    storageConstraint.setComponentNodeInstance(workerI);
                    storageConstraint
                            .setConstraintCategory(Constraint.ConstraintCategory.COMPONENT_HOSTING.name());
                    storageConstraint.setConstraintType(Constraint.ConstraintType.HARD.name());
                    storageConstraint.setConstraintMetric(Constraint.ConstraintMetric.MIN_STORAGE.name());
                    storageConstraint.setDeletableConstraint(false);
                    storageConstraint.setConstraintUnit("gb");
                    storageConstraint.setConstraintValue(flavorInstanceNew.getStorage() + "");
                    storageConstraint.setCountry(null);
                    storageConstraint.setGraphLinkNodeInstance(null);
                    storageConstraint.setInterfaceInstance(null);
                    storageConstraint.setQi(null);
                    storageConstraint.setDateCreated(new Date());
                    storageConstraint.setLastModified(new Date());
                    storageConstraint.setApplicationInstance(applicationInstance);
                    constraintDAO.save(storageConstraint);


                } else {
                    workerI.setFlavorInstance(null);
                }

                // Health Checks
                if (healthCheckInstanceDAO.findByComponentNodeInstance(componentNodeInstance).isPresent()) {

                    HealthCheckInstance oldHealthCheckInstance = healthCheckInstanceDAO
                            .findByComponentNodeInstance(componentNodeInstance).get();

                    HealthCheckInstance healthCheckInstanceNew = new HealthCheckInstance();
                    healthCheckInstanceNew.setHealthCheck(oldHealthCheckInstance.getHealthCheck());
                    healthCheckInstanceNew.setDateCreated(new Date());
                    healthCheckInstanceNew.setLastModified(new Date());

                    healthCheckInstanceNew.setInterval(oldHealthCheckInstance.getInterval());
                    healthCheckInstanceNew.setArgs(
                            null != oldHealthCheckInstance.getArgs() && !oldHealthCheckInstance.getArgs()
                                    .isEmpty() ? oldHealthCheckInstance.getArgs() : null);
                    healthCheckInstanceNew.setHttpURL(
                            null != oldHealthCheckInstance.getHttpURL() && !oldHealthCheckInstance.getHttpURL()
                                    .isEmpty() ? oldHealthCheckInstance.getHttpURL() : null);

                    healthCheckInstanceNew.setComponentNodeInstance(workerI);
                    healthCheckInstanceDAO.save(healthCheckInstanceNew);
                    workerI.setHealthCheckInstance(healthCheckInstanceNew);

                } else {
                    workerI.setHealthCheckInstance(null);
                }

                // Locations
                if (null != componentNodeInstance.getLocationInstances() && !componentNodeInstance
                        .getLocationInstances().isEmpty()) {
                    List<LocationInstance> locationInstances = new ArrayList<>();

                    componentNodeInstance.getLocationInstances().stream().forEach(locationInstance -> {

                        try {

                            LocationInstance locationInstanceNew = new LocationInstance();
                            locationInstanceNew.setRegion(locationInstance.getRegion());
                            locationInstanceNew.setComponentNodeInstance(workerI);
                            locationInstanceNew.setCountry(
                                    null != locationInstance.getCountry() ? locationInstance.getCountry() : null);
                            locationInstanceNew.setDateCreated(new Date());
                            locationInstanceNew.setLastModified(new Date());
                            locationInstanceDAO.save(locationInstanceNew);
                            locationInstances.add(locationInstanceNew);

                            Constraint regionConstraint = new Constraint();
                            regionConstraint.setComponentNodeInstance(workerI);
                            regionConstraint
                                    .setConstraintCategory(Constraint.ConstraintCategory.COMPONENT_HOSTING.name());
                            regionConstraint.setConstraintType(Constraint.ConstraintType.HARD.name());
                            regionConstraint.setConstraintMetric(Constraint.ConstraintMetric.REGION.name());
                            regionConstraint.setDeletableConstraint(false);
                            regionConstraint.setConstraintUnit("region");
                            regionConstraint.setConstraintValue(locationInstanceNew.getRegion() + "");
                            regionConstraint.setCountry(null);
                            regionConstraint.setGraphLinkNodeInstance(null);
                            regionConstraint.setInterfaceInstance(null);
                            regionConstraint.setQi(null);
                            regionConstraint.setDateCreated(new Date());
                            regionConstraint.setLastModified(new Date());
                            regionConstraint.setApplicationInstance(applicationInstance);
                            constraintDAO.save(regionConstraint);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    });

                    workerI.setLocationInstances(locationInstances);

                } else {

                    workerI.setLocationInstances(null);

                }

                // Volumes
                if (null != componentNodeInstance.getVolumeInstances() && !componentNodeInstance
                        .getVolumeInstances().isEmpty()) {
                    List<VolumeInstance> volumeInstances = new ArrayList<>();

                    componentNodeInstance.getVolumeInstances().stream().forEach(volumeInstance -> {

                        try {

                            VolumeInstance volumeInstanceNew = new VolumeInstance();
                            volumeInstanceNew.setDockerPath(volumeInstance.getDockerPath());
                            volumeInstanceNew.setHostPath(volumeInstance.getHostPath());
                            volumeInstanceNew.setVolume(volumeInstance.getVolume());
                            volumeInstanceNew.setLastModified(new Date());
                            volumeInstanceNew.setDateCreated(new Date());
                            volumeInstanceNew.setComponentNodeInstance(workerI);
                            volumeInstanceDAO.save(volumeInstanceNew);
                            volumeInstances.add(volumeInstanceNew);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    });

                    workerI.setVolumeInstances(volumeInstances);

                } else {

                    workerI.setVolumeInstances(null);

                }

                componentNodeInstanceDAO.save(workerI);

//                ApplicationInstance existingApplicationInstance = applicationInstanceDAO.findById(applicationInstance.getId()).get();
//                existingApplicationInstance.getComponentNodeInstances().add(workerI);
//                existingApplicationInstance.setLastModified(new Date());
//                applicationInstanceDAO.save(existingApplicationInstance);

                workers.put(workerI.getComponentNodeInstanceID(), workerI);

                // TODO Convert workers to orchestrator component node instances;
                OrchestratorComponentNodeInstance workerOrchestratorCNI = new OrchestratorComponentNodeInstance();
                workerOrchestratorCNI.setComponentNodeInstanceHexID(workerI.getHexID() + "");
                workerOrchestratorCNI.setComponentNodeInstanceID(workerI.getComponentNodeInstanceID() + "");
                workerOrchestratorCNI.setComponentNodeInstanceName(workerI.getName());
                workerOrchestratorCNI.setComponentNodeHexID(workerI.getComponentNode().getHexID() + "");
                workerOrchestratorCNI
                        .setComponentNodeID(workerI.getComponentNode().getComponentNodeID() + "");
                workerOrchestratorCNI.setComponentNodeName(workerI.getComponentNode().getName());
                workerOrchestratorCNI.setProviderID(vimID);
                workerOrchestratorCNI.setMinimumWorkers(workerI.getMinimumWorkers());
                workerOrchestratorCNI.setMaximumWorkers(workerI.getMaximumWorkers());
                workerOrchestratorCNI.setStatusIDS(
                        null != workerI.getStatusIDS() && workerI.getStatusIDS().booleanValue());
                workerOrchestratorCNI.setStatusIPS(
                        null != workerI.getStatusIPS() && workerI.getStatusIPS().booleanValue());

                workerOrchestratorCNI.setNetworkModeHost(
                        null != workerI.getNetworkModeHost() && workerI.getNetworkModeHost()
                                .booleanValue());
                workerOrchestratorCNI.setPrivilege(
                        null != workerI.getPrivilege() && workerI.getPrivilege()
                                .booleanValue());
                workerOrchestratorCNI.setHostname(
                        null != workerI.getHostname() && !workerI.getHostname()
                                .isEmpty() ? workerI.getHostname() : null);
                workerOrchestratorCNI.setDnsEntry(
                        null != workerI.getDnsEntry() && !workerI.getDnsEntry()
                                .isEmpty() ? workerI.getDnsEntry() : null);
                workerOrchestratorCNI.setSharedMemorySize(
                        null != workerI.getSharedMemorySize() && !workerI.getSharedMemorySize()
                                .isEmpty() ? workerI.getSharedMemorySize() : null);
                workerOrchestratorCNI.setDockerUsername(
                        null != workerI.getComponentNode().getComponent().getDockerUsername()
                                && !workerI.getComponentNode().getComponent().getDockerUsername()
                                .isEmpty() ? workerI.getComponentNode().getComponent().getDockerUsername() : null);
                workerOrchestratorCNI.setDockerPassword(
                        null != workerI.getComponentNode().getComponent().getDockerPassword()
                                && !workerI.getComponentNode().getComponent().getDockerPassword()
                                .isEmpty() ? workerI.getComponentNode().getComponent().getDockerPassword() : null);

                if (null != workerI.getCapabilityAdds() && !workerI.getCapabilityAdds().isEmpty()) {
                    Collection<String> newCapabilities = new ArrayList<>();
                    Collection<Component.CapabilityAdd> exCapabilities = workerI.getCapabilityAdds();
                    exCapabilities.stream().forEach(capability -> {
                        newCapabilities.add(capability.getFriendlyName());
                    });

                    workerOrchestratorCNI.setCapabilityAdds(newCapabilities);
                } else {
                    workerOrchestratorCNI.setCapabilityAdds(null);
                }
                if (null != workerI.getCapabilityDrops() && !workerI.getCapabilityDrops().isEmpty()) {
                    Collection<String> newCapabilities = new ArrayList<>();
                    Collection<Component.CapabilityDrop> exCapabilities = workerI.getCapabilityDrops();
                    exCapabilities.stream().forEach(capability -> {
                        newCapabilities.add(capability.getFriendlyName());
                    });

                    workerOrchestratorCNI.setCapabilityDrops(newCapabilities);
                } else {
                    workerOrchestratorCNI.setCapabilityDrops(null);
                }

                workerOrchestratorCNI.setCommand(
                        null != workerI.getCommand() && !workerI.getCommand().isEmpty() ? Arrays
                                .asList(workerI.getCommand()) : null);

                workerOrchestratorCNI.setImage(workerI.getComponentNode().getComponent().getDockerImage());
                workerOrchestratorCNI
                        .setRegistry(workerI.getComponentNode().getComponent().getDockerRegistry());

                if (null != workerI.getSshKey()) {
                    workerOrchestratorCNI.setSshKey(workerI.getSshKey().getSshKey());
                } else {
                    workerOrchestratorCNI.setSshKey(null);
                }

                // Scaling
                OrchestratorElasticity orchestratorElasticity = new OrchestratorElasticity();
                orchestratorElasticity.setProfile(workerI.getComponentNode().getComponent().getElasticityController());
                orchestratorElasticity
                        .setType(workerI.getComponentNode().getComponent().getElasticityControllerMode());
                workerOrchestratorCNI.setMonitoringElasticity(orchestratorElasticity);

                // Required Interfaces
                List<GraphLinkNodeInstance> graphLinkNodeInstancesOfWorkerI = graphLinkNodeInstanceDAO
                        .findAllByApplicationInstanceAndComponentNodeInstanceFrom(applicationInstance, workerI);

                if (null != graphLinkNodeInstancesOfWorkerI && !graphLinkNodeInstancesOfWorkerI.isEmpty()) {

                    List<OrchestratorDependency> dependsOn = new ArrayList<>();

                    graphLinkNodeInstancesOfWorkerI.stream()
                            .filter(graphLinkNodeInstance -> graphLinkNodeInstance.getComponentNodeInstanceFrom()
                                    .getComponentNodeInstanceID().equals(workerI.getComponentNodeInstanceID()))
                            .forEach(graphLinkNodeInstance -> {

                                try {

                                    if (dependsOn.stream().filter(dep -> dep.getDependency()
                                                    .equals(graphLinkNodeInstance.getComponentNodeInstanceTo().getHexID()))
                                            .collect(Collectors.toList()).isEmpty()) {

                                        OrchestratorDependency orchestratorDependency = new OrchestratorDependency();
                                        orchestratorDependency.setDependency(
                                                graphLinkNodeInstance.getComponentNodeInstanceTo().getHexID());
                                        orchestratorDependency
                                                .setNetworkAttachmentPoint(workerI.getProvider().getNetworkID());
                                        orchestratorDependency.setVna(
                                                graphLinkNodeInstance.getGraphLinkNode().getGraphLink().getInterfaceObj()
                                                        .getVna());

                                        // TODO CHECK
                                        dependsOn.add(orchestratorDependency);

                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();

                                }

                            });

                    workerOrchestratorCNI.setDependsOn(dependsOn);

                }

                // Exposed Interfaces
                if (null != workerI.getInterfaceInstances() && !workerI.getInterfaceInstances().isEmpty()) {

                    List<OrchestratorPort> ports = new ArrayList<>();

                    workerI.getInterfaceInstances().stream().forEach(interfaceInstance -> {

                        try {

                            OrchestratorPort orchestratorPort = new OrchestratorPort();
                            orchestratorPort.setTarget(interfaceInstance.getInterfaceObj().getPort());
                            orchestratorPort.setPublished(interfaceInstance.getPort());
                            orchestratorPort
                                    .setProtocol(interfaceInstance.getInterfaceObj().getTransmissionProtocol());
                            orchestratorPort.setVna(interfaceInstance.getInterfaceObj().getVna());
                            orchestratorPort.setNetworkAttachmentPoint(workerI.getProvider().getNetworkID());
                            orchestratorPort.setType(interfaceInstance.getInterfaceType());
                            ports.add(orchestratorPort);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    });

                    workerOrchestratorCNI.setPorts(ports);

                }

                if (null != workerI.getEnvironmentalVariableInstances() && !workerI
                        .getEnvironmentalVariableInstances().isEmpty()) {

                    // Environmental Variables
                    Map<String, String> orchestratorEnvironmentalVariables = new HashMap<>();
                    workerI.getEnvironmentalVariableInstances().stream()
                            .forEach(environmentalVariableInstance -> {

//                        if (environmentalVariableInstance.getValue().startsWith("@")) {
//
//                            // TODO CHECK
//                            // Find @ComponentNodeName in order to replace it with @ComponentNodeInstanceName
//                            ComponentNode componentNode = componentNodeDAO.findByName(environmentalVariableInstance.getValue().substring(1)).get();
//                            List<ComponentNodeInstance> cniList = existingApplicationInstance.getComponentNodeInstances().stream().filter(cNI -> cNI.getComponentNode().getComponentNodeID().equals(componentNode.getComponentNodeID())).collect(Collectors.toList());
//
//                            if (null != componentNode && null != cniList && !cniList.isEmpty()) {
//
//                                String componentNodeInstanceName = cniList.get(0).getName();
//
//                                orchestratorEnvironmentalVariables.put(environmentalVariableInstance.getKey(), "@" + componentNodeInstanceName);
//
//                            }
//
//                        } else {

                                try {

                                    orchestratorEnvironmentalVariables.put(environmentalVariableInstance.getKey(),
                                            environmentalVariableInstance.getValue());

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

//                        }

                            });

                    workerOrchestratorCNI.setEnvironmentalVariables(orchestratorEnvironmentalVariables);

                }

                if (null != workerI.getPluginInstances() && !workerI.getPluginInstances().isEmpty()) {

                    List<OrchestratorPlugin> orchestratorPlugins = new ArrayList<>();

                    workerI.getPluginInstances().stream().forEach(pluginInstance -> {

                        Plugin plugin = pluginDAO.findById(pluginInstance.getPlugin().getPluginID()).get();

                        OrchestratorPlugin orchestratorPlugin = new OrchestratorPlugin();
                        orchestratorPlugin.setId(pluginInstance.getPluginInstanceID() + "");
                        orchestratorPlugin.setName(plugin.getName());
                        orchestratorPlugin.setModuleName(
                                null != plugin.getModuleName() && !plugin.getModuleName().isEmpty() ? plugin
                                        .getModuleName() : null);
                        orchestratorPlugin.setDefaultPlugin(plugin.getDefaultPlugin().booleanValue());
                        orchestratorPlugin.setImmutablePlugin(plugin.getImmutablePlugin().booleanValue());
                        orchestratorPlugin.setDownloadURL(
                                null != plugin.getDownloadURL() && !plugin.getDownloadURL().isEmpty() ? plugin
                                        .getDownloadURL() : null);
                        orchestratorPlugin.setPluginType(
                                null != plugin.getPluginType() && !plugin.getPluginType().isEmpty()
                                        ? Plugin.PluginType.valueOf(plugin.getPluginType()).name() : null);
                        orchestratorPlugin.setPort(
                                null != plugin.getPort() && !plugin.getPort().isEmpty() ? plugin.getPort() : null);
                        orchestratorPlugin.setEndpoint(
                                null != plugin.getEndpoint() && !plugin.getEndpoint().isEmpty() ? plugin
                                        .getEndpoint() : null);
                        orchestratorPlugin.setDisabledPlugin(pluginInstance.getDeletedPlugin().booleanValue());
                        orchestratorPlugins.add(orchestratorPlugin);

                    });

                    workerOrchestratorCNI.setPlugins(orchestratorPlugins);

                }

                if (null != workerI.getDeviceInstances() && !workerI.getDeviceInstances().isEmpty()) {

                    // Device Instances
                    Map<String, String> orchestratorDevices = new HashMap<>();
                    workerI.getDeviceInstances().stream().forEach(deviceInstance -> {

                        try {

                            orchestratorDevices.put(deviceInstance.getKey(), deviceInstance.getValue());

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    });

                    workerOrchestratorCNI.setDevices(orchestratorDevices);

                }

                if (null != workerI.getVolumeInstances() && !workerI.getVolumeInstances().isEmpty()) {

                    // Volumes Instances
                    Map<String, String> orchestratorVolumes = new HashMap<>();
                    workerI.getVolumeInstances().stream().filter(volumeInstance -> !volumeInstance.getHostPath().isEmpty()).forEach(volumeInstance -> {

                        try {

                            orchestratorVolumes.put(volumeInstance.getHostPath(), volumeInstance.getDockerPath());

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    });

                    workerOrchestratorCNI.setVolumes(orchestratorVolumes);

                }

                if (null != workerI.getFlavorInstance()) {

                    OrchestratorFlavor orchestratorFlavor = new OrchestratorFlavor();
                    orchestratorFlavor.setRam(workerI.getFlavorInstance().getRam());
                    orchestratorFlavor.setvCPUs(workerI.getFlavorInstance().getvCPUs());
                    orchestratorFlavor.setStorage(workerI.getFlavorInstance().getStorage());
                    workerOrchestratorCNI.setFlavor(orchestratorFlavor);

                }

                if (null != workerI.getHealthCheckInstance()) {

                    OrchestratorHealthCheck orchestratorHealthCheck = new OrchestratorHealthCheck();

                    orchestratorHealthCheck.setInterval(workerI.getHealthCheckInstance().getInterval().toString());
                    orchestratorHealthCheck.setArgs(
                            null != workerI.getHealthCheckInstance().getArgs() && !workerI
                                    .getHealthCheckInstance().getArgs().isEmpty() ? workerI.getHealthCheckInstance()
                                    .getArgs() : null);
                    orchestratorHealthCheck.setHttpURL(
                            null != workerI.getHealthCheckInstance().getHttpURL() && !workerI
                                    .getHealthCheckInstance().getHttpURL().isEmpty() ? workerI
                                    .getHealthCheckInstance().getHttpURL() : null);

                    workerOrchestratorCNI.setHealthCheck(orchestratorHealthCheck);

                }

                if (null != workerI.getLocationInstances() && !workerI.getLocationInstances().isEmpty()) {

                    List<OrchestratorLocation> locations = new ArrayList<>();

                    workerI.getLocationInstances().stream().forEach(locationInstance -> {

                        try {

                            OrchestratorLocation orchestratorLocation = new OrchestratorLocation();
                            orchestratorLocation.setRegion(locationInstance.getRegion());
                            orchestratorLocation.setCountry(null);
                            locations.add(orchestratorLocation);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    });

                    workerOrchestratorCNI.setLocations(locations);
                }

                workerOrchestratorCNI.setLoadBalancer(false);
                workerOrchestratorCNI.setLambdaProxy(false);

                // TODO

                services.put(workerOrchestratorCNI.getComponentNodeInstanceID(), workerOrchestratorCNI);

            }
        }

        // STEP3b: Update also component node instance that it is load balanced.
        componentNodeInstance.setLoadBalancedBy(lambdaProxyLoadBalancer);
        componentNodeInstanceDAO.save(componentNodeInstance);

        // STEP3c: Create GraphLinkNodeInstances between LB and Workers
        if (!workers.isEmpty()) {

            workers.entrySet().stream().forEach(entry -> {

                ComponentNodeInstance worker = entry.getValue();
                ComponentNodeInstance loadBalancer = worker.getLoadBalancedBy();

                GraphLinkNodeInstance graphLinkNodeInstance = new GraphLinkNodeInstance();
                graphLinkNodeInstance.setComponentNodeInstanceFrom(loadBalancer);
                graphLinkNodeInstance.setComponentNodeInstanceTo(worker);
                graphLinkNodeInstance.setDateCreated(new Date());
                graphLinkNodeInstance.setLastModified(new Date());
                graphLinkNodeInstance.setApplicationInstance(applicationInstance);
                graphLinkNodeInstance.setGraphLinkNode(null);
                graphLinkNodeInstanceDAO.save(graphLinkNodeInstance);

            });

        }

        OrchestratorComponentNodeInstance loadBalancer = null;

        if (null == lambdaProxyOrchestratorLoadBalancers || lambdaProxyOrchestratorLoadBalancers
                .isEmpty()) {

            // STEP4: Convert Traefik Component Node Instance to Orchestrator Component Node Instance
            loadBalancer = new OrchestratorComponentNodeInstance();
            loadBalancer.setComponentNodeInstanceHexID("" + lambdaProxyLoadBalancer.getHexID());
            loadBalancer
                    .setComponentNodeInstanceID("" + lambdaProxyLoadBalancer.getComponentNodeInstanceID());
            loadBalancer.setComponentNodeInstanceName(lambdaProxyLoadBalancer.getName());
            loadBalancer
                    .setComponentNodeHexID("" + lambdaProxyLoadBalancer.getComponentNode().getHexID());
            loadBalancer
                    .setComponentNodeID("" + lambdaProxyLoadBalancer.getComponentNode().getComponentNodeID());
            loadBalancer.setComponentNodeName(lambdaProxyLoadBalancer.getComponentNode().getName());
            loadBalancer.setProviderID(vimID);

            OrchestratorElasticity orchestratorElasticity = new OrchestratorElasticity();
            orchestratorElasticity
                    .setProfile(lambdaProxyLoadBalancer.getComponentNode().getComponent().getElasticityController());
            loadBalancer.setMonitoringElasticity(orchestratorElasticity);

            List<String> commands = new ArrayList<>();
            for (String subCommand : lambdaProxyLoadBalancer.getCommand().split("\\,")) {

                if (!commands.contains(subCommand)) {
                    commands.add(subCommand);
                }
            }

            loadBalancer.setCommand(commands);

            loadBalancer
                    .setImage(lambdaProxyLoadBalancer.getComponentNode().getComponent().getDockerImage());
            loadBalancer.setRegistry(
                    lambdaProxyLoadBalancer.getComponentNode().getComponent().getDockerRegistry());
            loadBalancer.setDockerUsername(lambdaProxyLoadBalancer.getComponentNode().getComponent().getDockerUsername());
            loadBalancer.setDockerPassword(lambdaProxyLoadBalancer.getComponentNode().getComponent().getDockerPassword());
            loadBalancer.setLoadBalancer(false);
            loadBalancer.setLambdaProxy(true);

            if (null != lambdaProxyLoadBalancer.getSshKey()) {
                loadBalancer.setSshKey(lambdaProxyLoadBalancer.getSshKey().getSshKey());
            } else {
                loadBalancer.setSshKey(null);
            }

            // TODO What if initial component has more than one exposed interfaces ????

            OrchestratorPort orchestratorPortA = new OrchestratorPort();
            orchestratorPortA.setProtocol(Interface.TransmissionProtocol.TCP.name());
            orchestratorPortA.setTarget(uiInterfaceInstance.getPort());
            orchestratorPortA.setPublished(uiInterfaceInstance.getPort());
            orchestratorPortA.setVna(uiInterfaceInstance.getInterfaceObj().getVna());
            orchestratorPortA
                    .setNetworkAttachmentPoint(componentNodeInstance.getProvider().getNetworkID());
            orchestratorPortA.setType(uiInterfaceInstance.getInterfaceObj().getInterfaceType());
            loadBalancer.setPorts(Arrays.asList(orchestratorPortA));

            if (null != lambdaProxyLoadBalancer.getFlavorInstance()) {

                OrchestratorFlavor orchestratorFlavor = new OrchestratorFlavor();
                orchestratorFlavor.setRam(lambdaProxyLoadBalancer.getFlavorInstance().getRam());
                orchestratorFlavor.setvCPUs(lambdaProxyLoadBalancer.getFlavorInstance().getvCPUs());
                orchestratorFlavor.setStorage(lambdaProxyLoadBalancer.getFlavorInstance().getStorage());
                loadBalancer.setFlavor(orchestratorFlavor);

            }

        } else {
            loadBalancer = lambdaProxyOrchestratorLoadBalancers.get(1L);
        }

        if (null != componentNodeInstance.getInterfaceInstances() && !componentNodeInstance
                .getInterfaceInstances().isEmpty()) {

            List<OrchestratorPort> ports = null;

            if (null != loadBalancer.getPorts() && !loadBalancer.getPorts().isEmpty()) {
                ports = new ArrayList<>();
                ports.addAll(loadBalancer.getPorts());
            } else {
                ports = new ArrayList<>();
            }

            for (InterfaceInstance interfaceInstance : componentNodeInstance.getInterfaceInstances()) {

                OrchestratorPort orchestratorPort = new OrchestratorPort();
                orchestratorPort.setProtocol(interfaceInstance.getInterfaceObj().getTransmissionProtocol());
                orchestratorPort.setTarget(interfaceInstance.getPort());
                orchestratorPort.setPublished(interfaceInstance.getPort());
                orchestratorPort
                        .setNetworkAttachmentPoint(componentNodeInstance.getProvider().getNetworkID());
                orchestratorPort.setVna(interfaceInstance.getInterfaceObj().getVna());
                orchestratorPort.setType(interfaceInstance.getInterfaceObj().getInterfaceType());
                ports.add(orchestratorPort);

            }

            loadBalancer.setPorts(ports);

        }

        if (null != loadBalancer.getDependsOn() && !loadBalancer.getDependsOn().isEmpty()) {

            List<OrchestratorDependency> orchestratorDependencies = new ArrayList<>();

            loadBalancer.getDependsOn().stream().forEach(orchestratorDependency -> {

                if (orchestratorDependencies.stream()
                        .filter(od -> od.getDependency().equals(orchestratorDependency.getDependency()))
                        .collect(Collectors.toList()).isEmpty()) {

                    orchestratorDependencies.add(orchestratorDependency);

                }

            });

            if (orchestratorDependencies.stream().filter(
                            od -> od.getDependency().equals(componentNodeInstance.getComponentNode().getHexID()))
                    .collect(Collectors.toList()).isEmpty()) {

                orchestratorDependencies.add(
                        new OrchestratorDependency(componentNodeInstance.getComponentNode().getHexID(),
                                componentNodeInstance.getProvider().getNetworkID(),
                                componentNodeInstance.getInterfaceInstances().get(0).getInterfaceObj().getVna()));
            }

            loadBalancer.setDependsOn(orchestratorDependencies);

        } else {

            loadBalancer.setDependsOn(Arrays.asList(
                    new OrchestratorDependency(componentNodeInstance.getComponentNode().getHexID(),
                            componentNodeInstance.getProvider().getNetworkID(),
                            componentNodeInstance.getInterfaceInstances().get(0).getInterfaceObj().getVna())));

        }

        if (null != lambdaProxyLoadBalancer.getFlavorInstance()) {

            OrchestratorFlavor orchestratorFlavor = new OrchestratorFlavor();
            orchestratorFlavor.setRam(lambdaProxyLoadBalancer.getFlavorInstance().getRam());
            orchestratorFlavor.setvCPUs(lambdaProxyLoadBalancer.getFlavorInstance().getvCPUs());
            orchestratorFlavor.setStorage(lambdaProxyLoadBalancer.getFlavorInstance().getStorage());
            loadBalancer.setFlavor(orchestratorFlavor);

        }

        if (null != lambdaProxyLoadBalancer.getHealthCheckInstance()) {

            OrchestratorHealthCheck orchestratorHealthCheck = new OrchestratorHealthCheck();
            orchestratorHealthCheck
                    .setInterval(lambdaProxyLoadBalancer.getHealthCheckInstance().getInterval().toString());
            orchestratorHealthCheck.setArgs(
                    null != lambdaProxyLoadBalancer.getHealthCheckInstance().getArgs()
                            && !lambdaProxyLoadBalancer.getHealthCheckInstance().getArgs().isEmpty()
                            ? lambdaProxyLoadBalancer.getHealthCheckInstance().getArgs() : null);
            orchestratorHealthCheck.setHttpURL(
                    null != lambdaProxyLoadBalancer.getHealthCheckInstance().getHttpURL()
                            && !lambdaProxyLoadBalancer.getHealthCheckInstance().getHttpURL().isEmpty()
                            ? lambdaProxyLoadBalancer.getHealthCheckInstance().getHttpURL() : null);
            loadBalancer.setHealthCheck(orchestratorHealthCheck);

        }

        if (null != lambdaProxyLoadBalancer.getPluginInstances() && !lambdaProxyLoadBalancer
                .getPluginInstances().isEmpty()) {

            List<OrchestratorPlugin> orchestratorPlugins = new ArrayList<>();

            lambdaProxyLoadBalancer.getPluginInstances().stream().forEach(pluginInstance -> {

                Plugin plugin = pluginDAO.findById(pluginInstance.getPlugin().getPluginID()).get();

                OrchestratorPlugin orchestratorPlugin = new OrchestratorPlugin();
                orchestratorPlugin.setId(pluginInstance.getPluginInstanceID() + "");
                orchestratorPlugin.setName(plugin.getName());
                orchestratorPlugin.setModuleName(
                        null != plugin.getModuleName() && !plugin.getModuleName().isEmpty() ? plugin
                                .getModuleName() : null);
                orchestratorPlugin.setDefaultPlugin(plugin.getDefaultPlugin().booleanValue());
                orchestratorPlugin.setImmutablePlugin(plugin.getImmutablePlugin().booleanValue());
                orchestratorPlugin.setDownloadURL(
                        null != plugin.getDownloadURL() && !plugin.getDownloadURL().isEmpty() ? plugin
                                .getDownloadURL() : null);
                orchestratorPlugin.setPluginType(
                        null != plugin.getPluginType() && !plugin.getPluginType().isEmpty() ? Plugin.PluginType
                                .valueOf(plugin.getPluginType()).name() : null);
                orchestratorPlugin.setPort(
                        null != plugin.getPort() && !plugin.getPort().isEmpty() ? plugin.getPort() : null);
                orchestratorPlugin.setEndpoint(
                        null != plugin.getEndpoint() && !plugin.getEndpoint().isEmpty() ? plugin.getEndpoint()
                                : null);
                orchestratorPlugin.setDisabledPlugin(pluginInstance.getDeletedPlugin().booleanValue());
                orchestratorPlugins.add(orchestratorPlugin);

            });

            loadBalancer.setPlugins(orchestratorPlugins);

        }

        // Synchronize Hashmaps
        if (lambdaProxyOrchestratorLoadBalancers.containsKey(1L)) {
            lambdaProxyOrchestratorLoadBalancers.remove(1L);
            services.remove(loadBalancer.getComponentNodeInstanceID());
        }

        services.put(loadBalancer.getComponentNodeInstanceID(), loadBalancer);
        lambdaProxyOrchestratorLoadBalancers.put(1L, loadBalancer);

        lambdaProxyLoadBalancers.remove(1L);

        lambdaProxyLoadBalancers.put(1L, lambdaProxyLoadBalancer);

        return lambdaProxyLoadBalancer.getHexID();

    }
}
