package eu.orchestrator.elasticity.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class ElasticityTo implements Serializable {

    private Long id;

    private String name;

    private String elasticityStrategy;

    private String metricComponentNodeHexID;

    private List<MetricTo> metrics;

    private List<ComputationTo> computations;

    private List<ExpressionTo> expressions;

    private Date dateCreated;

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

    public String getElasticityStrategy() {
        return elasticityStrategy;
    }

    public void setElasticityStrategy(String elasticityStrategy) {
        this.elasticityStrategy = elasticityStrategy;
    }

    public String getMetricComponentNodeHexID() {
        return metricComponentNodeHexID;
    }

    public void setMetricComponentNodeHexID(String metricComponentNodeHexID) {
        this.metricComponentNodeHexID = metricComponentNodeHexID;
    }

    public List<MetricTo> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<MetricTo> metrics) {
        this.metrics = metrics;
    }

    public List<ComputationTo> getComputations() {
        return computations;
    }

    public void setComputations(List<ComputationTo> computations) {
        this.computations = computations;
    }

    public List<ExpressionTo> getExpressions() {
        return expressions;
    }

    public void setExpressions(List<ExpressionTo> expressions) {
        this.expressions = expressions;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }
}
