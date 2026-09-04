package eu.orchestrator.repository.domain.rainbow;

import eu.orchestrator.repository.domain.ApplicationInstance;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "slo")
public class Slo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "application_instance_id", nullable = false)
    private ApplicationInstance applicationInstance;

    @Column(nullable = false)
    private String name;

    @Column(name="elasticity_strategy", nullable = false)
    private String elasticityStrategy;

    @Column(name="component_node_hex_id", nullable = false)
    private String componentNodeHexId;

    @OneToMany(mappedBy = "slo", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Metric> metrics = new HashSet<>();

    @OneToMany(mappedBy = "slo", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Computation> computations = new HashSet<>();

    @OneToMany(mappedBy = "slo", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ExpressionGroup> expressions = new HashSet<>();

    @Column(name = "created_at")
    private Date createdAt = new Date();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ApplicationInstance getApplicationInstance() {
        return applicationInstance;
    }

    public void setApplicationInstance(
            ApplicationInstance applicationInstance) {
        this.applicationInstance = applicationInstance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullName() {
        return name + "-" + id;
    }

    public String getElasticityStrategy() {
        return elasticityStrategy;
    }

    public void setElasticityStrategy(String elasticityStrategy) {
        this.elasticityStrategy = elasticityStrategy;
    }

    public String getComponentNodeHexId() {
        return componentNodeHexId;
    }

    public void setComponentNodeHexId(String componentNodeHexId) {
        this.componentNodeHexId = componentNodeHexId;
    }

    public Set<Metric> getMetrics() {
        return metrics;
    }

    public void setMetrics(Set<Metric> metrics) {
        this.metrics = metrics;
    }

    public void addMetric(Metric metric) {
        this.metrics.add(metric);
        metric.setSlo(this);
    }

    public void removeMetric(
            Metric metric) {
        this.metrics.remove(metric);
        metric.setSlo(null);
    }

    public Set<Computation> getComputations() {
        return computations;
    }

    public void setComputations(
            Set<Computation> computations) {
        this.computations = computations;
    }

    public void addComputation(Computation computation) {
        this.computations.add(computation);
        computation.setSlo(this);
    }

    public void removeComputation(
            Computation computation) {
        this.computations.remove(computation);
        computation.setSlo(null);
    }

    public Set<ExpressionGroup> getExpressions() {
        return expressions;
    }

    public void setExpressions(
            Set<ExpressionGroup> expressions) {
        this.expressions = expressions;
    }

    public void addExpressionGroup(
            ExpressionGroup expressionGroup) {
        this.expressions.add(expressionGroup);
        expressionGroup.setSlo(this);
    }

    public void removeExpressionGroup(
            ExpressionGroup expressionGroup) {
        this.expressions.remove(expressionGroup);
        expressionGroup.setSlo(null);
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
