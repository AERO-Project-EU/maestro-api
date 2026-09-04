package eu.orchestrator.elasticity.spi.model.orchestrator;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 25/7/2019
 */
public class ConsulConfigTO {

    private String url;

    private String ipv6;

    private String port;

    private Boolean ipv6Enabled;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIpv6() {
        return ipv6;
    }

    public void setIpv6(String ipv6) {
        this.ipv6 = ipv6;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public Boolean getIpv6Enabled() {
        return ipv6Enabled;
    }

    public void setIpv6Enabled(Boolean ipv6Enabled) {
        this.ipv6Enabled = ipv6Enabled;
    }

}
