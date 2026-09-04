package eu.orchestator.core.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Konstantinos Theodosiou.
 * @email konstheodosiou@gmail.com
 * @date 22/3/2019
 */
@EnableConfigurationProperties
@Component
public class IntrusionDetectionConfig {

    @Value("${ids.registry}")
    private String registry;

    @Value("${ids.image}")
    private String image;

    @Value("${ids.username}")
    private String username;

    @Value("${ids.password}")
    private String password;

    @Value("${ids.config.url}")
    private String configUrl;

    @Value("${ids.config.username}")
    private String configUsername;

    @Value("${ids.config.password}")
    private String configPassword;

    public String getRegistry() {
        return registry;
    }

    public void setRegistry(String registry) {
        this.registry = registry;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfigUrl() {
        return configUrl;
    }

    public void setConfigUrl(String configUrl) {
        this.configUrl = configUrl;
    }

    public String getConfigUsername() {
        return configUsername;
    }

    public void setConfigUsername(String configUsername) {
        this.configUsername = configUsername;
    }

    public String getConfigPassword() {
        return configPassword;
    }

    public void setConfigPassword(String configPassword) {
        this.configPassword = configPassword;
    }
}
