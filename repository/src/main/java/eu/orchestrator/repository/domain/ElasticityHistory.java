package eu.orchestrator.repository.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 18/4/2019
 */

@Entity
@Table(name = "elasticity_history")
public class ElasticityHistory implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

//    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "application_instance", nullable = false)
    private ApplicationInstance applicationInstance;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "component_node", nullable = false)
    private ComponentNode componentNode;

    @Column(nullable = false, name = "workers_count")
    private Long workersCount;

    @Column(nullable = false, name = "active_workers")
    private Long activeWorkers;

    @Column(nullable = false, name = "status")
    private String status;

    @Column(nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP", name = "date_created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;

    @Column(nullable = false, name = "last_modified")
    private Date lastModified;

    public enum ElasticityStatus {

        DEPLOYING("Deploying"),
        SCALING_OUT("Scaling OUT"),
        SCALING_IN("Scaling IN"),
        NEUTRAL("Neutra");

        private String friendlyName;

        ElasticityStatus(String friendlyName) {
            this.friendlyName = friendlyName;
        }

        public String getFriendlyName() {
            return friendlyName;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ApplicationInstance getApplicationInstance() {
        return applicationInstance;
    }

    public void setApplicationInstance(ApplicationInstance applicationInstance) {
        this.applicationInstance = applicationInstance;
    }

    public ComponentNode getComponentNode() {
        return componentNode;
    }

    public void setComponentNode(ComponentNode componentNode) {
        this.componentNode = componentNode;
    }

    public Long getWorkersCount() {
        return workersCount;
    }

    public void setWorkersCount(Long workersCount) {
        this.workersCount = workersCount;
    }

    public Long getActiveWorkers() {
        return activeWorkers;
    }

    public void setActiveWorkers(Long activeWorkers) {
        this.activeWorkers = activeWorkers;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
