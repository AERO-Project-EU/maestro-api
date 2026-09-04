package eu.orchestrator.backend.transfer;

import java.io.Serializable;
import java.util.Date;

public class ProviderTypeTO implements Serializable {

    private Long id;
    private String name;
    private String friendlyName;
    private Boolean enabled;
    private Date dateCreated;
    private Date lastModified;

    public ProviderTypeTO() {
    }

    public ProviderTypeTO(Long id, String name, String friendlyName) {
        this.id = id;
        this.name = name;
        this.friendlyName = friendlyName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
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
