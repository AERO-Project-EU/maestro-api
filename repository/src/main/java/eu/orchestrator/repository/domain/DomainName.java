package eu.orchestrator.repository.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@Table(name = "domain_name")
public class DomainName implements Serializable{

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long domainNameID;

  @Column(nullable = false, name = "domain")
  private String domain;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "component_node_instance_ip", nullable = true)
  private  ComponentNodeInstanceIP componentNodeInstanceIP;

  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "user", nullable = true)
  private User user;

  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "organization", nullable = true)
  private Organization organization;

  @Column(nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP", name = "date_created")
  @Temporal(TemporalType.TIMESTAMP)
  private Date dateCreated;

  @Column(nullable = false, name = "last_modified")
  private Date lastModified;

  public Long getDomainNameID() {
    return domainNameID;
  }

  public void setDomainNameID(Long domainNameID) {
    this.domainNameID = domainNameID;
  }

  public String getDomain() {
    return domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  public ComponentNodeInstanceIP getComponentNodeInstanceIP() {
    return componentNodeInstanceIP;
  }

  public void setComponentNodeInstanceIP(
      ComponentNodeInstanceIP componentNodeInstanceIP) {
    this.componentNodeInstanceIP = componentNodeInstanceIP;
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

  public Organization getOrganization() {
    return organization;
  }

  public void setOrganization(Organization organization) {
    this.organization = organization;
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
