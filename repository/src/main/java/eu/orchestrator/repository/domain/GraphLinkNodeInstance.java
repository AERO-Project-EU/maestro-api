package eu.orchestrator.repository.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "graph_link_node_instance")
public class GraphLinkNodeInstance implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long graphLinkNodeInstanceID;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "application_instance", nullable = true)
  private ApplicationInstance applicationInstance;

  //    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "component_node_instance_from", nullable = true)
  private ComponentNodeInstance componentNodeInstanceFrom;

  //    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "component_node_instance_to", nullable = true)
  private ComponentNodeInstance componentNodeInstanceTo;

  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "graph_link_node", nullable = true)
  private GraphLinkNode graphLinkNode;

  @Column(nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP", name = "date_created")
  @Temporal(TemporalType.TIMESTAMP)
  private Date dateCreated;

  @Column(nullable = false, name = "last_modified")
  private Date lastModified;

  public GraphLinkNodeInstance() {
  }

  public Long getGraphLinkNodeInstanceID() {
    return graphLinkNodeInstanceID;
  }

  public void setGraphLinkNodeInstanceID(Long graphLinkNodeInstanceID) {
    this.graphLinkNodeInstanceID = graphLinkNodeInstanceID;
  }

  public ApplicationInstance getApplicationInstance() {
    return applicationInstance;
  }

  public void setApplicationInstance(ApplicationInstance applicationInstance) {
    this.applicationInstance = applicationInstance;
  }

  public ComponentNodeInstance getComponentNodeInstanceFrom() {
    return componentNodeInstanceFrom;
  }

  public void setComponentNodeInstanceFrom(ComponentNodeInstance componentNodeInstanceFrom) {
    this.componentNodeInstanceFrom = componentNodeInstanceFrom;
  }

  public ComponentNodeInstance getComponentNodeInstanceTo() {
    return componentNodeInstanceTo;
  }

  public void setComponentNodeInstanceTo(ComponentNodeInstance componentNodeInstanceTo) {
    this.componentNodeInstanceTo = componentNodeInstanceTo;
  }

  public GraphLinkNode getGraphLinkNode() {
    return graphLinkNode;
  }

  public void setGraphLinkNode(GraphLinkNode graphLinkNode) {
    this.graphLinkNode = graphLinkNode;
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
