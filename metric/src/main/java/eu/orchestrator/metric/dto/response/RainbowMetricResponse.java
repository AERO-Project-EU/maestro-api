package eu.orchestrator.metric.dto.response;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RainbowMetricResponse implements Serializable {

    private List<RainbowMetric> metric;


    public List<RainbowMetric> getMetric() {
        if (metric == null) {
            metric = new ArrayList<>();
        }
        return metric;
    }

    public void setMetric(List<RainbowMetric> metric) {
        this.metric = metric;
    }
}
