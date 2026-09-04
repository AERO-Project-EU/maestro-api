package eu.orchestrator.backend.transfer;

import java.io.Serializable;
import java.util.Date;

public class AlertTO implements Serializable {

    private Long componentNodeInstanceAlertID;
    private Long componentNodeInstanceID;
    private Long applicationInstanceID;
    private String graphInstanceID;
    private String message;
    private String status;
    private String componentNodeInstanceName;
    private Date dateCreated;
    private Date lastModified;

    public AlertTO() {
    }

    public Long getComponentNodeInstanceAlertID() {
        return componentNodeInstanceAlertID;
    }

    public void setComponentNodeInstanceAlertID(Long componentNodeInstanceAlertID) {
        this.componentNodeInstanceAlertID = componentNodeInstanceAlertID;
    }

    public Long getComponentNodeInstanceID() {
        return componentNodeInstanceID;
    }

    public void setComponentNodeInstanceID(Long componentNodeInstanceID) {
        this.componentNodeInstanceID = componentNodeInstanceID;
    }

    public Long getApplicationInstanceID() {
        return applicationInstanceID;
    }

    public void setApplicationInstanceID(Long applicationInstanceID) {
        this.applicationInstanceID = applicationInstanceID;
    }

    public String getGraphInstanceID() {
        return graphInstanceID;
    }

    public void setGraphInstanceID(String graphInstanceID) {
        this.graphInstanceID = graphInstanceID;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getComponentNodeInstanceName() {
        return componentNodeInstanceName;
    }

    public void setComponentNodeInstanceName(String componentNodeInstanceName) {
        this.componentNodeInstanceName = componentNodeInstanceName;
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
