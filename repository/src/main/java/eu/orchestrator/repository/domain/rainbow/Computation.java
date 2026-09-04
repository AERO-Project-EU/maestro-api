package eu.orchestrator.repository.domain.rainbow;

import java.io.Serializable;
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
@Table(name = "slo_computation")
public class Computation implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "slo_id", nullable = false)
    private Slo slo;

    @Column(nullable = false)
    private String name;

    @Column(name = "every", nullable = false)
    private int interval;

    @OneToMany(mappedBy = "computation", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<NestedMetric> nestedMetrics = new HashSet<>();

    @OneToMany(mappedBy = "computation", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Expression> expressions = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Slo getSlo() {
        return slo;
    }

    public void setSlo(Slo slo) {
        this.slo = slo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public Set<NestedMetric> getNestedMetrics() {
        return nestedMetrics;
    }

    public void setNestedMetrics(
            Set<NestedMetric> nestedMetrics) {
        this.nestedMetrics = nestedMetrics;
    }

    public void addNestedMetric(NestedMetric nestedMetric) {
        this.nestedMetrics.add(nestedMetric);
        nestedMetric.setComputation(this);
    }

    public void removeNestedMetric(NestedMetric nestedMetric) {
        this.nestedMetrics.remove(nestedMetric);
        nestedMetric.setComputation(null);
    }

    public Set<Expression> getExpressions() {
        return expressions;
    }

    public void setExpressions(
            Set<Expression> expressions) {
        this.expressions = expressions;
    }

    public void addExpressions(Expression expression) {
        this.expressions.add(expression);
        expression.setComputation(this);
    }

    public void removeExpressions(Expression expression) {
        this.expressions.remove(expression);
        expression.setComputation(null);
    }
}
