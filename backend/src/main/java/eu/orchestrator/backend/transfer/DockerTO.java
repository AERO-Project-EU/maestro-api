package eu.orchestrator.backend.transfer;

import java.io.Serializable;


public class DockerTO implements Serializable {

    private String dockerImage;

    private String dockerRegistry;

    private String dockerUsername;

    private String dockerPassword;

    public DockerTO() {}

    public DockerTO(String dockerImage, String dockerRegistry, String dockerUsername, String dockerPassword) {
        this.dockerImage = dockerImage;
        this.dockerRegistry = dockerRegistry;
        this.dockerUsername = dockerUsername;
        this.dockerPassword = dockerPassword;
    }

    public String getDockerImage() {
        return dockerImage;
    }

    public void setDockerImage(String dockerImage) {
        this.dockerImage = dockerImage;
    }

    public String getDockerRegistry() {
        return dockerRegistry;
    }

    public void setDockerRegistry(String dockerRegistry) {
        this.dockerRegistry = dockerRegistry;
    }

    public String getDockerUsername() {
        return dockerUsername;
    }

    public void setDockerUsername(String dockerUsername) {
        this.dockerUsername = dockerUsername;
    }

    public String getDockerPassword() {
        return dockerPassword;
    }

    public void setDockerPassword(String dockerPassword) {
        this.dockerPassword = dockerPassword;
    }

    @Override
    public String toString() {
        return "DockerTO{" +
                "dockerImage='" + dockerImage + '\'' +
                ", dockerRegistry='" + dockerRegistry + '\'' +
                ", dockerUsername='" + dockerUsername + '\'' +
                ", dockerPassword='" + dockerPassword + '\'' +
                '}';
    }
}
