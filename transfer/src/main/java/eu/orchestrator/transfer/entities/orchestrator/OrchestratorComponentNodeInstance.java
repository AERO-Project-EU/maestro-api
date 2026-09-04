package eu.orchestrator.transfer.entities.orchestrator;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class OrchestratorComponentNodeInstance implements Serializable {

    String componentNodeInstanceID;
    String componentNodeInstanceHexID;
    String componentNodeInstanceName;
    String componentNodeID;
    String componentNodeHexID;
    String componentNodeName;

    // Placement
    String providerID;

    String flavorID;
    String image;
    String registry;
    String sshKey;

    //
    Boolean statusIDS;
    Boolean statusIPS;
    Integer minimumWorkers;
    Integer maximumWorkers;

    List<String> command;
    List<OrchestratorDependency> dependsOn;

    Map<String, String> environmentalVariables;
    Map<String, String> devices;
    Map<String, String> volumes;

    //tells as if the service is a load balancer service
    List<OrchestratorPort> ports;
    Boolean loadBalancer;
    Boolean lambdaProxy;

    OrchestratorElasticity monitoringElasticity;
    OrchestratorHealthCheck healthCheck;
    OrchestratorFlavor flavor;
    List<OrchestratorLocation> locations;
    List<OrchestratorPlugin> plugins;

    Collection<String> capabilityAdds;
    Collection<String> capabilityDrops;
    Boolean networkModeHost;
    Boolean privilege;
    String hostname;
    String dnsEntry;
    String sharedMemorySize;
    String dockerUsername;
    String dockerPassword;

    private Object controllerMetadata;
    private Boolean isController;
    private String elasticityControllerAdapterImplementation;
    private String balancedByComponentNodeHexID;
    private String ulimitMemlockSoft;
    private String ulimitMemlockHard;
    private String dockerExecutionUser;

    private Boolean hasEnableSecurity;
    private Boolean produceHashes;
    private Boolean statusSoc;

    // add somehow the adapter of the proper controller

    public OrchestratorComponentNodeInstance() {
    }

    public String getComponentNodeInstanceID() {
        return componentNodeInstanceID;
    }

    public void setComponentNodeInstanceID(String componentNodeInstanceID) {
        this.componentNodeInstanceID = componentNodeInstanceID;
    }

    public String getComponentNodeInstanceName() {
        return componentNodeInstanceName;
    }

    public void setComponentNodeInstanceName(String componentNodeInstanceName) {
        this.componentNodeInstanceName = componentNodeInstanceName;
    }

    public Object getControllerMetadata() {
        return controllerMetadata;
    }

    public void setControllerMetadata(Object controllerMetadata) {
        this.controllerMetadata = controllerMetadata;
    }

    public Boolean getController() {
        return isController;
    }

    public void setController(Boolean controller) {
        isController = controller;
    }

    public String getComponentNodeID() {
        return componentNodeID;
    }

    public void setComponentNodeID(String componentNodeID) {
        this.componentNodeID = componentNodeID;
    }

    public String getComponentNodeName() {
        return componentNodeName;
    }

    public void setComponentNodeName(String componentNodeName) {
        this.componentNodeName = componentNodeName;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getRegistry() {
        return registry;
    }

    public void setRegistry(String registry) {
        this.registry = registry;
    }

    public List<String> getCommand() {
        return command;
    }

    public void setCommand(List<String> command) {
        this.command = command;
    }

    public List<OrchestratorDependency> getDependsOn() {
        return dependsOn;
    }

    public void setDependsOn(List<OrchestratorDependency> dependsOn) {
        this.dependsOn = dependsOn;
    }

    public List<OrchestratorPort> getPorts() {
        return ports;
    }

    public void setPorts(List<OrchestratorPort> ports) {
        this.ports = ports;
    }

    public Boolean getLoadBalancer() {
        return loadBalancer;
    }

    public void setLoadBalancer(Boolean loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    public Map<String, String> getEnvironmentalVariables() {
        return environmentalVariables;
    }

    public void setEnvironmentalVariables(Map<String, String> environmentalVariables) {
        this.environmentalVariables = environmentalVariables;
    }

    public OrchestratorElasticity getMonitoringElasticity() {
        return monitoringElasticity;
    }

    public void setMonitoringElasticity(OrchestratorElasticity monitoringElasticity) {
        this.monitoringElasticity = monitoringElasticity;
    }

    public OrchestratorFlavor getFlavor() {
        return flavor;
    }

    public void setFlavor(OrchestratorFlavor flavor) {
        this.flavor = flavor;
    }

    public List<OrchestratorLocation> getLocations() {
        return locations;
    }

    public void setLocations(List<OrchestratorLocation> locations) {
        this.locations = locations;
    }

    public String getSshKey() {
        return sshKey;
    }

    public void setSshKey(String sshKey) {
        this.sshKey = sshKey;
    }

    public String getProviderID() {
        return providerID;
    }

    public void setProviderID(String providerID) {
        this.providerID = providerID;
    }

    public Boolean getStatusIDS() {
        return statusIDS;
    }

    public void setStatusIDS(Boolean statusIDS) {
        this.statusIDS = statusIDS;
    }

    public Boolean getStatusIPS() {
        return statusIPS;
    }

    public void setStatusIPS(Boolean statusIPS) {
        this.statusIPS = statusIPS;
    }

    public Integer getMinimumWorkers() {
        return minimumWorkers;
    }

    public void setMinimumWorkers(Integer minimumWorkers) {
        this.minimumWorkers = minimumWorkers;
    }

    public Integer getMaximumWorkers() {
        return maximumWorkers;
    }

    public void setMaximumWorkers(Integer maximumWorkers) {
        this.maximumWorkers = maximumWorkers;
    }

    public Boolean getLambdaProxy() {
        return lambdaProxy;
    }

    public void setLambdaProxy(Boolean lambdaProxy) {
        this.lambdaProxy = lambdaProxy;
    }

    public OrchestratorHealthCheck getHealthCheck() {
        return healthCheck;
    }

    public void setHealthCheck(OrchestratorHealthCheck healthCheck) {
        this.healthCheck = healthCheck;
    }

    public Map<String, String> getDevices() {
        return devices;
    }

    public void setDevices(Map<String, String> devices) {
        this.devices = devices;
    }


    public Map<String, String> getVolumes() {
        return volumes;
    }

    public void setVolumes(Map<String, String> volumes) {
        this.volumes = volumes;
    }

    public String getComponentNodeInstanceHexID() {
        return componentNodeInstanceHexID;
    }

    public void setComponentNodeInstanceHexID(String componentNodeInstanceHexID) {
        this.componentNodeInstanceHexID = componentNodeInstanceHexID;
    }

    public String getComponentNodeHexID() {
        return componentNodeHexID;
    }

    public void setComponentNodeHexID(String componentNodeHexID) {
        this.componentNodeHexID = componentNodeHexID;
    }

    public String getFlavorID() {
        return flavorID;
    }

    public void setFlavorID(String flavorID) {
        this.flavorID = flavorID;
    }

    public List<OrchestratorPlugin> getPlugins() {
        return plugins;
    }

    public void setPlugins(List<OrchestratorPlugin> plugins) {
        this.plugins = plugins;
    }

    public Collection<String> getCapabilityAdds() {
        return capabilityAdds;
    }

    public void setCapabilityAdds(Collection<String> capabilityAdds) {
        this.capabilityAdds = capabilityAdds;
    }

    public Collection<String> getCapabilityDrops() {
        return capabilityDrops;
    }

    public void setCapabilityDrops(Collection<String> capabilityDrops) {
        this.capabilityDrops = capabilityDrops;
    }

    public Boolean getNetworkModeHost() {
        return networkModeHost;
    }

    public void setNetworkModeHost(Boolean networkModeHost) {
        this.networkModeHost = networkModeHost;
    }

    public Boolean getPrivilege() {
        return privilege;
    }

    public void setPrivilege(Boolean privilege) {
        this.privilege = privilege;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getDnsEntry() {
        return dnsEntry;
    }

    public void setDnsEntry(String dnsEntry) {
        this.dnsEntry = dnsEntry;
    }

    public String getSharedMemorySize() {
        return sharedMemorySize;
    }

    public void setSharedMemorySize(String sharedMemorySize) {
        this.sharedMemorySize = sharedMemorySize;
    }

    public String getDockerUsername() {
        return dockerUsername;
    }

    public void setDockerUsername(String dockerUsername) {
        this.dockerUsername = dockerUsername;
    }

    public String getDockerPassword() {
        return dockerPassword;
    }

    public void setDockerPassword(String dockerPassword) {
        this.dockerPassword = dockerPassword;
    }

    public String getElasticityControllerAdapterImplementation() {
        return elasticityControllerAdapterImplementation;
    }

    public void setElasticityControllerAdapterImplementation(String elasticityControllerAdapterImplementation) {
        this.elasticityControllerAdapterImplementation = elasticityControllerAdapterImplementation;
    }

    public String getBalancedByComponentNodeHexID() {
        return balancedByComponentNodeHexID;
    }

    public void setBalancedByComponentNodeHexID(String balancedByComponentNodeHexID) {
        this.balancedByComponentNodeHexID = balancedByComponentNodeHexID;
    }

    public String getUlimitMemlockSoft() {
        return ulimitMemlockSoft;
    }

    public void setUlimitMemlockSoft(String ulimitMemlockSoft) {
        this.ulimitMemlockSoft = ulimitMemlockSoft;
    }

    public String getUlimitMemlockHard() {
        return ulimitMemlockHard;
    }

    public void setUlimitMemlockHard(String ulimitMemlockHard) {
        this.ulimitMemlockHard = ulimitMemlockHard;
    }

    public String getDockerExecutionUser() {
        return dockerExecutionUser;
    }

    public void setDockerExecutionUser(String dockerExecutionUser) {
        this.dockerExecutionUser = dockerExecutionUser;
    }

    public Boolean getHasEnableSecurity() {
        return hasEnableSecurity;
    }

    public void setHasEnableSecurity(Boolean hasEnableSecurity) {
        this.hasEnableSecurity = hasEnableSecurity;
    }

    public Boolean getProduceHashes() {
        return produceHashes;
    }

    public void setProduceHashes(Boolean produceHashes) {
        this.produceHashes = produceHashes;
    }

    public Boolean getStatusSoc() {
        return statusSoc;
    }

    public void setStatusSoc(Boolean statusSoc) {
        this.statusSoc = statusSoc;
    }
}
