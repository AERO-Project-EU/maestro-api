package eu.orchestrator.backend.service.model;

import java.io.Serializable;

public class NetworkPolicyRequest implements Serializable {

    private String componentNodeInstanceHexId;
    private String ip;


    public String getComponentNodeInstanceHexId() {
        return componentNodeInstanceHexId;
    }

    public void setComponentNodeInstanceHexId(String componentNodeInstanceHexId) {
        this.componentNodeInstanceHexId = componentNodeInstanceHexId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
