package eu.orchestrator.transfer.entities.metricExporter.requests;

import java.io.Serializable;

public class MetricsSource implements Serializable {

    private String type;

    private String url;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
