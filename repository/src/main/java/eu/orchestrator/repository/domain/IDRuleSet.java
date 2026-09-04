package eu.orchestrator.repository.domain;

import jakarta.persistence.Convert;
import org.hibernate.type.NumericBooleanConverter;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "id_rule_set")
public class IDRuleSet implements Serializable, Comparable<IDRuleSet> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = true)
  private String name;

  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "user", nullable = true)
  private User user;

  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "organization", nullable = true)
  private Organization organization;

  @Column(nullable = true, columnDefinition = "timestamp default CURRENT_TIMESTAMP", name = "date_created")
  @Temporal(TemporalType.TIMESTAMP)
  private Date dateCreated;

  @Column(nullable = true, name = "last_modified")
  private Date lastModified;

  //    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "idRuleSet")
  private List<IDRule> iDRules;

  @Column(nullable = true, name = "is_predefined", columnDefinition = "TINYINT(1) UNSIGNED default '0'")
  @Convert(converter = NumericBooleanConverter.class)
  private Boolean predefinedIDRuleSet = false;

  @Transient
  private Integer rulesCounter;

  @Column(nullable = true, name = "is_public", columnDefinition = "TINYINT(1) UNSIGNED default '0'")
  @Convert(converter = NumericBooleanConverter.class)
  private Boolean publicIDRuleSet = false;

  public IDRuleSet() {
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

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
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

  public List<IDRule> getiDRules() {
    return iDRules;
  }

  public void setiDRules(List<IDRule> iDRules) {
    this.iDRules = iDRules;
  }

  public Integer getRulesCounter() {
    return rulesCounter;
  }

  public void setRulesCounter(Integer rulesCounter) {
    this.rulesCounter = rulesCounter;
  }

  public Boolean getPredefinedIDRuleSet() {
    return predefinedIDRuleSet;
  }

  public void setPredefinedIDRuleSet(Boolean predefinedIDRuleSet) {
    this.predefinedIDRuleSet = predefinedIDRuleSet;
  }

  @Override
  public int compareTo(IDRuleSet compareLabel) {
    Long compareQuantity = compareLabel.getId();

    return this.id.compareTo(compareQuantity);
  }

  public List<Long> getRuleIDs() {

    List<Long> ruleIDs = new ArrayList<>();

    if (null != this.iDRules && !this.iDRules.isEmpty()) {

      this.iDRules
          .stream()
          .forEach(
              idRule -> {
                ruleIDs.add(idRule.getRuleID());
              });
    }

    return ruleIDs;
  }

  public Organization getOrganization() {
    return organization;
  }

  public void setOrganization(Organization organization) {
    this.organization = organization;
  }

  public Boolean getPublicIDRuleSet() {
    return publicIDRuleSet;
  }

  public void setPublicIDRuleSet(Boolean publicIDRuleSet) {
    this.publicIDRuleSet = publicIDRuleSet;
  }

  public Boolean hasEditAllowance(User user) {
    if (user.isAdmin() || getUser().equals(user)
      || (user.isOrganizationAdmin() && user.getOrganization().equals(getUser().getOrganization())))
    {
      return true;
    }
    return false;
  }

  public Boolean hasDeleteAllowance(User user) {
    if (user.isAdmin() || getUser().equals(user)
      || (user.isOrganizationAdmin() && user.getOrganization().equals(getUser().getOrganization())))
    {
      return true;
    }
    return false;
  }
}
