package eu.orchestrator.repository.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "soc_policy")
public class SocPolicy implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(nullable = true)
    private String name;

    @Column(nullable = true, name = "hex_id", unique = true)
    private String hexID;


    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "application_instance", nullable = true)
    private ApplicationInstance applicationInstance;

    //TODO
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "socPolicy")
    private List<SocPolicyInputKafkaStream> inputStreamList;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "socPolicy")
    private List<SocPolicyKafkaExpression> kafkaExpressionList;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "socPolicy")
    private List<SocPolicyDroolsExpression> droolsExpressionList;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "socPolicy")
    private List<SocPolicyOutputDroolsAction> outputDroolsActionList;

    @Column(nullable = true)
    private String droolsInertiaPeriodInSecond;

    @Column(nullable = true, length = 2000)
    private String kafkaRuleExpression;

    @Column(nullable = true)
    private String status;

    @Column(nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP", name = "date_created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;

    @JsonIgnore
    @Column(nullable = false, name = "last_modified")
    private Date lastModified;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "user", nullable = true)
    private User user;

    public SocPolicy() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHexID() {
        return hexID;
    }

    public void setHexID(String hexID) {
        this.hexID = hexID;
    }

    public ApplicationInstance getApplicationInstance() {
        return applicationInstance;
    }

    public void setApplicationInstance(ApplicationInstance applicationInstance) {
        this.applicationInstance = applicationInstance;
    }

    public List<SocPolicyInputKafkaStream> getInputStreamList() {
        return inputStreamList;
    }

    public void setInputStreamList(List<SocPolicyInputKafkaStream> inputStreamList) {
        this.inputStreamList = inputStreamList;
    }

    public List<SocPolicyKafkaExpression> getKafkaExpressionList() {
        return kafkaExpressionList;
    }

    public void setKafkaExpressionList(List<SocPolicyKafkaExpression> kafkaExpressionList) {
        this.kafkaExpressionList = kafkaExpressionList;
    }

    public List<SocPolicyDroolsExpression> getDroolsExpressionList() {
        return droolsExpressionList;
    }

    public void setDroolsExpressionList(List<SocPolicyDroolsExpression> droolExpressionList) {
        this.droolsExpressionList = droolExpressionList;
    }

    public List<SocPolicyOutputDroolsAction> getOutputDroolsActionList() {
        return outputDroolsActionList;
    }

    public void setOutputDroolsActionList(List<SocPolicyOutputDroolsAction> outputDroolActionList) {
        this.outputDroolsActionList = outputDroolActionList;
    }

    public String getDroolsInertiaPeriodInSecond() {
        return droolsInertiaPeriodInSecond;
    }

    public void setDroolsInertiaPeriodInSecond(String droolInertiaPeriodInSecond) {
        this.droolsInertiaPeriodInSecond = droolInertiaPeriodInSecond;
    }

    public String getKafkaRuleExpression() {
        return kafkaRuleExpression;
    }

    public void setKafkaRuleExpression(String kafkaRuleExpression) {
        this.kafkaRuleExpression = kafkaRuleExpression;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Boolean hasEditAllowance(User user) {
        if (user != null && (getUser().equals(user) ||
                user.isAdmin() ||
                (user.isOrganizationAdmin() && user.getOrganization()
                        .equals(getUser().getOrganization())))) {
            return true;
        }
        return false;
    }

    public Boolean hasDeleteAllowance(User user) {
        if (user != null && (getUser().equals(user) ||
                user.isAdmin() || (user.isOrganizationAdmin() && user.getOrganization()
                .equals(getUser().getOrganization())))) {
            return true;
        }
        return false;
    }

    /**
     * Inner enum class
     */

    public enum RuntimePolicyStatus {

        PENDING("Pending"),
        APPLIED_STREAM("Applied Stream (Drools pending)"),
        APPLIED_DROOLS("Applied (Stream and Drools)"),
        ERROR_OCCURRED_STREAM("Error Occurred On Stream"),
        ERROR_OCCURRED_DROOLS("Error Occurred On Drools");

        private String friendlyName;

        RuntimePolicyStatus(String friendlyName) {
            this.friendlyName = friendlyName;
        }

        public String getFriendlyName() {
            return friendlyName;
        }
    }
}
