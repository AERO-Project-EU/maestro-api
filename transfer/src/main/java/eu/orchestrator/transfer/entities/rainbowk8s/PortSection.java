package eu.orchestrator.transfer.entities.rainbowk8s;

import java.io.Serializable;

public class PortSection implements Serializable {

    private String name;
    private int port;
    private String protocol;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }


    public static class Builder {

        private String name;
        private int port;
        private String protocol;

        public PortSection.Builder withName(String name) {
            this.name = name;
            return this;
        }

        public PortSection.Builder withPort(int port) {
            this.port = port;
            return this;
        }

        public PortSection.Builder withProtocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public PortSection build() {
            PortSection portSection = new PortSection();

            portSection.setName(this.name);
            portSection.setPort(this.port);
            portSection.setProtocol(this.protocol);

            return portSection;
        }
    }
}
