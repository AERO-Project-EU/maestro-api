package eu.orchestrator.backend.transfer;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class ComponentTO implements Serializable {

    private Long id;
    private String name;
    private String hexID;
    private Boolean publicComponent;
    private Date dateCreated;
    private Date lastModified;
    private String elasticityController;
    private List<InterfaceTO> exposedInterfaces;
    private Boolean allowEdit;
    private Boolean allowDelete;
    private String organization;

    public ComponentTO() {
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

    public String getHexID() {
        return hexID;
    }

    public void setHexID(String hexID) {
        this.hexID = hexID;
    }

    public Boolean getPublicComponent() {
        return publicComponent;
    }

    public void setPublicComponent(Boolean publicComponent) {
        this.publicComponent = publicComponent;
    }

    public String getElasticityController() {
        return elasticityController;
    }

    public void setElasticityController(String elasticityController) {
        this.elasticityController = elasticityController;
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

    public List<InterfaceTO> getExposedInterfaces() {
        return exposedInterfaces;
    }

    public void setExposedInterfaces(
            List<InterfaceTO> exposedInterfaces) {
        this.exposedInterfaces = exposedInterfaces;
    }
}
