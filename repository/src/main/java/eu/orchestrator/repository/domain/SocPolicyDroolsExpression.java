package eu.orchestrator.repository.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "soc_policy_drools_expression")
public class SocPolicyDroolsExpression implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "soc_policy", nullable = true)
    private SocPolicy socPolicy;

    @Column(nullable = true)
    private String type;

    @Column(nullable = true)
    private String url;

    @Column(nullable = true)
    private String context;

    @Column(nullable = true)
    private String restMethod;

    @Column(nullable = true)
    private String brokerTopicName;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "socPolicyDroolsExpression")
    private List<SocPolicyDroolsExpressionHeader> header;

    @JsonProperty("graphHexID")
    @Column(nullable = true, name = "application_hex_id")
    private String applicationHexID;

    @JsonProperty("graphInstanceHexID")
    @Column(nullable = true, name = "application_instance_hex_id")
    private String applicationInstanceHexID;

    @Column(nullable = true, name = "component_node_hex_id")
    private String componentNodeHexID;

    @Column(nullable = true, name = "component_node_intance_hex_id")
    private String componentNodeInstanceHexID;

    @JsonIgnore
    @Column(nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP", name = "date_created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;

    @JsonIgnore
    @Column(nullable = false, name = "last_modified")
    private Date lastModified;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SocPolicy getSocPolicy() {
        return socPolicy;
    }

    public void setSocPolicy(SocPolicy socPolicy) {
        this.socPolicy = socPolicy;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getRestMethod() {
        return restMethod;
    }

    public void setRestMethod(String restMethod) {
        this.restMethod = restMethod;
    }

    public String getBrokerTopicName() {
        return brokerTopicName;
    }

    public void setBrokerTopicName(String brokerTopicName) {
        this.brokerTopicName = brokerTopicName;
    }

    public List<SocPolicyDroolsExpressionHeader> getHeader() {
        return header;
    }

    public void setHeader(List<SocPolicyDroolsExpressionHeader> header) {
        this.header = header;
    }

    public String getApplicationHexID() {
        return applicationHexID;
    }

    public void setApplicationHexID(String applicationHexID) {
        this.applicationHexID = applicationHexID;
    }

    public String getApplicationInstanceHexID() {
        return applicationInstanceHexID;
    }

    public void setApplicationInstanceHexID(String applicationInstanceHexID) {
        this.applicationInstanceHexID = applicationInstanceHexID;
    }

    public String getComponentNodeHexID() {
        return componentNodeHexID;
    }

    public void setComponentNodeHexID(String componentNodeHexID) {
        this.componentNodeHexID = componentNodeHexID;
    }

    public String getComponentNodeInstanceHexID() {
        return componentNodeInstanceHexID;
    }

    public void setComponentNodeInstanceHexID(String componentNodeInstanceHexID) {
        this.componentNodeInstanceHexID = componentNodeInstanceHexID;
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

    /**
     *
     * Inner enum class
     *
     */
    public enum Type {
        COMMAND("execute command"),
//        INTERACT_WITH_WAZUH("interact with WAZUH"),
//        INTERACT_WITH_OWLH("interact with OWLH"),
//        INTERACT_WITH_IPS("interact with IPS"),
//        INTERACT_WITH_CONTROL_PLANE("interact with Control Plane"),
        REST_CALL("rest call"),
        PUSH_MESSAGE_TO_KAFKA("push message to kafka"),
        PUSH_MESSAGE_TO_RABBITMQ("push message to rabbitmq");
        private String friendlyName;

        Type(String friendlyName) {
            this.friendlyName = friendlyName;
        }

        public String getFriendlyName() {
            return friendlyName;
        }
    }

    public enum RestMethod {
        POST("POST"),
        GET("GET"),
        PUT("PUT"),
        DELETE("DELETE");
        private String friendlyName;

        RestMethod(String friendlyName) {
            this.friendlyName = friendlyName;
        }

        public String getFriendlyName() {
            return friendlyName;
        }
    }
}
