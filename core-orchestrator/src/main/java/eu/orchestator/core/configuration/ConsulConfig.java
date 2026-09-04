package eu.orchestator.core.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Panagiotis Parthenis.
 */
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "consul")
@Component
public class ConsulConfig {

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
