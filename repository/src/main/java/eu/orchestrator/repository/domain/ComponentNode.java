package eu.orchestrator.repository.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "component_node")
public class ComponentNode implements Serializable, Comparable<ComponentNode> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long componentNodeID;

  // This node ID is created on the frontend side to cover the Graph Editor's business logic needs.
  // graphNodeID is filled through the Application-ComponentNodes REST API
  @Column(nullable = true)
  private String graphNodeID;

  @Column(nullable = true, name = "hex_id", unique = true)
  private String hexID;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "application", nullable = true)
  private Application application;

  @Column(nullable = true)
  private String name;

  //    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "component", nullable = true)
  private Component component;

  @Column(nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP", name = "date_created")
  @Temporal(TemporalType.TIMESTAMP)
  private Date dateCreated;

  @Column(nullable = false, name = "last_modified")
  private Date lastModified;

  @Transient
  private String componentName;

  public ComponentNode() {
  }

  public Long getComponentNodeID() {
    return componentNodeID;
  }

  public void setComponentNodeID(Long componentNodeID) {
    this.componentNodeID = componentNodeID;
  }

  public String getGraphNodeID() {
    return graphNodeID;
  }

  public void setGraphNodeID(String graphNodeID) {
    this.graphNodeID = graphNodeID;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Component getComponent() {
    return component;
  }

  public void setComponent(Component component) {
    this.component = component;
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

  public Application getApplication() {
    return application;
  }

  public void setApplication(Application application) {
    this.application = application;
  }

  @Override
  public int compareTo(ComponentNode compareLabel) {
    String compareQuantity = compareLabel.getName();

    return this.name.compareTo(compareQuantity);
  }

  public String getHexID() {
    return hexID;
  }

  public void setHexID(String hexID) {
    this.hexID = hexID;
  }

  public String getComponentName() {
    return componentName;
  }

  public void setComponentName(String componentName) {
    this.componentName = componentName;
  }
}
