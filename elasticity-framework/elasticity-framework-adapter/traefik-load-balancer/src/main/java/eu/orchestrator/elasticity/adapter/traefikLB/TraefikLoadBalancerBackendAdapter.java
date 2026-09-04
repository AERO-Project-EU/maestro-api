package eu.orchestrator.elasticity.adapter.traefikLB;

import eu.orchestrator.elasticity.spi.model.backend.*;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorScalingRequest;
import eu.orchestrator.elasticity.spi.service.backend.BalancerMediator;
import eu.orchestrator.elasticity.spi.service.backend.ComponentService;
import eu.orchestrator.elasticity.spi.service.backend.ScalingService;
import eu.orchestrator.elasticity.spi.adapter.ElasticityFrameworkBackend;
import eu.orchestrator.elasticity.spi.service.backend.ElasticityControllerService;
import eu.orchestrator.repository.domain.*;
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
public class TraefikLoadBalancerBackendAdapter implements ElasticityFrameworkBackend {
    private static final Logger logger = Logger.getLogger(TraefikLoadBalancerBackendAdapter.class.getName());

    @Autowired
    ElasticityControllerService elasticityControllerService;

    @Autowired
    ScalingService scalingService;

    @Autowired
    ComponentService componentService;

    @Autowired
    BalancerMediator balancerΜediator;

    // Registry the Traefik image is pulled from. Leave the credentials empty for a public registry.
    @Value("${elasticity.traefik.load-balancer.image:traefik:alpine}")
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
        logger.info("Traefik Load Balancer initialization has started...");
        ElasticityControllerComponent balancerComponent = new ElasticityControllerComponent();

        List<Component.CapabilityDrop> capabilitiesDrop = new ArrayList<>();
        capabilitiesDrop.add(Component.CapabilityDrop.DAC_OVERRIDE);
        capabilitiesDrop.add(Component.CapabilityDrop.FSETID);
        balancerComponent.setCapabilitiesDrop(capabilitiesDrop);

        List<Component.CapabilityAdd> capabilitiesAdd = new ArrayList<>();
        capabilitiesAdd.add(Component.CapabilityAdd.SYS_MODULE);
        capabilitiesAdd.add(Component.CapabilityAdd.SYS_TTY_CONFIG);
        balancerComponent.setCapabilitiesAdd(capabilitiesAdd);

        // Load Balancer (Traefik)

        balancerComponent.setName("Traefik");
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
        ElasticityControllerExposedInterface lbUIExposedInterface = new ElasticityControllerExposedInterface();
        lbUIExposedInterface.setInterfaceType(ElasticityControllerExposedInterface.InterfaceType.ACCESS.name());
        lbUIExposedInterface.setName("traefikUI");
        lbUIExposedInterface.setPort(traefikPort);
        lbUIExposedInterface.setVna("VNA0");
        lbUIExposedInterface.setTransmissionProtocol(ElasticityControllerExposedInterface.TransmissionProtocol.TCP.name());

        balancerExposedInterfaceList.add(lbUIExposedInterface);
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
        loadBalancerHealthCheck.setName("LoadBalancerHealthCheck");
        loadBalancerHealthCheck.setInterval(new Long(10));
        loadBalancerHealthCheck.setArgs(null);
        loadBalancerHealthCheck.setHttpURL("http://localhost:" + traefikPort + "/metrics");

        balancerComponent.setHealthCheck(loadBalancerHealthCheck);
        balancerComponent.setDockerExecutionUser(null);
        balancerComponent.setUlimitMemlockSoft(null);
        balancerComponent.setUlimitMemlockHard(null);

        System.out.println(elasticityControllerService);
        elasticityControllerService.createElasticityControllerComponent(balancerComponent);

        logger.info("Traefik Load balancer has been added successfully!");
    }

    @Override
    public ElasticityMetadata getElasticityType() {
        ElasticityMetadata elasticityMetadata = new ElasticityMetadata();
        elasticityMetadata.setName("HORIZONTAL");

        elasticityMetadata.setElasticityImpl(TraefikLoadBalancerOrchestratorAdapter.class.getName());
        elasticityMetadata.setElasticityBackendImpl(TraefikLoadBalancerBackendAdapter.class.getName());
        elasticityMetadata.setUserDefined(true);

        return  elasticityMetadata;
    }

    @Override
    public ElasticityObjects configureElasticityController(ElasticityObjects elasticityObjects) {
        // STEP0: Initialize workers map

        Map<Long, ComponentNodeInstance> workers = new HashMap<>();
        workers.put(elasticityObjects.getWorkerComponentNodeInstance().getComponentNodeInstanceID(), elasticityObjects.getWorkerComponentNodeInstance());
        elasticityObjects.setWorkers(workers);

        String componentName = "Traefik";
        String balancedComponentName = elasticityObjects.getWorkerComponentNodeInstance().getName();
        ComponentNode traefikComponentNode = elasticityControllerService.createElasticityControllerComponentNode(componentName, balancedComponentName, "LB");

        //TODO rethink the provider that is given
        ElasticityControllerInstanceArguments balancerInstanceArguments = new ElasticityControllerInstanceArguments();

        balancerInstanceArguments.setStatusIDS(false);
        balancerInstanceArguments.setStatusIPS(false);

        String commandIPv6 = "-c,--api,--consul.prefix=" + elasticityObjects.getApplicationInstance().getApplication().getHexID() + "/" + elasticityObjects.getApplicationInstance()
                .getHexID() + "/" + traefikComponentNode.getHexID()
                + "/lbConfiguration,--consul.endpoint=" + balancerΜediator.getConsulIPv6URL();

        String command = "-c,--api,--consul.prefix=" + elasticityObjects.getApplicationInstance().getApplication().getHexID() + "/" + elasticityObjects.getApplicationInstance()
                .getHexID() + "/" + traefikComponentNode.getHexID()
                + "/lbConfiguration,--consul.endpoint=" + balancerΜediator.getConsulIPv4URL();

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
        balancerInstanceArguments.setElasticityControllerOrchestratorAdapterImplementation("eu.orchestrator.elasticity.adapter.traefikLB.TraefikLoadBalancerOrchestratorAdapter");

        elasticityObjects = elasticityControllerService.createElasticityControllerNodeInstance(traefikComponentNode, elasticityObjects, balancerInstanceArguments);

        elasticityObjects = elasticityControllerService.fetchWorkerInterfaces(elasticityObjects);
        elasticityObjects = elasticityControllerService.addBalancedBy(elasticityObjects);

        elasticityObjects = elasticityControllerService.addComponentInterfacesToElasticityController(elasticityObjects);
        elasticityControllerService.moveConstraintsToElasticityController(elasticityObjects);
        elasticityObjects = elasticityControllerService.addWorkers(elasticityObjects);
        elasticityControllerService.linkWorkersWithElasticityController(elasticityObjects);

        elasticityObjects = elasticityControllerService.generateElasticityControllerService(elasticityObjects, true,
                true, true, false);

        return elasticityObjects;
    }

    @Override
    public ScalingObjects scaleOut(ScalingObjects scalingRequest) {
        //TODO find what to do with the placement
        //TODO find what to do with the volumes

        ScalingObjects scalingResponse = scalingService.createWorkers(scalingRequest);
        if(!scalingResponse.getProceedWithScaling()){
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
