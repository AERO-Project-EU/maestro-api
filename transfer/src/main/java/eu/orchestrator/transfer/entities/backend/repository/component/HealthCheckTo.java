package eu.orchestrator.transfer.entities.backend.repository.component;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 24/1/22
 */
public class HealthCheckTo implements Serializable {

    private Long healthCheckID;
    private String name;
    private String httpURL;
    private String args;
    private Long interval;
    private Date dateCreated;
    private Date lastModified;

    public HealthCheckTo() {
    }

    public Long getHealthCheckID() {
        return healthCheckID;
    }

    public void setHealthCheckID(Long healthCheckID) {
        this.healthCheckID = healthCheckID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHttpURL() {
        return httpURL;
    }

    public void setHttpURL(String httpURL) {
        this.httpURL = httpURL;
    }

    public String getArgs() {
        return args;
    }

    public void setArgs(String args) {
        this.args = args;
    }

    public Long getInterval() {
        return interval;
    }

    public void setInterval(Long interval) {
        this.interval = interval;
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
