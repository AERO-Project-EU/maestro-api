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
@Table(name = "slo_expression_group")
public class ExpressionGroup implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "slo_id", nullable = false)
    private Slo slo;

    @OneToMany(mappedBy = "expressionGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Expression> orClauses = new HashSet<>();

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

    public Set<Expression> getOrClauses() {
        return orClauses;
    }

    public void setOrClauses(Set<Expression> orClauses) {
        this.orClauses = orClauses;
    }

    public void addOrClause(Expression expression) {
        this.orClauses.add(expression);
        expression.setExpressionGroup(this);
    }

    public void removeOrClause(Expression expression) {
        this.orClauses.remove(expression);
        expression.setExpressionGroup(null);
    }
}
