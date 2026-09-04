package eu.orchestrator.elasticity.spi.service.backend;

import eu.orchestrator.elasticity.spi.model.backend.ScalingObjects;
import eu.orchestrator.transfer.entities.orchestrator.*;
import eu.orchestrator.repository.dao.*;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import eu.orchestrator.repository.domain.*;
@org.springframework.stereotype.Service
@Transactional
public class ScalingService {

    private static final Logger logger = Logger.getLogger(ScalingService.class.getName());


    @Autowired
    ApplicationInstanceDAO applicationInstanceDAO;

    @Autowired
    ApplicationDAO applicationDAO;

    @Autowired
    ComponentNodeInstanceDAO componentNodeInstanceDAO;

    @Autowired
    ComponentNodeInstanceHashDAO componentNodeInstanceHashDAO;

    @Autowired
    SecurityConfigurationResultDAO securityConfigurationResultDAO;

    @Autowired
    ComponentNodeDAO componentNodeDAO;

    @Autowired
    ComponentDAO componentDAO;

    @Autowired
    ConstraintDAO constraintDAO;

    @Autowired
    RequirementDAO requirementDAO;

    @Autowired
    InterfaceInstanceDAO interfaceInstanceDAO;

    @Autowired
    EnvironmentalVariableInstanceDAO environmentalVariableInstanceDAO;

    @Autowired
    FlavorInstanceDAO flavorInstanceDAO;

    @Autowired
    LocationInstanceDAO locationInstanceDAO;

    @Autowired
    VolumeInstanceDAO volumeInstanceDAO;

    @Autowired
    ComponentNodeInstanceStatusDAO componentNodeInstanceStatusDAO;

    @Autowired
    ComponentNodeInstanceAlertDAO componentNodeInstanceAlertDAO;

    @Autowired
    PluginInstanceDAO pluginInstanceDAO;

    @Autowired
    PluginDAO pluginDAO;

    @Autowired
    IDRuleSetDAO idRuleSetDAO;

    @Autowired
    IDRuleSetInstanceDAO idRuleSetInstanceDAO;

    @Autowired
    RuntimePolicyDAO runtimePolicyDAO;

    @Autowired
    GraphLinkNodeInstanceDAO graphLinkNodeInstanceDAO;

    @Autowired
    HealthCheckInstanceDAO healthCheckInstanceDAO;

    @Autowired
    DeviceInstanceDAO deviceInstanceDAO;

    @Autowired
    SliceDAO sliceDAO;

    @Autowired
    SlicePlacementDAO slicePlacementDAO;

    @Autowired
    SlicePlacementAttachmentPointDAO slicePlacementAttachmentPointDAO;

    @Autowired
    GraphLinkNodeDAO graphLinkNodeDAO;

    @Autowired
    GraphLinkDAO graphLinkDAO;

    @Autowired
    InterfaceDAO interfaceDAO;

    @Autowired
    ElasticityHistoryDAO elasticityHistoryDAO;

    @Autowired
    EntityManager entityManager;

    @Autowired
    ComponentService componentService;

    public ScalingObjects createWorkers(ScalingObjects scalingRequest){
        int numberOfWorkers = scalingRequest.getNumberOfWorkers();
        ApplicationInstance applicationInstance = scalingRequest.getApplicationInstance();
        ComponentNodeInstance componentNodeInstance = scalingRequest.getComponentNodeInstanceWorker();

        List<ComponentNodeInstance> componentNodeInstanceList = new ArrayList<>();
        List<OrchestratorComponentNodeInstance> services = new ArrayList<>();

        // Check number of existing workers
        int existingWorkers = componentNodeInstanceDAO
                .findAllByLoadBalancedByAndLoadBalancedByIsNotNull(
                        componentNodeInstance.getLoadBalancedBy()).size();
        scalingRequest.setExistingWorkers(existingWorkers);

        logger.info("Existing Workers: " + existingWorkers);
        logger.info("Number of Workers: " + numberOfWorkers);

        for (int i = existingWorkers;
             i < existingWorkers + numberOfWorkers; i++) {


            boolean nameExists = true;

            String name = "Worker" + i;

            int j = i;

            do {
                if (componentNodeInstanceDAO.findByApplicationInstanceAndName(applicationInstance,
                        componentNodeInstance.getComponentNode().getName() + name).isPresent()) {
                    j++;
                    name = "Worker" + j;
                } else {
                    nameExists = false;
                }

            } while (nameExists);

            // Replicate Component Node Instance
            ComponentNodeInstance workerI = componentService.replicateWorkerInstance(applicationInstance,
                    componentNodeInstance.getLoadBalancedBy(), componentNodeInstance,
                    componentNodeInstance.getComponentNode().getName() + name, false);
            componentNodeInstanceList.add(workerI);

            OrchestratorComponentNodeInstance workerOrchestratorCNI = componentService.translateWorkerInstance(applicationInstance,
                    workerI, workerI.getProvider().getProviderID() + "");

            services.add(workerOrchestratorCNI);
        }

        if(services.isEmpty()){
            logger.info("Services are empty for some reason: " + services.size());

            scalingRequest.setProceedWithScaling(false);
            return scalingRequest;
        }

        componentNodeInstanceList.stream().forEach(worker -> {

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

        scalingRequest.setProceedWithScaling(true);
        scalingRequest.setServiceList(services);
        scalingRequest.setComponentNodeInstanceList(componentNodeInstanceList);

        return scalingRequest;
    }

    public ScalingObjects getOrchestratorApplicationInstance(ScalingObjects scalingRequest){
        Application application = scalingRequest.getApplication();
        ApplicationInstance applicationInstance = scalingRequest.getApplicationInstance();
        ComponentNodeInstance componentNodeInstance = scalingRequest.getComponentNodeInstanceWorker();


        OrchestratorApplicationInstance orchestratorApplicationInstance = new OrchestratorApplicationInstance();
        orchestratorApplicationInstance.setGraphInstanceHexID(applicationInstance.getHexID());
        orchestratorApplicationInstance
                .setGraphInstanceID(applicationInstance.getApplicationInstanceID() + "");
        orchestratorApplicationInstance.setGraphInstanceName(applicationInstance.getName());
        orchestratorApplicationInstance.setGraphHexID(application.getHexID());
        orchestratorApplicationInstance.setGraphID(application.getId() + "");
        orchestratorApplicationInstance.setGraphName(application.getName());
        orchestratorApplicationInstance.setIpv6Enabled(applicationInstance.getOverlay());

        List<OrchestratorProviderAuthenticationDetails> vimDescriptors = new ArrayList<>();

        OrchestratorProviderAuthenticationDetails orchestratorProviderAuthenticationDetails = new OrchestratorProviderAuthenticationDetails();
        orchestratorProviderAuthenticationDetails
                .setId(componentNodeInstance.getProvider().getProviderID() + "");
        orchestratorProviderAuthenticationDetails
                .setName(componentNodeInstance.getProvider().getName() + "");
        orchestratorProviderAuthenticationDetails
                .setAdapterType(componentNodeInstance.getProvider().getProviderType().getName());
        orchestratorProviderAuthenticationDetails.setAdapterImplementation(
                componentNodeInstance.getProvider().getProviderType().getAdapterImplementation());
        orchestratorProviderAuthenticationDetails.setProxy(
                null != componentNodeInstance.getProvider().getProxy() ? componentNodeInstance
                        .getProvider().getProxy() : null);
        orchestratorProviderAuthenticationDetails.setMeshIdentifier(
                null != componentNodeInstance.getProvider().getMeshIdentifier()
                        ? componentNodeInstance.getProvider().getMeshIdentifier() : null);
        orchestratorProviderAuthenticationDetails.setDomain(
                null != componentNodeInstance.getProvider().getDomain() ? componentNodeInstance
                        .getProvider().getDomain() : null);
        orchestratorProviderAuthenticationDetails.setProject(
                null != componentNodeInstance.getProvider().getProject() ? componentNodeInstance
                        .getProvider().getProject() : null);
        orchestratorProviderAuthenticationDetails.setEndpoint(
                null != componentNodeInstance.getProvider().getEndpoint() ? componentNodeInstance
                        .getProvider().getEndpoint() : null);
        orchestratorProviderAuthenticationDetails.setUsername(
                null != componentNodeInstance.getProvider().getUsername() ? componentNodeInstance
                        .getProvider().getUsername() : null);
        orchestratorProviderAuthenticationDetails.setPassword(
                null != componentNodeInstance.getProvider().getPassword() ? componentNodeInstance
                        .getProvider().getPassword() : null);
        orchestratorProviderAuthenticationDetails.setPrivateKey(
                null != componentNodeInstance.getProvider().getPrivateKey() ? componentNodeInstance
                        .getProvider().getPrivateKey() : null);
        orchestratorProviderAuthenticationDetails.setPublicKey(
                null != componentNodeInstance.getProvider().getPublicKey() ? componentNodeInstance
                        .getProvider().getPublicKey() : null);
        orchestratorProviderAuthenticationDetails.setImageID(
                null != componentNodeInstance.getProvider().getImageID() ? componentNodeInstance
                        .getProvider().getImageID() : null);
        orchestratorProviderAuthenticationDetails.setNetworkID(
                null != componentNodeInstance.getProvider().getNetworkID() ? componentNodeInstance
                        .getProvider().getNetworkID()
                        : null != componentNodeInstance.getProvider().getExternalNetworkID()
                        ? componentNodeInstance.getProvider().getExternalNetworkID() : null);
        orchestratorProviderAuthenticationDetails.setPublicNetwork(
                null != componentNodeInstance.getProvider().getPublicNetwork() ? componentNodeInstance
                        .getProvider().getPublicNetwork() : null);

        vimDescriptors.add(orchestratorProviderAuthenticationDetails);

        orchestratorApplicationInstance.setProviderAuthenticationDetails(vimDescriptors);

        List<OrchestratorComponentNodeInstance> services = scalingRequest.getServiceList();
        services.stream().forEach(service -> {

            if (sliceDAO.findByApplicationInstance(applicationInstance).isPresent()) {

                try {

                    ComponentNodeInstance newComponentNodeInstance = componentNodeInstanceDAO
                            .findById(Long.valueOf(service.getComponentNodeInstanceID())).get();

                    Slice slice = sliceDAO.findByApplicationInstance(applicationInstance).get();

                    // Check if there is a slice
                    SlicePlacement slicePlacement = slicePlacementDAO
                            .findBySliceAndComponentNodeInstance(slice, componentNodeInstance).get();

                    SlicePlacement newSlicePlacement = new SlicePlacement();
                    newSlicePlacement.setFlavorID(slicePlacement.getFlavorID());
                    newSlicePlacement.setComponentNodeInstance(newComponentNodeInstance);
                    newSlicePlacement.setLastModified(new Date());
                    newSlicePlacement.setDateCreated(new Date());
                    newSlicePlacement.setSlice(slice);
                    newSlicePlacement.setProvider(slicePlacement.getProvider());
                    slicePlacementDAO.save(newSlicePlacement);

                    List<SlicePlacementAttachmentPoint> attachmentPoints = slicePlacementAttachmentPointDAO
                            .findAllBySlicePlacement(slicePlacement);

                    // Handle attachment points
                    String vimID = slicePlacement.getProvider().getProviderID() + "";
                    service.setProviderID(vimID);
                    service.getFlavor().setId(slicePlacement.getFlavorID());

                    if (null != attachmentPoints && !attachmentPoints.isEmpty()) {

                        // Required Interfaces
                        List<GraphLinkNodeInstance> graphLinkNodeInstances = graphLinkNodeInstanceDAO
                                .findAllByApplicationInstanceAndComponentNodeInstanceFrom(
                                        applicationInstance, newComponentNodeInstance);

                        // Exposed Interfaces
                        List<InterfaceInstance> interfaceInstances = interfaceInstanceDAO
                                .findAllByComponentNodeInstanceOrderByDateCreatedDesc(
                                        newComponentNodeInstance, null).getContent();

                        List<SlicePlacementAttachmentPoint> newAttachmentPoints = new ArrayList<>();

                        // Create new SlicePlacementAttachmentPoints
                        attachmentPoints.stream().forEach(attachmentPoint -> {

                            SlicePlacementAttachmentPoint slicePlacementAttachmentPoint = new SlicePlacementAttachmentPoint();
                            slicePlacementAttachmentPoint.setSlicePlacement(newSlicePlacement);
                            slicePlacementAttachmentPoint
                                    .setAttachmentPoint(attachmentPoint.getAttachmentPoint());
                            if (null != attachmentPoint.getGraphLinkNodeInstance()) {
                                slicePlacementAttachmentPoint.setGraphLinkNodeInstance(
                                        graphLinkNodeInstances.stream().filter(
                                                graphLinkNodeInstance -> graphLinkNodeInstance.getGraphLinkNode()
                                                        .getGraphLink().getGraphLinkID().equals(
                                                                attachmentPoint.getGraphLinkNodeInstance()
                                                                        .getGraphLinkNode().getGraphLink().getGraphLinkID()))
                                                .collect(Collectors.toList()).get(0));
                            } else if (null != attachmentPoint.getInterfaceInstance()) {
                                slicePlacementAttachmentPoint.setInterfaceInstance(
                                        interfaceInstances.stream().filter(
                                                interfaceInstance -> interfaceInstance.getInterfaceObj()
                                                        .getInterfaceID().equals(
                                                                attachmentPoint.getInterfaceInstance().getInterfaceObj()
                                                                        .getInterfaceID())).collect(Collectors.toList())
                                                .get(0));
                            }
                            slicePlacementAttachmentPoint.setDateCreated(new Date());
                            slicePlacementAttachmentPoint.setLastModified(new Date());
                            slicePlacementAttachmentPointDAO.save(slicePlacementAttachmentPoint);
                            newAttachmentPoints.add(slicePlacementAttachmentPoint);

                        });

                        newAttachmentPoints.stream().forEach(attachmentPoint -> {

                            if (null != attachmentPoint.getGraphLinkNodeInstance()) {

                                if (null != service.getDependsOn() && !service.getDependsOn().isEmpty()) {

                                    service.getDependsOn().stream().forEach(dependency -> {

                                        if (null != graphLinkNodeInstances &&
                                                !graphLinkNodeInstances.stream().filter(graphLinkNodeInstance ->
                                                        attachmentPoint.getGraphLinkNodeInstance()
                                                                .getGraphLinkNodeInstanceID() == graphLinkNodeInstance
                                                                .getGraphLinkNodeInstanceID() && graphLinkNodeInstance
                                                                .getComponentNodeInstanceTo().getComponentNode().getHexID()
                                                                .equals(dependency.getDependency()))
                                                        .collect(Collectors.toList()).isEmpty()) {

                                            dependency
                                                    .setNetworkAttachmentPoint(attachmentPoint.getAttachmentPoint());

                                            GraphLinkNodeInstance graphLinkNodeInstance = graphLinkNodeInstanceDAO
                                                    .findById(attachmentPoint.getGraphLinkNodeInstance()
                                                            .getGraphLinkNodeInstanceID()).get();

                                            if (null != graphLinkNodeInstance.getGraphLinkNode()) {

                                                GraphLinkNode graphLinkNode = graphLinkNodeDAO.findById(
                                                        graphLinkNodeInstance.getGraphLinkNode().getGraphLinkNodeID())
                                                        .get();

                                                GraphLink graphLink = graphLinkDAO
                                                        .findById(graphLinkNode.getGraphLink().getGraphLinkID()).get();

                                                Interface intfrc = interfaceDAO
                                                        .findById(graphLink.getInterfaceObj().getInterfaceID()).get();

                                                dependency.setVna(intfrc.getVna());

                                            } else {
                                                // Load Balancers
                                                dependency.setVna("VNA0");

                                            }

                                        }

                                    });

                                }

                            } else {

                                if (null != service.getPorts() && !service.getPorts().isEmpty()) {

                                    service.getPorts().stream().forEach(port -> {

                                        if (null != interfaceInstances && !interfaceInstances.stream().filter(
                                                interfaceInstance ->
                                                        attachmentPoint.getInterfaceInstance().getInterfaceInstanceID()
                                                                == interfaceInstance.getInterfaceInstanceID()
                                                                && interfaceInstance.getInterfaceObj().getPort()
                                                                .equals(port.getTarget())).collect(Collectors.toList())
                                                .isEmpty()) {

                                            port.setNetworkAttachmentPoint(attachmentPoint.getAttachmentPoint());
                                            port.setVna(attachmentPoint.getInterfaceInstance().getInterfaceObj()
                                                    .getVna()); // TODO

                                        }

                                    });

                                }

                            }

                        });

                    }

                    if (null != service.getDependsOn() && !service.getDependsOn().isEmpty()) {

                        service.getDependsOn().stream().forEach(dependency -> {
                            if (null == dependency.getNetworkAttachmentPoint() || dependency
                                    .getNetworkAttachmentPoint().isEmpty()) {
                                dependency.setNetworkAttachmentPoint(
                                        null != newSlicePlacement.getProvider().getNetworkID() ?
                                                newSlicePlacement.getProvider().getNetworkID() :
                                                newSlicePlacement.getProvider().getExternalNetworkID());
                            }
                        });


                    }

                    if (null != service.getPorts() && !service.getPorts().isEmpty()) {

                        service.getPorts().stream().forEach(port -> {
                            if (null == port.getNetworkAttachmentPoint() || port
                                    .getNetworkAttachmentPoint().isEmpty()) {

                                port.setNetworkAttachmentPoint(
                                        null != newSlicePlacement.getProvider().getNetworkID() ?
                                                newSlicePlacement.getProvider().getNetworkID() :
                                                newSlicePlacement.getProvider().getExternalNetworkID());
                            }
                        });

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    logger.log(Level.SEVERE, e.getMessage(), e);
                }


            }

            try {

                if (null != service.getDependsOn() && !service.getDependsOn().isEmpty()) {

                    List<OrchestratorDependency> newDependencies = new ArrayList<>();

                    service.getDependsOn().stream().forEach(dependency -> {

                        try {

                            if (newDependencies.stream()
                                    .filter(dep -> dep.getDependency().equals(dependency.getDependency()))
                                    .collect(Collectors.toList()).isEmpty()) {

                                // TODO Convert component Node Instance HexID to Component Node HexID

                                ComponentNodeInstance servCNI = componentNodeInstanceDAO
                                        .findById(Long.valueOf(service.getComponentNodeInstanceID())).get();

                                if (null != servCNI && null != servCNI.getLoadBalancedBy()) {

                                    if (componentNodeInstanceDAO.findByHexID(dependency.getDependency())
                                            .isPresent()) {

                                        ComponentNodeInstance cni = componentNodeInstanceDAO
                                                .findByHexID(dependency.getDependency()).get();
                                        dependency.setDependency(cni.getComponentNode().getHexID());

                                    } else {

                                        ComponentNode componentNode = componentNodeDAO
                                                .findByHexID(dependency.getDependency()).get();
                                        dependency.setDependency(componentNode.getHexID());

                                    }

                                } else {

                                    ComponentNodeInstance cni = componentNodeInstanceDAO
                                            .findByHexID(dependency.getDependency()).get();
                                    dependency.setDependency(cni.getComponentNode().getHexID());

                                }

                                newDependencies.add(dependency);

                            }


                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    });

                    service.setDependsOn(newDependencies);

                }

                if (null != service.getEnvironmentalVariables() && !service
                        .getEnvironmentalVariables().isEmpty()) {

                    Map<String, String> newEnvVars = new HashMap<>();

                    service.getEnvironmentalVariables().entrySet().stream().forEach(envVar -> {
                        try {

                            if (envVar.getValue().startsWith("@") || envVar.getValue().startsWith("#")) {

                                String value = envVar.getValue().substring(1);

                                ComponentNodeInstance dependencyCNI = componentNodeInstanceDAO
                                        .findByNameAndApplicationInstance(value, applicationInstance).get();

                                // TODO Convert component Node Instance HexID to Component Node HexID
                                ComponentNodeInstance cni = componentNodeInstanceDAO
                                        .findByHexID(dependencyCNI.getHexID()).get();
                                newEnvVars.put(envVar.getKey(), envVar.getValue().charAt(0)+ cni.getComponentNode().getHexID());


                            } else {
                                newEnvVars.put(envVar.getKey(), envVar.getValue());
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    });

                    service.setEnvironmentalVariables(newEnvVars);

                }


            } catch (Exception e) {
                e.printStackTrace();
                logger.log(Level.SEVERE, e.getMessage(), e);
            }

        });

        orchestratorApplicationInstance.setServices(services);
        scalingRequest.setOrchestratorApplicationInstance(orchestratorApplicationInstance);

        return scalingRequest;
    }

    public boolean decreaseWorkers(OrchestratorScalingRequest orchestratorScalingRequest){
        ApplicationInstance applicationInstance = applicationInstanceDAO
                .findById(Long.valueOf(orchestratorScalingRequest.getGraphInstanceID())).get();

        // Find existing worker
        ComponentNodeInstance componentNodeInstance = componentNodeInstanceDAO
                .findById(Long.valueOf(orchestratorScalingRequest.getComponentNodeInstanceID()))
                .get();

        List<GraphLinkNodeInstance> graphLinkNodeInstancesFrom = graphLinkNodeInstanceDAO
                .findAllByApplicationInstanceAndComponentNodeInstanceFrom(applicationInstance,
                        componentNodeInstance);

        if (null != graphLinkNodeInstancesFrom && !graphLinkNodeInstancesFrom.isEmpty()) {

            // TODO Check if we have constraints on this GLNI
            List<Constraint> constraints = constraintDAO
                    .findAllByApplicationInstance(applicationInstance, null).getContent();

            if (null != constraints && !constraints.isEmpty()) {

                constraints.stream()
                        .filter(constraint -> null != constraint.getGraphLinkNodeInstance())
                        .forEach(constraint -> {

                            graphLinkNodeInstancesFrom.stream().forEach(graphLinkNodeInstance -> {

                                if (constraint.getGraphLinkNodeInstance().getGraphLinkNodeInstanceID().equals(graphLinkNodeInstance.getGraphLinkNodeInstanceID())) {

                                    constraintDAO.delete(constraint);
                                }
                            });
                        });
            }

            graphLinkNodeInstanceDAO.deleteAll(graphLinkNodeInstancesFrom);
        }

        List<GraphLinkNodeInstance> graphLinkNodeInstancesTo = graphLinkNodeInstanceDAO
                .findAllByApplicationInstanceAndComponentNodeInstanceTo(applicationInstance,
                        componentNodeInstance);

        if (null != graphLinkNodeInstancesTo && !graphLinkNodeInstancesTo.isEmpty()) {

            // TODO Check if we have constraints on this GLNI

            List<Constraint> constraints = constraintDAO
                    .findAllByApplicationInstance(applicationInstance, null).getContent();

            if (null != constraints && !constraints.isEmpty()) {

                constraints.stream()
                        .filter(constraint -> null != constraint.getGraphLinkNodeInstance())
                        .forEach(constraint -> {

                            graphLinkNodeInstancesTo.stream().forEach(graphLinkNodeInstance -> {

                                if (constraint.getGraphLinkNodeInstance().getGraphLinkNodeInstanceID().equals(graphLinkNodeInstance.getGraphLinkNodeInstanceID())) {

                                    constraintDAO.delete(constraint);
                                }
                            });
                        });
            }

            graphLinkNodeInstanceDAO.deleteAll(graphLinkNodeInstancesTo);
        }

        // Delete this worker
        List<VolumeInstance> volumeInstances = volumeInstanceDAO
                .findAllByComponentNodeInstanceOrderByDateCreatedDesc(componentNodeInstance, null)
                .getContent();

        //TODO check this when volumes are done
        if (null != volumeInstances && !volumeInstances.isEmpty()) {
            volumeInstanceDAO.deleteAll(volumeInstances);
        }

        List<InterfaceInstance> interfaceInstances = interfaceInstanceDAO
                .findAllByComponentNodeInstanceOrderByDateCreatedDesc(componentNodeInstance, null)
                .getContent();

        if (null != interfaceInstances && !interfaceInstances.isEmpty()) {

            // TODO Check if we have constraints on this II
            List<Constraint> constraints = constraintDAO
                    .findAllByApplicationInstance(applicationInstance, null).getContent();

            if (null != constraints && !constraints.isEmpty()) {

                constraints.stream().filter(constraint -> null != constraint.getInterfaceInstance())
                        .forEach(constraint -> {

                            interfaceInstances.stream().forEach(interfaceInstance -> {

                                if (constraint.getInterfaceInstance().getInterfaceInstanceID().equals(interfaceInstance.getInterfaceInstanceID())) {
                                    constraintDAO.delete(constraint);
                                }
                            });
                        });
            }
            interfaceInstanceDAO.deleteAll(interfaceInstances);
        }

        List<EnvironmentalVariableInstance> environmentalVariableInstances = environmentalVariableInstanceDAO
                .findAllByComponentNodeInstanceOrderByDateCreatedDesc(componentNodeInstance, null)
                .getContent();

        if (null != environmentalVariableInstances && !environmentalVariableInstances
                .isEmpty()) {
            environmentalVariableInstanceDAO.deleteAll(environmentalVariableInstances);
        }

        List<PluginInstance> pluginInstances = pluginInstanceDAO
                .findAllByComponentNodeInstanceOrderByDateCreatedDesc(componentNodeInstance, null)
                .getContent();

        if (null != pluginInstances && !pluginInstances.isEmpty()) {
            pluginInstanceDAO.deleteAll(pluginInstances);
        }

        List<IDRuleSetInstance> idRuleSetInstances = idRuleSetInstanceDAO
                .findAllByComponentNodeInstanceOrderByDateCreatedDesc(componentNodeInstance, null)
                .getContent();

        if (null != idRuleSetInstances && !idRuleSetInstances.isEmpty()) {
            idRuleSetInstanceDAO.deleteAll(idRuleSetInstances);
        }

        List<DeviceInstance> deviceInstances = deviceInstanceDAO
                .findAllByComponentNodeInstanceOrderByDateCreatedDesc(componentNodeInstance, null)
                .getContent();

        if (null != deviceInstances && !deviceInstances.isEmpty()) {
            deviceInstanceDAO.deleteAll(deviceInstances);
        }

        List<LocationInstance> locationInstances = locationInstanceDAO
                .findAllByComponentNodeInstance(componentNodeInstance, null).getContent();

        if (null != locationInstances && !locationInstances.isEmpty()) {
            locationInstanceDAO.deleteAll(locationInstances);
        }

        if (flavorInstanceDAO.findByComponentNodeInstance(componentNodeInstance).isPresent()) {
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
                .findAllByComponentNodeInstanceOrderByDateCreatedDesc(componentNodeInstance, null)
                .getContent();

        if (null != componentNodeInstanceStatuses && !componentNodeInstanceStatuses.isEmpty()) {

            componentNodeInstanceStatuses.stream().forEach(componentNodeInstanceStatus -> {
                componentNodeInstanceStatus.setComponentNodeInstance(null);
                //TODO changed to delete from save
                componentNodeInstanceStatusDAO.delete(componentNodeInstanceStatus);
            });
        }

        List<ComponentNodeInstanceAlert> componentNodeInstanceAlerts = componentNodeInstanceAlertDAO
                .findAllByComponentNodeInstanceOrderByDateCreatedDesc(componentNodeInstance, null)
                .getContent();

        if (null != componentNodeInstanceAlerts && !componentNodeInstanceAlerts.isEmpty()) {

            componentNodeInstanceAlerts.stream().forEach(componentNodeInstanceAlert -> {
                componentNodeInstanceAlert.setComponentNodeInstance(null);
                //TODO changed to delete from save
                componentNodeInstanceAlertDAO.delete(componentNodeInstanceAlert);
            });
        }

        List<Constraint> constraints = constraintDAO
                .findAllByApplicationInstance(applicationInstance, null).getContent();

        if (null != constraints && !constraints.isEmpty()) {

            constraints.stream().filter(
                    constraint -> null != constraint.getComponentNodeInstance() && constraint
                            .getComponentNodeInstance().getComponentNodeInstanceID()
                            .equals(componentNodeInstance.getComponentNodeInstanceID()))
                    .forEach(constraint -> {
                        constraintDAO.delete(constraint);
                    });
        }

        // ΤΟDO for Astrid
        // Delete hashing
        componentNodeInstanceHashDAO.deleteAllByComponentNodeInstance(componentNodeInstance);
        // Delete results
        securityConfigurationResultDAO.deleteAllByComponentNodeInstance(componentNodeInstance);

        Query q = entityManager
                .createNativeQuery("DELETE FROM component_node_instance WHERE id = ?");
        q.setParameter(1, componentNodeInstance.getComponentNodeInstanceID());
        entityManager.joinTransaction();
        q.executeUpdate();

        return true;
    }


    //TODO implement when the volumes are finished
    public void addSharedVolume(){}
    public void addBackupVolume(){}
    public void removeSharedVolume(){}
    public void removeBackupVolume(){}
}
