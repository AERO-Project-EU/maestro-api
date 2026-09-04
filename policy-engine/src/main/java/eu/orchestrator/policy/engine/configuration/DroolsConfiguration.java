package eu.orchestrator.policy.engine.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Panagiotis Parthenis
 */

@EnableConfigurationProperties
@ConfigurationProperties(prefix = "system.drools")
@Component
public class DroolsConfiguration {

  private String filePath;

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }
}
