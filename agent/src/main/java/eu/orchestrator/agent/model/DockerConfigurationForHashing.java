package eu.orchestrator.agent.model;

import java.util.List;
import java.util.Map;

/**
 * @author Panagiotis Parthenis.
 */
public class DockerConfigurationForHashing {

    // Docker image hash
    private String image;

    // Docker Environment Variable
    private List<String> environmentVariableKey;

    // Docker Credentials
    private String registry;
    private String username;
    private String password;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public List<String> getEnvironmentVariableKey() {
        return environmentVariableKey;
    }

    public void setEnvironmentVariableKey(List<String> environmentVariableKey) {
        this.environmentVariableKey = environmentVariableKey;
    }

    public String getRegistry() {
        return registry;
    }

    public void setRegistry(String registry) {
        this.registry = registry;
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
}
