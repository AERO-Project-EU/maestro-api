package eu.orchestrator.policy.engine.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Panagiotis Parthenis
 */

@EnableConfigurationProperties
@ConfigurationProperties(prefix = "system.kafka")
@Component
public class KafkaConfiguration {

  private String url;
  private String port;
  private String orchestratorTopicName;
  private String backendTopicName;
  private String infoTopicName;

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

  public String getOrchestratorTopicName() {
    return orchestratorTopicName;
  }

  public void setOrchestratorTopicName(String orchestratorTopicName) {
    this.orchestratorTopicName = orchestratorTopicName;
  }

  public String getBackendTopicName() {
    return backendTopicName;
  }

  public void setBackendTopicName(String backendTopicName) {
    this.backendTopicName = backendTopicName;
  }

  public String getInfoTopicName() {
    return infoTopicName;
  }

  public void setInfoTopicName(String infoTopicName) {
    this.infoTopicName = infoTopicName;
  }
}
