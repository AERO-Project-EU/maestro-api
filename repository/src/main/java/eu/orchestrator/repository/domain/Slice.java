package eu.orchestrator.repository.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "slice")
public class Slice implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = true, name = "hex_id")
  private String hexID;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "application_instance", nullable = true)
  private ApplicationInstance applicationInstance;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "user", nullable = true)
  private User user;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "organization", nullable = true)
  private Organization organization;

  @Lob
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @Column(nullable = true, name = "orchestrator_application_instance")
  private String orchestratorApplicationInstance;

  @Column(nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP", name = "date_created")
  @Temporal(TemporalType.TIMESTAMP)
  private Date dateCreated;

  @Column(nullable = false, name = "last_modified")
  private Date lastModified;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "slice")
  private List<SliceProvider> providers;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "slice")
  private List<SlicePlacement> placements;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "slice")
  private List<SliceConstraintSatisfaction> constraintSatisfactions;

  public Slice() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
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

  public Date getLastModified() {
    return lastModified;
  }

  public void setLastModified(Date lastModified) {
    this.lastModified = lastModified;
  }

  public Date getDateCreated() {
    return dateCreated;
  }

  public void setDateCreated(Date dateCreated) {
    this.dateCreated = dateCreated;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public String getOrchestratorApplicationInstance() {
    return orchestratorApplicationInstance;
  }

  public void setOrchestratorApplicationInstance(String orchestratorApplicationInstance) {
    this.orchestratorApplicationInstance = orchestratorApplicationInstance;
  }

  public List<SliceProvider> getProviders() {
    return providers;
  }

  public void setProviders(List<SliceProvider> providers) {
    this.providers = providers;
  }

  public List<SlicePlacement> getPlacements() {
    return placements;
  }

  public void setPlacements(List<SlicePlacement> placements) {
    this.placements = placements;
  }

  public List<SliceConstraintSatisfaction> getConstraintSatisfactions() {
    return constraintSatisfactions;
  }

  public void setConstraintSatisfactions(
      List<SliceConstraintSatisfaction> constraintSatisfactions) {
    this.constraintSatisfactions = constraintSatisfactions;
  }

  public Organization getOrganization() {
    return organization;
  }

  public void setOrganization(Organization organization) {
    this.organization = organization;
  }

  @Override
  public String toString() {
    return "Slice{" +
            "id=" + id +
            ", applicationInstance=" + applicationInstance +
            ", dateCreated=" + dateCreated +
            '}';
  }
}
