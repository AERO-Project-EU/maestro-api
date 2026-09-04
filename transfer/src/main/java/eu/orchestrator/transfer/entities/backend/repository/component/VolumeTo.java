package eu.orchestrator.transfer.entities.backend.repository.component;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 24/1/22
 */
public class VolumeTo implements Serializable {

    private Long volumeID;
    private String dockerPath;
    private Boolean isFile = false;
    private Date dateCreated;
    private Date lastModified;

    public Long getVolumeID() {
        return volumeID;
    }

    public void setVolumeID(Long volumeID) {
        this.volumeID = volumeID;
    }

    public String getDockerPath() {
        return dockerPath;
    }

    public void setDockerPath(String dockerPath) {
        this.dockerPath = dockerPath;
    }

    public Boolean getFile() {
        return isFile;
    }

    public void setFile(Boolean file) {
        isFile = file;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }
}
