package eu.orchestrator.repository.domain;


import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@Table(name = "profile")
public class Profile implements Serializable {

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

  @Column(nullable = true)
  private String status;

  @Column(nullable = true)
  private String algorithm;

  @Column(nullable = true, name = "step")
  private Long step;

  @Column(nullable = true)
  private Date startTime;

  @Column(nullable = true)
  private Date endTime;

  @Column(nullable = true, name = "hex_id", unique = true)
  private String hexID;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "profile")
  private List<ProfileMetric> profileMetrics;

  @Column(nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP", name = "date_created")
  @Temporal(TemporalType.TIMESTAMP)
  private Date dateCreated;

  @Column(nullable = false, name = "last_modified")
  private Date lastModified;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "user", nullable = true)
  private User user;

  public enum ProfileStatus {

    PROCESS("Process"),
    COMPLETE("Complete"),
    ERROR_OCCURRED("Error Occurred");

    private String friendlyName;

    ProfileStatus(String friendlyName) {
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

  public ApplicationInstance getApplicationInstance() {
    return applicationInstance;
  }

  public void setApplicationInstance(
      ApplicationInstance applicationInstance) {
    this.applicationInstance = applicationInstance;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getAlgorithm() {
    return algorithm;
  }

  public void setAlgorithm(String algorithm) {
    this.algorithm = algorithm;
  }

  public Date getStartTime() {
    return startTime;
  }

  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  public Date getEndTime() {
    return endTime;
  }

  public void setEndTime(Date endTime) {
    this.endTime = endTime;
  }

  public String getHexID() {
    return hexID;
  }

  public void setHexID(String hexID) {
    this.hexID = hexID;
  }

  public List<ProfileMetric> getProfileMetrics() {
    return profileMetrics;
  }

  public void setProfileMetrics(
      List<ProfileMetric> profileMetrics) {
    this.profileMetrics = profileMetrics;
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

  public Long getStep() {
    return step;
  }

  public void setStep(Long step) {
    this.step = step;
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
