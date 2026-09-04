package eu.orchestrator.backend.service.model;

import java.io.Serializable;

public class PrometheusResult implements Serializable {

    private PrometheusMetric metric;
    private String[] value;


    public PrometheusMetric getMetric() {
        return metric;
    }

    public void setMetric(PrometheusMetric metric) {
        this.metric = metric;
    }

    public String[] getValue() {
        return value;
    }

    public void setValue(String[] value) {
        this.value = value;
    }
}
