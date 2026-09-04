package eu.orchestrator.backend.util;

import eu.orchestrator.elasticity.spi.adapter.ElasticityFrameworkBackend;
import eu.orchestrator.elasticity.spi.model.backend.ElasticityObjects;
import eu.orchestrator.repository.dao.ApplicationDAO;
import eu.orchestrator.repository.dao.ApplicationInstanceDAO;
import eu.orchestrator.repository.dao.ComponentDAO;
import eu.orchestrator.repository.dao.ComponentNodeDAO;
import eu.orchestrator.repository.dao.ComponentNodeInstanceDAO;
import eu.orchestrator.repository.dao.ComponentNodeInstanceStatusDAO;
import eu.orchestrator.repository.dao.ConstraintDAO;
import eu.orchestrator.repository.dao.DeviceDAO;
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
import eu.orchestrator.repository.dao.ProviderDAO;
import eu.orchestrator.repository.dao.ProviderTypeDAO;
import eu.orchestrator.repository.dao.RequirementDAO;
import eu.orchestrator.repository.dao.RuntimePolicyDAO;
import eu.orchestrator.repository.dao.SliceConstraintSatisfactionDAO;
import eu.orchestrator.repository.dao.SliceDAO;
import eu.orchestrator.repository.dao.SlicePlacementAttachmentPointDAO;
import eu.orchestrator.repository.dao.SlicePlacementDAO;
import eu.orchestrator.repository.dao.SliceProviderDAO;
import eu.orchestrator.repository.dao.VolumeInstanceDAO;
import eu.orchestrator.repository.domain.Application;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.Component;
import eu.orchestrator.repository.domain.ComponentNode;
import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.repository.domain.ComponentNodeInstance.SecurityEnablers;
import eu.orchestrator.repository.domain.ComponentNodeInstanceStatus;
import eu.orchestrator.repository.domain.Constraint;
import eu.orchestrator.repository.domain.GraphLinkNodeInstance;
import eu.orchestrator.repository.domain.Plugin;
import eu.orchestrator.repository.domain.Provider;
import eu.orchestrator.repository.domain.Slice;
import eu.orchestrator.repository.domain.SliceConstraintSatisfaction;
import eu.orchestrator.repository.domain.SlicePlacement;
import eu.orchestrator.repository.domain.SlicePlacementAttachmentPoint;
import eu.orchestrator.repository.domain.SliceProvider;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorApplicationInstance;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorChangedStatusNotification;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorComponentNodeInstance;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorDependency;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorElasticity;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorFlavor;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorHealthCheck;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorKafkaConfig;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorLocation;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorPlugin;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorPort;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorProviderAuthenticationDetails;
import eu.orchestrator.transfer.entities.oss.SliceIntent;
import eu.orchestrator.backend.config.KafkaTopicConfig;
import eu.orchestrator.backend.kafka.Sender;
import eu.orchestrator.backend.service.elasticity.ElasticityService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import jakarta.persistence.EntityManager;

public class OrchestratorUtil {

    private static final Logger logger = Logger.getLogger(OrchestratorUtil.class.getName());

    public static boolean executeDeployment(ApplicationInstance existingApplicationInstance, ApplicationDAO applicationDao,
            ComponentNodeInstanceStatusDAO componentNodeInstanceStatusDao, ComponentNodeDAO componentNodeDao, ComponentNodeInstanceDAO componentNodeInstanceDao,
            GraphLinkNodeInstanceDAO graphLinkNodeInstanceDao, PluginDAO pluginDao, RestTemplate restTemplate, String orchestratorURL, String uiURL,
            ElasticityService elasticityService, KafkaTopicConfig kafkaTopicConfig, Sender sender) {

        boolean isSuccess = false;

        Map<Long, ComponentNodeInstance> mapOfNeededChanges = new HashMap<>();

        Map<Long, ComponentNodeInstance> lambdaProxyLoadBalancers = new HashMap<>();
        Map<Long, OrchestratorComponentNodeInstance> lambdaProxyOrchestratorLoadBalancers = new HashMap<>();

        /////////////

        Map<String, String> updatedDependsOnComponentNodeInstanceURIs = new HashMap<>();

        /////////////

        Application existingApplication = applicationDao
                .findById(existingApplicationInstance.getApplication().getId()).get();

        // Convert ApplicationInstance to OrchestratorApplicationInstance
        OrchestratorApplicationInstance orchestratorApplicationInstance = new OrchestratorApplicationInstance();
        orchestratorApplicationInstance.setCallbackURL(
                uiURL + "/api/v1/callback/deployment/" + existingApplicationInstance
                        .getApplicationInstanceID());
        orchestratorApplicationInstance.setGraphInstanceHexID(existingApplicationInstance.getHexID());
        orchestratorApplicationInstance
                .setGraphInstanceID(existingApplicationInstance.getApplicationInstanceID() + "");
        orchestratorApplicationInstance.setGraphInstanceName(existingApplicationInstance.getName());
        orchestratorApplicationInstance.setGraphHexID(existingApplication.getHexID());
        orchestratorApplicationInstance.setGraphID(existingApplication.getId() + "");
        orchestratorApplicationInstance.setGraphName(existingApplication.getName());
        orchestratorApplicationInstance.setIpv6Enabled(existingApplicationInstance.getOverlay());

        OrchestratorKafkaConfig orchestratorKafkaConfig = new OrchestratorKafkaConfig();
        orchestratorKafkaConfig.setKafkaServer(kafkaTopicConfig.getKafkaServer());
        orchestratorKafkaConfig.setAgentIdsAlertTopic(kafkaTopicConfig.getAgentIdsAlertTopic());
        orchestratorKafkaConfig.setAgentIdsConfigTopic(kafkaTopicConfig.getAgentIdsConfigTopic());
        orchestratorKafkaConfig.setAgentIpsConfigTopic(kafkaTopicConfig.getAgentIpsConfigTopic());
        orchestratorKafkaConfig.setAgentSecurityConfigResultTopic(kafkaTopicConfig.getAgentSecurityConfigResultTopic());
        orchestratorKafkaConfig.setAgentSecurityConfigTopic(kafkaTopicConfig.getAgentSecurityConfigTopic());
        orchestratorKafkaConfig.setAgentSocConfigTopic(kafkaTopicConfig.getAgentSocConfigTopic());
        orchestratorKafkaConfig.setOrchestratorSecurityConfigResultTopic(kafkaTopicConfig.getOrchestratorSecurityConfigResultTopic());

        orchestratorApplicationInstance.setOrchestratorKafkaConfig(orchestratorKafkaConfig);

        Map<String, OrchestratorProviderAuthenticationDetails> vimDescriptors = new HashMap<>();

        // Component Node Instances
        Map<String, OrchestratorComponentNodeInstance> services = new HashMap<>();

        List<ComponentNodeInstance> existingComponentNodeInstances = new ArrayList<>(
                existingApplicationInstance.getComponentNodeInstances());

//    existingComponentNodeInstances.stream().forEach(componentNodeInstance -> {
        for (ComponentNodeInstance componentNodeInstance : existingComponentNodeInstances) {
            OrchestratorComponentNodeInstance orchestratorComponentNodeInstance = new OrchestratorComponentNodeInstance();

            // Default deployment
            OrchestratorProviderAuthenticationDetails orchestratorProviderAuthenticationDetails = new OrchestratorProviderAuthenticationDetails();
            orchestratorProviderAuthenticationDetails
                    .setId(componentNodeInstance.getProvider().getProviderID() + "");
            orchestratorProviderAuthenticationDetails
                    .setAdapterType(componentNodeInstance.getProvider().getProviderType().getName());
            orchestratorProviderAuthenticationDetails.setAdapterImplementation(
                    componentNodeInstance.getProvider().getProviderType().getAdapterImplementation());
            orchestratorProviderAuthenticationDetails.setProxy(
                    null != componentNodeInstance.getProvider().getProxy() ? componentNodeInstance
                            .getProvider().getProxy() : null);
            orchestratorProviderAuthenticationDetails.setMeshIdentifier(
                    null != componentNodeInstance.getProvider().getMeshIdentifier() ? componentNodeInstance
                            .getProvider().getMeshIdentifier() : null);
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
                    null != componentNodeInstance.getProvider().getImageID() && !componentNodeInstance
                            .getProvider().getImageID().isEmpty() ? componentNodeInstance.getProvider()
                            .getImageID() : null);
            orchestratorProviderAuthenticationDetails.setNetworkID(
                    null != componentNodeInstance.getProvider().getNetworkID() && !componentNodeInstance
                            .getProvider().getNetworkID().isEmpty() ? componentNodeInstance.getProvider()
                            .getNetworkID() : null);
            orchestratorProviderAuthenticationDetails.setPublicNetwork(
                    null != componentNodeInstance.getProvider().getPublicNetwork() ? componentNodeInstance
                            .getProvider().getPublicNetwork() : null);

            if (!vimDescriptors.containsKey(componentNodeInstance.getProvider().getProviderID() + "")) {
                vimDescriptors.put(componentNodeInstance.getProvider().getProviderID() + "",
                        orchestratorProviderAuthenticationDetails);
            }

            orchestratorComponentNodeInstance
                    .setProviderID(orchestratorProviderAuthenticationDetails.getId());
            orchestratorComponentNodeInstance
                    .setComponentNodeInstanceHexID(componentNodeInstance.getHexID() + "");
            orchestratorComponentNodeInstance
                    .setComponentNodeInstanceID(componentNodeInstance.getComponentNodeInstanceID() + "");
            orchestratorComponentNodeInstance
                    .setComponentNodeInstanceName(componentNodeInstance.getName());
            orchestratorComponentNodeInstance
                    .setComponentNodeHexID(componentNodeInstance.getComponentNode().getHexID() + "");
            orchestratorComponentNodeInstance
                    .setComponentNodeID(componentNodeInstance.getComponentNode().getComponentNodeID() + "");
            orchestratorComponentNodeInstance
                    .setComponentNodeName(componentNodeInstance.getComponentNode().getName());
            orchestratorComponentNodeInstance
                    .setMinimumWorkers(componentNodeInstance.getMinimumWorkers());
            orchestratorComponentNodeInstance
                    .setMaximumWorkers(componentNodeInstance.getMaximumWorkers());
            orchestratorComponentNodeInstance.setStatusIDS(
                    null != componentNodeInstance.getStatusIDS() && componentNodeInstance.getStatusIDS()
                            .booleanValue());
            orchestratorComponentNodeInstance.setStatusIPS(
                    null != componentNodeInstance.getStatusIPS() && componentNodeInstance.getStatusIPS()
                            .booleanValue());
            orchestratorComponentNodeInstance.setStatusSoc(
                    null != componentNodeInstance.getStatusSOC() && componentNodeInstance.getStatusSOC()
                            .booleanValue());

            orchestratorComponentNodeInstance.setNetworkModeHost(
                    null != componentNodeInstance.getNetworkModeHost() && componentNodeInstance.getNetworkModeHost()
                            .booleanValue());
            orchestratorComponentNodeInstance.setPrivilege(
                    null != componentNodeInstance.getPrivilege() && componentNodeInstance.getPrivilege()
                            .booleanValue());
            orchestratorComponentNodeInstance.setHostname(
                    null != componentNodeInstance.getHostname() && !componentNodeInstance.getHostname()
                            .isEmpty() ? componentNodeInstance.getHostname() : null);
            orchestratorComponentNodeInstance.setDnsEntry(
                    null != componentNodeInstance.getDnsEntry() && !componentNodeInstance.getDnsEntry()
                            .isEmpty() ? componentNodeInstance.getDnsEntry() : null);
            orchestratorComponentNodeInstance.setSharedMemorySize(
                    null != componentNodeInstance.getSharedMemorySize() && !componentNodeInstance.getSharedMemorySize()
                            .isEmpty() ? componentNodeInstance.getSharedMemorySize() : null);
            orchestratorComponentNodeInstance.setDockerUsername(
                    null != componentNodeInstance.getComponentNode().getComponent().getDockerUsername()
                            && !componentNodeInstance.getComponentNode().getComponent().getDockerUsername()
                            .isEmpty() ? componentNodeInstance.getComponentNode().getComponent().getDockerUsername() : null);
            orchestratorComponentNodeInstance.setDockerPassword(
                    null != componentNodeInstance.getComponentNode().getComponent().getDockerPassword()
                            && !componentNodeInstance.getComponentNode().getComponent().getDockerPassword()
                            .isEmpty() ? componentNodeInstance.getComponentNode().getComponent().getDockerPassword() : null);

            orchestratorComponentNodeInstance.setUlimitMemlockSoft(componentNodeInstance.getComponentNode().getComponent().getUlimitMemlockSoft());
            orchestratorComponentNodeInstance.setUlimitMemlockHard(componentNodeInstance.getComponentNode().getComponent().getUlimitMemlockHard());
            orchestratorComponentNodeInstance.setDockerExecutionUser(componentNodeInstance.getComponentNode().getComponent().getDockerExecutionUser());

            if (null != componentNodeInstance.getCapabilityAdds() && !componentNodeInstance.getCapabilityAdds().isEmpty()) {
                Collection<String> newCapabilities = new ArrayList<>();
                Collection<Component.CapabilityAdd> exCapabilities = componentNodeInstance.getCapabilityAdds();
                exCapabilities.stream().forEach(capability -> {
                    newCapabilities.add(capability.getFriendlyName());
                });

                orchestratorComponentNodeInstance.setCapabilityAdds(newCapabilities);
            } else {
                orchestratorComponentNodeInstance.setCapabilityAdds(null);
            }
            if (null != componentNodeInstance.getCapabilityDrops() && !componentNodeInstance.getCapabilityDrops().isEmpty()) {
                Collection<String> newCapabilities = new ArrayList<>();
                Collection<Component.CapabilityDrop> exCapabilities = componentNodeInstance.getCapabilityDrops();
                exCapabilities.stream().forEach(capability -> {
                    newCapabilities.add(capability.getFriendlyName());
                });

                orchestratorComponentNodeInstance.setCapabilityDrops(newCapabilities);
            } else {
                orchestratorComponentNodeInstance.setCapabilityDrops(null);
            }

            orchestratorComponentNodeInstance.setCommand(
                    null != componentNodeInstance.getCommand() && !componentNodeInstance.getCommand()
                            .isEmpty() ? Arrays.asList(componentNodeInstance.getCommand()) : null);

            orchestratorComponentNodeInstance
                    .setImage(componentNodeInstance.getComponentNode().getComponent().getDockerImage());
            orchestratorComponentNodeInstance
                    .setRegistry(componentNodeInstance.getComponentNode().getComponent().getDockerRegistry());

            if (null != componentNodeInstance.getSshKey()) {
                orchestratorComponentNodeInstance.setSshKey(componentNodeInstance.getSshKey().getSshKey());
            } else {
                orchestratorComponentNodeInstance.setSshKey(null);
            }

            //TODO Astrid security enablers
            if (componentNodeInstance.getSecurityEnablers() != null && !componentNodeInstance.getSecurityEnablers().isEmpty()) {
                orchestratorComponentNodeInstance.setHasEnableSecurity(true);
                orchestratorComponentNodeInstance.setProduceHashes(
                        componentNodeInstance.getSecurityEnablers().contains(SecurityEnablers.CONFIGURATION_INTEGRITY_VERIFICATION) ||
                                componentNodeInstance.getSecurityEnablers().contains(SecurityEnablers.RUNTIME_FILE_INTEGRITY));
            } else {
                orchestratorComponentNodeInstance.setHasEnableSecurity(false);
            }

            // Scaling
            //TODO change this appropriately after the modification of the front-end, don't forget to change it to the other methods too
            OrchestratorElasticity orchestratorElasticity = new OrchestratorElasticity();
            orchestratorElasticity
                    .setProfile(componentNodeInstance.getComponentNode().getComponent().getElasticityController());
            orchestratorElasticity.setType(
                    componentNodeInstance.getComponentNode().getComponent().getElasticityController()
                            .equals("LAMBDA_FUNCTION") ? componentNodeInstance
                            .getComponentNode().getComponent().getElasticityControllerMode() : null);
            orchestratorComponentNodeInstance.setMonitoringElasticity(orchestratorElasticity);

            // If CNI requires elasticity add the appropriate controller
            ElasticityFrameworkBackend elasticityAdapter = elasticityService.fetchElasticityBackendAdapter(componentNodeInstance);
            if (elasticityAdapter != null) {
                logger.info("Component: " + componentNodeInstance.getComponentNode().getComponent().getName() + " needs elasticity");

                ElasticityObjects elasticityObjects = new ElasticityObjects();
                elasticityObjects.setServices(services);
                elasticityObjects.setMapOfNeededChanges(mapOfNeededChanges);
                elasticityObjects.setApplicationInstance(existingApplicationInstance);
                elasticityObjects.setWorkerComponentNodeInstance(componentNodeInstance);
                elasticityObjects.setVimID(orchestratorProviderAuthenticationDetails.getId());

                elasticityObjects = elasticityAdapter.configureElasticityController(elasticityObjects);

                String elasticityControllerHexID = elasticityObjects.getElasticityController().getHexID();
                services = elasticityObjects.getServices();
                mapOfNeededChanges = elasticityObjects.getMapOfNeededChanges();
//        existingApplicationInstance = elasticityObjects.getApplicationInstance();
                componentNodeInstance = elasticityObjects.getWorkerComponentNodeInstance();

                if (!updatedDependsOnComponentNodeInstanceURIs
                        .containsKey(componentNodeInstance.getHexID())) {
                    updatedDependsOnComponentNodeInstanceURIs
                            .put(componentNodeInstance.getHexID(), elasticityControllerHexID);
                }

                logger.info("Elasticity controller for: " + componentNodeInstance.getName() + " has been added");
                Optional<ComponentNodeInstance> tempComponentNodeInstanceOP = componentNodeInstanceDao.findByHexID(componentNodeInstance.getHexID());
                componentNodeInstance = tempComponentNodeInstanceOP.get();

            }

            ComponentNodeInstance updatedInterfacesComponentNodeInstance = componentNodeInstance;

            if (null != componentNodeInstance.getLoadBalancedBy()) {
                orchestratorComponentNodeInstance.setBalancedByComponentNodeHexID(componentNodeInstance.getLoadBalancedBy().getComponentNode().getHexID());
            }

            // Required Interfaces
            if (null != existingApplicationInstance.getGraphLinkNodeInstances()
                    && !existingApplicationInstance.getGraphLinkNodeInstances().isEmpty()) {

                List<OrchestratorDependency> dependsOn = new ArrayList<>();

                existingApplicationInstance.getGraphLinkNodeInstances().stream()
                        .filter(graphLinkNodeInstance -> graphLinkNodeInstance.getComponentNodeInstanceFrom()
                                .getComponentNodeInstanceID()
                                .equals(updatedInterfacesComponentNodeInstance.getComponentNodeInstanceID()))
                        .forEach(graphLinkNodeInstance -> {

                            try {

                                if (dependsOn.stream().filter(dep -> dep.getDependency()
                                                .equals(graphLinkNodeInstance.getComponentNodeInstanceTo().getHexID()))
                                        .collect(Collectors.toList()).isEmpty()) {

                                    OrchestratorDependency orchestratorDependency = new OrchestratorDependency();
                                    orchestratorDependency
                                            .setDependency(graphLinkNodeInstance.getComponentNodeInstanceTo().getHexID());
                                    orchestratorDependency.setVna(
                                            graphLinkNodeInstance.getGraphLinkNode().getGraphLink().getInterfaceObj()
                                                    .getVna());
                                    orchestratorDependency.setNetworkAttachmentPoint(
                                            orchestratorProviderAuthenticationDetails.getNetworkID());

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
            if (null != updatedInterfacesComponentNodeInstance.getInterfaceInstances() && !updatedInterfacesComponentNodeInstance
                    .getInterfaceInstances().isEmpty()) {

                List<OrchestratorPort> ports = new ArrayList<>();

                updatedInterfacesComponentNodeInstance.getInterfaceInstances().stream().forEach(interfaceInstance -> {

                    try {

                        OrchestratorPort orchestratorPort = new OrchestratorPort();
                        orchestratorPort.setTarget(interfaceInstance.getInterfaceObj().getPort());
                        orchestratorPort.setPublished(interfaceInstance.getPort());
                        orchestratorPort
                                .setProtocol(interfaceInstance.getInterfaceObj().getTransmissionProtocol());
                        orchestratorPort.setVna(interfaceInstance.getInterfaceObj().getVna());
                        orchestratorPort.setType(interfaceInstance.getInterfaceType());
                        orchestratorPort.setNetworkAttachmentPoint(
                                orchestratorProviderAuthenticationDetails.getNetworkID());
                        ports.add(orchestratorPort);

                    } catch (Exception e) {
                        e.printStackTrace();

                    }

                });

                orchestratorComponentNodeInstance.setPorts(ports);

            }

            if (null != componentNodeInstance.getEnvironmentalVariableInstances()
                    && !componentNodeInstance.getEnvironmentalVariableInstances().isEmpty()) {

                // Environmental Variables
                Map<String, String> orchestratorEnvironmentalVariables = new HashMap<>();
                componentNodeInstance.getEnvironmentalVariableInstances().stream()
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

//                        }

                            } catch (Exception e) {
                                e.printStackTrace();

                            }

                        });

                orchestratorComponentNodeInstance
                        .setEnvironmentalVariables(orchestratorEnvironmentalVariables);

            }

            if (null != componentNodeInstance.getPluginInstances() && !componentNodeInstance
                    .getPluginInstances().isEmpty()) {

                List<OrchestratorPlugin> orchestratorPlugins = new ArrayList<>();

                componentNodeInstance.getPluginInstances().stream().forEach(pluginInstance -> {

                    Plugin plugin = pluginDao.findById(pluginInstance.getPlugin().getPluginID()).get();

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
                            null != plugin.getEndpoint() && !plugin.getEndpoint().isEmpty() ? plugin.getEndpoint()
                                    : null);
                    orchestratorPlugin.setDisabledPlugin(pluginInstance.getDeletedPlugin().booleanValue());
                    orchestratorPlugins.add(orchestratorPlugin);

                });

                orchestratorComponentNodeInstance.setPlugins(orchestratorPlugins);

            }

            if (null != componentNodeInstance.getDeviceInstances() && !componentNodeInstance
                    .getDeviceInstances().isEmpty()) {

                // Devices
                Map<String, String> orchestratorDevices = new HashMap<>();
                componentNodeInstance.getDeviceInstances().stream().forEach(deviceInstance -> {

                    try {

                        orchestratorDevices.put(deviceInstance.getKey(), deviceInstance.getValue());

                    } catch (Exception e) {
                        e.printStackTrace();

                    }

                });

                orchestratorComponentNodeInstance.setDevices(orchestratorDevices);

            }

            if (null != componentNodeInstance.getVolumeInstances() && !componentNodeInstance
                    .getVolumeInstances().isEmpty()) {

                // Volumes
                Map<String, String> orchestratorVolumes = new HashMap<>();
                componentNodeInstance.getVolumeInstances().stream().filter(volumeInstance -> !volumeInstance.getHostPath().isEmpty())
                        .forEach(volumeInstance -> {

                            try {

                                orchestratorVolumes.put(volumeInstance.getHostPath(), volumeInstance.getDockerPath());

                            } catch (Exception e) {
                                e.printStackTrace();

                            }

                        });

                orchestratorComponentNodeInstance.setVolumes(orchestratorVolumes);

            }

            if (null != componentNodeInstance.getFlavorInstance()) {

                OrchestratorFlavor orchestratorFlavor = new OrchestratorFlavor();
                orchestratorFlavor.setRam(componentNodeInstance.getFlavorInstance().getRam());
                orchestratorFlavor.setvCPUs(componentNodeInstance.getFlavorInstance().getvCPUs());
                orchestratorFlavor.setStorage(componentNodeInstance.getFlavorInstance().getStorage());
                orchestratorComponentNodeInstance.setFlavor(orchestratorFlavor);

            }

            if (null != componentNodeInstance.getHealthCheckInstance()) {

                OrchestratorHealthCheck orchestratorHealthCheck = new OrchestratorHealthCheck();
                orchestratorHealthCheck
                        .setInterval(componentNodeInstance.getHealthCheckInstance().getInterval().toString());
                orchestratorHealthCheck.setArgs(
                        null != componentNodeInstance.getHealthCheckInstance().getArgs()
                                && !componentNodeInstance.getHealthCheckInstance().getArgs().isEmpty()
                                ? componentNodeInstance.getHealthCheckInstance().getArgs() : null);
                orchestratorHealthCheck.setHttpURL(
                        null != componentNodeInstance.getHealthCheckInstance().getHttpURL()
                                && !componentNodeInstance.getHealthCheckInstance().getHttpURL().isEmpty()
                                ? componentNodeInstance.getHealthCheckInstance().getHttpURL() : null);

                orchestratorComponentNodeInstance.setHealthCheck(orchestratorHealthCheck);

            }

            if (null != componentNodeInstance.getLocationInstances() && !componentNodeInstance
                    .getLocationInstances().isEmpty()) {

                List<OrchestratorLocation> locations = new ArrayList<>();

                componentNodeInstance.getLocationInstances().stream().forEach(locationInstance -> {

                    try {

                        OrchestratorLocation orchestratorLocation = new OrchestratorLocation();
                        orchestratorLocation.setRegion(locationInstance.getRegion());
                        orchestratorLocation.setCountry(null);
                        locations.add(orchestratorLocation);

                    } catch (Exception e) {
                        e.printStackTrace();

                    }

                });

                orchestratorComponentNodeInstance.setLocations(locations);
            }

            orchestratorComponentNodeInstance.setLoadBalancer(false);
            orchestratorComponentNodeInstance.setLambdaProxy(false);

            // TODO

            ComponentNodeInstanceStatus componentNodeInstanceStatus = new ComponentNodeInstanceStatus();
            componentNodeInstanceStatus.setReportedChange(
                    OrchestratorChangedStatusNotification.ChangeType.GraphStatusChange.name());
            componentNodeInstanceStatus.setLastModified(new Date());
            componentNodeInstanceStatus.setDateCreated(new Date());
            componentNodeInstanceStatus.setComponentNodeInstance(componentNodeInstance);
            componentNodeInstanceStatus.setMessage("Component is loading...");
            componentNodeInstanceStatus.setStatus("LOADING");
            componentNodeInstanceStatus.setApplicationInstance(existingApplicationInstance);
            componentNodeInstanceStatusDao.save(componentNodeInstanceStatus);

            services.put(orchestratorComponentNodeInstance.getComponentNodeInstanceID(),
                    orchestratorComponentNodeInstance);

        }
//    });

        logger.info("Services have been processed successfully!");

        orchestratorApplicationInstance.setProviderAuthenticationDetails(
                vimDescriptors.values().stream().collect(Collectors.toList()));
        orchestratorApplicationInstance.setServices(new ArrayList<>(services.values()));

        // Check if dependencies have to be updated to LB CNIs

        services.values().stream().forEach(service -> {

            try {

                if (!service.getLoadBalancer().booleanValue() && !service.getLambdaProxy().booleanValue()) {

                    if (null != service.getDependsOn() && !service.getDependsOn().isEmpty()) {

                        List<OrchestratorDependency> newDependencies = new ArrayList<>();

                        service.getDependsOn().stream().forEach(dependency -> {

                            try {

                                if (!updatedDependsOnComponentNodeInstanceURIs.isEmpty()
                                        && updatedDependsOnComponentNodeInstanceURIs
                                        .containsKey(dependency.getDependency())) {

                                    ComponentNodeInstance componentNodeInstance = componentNodeInstanceDao
                                            .findByHexID(
                                                    updatedDependsOnComponentNodeInstanceURIs.get(dependency.getDependency()))
                                            .get();

                                    if (newDependencies.stream().filter(dep -> dep.getDependency()
                                                    .equals(componentNodeInstance.getComponentNode().getHexID()))
                                            .collect(Collectors.toList()).isEmpty()) {

                                        OrchestratorDependency orchestratorDependency = new OrchestratorDependency();

                                        // TODO Convert component Node Instance HexID to Component Node HexID

                                        orchestratorDependency
                                                .setDependency(componentNodeInstance.getComponentNode().getHexID());
//                                        orchestratorDependency.setDependency(updatedDependsOnComponentNodeInstanceURIs.get(dependency.getDependency()));
                                        orchestratorDependency
                                                .setNetworkAttachmentPoint(dependency.getNetworkAttachmentPoint());
                                        orchestratorDependency.setVna(dependency.getVna());
                                        newDependencies.add(orchestratorDependency);

                                    }

                                } else {

                                    if (newDependencies.stream()
                                            .filter(dep -> dep.getDependency().equals(dependency.getDependency()))
                                            .collect(Collectors.toList()).isEmpty()) {

                                        // TODO Convert component Node Instance HexID to Component Node HexID

                                        ComponentNodeInstance servCNI = componentNodeInstanceDao
                                                .findById(Long.valueOf(service.getComponentNodeInstanceID())).get();

                                        if (null != servCNI && null != servCNI.getLoadBalancedBy()) {

                                            if (componentNodeInstanceDao.findByHexID(dependency.getDependency())
                                                    .isPresent()) {

                                                ComponentNodeInstance componentNodeInstance = componentNodeInstanceDao
                                                        .findByHexID(dependency.getDependency()).get();
                                                dependency
                                                        .setDependency(componentNodeInstance.getComponentNode().getHexID());

                                            } else {

                                                ComponentNode componentNode = componentNodeDao
                                                        .findByHexID(dependency.getDependency()).get();
                                                dependency.setDependency(componentNode.getHexID());

                                            }

                                        } else {

                                            ComponentNodeInstance componentNodeInstance = componentNodeInstanceDao
                                                    .findByHexID(dependency.getDependency()).get();
                                            dependency.setDependency(componentNodeInstance.getComponentNode().getHexID());

                                        }

                                        newDependencies.add(dependency);

                                    }
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        });

                        service.setDependsOn(newDependencies);

                    }
                }

                if (null != service.getEnvironmentalVariables() && !service.getEnvironmentalVariables()
                        .isEmpty()) {

                    Map<String, String> newEnvVars = new HashMap<>();

                    service.getEnvironmentalVariables().entrySet().stream().forEach(envVar -> {
                        try {

                            if (envVar.getValue().startsWith("@") || envVar.getValue().startsWith("#")) {

                                String value = envVar.getValue().substring(1);
                                if (value.equals("IPV4_PRIVATE") || value.equals("IPV6_PRIVATE") || value.equals("IPV4_PUBLIC")) {
                                    newEnvVars.put(envVar.getKey(), envVar.getValue());
                                } else {

                                    ComponentNodeInstance dependencyCNI = componentNodeInstanceDao
                                            .findByNameAndApplicationInstance(value, existingApplicationInstance).get();

                                    if (updatedDependsOnComponentNodeInstanceURIs.containsKey(dependencyCNI.getHexID())) {

                                        // TODO Convert component Node Instance HexID to Component Node HexID
                                        ComponentNodeInstance componentNodeInstance = componentNodeInstanceDao.findByHexID(
                                                updatedDependsOnComponentNodeInstanceURIs.get(dependencyCNI.getHexID())).get();
                                        newEnvVars.put(envVar.getKey(), envVar.getValue().charAt(0) + componentNodeInstance.getComponentNode().getHexID());

//                                    newEnvVars.put(envVar.getKey(), "@" + updatedDependsOnComponentNodeInstanceURIs.get(dependencyCNI.getHexID()));

                                    } else {

                                        // TODO Convert component Node Instance HexID to Component Node HexID
                                        ComponentNodeInstance componentNodeInstance = componentNodeInstanceDao
                                                .findByHexID(dependencyCNI.getHexID()).get();
                                        newEnvVars.put(envVar.getKey(),
                                                envVar.getValue().charAt(0) + componentNodeInstance.getComponentNode().getHexID());

//                                    newEnvVars.put(envVar.getKey(), "@" + dependencyCNI.getHexID());
                                    }
                                }
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
            }

        });

        logger.info("Changes have been processed successfully!");

        if (!mapOfNeededChanges.isEmpty()) {

            // Create Fake GraphLinks
            mapOfNeededChanges.entrySet().stream().forEach(entry -> {

                Long componentNodeInstanceID = entry.getKey();
                ComponentNodeInstance componentNodeInstance = componentNodeInstanceDao
                        .findById(componentNodeInstanceID).get();
                ComponentNodeInstance traefikComponentNodeInstance = entry.getValue();

                List<GraphLinkNodeInstance> graphLinkNodeInstances = graphLinkNodeInstanceDao
                        .findAllByApplicationInstanceAndComponentNodeInstanceTo(existingApplicationInstance,
                                componentNodeInstance);

                if (null != graphLinkNodeInstances && !graphLinkNodeInstances.isEmpty()) {

                    graphLinkNodeInstances.stream().forEach(graphLinkNodeInstance -> {

                        try {

                            if (null != graphLinkNodeInstance.getGraphLinkNode()) {

                                graphLinkNodeInstance.setComponentNodeInstanceTo(traefikComponentNodeInstance);
                                graphLinkNodeInstance.setLastModified(new Date());
                                graphLinkNodeInstanceDao.save(graphLinkNodeInstance);

                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    });

                }

            });

        }

        logger.info("Graph Link Node Instances have been processed successfully!");

        // Send orchestrator application instance to Orchestrator

        logger.info("Orchestrator AppInstance: " + new Gson().toJson(orchestratorApplicationInstance));

        HttpEntity entity = new HttpEntity(orchestratorApplicationInstance, null);

        try {

            ResponseEntity<String> responseEntity = restTemplate
                    .exchange(orchestratorURL + "/api/v1/deploy", HttpMethod.POST, entity, String.class);

            if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {

                logger.info("Response: " + responseEntity.getBody());

                JSONObject callbackJSON = new JSONObject(responseEntity.getBody());

                if (callbackJSON.getString("code").equals("SUCCESS")) {

                    // Update Status of Application Instance
                    isSuccess = true;

                }

            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.log(Level.SEVERE, e.getMessage(), e);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        //TODO send deployment topology into kafka topic
        try {
            String topologyAsString = objectMapper.writeValueAsString(orchestratorApplicationInstance);
            sender.sendOrchestratorDeploymentTopology(topologyAsString);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            logger.log(Level.SEVERE, e.getMessage(), e);
        }

        return isSuccess;

    }


    public static Slice requestPlacement(ApplicationInstance applicationInstance, SliceDAO sliceDAO,
            SliceConstraintSatisfactionDAO sliceConstraintSatisfactionDAO,
            SliceProviderDAO sliceProviderDAO, SlicePlacementDAO slicePlacementDAO,
            SlicePlacementAttachmentPointDAO slicePlacementAttachmentPointDAO,
            ApplicationInstanceDAO applicationInstanceDAO, ApplicationDAO applicationDAO,
            ComponentNodeInstanceDAO componentNodeInstanceDAO,
            ComponentNodeInstanceStatusDAO componentNodeInstanceStatusDAO,
            ComponentNodeDAO componentNodeDAO, ComponentDAO componentDAO,
            InterfaceInstanceDAO interfaceInstanceDAO, FlavorInstanceDAO flavorInstanceDAO,
            HealthCheckDAO healthCheckDAO, HealthCheckInstanceDAO healthCheckInstanceDAO,
            DeviceInstanceDAO deviceInstanceDAO, DeviceDAO deviceDAO,
            EnvironmentalVariableInstanceDAO environmentalVariableInstanceDAO,
            VolumeInstanceDAO volumeInstanceDAO, LocationInstanceDAO locationInstanceDAO,
            GraphLinkNodeInstanceDAO graphLinkNodeInstanceDAO, RequirementDAO requirementDAO,
            ProviderDAO providerDAO, ConstraintDAO constraintDAO, RuntimePolicyDAO runtimePolicyDAO,
            PluginInstanceDAO pluginInstanceDAO, PluginDAO pluginDAO,
            IDRuleSetInstanceDAO idRuleSetInstanceDAO, IDRuleSetDAO idRuleSetDAO,
            EntityManager entityManager, RestTemplate restTemplate, ObjectMapper objectMapper,
            String orchestratorURL, String uiURL, String consulURL, String consulURLIPv6, ElasticityService elasticityService,
            KafkaTopicConfig kafkaTopicConfig) {

        boolean isSuccess = false;

        // Initialize Slice Intent transfer object
        SliceIntent sliceIntent = new SliceIntent();
        sliceIntent.setApplicationInstanceID(applicationInstance.getApplicationInstanceID() + "");
        sliceIntent.setName(applicationInstance.getName());
        sliceIntent.setDescription(applicationInstance.getDescription());

        isSuccess = prepareDeploymentForPolicyDefined(sliceIntent, applicationInstance,
                applicationInstanceDAO, applicationDAO, componentNodeInstanceDAO,
                componentNodeInstanceStatusDAO, componentNodeDAO, componentDAO, interfaceInstanceDAO,
                environmentalVariableInstanceDAO, pluginInstanceDAO, pluginDAO, flavorInstanceDAO,
                healthCheckDAO, healthCheckInstanceDAO, deviceDAO, deviceInstanceDAO, locationInstanceDAO,
                volumeInstanceDAO, graphLinkNodeInstanceDAO, requirementDAO, constraintDAO,
                runtimePolicyDAO, idRuleSetInstanceDAO, idRuleSetDAO, entityManager, restTemplate,
                orchestratorURL, uiURL, consulURL, consulURLIPv6, elasticityService, kafkaTopicConfig);

        // Receive synchronous response with the slice

        if (isSuccess) {

            // TODO Call orchestrator to receive slice

            // TODO FAKE
            try {

                Slice nSlice = new Slice();
                nSlice.setApplicationInstance(applicationInstance);
                nSlice.setDateCreated(new Date());
                nSlice.setLastModified(new Date());
                nSlice.setHexID(Util.createRandomHEXString());
                nSlice.setUser(applicationInstance.getUser());
                String orchestratorApplicationInstanceSTR = objectMapper
                        .writeValueAsString(sliceIntent.getOrchestratorApplicationInstance());
                nSlice.setOrchestratorApplicationInstance(orchestratorApplicationInstanceSTR);
                sliceDAO.save(nSlice);

                List<Constraint> constraints = constraintDAO
                        .findAllByApplicationInstance(applicationInstance, null).getContent();

                // Register application instance constraint satisfactions
                if (null != constraints && !constraints.isEmpty()) {

                    constraints.forEach(constraint -> {

                        SliceConstraintSatisfaction sliceConstraintSatisfaction = new SliceConstraintSatisfaction();
                        sliceConstraintSatisfaction.setSlice(nSlice);

                        sliceConstraintSatisfaction.setConstraint(constraint);
                        sliceConstraintSatisfaction.setSatisfied(true);
                        sliceConstraintSatisfaction.setDateCreated(new Date());
                        sliceConstraintSatisfaction.setLastModified(new Date());

                        sliceConstraintSatisfactionDAO.save(sliceConstraintSatisfaction);

                    });

                }

                Provider provider = providerDAO.findByName("UBIDELL").get();

                SliceProvider sliceProvider = new SliceProvider();
                sliceProvider.setSlice(nSlice);
                sliceProvider.setProvider(provider);
                sliceProvider.setDateCreated(new Date());
                sliceProvider.setLastModified(new Date());
                sliceProviderDAO.save(sliceProvider);

                List<GraphLinkNodeInstance> graphLinkNodeInstances = graphLinkNodeInstanceDAO
                        .findAllByApplicationInstanceOrderByDateCreatedDesc(applicationInstance, null)
                        .getContent();

                // Register SlicePlacement
                if (null != sliceIntent.getOrchestratorApplicationInstance().getServices() && !sliceIntent
                        .getOrchestratorApplicationInstance().getServices().isEmpty()) {

                    sliceIntent.getOrchestratorApplicationInstance().getServices().stream()
                            .forEach(service -> {

                                SlicePlacement slicePlacement = new SlicePlacement();
                                slicePlacement.setSlice(nSlice);
                                slicePlacement.setComponentNodeInstance(componentNodeInstanceDAO
                                        .findById(Long.valueOf(service.getComponentNodeInstanceID())).get());
                                slicePlacement.setProvider(providerDAO.findByName("UBIDELL").get());
                                slicePlacement.setFlavorID(UUID.randomUUID().toString());
                                slicePlacement.setDateCreated(new Date());
                                slicePlacement.setLastModified(new Date());
                                slicePlacementDAO.save(slicePlacement);

                                if (null != service.getDependsOn() && !service.getDependsOn().isEmpty()) {

                                    service.getDependsOn().stream().forEach(dependency -> {

                                        SlicePlacementAttachmentPoint slicePlacementAttachmentPoint = new SlicePlacementAttachmentPoint();
                                        slicePlacementAttachmentPoint.setSlicePlacement(slicePlacement);
                                        slicePlacementAttachmentPoint.setGraphLinkNodeInstance(null);
                                        slicePlacementAttachmentPoint
                                                .setAttachmentPoint(providerDAO.findByName("UBIDELL").get().getNetworkID());
                                        slicePlacementAttachmentPoint.setDateCreated(new Date());
                                        slicePlacementAttachmentPoint.setLastModified(new Date());
                                        slicePlacementAttachmentPointDAO.save(slicePlacementAttachmentPoint);

                                    });

                                }

                            });


                }

                return nSlice;

            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }

        }

        return null;

    }

    public static boolean executePolicyDefinedDeployment(ApplicationInstance applicationInstance,
            Slice slice, SliceDAO sliceDAO, SliceProviderDAO sliceProviderDAO,
            SlicePlacementDAO slicePlacementDAO,
            SlicePlacementAttachmentPointDAO slicePlacementAttachmentPointDAO,
            ComponentNodeInstanceDAO componentNodeInstanceDAO, ProviderDAO providerDAO,
            ProviderTypeDAO providerTypeDAO, GraphLinkNodeInstanceDAO graphLinkNodeInstanceDAO,
            EntityManager entityManager, ObjectMapper objectMapper, RestTemplate restTemplate,
            String orchestratorURL) {

        boolean isSuccess = false;

        try {

            OrchestratorApplicationInstance orchestratorApplicationInstance = objectMapper
                    .readValue(slice.getOrchestratorApplicationInstance(),
                            OrchestratorApplicationInstance.class);

            if (null != orchestratorApplicationInstance) {

                List<SliceProvider> sliceProviders = sliceProviderDAO.findAllBySlice(slice);

                // Check if we have more than one VIMs
                if (null != sliceProviders && !sliceProviders.isEmpty()) {

                    List<OrchestratorProviderAuthenticationDetails> vimDescriptors = new ArrayList<>();

                    sliceProviders.stream().forEach(sliceProvider -> {

                        Provider vim = providerDAO.findById(sliceProvider.getProvider().getProviderID()).get();

                        OrchestratorProviderAuthenticationDetails orchestratorProviderAuthenticationDetails = new OrchestratorProviderAuthenticationDetails();
                        orchestratorProviderAuthenticationDetails.setId(String.valueOf(vim.getProviderID()));
                        orchestratorProviderAuthenticationDetails
                                .setAdapterType(vim.getProviderType().getName());
                        orchestratorProviderAuthenticationDetails
                                .setAdapterImplementation(vim.getProviderType().getAdapterImplementation());
                        orchestratorProviderAuthenticationDetails.setDomain(vim.getDomain());
                        orchestratorProviderAuthenticationDetails.setProject(vim.getProject());
                        orchestratorProviderAuthenticationDetails.setEndpoint(vim.getEndpoint());
                        orchestratorProviderAuthenticationDetails.setUsername(vim.getUsername());
                        orchestratorProviderAuthenticationDetails.setPassword(vim.getPassword());
                        orchestratorProviderAuthenticationDetails.setPrivateKey(null);
                        orchestratorProviderAuthenticationDetails.setPublicKey(null);
                        orchestratorProviderAuthenticationDetails.setImageID(
                                null != vim.getImageID() && !vim.getImageID().isEmpty() ? vim.getImageID() : null);
                        orchestratorProviderAuthenticationDetails.setNetworkID(
                                null != vim.getNetworkID() && !vim.getNetworkID().isEmpty() ? vim.getNetworkID()
                                        : null);
                        orchestratorProviderAuthenticationDetails.setPublicNetwork(vim.getPublicNetwork());

                        // TODO Handle also METADATA

                        vimDescriptors.add(orchestratorProviderAuthenticationDetails);


                    });

                    orchestratorApplicationInstance.setProviderAuthenticationDetails(vimDescriptors);

                }

                orchestratorApplicationInstance.getServices().stream().forEach(service -> {

                    ComponentNodeInstance componentNodeInstance = componentNodeInstanceDAO
                            .findById(Long.valueOf(service.getComponentNodeInstanceID())).get();

                    SlicePlacement slicePlacement = slicePlacementDAO
                            .findBySliceAndComponentNodeInstance(slice, componentNodeInstance).get();

                    List<SlicePlacementAttachmentPoint> attachmentPoints = slicePlacementAttachmentPointDAO
                            .findAllBySlicePlacement(slicePlacement);

                    // Handle attachment points
                    String vimID = String.valueOf(slicePlacement.getProvider().getProviderID());
                    service.setProviderID(vimID);

                    service.getFlavor().setId(slicePlacement.getFlavorID());

                    componentNodeInstance.setProvider(slicePlacement.getProvider());
                    componentNodeInstance.setLastModified(new Date());
                    componentNodeInstanceDAO.save(componentNodeInstance);

                    List<GraphLinkNodeInstance> graphLinkNodeInstances = graphLinkNodeInstanceDAO
                            .findAllByApplicationInstanceAndComponentNodeInstanceFrom(applicationInstance,
                                    componentNodeInstance);

                    if (null != attachmentPoints && !attachmentPoints.isEmpty()) {

                        attachmentPoints.stream().forEach(attachmentPoint -> {

                            service.getDependsOn().stream().forEach(dependency -> {

                                if (!graphLinkNodeInstances.stream().filter(
                                                graphLinkNodeInstance -> graphLinkNodeInstance.getComponentNodeInstanceTo()
                                                        .getComponentNode().getHexID().equals(dependency.getDependency()))
                                        .collect(Collectors.toList()).isEmpty()) {

                                    dependency.setNetworkAttachmentPoint(attachmentPoint.getAttachmentPoint());

                                }

                            });


                        });

                    }

                });

                // Send orchestrator application instance to Orchestrator

                logger.info("Orchestrator AppInstance: " + new GsonBuilder().disableHtmlEscaping().create()
                        .toJson(orchestratorApplicationInstance));

                slice.setOrchestratorApplicationInstance(
                        objectMapper.writeValueAsString(orchestratorApplicationInstance));
                slice.setLastModified(new Date());
                sliceDAO.save(slice);

                HttpEntity entity = new HttpEntity(orchestratorApplicationInstance, null);

                ResponseEntity<String> responseEntity = restTemplate
                        .exchange(orchestratorURL + "/api/v1/deploy", HttpMethod.POST, entity, String.class);

                if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {

                    logger.info("Response: " + responseEntity.getBody());

                    JSONObject callbackJSON = new JSONObject(responseEntity.getBody());

                    if (callbackJSON.getString("code").equals("SUCCESS")) {

                        // Update Status of Application Instance
                        isSuccess = true;

                    }

                }

            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.log(Level.SEVERE, e.getMessage(), e);
        }

        return isSuccess;

    }

    public static boolean prepareDeploymentForPolicyDefined(SliceIntent sliceIntent,
            ApplicationInstance applicationInstance, ApplicationInstanceDAO applicationInstanceDAO,
            ApplicationDAO applicationDAO, ComponentNodeInstanceDAO componentNodeInstanceDAO,
            ComponentNodeInstanceStatusDAO componentNodeInstanceStatusDAO,
            ComponentNodeDAO componentNodeDAO, ComponentDAO componentDAO,
            InterfaceInstanceDAO interfaceInstanceDAO,
            EnvironmentalVariableInstanceDAO environmentalVariableInstanceDAO,
            PluginInstanceDAO pluginInstanceDAO, PluginDAO pluginDAO, FlavorInstanceDAO flavorInstanceDAO,
            HealthCheckDAO healthCheckDAO, HealthCheckInstanceDAO healthCheckInstanceDAO,
            DeviceDAO deviceDAO, DeviceInstanceDAO deviceInstanceDAO,
            LocationInstanceDAO locationInstanceDAO, VolumeInstanceDAO volumeInstanceDAO,
            GraphLinkNodeInstanceDAO graphLinkNodeInstanceDAO, RequirementDAO requirementDAO,
            ConstraintDAO constraintDAO, RuntimePolicyDAO runtimePolicyDAO,
            IDRuleSetInstanceDAO idRuleSetInstanceDAO, IDRuleSetDAO idRuleSetDAO,
            EntityManager entityManager, RestTemplate restTemplate, String orchestratorURL, String uiURL,
            String consulURL, String consulURLIPv6, ElasticityService elasticityService, KafkaTopicConfig kafkaTopicConfig) {

        boolean isSuccess = true;

        Map<Long, ComponentNodeInstance> mapOfNeededChanges = new HashMap<>();

        Map<Long, ComponentNodeInstance> lambdaProxyLoadBalancers = new HashMap<>();
        Map<Long, OrchestratorComponentNodeInstance> lambdaProxyOrchestratorLoadBalancers = new HashMap<>();

        /////////////

        Map<String, String> updatedDependsOnComponentNodeInstanceURIs = new HashMap<>();

        /////////////

        Application existingApplication = applicationDAO
                .findById(applicationInstance.getApplication().getId()).get();

        // Convert ApplicationInstance to OrchestratorApplicationInstance
        OrchestratorApplicationInstance orchestratorApplicationInstance = new OrchestratorApplicationInstance();
        orchestratorApplicationInstance.setCallbackURL(
                uiURL + "/api/v1/callback/deployment/" + applicationInstance.getApplicationInstanceID());
        orchestratorApplicationInstance.setGraphInstanceHexID(applicationInstance.getHexID());
        orchestratorApplicationInstance
                .setGraphInstanceID(applicationInstance.getApplicationInstanceID() + "");
        orchestratorApplicationInstance.setGraphInstanceName(applicationInstance.getName());
        orchestratorApplicationInstance.setGraphHexID(existingApplication.getHexID());
        orchestratorApplicationInstance.setGraphID(existingApplication.getId() + "");
        orchestratorApplicationInstance.setGraphName(existingApplication.getName());
        orchestratorApplicationInstance.setIpv6Enabled(applicationInstance.getOverlay());

        OrchestratorKafkaConfig orchestratorKafkaConfig = new OrchestratorKafkaConfig();
        orchestratorKafkaConfig.setKafkaServer(kafkaTopicConfig.getKafkaServer());
        orchestratorKafkaConfig.setAgentIdsAlertTopic(kafkaTopicConfig.getAgentIdsAlertTopic());
        orchestratorKafkaConfig.setAgentIdsConfigTopic(kafkaTopicConfig.getAgentIdsConfigTopic());
        orchestratorKafkaConfig.setAgentIpsConfigTopic(kafkaTopicConfig.getAgentIpsConfigTopic());
        orchestratorKafkaConfig.setAgentSecurityConfigResultTopic(kafkaTopicConfig.getAgentSecurityConfigResultTopic());
        orchestratorKafkaConfig.setAgentSecurityConfigTopic(kafkaTopicConfig.getAgentSecurityConfigTopic());
        orchestratorKafkaConfig.setAgentSocConfigTopic(kafkaTopicConfig.getAgentSocConfigTopic());
        orchestratorKafkaConfig.setOrchestratorSecurityConfigResultTopic(kafkaTopicConfig.getOrchestratorSecurityConfigResultTopic());

        orchestratorApplicationInstance.setOrchestratorKafkaConfig(orchestratorKafkaConfig);

        Map<String, OrchestratorProviderAuthenticationDetails> vimDescriptors = new HashMap<>();

        // Component Node Instances
        Map<String, OrchestratorComponentNodeInstance> services = new HashMap<>();

        List<ComponentNodeInstance> existingComponentNodeInstances = new ArrayList<>(
                applicationInstance.getComponentNodeInstances());

        for (ComponentNodeInstance componentNodeInstance : existingComponentNodeInstances) {
//    existingComponentNodeInstances.stream().forEach(componentNodeInstance -> {

            OrchestratorComponentNodeInstance orchestratorComponentNodeInstance = new OrchestratorComponentNodeInstance();

            // Default deployment
            OrchestratorProviderAuthenticationDetails orchestratorProviderAuthenticationDetails = new OrchestratorProviderAuthenticationDetails();
            orchestratorProviderAuthenticationDetails
                    .setId(componentNodeInstance.getProvider().getProviderID() + "");
            orchestratorProviderAuthenticationDetails
                    .setAdapterType(componentNodeInstance.getProvider().getProviderType().getName());
            orchestratorProviderAuthenticationDetails.setProxy(
                    null != componentNodeInstance.getProvider().getProxy() ? componentNodeInstance
                            .getProvider().getProxy() : null);
            orchestratorProviderAuthenticationDetails.setAdapterImplementation(
                    componentNodeInstance.getProvider().getProviderType().getAdapterImplementation());
            orchestratorProviderAuthenticationDetails.setMeshIdentifier(
                    null != componentNodeInstance.getProvider().getMeshIdentifier() ? componentNodeInstance
                            .getProvider().getMeshIdentifier() : null);
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
                    null != componentNodeInstance.getProvider().getImageID() && !componentNodeInstance
                            .getProvider().getImageID().isEmpty() ? componentNodeInstance.getProvider()
                            .getImageID() : null);
            orchestratorProviderAuthenticationDetails.setNetworkID(
                    null != componentNodeInstance.getProvider().getNetworkID() && !componentNodeInstance
                            .getProvider().getNetworkID().isEmpty() ? componentNodeInstance.getProvider()
                            .getNetworkID() : null);
            orchestratorProviderAuthenticationDetails.setPublicNetwork(
                    null != componentNodeInstance.getProvider().getPublicNetwork() ? componentNodeInstance
                            .getProvider().getPublicNetwork() : null);

            if (!vimDescriptors.containsKey(componentNodeInstance.getProvider().getProviderID() + "")) {
                vimDescriptors.put(componentNodeInstance.getProvider().getProviderID() + "",
                        orchestratorProviderAuthenticationDetails);
            }

            orchestratorComponentNodeInstance
                    .setProviderID(orchestratorProviderAuthenticationDetails.getId());
            orchestratorComponentNodeInstance
                    .setComponentNodeInstanceHexID(componentNodeInstance.getHexID() + "");
            orchestratorComponentNodeInstance
                    .setComponentNodeInstanceID(componentNodeInstance.getComponentNodeInstanceID() + "");
            orchestratorComponentNodeInstance
                    .setComponentNodeInstanceName(componentNodeInstance.getName());
            orchestratorComponentNodeInstance
                    .setComponentNodeHexID(componentNodeInstance.getComponentNode().getHexID() + "");
            orchestratorComponentNodeInstance
                    .setComponentNodeID(componentNodeInstance.getComponentNode().getComponentNodeID() + "");
            orchestratorComponentNodeInstance
                    .setComponentNodeName(componentNodeInstance.getComponentNode().getName());
            orchestratorComponentNodeInstance
                    .setMinimumWorkers(componentNodeInstance.getMinimumWorkers());
            orchestratorComponentNodeInstance
                    .setMaximumWorkers(componentNodeInstance.getMaximumWorkers());
            orchestratorComponentNodeInstance.setStatusIDS(
                    null != componentNodeInstance.getStatusIDS() && componentNodeInstance.getStatusIDS()
                            .booleanValue());
            orchestratorComponentNodeInstance.setStatusIPS(
                    null != componentNodeInstance.getStatusIPS() && componentNodeInstance.getStatusIPS()
                            .booleanValue());
            orchestratorComponentNodeInstance.setStatusSoc(
                    null != componentNodeInstance.getStatusSOC() && componentNodeInstance.getStatusSOC()
                            .booleanValue());

            orchestratorComponentNodeInstance.setNetworkModeHost(
                    null != componentNodeInstance.getNetworkModeHost() && componentNodeInstance.getNetworkModeHost()
                            .booleanValue());
            orchestratorComponentNodeInstance.setPrivilege(
                    null != componentNodeInstance.getPrivilege() && componentNodeInstance.getPrivilege()
                            .booleanValue());
            orchestratorComponentNodeInstance.setHostname(
                    null != componentNodeInstance.getHostname() && !componentNodeInstance.getHostname()
                            .isEmpty() ? componentNodeInstance.getHostname() : null);
            orchestratorComponentNodeInstance.setDnsEntry(
                    null != componentNodeInstance.getDnsEntry() && !componentNodeInstance.getDnsEntry()
                            .isEmpty() ? componentNodeInstance.getDnsEntry() : null);
            orchestratorComponentNodeInstance.setSharedMemorySize(
                    null != componentNodeInstance.getSharedMemorySize() && !componentNodeInstance.getSharedMemorySize()
                            .isEmpty() ? componentNodeInstance.getSharedMemorySize() : null);
            orchestratorComponentNodeInstance.setDockerUsername(
                    null != componentNodeInstance.getComponentNode().getComponent().getDockerUsername()
                            && !componentNodeInstance.getComponentNode().getComponent().getDockerUsername()
                            .isEmpty() ? componentNodeInstance.getComponentNode().getComponent().getDockerUsername() : null);
            orchestratorComponentNodeInstance.setDockerPassword(
                    null != componentNodeInstance.getComponentNode().getComponent().getDockerPassword()
                            && !componentNodeInstance.getComponentNode().getComponent().getDockerPassword()
                            .isEmpty() ? componentNodeInstance.getComponentNode().getComponent().getDockerPassword() : null);

            orchestratorComponentNodeInstance.setUlimitMemlockSoft(componentNodeInstance.getComponentNode().getComponent().getUlimitMemlockSoft());
            orchestratorComponentNodeInstance.setUlimitMemlockHard(componentNodeInstance.getComponentNode().getComponent().getUlimitMemlockHard());
            orchestratorComponentNodeInstance.setDockerExecutionUser(componentNodeInstance.getComponentNode().getComponent().getDockerExecutionUser());

            if (null != componentNodeInstance.getCapabilityAdds() && !componentNodeInstance.getCapabilityAdds().isEmpty()) {
                Collection<String> newCapabilities = new ArrayList<>();
                Collection<Component.CapabilityAdd> exCapabilities = componentNodeInstance.getCapabilityAdds();
                exCapabilities.stream().forEach(capability -> {
                    newCapabilities.add(capability.getFriendlyName());
                });

                orchestratorComponentNodeInstance.setCapabilityAdds(newCapabilities);
            } else {
                orchestratorComponentNodeInstance.setCapabilityAdds(null);
            }
            if (null != componentNodeInstance.getCapabilityDrops() && !componentNodeInstance.getCapabilityDrops().isEmpty()) {
                Collection<String> newCapabilities = new ArrayList<>();
                Collection<Component.CapabilityDrop> exCapabilities = componentNodeInstance.getCapabilityDrops();
                exCapabilities.stream().forEach(capability -> {
                    newCapabilities.add(capability.getFriendlyName());
                });

                orchestratorComponentNodeInstance.setCapabilityDrops(newCapabilities);
            } else {
                orchestratorComponentNodeInstance.setCapabilityDrops(null);
            }

            orchestratorComponentNodeInstance.setCommand(
                    null != componentNodeInstance.getCommand() && !componentNodeInstance.getCommand()
                            .isEmpty() ? Arrays.asList(componentNodeInstance.getCommand()) : null);

            orchestratorComponentNodeInstance
                    .setImage(componentNodeInstance.getComponentNode().getComponent().getDockerImage());
            orchestratorComponentNodeInstance
                    .setRegistry(componentNodeInstance.getComponentNode().getComponent().getDockerRegistry());

            if (null != componentNodeInstance.getSshKey()) {
                orchestratorComponentNodeInstance.setSshKey(componentNodeInstance.getSshKey().getSshKey());
            } else {
                orchestratorComponentNodeInstance.setSshKey(null);
            }

            //TODO Astrid security enablers
            if (componentNodeInstance.getSecurityEnablers() != null && !componentNodeInstance.getSecurityEnablers().isEmpty()) {
                orchestratorComponentNodeInstance.setHasEnableSecurity(true);
                orchestratorComponentNodeInstance.setProduceHashes(
                        componentNodeInstance.getSecurityEnablers().contains(SecurityEnablers.CONFIGURATION_INTEGRITY_VERIFICATION) ||
                                componentNodeInstance.getSecurityEnablers().contains(SecurityEnablers.RUNTIME_FILE_INTEGRITY));
            } else {
                orchestratorComponentNodeInstance.setHasEnableSecurity(false);
            }

            // Scaling
            OrchestratorElasticity orchestratorElasticity = new OrchestratorElasticity();
            orchestratorElasticity
                    .setProfile(componentNodeInstance.getComponentNode().getComponent().getElasticityController());
            orchestratorElasticity.setType(
                    componentNodeInstance.getComponentNode().getComponent().getElasticityController()
                            .equals("LAMBDA_FUNCTION") ? componentNodeInstance
                            .getComponentNode().getComponent().getElasticityControllerMode() : null);
            orchestratorComponentNodeInstance.setMonitoringElasticity(orchestratorElasticity);

            // If CNI requires elasticity add the appropriate controller
            ElasticityFrameworkBackend elasticityAdapter = elasticityService.fetchElasticityBackendAdapter(componentNodeInstance);
            if (elasticityAdapter != null) {
                logger.info("Component: " + componentNodeInstance.getComponentNode().getComponent().getName() + " needs elasticity");

                ElasticityObjects elasticityObjects = new ElasticityObjects();
                elasticityObjects.setServices(services);
                elasticityObjects.setMapOfNeededChanges(mapOfNeededChanges);
                elasticityObjects.setApplicationInstance(applicationInstance);
                elasticityObjects.setWorkerComponentNodeInstance(componentNodeInstance);
                elasticityObjects.setVimID(orchestratorProviderAuthenticationDetails.getId());

                elasticityObjects = elasticityAdapter.configureElasticityController(elasticityObjects);

                String elasticityControllerHexID = elasticityObjects.getElasticityController().getHexID();
                services = elasticityObjects.getServices();
                mapOfNeededChanges = elasticityObjects.getMapOfNeededChanges();
//        existingApplicationInstance = elasticityObjects.getApplicationInstance();
                componentNodeInstance = elasticityObjects.getWorkerComponentNodeInstance();

                if (!updatedDependsOnComponentNodeInstanceURIs
                        .containsKey(componentNodeInstance.getHexID())) {
                    updatedDependsOnComponentNodeInstanceURIs
                            .put(componentNodeInstance.getHexID(), elasticityControllerHexID);
                }

                logger.info("Elasticity controller for: " + componentNodeInstance.getName() + " has been added");
                Optional<ComponentNodeInstance> tempComponentNodeInstanceOP = componentNodeInstanceDAO.findByHexID(componentNodeInstance.getHexID());
                componentNodeInstance = tempComponentNodeInstanceOP.get();

            }

            ComponentNodeInstance updatedInterfacesComponentNodeInstance = componentNodeInstance;

            if (null != componentNodeInstance.getLoadBalancedBy()) {
                orchestratorComponentNodeInstance.setBalancedByComponentNodeHexID(componentNodeInstance.getLoadBalancedBy().getComponentNode().getHexID());
            }

            // Required Interfaces
            if (null != applicationInstance.getGraphLinkNodeInstances() && !applicationInstance
                    .getGraphLinkNodeInstances().isEmpty()) {

                List<OrchestratorDependency> dependsOn = new ArrayList<>();

                applicationInstance.getGraphLinkNodeInstances().stream()
                        .filter(graphLinkNodeInstance -> graphLinkNodeInstance.getComponentNodeInstanceFrom()
                                .getComponentNodeInstanceID()
                                .equals(updatedInterfacesComponentNodeInstance.getComponentNodeInstanceID()))
                        .forEach(graphLinkNodeInstance -> {

                            try {

                                if (dependsOn.stream().filter(dep -> dep.getDependency()
                                                .equals(graphLinkNodeInstance.getComponentNodeInstanceTo().getHexID()))
                                        .collect(Collectors.toList()).isEmpty()) {

                                    OrchestratorDependency orchestratorDependency = new OrchestratorDependency();
                                    orchestratorDependency
                                            .setDependency(graphLinkNodeInstance.getComponentNodeInstanceTo().getHexID());
                                    orchestratorDependency.setNetworkAttachmentPoint(
                                            orchestratorProviderAuthenticationDetails.getNetworkID());
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
            if (null != updatedInterfacesComponentNodeInstance.getInterfaceInstances() && !updatedInterfacesComponentNodeInstance
                    .getInterfaceInstances().isEmpty()) {

                List<OrchestratorPort> ports = new ArrayList<>();

                updatedInterfacesComponentNodeInstance.getInterfaceInstances().stream().forEach(interfaceInstance -> {

                    try {

                        OrchestratorPort orchestratorPort = new OrchestratorPort();
                        orchestratorPort.setTarget(interfaceInstance.getInterfaceObj().getPort());
                        orchestratorPort.setPublished(interfaceInstance.getPort());
                        orchestratorPort
                                .setProtocol(interfaceInstance.getInterfaceObj().getTransmissionProtocol());
                        orchestratorPort.setVna(interfaceInstance.getInterfaceObj().getVna());
                        orchestratorPort.setNetworkAttachmentPoint(
                                orchestratorProviderAuthenticationDetails.getNetworkID());
                        orchestratorPort.setType(interfaceInstance.getInterfaceType());
                        ports.add(orchestratorPort);

                    } catch (Exception e) {
                        e.printStackTrace();

                    }

                });

                orchestratorComponentNodeInstance.setPorts(ports);

            }

            if (null != componentNodeInstance.getEnvironmentalVariableInstances()
                    && !componentNodeInstance.getEnvironmentalVariableInstances().isEmpty()) {

                // Environmental Variables
                Map<String, String> orchestratorEnvironmentalVariables = new HashMap<>();
                componentNodeInstance.getEnvironmentalVariableInstances().stream()
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

//                        }

                            } catch (Exception e) {
                                e.printStackTrace();

                            }

                        });

                orchestratorComponentNodeInstance
                        .setEnvironmentalVariables(orchestratorEnvironmentalVariables);

            }

            if (null != componentNodeInstance.getPluginInstances() && !componentNodeInstance
                    .getPluginInstances().isEmpty()) {

                List<OrchestratorPlugin> orchestratorPlugins = new ArrayList<>();

                componentNodeInstance.getPluginInstances().stream().forEach(pluginInstance -> {

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
                            null != plugin.getEndpoint() && !plugin.getEndpoint().isEmpty() ? plugin.getEndpoint()
                                    : null);
                    orchestratorPlugin.setDisabledPlugin(pluginInstance.getDeletedPlugin().booleanValue());
                    orchestratorPlugins.add(orchestratorPlugin);

                });

                orchestratorComponentNodeInstance.setPlugins(orchestratorPlugins);

            }

            if (null != componentNodeInstance.getDeviceInstances() && !componentNodeInstance
                    .getDeviceInstances().isEmpty()) {

                // Devices
                Map<String, String> orchestratorDevices = new HashMap<>();
                componentNodeInstance.getDeviceInstances().stream().forEach(deviceInstance -> {

                    try {

                        orchestratorDevices.put(deviceInstance.getKey(), deviceInstance.getValue());

                    } catch (Exception e) {
                        e.printStackTrace();

                    }

                });

                orchestratorComponentNodeInstance.setDevices(orchestratorDevices);

            }
            if (null != componentNodeInstance.getVolumeInstances() && !componentNodeInstance
                    .getVolumeInstances().isEmpty()) {

                // Volumes
                Map<String, String> orchestratorVolumes = new HashMap<>();
                componentNodeInstance.getVolumeInstances().stream().filter(volumeInstance -> !volumeInstance.getHostPath().isEmpty())
                        .forEach(volumeInstance -> {

                            try {

                                orchestratorVolumes.put(volumeInstance.getHostPath(), volumeInstance.getDockerPath());

                            } catch (Exception e) {
                                e.printStackTrace();

                            }

                        });

                orchestratorComponentNodeInstance.setVolumes(orchestratorVolumes);

            }

            if (null != componentNodeInstance.getFlavorInstance()) {

                OrchestratorFlavor orchestratorFlavor = new OrchestratorFlavor();
                orchestratorFlavor.setRam(componentNodeInstance.getFlavorInstance().getRam());
                orchestratorFlavor.setvCPUs(componentNodeInstance.getFlavorInstance().getvCPUs());
                orchestratorFlavor.setStorage(componentNodeInstance.getFlavorInstance().getStorage());
                orchestratorComponentNodeInstance.setFlavor(orchestratorFlavor);

            }

            if (null != componentNodeInstance.getHealthCheckInstance()) {

                OrchestratorHealthCheck orchestratorHealthCheck = new OrchestratorHealthCheck();
                orchestratorHealthCheck
                        .setInterval(componentNodeInstance.getHealthCheckInstance().getInterval().toString());
                orchestratorHealthCheck.setArgs(
                        null != componentNodeInstance.getHealthCheckInstance().getArgs()
                                && !componentNodeInstance.getHealthCheckInstance().getArgs().isEmpty()
                                ? componentNodeInstance.getHealthCheckInstance().getArgs() : null);
                orchestratorHealthCheck.setHttpURL(
                        null != componentNodeInstance.getHealthCheckInstance().getHttpURL()
                                && !componentNodeInstance.getHealthCheckInstance().getHttpURL().isEmpty()
                                ? componentNodeInstance.getHealthCheckInstance().getHttpURL() : null);

                orchestratorComponentNodeInstance.setHealthCheck(orchestratorHealthCheck);

            }

            if (null != componentNodeInstance.getLocationInstances() && !componentNodeInstance
                    .getLocationInstances().isEmpty()) {

                List<OrchestratorLocation> locations = new ArrayList<>();

                componentNodeInstance.getLocationInstances().stream().forEach(locationInstance -> {

                    try {

                        OrchestratorLocation orchestratorLocation = new OrchestratorLocation();
                        orchestratorLocation.setRegion(locationInstance.getRegion());
                        orchestratorLocation.setCountry(null);
                        locations.add(orchestratorLocation);

                    } catch (Exception e) {
                        e.printStackTrace();

                    }

                });

                orchestratorComponentNodeInstance.setLocations(locations);
            }

            orchestratorComponentNodeInstance.setLoadBalancer(false);
            orchestratorComponentNodeInstance.setLambdaProxy(false);

            // TODO

            ComponentNodeInstanceStatus componentNodeInstanceStatus = new ComponentNodeInstanceStatus();
            componentNodeInstanceStatus.setReportedChange(
                    OrchestratorChangedStatusNotification.ChangeType.GraphStatusChange.name());
            componentNodeInstanceStatus.setLastModified(new Date());
            componentNodeInstanceStatus.setDateCreated(new Date());
            componentNodeInstanceStatus.setComponentNodeInstance(componentNodeInstance);
            componentNodeInstanceStatus.setMessage("Component is loading...");
            componentNodeInstanceStatus.setStatus("LOADING");
            componentNodeInstanceStatus.setApplicationInstance(applicationInstance);
            componentNodeInstanceStatusDAO.save(componentNodeInstanceStatus);

            services.put(orchestratorComponentNodeInstance.getComponentNodeInstanceID(),
                    orchestratorComponentNodeInstance);

//    });
        }

        orchestratorApplicationInstance.setProviderAuthenticationDetails(
                vimDescriptors.values().stream().collect(Collectors.toList()));
        orchestratorApplicationInstance.setServices(new ArrayList<>(services.values()));

        // Check if dependencies have to be updated to LB CNIs

        services.values().stream().forEach(service -> {

            try {

                if (!service.getLoadBalancer().booleanValue() && !service.getLambdaProxy().booleanValue()) {

                    if (null != service.getDependsOn() && !service.getDependsOn().isEmpty()) {

                        List<OrchestratorDependency> newDependencies = new ArrayList<>();

                        service.getDependsOn().stream().forEach(dependency -> {

                            try {

                                if (!updatedDependsOnComponentNodeInstanceURIs.isEmpty()
                                        && updatedDependsOnComponentNodeInstanceURIs
                                        .containsKey(dependency.getDependency())) {

                                    ComponentNodeInstance componentNodeInstance = componentNodeInstanceDAO
                                            .findByHexID(
                                                    updatedDependsOnComponentNodeInstanceURIs.get(dependency.getDependency()))
                                            .get();

                                    if (newDependencies.stream().filter(dep -> dep.getDependency()
                                                    .equals(componentNodeInstance.getComponentNode().getHexID()))
                                            .collect(Collectors.toList()).isEmpty()) {

                                        OrchestratorDependency orchestratorDependency = new OrchestratorDependency();

                                        // TODO Convert component Node Instance HexID to Component Node HexID

                                        orchestratorDependency
                                                .setDependency(componentNodeInstance.getComponentNode().getHexID());
//                                        orchestratorDependency.setDependency(updatedDependsOnComponentNodeInstanceURIs.get(dependency.getDependency()));
                                        orchestratorDependency
                                                .setNetworkAttachmentPoint(dependency.getNetworkAttachmentPoint());
                                        orchestratorDependency.setVna(dependency.getVna());
                                        newDependencies.add(orchestratorDependency);

                                    }

                                } else {

                                    if (newDependencies.stream()
                                            .filter(dep -> dep.getDependency().equals(dependency.getDependency()))
                                            .collect(Collectors.toList()).isEmpty()) {

                                        // TODO Convert component Node Instance HexID to Component Node HexID

                                        ComponentNodeInstance servCNI = componentNodeInstanceDAO
                                                .findById(Long.valueOf(service.getComponentNodeInstanceID())).get();

                                        if (null != servCNI && null != servCNI.getLoadBalancedBy()) {

                                            if (componentNodeInstanceDAO.findByHexID(dependency.getDependency())
                                                    .isPresent()) {

                                                ComponentNodeInstance componentNodeInstance = componentNodeInstanceDAO
                                                        .findByHexID(dependency.getDependency()).get();
                                                dependency
                                                        .setDependency(componentNodeInstance.getComponentNode().getHexID());

                                            } else {

                                                ComponentNode componentNode = componentNodeDAO
                                                        .findByHexID(dependency.getDependency()).get();
                                                dependency.setDependency(componentNode.getHexID());

                                            }

                                        } else {

                                            ComponentNodeInstance componentNodeInstance = componentNodeInstanceDAO
                                                    .findByHexID(dependency.getDependency()).get();
                                            dependency.setDependency(componentNodeInstance.getComponentNode().getHexID());

                                        }

                                        newDependencies.add(dependency);

                                    }
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        });

                        service.setDependsOn(newDependencies);

                    }
                }

                if (null != service.getEnvironmentalVariables() && !service.getEnvironmentalVariables()
                        .isEmpty()) {

                    Map<String, String> newEnvVars = new HashMap<>();

                    service.getEnvironmentalVariables().entrySet().stream().forEach(envVar -> {
                        try {

                            if (envVar.getValue().startsWith("@") || envVar.getValue().startsWith("#")) {

                                String value = envVar.getValue().substring(1);
                                if (value.equals("IPV4_PRIVATE") || value.equals("IPV6_PRIVATE") || value.equals("IPV4_PUBLIC")) {
                                    newEnvVars.put(envVar.getKey(), envVar.getValue());
                                } else {

                                    ComponentNodeInstance dependencyCNI = componentNodeInstanceDAO
                                            .findByNameAndApplicationInstance(value, applicationInstance).get();

                                    if (updatedDependsOnComponentNodeInstanceURIs
                                            .containsKey(dependencyCNI.getHexID())) {

                                        // TODO Convert component Node Instance HexID to Component Node HexID
                                        ComponentNodeInstance componentNodeInstance = componentNodeInstanceDAO
                                                .findByHexID(
                                                        updatedDependsOnComponentNodeInstanceURIs.get(dependencyCNI.getHexID()))
                                                .get();
                                        newEnvVars.put(envVar.getKey(),
                                                envVar.getValue().charAt(0) + componentNodeInstance.getComponentNode().getHexID());

//                                    newEnvVars.put(envVar.getKey(), "@" + updatedDependsOnComponentNodeInstanceURIs.get(dependencyCNI.getHexID()));

                                    } else {

                                        // TODO Convert component Node Instance HexID to Component Node HexID
                                        ComponentNodeInstance componentNodeInstance = componentNodeInstanceDAO
                                                .findByHexID(dependencyCNI.getHexID()).get();
                                        newEnvVars.put(envVar.getKey(),
                                                envVar.getValue().charAt(0) + componentNodeInstance.getComponentNode().getHexID());

//                                    newEnvVars.put(envVar.getKey(), "@" + dependencyCNI.getHexID());
                                    }
                                }
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
            }

        });

        if (!mapOfNeededChanges.isEmpty()) {

            // Create Fake GraphLinks
            mapOfNeededChanges.entrySet().stream().forEach(entry -> {

                Long componentNodeInstanceID = entry.getKey();
                ComponentNodeInstance componentNodeInstance = componentNodeInstanceDAO
                        .findById(componentNodeInstanceID).get();
                ComponentNodeInstance traefikComponentNodeInstance = entry.getValue();

                List<GraphLinkNodeInstance> graphLinkNodeInstances = graphLinkNodeInstanceDAO
                        .findAllByApplicationInstanceAndComponentNodeInstanceTo(applicationInstance,
                                componentNodeInstance);

                if (null != graphLinkNodeInstances && !graphLinkNodeInstances.isEmpty()) {

                    graphLinkNodeInstances.stream().forEach(graphLinkNodeInstance -> {

                        try {

                            if (null != graphLinkNodeInstance.getGraphLinkNode()) {

                                graphLinkNodeInstance.setComponentNodeInstanceTo(traefikComponentNodeInstance);
                                graphLinkNodeInstance.setLastModified(new Date());
                                graphLinkNodeInstanceDAO.save(graphLinkNodeInstance);

                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    });

                }

            });

        }

        // Send orchestrator application instance to Orchestrator

        logger.info("Orchestrator AppInstance: " + new Gson().toJson(orchestratorApplicationInstance));

        sliceIntent.setOrchestratorApplicationInstance(orchestratorApplicationInstance);

        return isSuccess;
    }
}
