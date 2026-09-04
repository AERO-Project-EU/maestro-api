package eu.orchestator.core.loops.metric.model;

import java.util.HashMap;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 17/9/2019
 */
public class AgentVmMetrics {

    private String storage; //GB
    private String ram; //GB
    private String cpu;
    private String operatingSystem;
    private String operatingSystemVersion;
    private String operatingSystemArchitecture;
    private String javaVersion;
    private String dockerVersion;
    private String netdataVersion;
    private String consulVersion;
    private String agentVersion;
    private HashMap<String, String> lscpu = new HashMap<>();

    @Override
    public String toString() {
        return "MachineConstants{" + "\n" + "storage='" + storage + '\'' + "\n" + ", RAM='" + ram + '\'' + "\n" + ", CPU='" + cpu + '\'' + "\n"
                + ", operatingSystem='" + operatingSystem + '\'' + "\n" + ", operatingSystemVersion='" + operatingSystemVersion + '\'' + "\n"
                + ", operatingSystemArchitecture='" + operatingSystemArchitecture + '\'' + "\n" + ", javaVersion='" + javaVersion + '\'' + "\n"
                + ", dockerVersion='" + dockerVersion + '\'' + "\n" + ", netdataVersion='" + netdataVersion + '\'' + "\n" + ", consulVersion='" + consulVersion
                + '\'' + "\n" + ", agentVersion='" + agentVersion + '\'' + "\n" + ", lscpu=" + lscpu + "\n" + '}';
    }

    public AgentVmMetrics() {
    }

    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }

    public String getRam() {
        return ram;
    }

    public void setRam(String ram) {
        this.ram = ram;
    }

    public String getCpu() {
        return cpu;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public String getOperatingSystemVersion() {
        return operatingSystemVersion;
    }

    public void setOperatingSystemVersion(String operatingSystemVersion) {
        this.operatingSystemVersion = operatingSystemVersion;
    }

    public String getOperatingSystemArchitecture() {
        return operatingSystemArchitecture;
    }

    public void setOperatingSystemArchitecture(String operatingSystemArchitecture) {
        this.operatingSystemArchitecture = operatingSystemArchitecture;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
    }

    public String getDockerVersion() {
        return dockerVersion;
    }

    public void setDockerVersion(String dockerVersion) {
        this.dockerVersion = dockerVersion;
    }

    public String getNetdataVersion() {
        return netdataVersion;
    }

    public void setNetdataVersion(String netdataVersion) {
        this.netdataVersion = netdataVersion;
    }

    public String getConsulVersion() {
        return consulVersion;
    }

    public void setConsulVersion(String consulVersion) {
        this.consulVersion = consulVersion;
    }

    public String getAgentVersion() {
        return agentVersion;
    }

    public void setAgentVersion(String agentVersion) {
        this.agentVersion = agentVersion;
    }

    public HashMap<String, String> getLscpu() {
        return lscpu;
    }

    public void setLscpu(HashMap<String, String> lscpu) {
        this.lscpu = lscpu;
    }
}
