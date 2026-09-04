package eu.orchestrator.agent.model;

import eu.orchestrator.agent.util.CommandLineExecutor;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.HashMap;

/**
 * @author Panagiotis Parthenis.
 */
public class MachineConstants {

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



    public MachineConstants() {
        double gigabytes = (double) 1024 * 1024 * 1024;
        double memorySize = ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize();
        this.storage = new File("/").getTotalSpace() / gigabytes + "";
        this.ram = memorySize / gigabytes + "";
        this.cpu = Runtime.getRuntime().availableProcessors() + "";
        this.operatingSystem = System.getProperty("os.name");
        this.operatingSystemVersion = System.getProperty("os.version");
        this.operatingSystemArchitecture = System.getProperty("os.arch");
        this.javaVersion = System.getProperty("java.runtime.version");
        this.agentVersion = "v2.6";

        String[] cmd = {
                "/bin/sh",
                "-c",
                ""
        };

        cmd[2] = "lscpu";
        String[] lines = CommandLineExecutor.multiLine(cmd).split("\n");
        for (String line : lines) {
            String[] temp = line.split(":");
            this.lscpu.put(temp[0], temp[1].trim());
        }

        cmd[2] = "/opt/netdata/bin/netdata -version";
        String commandOutput = CommandLineExecutor.multiLine(cmd);
        if (commandOutput != null) {
            this.netdataVersion = commandOutput.replace("\n", "");
        }

        cmd[2] = "docker -v";
        commandOutput = CommandLineExecutor.multiLine(cmd);
        if (commandOutput != null) {
            this.dockerVersion = commandOutput.replace("\n", "");
        }

        cmd[2] = "/opt/consul version";
        commandOutput = CommandLineExecutor.multiLine(cmd);
        if (commandOutput != null) {
            this.consulVersion = commandOutput.replace("\n", "");
        }
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
