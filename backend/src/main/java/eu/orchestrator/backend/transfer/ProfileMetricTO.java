package eu.orchestrator.backend.transfer;

import java.io.Serializable;

public class ProfileMetricTO implements Serializable {

    private String componentNodeHexId;
    private String metric;
    private String dimension;

    public String getComponentNodeHexId() {
        return componentNodeHexId;
    }

    public void setComponentNodeHexId(String componentNodeHexId) {
        this.componentNodeHexId = componentNodeHexId;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public String getDimension() {
        return dimension;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }
}
