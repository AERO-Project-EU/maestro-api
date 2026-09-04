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

// Distinct JPA entity name: Hibernate 5.6+ forbids two @Entity types sharing the simple name "Metric"
// (the other being eu.orchestrator.repository.domain.Metric -> table "metric"). Table mapping is unchanged.
@Entity(name = "SloMetric")
@Table(name = "slo_metric")
public class Metric implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "slo_id", nullable = false)
    private Slo slo;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    private String function;

    @Column(name="window_time", nullable = false)
    private int windowTime;

    @OneToMany(mappedBy = "metric", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<NestedMetric> nestedMetrics = new HashSet<>();

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

    public int getWindowTime() {
        return windowTime;
    }

    public void setWindowTime(int windowTime) {
        this.windowTime = windowTime;
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
        nestedMetric.setMetric(this);
    }

    public void removeNestedMetric(NestedMetric nestedMetric) {
        this.nestedMetrics.remove(nestedMetric);
        nestedMetric.setComputation(null);
    }
}
