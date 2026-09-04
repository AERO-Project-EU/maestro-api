package eu.orchestrator.transfer.entities.rainbowk8s;

import java.io.Serializable;

public class ContainerPortSection implements Serializable {

    private String name;
    private int containerPort;
    private String protocol;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getContainerPort() {
        return containerPort;
    }

    public void setContainerPort(int containerPort) {
        this.containerPort = containerPort;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }


    public static class Builder {

        private String name;
        private int containerPort;
        private String protocol;

        public ContainerPortSection.Builder withName(String name) {
            this.name = name;
            return this;
        }

        public ContainerPortSection.Builder withContainerPort(int containerPort) {
            this.containerPort = containerPort;
            return this;
        }

        public ContainerPortSection.Builder withProtocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public ContainerPortSection build() {
            ContainerPortSection containerPortSection = new ContainerPortSection();

            containerPortSection.setName(this.name);
            containerPortSection.setContainerPort(this.containerPort);
            containerPortSection.setProtocol(this.protocol);

            return containerPortSection;
        }
    }
}
