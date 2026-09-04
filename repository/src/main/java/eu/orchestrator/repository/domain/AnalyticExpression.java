package eu.orchestrator.repository.domain;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@Table(name = "analytic_expression")
public class AnalyticExpression implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long analyticExpressionId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "analytic", nullable = false)
    private Analytic analytic;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "component_node_instance", nullable = false)
    private ComponentNodeInstance componentNodeInstance;

    @Column(nullable = false)
    private String metric;

    @Column
    private String dimension;

    @Column(name = "arithmetic_operator")
    private String arithmeticOperator;

    @Column
    private Long metricOrder;

    @Column(nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP", name = "date_created")
    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    private Date dateCreated;

    @Column(nullable = false, name = "last_modified")
    @Temporal(TemporalType.TIMESTAMP)
    @UpdateTimestamp
    private Date lastModified;

    public AnalyticExpression() {
    }

    public enum AnalyticExpressionArithmeticOperator {

        ADDITION("+"),
        SUBTRACTION("-"),
        MULTIPLICATION("*"),
        DIVISION("/");

        private final String friendlyName;

        AnalyticExpressionArithmeticOperator(String friendlyName) {
            this.friendlyName = friendlyName;
        }

        public String getFriendlyName() {
            return friendlyName;
        }
    }

    public Long getAnalyticExpressionId() {
        return analyticExpressionId;
    }

    public void setAnalyticExpressionId(Long analyticExpressionId) {
        this.analyticExpressionId = analyticExpressionId;
    }

    public Analytic getAnalytic() {
        return analytic;
    }

    public void setAnalytic(Analytic analytic) {
        this.analytic = analytic;
    }

    public ComponentNodeInstance getComponentNodeInstance() {
        return componentNodeInstance;
    }

    public void setComponentNodeInstance(ComponentNodeInstance componentNodeInstance) {
        this.componentNodeInstance = componentNodeInstance;
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
