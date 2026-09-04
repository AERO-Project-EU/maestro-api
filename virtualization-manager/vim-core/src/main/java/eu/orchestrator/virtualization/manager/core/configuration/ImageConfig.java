package eu.orchestrator.virtualization.manager.core.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Konstantinos Theodosiou
 */

@EnableConfigurationProperties
@ConfigurationProperties(prefix="openstack.image")
@Component
public class ImageConfig {

  private String id;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

}
