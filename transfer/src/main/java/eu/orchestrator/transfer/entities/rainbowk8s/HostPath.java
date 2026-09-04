package eu.orchestrator.transfer.entities.rainbowk8s;

import java.io.Serializable;

public class HostPath implements Serializable {

    private String path;


    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
