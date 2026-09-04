package eu.orchestrator.elasticity.adapter.traefikLambdaProxy;

import eu.orchestrator.elasticity.spi.adapter.ElasticityFrameworkBackend;
import eu.orchestrator.elasticity.spi.model.backend.*;
import eu.orchestrator.elasticity.spi.service.backend.BalancerMediator;
import eu.orchestrator.elasticity.spi.service.backend.ComponentService;
import eu.orchestrator.elasticity.spi.service.backend.ElasticityControllerService;
import eu.orchestrator.elasticity.spi.service.backend.ScalingService;
import eu.orchestrator.repository.domain.Component;
import eu.orchestrator.repository.domain.ComponentNode;
import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorScalingRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.*;
import java.util.logging.Logger;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 8/8/2019
 */
@org.springframework.stereotype.Component
public class TraefikLambdaProxyBackendAdapter implements ElasticityFrameworkBackend {
    private static final Logger logger = Logger.getLogger(TraefikLambdaProxyBackendAdapter.class.getName());

    @Autowired
    ElasticityControllerService elasticityControllerService;

    @Autowired
    ScalingService scalingService;

    @Autowired
    ComponentService componentService;

    @Autowired
    BalancerMediator balancerΜediator;

    // Registry the Traefik image is pulled from. Leave the credentials empty for a public registry.
    @Value("${elasticity.traefik.lambda-proxy.image:traefik:v1.7-alpine}")
    String traefikImage;

    @Value("${elasticity.traefik.registry.host:}")
    String traefikRegistry;

    @Value("${elasticity.traefik.registry.username:}")
    String traefikRegistryUsername;

    @Value("${elasticity.traefik.registry.password:}")
    String traefikRegistryPassword;

    // Port the Traefik admin/metrics interface is exposed on
    @Value("${elasticity.traefik.port:15568}")
    String traefikPort;

    @Override
    public void addComponentToDB() {
        logger.info("Traefik Lambda Proxy initialization has started...");
        ElasticityControllerComponent balancerComponent = new ElasticityControllerComponent();

        List<Component.CapabilityDrop> capabilitiesDrop = new ArrayList<>();
        capabilitiesDrop.add(Component.CapabilityDrop.DAC_OVERRIDE);
        capabilitiesDrop.add(Component.CapabilityDrop.FSETID);
        balancerComponent.setCapabilitiesDrop(capabilitiesDrop);

        List<Component.CapabilityAdd> capabilitiesAdd = new ArrayList<>();
        capabilitiesAdd.add(Component.CapabilityAdd.SYS_MODULE);
        capabilitiesAdd.add(Component.CapabilityAdd.SYS_TTY_CONFIG);
        balancerComponent.setCapabilitiesAdd(capabilitiesAdd);

        // Lambda Proxy (Traefik)

        balancerComponent.setName("LambdaProxy");
        balancerComponent.setDockerImage(traefikImage);
        balancerComponent.setDockerRegistry(traefikRegistry);
        balancerComponent.setDockerUsername(traefikRegistryUsername);
        balancerComponent.setDockerPassword(traefikRegistryPassword);
        balancerComponent.setArchitecture(eu.orchestrator.repository.domain.Component.Architecture.X86.name());
        //Add plugins if any
//        Plugin plugin = componentService.findPluginByName("pluginName");
//        balancerComponent.setPlugins(new TreeSet<>(plugin));
        List<String> labelList = new ArrayList<>();
        labelList.add("Load Balancer");
        balancerComponent.setLabelNameList(labelList);

        // Exposed Interface
        List<ElasticityControllerExposedInterface> balancerExposedInterfaceList = new ArrayList<>();
        ElasticityControllerExposedInterface lambdaProxyUIExposedInterface = new ElasticityControllerExposedInterface();
        lambdaProxyUIExposedInterface.setInterfaceType(ElasticityControllerExposedInterface.InterfaceType.ACCESS.name());
        lambdaProxyUIExposedInterface.setName("lambdaProxyUI");
        lambdaProxyUIExposedInterface.setPort(traefikPort);
        lambdaProxyUIExposedInterface.setVna("VNA0");
        lambdaProxyUIExposedInterface.setTransmissionProtocol(ElasticityControllerExposedInterface.TransmissionProtocol.TCP.name());

        balancerExposedInterfaceList.add(lambdaProxyUIExposedInterface);
        balancerComponent.setExposedInterfaces(balancerExposedInterfaceList);

        // Minimum Execution Requirements
        ElasticityControllerRequirement lbRequirement = new ElasticityControllerRequirement();
        lbRequirement.setGpuRequired(false);
        lbRequirement.setHypervisorType(ElasticityControllerRequirement.HypervisorType.ESXI.name());
        lbRequirement.setRam(2048);
        lbRequirement.setStorage(20);
        lbRequirement.setvCPUs(1);
        balancerComponent.setRequirement(lbRequirement);

        // Health Check
        ElasticityControllerHealthCheck loadBalancerHealthCheck = new ElasticityControllerHealthCheck();
        loadBalancerHealthCheck.setName("LambdaProxyHealthCheck");
        loadBalancerHealthCheck.setInterval(new Long(10));
        loadBalancerHealthCheck.setArgs(null);
        loadBalancerHealthCheck.setHttpURL("http://localhost:" + traefikPort + "/metrics");

        balancerComponent.setHealthCheck(loadBalancerHealthCheck);
        balancerComponent.setDockerExecutionUser(null);
        balancerComponent.setUlimitMemlockSoft(null);
        balancerComponent.setUlimitMemlockHard(null);

        elasticityControllerService.createElasticityControllerComponent(balancerComponent);

        logger.info("Traefik Load balancer has been added successfully!");
    }

    @Override
    public ElasticityMetadata getElasticityType() {
        ElasticityMetadata elasticityMetadata = new ElasticityMetadata();
        elasticityMetadata.setName("LAMBDA_FUNCTION");

        elasticityMetadata.setElasticityImpl(TraefikLambdaProxyOrchestratorAdapter.class.getName());
        elasticityMetadata.setElasticityBackendImpl(TraefikLambdaProxyBackendAdapter.class.getName());

        List<String> elasticityMode = new ArrayList<>();
        elasticityMode.add("HTTP");
        elasticityMode.add("gRPC");
        elasticityMetadata.setElasticityMode(elasticityMode);
        elasticityMetadata.setUserDefined(true);

        return  elasticityMetadata;
    }

    @Override
    public ElasticityObjects configureElasticityController(ElasticityObjects elasticityObjects) {
        // STEP0: Initialize workers map
        String componentName = "LambdaProxy";

        //TODO Check first if the componentNode exists
        Boolean elasticityControllerExistance = false;
        elasticityControllerExistance = componentService.checkElasticityControllerInstance(componentName, "", elasticityObjects);


        Map<Long, ComponentNodeInstance> workers = new HashMap<>();
        workers.put(elasticityObjects.getWorkerComponentNodeInstance().getComponentNodeInstanceID(), elasticityObjects.getWorkerComponentNodeInstance());
        elasticityObjects.setWorkers(workers);

        if(elasticityControllerExistance == false) {
            ComponentNode traefikComponentNode = elasticityControllerService.createElasticityControllerComponentNode(componentName, "LambdaProxy", "");
            ElasticityControllerInstanceArguments balancerInstanceArguments = new ElasticityControllerInstanceArguments();
            //TODO rethink the provider that is given

            balancerInstanceArguments.setStatusIDS(false);
            balancerInstanceArguments.setStatusIPS(false);

            String commandIPv6 = "-c,--api,--consul.prefix=" + elasticityObjects.getApplicationInstance().getApplication().getHexID() + "/" + elasticityObjects.getApplicationInstance()
                    .getHexID() + "/" + traefikComponentNode.getHexID()
                    + "/lbConfiguration,--consul.endpoint=" + balancerΜediator.getConsulIPv6URL();

            String command = "-c,--api,--consul.prefix=" + elasticityObjects.getApplicationInstance().getApplication().getHexID() + "/" + elasticityObjects.getApplicationInstance()
                    .getHexID() + "/" + traefikComponentNode.getHexID()
                    + "/lbConfiguration,--consul.endpoint=" + balancerΜediator.getConsulIPv4URL() + " --debug=true --traefiklog=true --accesslog=true";

            balancerInstanceArguments.setCommand(command);
            balancerInstanceArguments.setCommandIPv6(commandIPv6);

            balancerInstanceArguments.setHostname(null);
            balancerInstanceArguments.setDnsEntry(null);
            balancerInstanceArguments.setPrivilege(false);
            balancerInstanceArguments.setNetworkModeHost(false);
            balancerInstanceArguments.setSharedMemorySize(null);
            balancerInstanceArguments.setProvider(elasticityObjects.getWorkerComponentNodeInstance().getProvider());
            balancerInstanceArguments.setVolumeInstances(null);
            balancerInstanceArguments.setCapabilityAdds(traefikComponentNode.getComponent().getCapabilityAdds());
            balancerInstanceArguments.setCapabilityDrops(traefikComponentNode.getComponent().getCapabilityDrops());
            balancerInstanceArguments.setEnvironmentalVariableInstances(null);
            balancerInstanceArguments.setStatusIDS(elasticityObjects.getWorkerComponentNodeInstance().getStatusIDS());
            balancerInstanceArguments.setStatusIPS(elasticityObjects.getWorkerComponentNodeInstance().getStatusIPS());
            balancerInstanceArguments.setStatusSoc(elasticityObjects.getWorkerComponentNodeInstance().getStatusSOC());
            balancerInstanceArguments.setElasticityControllerOrchestratorAdapterImplementation("eu.orchestrator.elasticity.adapter.traefikLambdaProxy.TraefikLambdaProxyOrchestratorAdapter");

            elasticityObjects = elasticityControllerService.createElasticityControllerNodeInstance(traefikComponentNode, elasticityObjects, balancerInstanceArguments);

        }

        elasticityObjects = elasticityControllerService.fetchWorkerInterfaces(elasticityObjects);
        elasticityObjects = elasticityControllerService.addBalancedBy(elasticityObjects);

        elasticityObjects = elasticityControllerService.addComponentInterfacesToElasticityController(elasticityObjects);
        elasticityControllerService.moveConstraintsToElasticityController(elasticityObjects);
        elasticityObjects = elasticityControllerService.addWorkers(elasticityObjects);
        elasticityControllerService.linkWorkersWithElasticityController(elasticityObjects);

        elasticityObjects = elasticityControllerService.generateElasticityControllerService(elasticityObjects, ! elasticityControllerExistance, true, true, false);

        return elasticityObjects;
    }

    @Override
    public ScalingObjects scaleOut(ScalingObjects scalingRequest) {
        //TODO find what to do with the placement
        //TODO find what to do with the volumes

        ScalingObjects scalingResponse = scalingService.createWorkers(scalingRequest);
        if(!scalingResponse.getProceedWithScaling()){
            logger.severe("Scale out createWorkers failed");
            return scalingResponse;
        }

        scalingResponse = scalingService.getOrchestratorApplicationInstance(scalingResponse);
        return scalingResponse;
    }

    @Override
    public Boolean scaleIn(OrchestratorScalingRequest orchestratorScalingRequest) {
        //TODO find what to do with the volumes

        return scalingService.decreaseWorkers(orchestratorScalingRequest);
    }
}
