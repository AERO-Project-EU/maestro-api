package eu.orchestrator.transfer.entities.policyEngine;

import java.io.Serializable;

public class PolicyEngineTrigger implements Serializable {
    private String policyUUID;
    private String policyName;
    private String applicationInstanceHexId;
    private PolicyEngineAction action;

    public String getPolicyUUID() {
        return policyUUID;
    }

    public void setPolicyUUID(String policyUUID) {
        this.policyUUID = policyUUID;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public String getApplicationInstanceHexId() {
        return applicationInstanceHexId;
    }

    public void setApplicationInstanceHexId(String applicationInstanceHexId) {
        this.applicationInstanceHexId = applicationInstanceHexId;
    }

    public PolicyEngineAction getAction() {
        return action;
    }

    public void setAction(PolicyEngineAction action) {
        this.action = action;
    }

    @Override
    public String toString() {
        return "PolicyEngineTrigger{" +
                "policyUUID='" + policyUUID + '\'' +
                ", policyName='" + policyName + '\'' +
                ", applicationInstanceHexId='" + applicationInstanceHexId + '\'' +
                ", action=" + action +
                '}';
    }
}
