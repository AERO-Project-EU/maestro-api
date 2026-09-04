package eu.orchestator.core.model.agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Konstantinos Theodosiou.
 * @email konstheodosiou@gmail.com
 * @date 22/3/2019
 */
public class Arguments {

    private List<String> dns = new ArrayList<>();

    private List<String> envs = new ArrayList<>();

    private List<String> ports = new ArrayList<>();

    private Map<String, String> volumes = new HashMap<>();

    private List<String> commands = new ArrayList<>();

    private List<String> device = new ArrayList<>();

    private List<String> entrypoints = new ArrayList<>();

    private Boolean privileged = false;

    private List<String> capabilityDrops = new ArrayList<>();

    private List<String> capabilityAdds = new ArrayList<>();

    private Boolean networkModeHost = false;

    private String hostname;

    private String sharedMemorySize;

    // only the value [ user | user:group | uid | uid:gid | user:gid | uid:group ]
    private String user;

    // only the value [ number | number:number ]
    private String ulimitMemlock;

    public List<String> getDns() {
        return dns;
    }

    public void setDns(List<String> dns) {
        this.dns = dns;
    }

    public List<String> getEnvs() {
        return envs;
    }

    public void setEnvs(List<String> envs) {
        this.envs = envs;
    }

    public List<String> getPorts() {
        return ports;
    }

    public void setPorts(List<String> ports) {
        this.ports = ports;
    }

    public Map<String, String> getVolumes() {
        return volumes;
    }

    public void setVolumes(Map<String, String> volumes) {
        this.volumes = volumes;
    }

    public List<String> getCommands() {
        return commands;
    }

    public void setCommands(List<String> commands) {
        this.commands = commands;
    }

    public List<String> getDevice() {
        return device;
    }

    public void setDevice(List<String> device) {
        this.device = device;
    }

    public List<String> getEntrypoints() {
        return entrypoints;
    }

    public void setEntrypoints(List<String> entrypoints) {
        this.entrypoints = entrypoints;
    }

    public Boolean getPrivileged() {
        return privileged;
    }

    public void setPrivileged(Boolean privileged) {
        this.privileged = privileged;
    }

    public List<String> getCapabilityDrops() {
        return capabilityDrops;
    }

    public void setCapabilityDrops(List<String> capabilityDrops) {
        this.capabilityDrops = capabilityDrops;
    }

    public List<String> getCapabilityAdds() {
        return capabilityAdds;
    }

    public void setCapabilityAdds(List<String> capabilityAdds) {
        this.capabilityAdds = capabilityAdds;
    }

    public Boolean getNetworkModeHost() {
        return networkModeHost;
    }

    public void setNetworkModeHost(Boolean networkModeHost) {
        this.networkModeHost = networkModeHost;
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

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUlimitMemlock() {
        return ulimitMemlock;
    }

    public void setUlimitMemlock(String ulimitMemlock) {
        this.ulimitMemlock = ulimitMemlock;
    }

    @Override
    public String toString() {
        return "Arguments{"
                + "dns=" + dns
                + ", envs=" + envs
                + ", ports=" + ports
                + ", volumes=" + volumes
                + ", commands=" + commands
                + ", device=" + device
                + ", entrypoints=" + entrypoints
                + ", privileged=" + privileged
                + ", capabilityDrops=" + capabilityDrops
                + ", capabilityAdds=" + capabilityAdds
                + ", networkModeHost=" + networkModeHost
                + ", hostname='" + hostname + '\''
                + ", sharedMemorySize='" + sharedMemorySize + '\''
                + '}';
    }
}
