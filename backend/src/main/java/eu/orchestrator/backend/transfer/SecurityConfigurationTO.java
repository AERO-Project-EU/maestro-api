package eu.orchestrator.backend.transfer;

import java.io.Serializable;
import java.util.Date;
import java.util.List;


public class SecurityConfigurationTO implements Serializable {

    private Long id;
    private String name;
    private List<String> componentNodeHexIDs;
    private String securityConfigurationType;
    private Date dateCreated;

    private Boolean allowEdit;
    private Boolean allowDelete;

    public SecurityConfigurationTO() {
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

    public List<String> getComponentNodeHexIDs() {
        return componentNodeHexIDs;
    }

    public void setComponentNodeHexIDs(List<String> componentNodeHexIDs) {
        this.componentNodeHexIDs = componentNodeHexIDs;
    }

    public String getSecurityConfigurationType() {
        return securityConfigurationType;
    }

    public void setSecurityConfigurationType(String securityConfigurationType) {
        this.securityConfigurationType = securityConfigurationType;
    }


    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
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
