package eu.orchestrator.repository.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "runtime_policy")
public class RuntimePolicy implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "application_instance", nullable = true)
  private ApplicationInstance applicationInstance;

  @Column(nullable = true)
  private String name;

  @Column(nullable = true, name = "hex_id", unique = true)
  private String hexID;

  @Column(nullable = true)
  private String type;

  @Column(nullable = true, name = "policy", columnDefinition = "TEXT")
  private String policy;

  @Column(nullable = true, name = "policy_expression", columnDefinition = "TEXT")
  private String policyExpression;

  @Column(nullable = true, name = "policy_period")
  private String policyPeriod;

  @Column(nullable = true, name = "inertia_period")
  private String inertiaPeriod;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "runtimePolicy")
  private List<RuntimePolicyAction> actions;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "runtimePolicy")
  private List<RuntimePolicyExpression> expressions;

  @Column(nullable = true)
  private String status;

  @Column(nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP", name = "date_created")
  @Temporal(TemporalType.TIMESTAMP)
  private Date dateCreated;

  @Column(nullable = false, name = "last_modified")
  private Date lastModified;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "user", nullable = true)
  private User user;

  public RuntimePolicy() {
  }

  public enum Type {

    ELASTICITY,
    SECURITY;
  }

  public enum RuntimePolicyStatus {

    PENDING("Pending"),
    APPLIED("Applied"),
    ERROR_OCCURRED("Error Occurred");

    private String friendlyName;

    RuntimePolicyStatus(String friendlyName) {
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

  public String getPolicy() {
    return policy;
  }

  public void setPolicy(String policy) {
    this.policy = policy;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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

  public ApplicationInstance getApplicationInstance() {
    return applicationInstance;
  }

  public void setApplicationInstance(ApplicationInstance applicationInstance) {
    this.applicationInstance = applicationInstance;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public List<RuntimePolicyAction> getActions() {
    return actions;
  }

  public void setActions(List<RuntimePolicyAction> actions) {
    this.actions = actions;
  }

  public String getPolicyExpression() {
    return policyExpression;
  }

  public void setPolicyExpression(String policyExpression) {
    this.policyExpression = policyExpression;
  }

  public String getPolicyPeriod() {
    return policyPeriod;
  }

  public void setPolicyPeriod(String policyPeriod) {
    this.policyPeriod = policyPeriod;
  }

  public String getInertiaPeriod() {
    return inertiaPeriod;
  }

  public void setInertiaPeriod(String inertiaPeriod) {
    this.inertiaPeriod = inertiaPeriod;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getHexID() {
    return hexID;
  }

  public void setHexID(String hexID) {
    this.hexID = hexID;
  }

  public List<RuntimePolicyExpression> getExpressions() {
    return expressions;
  }

  public void setExpressions(List<RuntimePolicyExpression> expressions) {
    this.expressions = expressions;
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
}
