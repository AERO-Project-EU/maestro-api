package eu.orchestrator.elasticity.spi.service.backend;

import eu.orchestrator.elasticity.spi.model.backend.ElasticityObjects;
import eu.orchestrator.elasticity.spi.util.Util;
import eu.orchestrator.repository.domain.ComponentNodeInstance.SecurityEnablers;
import eu.orchestrator.transfer.entities.orchestrator.*;
import eu.orchestrator.repository.dao.*;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import eu.orchestrator.repository.domain.*;
@org.springframework.stereotype.Service
@Transactional
public class ComponentService {

    private static final Logger logger = Logger.getLogger(ElasticityControllerService.class.getName());

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    ComponentNodeInstanceDAO componentNodeInstanceDAO;

    @Autowired
    ComponentNodeInstanceHashDAO componentNodeInstanceHashDAO;

    @Autowired
    ComponentNodeInstanceStatusDAO componentNodeInstanceStatusDAO;

    @Autowired
    FlavorInstanceDAO flavorInstanceDAO;

    @Autowired
    HealthCheckInstanceDAO healthCheckInstanceDAO;

    @Autowired
    PluginDAO pluginDAO;

    @Autowired
    PluginInstanceDAO pluginInstanceDAO;

    @Autowired
    InterfaceInstanceDAO interfaceInstanceDAO;

    @Autowired
    ConstraintDAO constraintDAO;

    @Autowired
    GraphLinkNodeInstanceDAO graphLinkNodeInstanceDAO;

    @Autowired
    EnvironmentalVariableInstanceDAO environmentalVariableInstanceDAO;

    @Autowired
    IDRuleSetInstanceDAO idRuleSetInstanceDAO;

    @Autowired
    DeviceInstanceDAO deviceInstanceDAO;

    @Autowired
    LocationInstanceDAO locationInstanceDAO;

    @Autowired
    VolumeInstanceDAO volumeInstanceDAO;

    public Plugin findPluginByName(String name) {
        Optional<Plugin> pluginOp = pluginDAO.findByName(name);
        if(pluginOp!=null && pluginOp.isPresent()){
            Plugin plugin = pluginOp.get();
            return plugin;
        }
        return null;
    }

    public Boolean checkElasticityControllerInstance(String name, String suffix, ElasticityObjects elasticityObjects){
        Optional<ComponentNodeInstance> elasticityControllerInstance = componentNodeInstanceDAO.
                findByNameAndApplicationInstance(name + suffix, elasticityObjects.getApplicationInstance());

        if(!elasticityControllerInstance.isPresent()){
            return false;
        }

        //ADD ComponentNodeInstance to elasticityObjects
        elasticityObjects.setElasticityController(elasticityControllerInstance.get());
        return true;
    }


    public ComponentNodeInstance replicateWorkerInstance(ApplicationInstance applicationInstance, ComponentNodeInstance balancerInstance,
                                                         ComponentNodeInstance originalWorker, String name, Boolean initStatus){

        ComponentNodeInstance replicatedWorker = new ComponentNodeInstance();
        replicatedWorker.setName(name);
        replicatedWorker.setLoadBalancer(false);
        replicatedWorker.setLoadBalancedBy(balancerInstance);
        replicatedWorker.setHexID(Util.createRandomHEXString(entityManager));
        replicatedWorker.setCommand(
                null != originalWorker.getCommand() && !originalWorker.getCommand()
                        .isEmpty() ? originalWorker.getCommand() : null);
        replicatedWorker.setSshKey(
                null != originalWorker.getSshKey() ? originalWorker.getSshKey() : null);
        replicatedWorker.setDateCreated(new Date());
        //TODO change in the future where the replicated worker will be deployed
        replicatedWorker.setProvider(originalWorker.getProvider());
        replicatedWorker.setLastModified(new Date());
        replicatedWorker.setComponentNode(originalWorker.getComponentNode());
        replicatedWorker.setApplicationInstance(applicationInstance);
        replicatedWorker.setStatusIDS(
                null != originalWorker.getStatusIDS() ? originalWorker.getStatusIDS()
                        .booleanValue() : false);
        replicatedWorker.setStatusIPS(
                null != originalWorker.getStatusIPS() ? originalWorker.getStatusIPS()
                        .booleanValue() : false);
        replicatedWorker.setStatusSOC(
                null != originalWorker.getStatusSOC() ? originalWorker.getStatusSOC()
                        .booleanValue() : false);
        replicatedWorker.setNetworkModeHost(
                null != originalWorker.getNetworkModeHost() ? originalWorker.getNetworkModeHost()
                        .booleanValue() : false);
        replicatedWorker.setPrivilege(
                null != originalWorker.getPrivilege() ? originalWorker.getPrivilege()
                        .booleanValue() : false);
        replicatedWorker.setHostname(
                null != originalWorker.getHostname() && !originalWorker.getHostname()
                        .isEmpty() ? originalWorker.getHostname() : null);
        replicatedWorker.setDnsEntry(
                null != originalWorker.getDnsEntry() && !originalWorker.getDnsEntry()
                        .isEmpty() ? originalWorker.getDnsEntry() : null);
        replicatedWorker.setSharedMemorySize(
                null != originalWorker.getSharedMemorySize() && !originalWorker.getSharedMemorySize()
                        .isEmpty() ? originalWorker.getSharedMemorySize() : null);

        if(null!= originalWorker.getCapabilityAdds() && !originalWorker.getCapabilityAdds().isEmpty()){
            Collection<Component.CapabilityAdd> newCapabilities = new ArrayList<>();
            Collection<Component.CapabilityAdd> exCapabilities = originalWorker.getCapabilityAdds();
            exCapabilities.stream().forEach(capability ->{
                newCapabilities.add(capability);
            });

            replicatedWorker.setCapabilityAdds(newCapabilities);
        }else{
            replicatedWorker.setCapabilityAdds(null);
        }
        if(null!= originalWorker.getCapabilityDrops() && !originalWorker.getCapabilityDrops().isEmpty()){
            Collection<Component.CapabilityDrop> newCapabilities = new ArrayList<>();
            Collection<Component.CapabilityDrop> exCapabilities = originalWorker.getCapabilityDrops();
            exCapabilities.stream().forEach(capability ->{
                newCapabilities.add(capability);
            });

            replicatedWorker.setCapabilityDrops(newCapabilities);
        }else{
            replicatedWorker.setCapabilityDrops(null);
        }

        //TODO for astrid
        if(null!= originalWorker.getSecurityEnablers() && !originalWorker.getSecurityEnablers().isEmpty()){
            Collection<ComponentNodeInstance.SecurityEnablers> newEnablers = new ArrayList<>();
            Collection<ComponentNodeInstance.SecurityEnablers> exEnablers = originalWorker.getSecurityEnablers();
            exEnablers.stream().forEach(capability ->{
                newEnablers.add(capability);
            });

            replicatedWorker.setSecurityEnablers(newEnablers);
        }else{
            replicatedWorker.setSecurityEnablers(null);
        }

        replicatedWorker.setMaximumWorkers(originalWorker.getMaximumWorkers());
        replicatedWorker.setMinimumWorkers(originalWorker.getMinimumWorkers());
        ComponentNodeInstance replicatedWorkerDB =  componentNodeInstanceDAO.save(replicatedWorker);

        for (ComponentNodeInstanceHash componentNodeInstanceHash : originalWorker.getComponentNodeInstanceHashList()){

            //TODO astrid logic re-calculate hash ?
            ComponentNodeInstanceHash componentNodeInstanceHashNew = new ComponentNodeInstanceHash();
            componentNodeInstanceHashNew.setType(componentNodeInstanceHash.getType());
            componentNodeInstanceHashNew.setValue(componentNodeInstanceHash.getValue());
            componentNodeInstanceHashNew.setDateCreated(new Date());
            componentNodeInstanceHashNew.setLastModified(new Date());
            componentNodeInstanceHashNew.setComponentNodeInstance(replicatedWorkerDB);
            componentNodeInstanceHashDAO.save(componentNodeInstanceHashNew);
        }

        //Check if we want to initialise the status
        if(initStatus) {
            ComponentNodeInstanceStatus componentNodeInstanceStatusWorkerI = new ComponentNodeInstanceStatus();
            componentNodeInstanceStatusWorkerI.setReportedChange(
                    OrchestratorChangedStatusNotification.ChangeType.GraphStatusChange.name());
            componentNodeInstanceStatusWorkerI.setLastModified(new Date());
            componentNodeInstanceStatusWorkerI.setDateCreated(new Date());
            componentNodeInstanceStatusWorkerI.setComponentNodeInstance(replicatedWorker);
            componentNodeInstanceStatusWorkerI.setMessage("Component is loading...");
            componentNodeInstanceStatusWorkerI.setStatus("LOADING");
            componentNodeInstanceStatusWorkerI.setApplicationInstance(applicationInstance);
            componentNodeInstanceStatusDAO.save(componentNodeInstanceStatusWorkerI);
        }

        Constraint minWorkersConstraint = new Constraint();
        minWorkersConstraint.setComponentNodeInstance(replicatedWorker);
        minWorkersConstraint
                .setConstraintCategory(Constraint.ConstraintCategory.COMPONENT_HOSTING.name());
        minWorkersConstraint.setConstraintType(Constraint.ConstraintType.HARD.name());
        minWorkersConstraint.setConstraintMetric(Constraint.ConstraintMetric.MIN_WORKERS.name());
        minWorkersConstraint.setDeletableConstraint(false);
        minWorkersConstraint.setConstraintValue(replicatedWorker.getMinimumWorkers() + "");
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
        maxWorkersConstraint.setComponentNodeInstance(replicatedWorker);
        maxWorkersConstraint
                .setConstraintCategory(Constraint.ConstraintCategory.COMPONENT_HOSTING.name());
        maxWorkersConstraint.setConstraintType(Constraint.ConstraintType.HARD.name());
        maxWorkersConstraint.setConstraintMetric(Constraint.ConstraintMetric.MAX_WORKERS.name());
        maxWorkersConstraint.setDeletableConstraint(false);
        maxWorkersConstraint.setConstraintValue(replicatedWorker.getMaximumWorkers() + "");
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
                .findAllByApplicationInstanceAndComponentNodeInstanceFrom(applicationInstance, originalWorker);

        if (null != graphLinkNodeInstances && !graphLinkNodeInstances.isEmpty()) {

            graphLinkNodeInstances.stream().forEach(graphLinkNodeInstance -> {

                GraphLinkNodeInstance graphLNI = new GraphLinkNodeInstance();
                graphLNI.setGraphLinkNode(graphLinkNodeInstance.getGraphLinkNode());
                graphLNI.setApplicationInstance(applicationInstance);
                graphLNI.setComponentNodeInstanceTo(graphLinkNodeInstance.getComponentNodeInstanceTo());
                graphLNI.setComponentNodeInstanceFrom(replicatedWorker);
                graphLNI.setDateCreated(new Date());
                graphLNI.setLastModified(new Date());
                graphLinkNodeInstanceDAO.save(graphLNI);

            });
        }

        // Check interface instances
        if (null != originalWorker.getInterfaceInstances() && !originalWorker.getInterfaceInstances().isEmpty()) {
            List<InterfaceInstance> interfaceInstances = new ArrayList<>();

            originalWorker.getInterfaceInstances().stream().forEach(interfaceInstance -> {

                try {

                    InterfaceInstance interfaceInstanceNew = new InterfaceInstance();
                    interfaceInstanceNew.setDateCreated(new Date());
                    interfaceInstanceNew.setLastModified(new Date());
                    interfaceInstanceNew.setComponentNodeInstance(replicatedWorker);
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

            replicatedWorker.setInterfaceInstances(interfaceInstances);
        } else {

            replicatedWorker.setInterfaceInstances(null);
        }

        // Check environmental variables
        if (null != originalWorker.getEnvironmentalVariableInstances()
                && !originalWorker.getEnvironmentalVariableInstances().isEmpty()) {
            List<EnvironmentalVariableInstance> environmentalVariableInstances = new ArrayList<>();

            originalWorker.getEnvironmentalVariableInstances().stream()
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
                            environmentalVariableInstanceNew.setComponentNodeInstance(replicatedWorker);
                            environmentalVariableInstanceDAO.save(environmentalVariableInstanceNew);
                            environmentalVariableInstances.add(environmentalVariableInstanceNew);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    });

            replicatedWorker.setEnvironmentalVariableInstances(environmentalVariableInstances);
        } else {

            replicatedWorker.setEnvironmentalVariableInstances(null);
        }

        // Check plugin instances
        if (null != originalWorker.getPluginInstances() && !originalWorker.getPluginInstances().isEmpty()) {
            List<PluginInstance> pluginInstances = new ArrayList<>();

            originalWorker.getPluginInstances().stream().forEach(pluginInstance -> {

                try {

                    PluginInstance pluginInstanceNew = new PluginInstance();
                    pluginInstanceNew.setComponentNodeInstance(replicatedWorker);
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

            replicatedWorker.setPluginInstances(pluginInstances);
        } else {

            replicatedWorker.setPluginInstances(null);
        }

        if (null != originalWorker.getiDRuleSetInstances() && !originalWorker.getiDRuleSetInstances().isEmpty()) {
            List<IDRuleSetInstance> idRuleSetInstances = new ArrayList<>();

            originalWorker.getiDRuleSetInstances().stream().forEach(idRuleSetInstance -> {

                try {

                    IDRuleSetInstance idRuleSetInstanceNew = new IDRuleSetInstance();
                    idRuleSetInstanceNew.setComponentNodeInstance(replicatedWorker);
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

            replicatedWorker.setiDRuleSetInstances(idRuleSetInstances);
        } else {

            replicatedWorker.setiDRuleSetInstances(null);
        }

        // Check devices
        if (null != originalWorker.getDeviceInstances() && !originalWorker.getDeviceInstances().isEmpty()) {
            List<DeviceInstance> deviceInstances = new ArrayList<>();

            originalWorker.getDeviceInstances().stream().forEach(deviceInstance -> {

                try {

                    DeviceInstance deviceInstanceNew = new DeviceInstance();
                    deviceInstanceNew.setValue(deviceInstance.getValue());
                    deviceInstanceNew.setKey(deviceInstance.getKey());
                    deviceInstanceNew.setLastModified(new Date());
                    deviceInstanceNew.setDateCreated(new Date());
                    deviceInstanceNew.setDevice(deviceInstance.getDevice());
                    deviceInstanceNew.setComponentNodeInstance(replicatedWorker);
                    deviceInstanceDAO.save(deviceInstanceNew);
                    deviceInstances.add(deviceInstanceNew);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            });

            replicatedWorker.setDeviceInstances(deviceInstances);
        } else {

            replicatedWorker.setDeviceInstances(null);
        }

        // Flavors
        if (flavorInstanceDAO.findByComponentNodeInstance(originalWorker).isPresent()) {

            FlavorInstance oldFlavorInstance = flavorInstanceDAO.findByComponentNodeInstance(originalWorker).get();

            FlavorInstance flavorInstanceNew = new FlavorInstance();
            flavorInstanceNew.setDateCreated(new Date());
            flavorInstanceNew.setLastModified(new Date());
            flavorInstanceNew.setRam(oldFlavorInstance.getRam());
            flavorInstanceNew.setStorage(oldFlavorInstance.getStorage());
            flavorInstanceNew.setvCPUs(oldFlavorInstance.getvCPUs());
            flavorInstanceNew.setComponentNodeInstance(replicatedWorker);
            flavorInstanceDAO.save(flavorInstanceNew);
            replicatedWorker.setFlavorInstance(flavorInstanceNew);

            Constraint cpuConstraint = new Constraint();
            cpuConstraint.setComponentNodeInstance(replicatedWorker);
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
            ramConstraint.setComponentNodeInstance(replicatedWorker);
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
            storageConstraint.setComponentNodeInstance(replicatedWorker);
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
            replicatedWorker.setFlavorInstance(null);
        }

        // Health Checks
        if (healthCheckInstanceDAO.findByComponentNodeInstance(originalWorker).isPresent()) {

            HealthCheckInstance oldHealthCheckInstance = healthCheckInstanceDAO
                    .findByComponentNodeInstance(originalWorker).get();

            HealthCheckInstance healthCheckInstanceNew = new HealthCheckInstance();
            healthCheckInstanceNew.setHealthCheck(oldHealthCheckInstance.getHealthCheck());
            healthCheckInstanceNew.setDateCreated(new Date());
            healthCheckInstanceNew.setLastModified(new Date());
            healthCheckInstanceNew.setName(oldHealthCheckInstance.getName());

            healthCheckInstanceNew.setInterval(oldHealthCheckInstance.getInterval());
            healthCheckInstanceNew.setArgs(
                    null != oldHealthCheckInstance.getArgs() && !oldHealthCheckInstance.getArgs()
                            .isEmpty() ? oldHealthCheckInstance.getArgs() : null);
            healthCheckInstanceNew.setHttpURL(
                    null != oldHealthCheckInstance.getHttpURL() && !oldHealthCheckInstance.getHttpURL()
                            .isEmpty() ? oldHealthCheckInstance.getHttpURL() : null);

            healthCheckInstanceNew.setComponentNodeInstance(replicatedWorker);
            healthCheckInstanceDAO.save(healthCheckInstanceNew);
            replicatedWorker.setHealthCheckInstance(healthCheckInstanceNew);

        } else {
            replicatedWorker.setHealthCheckInstance(null);
        }

        // Locations
        if (null != originalWorker.getLocationInstances() && !originalWorker.getLocationInstances().isEmpty()) {
            List<LocationInstance> locationInstances = new ArrayList<>();

            originalWorker.getLocationInstances().stream().forEach(locationInstance -> {

                try {

                    LocationInstance locationInstanceNew = new LocationInstance();
                    locationInstanceNew.setRegion(locationInstance.getRegion());
                    locationInstanceNew.setComponentNodeInstance(replicatedWorker);
                    locationInstanceNew.setCountry(
                            null != locationInstance.getCountry() ? locationInstance.getCountry() : null);
                    locationInstanceNew.setDateCreated(new Date());
                    locationInstanceNew.setLastModified(new Date());
                    locationInstanceDAO.save(locationInstanceNew);
                    locationInstances.add(locationInstanceNew);

                    Constraint regionConstraint = new Constraint();
                    regionConstraint.setComponentNodeInstance(replicatedWorker);
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

            replicatedWorker.setLocationInstances(locationInstances);

        } else {

            replicatedWorker.setLocationInstances(null);
        }


        // TODO rethink when the volumes are added
        // Volumes
        if (null != originalWorker.getVolumeInstances() && !originalWorker.getVolumeInstances().isEmpty()) {
            List<VolumeInstance> volumeInstances = new ArrayList<>();

            originalWorker.getVolumeInstances().stream().forEach(volumeInstance -> {

                try {


                    VolumeInstance volumeInstanceNew = new VolumeInstance();
                    volumeInstanceNew.setDockerPath(volumeInstance.getDockerPath());
                    volumeInstanceNew.setHostPath(volumeInstance.getHostPath());
                    volumeInstanceNew.setVolume(volumeInstance.getVolume());
                    volumeInstanceNew.setLastModified(new Date());
                    volumeInstanceNew.setDateCreated(new Date());
                    volumeInstanceNew.setComponentNodeInstance(replicatedWorker);
                    volumeInstanceDAO.save(volumeInstanceNew);
                    volumeInstances.add(volumeInstanceNew);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            });

            replicatedWorker.setVolumeInstances(volumeInstances);

        } else {

            replicatedWorker.setVolumeInstances(null);
        }

        componentNodeInstanceDAO.save(replicatedWorker);


        return replicatedWorker;
    }

    public OrchestratorComponentNodeInstance translateWorkerInstance(ApplicationInstance applicationInstance, ComponentNodeInstance componentNodeINstance, String providerID){

        OrchestratorComponentNodeInstance orchestratorComponentNodeInstance = new OrchestratorComponentNodeInstance();
        orchestratorComponentNodeInstance.setComponentNodeInstanceHexID(componentNodeINstance.getHexID());
        orchestratorComponentNodeInstance.setComponentNodeInstanceID(componentNodeINstance.getComponentNodeInstanceID() + "");
        orchestratorComponentNodeInstance.setComponentNodeInstanceName(componentNodeINstance.getName());
        orchestratorComponentNodeInstance
                .setComponentNodeID(componentNodeINstance.getComponentNode().getComponentNodeID() + "");
        orchestratorComponentNodeInstance.setComponentNodeHexID(componentNodeINstance.getComponentNode().getHexID());
        orchestratorComponentNodeInstance.setComponentNodeName(componentNodeINstance.getComponentNode().getName());
        // TODO think the providerID for the added workers
        orchestratorComponentNodeInstance.setProviderID(providerID);
        orchestratorComponentNodeInstance.setMinimumWorkers(componentNodeINstance.getMinimumWorkers());
        orchestratorComponentNodeInstance.setMaximumWorkers(componentNodeINstance.getMaximumWorkers());
        orchestratorComponentNodeInstance.setStatusIPS(
                null != componentNodeINstance.getStatusIPS() ? componentNodeINstance.getStatusIPS().booleanValue() : false);
        orchestratorComponentNodeInstance.setStatusIDS(
                null != componentNodeINstance.getStatusIDS() ? componentNodeINstance.getStatusIDS().booleanValue() : false);
        orchestratorComponentNodeInstance.setStatusSoc(
                null != componentNodeINstance.getStatusSOC() ? componentNodeINstance.getStatusSOC().booleanValue() : false);

        orchestratorComponentNodeInstance.setNetworkModeHost(
                null != componentNodeINstance.getNetworkModeHost() ? componentNodeINstance.getNetworkModeHost()
                        .booleanValue() : false);
        orchestratorComponentNodeInstance.setPrivilege(
                null != componentNodeINstance.getPrivilege() ? componentNodeINstance.getPrivilege()
                        .booleanValue() : false);
        orchestratorComponentNodeInstance.setHostname(
                null != componentNodeINstance.getHostname() && !componentNodeINstance.getHostname()
                        .isEmpty() ? componentNodeINstance.getHostname() : null);
        orchestratorComponentNodeInstance.setDnsEntry(
                null != componentNodeINstance.getDnsEntry() && !componentNodeINstance.getDnsEntry()
                        .isEmpty() ? componentNodeINstance.getDnsEntry() : null);
        orchestratorComponentNodeInstance.setSharedMemorySize(
                null != componentNodeINstance.getSharedMemorySize() && !componentNodeINstance.getSharedMemorySize()
                        .isEmpty() ? componentNodeINstance.getSharedMemorySize() : null);
        orchestratorComponentNodeInstance.setDockerUsername(
                null != componentNodeINstance.getComponentNode().getComponent().getDockerUsername()
                        && !componentNodeINstance.getComponentNode().getComponent().getDockerUsername()
                        .isEmpty() ? componentNodeINstance.getComponentNode().getComponent().getDockerUsername() : null);
        orchestratorComponentNodeInstance.setDockerPassword(
                null != componentNodeINstance.getComponentNode().getComponent().getDockerPassword()
                        && !componentNodeINstance.getComponentNode().getComponent().getDockerPassword()
                        .isEmpty() ? componentNodeINstance.getComponentNode().getComponent().getDockerPassword() : null);

        if(null != componentNodeINstance.getLoadBalancedBy()){
            orchestratorComponentNodeInstance.setBalancedByComponentNodeHexID(componentNodeINstance.getLoadBalancedBy().getComponentNode().getHexID());
        }

        orchestratorComponentNodeInstance.setUlimitMemlockSoft(componentNodeINstance.getComponentNode().getComponent().getUlimitMemlockSoft());
        orchestratorComponentNodeInstance.setUlimitMemlockHard(componentNodeINstance.getComponentNode().getComponent().getUlimitMemlockHard());
        orchestratorComponentNodeInstance.setDockerExecutionUser(componentNodeINstance.getComponentNode().getComponent().getDockerExecutionUser());

        if(null!= componentNodeINstance.getCapabilityAdds() && !componentNodeINstance.getCapabilityAdds().isEmpty()){
            Collection<String> newCapabilities = new ArrayList<>();
            Collection<Component.CapabilityAdd> exCapabilities = componentNodeINstance.getCapabilityAdds();
            exCapabilities.stream().forEach(capability ->{
                newCapabilities.add(capability.getFriendlyName());
            });

            orchestratorComponentNodeInstance.setCapabilityAdds(newCapabilities);
        }else{
            orchestratorComponentNodeInstance.setCapabilityAdds(null);
        }
        if(null!= componentNodeINstance.getCapabilityDrops() && !componentNodeINstance.getCapabilityDrops().isEmpty()){
            Collection<String> newCapabilities = new ArrayList<>();
            Collection<Component.CapabilityDrop> exCapabilities = componentNodeINstance.getCapabilityDrops();
            exCapabilities.stream().forEach(capability ->{
                newCapabilities.add(capability.getFriendlyName());
            });

            orchestratorComponentNodeInstance.setCapabilityDrops(newCapabilities);
        }else{
            orchestratorComponentNodeInstance.setCapabilityDrops(null);
        }


        orchestratorComponentNodeInstance.setCommand(
                null != componentNodeINstance.getCommand() && !componentNodeINstance.getCommand().isEmpty() ? Arrays
                        .asList(componentNodeINstance.getCommand()) : null);

        orchestratorComponentNodeInstance.setImage(componentNodeINstance.getComponentNode().getComponent().getDockerImage());
        orchestratorComponentNodeInstance
                .setRegistry(componentNodeINstance.getComponentNode().getComponent().getDockerRegistry());

        if (null != componentNodeINstance.getSshKey()) {
            orchestratorComponentNodeInstance.setSshKey(componentNodeINstance.getSshKey().getSshKey());
        } else {
            orchestratorComponentNodeInstance.setSshKey(null);
        }

        //TODO Astrid security enablers
        if(componentNodeINstance.getSecurityEnablers()!=null && !componentNodeINstance.getSecurityEnablers().isEmpty()) {
            orchestratorComponentNodeInstance.setHasEnableSecurity(true);
            if (componentNodeINstance.getSecurityEnablers().contains(SecurityEnablers.CONFIGURATION_INTEGRITY_VERIFICATION) ||
                    componentNodeINstance.getSecurityEnablers().contains(SecurityEnablers.RUNTIME_FILE_INTEGRITY)){
                orchestratorComponentNodeInstance.setProduceHashes(true);
            }else{
                orchestratorComponentNodeInstance.setProduceHashes(false);
            }
        }else{
            orchestratorComponentNodeInstance.setHasEnableSecurity(false);
        }
        
        // Scaling
        OrchestratorElasticity orchestratorElasticity = new OrchestratorElasticity();
        orchestratorElasticity.setProfile(componentNodeINstance.getComponentNode().getComponent().getElasticityController());
        orchestratorElasticity
                .setType(componentNodeINstance.getComponentNode().getComponent().getElasticityControllerMode());
        orchestratorComponentNodeInstance.setMonitoringElasticity(orchestratorElasticity);

        // Required Interfaces
        List<GraphLinkNodeInstance> graphLinkNodeInstancesOfWorkerI = graphLinkNodeInstanceDAO
                .findAllByApplicationInstanceAndComponentNodeInstanceFrom(applicationInstance, componentNodeINstance);

        if (null != graphLinkNodeInstancesOfWorkerI && !graphLinkNodeInstancesOfWorkerI.isEmpty()) {

            List<OrchestratorDependency> dependsOn = new ArrayList<>();

            graphLinkNodeInstancesOfWorkerI.stream()
                    .filter(graphLinkNodeInstance -> graphLinkNodeInstance.getComponentNodeInstanceFrom()
                            .getComponentNodeInstanceID().equals(componentNodeINstance.getComponentNodeInstanceID()))
                    .forEach(graphLinkNodeInstance -> {

                        try {

                            if (dependsOn.stream().filter(dep -> dep.getDependency()
                                    .equals(graphLinkNodeInstance.getComponentNodeInstanceTo().getHexID()))
                                    .collect(Collectors.toList()).isEmpty()) {

                                OrchestratorDependency orchestratorDependency = new OrchestratorDependency();
                                orchestratorDependency.setDependency(
                                        graphLinkNodeInstance.getComponentNodeInstanceTo().getHexID());
                                orchestratorDependency
                                        .setNetworkAttachmentPoint(componentNodeINstance.getProvider().getNetworkID());
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

            orchestratorComponentNodeInstance.setDependsOn(dependsOn);

        }

        // Exposed Interfaces
        if (null != componentNodeINstance.getInterfaceInstances() && !componentNodeINstance.getInterfaceInstances().isEmpty()) {

            List<OrchestratorPort> ports = new ArrayList<>();

            componentNodeINstance.getInterfaceInstances().stream().forEach(interfaceInstance -> {

                OrchestratorPort orchestratorPort = new OrchestratorPort();
                orchestratorPort.setTarget(interfaceInstance.getInterfaceObj().getPort());
                orchestratorPort.setPublished(interfaceInstance.getPort());
                orchestratorPort
                        .setProtocol(interfaceInstance.getInterfaceObj().getTransmissionProtocol());
                orchestratorPort.setVna(interfaceInstance.getInterfaceObj().getVna());
                orchestratorPort.setType(interfaceInstance.getInterfaceType());
                orchestratorPort.setNetworkAttachmentPoint(componentNodeINstance.getProvider().getNetworkID());
                ports.add(orchestratorPort);

            });

            orchestratorComponentNodeInstance.setPorts(ports);

        }

        if (null != componentNodeINstance.getEnvironmentalVariableInstances() && !componentNodeINstance
                .getEnvironmentalVariableInstances().isEmpty()) {

            // Environmental Variables
            Map<String, String> orchestratorEnvironmentalVariables = new HashMap<>();
            componentNodeINstance.getEnvironmentalVariableInstances().stream()
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

            orchestratorComponentNodeInstance.setEnvironmentalVariables(orchestratorEnvironmentalVariables);

        }

        if (null != componentNodeINstance.getPluginInstances() && !componentNodeINstance.getPluginInstances().isEmpty()) {

            List<OrchestratorPlugin> orchestratorPlugins = new ArrayList<>();

            componentNodeINstance.getPluginInstances().stream().forEach(pluginInstance -> {

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

            orchestratorComponentNodeInstance.setPlugins(orchestratorPlugins);

        }

        if (null != componentNodeINstance.getDeviceInstances() && !componentNodeINstance.getDeviceInstances().isEmpty()) {

            // Devices
            Map<String, String> orchestratorDevices = new HashMap<>();
            componentNodeINstance.getDeviceInstances().stream().forEach(deviceInstance -> {

                orchestratorDevices.put(deviceInstance.getKey(), deviceInstance.getValue());

            });

            orchestratorComponentNodeInstance.setDevices(orchestratorDevices);
        }

        if (null != componentNodeINstance.getFlavorInstance()) {
            OrchestratorFlavor orchestratorFlavor = new OrchestratorFlavor();
            orchestratorFlavor.setRam(componentNodeINstance.getFlavorInstance().getRam());
            orchestratorFlavor.setvCPUs(componentNodeINstance.getFlavorInstance().getvCPUs());
            orchestratorFlavor.setStorage(componentNodeINstance.getFlavorInstance().getStorage());
            orchestratorComponentNodeInstance.setFlavor(orchestratorFlavor);
        }

        if (null != componentNodeINstance.getHealthCheckInstance()) {

            OrchestratorHealthCheck orchestratorHealthCheck = new OrchestratorHealthCheck();

            orchestratorHealthCheck.setInterval(componentNodeINstance.getHealthCheckInstance().getInterval().toString());
            orchestratorHealthCheck.setArgs(
                    null != componentNodeINstance.getHealthCheckInstance().getArgs() && !componentNodeINstance
                            .getHealthCheckInstance().getArgs().isEmpty() ? componentNodeINstance.getHealthCheckInstance()
                            .getArgs() : null);
            orchestratorHealthCheck.setHttpURL(
                    null != componentNodeINstance.getHealthCheckInstance().getHttpURL() && !componentNodeINstance
                            .getHealthCheckInstance().getHttpURL().isEmpty() ? componentNodeINstance
                            .getHealthCheckInstance().getHttpURL() : null);

            orchestratorComponentNodeInstance.setHealthCheck(orchestratorHealthCheck);
        }

        if (null != componentNodeINstance.getLocationInstances() && !componentNodeINstance.getLocationInstances().isEmpty()) {

            List<OrchestratorLocation> locations = new ArrayList<>();

            componentNodeINstance.getLocationInstances().stream().forEach(locationInstance -> {

                OrchestratorLocation orchestratorLocation = new OrchestratorLocation();
                orchestratorLocation.setRegion(locationInstance.getRegion());
                orchestratorLocation.setCountry(null);
                locations.add(orchestratorLocation);

            });

            orchestratorComponentNodeInstance.setLocations(locations);
        }

        orchestratorComponentNodeInstance.setLoadBalancer(false);
        orchestratorComponentNodeInstance.setLambdaProxy(false);

        return orchestratorComponentNodeInstance;
    }
}
