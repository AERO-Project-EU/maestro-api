package eu.orchestrator.backend.transfer;

import eu.orchestrator.repository.domain.ServerlessPropertiesInstance;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class ComponentNodeInstanceTO implements Serializable {

    private Long componentNodeInstanceID;
    private String name;
    private String hexID;
    private ComponentNodeTO componentNode;
    private SSHKeyTO sshKey;
    private ProviderTO provider;
    private FlavorInstanceTO flavorInstance;
    private ServerlessPropertiesInstanceTO serverlessPropertiesInstance;
    private HealthCheckInstanceTO healthCheckInstance;
    private List<LocationInstanceTO> locationInstances;
    private String command;
    private String kubernetesRuntimeClassName;
    private List<PluginInstanceTO> pluginInstances;
    private List<IDRuleSetInstanceTO> iDRuleSetInstances;
    private List<EnvironmentalVariableInstanceTO> environmentalVariableInstances;
    private List<DeviceInstanceTO> deviceInstances;
    private List<InterfaceInstanceTO> interfaceInstances;
    private List<VolumeInstanceTO> volumeInstances;
    private List<IPTO> componentNodeInstanceIPs;
    private List<AlertTO> componentNodeInstanceAlerts;
    private List<StatusTO> componentNodeInstanceStatuses;
    private Boolean statusIDS = false;
    private Boolean statusIPS = false;
    private Boolean statusSOC = false;
    private Integer minimumWorkers;
    private Integer maximumWorkers;
    private Collection<String> capabilityDrops;
    private Collection<String> capabilityAdds;
    private Collection<String> securityEnablers;
    private Boolean networkModeHost = false;
    private Boolean privilege = false;
    private String hostname;
    private String sharedMemorySize;
    private String dnsEntry;
    private Boolean allowEdit;
    private Boolean allowDelete;
    private Date dateCreated;
    private Date lastModified;

    // TESTING
    private Boolean loadBalancer = false;
    private ComponentNodeInstanceTO loadBalancedBy;


    public ComponentNodeInstanceTO() {
    }

    public Long getComponentNodeInstanceID() {
        return componentNodeInstanceID;
    }

    public void setComponentNodeInstanceID(Long componentNodeInstanceID) {
        this.componentNodeInstanceID = componentNodeInstanceID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHexID() {
        return hexID;
    }

    public void setHexID(String hexID) {
        this.hexID = hexID;
    }

    public ComponentNodeTO getComponentNode() {
        return componentNode;
    }

    public void setComponentNode(ComponentNodeTO componentNode) {
        this.componentNode = componentNode;
    }

    public SSHKeyTO getSshKey() {
        return sshKey;
    }

    public void setSshKey(SSHKeyTO sshKey) {
        this.sshKey = sshKey;
    }

    public ProviderTO getProvider() {
        return provider;
    }

    public void setProvider(ProviderTO provider) {
        this.provider = provider;
    }

    public FlavorInstanceTO getFlavorInstance() {
        return flavorInstance;
    }

    public void setFlavorInstance(FlavorInstanceTO flavorInstance) {
        this.flavorInstance = flavorInstance;
    }

    public ServerlessPropertiesInstanceTO getServerlessPropertiesInstance() {
        return serverlessPropertiesInstance;
    }

    public void setServerlessPropertiesInstance(ServerlessPropertiesInstanceTO serverlessPropertiesInstance) {
        this.serverlessPropertiesInstance = serverlessPropertiesInstance;
    }

    public HealthCheckInstanceTO getHealthCheckInstance() {
        return healthCheckInstance;
    }

    public void setHealthCheckInstance(HealthCheckInstanceTO healthCheckInstance) {
        this.healthCheckInstance = healthCheckInstance;
    }

    public List<LocationInstanceTO> getLocationInstances() {
        return locationInstances;
    }

    public void setLocationInstances(List<LocationInstanceTO> locationInstances) {
        this.locationInstances = locationInstances;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getKubernetesRuntimeClassName() {
        return kubernetesRuntimeClassName;
    }

    public void setKubernetesRuntimeClassName(String kubernetesRuntimeClassName) {
        this.kubernetesRuntimeClassName = kubernetesRuntimeClassName;
    }

    public List<PluginInstanceTO> getPluginInstances() {
        return pluginInstances;
    }

    public void setPluginInstances(List<PluginInstanceTO> pluginInstances) {
        this.pluginInstances = pluginInstances;
    }

    public List<IDRuleSetInstanceTO> getiDRuleSetInstances() {
        return iDRuleSetInstances;
    }

    public void setiDRuleSetInstances(List<IDRuleSetInstanceTO> iDRuleSetInstances) {
        this.iDRuleSetInstances = iDRuleSetInstances;
    }

    public List<EnvironmentalVariableInstanceTO> getEnvironmentalVariableInstances() {
        return environmentalVariableInstances;
    }

    public void setEnvironmentalVariableInstances(
            List<EnvironmentalVariableInstanceTO> environmentalVariableInstances) {
        this.environmentalVariableInstances = environmentalVariableInstances;
    }

    public List<DeviceInstanceTO> getDeviceInstances() {
        return deviceInstances;
    }

    public void setDeviceInstances(List<DeviceInstanceTO> deviceInstances) {
        this.deviceInstances = deviceInstances;
    }

    public List<InterfaceInstanceTO> getInterfaceInstances() {
        return interfaceInstances;
    }

    public void setInterfaceInstances(List<InterfaceInstanceTO> interfaceInstances) {
        this.interfaceInstances = interfaceInstances;
    }

    public List<VolumeInstanceTO> getVolumeInstances() {
        return volumeInstances;
    }

    public void setVolumeInstances(List<VolumeInstanceTO> volumeInstances) {
        this.volumeInstances = volumeInstances;
    }

    public List<IPTO> getComponentNodeInstanceIPs() {
        return componentNodeInstanceIPs;
    }

    public void setComponentNodeInstanceIPs(
            List<IPTO> componentNodeInstanceIPs) {
        this.componentNodeInstanceIPs = componentNodeInstanceIPs;
    }

    public List<AlertTO> getComponentNodeInstanceAlerts() {
        return componentNodeInstanceAlerts;
    }

    public void setComponentNodeInstanceAlerts(
            List<AlertTO> componentNodeInstanceAlerts) {
        this.componentNodeInstanceAlerts = componentNodeInstanceAlerts;
    }

    public List<StatusTO> getComponentNodeInstanceStatuses() {
        return componentNodeInstanceStatuses;
    }

    public void setComponentNodeInstanceStatuses(
            List<StatusTO> componentNodeInstanceStatuses) {
        this.componentNodeInstanceStatuses = componentNodeInstanceStatuses;
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

    public Boolean getStatusSOC() {
        return statusSOC;
    }

    public void setStatusSOC(Boolean statusSOC) {
        this.statusSOC = statusSOC;
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

    public Boolean getLoadBalancer() {
        return loadBalancer;
    }

    public void setLoadBalancer(Boolean loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    public ComponentNodeInstanceTO getLoadBalancedBy() {
        return loadBalancedBy;
    }

    public void setLoadBalancedBy(ComponentNodeInstanceTO loadBalancedBy) {
        this.loadBalancedBy = loadBalancedBy;
    }


    public Collection<String> getCapabilityDrops() {
        return capabilityDrops;
    }

    public void setCapabilityDrops(Collection<String> capabilityDrops) {
        this.capabilityDrops = capabilityDrops;
    }

    public Collection<String> getCapabilityAdds() {
        return capabilityAdds;
    }

    public void setCapabilityAdds(Collection<String> capabilityAdds) {
        this.capabilityAdds = capabilityAdds;
    }

    public Collection<String> getSecurityEnablers() {
        return securityEnablers;
    }

    public void setSecurityEnablers(Collection<String> securityEnablers) {
        this.securityEnablers = securityEnablers;
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

    public String getSharedMemorySize() {
        return sharedMemorySize;
    }

    public void setSharedMemorySize(String sharedMemorySize) {
        this.sharedMemorySize = sharedMemorySize;
    }

    public String getDnsEntry() {
        return dnsEntry;
    }

    public void setDnsEntry(String dnsEntry) {
        this.dnsEntry = dnsEntry;
    }

    public Boolean getAllowEdit() {
        return allowEdit;
    }

    public void setAllowEdit(Boolean allowEdit) {
        this.allowEdit = allowEdit;
    }

    public Boolean getAllowDelete() {
        return allowDelete;
    }

    public void setAllowDelete(Boolean allowDelete) {
        this.allowDelete = allowDelete;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

}
