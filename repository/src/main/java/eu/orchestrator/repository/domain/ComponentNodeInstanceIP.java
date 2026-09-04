package eu.orchestrator.repository.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "component_node_instance_ip")
public class ComponentNodeInstanceIP implements Serializable,
    Comparable<ComponentNodeInstanceIP> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long componentNodeInstanceIPID;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "component_node_instance", nullable = true)
  private ComponentNodeInstance componentNodeInstance;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "application_instance", nullable = true)
  private ApplicationInstance applicationInstance;

  @Column(nullable = true, name = "ip")
  private String ip;

  @Column(nullable = true, name = "type")
  private String type;

  @Column(nullable = true, name = "network")
  private String network;

  @Column(nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP", name = "date_created")
  @Temporal(TemporalType.TIMESTAMP)
  private Date dateCreated;

  @Column(nullable = false, name = "last_modified")
  private Date lastModified;

  @Transient
  private String componentNodeInstanceName;

  @Transient
  private String graphInstanceID;

  public ComponentNodeInstanceIP() {
  }

  public Long getComponentNodeInstanceIPID() {
    return componentNodeInstanceIPID;
  }

  public void setComponentNodeInstanceIPID(Long componentNodeInstanceIPID) {
    this.componentNodeInstanceIPID = componentNodeInstanceIPID;
  }

  public ComponentNodeInstance getComponentNodeInstance() {
    return componentNodeInstance;
  }

  public void setComponentNodeInstance(ComponentNodeInstance componentNodeInstance) {
    this.componentNodeInstance = componentNodeInstance;
  }

  public ApplicationInstance getApplicationInstance() {
    return applicationInstance;
  }

  public void setApplicationInstance(ApplicationInstance applicationInstance) {
    this.applicationInstance = applicationInstance;
  }

  public String getIp() {
    return ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getNetwork() {
    return network;
  }

  public void setNetwork(String network) {
    this.network = network;
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

  public String getGraphInstanceID() {
    return graphInstanceID;
  }

  public void setGraphInstanceID(String graphInstanceID) {
    this.graphInstanceID = graphInstanceID;
  }

  @Override
  public int compareTo(ComponentNodeInstanceIP componentNodeInstanceIP) {
    String compareQuantity = componentNodeInstanceIP.getNetwork();
    return this.network.compareTo(compareQuantity);
  }
}
