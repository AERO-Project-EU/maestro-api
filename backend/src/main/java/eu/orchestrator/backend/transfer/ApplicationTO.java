package eu.orchestrator.backend.transfer;

import java.io.Serializable;
import java.util.Date;

public class ApplicationTO implements Serializable {

    private Long id;
    private String hexID;
    private String name;
    private Boolean publicApplication;
    private Date dateCreated;
    private Date lastModified;
    private Boolean allowEdit;
    private Boolean allowDelete;
    private String organization;

    public ApplicationTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHexID() {
        return hexID;
    }

    public void setHexID(String hexID) {
        this.hexID = hexID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getPublicApplication() {
        return publicApplication;
    }

    public void setPublicApplication(Boolean publicApplication) {
        this.publicApplication = publicApplication;
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

    public Boolean getAllowEdit() {
        return allowEdit;
    }

    public void setAllowEdit(Boolean allowEdit) {
        this.allowEdit = allowEdit;
    }

    public Boolean getAllowDelete() {
        return allowDelete;
    }

    public void setAllowDelete(Boolean allowDelete) {
        this.allowDelete = allowDelete;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }
}
