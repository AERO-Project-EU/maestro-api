package eu.orchestrator.backend.transfer;

import java.io.Serializable;
import java.util.Date;

public class OrganizationTO implements Serializable {

    private Long id;
    private String name;
    private String status;
    private Integer usersCounter;
    private Date dateCreated;
    private Date lastModified;
    private Boolean allowEdit;
    private Boolean allowDelete;

    public OrganizationTO() {
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getUsersCounter() {
        return usersCounter;
    }

    public void setUsersCounter(Integer usersCounter) {
        this.usersCounter = usersCounter;
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
}

