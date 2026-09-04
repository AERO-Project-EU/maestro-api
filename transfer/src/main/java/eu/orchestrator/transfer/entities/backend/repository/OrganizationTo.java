package eu.orchestrator.transfer.entities.backend.repository;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 24/1/22
 */
public class OrganizationTo implements Serializable {

    private Long id;
    private String name;
    private String status;
    private List<UserTo> users;
    private Date dateCreated;
    private Date lastModified;

    public OrganizationTo() {
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

    public List<UserTo> getUsers() {
        return users;
    }

    public void setUsers(List<UserTo> users) {
        this.users = users;
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
