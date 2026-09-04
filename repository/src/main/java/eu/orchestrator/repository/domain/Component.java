package eu.orchestrator.repository.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import jakarta.persistence.*;

import jakarta.persistence.Convert;
import org.hibernate.type.NumericBooleanConverter;

@Entity
@Table(name = "component")
public class Component implements Serializable, Comparable<Component> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private String name;

    @Column(nullable = true, name = "hex_id", unique = true)
    private String hexID;

    @Column(nullable = true, name = "icon_content_type")
    private String iconContentType;

    @Column(nullable = true, name = "icon_filename")
    private String iconFilename;

    @Column(nullable = true, name = "icon_path")
    private String iconPath;

    @Column(nullable = true, name = "architecture")
    private String architecture;

    @Column(nullable = true, name = "elasticityControllerMode")
    private String elasticityControllerMode;

    @Column(nullable = true, name = "icon_content")
    private byte[] iconContent;

    @Column(nullable = true, name = "icon_base64")
    private String iconBase64;

    @Column(nullable = true, name = "docker_image")
    private String dockerImage;

    @Column(nullable = true, name = "docker_registry")
    private String dockerRegistry;

    @Transient
    private Boolean dockerCredentialsUsing = false;

    @Transient
    private Boolean dockerCustomRegistry = false;

    @Column(nullable = true, name = "docker_username")
    private String dockerUsername;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = true, name = "docker_password", length = 3000)
    private String dockerPassword;

    //    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
//    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "component")
//    @OrderBy(value = "name")
//    @ManyToMany(fetch = FetchType.EAGER)
//    @JoinTable(
//            name = "component_interface",
//            joinColumns = @JoinColumn(name = "component_id", referencedColumnName = "id"),
//            inverseJoinColumns = @JoinColumn(name = "interface_id", referencedColumnName = "id")
//    )
//    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
//    private SortedSet<Interface> exposedInterfaces = new TreeSet<>();

    //  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "component")
    private List<Interface> exposedInterfaces;

    //      @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "component")
    private List<GraphLink> requiredInterfaces;

    //      @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "component",optional = false)
    private Requirement requirement;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "component")
    private ServerlessProperties serverlessProperties;

    @Column(nullable = true, name = "kubernetes_runtime_class_name")
    private String kubernetesRuntimeClassName;

    //      @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "component")
    private HealthCheck healthCheck;

    //      @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "component")
    private List<EnvironmentalVariable> environmentalVariables;

    //      @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "component")
    private List<Device> devices;

    @Column(nullable = true, name = "elasticityController")
    private String elasticityController;

    //      @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "component")
    private List<Volume> volumes;

    /*@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "component")
    private List<Tag> tags;*/

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "component_label",
            joinColumns = @JoinColumn(name = "component_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "label_id", referencedColumnName = "id")
    )
//    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private SortedSet<Label> labels = new TreeSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "component_plugin",
            joinColumns = @JoinColumn(name = "component_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "plugin_id", referencedColumnName = "id")
    )
//    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private SortedSet<Plugin> plugins = new TreeSet<>();

    @Column(nullable = true, name = "is_public", columnDefinition = "TINYINT(1) UNSIGNED default '0'")
    @Convert(converter = NumericBooleanConverter.class)
    private Boolean publicComponent;

    @Column(name = "is_network_mode_host")
    private Boolean networkModeHost = false;

    @Column(name = "is_privilege")
    private Boolean privilege = false;

    @Column(nullable = true, name = "hostname")
    private String hostname;

    @Column(nullable = true, name = "shared_memory_size")
    private String sharedMemorySize;

    @Column(nullable = true)
    private String command;

    @ElementCollection(fetch = FetchType.LAZY)
    private Collection<CapabilityDrop> capabilityDrops;

    @ElementCollection(fetch = FetchType.LAZY)
    private Collection<CapabilityAdd> capabilityAdds;

    @Column(name = "ulimit_memlock_soft")
    private String ulimitMemlockSoft;

    @Column(name = "ulimit_memlock_hard")
    private String ulimitMemlockHard;

    @Column(name = "docker_execution_user")
    private String dockerExecutionUser;

    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "user", nullable = true)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "organization", nullable = true)
    private Organization organization;

    @Column(nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP", name = "date_created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;

    @Column(nullable = false, name = "last_modified")
    private Date lastModified;

    public Component() {
    }

    public enum CapabilityDrop {
        //The lists Linux capability options which are allowed by default and can be dropped

        SETPCAP("SETPCAP"),
        MKNOD("MKNOD"),
        AUDIT_WRITE("AUDIT_WRITE"),
        CHOWN("CHOWN"),
        NET_RAW("NET_RAW"),
        DAC_OVERRIDE("DAC_OVERRIDE"),
        FOWNER("FOWNER"),
        FSETID("FSETID"),
        KILL("KILL"),
        SETGID("SETGID"),
        SETUID("SETUID"),
        NET_BIND_SERVICE("NET_BIND_SERVICE"),
        SYS_CHROOT("SYS_CHROOT"),
        SETFCAP("SETFCAP"),
        ALL("ALL");

        private String friendlyName;

        CapabilityDrop(String friendlyName) {
            this.friendlyName = friendlyName;
        }

        public String getFriendlyName() {
            return friendlyName;
        }
    }

    public enum CapabilityAdd {
        //The list Linux capability options which are not granted by default and may be added.

        SYS_MODULE("SYS_MODULE"),
        SYS_RAWIO("SYS_RAWIO"),
        SYS_PACCT("SYS_PACCT"),
        SYS_ADMIN("SYS_ADMIN"),
        SYS_NICE("SYS_NICE"),
        SYS_RESOURCE("SYS_RESOURCE"),
        SYS_TIME("SYS_TIME"),
        SYS_TTY_CONFIG("SYS_TTY_CONFIG"),
        AUDIT_CONTROL("AUDIT_CONTROL"),
        MAC_ADMIN("MAC_ADMIN"),
        MAC_OVERRIDE("MAC_OVERRIDE"),
        NET_ADMIN("NET_ADMIN"),
        SYSLOG("SYSLOG"),
        DAC_READ_SEARCH("DAC_READ_SEARCH"),
        LINUX_IMMUTABLE("LINUX_IMMUTABLE"),
        NET_BROADCAST("NET_BROADCAST"),
        IPC_LOCK("IPC_LOCK"),
        IPC_OWNER("IPC_OWNER"),
        SYS_PTRACE("SYS_PTRACE"),
        SYS_BOOT("SYS_BOOT"),
        LEASE("LEASE"),
        WAKE_ALARM("WAKE_ALARM"),
        BLOCK_SUSPEND("BLOCK_SUSPEND"),
        ALL("ALL");

        private String friendlyName;

        CapabilityAdd(String friendlyName) {
            this.friendlyName = friendlyName;
        }

        public String getFriendlyName() {
            return friendlyName;
        }
    }

    public enum Architecture {

        X86("x86"),
        ARM64("arm64"),
        AMD64("amd64");

        private String friendlyName;

        Architecture(String friendlyName) {
            this.friendlyName = friendlyName;
        }

        public String getFriendlyName() {
            return friendlyName;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getIconContent() {
        return iconContent;
    }

    public void setIconContent(byte[] iconContent) {
        this.iconContent = iconContent;
    }

    public String getIconBase64() {
        return iconBase64;
    }

    public void setIconBase64(String iconBase64) {
        this.iconBase64 = iconBase64;
    }

    public String getDockerImage() {
        return dockerImage;
    }

    public void setDockerImage(String dockerImage) {
        this.dockerImage = dockerImage;
    }

    public String getDockerRegistry() {
        return dockerRegistry;
    }

    public void setDockerRegistry(String dockerRegistry) {
        this.dockerRegistry = dockerRegistry;
    }

    public Boolean getDockerCredentialsUsing() {
        return dockerCredentialsUsing;
    }

    public void setDockerCredentialsUsing(Boolean dockerCredentialsUsing) {
        this.dockerCredentialsUsing = dockerCredentialsUsing;
    }

    public Boolean getDockerCustomRegistry() {
        return dockerCustomRegistry;
    }

    public void setDockerCustomRegistry(Boolean dockerCustomRegistry) {
        this.dockerCustomRegistry = dockerCustomRegistry;
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

    public List<Interface> getExposedInterfaces() {
        return exposedInterfaces;
    }

    public void setExposedInterfaces(
            List<Interface> exposedInterfaces) {
        this.exposedInterfaces = exposedInterfaces;
    }

    public List<GraphLink> getRequiredInterfaces() {
        return requiredInterfaces;
    }

    public void setRequiredInterfaces(List<GraphLink> requiredInterfaces) {
        this.requiredInterfaces = requiredInterfaces;
    }

    public Requirement getRequirement() {
        return requirement;
    }

    public void setRequirement(Requirement requirement) {
        this.requirement = requirement;
    }

    public String getKubernetesRuntimeClassName() {
        return kubernetesRuntimeClassName;
    }

    public void setKubernetesRuntimeClassName(String kubernetesRuntimeClassName) {
        this.kubernetesRuntimeClassName = kubernetesRuntimeClassName;
    }

    public ServerlessProperties getServerlessProperties() {
        return serverlessProperties;
    }

    public void setServerlessProperties(ServerlessProperties serverlessProperties) {
        this.serverlessProperties = serverlessProperties;
    }

    public List<EnvironmentalVariable> getEnvironmentalVariables() {
        return environmentalVariables;
    }

    public void setEnvironmentalVariables(List<EnvironmentalVariable> environmentalVariables) {
        this.environmentalVariables = environmentalVariables;
    }

    public String getElasticityController() {
        return elasticityController;
    }

    public void setElasticityController(String elasticityController) {
        this.elasticityController = elasticityController;
    }

    public SortedSet<Plugin> getPlugins() {
        return plugins;
    }

    public void setPlugins(SortedSet<Plugin> plugins) {
        this.plugins = plugins;
    }

    public SortedSet<Label> getLabels() {
        return labels;
    }

    public void setLabels(SortedSet<Label> labels) {
        this.labels = labels;
    }

    public Boolean getPublicComponent() {
        return publicComponent;
    }

    public void setPublicComponent(Boolean publicComponent) {
        this.publicComponent = publicComponent;
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

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Collection<CapabilityDrop> getCapabilityDrops() {
        return capabilityDrops;
    }

    public void setCapabilityDrops(
            Collection<CapabilityDrop> capabilityDrops) {
        this.capabilityDrops = capabilityDrops;
    }

    public Collection<CapabilityAdd> getCapabilityAdds() {
        return capabilityAdds;
    }

    public void setCapabilityAdds(
            Collection<CapabilityAdd> capabilityAdds) {
        this.capabilityAdds = capabilityAdds;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getIconContentType() {
        return iconContentType;
    }

    public void setIconContentType(String iconContentType) {
        this.iconContentType = iconContentType;
    }

    public String getIconFilename() {
        return iconFilename;
    }

    public void setIconFilename(String iconFilename) {
        this.iconFilename = iconFilename;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    public List<Volume> getVolumes() {
        return volumes;
    }

    public void setVolumes(List<Volume> volumes) {
        this.volumes = volumes;
    }

    public HealthCheck getHealthCheck() {
        return healthCheck;
    }

    public void setHealthCheck(HealthCheck healthCheck) {
        this.healthCheck = healthCheck;
    }

    @Override
    public int compareTo(Component compareLabel) {
        Long compareQuantity = ((Component) compareLabel).getId();

        return this.id.compareTo(compareQuantity);
    }

    public String getArchitecture() {
        return architecture;
    }

    public void setArchitecture(String architecture) {
        this.architecture = architecture;
    }

    public String getElasticityControllerMode() {
        return elasticityControllerMode;
    }

    public void setElasticityControllerMode(String elasticityControllerMode) {
        this.elasticityControllerMode = elasticityControllerMode;
    }

    public List<Device> getDevices() {
        return devices;
    }

    public void setDevices(List<Device> devices) {
        this.devices = devices;
    }

    public String getHexID() {
        return hexID;
    }

    public void setHexID(String hexID) {
        this.hexID = hexID;
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

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public List<Long> getEnvironmentalVariableIDs() {

        List<Long> environmentalVariableIDs = new ArrayList<>();

        if (null != this.environmentalVariables && !this.environmentalVariables.isEmpty()) {

            this.environmentalVariables
                    .stream()
                    .forEach(
                            environmentalVariable -> {
                                environmentalVariableIDs.add(environmentalVariable.getEnvironmentalVariableID());
                            });
        }

        return environmentalVariableIDs;
    }

    public List<Long> getGraphLinkIDs() {

        List<Long> graphLinkIDs = new ArrayList<>();

        if (null != this.requiredInterfaces && !this.requiredInterfaces.isEmpty()) {

            this.requiredInterfaces
                    .stream()
                    .forEach(
                            graphLink -> {
                                graphLinkIDs.add(graphLink.getGraphLinkID());
                            });
        }

        return graphLinkIDs;
    }

    public List<Long> getVolumeIDs() {

        List<Long> volumeIDs = new ArrayList<>();

        if (null != this.volumes && !this.volumes.isEmpty()) {

            this.volumes
                    .stream()
                    .forEach(
                            volume -> {
                                volumeIDs.add(volume.getVolumeID());
                            });
        }

        return volumeIDs;
    }

    public List<Long> getDeviceIDs() {

        List<Long> deviceIDs = new ArrayList<>();

        if (null != this.devices && !this.devices.isEmpty()) {

            this.devices
                    .stream()
                    .forEach(
                            device -> {
                                deviceIDs.add(device.getDeviceID());
                            });
        }

        return deviceIDs;
    }

    public List<Long> getInterfaceIDs() {

        List<Long> interfaceIDs = new ArrayList<>();

        if (null != this.exposedInterfaces && !this.exposedInterfaces.isEmpty()) {

            this.exposedInterfaces
                    .stream()
                    .forEach(
                            exposedInterface -> {
                                interfaceIDs.add(exposedInterface.getInterfaceID());
                            });
        }

        return interfaceIDs;
    }

    public Boolean hasEditAllowance(User user) {
        if (user != null && (getUser().getId().equals(user.getId()) ||
                user.isAdmin() ||
                (user.isOrganizationAdmin() && user.getOrganization()
                        .equals(getUser().getOrganization())))) {
            return true;
        }
        return false;
    }

    public Boolean hasDeleteAllowance(User user) {
        if (user != null && (getUser().getId().equals(user.getId()) ||
                user.isAdmin() || (user.isOrganizationAdmin() && user.getOrganization()
                .equals(getUser().getOrganization())))) {
            return true;
        }
        return false;
    }
}
