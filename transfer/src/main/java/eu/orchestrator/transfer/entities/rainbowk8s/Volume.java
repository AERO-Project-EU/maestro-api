package eu.orchestrator.transfer.entities.rainbowk8s;

import java.io.Serializable;

public class Volume implements Serializable {

    private String name;
    private String emptyDir;
    private HostPath hostPath;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmptyDir() {
        return emptyDir;
    }

    public void setEmptyDir(String emptyDir) {
        this.emptyDir = emptyDir;
    }

    public HostPath getHostPath() {
        return hostPath;
    }

    public void setHostPath(HostPath hostPath) {
        this.hostPath = hostPath;
    }
}
