package eu.orchestrator.repository.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "id_rule")
public class IDRule implements Serializable, Comparable<IDRule> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long ruleID;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "id_rule_set", nullable = true)
  private IDRuleSet idRuleSet;

  @Column(nullable = true, columnDefinition = "LONGTEXT")
  private String name;

  @Column(nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP", name = "date_created")
  @Temporal(TemporalType.TIMESTAMP)
  private Date dateCreated;

  @Column(nullable = false, name = "last_modified")
  private Date lastModified;

  public IDRule() {
  }

  public Long getRuleID() {
    return ruleID;
  }

  public void setRuleID(Long ruleID) {
    this.ruleID = ruleID;
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

  @Override
  public int compareTo(IDRule compareLabel) {
    String compareQuantity = compareLabel.getName();

    return this.name.compareTo(compareQuantity);
  }
}
