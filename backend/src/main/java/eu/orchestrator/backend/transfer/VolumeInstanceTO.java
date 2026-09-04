package eu.orchestrator.backend.transfer;

import java.io.Serializable;

public class VolumeInstanceTO implements Serializable {

    private Long volumeInstanceID;
    private VolumeTO volume;

    private String dockerPath;
    private String hostPath;

    // deprecated
    private String nameOld;
    private String name;

    public VolumeInstanceTO() {
    }

    public Long getVolumeInstanceID() {
        return volumeInstanceID;
    }

    public void setVolumeInstanceID(Long volumeInstanceID) {
        this.volumeInstanceID = volumeInstanceID;
    }

    public VolumeTO getVolume() {
        return volume;
    }

    public void setVolume(VolumeTO volume) {
        this.volume = volume;
    }

    public String getNameOld() {
        return nameOld;
    }

    public void setNameOld(String nameOld) {
        this.nameOld = nameOld;
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

    public String getHostPath() {
        return hostPath;
    }

    public void setHostPath(String hostPath) {
        this.hostPath = hostPath;
    }
}
