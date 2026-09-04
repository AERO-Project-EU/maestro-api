package eu.orchestrator.backend.transfer;

import java.io.Serializable;
import java.util.Date;

public class StatusTO implements Serializable {

    private Long componentNodeInstanceStatusID;
    private Long componentNodeInstanceID;
    private String componentNodeInstanceName;
    private String applicationInstanceName;
    private Long applicationInstanceID;
    private String reportedChange;
    private String message;
    private String status;
    private Date dateCreated;
    private Date lastModified;


    public StatusTO() {
    }

    public Long getComponentNodeInstanceStatusID() {
        return componentNodeInstanceStatusID;
    }

    public void setComponentNodeInstanceStatusID(Long componentNodeInstanceStatusID) {
        this.componentNodeInstanceStatusID = componentNodeInstanceStatusID;
    }

    public Long getComponentNodeInstanceID() {
        return componentNodeInstanceID;
    }

    public void setComponentNodeInstanceID(Long componentNodeInstanceID) {
        this.componentNodeInstanceID = componentNodeInstanceID;
    }

    public String getComponentNodeInstanceName() {
        return componentNodeInstanceName;
    }

    public void setComponentNodeInstanceName(String componentNodeInstanceName) {
        this.componentNodeInstanceName = componentNodeInstanceName;
    }

    public String getApplicationInstanceName() {
        return applicationInstanceName;
    }

    public void setApplicationInstanceName(String applicationInstanceName) {
        this.applicationInstanceName = applicationInstanceName;
    }

    public Long getApplicationInstanceID() {
        return applicationInstanceID;
    }

    public void setApplicationInstanceID(Long applicationInstanceID) {
        this.applicationInstanceID = applicationInstanceID;
    }

    public String getReportedChange() {
        return reportedChange;
    }

    public void setReportedChange(String reportedChange) {
        this.reportedChange = reportedChange;
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
