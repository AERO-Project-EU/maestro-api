package eu.orchestrator.backend.transfer;

import java.io.Serializable;
import java.util.Date;

public class IDRuleSetTO implements Serializable {

    private Long id;
    private String name;
    private Date dateCreated;
    private Date lastModified;
    private Integer rulesCounter;
    private Boolean allowEdit;
    private Boolean allowDelete;
    private Boolean publicIDRuleSet;
    private String organization;


    public IDRuleSetTO() {
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

    public Integer getRulesCounter() {
        return rulesCounter;
    }

    public void setRulesCounter(Integer rulesCounter) {
        this.rulesCounter = rulesCounter;
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

    public Boolean getPublicIDRuleSet() {
        return publicIDRuleSet;
    }

    public void setPublicIDRuleSet(Boolean publicIDRuleSet) {
        this.publicIDRuleSet = publicIDRuleSet;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }
}
