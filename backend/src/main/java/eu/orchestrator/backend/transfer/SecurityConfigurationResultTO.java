package eu.orchestrator.backend.transfer;

import java.io.Serializable;
import java.util.Date;


public class SecurityConfigurationResultTO implements Serializable {

    private String componentNodeInstance;
    private String status;
    private String description;
    private Date dateUpdated;

    public SecurityConfigurationResultTO() {
    }

    public String getComponentNodeInstance() {
        return componentNodeInstance;
    }

    public void setComponentNodeInstance(String componentNodeInstance) {
        this.componentNodeInstance = componentNodeInstance;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(Date dateUpdated) {
        this.dateUpdated = dateUpdated;
    }
}
