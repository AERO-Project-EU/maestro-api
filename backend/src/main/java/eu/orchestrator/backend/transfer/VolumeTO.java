package eu.orchestrator.backend.transfer;

import java.io.Serializable;

public class VolumeTO implements Serializable {

    private Long volumeID;

    private String dockerPath;

    //deprecated
    private String name;

    public VolumeTO() {
    }

    public Long getVolumeID() {
        return volumeID;
    }

    public void setVolumeID(Long volumeID) {
        this.volumeID = volumeID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDockerPath() {
        return dockerPath;
    }

    public void setDockerPath(String dockerPath) {
        this.dockerPath = dockerPath;
    }
}
