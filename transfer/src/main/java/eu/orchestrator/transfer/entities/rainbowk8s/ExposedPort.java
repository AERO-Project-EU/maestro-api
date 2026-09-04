package eu.orchestrator.transfer.entities.rainbowk8s;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ExposedPort implements Serializable {

    private String type;
    private List<PortSection> ports;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<PortSection> getPorts() {
        if (ports == null) {
            ports = new ArrayList<>();
        }
        return ports;
    }

    public void setPorts(List<PortSection> ports) {
        this.ports = ports;
    }


    public static class Builder {

        private String type;
        private List<PortSection> ports;


        public ExposedPort.Builder withType(String type) {
            this.type = type;
            return this;
        }

        public ExposedPort.Builder withPorts(List<PortSection> ports) {
            this.ports = ports;
            return this;
        }


        public ExposedPort build() {
            ExposedPort exposedPort = new ExposedPort();

            exposedPort.setType(this.type);
            exposedPort.setPorts(this.ports);

            return exposedPort;
        }
    }
}
