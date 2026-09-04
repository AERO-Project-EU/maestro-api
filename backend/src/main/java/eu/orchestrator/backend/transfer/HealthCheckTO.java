package eu.orchestrator.backend.transfer;

import java.io.Serializable;

public class HealthCheckTO implements Serializable {

    private Long healthCheckID;
    private String name;
    private String httpURL;
    private String args;
    private Long interval;

    public HealthCheckTO() {
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
}
