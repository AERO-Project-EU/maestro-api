package eu.orchestrator.transfer.entities.metricExporter.requests;

import java.io.Serializable;

public class MetricsRequestTO implements Serializable {

    private ComponentInstance instance;

    private MetricsSource source;

    public ComponentInstance getInstance() {
        return instance;
    }

    public void setInstance(ComponentInstance instance) {
        this.instance = instance;
    }

    public MetricsSource getSource() {
        return source;
    }

    public void setSource(MetricsSource source) {
        this.source = source;
    }

}
