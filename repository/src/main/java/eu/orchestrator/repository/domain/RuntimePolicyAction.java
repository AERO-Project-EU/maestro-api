package eu.orchestrator.repository.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "runtime_policy_action")
public class RuntimePolicyAction implements Serializable {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "runtime_policy", nullable = true)
    private RuntimePolicy runtimePolicy;

    @Column(nullable = true, name = "component_name")
    private String componentName;

    @Column(nullable = true, name = "component_hex_id")
    private String componentHexID;

    @Column(nullable = true)
    private String type;

    @Column(nullable = true)
    private Integer workers;

    @Column(nullable = true) //  columnDefinition="TEXT"
    private String context;

    @Column(nullable = true, name = "topic_url")
    private String topicURL;

    @Column(nullable = true, name = "topic_port")
    private String topicPort;

    @Column(nullable = true, name = "topic_name")
    private String topicName;

    @Column(nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP", name = "date_created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;

    @Column(nullable = false, name = "last_modified")
    private Date lastModified;

    public RuntimePolicyAction() {
    }

    public enum ActionType {

        scaleOut("Scale Out"),
        scaleIn("Scale In"),
        info("Info"),
        pushIntoTopic("Push into Topic");

        private String friendlyName;

        ActionType(String friendlyName) {
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

    public RuntimePolicy getRuntimePolicy() {
        return runtimePolicy;
    }

    public void setRuntimePolicy(RuntimePolicy runtimePolicy) {
        this.runtimePolicy = runtimePolicy;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getTopicURL() {
        return topicURL;
    }

    public void setTopicURL(String topicURL) {
        this.topicURL = topicURL;
    }

    public String getTopicPort() {
        return topicPort;
    }

    public void setTopicPort(String topicPort) {
        this.topicPort = topicPort;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getWorkers() {
        return workers;
    }

    public void setWorkers(Integer workers) {
        this.workers = workers;
    }

    public String getComponentHexID() {
        return componentHexID;
    }

    public void setComponentHexID(String componentHexID) {
        this.componentHexID = componentHexID;
    }
}
