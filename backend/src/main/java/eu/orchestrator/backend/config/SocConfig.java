package eu.orchestrator.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 17/12/19
 */

@EnableConfigurationProperties
@ConfigurationProperties(prefix = "soc.server")
@Component
public class SocConfig {

    private String host;
    private String ksqlPort;
    private String brokerPort;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getKsqlPort() {
        return ksqlPort;
    }

    public void setKsqlPort(String ksqlPort) {
        this.ksqlPort = ksqlPort;
    }

    public String getBrokerPort() {
        return brokerPort;
    }

    public void setBrokerPort(String brokerPort) {
        this.brokerPort = brokerPort;
    }
}
