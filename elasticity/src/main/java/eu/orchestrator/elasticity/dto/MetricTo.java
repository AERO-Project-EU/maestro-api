package eu.orchestrator.elasticity.dto;

import java.io.Serializable;

public class MetricTo implements Serializable {

    private String name;

    private String metric;

    private String function;

    private Integer windowTime;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public Integer getWindowTime() {
        return windowTime;
    }

    public void setWindowTime(Integer windowTime) {
        this.windowTime = windowTime;
    }
}
