package eu.orchestrator.transfer.entities.backend.repository.component;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 24/1/22
 */
public class EnvironmentalVariableTo implements Serializable {

    private Long environmentalVariableID;
    private String key;
    private String value;
    private Date dateCreated;
    private Date lastModified;

    public EnvironmentalVariableTo() {
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
