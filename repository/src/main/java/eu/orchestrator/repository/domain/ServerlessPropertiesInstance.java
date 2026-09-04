package eu.orchestrator.repository.domain;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "serverless_properties_instance")
public class ServerlessPropertiesInstance implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long serverlessPropertiesInstanceID;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @OneToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "component_node_instance", nullable = true)
    private ComponentNodeInstance componentNodeInstance;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "serverless_properties", nullable = true)
    private ServerlessProperties serverlessProperties;

    @Column(nullable = true, name = "autoscaler")
    private String autoscaler;

    @Column(nullable = true, name = "metric")
    private String metric;

    @Column(nullable = true, name = "target_value")
    private String targetValue;

    @Column(nullable = true, name = "min_scale")
    private String minScale;

    @Column(nullable = true, name = "max_scale")
    private String maxScale;

    @Column(nullable = true, name = "window_size")
    private String windowSize;

    @Column(nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP", name = "date_created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;

    @Column(nullable = false, name = "last_modified")
    private Date lastModified;

    public ServerlessPropertiesInstance() {
    }

    public Long getServerlessPropertiesInstanceID() {
        return serverlessPropertiesInstanceID;
    }

    public void setServerlessPropertiesInstanceID(Long serverlessPropertiesInstanceID) {
        this.serverlessPropertiesInstanceID = serverlessPropertiesInstanceID;
    }

    public ComponentNodeInstance getComponentNodeInstance() {
        return componentNodeInstance;
    }

    public void setComponentNodeInstance(ComponentNodeInstance componentNodeInstance) {
        this.componentNodeInstance = componentNodeInstance;
    }

    public ServerlessProperties getServerlessProperties() {
        return serverlessProperties;
    }

    public void setServerlessProperties(ServerlessProperties serverlessProperties) {
        this.serverlessProperties = serverlessProperties;
    }

    public String getAutoscaler() {
        return autoscaler;
    }

    public void setAutoscaler(String autoscaler) {
        this.autoscaler = autoscaler;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public String getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(String targetValue) {
        this.targetValue = targetValue;
    }

    public String getMinScale() {
        return minScale;
    }

    public void setMinScale(String minScale) {
        this.minScale = minScale;
    }

    public String getMaxScale() {
        return maxScale;
    }

    public void setMaxScale(String maxScale) {
        this.maxScale = maxScale;
    }

    public String getWindowSize() {
        return windowSize;
    }

    public void setWindowSize(String windowSize) {
        this.windowSize = windowSize;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }
}
