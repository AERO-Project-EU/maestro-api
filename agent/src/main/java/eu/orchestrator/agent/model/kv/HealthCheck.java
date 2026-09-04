package eu.orchestrator.agent.model.kv;

/**
 * @author Panagiotis Parthenis.
 */
public class HealthCheck {

    private String interval;
    private String args;
    private String httpEndpoint;

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public String getArgs() {
        return args;
    }

    public void setArgs(String args) {
        this.args = args;
    }

    public String getHttpEndpoint() {
        return httpEndpoint;
    }

    public void setHttpEndpoint(String httpEndpoint) {
        this.httpEndpoint = httpEndpoint;
    }

}
