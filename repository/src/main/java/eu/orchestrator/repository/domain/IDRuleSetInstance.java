package eu.orchestrator.repository.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "id_rule_set_instance")
public class IDRuleSetInstance implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long ruleSetInstanceID;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "component_node_instance", nullable = true)
  private ComponentNodeInstance componentNodeInstance;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "id_rule_set", nullable = true)
  private IDRuleSet idRuleSet;

  @Column(nullable = true)
  private String name;

  @Column(nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP", name = "date_created")
  @Temporal(TemporalType.TIMESTAMP)
  private Date dateCreated;

  @Column(nullable = false, name = "last_modified")
  private Date lastModified;

  @Transient
  private Long idRuleSetID;

  public IDRuleSetInstance() {
  }

  public Long getRuleSetInstanceID() {
    return ruleSetInstanceID;
  }

  public void setRuleSetInstanceID(Long ruleSetInstanceID) {
    this.ruleSetInstanceID = ruleSetInstanceID;
  }

  public ComponentNodeInstance getComponentNodeInstance() {
    return componentNodeInstance;
  }

  public void setComponentNodeInstance(ComponentNodeInstance componentNodeInstance) {
    this.componentNodeInstance = componentNodeInstance;
  }

  public IDRuleSet getIdRuleSet() {
    return idRuleSet;
  }

  public void setIdRuleSet(IDRuleSet idRuleSet) {
    this.idRuleSet = idRuleSet;
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

  public Long getIdRuleSetID() {
    return idRuleSetID;
  }

  public void setIdRuleSetID(Long idRuleSetID) {
    this.idRuleSetID = idRuleSetID;
  }
}
