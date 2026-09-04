package eu.orchestrator.repository.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "health_check")
public class HealthCheck implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long healthCheckID;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @OneToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "component", nullable = true)
  private Component component;

  @Column(nullable = true, name = "name")
  private String name;

  @Column(nullable = true, name = "http_url")
  private String httpURL;

  @Column(nullable = true, name = "args")
  private String args;

  @Column(nullable = true, name = "interval_time")
  private Long interval;

  @Column(nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP", name = "date_created")
  @Temporal(TemporalType.TIMESTAMP)
  private Date dateCreated;

  @Column(nullable = false, name = "last_modified")
  private Date lastModified;

  public HealthCheck() {
  }

  public Long getHealthCheckID() {
    return healthCheckID;
  }

  public void setHealthCheckID(Long healthCheckID) {
    this.healthCheckID = healthCheckID;
  }

  public Component getComponent() {
    return component;
  }

  public void setComponent(Component component) {
    this.component = component;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getHttpURL() {
    return httpURL;
  }

  public void setHttpURL(String httpURL) {
    this.httpURL = httpURL;
  }

  public String getArgs() {
    return args;
  }

  public void setArgs(String args) {
    this.args = args;
  }

  public Long getInterval() {
    return interval;
  }

  public void setInterval(Long interval) {
    this.interval = interval;
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
}
