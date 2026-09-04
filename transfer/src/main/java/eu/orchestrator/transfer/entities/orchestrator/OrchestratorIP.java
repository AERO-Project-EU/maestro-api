package eu.orchestrator.transfer.entities.orchestrator;

import java.io.Serializable;

public class OrchestratorIP implements Serializable {

    private String interfaceType;
    private String network;
    private String ip;

    public OrchestratorIP() {
    }

    public String getInterfaceType() {
        return interfaceType;
    }

    public void setInterfaceType(String interfaceType) {
        this.interfaceType = interfaceType;
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
