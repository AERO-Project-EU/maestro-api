package eu.orchestrator.policy.engine.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Panagiotis Parthenis
 */

@EnableConfigurationProperties
@ConfigurationProperties(prefix = "system.prometheus")
@Component
public class PrometheusConfiguration {

  private String url;
  private String port;
  private String filePath;

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getPort() {
    return port;
  }

  public void setPort(String port) {
    this.port = port;
  }

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }
}
