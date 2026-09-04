package eu.orchestrator.transfer.entities.backend.repository.component;

import eu.orchestrator.transfer.entities.backend.repository.LabelTo;
import eu.orchestrator.transfer.entities.backend.repository.OrganizationTo;
import eu.orchestrator.transfer.entities.backend.repository.UserTo;

import java.io.Serializable;
import java.util.*;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 24/1/22
 */
public class ComponentTo implements Serializable {

    private Long id;
    private String name;
    private String hexID;
    private String architecture;
    private String elasticityControllerMode;
    private String dockerImage;
    private String dockerRegistry;
    private Boolean dockerCredentialsUsing = false;
    private Boolean dockerCustomRegistry = false;
    private String dockerUsername;
    private String dockerPassword;
    private List<InterfaceTo> exposedInterfaces;
    private List<GraphLinkTo> requiredInterfaces;
    private RequirementTo requirement;
    private ServerlessPropertiesTo serverlessProperties;
    private HealthCheckTo healthCheck;
    private List<EnvironmentalVariableTo> environmentalVariables;
    private List<DeviceTo> devices;
    private String elasticityController;
    private List<VolumeTo> volumes;
    private SortedSet<LabelTo> labels = new TreeSet<LabelTo>();
    private SortedSet<PluginTo> plugins = new TreeSet<PluginTo>();
    private Boolean publicComponent;
    private Boolean networkModeHost = false;
    private Boolean privilege = false;
    private String hostname;
    private String sharedMemorySize;
    private String command;
    private List<CapabilityDrop> capabilityDrops;
    private List<CapabilityAdd> capabilityAdds;
    private String ulimitMemlockSoft;
    private String ulimitMemlockHard;
    private String dockerExecutionUser;
    private UserTo user;
    private OrganizationTo organization;
    private Date dateCreated;
    private Date lastModified;


    public ComponentTo() {
    }




    //TODO move to another place
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

    //TODO move to another place
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

    public String getHexID() {
        return hexID;
    }

    public void setHexID(String hexID) {
        this.hexID = hexID;
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

    public List<InterfaceTo> getExposedInterfaces() {
        return exposedInterfaces;
    }

    public void setExposedInterfaces(List<InterfaceTo> exposedInterfaces) {
        this.exposedInterfaces = exposedInterfaces;
    }

    public List<GraphLinkTo> getRequiredInterfaces() {
        return requiredInterfaces;
    }

    public void setRequiredInterfaces(List<GraphLinkTo> requiredInterfaces) {
        this.requiredInterfaces = requiredInterfaces;
    }

    public RequirementTo getRequirement() {
        return requirement;
    }

    public void setRequirement(RequirementTo requirement) {
        this.requirement = requirement;
    }

    public ServerlessPropertiesTo getServerlessProperties() {
        return serverlessProperties;
    }

    public void setServerlessProperties(ServerlessPropertiesTo serverlessProperties) {
        this.serverlessProperties = serverlessProperties;
    }

    public HealthCheckTo getHealthCheck() {
        return healthCheck;
    }

    public void setHealthCheck(HealthCheckTo healthCheck) {
        this.healthCheck = healthCheck;
    }

    public List<EnvironmentalVariableTo> getEnvironmentalVariables() {
        return environmentalVariables;
    }

    public void setEnvironmentalVariables(List<EnvironmentalVariableTo> environmentalVariables) {
        this.environmentalVariables = environmentalVariables;
    }

    public List<DeviceTo> getDevices() {
        return devices;
    }

    public void setDevices(List<DeviceTo> devices) {
        this.devices = devices;
    }

    public String getElasticityController() {
        return elasticityController;
    }

    public void setElasticityController(String elasticityController) {
        this.elasticityController = elasticityController;
    }

    public List<VolumeTo> getVolumes() {
        return volumes;
    }

    public void setVolumes(List<VolumeTo> volumes) {
        this.volumes = volumes;
    }

    public SortedSet<LabelTo> getLabels() {
        return labels;
    }

    public void setLabels(SortedSet<LabelTo> labels) {
        this.labels = labels;
    }

    public SortedSet<PluginTo> getPlugins() {
        return plugins;
    }

    public void setPlugins(SortedSet<PluginTo> plugins) {
        this.plugins = plugins;
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

    public List<CapabilityDrop> getCapabilityDrops() {
        return capabilityDrops;
    }

    public void setCapabilityDrops(List<CapabilityDrop> capabilityDrops) {
        this.capabilityDrops = capabilityDrops;
    }

    public List<CapabilityAdd> getCapabilityAdds() {
        return capabilityAdds;
    }

    public void setCapabilityAdds(List<CapabilityAdd> capabilityAdds) {
        this.capabilityAdds = capabilityAdds;
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

    public UserTo getUser() {
        return user;
    }

    public void setUser(UserTo user) {
        this.user = user;
    }

    public OrganizationTo getOrganization() {
        return organization;
    }

    public void setOrganization(OrganizationTo organization) {
        this.organization = organization;
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
