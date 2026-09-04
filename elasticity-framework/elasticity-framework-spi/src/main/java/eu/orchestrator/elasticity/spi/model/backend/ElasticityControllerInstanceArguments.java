package eu.orchestrator.elasticity.spi.model.backend;

import eu.orchestrator.repository.domain.*;

import java.util.Collection;
import java.util.List;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 10/7/2019
 */
public class ElasticityControllerInstanceArguments {

    private Boolean statusIDS;
    private Boolean statusIPS;
    private Boolean statusSoc;
    private String command;
    private String commandIPv6;
    private String hostname;
    private String dnsEntry;
    private Boolean privilege;
    private Boolean networkModeHost;
    private String sharedMemorySize;
    private Provider provider;
    private List<VolumeInstance> volumeInstances;
    private Collection<Component.CapabilityDrop> capabilityDrops;
    private Collection<Component.CapabilityAdd> capabilityAdds;
    private List<EnvironmentalVariableInstance> environmentalVariableInstances;
    private String elasticityControllerOrchestratorAdapterImplementation;

    public ElasticityControllerInstanceArguments() {
        this.statusIDS = false;
        this.statusIPS = false;
        this.command = null;
        this.commandIPv6 = null;
        this.hostname = null;
        this.dnsEntry = null;
        this.privilege = false;
        this.networkModeHost = false;
        this.sharedMemorySize = null;
        this.provider = null;
        this.volumeInstances = null;
        this.capabilityDrops = null;
        this.capabilityAdds = null;
        this.environmentalVariableInstances = null;
        this.elasticityControllerOrchestratorAdapterImplementation = null;
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

    public Boolean getStatusSoc() {
        return statusSoc;
    }

    public void setStatusSoc(Boolean statusSoc) {
        this.statusSoc = statusSoc;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getCommandIPv6() {
        return commandIPv6;
    }

    public void setCommandIPv6(String commandIPv6) {
        this.commandIPv6 = commandIPv6;
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

    public Boolean getPrivilege() {
        return privilege;
    }

    public void setPrivilege(Boolean privilege) {
        this.privilege = privilege;
    }

    public Boolean getNetworkModeHost() {
        return networkModeHost;
    }

    public void setNetworkModeHost(Boolean networkModeHost) {
        this.networkModeHost = networkModeHost;
    }

    public String getSharedMemorySize() {
        return sharedMemorySize;
    }

    public void setSharedMemorySize(String sharedMemorySize) {
        this.sharedMemorySize = sharedMemorySize;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public List<VolumeInstance> getVolumeInstances() {
        return volumeInstances;
    }

    public void setVolumeInstances(List<VolumeInstance> volumeInstances) {
        this.volumeInstances = volumeInstances;
    }

    public Collection<Component.CapabilityDrop> getCapabilityDrops() {
        return capabilityDrops;
    }

    public void setCapabilityDrops(Collection<Component.CapabilityDrop> capabilityDrops) {
        this.capabilityDrops = capabilityDrops;
    }

    public Collection<Component.CapabilityAdd> getCapabilityAdds() {
        return capabilityAdds;
    }

    public void setCapabilityAdds(Collection<Component.CapabilityAdd> capabilityAdds) {
        this.capabilityAdds = capabilityAdds;
    }

    public List<EnvironmentalVariableInstance> getEnvironmentalVariableInstances() {
        return environmentalVariableInstances;
    }

    public void setEnvironmentalVariableInstances(List<EnvironmentalVariableInstance> environmentalVariableInstances) {
        this.environmentalVariableInstances = environmentalVariableInstances;
    }

    public String getElasticityControllerOrchestratorAdapterImplementation() {
        return elasticityControllerOrchestratorAdapterImplementation;
    }

    public void setElasticityControllerOrchestratorAdapterImplementation(String elasticityControllerOrchestratorAdapterImplementation) {
        this.elasticityControllerOrchestratorAdapterImplementation = elasticityControllerOrchestratorAdapterImplementation;
    }
}
