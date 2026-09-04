package eu.orchestrator.backend.transfer;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class RuntimePolicyTO implements Serializable {

    private Long id;
    private Long applicationInstanceID;
    private String applicationInstanceName;
    private String name;
    private String hexID;
    private String type;
    private String policy;
    private String policyExpression;
    private String policyPeriod;
    private String inertiaPeriod;
    private List<RuntimePolicyActionTO> actions;
    private List<RuntimePolicyExpressionTO> expressions;
    private String status;
    private Date dateCreated;
    private Date lastModified;
    private String username;
    private Boolean allowEdit;
    private Boolean allowDelete;

    public RuntimePolicyTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getApplicationInstanceID() {
        return applicationInstanceID;
    }

    public void setApplicationInstanceID(Long applicationInstanceID) {
        this.applicationInstanceID = applicationInstanceID;
    }

    public String getApplicationInstanceName() {
        return applicationInstanceName;
    }

    public void setApplicationInstanceName(String applicationInstanceName) {
        this.applicationInstanceName = applicationInstanceName;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    public String getPolicyExpression() {
        return policyExpression;
    }

    public void setPolicyExpression(String policyExpression) {
        this.policyExpression = policyExpression;
    }

    public String getPolicyPeriod() {
        return policyPeriod;
    }

    public void setPolicyPeriod(String policyPeriod) {
        this.policyPeriod = policyPeriod;
    }

    public String getInertiaPeriod() {
        return inertiaPeriod;
    }

    public void setInertiaPeriod(String inertiaPeriod) {
        this.inertiaPeriod = inertiaPeriod;
    }

    public List<RuntimePolicyActionTO> getActions() {
        return actions;
    }

    public void setActions(List<RuntimePolicyActionTO> actions) {
        this.actions = actions;
    }

    public List<RuntimePolicyExpressionTO> getExpressions() {
        return expressions;
    }

    public void setExpressions(List<RuntimePolicyExpressionTO> expressions) {
        this.expressions = expressions;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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
