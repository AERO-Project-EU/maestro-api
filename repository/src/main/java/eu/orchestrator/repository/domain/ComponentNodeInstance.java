package eu.orchestrator.repository.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Convert;
import org.hibernate.type.NumericBooleanConverter;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

@Entity
@Table(name = "component_node_instance")
public class ComponentNodeInstance implements Serializable, Comparable<ComponentNodeInstance> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long componentNodeInstanceID;

  @Column(nullable = true)
  private String name;

  @Column(nullable = true, name = "hex_id", unique = true)
  private String hexID;

  //    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // todo
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "component_node", nullable = true)
  private ComponentNode componentNode;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "application_instance", nullable = true)
  private ApplicationInstance applicationInstance;

  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "ssh_key", nullable = true)
  private SSHKey sshKey;

  //    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "provider", nullable = true)
  private Provider provider;

  //    @ManyToOne(fetch = FetchType.EAGER, optional = true)
//    @JoinColumn(name = "flavor_instance", nullable = true)
  @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "componentNodeInstance")
  private FlavorInstance flavorInstance;

  @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "componentNodeInstance")
  private ServerlessPropertiesInstance serverlessPropertiesInstance;

  @Column(nullable = true, name = "kubernetes_runtime_class_name")
  private String kubernetesRuntimeClassName;

  @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "componentNodeInstance")
  private HealthCheckInstance healthCheckInstance;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "componentNodeInstance")
  private List<LocationInstance> locationInstances;

  @Column(nullable = true)
  private String command;

  //    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "componentNodeInstance")
  private List<PluginInstance> pluginInstances;

  //    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "componentNodeInstance")
  private List<IDRuleSetInstance> iDRuleSetInstances;

  //    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "componentNodeInstance")
  private List<EnvironmentalVariableInstance> environmentalVariableInstances;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "componentNodeInstance")
  private List<DeviceInstance> deviceInstances;

  //    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "componentNodeInstance")
  private List<InterfaceInstance> interfaceInstances;

  //    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "componentNodeInstance")
  private List<VolumeInstance> volumeInstances;

  //TODO for astrid
  @ElementCollection(fetch = FetchType.LAZY)
  private Collection<SecurityEnablers> securityEnablers;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "componentNodeInstance")
  private List<SecurityConfigurationResult> securityConfigurationResultList;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "componentNodeInstance")
  private List<ComponentNodeInstanceHash> componentNodeInstanceHashList;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "componentNodeInstance")
  private SortedSet<ComponentNodeInstanceStatus> componentNodeInstanceStatuses = new TreeSet<>();

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "componentNodeInstance")
  private SortedSet<ComponentNodeInstanceAlert> componentNodeInstanceAlerts = new TreeSet<>();

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "componentNodeInstance")
  private SortedSet<ComponentNodeInstanceIP> componentNodeInstanceIPs = new TreeSet<>();

  @Column(nullable = true, name = "ids_status", columnDefinition = "TINYINT(1) UNSIGNED default '0'")
  @Convert(converter = NumericBooleanConverter.class)
  private Boolean statusIDS = false;

  @Column(nullable = true, name = "soc_status", columnDefinition = "TINYINT(1) UNSIGNED default '0'")
  @Convert(converter = NumericBooleanConverter.class)
  private Boolean statusSOC = false;

  @Column(nullable = true, name = "ips_status", columnDefinition = "TINYINT(1) UNSIGNED default '0'")
  @Convert(converter = NumericBooleanConverter.class)
  private Boolean statusIPS = false;

  @Column(nullable = true, name = "minimum_workers")
  private Integer minimumWorkers;

  @Column(nullable = true, name = "maximum_workers")
  private Integer maximumWorkers;

  @ElementCollection(fetch = FetchType.LAZY)
  private Collection<Component.CapabilityDrop> capabilityDrops;

  @ElementCollection(fetch = FetchType.LAZY)
  private Collection<Component.CapabilityAdd> capabilityAdds;

  @Column(name = "is_network_mode_host")
  private Boolean networkModeHost = false;

  @Column(name = "is_privilege")
  private Boolean privilege = false;

  @Column(nullable = true, name = "hostname")
  private String hostname;

  @Column(nullable = true, name = "shared_memory_size")
  private String sharedMemorySize;

  @Column(nullable = true, name = "dns_entry")
  private String dnsEntry;

  @Column(nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP", name = "date_created")
  @Temporal(TemporalType.TIMESTAMP)
  private Date dateCreated;

  @Column(nullable = false, name = "last_modified")
  private Date lastModified;

  // TESTING
  @Column(nullable = true, name = "load_balancer", columnDefinition = "TINYINT(1) UNSIGNED default '0'")
  @Convert(converter = NumericBooleanConverter.class)
  private Boolean loadBalancer = false;

  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "load_balanced_by", nullable = true)
  private ComponentNodeInstance loadBalancedBy;

  // Hibernate 6 forbids a bare @JoinColumn on @ManyToMany (it was ineffective anyway); use the default join table.
  @ManyToMany(fetch = FetchType.EAGER)
  private List<SecurityPolicy> securityPolicies;

  public ComponentNodeInstance() {
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

  public ComponentNode getComponentNode() {
    return componentNode;
  }

  public void setComponentNode(ComponentNode componentNode) {
    this.componentNode = componentNode;
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

  public String getCommand() {
    return command;
  }

  public void setCommand(String command) {
    this.command = command;
  }

  public List<EnvironmentalVariableInstance> getEnvironmentalVariableInstances() {
    return environmentalVariableInstances;
  }

  public void setEnvironmentalVariableInstances(
      List<EnvironmentalVariableInstance> environmentalVariableInstances) {
    this.environmentalVariableInstances = environmentalVariableInstances;
  }

  public List<InterfaceInstance> getInterfaceInstances() {
    return interfaceInstances;
  }

  public void setInterfaceInstances(List<InterfaceInstance> interfaceInstances) {
    this.interfaceInstances = interfaceInstances;
  }

  public List<VolumeInstance> getVolumeInstances() {
    return volumeInstances;
  }

  public void setVolumeInstances(List<VolumeInstance> volumeInstances) {
    this.volumeInstances = volumeInstances;
  }

  @Override
  public int compareTo(ComponentNodeInstance compareLabel) {
    String compareQuantity = compareLabel.getName();

    return this.name.compareTo(compareQuantity);
  }

  public ApplicationInstance getApplicationInstance() {
    return applicationInstance;
  }

  public void setApplicationInstance(ApplicationInstance applicationInstance) {
    this.applicationInstance = applicationInstance;
  }

  public SSHKey getSshKey() {
    return sshKey;
  }

  public void setSshKey(SSHKey sshKey) {
    this.sshKey = sshKey;
  }

  public FlavorInstance getFlavorInstance() {
    return flavorInstance;
  }

  public void setFlavorInstance(FlavorInstance flavorInstance) {
    this.flavorInstance = flavorInstance;
  }

  public ServerlessPropertiesInstance getServerlessPropertiesInstance() {
    return serverlessPropertiesInstance;
  }

  public void setServerlessPropertiesInstance(ServerlessPropertiesInstance serverlessPropertiesInstance) {
    this.serverlessPropertiesInstance = serverlessPropertiesInstance;
  }

  public String getKubernetesRuntimeClassName() {
    return kubernetesRuntimeClassName;
  }

  public void setKubernetesRuntimeClassName(String kubernetesRuntimeClassName) {
    this.kubernetesRuntimeClassName = kubernetesRuntimeClassName;
  }

  public List<LocationInstance> getLocationInstances() {
    return locationInstances;
  }

  public void setLocationInstances(List<LocationInstance> locationInstances) {
    this.locationInstances = locationInstances;
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

  public Boolean getStatusIDS() {
    return statusIDS;
  }

  public void setStatusIDS(Boolean statusIDS) {
    this.statusIDS = statusIDS;
  }

  public Boolean getStatusSOC() {
    return statusSOC;
  }

  public void setStatusSOC(Boolean statusSOC) {
    this.statusSOC = statusSOC;
  }

  public Boolean getStatusIPS() {
    return statusIPS;
  }

  public void setStatusIPS(Boolean statusIPS) {
    this.statusIPS = statusIPS;
  }

  public Boolean getLoadBalancer() {
    return loadBalancer;
  }

  public void setLoadBalancer(Boolean loadBalancer) {
    this.loadBalancer = loadBalancer;
  }

  public ComponentNodeInstance getLoadBalancedBy() {
    return loadBalancedBy;
  }

  public void setLoadBalancedBy(ComponentNodeInstance loadBalancedBy) {
    this.loadBalancedBy = loadBalancedBy;
  }

  public SortedSet<ComponentNodeInstanceStatus> getComponentNodeInstanceStatuses() {
    return componentNodeInstanceStatuses;
  }

  public void setComponentNodeInstanceStatuses(
      SortedSet<ComponentNodeInstanceStatus> componentNodeInstanceStatuses) {
    this.componentNodeInstanceStatuses = componentNodeInstanceStatuses;
  }

  public Provider getProvider() {
    return provider;
  }

  public void setProvider(Provider provider) {
    this.provider = provider;
  }

  public HealthCheckInstance getHealthCheckInstance() {
    return healthCheckInstance;
  }

  public void setHealthCheckInstance(HealthCheckInstance healthCheckInstance) {
    this.healthCheckInstance = healthCheckInstance;
  }

  public List<DeviceInstance> getDeviceInstances() {
    return deviceInstances;
  }

  public void setDeviceInstances(List<DeviceInstance> deviceInstances) {
    this.deviceInstances = deviceInstances;
  }

  public String getHexID() {
    return hexID;
  }

  public void setHexID(String hexID) {
    this.hexID = hexID;
  }

  public List<PluginInstance> getPluginInstances() {
    return pluginInstances;
  }

  public void setPluginInstances(List<PluginInstance> pluginInstances) {
    this.pluginInstances = pluginInstances;
  }

  public List<IDRuleSetInstance> getiDRuleSetInstances() {
    return iDRuleSetInstances;
  }

  public void setiDRuleSetInstances(List<IDRuleSetInstance> iDRuleSetInstances) {
    this.iDRuleSetInstances = iDRuleSetInstances;
  }

  public SortedSet<ComponentNodeInstanceAlert> getComponentNodeInstanceAlerts() {
    return componentNodeInstanceAlerts;
  }

  public void setComponentNodeInstanceAlerts(
      SortedSet<ComponentNodeInstanceAlert> componentNodeInstanceAlerts) {
    this.componentNodeInstanceAlerts = componentNodeInstanceAlerts;
  }

  public SortedSet<ComponentNodeInstanceIP> getComponentNodeInstanceIPs() {
    return componentNodeInstanceIPs;
  }

  public void setComponentNodeInstanceIPs(
      SortedSet<ComponentNodeInstanceIP> componentNodeInstanceIPs) {
    this.componentNodeInstanceIPs = componentNodeInstanceIPs;
  }

  public Collection<SecurityEnablers> getSecurityEnablers() {
    return securityEnablers;
  }

  public void setSecurityEnablers(Collection<SecurityEnablers> securityEnablersList) {
    this.securityEnablers = securityEnablersList;
  }

  public List<SecurityConfigurationResult> getSecurityConfigurationResultList() {
    return securityConfigurationResultList;
  }

  public void setSecurityConfigurationResultList(List<SecurityConfigurationResult> securityConfigurationResultList) {
    this.securityConfigurationResultList = securityConfigurationResultList;
  }

  public List<ComponentNodeInstanceHash> getComponentNodeInstanceHashList() {
    return componentNodeInstanceHashList;
  }

  public void setComponentNodeInstanceHashList(List<ComponentNodeInstanceHash> componentNodeInstanceHashList) {
    this.componentNodeInstanceHashList = componentNodeInstanceHashList;
  }

  public List<SecurityPolicy> getSecurityPolicies() {
    return securityPolicies;
  }

  public void setSecurityPolicies(List<SecurityPolicy> securityPolicies) {
    this.securityPolicies = securityPolicies;
  }

  /**
   * Inner enum class
   */

  public enum  SecurityEnablers{


    CONFIGURATION_INTEGRITY_VERIFICATION("Configuration Integrity Verification"),
    RUNTIME_FILE_INTEGRITY("Runtime File Integrity"),
    FORENSIC_ACTIVATION("Forensic Activation");
//    TSS_TRACING("TSS Tracing"),
//    BOOT_TIME_FUZZY("Boot-time Fuzzing");

    private String friendlyName;

    SecurityEnablers(String friendlyName) {
      this.friendlyName = friendlyName;
    }

    public String getFriendlyName() {
      return friendlyName;
    }
  }

  public User getUser() {
    return applicationInstance.getUser();
  }

  public Boolean hasEditAllowance(User user) {
    if (user != null && (getUser().equals(user) ||
            user.isAdmin() ||
            (user.isOrganizationAdmin() && user.getOrganization()
                    .equals(getUser().getOrganization())))) {
      return true;
    }
    return false;
  }

  public Boolean hasDeleteAllowance(User user) {
    if (user != null && (getUser().equals(user) ||
            user.isAdmin() || (user.isOrganizationAdmin() && user.getOrganization()
            .equals(getUser().getOrganization())))) {
      return true;
    }
    return false;
  }

}
