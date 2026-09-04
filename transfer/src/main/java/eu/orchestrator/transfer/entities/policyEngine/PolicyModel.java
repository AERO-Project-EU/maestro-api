package eu.orchestrator.transfer.entities.policyEngine;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author Panagiotis Parthenis
 */
public class PolicyModel implements Serializable {

    private String callbackURL;
    private String policyHexID;
    private String graphHexID;
    private String graphInstanceHexID;
    private PolicyType policyType;
    private String policyName;
    private String prometheusPolicyExpression;
    private String prometheusPolicyPeriod;
    private String droolsInertialPeriod;
    private ArrayList<DroolsAction> droolsActions = new ArrayList<>();
    private boolean creation;

    public PolicyType getPolicyType() {
        return policyType;
    }

    public void setPolicyType(PolicyType policyType) {
        this.policyType = policyType;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public String getPrometheusPolicyExpression() {
        return prometheusPolicyExpression;
    }

    public void setPrometheusPolicyExpression(String prometheusPolicyExpression) {
        this.prometheusPolicyExpression = prometheusPolicyExpression;
    }

    public String getPrometheusPolicyPeriod() {
        return prometheusPolicyPeriod;
    }

    public void setPrometheusPolicyPeriod(String prometheusPolicyPeriod) {
        this.prometheusPolicyPeriod = prometheusPolicyPeriod;
    }

    public String getDroolsInertialPeriod() {
        return droolsInertialPeriod;
    }

    public void setDroolsInertialPeriod(String droolsInertialPeriod) {
        this.droolsInertialPeriod = droolsInertialPeriod;
    }

    public ArrayList<DroolsAction> getDroolsActions() {
        return droolsActions;
    }

    public void setDroolsActions(ArrayList<DroolsAction> droolsActions) {
        this.droolsActions = droolsActions;
    }

    public boolean isCreation() {
        return creation;
    }

    public void setCreation(boolean creation) {
        this.creation = creation;
    }

    public String getCallbackURL() {
        return callbackURL;
    }

    public void setCallbackURL(String callbackURL) {
        this.callbackURL = callbackURL;
    }

    public String getGraphHexID() {
        return graphHexID;
    }

    public void setGraphHexID(String graphHexID) {
        this.graphHexID = graphHexID;
    }

    public String getGraphInstanceHexID() {
        return graphInstanceHexID;
    }

    public void setGraphInstanceHexID(String graphInstanceHexID) {
        this.graphInstanceHexID = graphInstanceHexID;
    }

    public String getPolicyHexID() {
        return policyHexID;
    }

    public void setPolicyHexID(String policyHexID) {
        this.policyHexID = policyHexID;
    }
}
