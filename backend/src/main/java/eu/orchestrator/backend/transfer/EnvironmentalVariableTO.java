package eu.orchestrator.backend.transfer;

import java.io.Serializable;

public class EnvironmentalVariableTO implements Serializable {

    private Long environmentalVariableID;
    private String key;
    private String value;

    public EnvironmentalVariableTO() {
    }

    public Long getEnvironmentalVariableID() {
        return environmentalVariableID;
    }

    public void setEnvironmentalVariableID(Long environmentalVariableID) {
        this.environmentalVariableID = environmentalVariableID;
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
