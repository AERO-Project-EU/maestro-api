package eu.orchestrator.backend.transfer;

import java.io.Serializable;

public class EnvironmentalVariableInstanceTO implements Serializable {

    private Long environmentalVariableInstanceID;
    private EnvironmentalVariableTO environmentalVariable;
    private String key;
    private String value;

    public EnvironmentalVariableInstanceTO() {
    }

    public Long getEnvironmentalVariableInstanceID() {
        return environmentalVariableInstanceID;
    }

    public void setEnvironmentalVariableInstanceID(Long environmentalVariableInstanceID) {
        this.environmentalVariableInstanceID = environmentalVariableInstanceID;
    }

    public EnvironmentalVariableTO getEnvironmentalVariable() {
        return environmentalVariable;
    }

    public void setEnvironmentalVariable(
            EnvironmentalVariableTO environmentalVariable) {
        this.environmentalVariable = environmentalVariable;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
