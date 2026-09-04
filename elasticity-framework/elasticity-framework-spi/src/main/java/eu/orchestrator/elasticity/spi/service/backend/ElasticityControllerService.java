package eu.orchestrator.elasticity.spi.service.backend;

import eu.orchestrator.elasticity.spi.util.Util;
import eu.orchestrator.repository.domain.ComponentNodeInstance.SecurityEnablers;
import eu.orchestrator.repository.domain.ComponentNodeInstanceHash.HashType;
import eu.orchestrator.transfer.entities.orchestrator.*;
import eu.orchestrator.elasticity.spi.model.backend.ElasticityControllerComponent;
import eu.orchestrator.elasticity.spi.model.backend.ElasticityControllerExposedInterface;
import eu.orchestrator.elasticity.spi.model.backend.ElasticityControllerInstanceArguments;
import eu.orchestrator.elasticity.spi.model.backend.ElasticityObjects;
import eu.orchestrator.repository.dao.*;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.client.RestTemplate;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import eu.orchestrator.repository.domain.*;
@org.springframework.stereotype.Service
@Transactional
@SuppressWarnings("Duplicates")
public class ElasticityControllerService {

    private static final Logger logger = Logger.getLogger(ElasticityControllerService.class.getName());

    @PersistenceContext
    EntityManager entityManager;

    @Value("${token.signer.secret}")
    private String secretToken;

    // Base URL of the registry's search API, used to look up the digest of the load balancer image.
    @Value("${elasticity.registry.search.url:}")
    private String registrySearchUrl;

    @Autowired
    UserDAO userDAO;

    @Autowired
    LabelDAO labelDAO;

    @Autowired
    ComponentDAO componentDAO;

    @Autowired
    ComponentNodeDAO componentNodeDAO;

    @Autowired
    ComponentNodeInstanceDAO componentNodeInstanceDAO;

    @Autowired
    ComponentNodeInstanceStatusDAO componentNodeInstanceStatusDAO;

    @Autowired
    RequirementDAO requirementDAO;

    @Autowired
    FlavorInstanceDAO flavorInstanceDAO;

    @Autowired
    HealthCheckDAO healthCheckDAO;

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
    InterfaceDAO interfaceDAO;

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

    @Autowired
    ComponentService componentService;

    @Autowired
    ComponentNodeInstanceHashDAO componentNodeInstanceHashDAO;

    public Component createElasticityControllerComponent(ElasticityControllerComponent balancerComponent){

        List<Plugin> listOfDefaultPlugins = pluginDAO.findAllByDefaultPlugin(true);
        //TODO get plugin list of the component

        List<Plugin> pluginList = listOfDefaultPlugins;

        List<Label> labelList = new ArrayList<>();
        for(String labelName: balancerComponent.getLabelNameList()) {

            Optional<Label> labelLBOp = labelDAO.findByName(labelName);
            Label labelLB;
            if (labelLBOp.isPresent()) {
                labelLB = labelLBOp.get();
            } else {
                labelLB = new Label();
                labelLB.setName(labelName);
                labelLB.setDateCreated(new Date());
                labelLB.setLastModified(new Date());
                labelDAO.save(labelLB);
            }
            labelList.add(labelLB);
        }

        Component loadBalancer = new Component();
        loadBalancer.setName(balancerComponent.getName());
        loadBalancer.setHexID(Util.createRandomHEXString(entityManager));
        loadBalancer.setDockerImage(balancerComponent.getDockerImage());
        loadBalancer.setDockerRegistry(balancerComponent.getDockerRegistry());
        loadBalancer.setDockerUsername(balancerComponent.getDockerUsername());
        loadBalancer.setDockerPassword(Util.encrypt(balancerComponent.getDockerPassword(),secretToken));

        loadBalancer.setCapabilityDrops(balancerComponent.getCapabilitiesDrop());
        loadBalancer.setCapabilityAdds(balancerComponent.getCapabilitiesAdd());
        loadBalancer.setArchitecture(balancerComponent.getArchitecture());
        loadBalancer.setPublicComponent(true);
        loadBalancer.setDateCreated(new Date());
        loadBalancer.setLastModified(new Date());
        Optional<User> adminOp = userDAO.findByUsername("admin");
        if(adminOp==null || !adminOp.isPresent()){
            //TODO throw exception
        }
        User admin = adminOp.get();
        loadBalancer.setUser(admin);
        loadBalancer.setOrganization(admin.getOrganization());
        loadBalancer.setElasticityController("NONE");

        loadBalancer.setPlugins(new TreeSet<>(pluginList));
        loadBalancer.setUlimitMemlockSoft(balancerComponent.getUlimitMemlockSoft());
        loadBalancer.setUlimitMemlockHard(balancerComponent.getUlimitMemlockHard());
        loadBalancer.setDockerExecutionUser(balancerComponent.getDockerExecutionUser());

        componentDAO.save(loadBalancer);
        loadBalancer.setLabels(new TreeSet<>(labelList));

        // Exposed Interface
        List<Interface> interfaceList = new ArrayList<>();
        for(ElasticityControllerExposedInterface balancerExposedInterface : balancerComponent.getExposedInterfaces()){

            Interface interfaceLBUI = new Interface();
            interfaceLBUI.setDateCreated(new Date());
            interfaceLBUI.setLastModified(new Date());
            interfaceLBUI.setInterfaceType(balancerExposedInterface.getInterfaceType());
            interfaceLBUI.setName(balancerExposedInterface.getName());
            interfaceLBUI.setPort(balancerExposedInterface.getPort());
            interfaceLBUI.setVna(balancerExposedInterface.getVna());
            interfaceLBUI.setTransmissionProtocol(balancerExposedInterface.getTransmissionProtocol());
            interfaceLBUI.setComponent(loadBalancer);
            interfaceDAO.save(interfaceLBUI);
        }

        // Minimum Execution Requirements
        Requirement requirementLB = new Requirement();
        requirementLB.setComponent(loadBalancer);
        requirementLB.setDateCreated(new Date());
        requirementLB.setLastModified(new Date());
        requirementLB.setGpuRequired(balancerComponent.getRequirement().getGpuRequired());
        requirementLB.setHypervisorType(balancerComponent.getRequirement().getHypervisorType());
        requirementLB.setRam(balancerComponent.getRequirement().getRam());
        requirementLB.setStorage(balancerComponent.getRequirement().getStorage());
        requirementLB.setvCPUs(balancerComponent.getRequirement().getvCPUs());
        requirementDAO.save(requirementLB);

        // Health Check
        HealthCheck loadBalancerHealthCheck = new HealthCheck();
        loadBalancerHealthCheck.setComponent(loadBalancer);
        loadBalancerHealthCheck.setDateCreated(new Date());
        loadBalancerHealthCheck.setLastModified(new Date());
        loadBalancerHealthCheck.setName(balancerComponent.getHealthCheck().getName());
        loadBalancerHealthCheck.setInterval(balancerComponent.getHealthCheck().getInterval());
        loadBalancerHealthCheck.setArgs(balancerComponent.getHealthCheck().getArgs());
        loadBalancerHealthCheck.setHttpURL(balancerComponent.getHealthCheck().getHttpURL());
        healthCheckDAO.save(loadBalancerHealthCheck);

        Component component = componentDAO.save(loadBalancer);
        return component;
    }

    public ComponentNode createElasticityControllerComponentNode(String componentName, String name, String suffix) {
        Component lbComponent = componentDAO.findByName(componentName).get();
        if (lbComponent == null) {
            //TODO throw exception
        }
        // STEP1: Create component node instance for Load Balancer
        ComponentNode lbComponentNode = new ComponentNode();
        lbComponentNode.setComponent(lbComponent);
        lbComponentNode.setHexID(Util.createRandomHEXString(entityManager));
        lbComponentNode.setDateCreated(new Date());
        lbComponentNode.setLastModified(new Date());
        lbComponentNode.setName(name + suffix);

        ComponentNode savedLBComponent = componentNodeDAO.save(lbComponentNode);
        return savedLBComponent;
    }

    public ElasticityObjects createElasticityControllerNodeInstance(ComponentNode lbComponentNode, ElasticityObjects elasticityObjects, ElasticityControllerInstanceArguments balancerInstanceArguments) {
        //Move elasticity controller orchestrator adapter implementation to ElasticityObject
        elasticityObjects.setElasticityControllerOrchestratorAdapterImplementation(balancerInstanceArguments.getElasticityControllerOrchestratorAdapterImplementation());

        ApplicationInstance applicationInstance = elasticityObjects.getApplicationInstance();
        ComponentNodeInstance balancedComponentNodeInstance = elasticityObjects.getWorkerComponentNodeInstance();
        Component lbComponent = lbComponentNode.getComponent();
        ComponentNodeInstance lbComponentNodeInstance = new ComponentNodeInstance();

        lbComponentNodeInstance.setLoadBalancer(true);
        lbComponentNodeInstance.setLoadBalancedBy(null);
        lbComponentNodeInstance.setSshKey(balancedComponentNodeInstance.getSshKey());
        lbComponentNodeInstance.setHexID(Util.createRandomHEXString(entityManager));

        //TODO when we add volumes rethink it
        lbComponentNodeInstance.setDeviceInstances(null);
        lbComponentNodeInstance.setLocationInstances(null);
        lbComponentNodeInstance.setProvider(balancerInstanceArguments.getProvider());
        lbComponentNodeInstance.setVolumeInstances(balancerInstanceArguments.getVolumeInstances());

        //TODO all those needed to be aquired by the arguments
        lbComponentNodeInstance.setDnsEntry(balancerInstanceArguments.getDnsEntry());
        lbComponentNodeInstance.setHostname(balancerInstanceArguments.getHostname());
        lbComponentNodeInstance.setStatusIPS(balancerInstanceArguments.getStatusIPS());
        lbComponentNodeInstance.setStatusIDS(balancerInstanceArguments.getStatusIDS());
        lbComponentNodeInstance.setStatusSOC(balancerInstanceArguments.getStatusSoc());
        lbComponentNodeInstance.setPrivilege(balancerInstanceArguments.getPrivilege());

        if(balancerInstanceArguments.getCapabilityAdds()!=null) {
            Collection<Component.CapabilityAdd> capabilityAdds = new ArrayList<Component.CapabilityAdd>();
            balancerInstanceArguments.getCapabilityAdds().stream().forEach(
                    capabilityAdd -> {
                        capabilityAdds.add(capabilityAdd);
                    }
            );
            lbComponentNodeInstance.setCapabilityAdds(capabilityAdds);
        }else{
            lbComponentNodeInstance.setCapabilityAdds(null);
        }

        if(balancedComponentNodeInstance.getCapabilityDrops()!=null) {
            Collection<Component.CapabilityDrop> capabilityDrops = new ArrayList<Component.CapabilityDrop>();
            balancerInstanceArguments.getCapabilityDrops().stream().forEach(
                    capabilityDrop -> {
                        capabilityDrops.add(capabilityDrop);
                    }
            );

            lbComponentNodeInstance.setCapabilityDrops(capabilityDrops);
        }else {
            lbComponentNodeInstance.setCapabilityDrops(null);
        }

        lbComponentNodeInstance.setNetworkModeHost(balancerInstanceArguments.getNetworkModeHost());
        lbComponentNodeInstance.setSharedMemorySize(balancerInstanceArguments.getSharedMemorySize());

        if(balancerInstanceArguments.getEnvironmentalVariableInstances()!=null) {
            List<EnvironmentalVariableInstance> environmentalVariableList = new ArrayList<>();
            balancerInstanceArguments.getEnvironmentalVariableInstances().stream().forEach(
                    environmentalVariable -> {
                        environmentalVariableList.add(environmentalVariable);
                    }
            );

            lbComponentNodeInstance.setEnvironmentalVariableInstances(environmentalVariableList);
        }else {
            lbComponentNodeInstance.setEnvironmentalVariableInstances(null);
        }

        lbComponentNodeInstance.setMinimumWorkers(1);
        lbComponentNodeInstance.setMaximumWorkers(1);
        lbComponentNodeInstance.setDateCreated(new Date());
        lbComponentNodeInstance.setLastModified(new Date());
        lbComponentNodeInstance.setName(lbComponentNode.getName());
        lbComponentNodeInstance.setComponentNode(lbComponentNode);
        lbComponentNodeInstance.setApplicationInstance(applicationInstance);

        //TODO get two commands for IPv4 and IPv6 installations from the arguments
        String command;
        if (applicationInstance.getOverlay()) {

            command = balancerInstanceArguments.getCommandIPv6();
        } else {
            command = balancerInstanceArguments.getCommand();
        }
        lbComponentNodeInstance.setCommand(command);

        componentNodeInstanceDAO.save(lbComponentNodeInstance);

        //TODO astrid
        if (null != balancedComponentNodeInstance.getSecurityEnablers() && !balancedComponentNodeInstance
                .getSecurityEnablers().isEmpty()) {

            Collection<ComponentNodeInstance.SecurityEnablers> securityEnablersCollection = new ArrayList<>();
            securityEnablersCollection.clear();
            balancedComponentNodeInstance.getSecurityEnablers().stream().forEach(securityEnabler -> {

                if ((securityEnabler.equals(SecurityEnablers.CONFIGURATION_INTEGRITY_VERIFICATION)
                        || securityEnabler.equals(SecurityEnablers.RUNTIME_FILE_INTEGRITY))
                        && componentNodeInstanceHashDAO.countByComponentNodeInstance(lbComponentNodeInstance) == 0){

                    JsonObject dockerCredential = new JsonObject();
                    dockerCredential.addProperty("dockerRegistry", lbComponentNodeInstance.getComponentNode().getComponent().getDockerRegistry());
                    dockerCredential.addProperty("dockerUsername", lbComponentNodeInstance.getComponentNode().getComponent().getDockerUsername());
                    dockerCredential.addProperty("dockerPassword", Util.decrypt(lbComponentNodeInstance.getComponentNode().getComponent().getDockerPassword(), secretToken));
                    String dockerCredentialAsString = dockerCredential.toString();
                    String dockerCredentialHashing = Util.stringHash(dockerCredentialAsString);

                    ComponentNodeInstanceHash componentNodeInstanceHash = new ComponentNodeInstanceHash();
                    componentNodeInstanceHash.setDateCreated(new Date());
                    componentNodeInstanceHash.setLastModified(new Date());
                    componentNodeInstanceHash.setComponentNodeInstance(lbComponentNodeInstance);
                    componentNodeInstanceHash.setValue(Util.encrypt(dockerCredentialHashing,secretToken));
                    componentNodeInstanceHash.setType(HashType.DOCKER_CREDENTIALS.name());
                    componentNodeInstanceHashDAO.save(componentNodeInstanceHash);


                    String dockerImageHash = "";
                    if (null !=  lbComponentNodeInstance.getComponentNode().getComponent().getDockerRegistry()){
                        String username = lbComponentNodeInstance.getComponentNode().getComponent().getDockerUsername();
                        String password = Util.decrypt(lbComponentNodeInstance.getComponentNode().getComponent().getDockerPassword(),secretToken);
                        String registry = lbComponentNodeInstance.getComponentNode().getComponent().getDockerRegistry();
                        String[] temp = registry.split("-");
                        registry = temp[0] + "-docker-" + temp[1];
                        String image = lbComponentNodeInstance.getComponentNode().getComponent().getDockerImage();
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

                            for (JsonElement dockerImage: jsonArrayOfDockerImages) {
                                JsonObject jsonObject = dockerImage.getAsJsonObject();
                                if ((jsonObject.get("name").getAsString().compareTo(imageName) == 0
                                        || jsonObject.get("name").getAsString().compareTo("library/"+imageName) == 0)
                                        && jsonObject.get("version").getAsString().compareTo(imageTag) == 0 ){

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
                    componentNodeInstanceHash.setComponentNodeInstance(lbComponentNodeInstance);
                    componentNodeInstanceHash.setValue(Util.encrypt(dockerImageHash,secretToken));
                    componentNodeInstanceHash.setType(HashType.DOCKER_IMAGE.name());
                    componentNodeInstanceHashDAO.save(componentNodeInstanceHash);
                }

                securityEnablersCollection.add(securityEnabler);
            });
            lbComponentNodeInstance.setSecurityEnablers(securityEnablersCollection);
        }else{
            Collection<ComponentNodeInstance.SecurityEnablers> securityEnablersCollection = new ArrayList<>();
            securityEnablersCollection.clear();
            lbComponentNodeInstance.setSecurityEnablers(securityEnablersCollection);
        }


        SortedSet<ComponentNodeInstance> currentComponentsNodeInstances = applicationInstance.getComponentNodeInstances();
        currentComponentsNodeInstances.add(lbComponentNodeInstance);
        applicationInstance.setComponentNodeInstances(currentComponentsNodeInstances);

        ComponentNodeInstanceStatus componentNodeInstanceStatus = new ComponentNodeInstanceStatus();
        componentNodeInstanceStatus.setReportedChange(OrchestratorChangedStatusNotification.ChangeType.GraphStatusChange.name());
        componentNodeInstanceStatus.setLastModified(new Date());
        componentNodeInstanceStatus.setDateCreated(new Date());
        componentNodeInstanceStatus.setComponentNodeInstance(lbComponentNodeInstance);
        componentNodeInstanceStatus.setMessage("Component is loading...");
        componentNodeInstanceStatus.setStatus("LOADING");
        componentNodeInstanceStatus.setApplicationInstance(applicationInstance);
        componentNodeInstanceStatusDAO.save(componentNodeInstanceStatus);

        Requirement requirement = requirementDAO.findByComponent(lbComponent).get();

        FlavorInstance flavorInstance = new FlavorInstance();
        flavorInstance.setComponentNodeInstance(lbComponentNodeInstance);
        flavorInstance.setvCPUs(requirement.getvCPUs());
        flavorInstance.setStorage(requirement.getStorage());
        flavorInstance.setRam(requirement.getRam());
        flavorInstance.setLastModified(new Date());
        flavorInstance.setDateCreated(new Date());
        flavorInstanceDAO.save(flavorInstance);
        lbComponentNodeInstance.setFlavorInstance(flavorInstance);


        HealthCheck healthCheck = healthCheckDAO.findByComponent(lbComponent).get();

        HealthCheckInstance healthCheckInstance = new HealthCheckInstance();
        healthCheckInstance.setHealthCheck(healthCheck);
        healthCheckInstance.setComponentNodeInstance(lbComponentNodeInstance);
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
        lbComponentNodeInstance.setHealthCheckInstance(healthCheckInstance);

        logger.info("Size of plugins: " + lbComponent.getPlugins().size());

        List<Plugin> traefikPlugins = new ArrayList<>(lbComponent.getPlugins());

        logger.info("Size of plugins: " + traefikPlugins.size());

        // Plugins
        if (null != traefikPlugins && !traefikPlugins.isEmpty()) {

            List<PluginInstance> pluginInstances = new ArrayList<>();

            traefikPlugins.stream().forEach(plugin -> {

                try {

                    Plugin lPlugin = pluginDAO.findById(plugin.getPluginID()).get();

                    PluginInstance pluginInstance = new PluginInstance();
                    pluginInstance.setComponentNodeInstance(lbComponentNodeInstance);
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

            lbComponentNodeInstance.setPluginInstances(pluginInstances);

        } else {
            lbComponentNodeInstance.setPluginInstances(null);
        }

        // Expose Interfaces of Load Balancer
        List<InterfaceInstance> exposedInterfaces = new ArrayList<>();
        List<Interface> interfaceList = lbComponent.getExposedInterfaces();
        for (Interface exposedInterface : interfaceList) {

            InterfaceInstance interfaceInstance = new InterfaceInstance();
            interfaceInstance.setPort(exposedInterface.getPort());
            interfaceInstance.setName(exposedInterface.getName());
            interfaceInstance.setInterfaceObj(exposedInterface);
            interfaceInstance.setInterfaceType(exposedInterface.getInterfaceType());
            interfaceInstance.setComponentNodeInstance(lbComponentNodeInstance);
            interfaceInstance.setLastModified(new Date());
            interfaceInstance.setDateCreated(new Date());
            interfaceInstanceDAO.save(interfaceInstance);
            exposedInterfaces.add(interfaceInstance);
        }

        lbComponentNodeInstance.setInterfaceInstances(exposedInterfaces);

        elasticityObjects.setElasticityController(lbComponentNodeInstance);
        return elasticityObjects;

    }

    public ElasticityObjects fetchWorkerInterfaces(ElasticityObjects elasticityObjects){
        ComponentNodeInstance balancedComponentNodeInstance = elasticityObjects.getWorkerComponentNodeInstance();

        List<InterfaceInstance> loadBalancerInterfaceInstaceList = new ArrayList<>();
        for(InterfaceInstance interfaceInstance : balancedComponentNodeInstance.getInterfaceInstances()){
            loadBalancerInterfaceInstaceList.add(interfaceInstance);
        }
        elasticityObjects.setElasticityControllerInterfaceInstaceList(loadBalancerInterfaceInstaceList);
        elasticityObjects.setElasticityControllerComponentInterfaceInstaceList(balancedComponentNodeInstance.getInterfaceInstances());

        return elasticityObjects;
    }

    public ElasticityObjects addBalancedBy(ElasticityObjects elasticityObjects){
        ComponentNodeInstance balancedComponentNodeInstance = elasticityObjects.getWorkerComponentNodeInstance();
        ComponentNodeInstance lbComponentNodeInstance = elasticityObjects.getElasticityController();

        balancedComponentNodeInstance.setLoadBalancedBy(lbComponentNodeInstance);
        componentNodeInstanceDAO.save(balancedComponentNodeInstance);
        return elasticityObjects;
    }

    public ElasticityObjects addComponentInterfacesToElasticityController(ElasticityObjects elasticityObjects){
        ComponentNodeInstance balancedComponentNodeInstance = elasticityObjects.getWorkerComponentNodeInstance();
        ComponentNodeInstance lbComponentNodeInstance = elasticityObjects.getElasticityController();

        List<InterfaceInstance> exposedInterfaces = lbComponentNodeInstance.getInterfaceInstances();
        if(exposedInterfaces == null){
            logger.warning("Empty exposed interfaces " + elasticityObjects.getWorkerComponentNodeInstance().getName());
            exposedInterfaces = new ArrayList<>();
        }

        // Exposed interfaces of balanced component node instance
        if (null != balancedComponentNodeInstance.getInterfaceInstances() && !balancedComponentNodeInstance
                .getInterfaceInstances().isEmpty()) {

            for(InterfaceInstance interfaceInstance : balancedComponentNodeInstance.getInterfaceInstances()){
                try {
                    InterfaceInstance componentNodeInstanceExposedInterface = new InterfaceInstance();
                    componentNodeInstanceExposedInterface.setPort(interfaceInstance.getPort());
                    componentNodeInstanceExposedInterface.setName(interfaceInstance.getName());
                    componentNodeInstanceExposedInterface
                            .setInterfaceObj(interfaceInstance.getInterfaceObj());
                    componentNodeInstanceExposedInterface
                            .setComponentNodeInstance(lbComponentNodeInstance);
                    componentNodeInstanceExposedInterface.setInterfaceType(interfaceInstance.getInterfaceType());
                    componentNodeInstanceExposedInterface.setLastModified(new Date());
                    componentNodeInstanceExposedInterface.setDateCreated(new Date());
                    interfaceInstanceDAO.save(componentNodeInstanceExposedInterface);
                    exposedInterfaces.add(componentNodeInstanceExposedInterface);

                    // Check type of interface
                    if (interfaceInstance.getInterfaceObj().getInterfaceType()
                            .equals(Interface.InterfaceType.CORE.name())) {

                        if (!elasticityObjects.getMapOfNeededChanges()
                                .containsKey(balancedComponentNodeInstance.getComponentNodeInstanceID())) {
                            elasticityObjects.getMapOfNeededChanges().put(balancedComponentNodeInstance.getComponentNodeInstanceID(),
                                    lbComponentNodeInstance);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        lbComponentNodeInstance.setInterfaceInstances(exposedInterfaces);

        // Change the ACCESS interfaces of the components instances to CORE
        if (null != balancedComponentNodeInstance.getInterfaceInstances() && !balancedComponentNodeInstance
                .getInterfaceInstances().isEmpty()) {

            balancedComponentNodeInstance.getInterfaceInstances().stream().forEach(interfaceInstance -> {
                if (interfaceInstance.getInterfaceType().equals(Interface.InterfaceType.ACCESS.name())) {
                    interfaceInstance.setInterfaceType(Interface.InterfaceType.CORE.name());

                    interfaceInstanceDAO.save(interfaceInstance);
                }
            });
        }

        elasticityObjects.setElasticityController(lbComponentNodeInstance);
        return elasticityObjects;
    }

    public boolean moveConstraintsToElasticityController(ElasticityObjects elasticityObjects){
        // TODO STEP5: Have to move constraints of Component Node Instance to Traefik Component Node Instance
        ApplicationInstance applicationInstance = elasticityObjects.getApplicationInstance();
        ComponentNodeInstance balancedComponentNodeInstance = elasticityObjects.getWorkerComponentNodeInstance();
        ComponentNodeInstance lbComponentNodeInstance = elasticityObjects.getElasticityController();

        if (null != applicationInstance.getConstraints() && !applicationInstance.getConstraints()
                .isEmpty()) {
            //TODO think how many of those constraints must be moved
            applicationInstance.getConstraints().stream().filter(constraint ->
                    (null != constraint.getComponentNodeInstance() && constraint.getComponentNodeInstance().getComponentNodeInstanceID().equals(balancedComponentNodeInstance.getComponentNodeInstanceID())) || (null != constraint.getInterfaceInstance() && constraint.getInterfaceInstance().getInterfaceInstanceID().equals(balancedComponentNodeInstance.getInterfaceInstances().get(0).getInterfaceInstanceID()))|| null != constraint.getGraphLinkNodeInstance()).forEach(constraint -> {

                // Found constraints that should be moved to LB

                try {

                    if (null != constraint.getComponentNodeInstance()) {

                        // Component Constraint
                        Constraint componentConstraint = new Constraint();
                        componentConstraint.setComponentNodeInstance(lbComponentNodeInstance);
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
                                .getComponentNodeInstanceID().equals(balancedComponentNodeInstance.getComponentNode())) {

                            // Graph Link Constraint
                            GraphLinkNodeInstance graphLinkNodeInstance = graphLinkNodeInstanceDAO
                                    .findById(constraint.getGraphLinkNodeInstance().getGraphLinkNodeInstanceID())
                                    .get();
                            graphLinkNodeInstance.setComponentNodeInstanceTo(lbComponentNodeInstance);
                            graphLinkNodeInstance.setLastModified(new Date());
                            graphLinkNodeInstanceDAO.save(graphLinkNodeInstance);

                            constraint.setGraphLinkNodeInstance(graphLinkNodeInstance);
                            constraint.setLastModified(new Date());
                            constraintDAO.save(constraint);

                        }

                    } else {

                        InterfaceInstance interfaceInstance = interfaceInstanceDAO
                                .findAllByComponentNodeInstanceOrderByDateCreatedDesc(lbComponentNodeInstance,
                                        null).getContent().stream().filter(
                                        iInstance -> iInstance.getInterfaceObj().getInterfaceID().equals(
                                                balancedComponentNodeInstance.getInterfaceInstances().get(0).getInterfaceObj()
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
        return true;
    }

    public ElasticityObjects addWorkers(ElasticityObjects elasticityObjects){
        ApplicationInstance applicationInstance = elasticityObjects.getApplicationInstance();
        ComponentNodeInstance balancedComponentNodeInstance = elasticityObjects.getWorkerComponentNodeInstance();
        ComponentNodeInstance lbComponentNodeInstance = elasticityObjects.getElasticityController();

        // STEP3: Check if we need more than one workers
        if (balancedComponentNodeInstance.getMinimumWorkers() > 1
                && balancedComponentNodeInstance.getMaximumWorkers() > 1) {

            // Calculate Workers
            int numOfWorkers = balancedComponentNodeInstance.getMinimumWorkers();

            for (int i = 1; i < numOfWorkers; i++) {

                // Replicate Component Node Instance
                ComponentNodeInstance replicatedWorker = componentService
                        .replicateWorkerInstance(applicationInstance, lbComponentNodeInstance, balancedComponentNodeInstance,
                                balancedComponentNodeInstance.getName() + "Worker" + i , true);

                //Add replicated worker to the list
                elasticityObjects.getWorkers().put(replicatedWorker.getComponentNodeInstanceID(), replicatedWorker);

                //Translate replicated worker to orchestrator component instance
                OrchestratorComponentNodeInstance workerOrchestratorCNI = componentService
                        .translateWorkerInstance(applicationInstance, replicatedWorker, elasticityObjects.getVimID());

                //Add translated worker to the list
                elasticityObjects.getServices().put(workerOrchestratorCNI.getComponentNodeInstanceID(), workerOrchestratorCNI);
            }
        }

        return elasticityObjects;
    }

    public void linkWorkersWithElasticityController(ElasticityObjects elasticityObjects){

        if (!elasticityObjects.getWorkers().isEmpty()) {

            elasticityObjects.getWorkers().entrySet().stream().forEach(entry -> {

                ComponentNodeInstance worker = entry.getValue();
                ComponentNodeInstance loadBalancer = worker.getLoadBalancedBy();

                GraphLinkNodeInstance graphLinkNodeInstance = new GraphLinkNodeInstance();
                graphLinkNodeInstance.setComponentNodeInstanceFrom(loadBalancer);
                graphLinkNodeInstance.setComponentNodeInstanceTo(worker);
                graphLinkNodeInstance.setDateCreated(new Date());
                graphLinkNodeInstance.setLastModified(new Date());
                graphLinkNodeInstance.setApplicationInstance(elasticityObjects.getApplicationInstance());
                graphLinkNodeInstance.setGraphLinkNode(null);
                graphLinkNodeInstanceDAO.save(graphLinkNodeInstance);
            });
        }
    }

    private OrchestratorComponentNodeInstance searchElasticityControllerService(ElasticityObjects elasticityObjects){
        ComponentNodeInstance elasticityControllerComponentNodeInstance = elasticityObjects.getElasticityController();

        OrchestratorComponentNodeInstance elasticityControllerService = null;
        elasticityControllerService = elasticityObjects.getServices().get("" + elasticityControllerComponentNodeInstance.getComponentNodeInstanceID());

        return elasticityControllerService;
    }

    public ElasticityObjects generateElasticityControllerService(ElasticityObjects elasticityObjects, Boolean addNewElasticityControllerService,
                                                                 Boolean addBalancedComponentInterface, Boolean isLoadBalancer, Boolean isLambdaProxy) {
        ComponentNodeInstance loadBalancerComponentNodeInstance = elasticityObjects.getElasticityController();
        ComponentNode loadBalancerComponentNode = loadBalancerComponentNodeInstance.getComponentNode();
        Component loadBalancerComponent = loadBalancerComponentNode.getComponent();

        OrchestratorComponentNodeInstance loadBalancer = null;
        if (addNewElasticityControllerService == false) {
            loadBalancer = searchElasticityControllerService(elasticityObjects);
            if (loadBalancer == null) {
                addNewElasticityControllerService = true;
            }
        }

        if (addNewElasticityControllerService) {
            loadBalancer = new OrchestratorComponentNodeInstance();

            loadBalancer.setComponentNodeInstanceHexID("" + loadBalancerComponentNodeInstance.getHexID());
            loadBalancer
                    .setComponentNodeInstanceID("" + loadBalancerComponentNodeInstance.getComponentNodeInstanceID());
            loadBalancer.setComponentNodeInstanceName(loadBalancerComponentNodeInstance.getName());
            loadBalancer.setComponentNodeHexID("" + loadBalancerComponentNode.getHexID());
            loadBalancer.setComponentNodeID("" + loadBalancerComponentNode.getComponentNodeID());
            loadBalancer.setComponentNodeName(loadBalancerComponentNode.getName());
            loadBalancer.setProviderID(elasticityObjects.getVimID());

            OrchestratorElasticity orchestratorElasticity = new OrchestratorElasticity();
            orchestratorElasticity.setProfile(loadBalancerComponent.getElasticityController());
            loadBalancer.setMonitoringElasticity(orchestratorElasticity);

            List<String> commands = new ArrayList<>();
            for (String subCommand : loadBalancerComponentNodeInstance.getCommand().split("\\,")) {

                if (!commands.contains(subCommand)) {
                    commands.add(subCommand);
                }
            }

            loadBalancer.setCommand(commands);

            loadBalancer.setController(true);
            loadBalancer.setElasticityControllerAdapterImplementation(elasticityObjects.getElasticityControllerOrchestratorAdapterImplementation());

            loadBalancer.setImage(loadBalancerComponent.getDockerImage());
            loadBalancer.setRegistry(loadBalancerComponent.getDockerRegistry());
            loadBalancer.setDockerUsername(loadBalancerComponent.getDockerUsername());
            loadBalancer.setDockerPassword(loadBalancerComponent.getDockerPassword());

            loadBalancer.setUlimitMemlockSoft(loadBalancerComponent.getUlimitMemlockSoft());
            loadBalancer.setUlimitMemlockHard(loadBalancerComponent.getUlimitMemlockHard());
            loadBalancer.setDockerExecutionUser(loadBalancerComponent.getDockerExecutionUser());

            //TODO find a way to define it more properly and NOT with multiple true false values. maybe with an enum
            loadBalancer.setLoadBalancer(isLoadBalancer);
            loadBalancer.setLambdaProxy(isLambdaProxy);
            loadBalancer.setStatusIDS(loadBalancerComponentNodeInstance.getStatusIDS());
            loadBalancer.setStatusIPS(loadBalancerComponentNodeInstance.getStatusIPS());
            loadBalancer.setStatusSoc(loadBalancerComponentNodeInstance.getStatusSOC());

            if (null != loadBalancerComponentNodeInstance.getFlavorInstance()) {

                OrchestratorFlavor orchestratorFlavor = new OrchestratorFlavor();
                orchestratorFlavor.setRam(loadBalancerComponentNodeInstance.getFlavorInstance().getRam());
                orchestratorFlavor.setvCPUs(loadBalancerComponentNodeInstance.getFlavorInstance().getvCPUs());
                orchestratorFlavor.setStorage(loadBalancerComponentNodeInstance.getFlavorInstance().getStorage());
                loadBalancer.setFlavor(orchestratorFlavor);
            }

            if (null != loadBalancerComponentNodeInstance.getHealthCheckInstance()) {

                OrchestratorHealthCheck orchestratorHealthCheck = new OrchestratorHealthCheck();
                orchestratorHealthCheck
                        .setInterval(loadBalancerComponentNodeInstance.getHealthCheckInstance().getInterval().toString());
                orchestratorHealthCheck.setArgs(
                        null != loadBalancerComponentNodeInstance.getHealthCheckInstance().getArgs()
                                && !loadBalancerComponentNodeInstance.getHealthCheckInstance().getArgs().isEmpty()
                                ? loadBalancerComponentNodeInstance.getHealthCheckInstance().getArgs() : null);
                orchestratorHealthCheck.setHttpURL(
                        null != loadBalancerComponentNodeInstance.getHealthCheckInstance().getHttpURL()
                                && !loadBalancerComponentNodeInstance.getHealthCheckInstance().getHttpURL().isEmpty()
                                ? loadBalancerComponentNodeInstance.getHealthCheckInstance().getHttpURL() : null);
                loadBalancer.setHealthCheck(orchestratorHealthCheck);
            }

            if (null != loadBalancerComponentNodeInstance.getPluginInstances() && !loadBalancerComponentNodeInstance
                    .getPluginInstances().isEmpty()) {

                List<OrchestratorPlugin> orchestratorPlugins = new ArrayList<>();

                loadBalancerComponentNodeInstance.getPluginInstances().stream().forEach(pluginInstance -> {

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

        }


        //Mind this because I delete the previous ports and reply the WHOLE list again. So, maybe add only those that don't already exist?
        List<OrchestratorPort> loadBalancerOrchestratorPortList = new ArrayList<>();
        for (InterfaceInstance exposedInterfaceInstance : elasticityObjects.getElasticityController().getInterfaceInstances()) {
            OrchestratorPort orchestratorPort = new OrchestratorPort();
            orchestratorPort.setProtocol(exposedInterfaceInstance.getInterfaceObj().getTransmissionProtocol());
            orchestratorPort.setTarget(exposedInterfaceInstance.getPort());
            orchestratorPort.setPublished(exposedInterfaceInstance.getPort());
            orchestratorPort.setVna(exposedInterfaceInstance.getInterfaceObj().getVna());
            orchestratorPort.setType(exposedInterfaceInstance.getInterfaceType());
            orchestratorPort.setNetworkAttachmentPoint(loadBalancerComponentNodeInstance.getProvider().getNetworkID());
            loadBalancerOrchestratorPortList.add(orchestratorPort);
        }

//        if(addBalancedComponentInterface) {
//            //TODO THINK THE NETWORK IDs FOR THE OSS
//            for (InterfaceInstance exposedInterfaceInstance : elasticityObjects.getElasticityControllerComponentInterfaceInstaceList()) {
//                OrchestratorPort orchestratorPort = new OrchestratorPort();
//                orchestratorPort.setProtocol(exposedInterfaceInstance.getInterfaceObj().getTransmissionProtocol());
//                orchestratorPort.setTarget(exposedInterfaceInstance.getPort());
//                orchestratorPort.setPublished(exposedInterfaceInstance.getPort());
//                orchestratorPort.setVna(exposedInterfaceInstance.getInterfaceObj().getVna());
//                orchestratorPort.setType(exposedInterfaceInstance.getInterfaceType());
//                orchestratorPort.setNetworkAttachmentPoint(loadBalancerComponentNodeInstance.getProvider().getNetworkID());
//                loadBalancerOrchestratorPortList.add(orchestratorPort);
//            }
//        }

        loadBalancer.setPorts(loadBalancerOrchestratorPortList);

        List<OrchestratorDependency> orchestratorDependencyList = null;
        if (addNewElasticityControllerService){
            orchestratorDependencyList = new ArrayList<>();
        }else{
            orchestratorDependencyList = loadBalancer.getDependsOn();
            if(orchestratorDependencyList == null){
                orchestratorDependencyList = new ArrayList<>();
            }
        }
        for(InterfaceInstance interfaceInstance : elasticityObjects.getElasticityControllerComponentInterfaceInstaceList()){
            OrchestratorDependency orchestratorDependency = new OrchestratorDependency();

            orchestratorDependency.setDependency(interfaceInstance.getComponentNodeInstance().getComponentNode().getHexID());
            orchestratorDependency.setNetworkAttachmentPoint(interfaceInstance.getComponentNodeInstance().getProvider().getNetworkID());
            orchestratorDependency.setVna(interfaceInstance.getInterfaceObj().getVna());

            orchestratorDependencyList.add(orchestratorDependency);
        }
        loadBalancer.setDependsOn(orchestratorDependencyList);

        //TODO Astrid security enablers
        if(loadBalancerComponentNodeInstance.getSecurityEnablers()!=null && !loadBalancerComponentNodeInstance.getSecurityEnablers().isEmpty()) {
            loadBalancer.setHasEnableSecurity(true);
            if (loadBalancerComponentNodeInstance.getSecurityEnablers().contains(SecurityEnablers.CONFIGURATION_INTEGRITY_VERIFICATION) ||
                    loadBalancerComponentNodeInstance.getSecurityEnablers().contains(SecurityEnablers.RUNTIME_FILE_INTEGRITY)){
                loadBalancer.setProduceHashes(true);
            }else{
                loadBalancer.setProduceHashes(false);
            }
        }else{
            loadBalancer.setHasEnableSecurity(false);
        }

        elasticityObjects.getServices().put(loadBalancer.getComponentNodeInstanceID(), loadBalancer);
        return elasticityObjects;
    }

    // TODO when volumes implemented
    public void addSharedVolume(){}
}
