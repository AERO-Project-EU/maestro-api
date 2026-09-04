package eu.orchestator.core.model.orchestrator;

/**
 * @author Konstantinos Theodosiou.
 * @email konstheodosiou@gmail.com
 * @date 16/12/19
 */
public class HostnameMapping {

    private String hostname;
    private String ip;

    public HostnameMapping(String hostname) {
        this.hostname = hostname;
        this.ip = null;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
