package eu.orchestrator.elasticity.spi.model.backend;

import eu.orchestrator.repository.domain.Component;
import eu.orchestrator.repository.domain.Plugin;

import java.util.List;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 10/7/2019
 */
public class ElasticityControllerComponent {
    List<Component.CapabilityDrop> capabilitiesDrop;
    List<Component.CapabilityAdd> capabilitiesAdd;
    List<Plugin> pluginList;
    List<String> labelNameList;

    private String name;
    private String architecture;
    private String dockerImage;
    private String dockerRegistry;
    private String dockerUsername;
    private String dockerPassword;
    private List<ElasticityControllerExposedInterface> exposedInterfaces;
    private ElasticityControllerRequirement requirement;
    private ElasticityControllerHealthCheck healthCheck;
    private List<ElasticityControllerEnvironmentalVariable> environmentalVariables;
    private List<ElasticityControllerVolume> volumes;
    private Boolean networkModeHost = false;
    private Boolean privilege = false;
    private String hostname;
    private String sharedMemorySize;
    private String ulimitMemlockSoft;
    private String ulimitMemlockHard;
    private String dockerExecutionUser;


    public List<Component.CapabilityDrop> getCapabilitiesDrop() {
        return capabilitiesDrop;
    }

    public void setCapabilitiesDrop(List<Component.CapabilityDrop> capabilitiesDrop) {
        this.capabilitiesDrop = capabilitiesDrop;
    }

    public List<Component.CapabilityAdd> getCapabilitiesAdd() {
        return capabilitiesAdd;
    }

    public void setCapabilitiesAdd(List<Component.CapabilityAdd> capabilitiesAdd) {
        this.capabilitiesAdd = capabilitiesAdd;
    }

    public List<Plugin> getPluginList() {
        return pluginList;
    }

    public void setPluginList(List<Plugin> pluginList) {
        this.pluginList = pluginList;
    }

    public List<String> getLabelNameList() {
        return labelNameList;
    }

    public void setLabelNameList(List<String> labelNameList) {
        this.labelNameList = labelNameList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArchitecture() {
        return architecture;
    }

    public void setArchitecture(String architecture) {
        this.architecture = architecture;
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

    public List<ElasticityControllerExposedInterface> getExposedInterfaces() {
        return exposedInterfaces;
    }

    public void setExposedInterfaces(List<ElasticityControllerExposedInterface> exposedInterfaces) {
        this.exposedInterfaces = exposedInterfaces;
    }

    public ElasticityControllerRequirement getRequirement() {
        return requirement;
    }

    public void setRequirement(ElasticityControllerRequirement requirement) {
        this.requirement = requirement;
    }

    public ElasticityControllerHealthCheck getHealthCheck() {
        return healthCheck;
    }

    public void setHealthCheck(ElasticityControllerHealthCheck healthCheck) {
        this.healthCheck = healthCheck;
    }

    public List<ElasticityControllerEnvironmentalVariable> getEnvironmentalVariables() {
        return environmentalVariables;
    }

    public void setEnvironmentalVariables(List<ElasticityControllerEnvironmentalVariable> environmentalVariables) {
        this.environmentalVariables = environmentalVariables;
    }

    public List<ElasticityControllerVolume> getVolumes() {
        return volumes;
    }

    public void setVolumes(List<ElasticityControllerVolume> volumes) {
        this.volumes = volumes;
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

    //TODO create a proper validator for the healtcheck and everything else
    public Boolean validate(){

        return false;
    }
}
