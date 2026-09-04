package eu.orchestrator.backend.transfer;

import java.io.Serializable;

public class ComponentIPTO implements Serializable {

    private String name;
    private String ip;

    public ComponentIPTO() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
