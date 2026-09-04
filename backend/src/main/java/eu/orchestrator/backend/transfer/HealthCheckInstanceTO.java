package eu.orchestrator.backend.transfer;

import java.io.Serializable;

public class HealthCheckInstanceTO implements Serializable {

    private Long healthCheckInstanceID;
    private HealthCheckTO healthCheck;
    private String name;
    private String httpURL;
    private String args;
    private Long interval;

    public HealthCheckInstanceTO() {
    }

    public Long getHealthCheckInstanceID() {
        return healthCheckInstanceID;
    }

    public void setHealthCheckInstanceID(Long healthCheckInstanceID) {
        this.healthCheckInstanceID = healthCheckInstanceID;
    }

    public HealthCheckTO getHealthCheck() {
        return healthCheck;
    }

    public void setHealthCheck(HealthCheckTO healthCheck) {
        this.healthCheck = healthCheck;
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
