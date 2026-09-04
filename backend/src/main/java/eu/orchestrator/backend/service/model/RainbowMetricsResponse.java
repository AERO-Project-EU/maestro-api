package eu.orchestrator.backend.service.model;

import java.io.Serializable;
import java.util.List;

public class RainbowMetricsResponse implements Serializable {

    private List<RainbowMetricsValues> monitoring;


    public List<RainbowMetricsValues> getMonitoring() {
        return monitoring;
    }

    public void setMonitoring(List<RainbowMetricsValues> monitoring) {
        this.monitoring = monitoring;
    }
}
