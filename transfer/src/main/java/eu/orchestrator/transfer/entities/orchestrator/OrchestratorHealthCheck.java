package eu.orchestrator.transfer.entities.orchestrator;

import java.io.Serializable;

public class OrchestratorHealthCheck implements Serializable {

    private String id;
    private String args;
    private String httpURL;
    private String interval;

    public OrchestratorHealthCheck() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getArgs() {
        return args;
    }

    public void setArgs(String args) {
        this.args = args;
    }

    public String getHttpURL() {
        return httpURL;
    }

    public void setHttpURL(String httpURL) {
        this.httpURL = httpURL;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }
}
