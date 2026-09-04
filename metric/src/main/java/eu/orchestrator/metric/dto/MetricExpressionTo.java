package eu.orchestrator.metric.dto;

import java.io.Serializable;

public class MetricExpressionTo implements Serializable {

    private Long id;
    private String componentNodeHexID;

    private String metric;
    private String dimension;
    private String arithmeticOperator;
    private Long metricOrder;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getComponentNodeHexID() {
        return componentNodeHexID;
    }

    public void setComponentNodeHexID(String componentNodeHexID) {
        this.componentNodeHexID = componentNodeHexID;
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

    public String getArithmeticOperator() {
        return arithmeticOperator;
    }

    public void setArithmeticOperator(String arithmeticOperator) {
        this.arithmeticOperator = arithmeticOperator;
    }

    public Long getMetricOrder() {
        return metricOrder;
    }

    public void setMetricOrder(Long metricOrder) {
        this.metricOrder = metricOrder;
    }
}
