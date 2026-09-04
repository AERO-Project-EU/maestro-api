package eu.orchestrator.repository.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "component_node_instance_status")
public class ComponentNodeInstanceStatus implements Serializable,
    Comparable<ComponentNodeInstanceStatus> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long componentNodeInstanceStatusID;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "component_node_instance", nullable = true)
  private ComponentNodeInstance componentNodeInstance;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "application_instance", nullable = true)
  private ApplicationInstance applicationInstance;

  @Column(nullable = true, name = "reported_change")
  private String reportedChange;

  @Column(nullable = true, name = "message")
  private String message;

  @Column(nullable = true, name = "status")
  private String status;

  @Column(nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP", name = "date_created")
  @Temporal(TemporalType.TIMESTAMP)
  private Date dateCreated;

  @Column(nullable = false, name = "last_modified")
  private Date lastModified;

  @Transient
  private String componentNodeInstanceName;

  public ComponentNodeInstanceStatus() {
  }

  public Long getComponentNodeInstanceStatusID() {
    return componentNodeInstanceStatusID;
  }

  public void setComponentNodeInstanceStatusID(Long componentNodeInstanceStatusID) {
    this.componentNodeInstanceStatusID = componentNodeInstanceStatusID;
  }

  public ComponentNodeInstance getComponentNodeInstance() {
    return componentNodeInstance;
  }

  public void setComponentNodeInstance(ComponentNodeInstance componentNodeInstance) {
    this.componentNodeInstance = componentNodeInstance;
  }

  public String getReportedChange() {
    return reportedChange;
  }

  public void setReportedChange(String reportedChange) {
    this.reportedChange = reportedChange;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
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

  public String getComponentNodeInstanceName() {
    return componentNodeInstanceName;
  }

  public void setComponentNodeInstanceName(String componentNodeInstanceName) {
    this.componentNodeInstanceName = componentNodeInstanceName;
  }

  public ApplicationInstance getApplicationInstance() {
    return applicationInstance;
  }

  public void setApplicationInstance(ApplicationInstance applicationInstance) {
    this.applicationInstance = applicationInstance;
  }

  @Override
  public int compareTo(ComponentNodeInstanceStatus componentNodeInstanceStatus) {
    Date compareQuantity = componentNodeInstanceStatus.getDateCreated();

    return this.dateCreated.compareTo(compareQuantity);
  }
}
