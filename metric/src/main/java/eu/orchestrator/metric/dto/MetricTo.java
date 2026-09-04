package eu.orchestrator.metric.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class MetricTo implements Serializable {

    private Long id;

    private String name;
    private String type;
    private String function;
    private Integer periodicity;
    private Integer window;
    private List<MetricExpressionTo> metricExpressions;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public Integer getPeriodicity() {
        return periodicity;
    }

    public void setPeriodicity(Integer periodicity) {
        this.periodicity = periodicity;
    }

    public Integer getWindow() {
        return window;
    }

    public void setWindow(Integer window) {
        this.window = window;
    }

    public List<MetricExpressionTo> getMetricExpressions() {
        return metricExpressions;
    }

    public void setMetricExpressions(List<MetricExpressionTo> metricExpressions) {
        this.metricExpressions = metricExpressions;
    }
}
