package eu.orchestator.core.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Panagiotis Parthenis.
 */

@EnableConfigurationProperties
@ConfigurationProperties(prefix = "registry.credentials")
@Component
public class RegistryConfig {

    private String username;

    private String password;

    private String email;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "RegistryConfig{" + "username='" + username + '\'' + ", password='" + password + '\'' + ", email='" + email + '\'' + '}';
    }
}
