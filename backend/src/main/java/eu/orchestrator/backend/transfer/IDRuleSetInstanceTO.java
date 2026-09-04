package eu.orchestrator.backend.transfer;

import java.io.Serializable;

public class IDRuleSetInstanceTO implements Serializable {

    private Long ruleSetInstanceID;
    private IDRuleSetTO idRuleSet;
    private String name;
    private Long idRuleSetID;

    public IDRuleSetInstanceTO() {
    }

    public Long getRuleSetInstanceID() {
        return ruleSetInstanceID;
    }

    public void setRuleSetInstanceID(Long ruleSetInstanceID) {
        this.ruleSetInstanceID = ruleSetInstanceID;
    }

    public IDRuleSetTO getIdRuleSet() {
        return idRuleSet;
    }

    public void setIdRuleSet(IDRuleSetTO idRuleSet) {
        this.idRuleSet = idRuleSet;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getIdRuleSetID() {
        return idRuleSetID;
    }

    public void setIdRuleSetID(Long idRuleSetID) {
        this.idRuleSetID = idRuleSetID;
    }
}
