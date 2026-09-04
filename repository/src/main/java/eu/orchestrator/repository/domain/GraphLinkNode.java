package eu.orchestrator.repository.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "graph_link_node")
public class GraphLinkNode implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long graphLinkNodeID;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "application", nullable = true)
  private Application application;

  //    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "component_node_from", nullable = true)
  private ComponentNode componentNodeFrom;

  //    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "component_node_to", nullable = true)
  private ComponentNode componentNodeTo;

  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "graph_link", nullable = true)
  private GraphLink graphLink;

  @Column(nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP", name = "date_created")
  @Temporal(TemporalType.TIMESTAMP)
  private Date dateCreated;

  @Column(nullable = false, name = "last_modified")
  private Date lastModified;

  public GraphLinkNode() {
  }

  public Long getGraphLinkNodeID() {
    return graphLinkNodeID;
  }

  public void setGraphLinkNodeID(Long graphLinkNodeID) {
    this.graphLinkNodeID = graphLinkNodeID;
  }

  public Application getApplication() {
    return application;
  }

  public void setApplication(Application application) {
    this.application = application;
  }

  public ComponentNode getComponentNodeFrom() {
    return componentNodeFrom;
  }

  public void setComponentNodeFrom(ComponentNode componentNodeFrom) {
    this.componentNodeFrom = componentNodeFrom;
  }

  public ComponentNode getComponentNodeTo() {
    return componentNodeTo;
  }

  public void setComponentNodeTo(ComponentNode componentNodeTo) {
    this.componentNodeTo = componentNodeTo;
  }

  public GraphLink getGraphLink() {
    return graphLink;
  }

  public void setGraphLink(GraphLink graphLink) {
    this.graphLink = graphLink;
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
