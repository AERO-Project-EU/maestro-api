package eu.orchestrator.metric.exporter.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@EnableConfigurationProperties
@ConfigurationProperties(prefix = "system.ports")
@Component
public class MonitoringPortsConfiguration {

    private String netdata = "19999";

    private String traefik = "15568";

    public String getNetdata() {
        return netdata;
    }

    public void setNetdata(String netdata) {
        this.netdata = netdata;
    }

    public String getTraefik() {
        return traefik;
    }

    public void setTraefik(String traefik) {
        this.traefik = traefik;
    }
}
