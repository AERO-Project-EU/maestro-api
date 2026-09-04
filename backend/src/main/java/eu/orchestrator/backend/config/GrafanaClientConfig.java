package eu.orchestrator.backend.config;

import eu.orchestrator.backend.grafana.GrafanaClient;
import eu.orchestrator.backend.grafana.RestTemplateGrafanaClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrafanaClientConfig {

    @Value("${grafana.server.token}")
    private String token;

    @Value("${grafana.server.url}")
    private String url;

    @Bean
    public GrafanaClient grafanaClient() {
        return new RestTemplateGrafanaClient(url, token);
    }
}
