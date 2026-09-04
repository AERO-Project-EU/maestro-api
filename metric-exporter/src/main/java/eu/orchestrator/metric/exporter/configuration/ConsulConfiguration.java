package eu.orchestrator.metric.exporter.configuration;

import com.ecwid.consul.v1.ConsulClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConsulConfiguration {

    @Value("${system.consul.url}")
    private String url;

    @Value("${system.consul.port}")
    private String port;

    @Bean(name = "consulClient")
    public ConsulClient client() {
        return new ConsulClient(url + ":" + port);
    }
}
