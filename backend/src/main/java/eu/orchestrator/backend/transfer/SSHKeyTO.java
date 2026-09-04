package eu.orchestrator.backend.transfer;

import eu.orchestrator.repository.domain.User;

import java.io.Serializable;
import java.util.Date;

public class SSHKeyTO implements Serializable {

    private Long id;
    private String friendlyName;
    private Boolean defaultSSH;
    private Date dateCreated;
    private Date lastModified;
    private Boolean allowEdit;
    private Boolean allowDelete;
    private String user;

    public SSHKeyTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public Boolean getDefaultSSH() {
        return defaultSSH;
    }

    public void setDefaultSSH(Boolean defaultSSH) {
        this.defaultSSH = defaultSSH;
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

    public String getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user.getUsername();
    }
}
