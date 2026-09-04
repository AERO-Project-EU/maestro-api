package eu.orchestrator.repository.domain.rainbow;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="slo_expression")
public class Expression implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "expression_group_id", nullable = false)
    private ExpressionGroup expressionGroup;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "computation_id", nullable = false)
    private Computation computation;

    @Column(name = "target_value", nullable = false)
    private Integer targetValue;

    @Column(nullable = false)
    private Integer tolerance;

    private boolean isHigherBetter;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ExpressionGroup getExpressionGroup() {
        return expressionGroup;
    }

    public void setExpressionGroup(ExpressionGroup expressionGroup) {
        this.expressionGroup = expressionGroup;
    }

    public Computation getComputation() {
        return computation;
    }

    public void setComputation(Computation computation) {
        this.computation = computation;
    }

    public Integer getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(Integer targetValue) {
        this.targetValue = targetValue;
    }

    public Integer getTolerance() {
        return tolerance;
    }

    public void setTolerance(Integer tolerance) {
        this.tolerance = tolerance;
    }

    public boolean isHigherBetter() {
        return isHigherBetter;
    }

    public void setHigherBetter(boolean higherBetter) {
        isHigherBetter = higherBetter;
    }
}
