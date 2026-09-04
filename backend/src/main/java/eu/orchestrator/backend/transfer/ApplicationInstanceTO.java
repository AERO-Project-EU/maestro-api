package eu.orchestrator.backend.transfer;

import eu.orchestrator.repository.domain.Application;
import eu.orchestrator.repository.domain.ApplicationInstanceQuota;
import eu.orchestrator.repository.domain.Slice;

import java.io.Serializable;
import java.util.Date;

public class ApplicationInstanceTO implements Serializable {

    private Long applicationInstanceID;
    private String hexID;
    private String name;
    private Application application;
    private ApplicationInstanceQuota applicationInstanceQuota;
    private String status;
    private Date dateCreated;
    private Date lastModified;
    private Boolean allowEdit;
    private Boolean allowDelete;
    private String organization;
    private Slice slice;

    public ApplicationInstanceTO() {
    }

    public Long getApplicationInstanceID() {
        return applicationInstanceID;
    }

    public void setApplicationInstanceID(Long applicationInstanceID) {
        this.applicationInstanceID = applicationInstanceID;
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

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public ApplicationInstanceQuota getApplicationInstanceQuota() {
        return applicationInstanceQuota;
    }

    public void setApplicationInstanceQuota(ApplicationInstanceQuota applicationInstanceQuota) {
        this.applicationInstanceQuota = applicationInstanceQuota;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public Slice getSlice() {
        return slice;
    }

    public void setSlice(Slice slice) {
        this.slice = slice;
    }
}
