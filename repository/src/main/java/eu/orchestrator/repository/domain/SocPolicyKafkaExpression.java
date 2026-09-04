package eu.orchestrator.repository.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "soc_policy_kafka_expression")
public class SocPolicyKafkaExpression implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "soc_policy", nullable = true)
    private SocPolicy socPolicy;

    @Column(nullable = true)
    private String inputTopic;

    @Column(nullable = true)
    private String operand;

    @Column(nullable = true)
    private String logical;

    @Column(nullable = true)
    private String fieldName;

    @Column(nullable = true)
    private String context;

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

    public String getInputTopic() {
        return inputTopic;
    }

    public void setInputTopic(String inputTopic) {
        this.inputTopic = inputTopic;
    }

    public String getOperand() {
        return operand;
    }

    public void setOperand(String operand) {
        this.operand = operand;
    }

    public String getLogical() {
        return logical;
    }

    public void setLogical(String logical) {
        this.logical = logical;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
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
     * Inner enum class
     */
    public enum Operand {
        // contains for strings
        LIKE("Like"),

        // >
        GREATER_THAN(">"),

        // <
        LESS_THAN("<"),

        // >=
        GREATER_OR_EQUAL_THAN(">="),

        // <=
        LESS_OR_EQUAL_THAN("=<");
        private String friendlyName;

        Operand(String friendlyName) {
            this.friendlyName = friendlyName;
        }

        public String getFriendlyName() {
            return friendlyName;
        }
    }

    public enum Logical {
        AND("AND"),
        OR("OR"),
        OPEN_PARETHESIS("("),
        CLOSE_PARETHESIS(")");
        private String friendlyName;

        Logical(String friendlyName) {
            this.friendlyName = friendlyName;
        }

        public String getFriendlyName() {
            return friendlyName;
        }
    }
}
