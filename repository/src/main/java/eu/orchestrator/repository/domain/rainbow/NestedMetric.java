package eu.orchestrator.repository.domain.rainbow;

import eu.orchestrator.repository.enums.ComputationNestedMetricType;
import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="slo_nested_metric")
public class NestedMetric implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "computation_id", nullable = false)
    private Computation computation;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "metric_id")
    private Metric metric;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ComputationNestedMetricType type = ComputationNestedMetricType.CONSTANT;

    private Double value;

    private String operand;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Computation getComputation() {
        return computation;
    }

    public void setComputation(Computation computation) {
        this.computation = computation;
    }

    public Metric getMetric() {
        return metric;
    }

    public void setMetric(Metric metric) {
        this.metric = metric;
    }

    public ComputationNestedMetricType getType() {
        return type;
    }

    public void setType(ComputationNestedMetricType type) {
        this.type = type;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public String getOperand() {
        return operand;
    }

    public void setOperand(String operand) {
        this.operand = operand;
    }
}
