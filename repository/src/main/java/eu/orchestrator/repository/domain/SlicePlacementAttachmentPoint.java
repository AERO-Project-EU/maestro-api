package eu.orchestrator.repository.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "slice_placement_attachment_point")
public class SlicePlacementAttachmentPoint implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "slice_placement", nullable = true)
  private SlicePlacement slicePlacement;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "graph_link_node_instance", nullable = true)
  private GraphLinkNodeInstance graphLinkNodeInstance;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "interface_instance", nullable = true)
  private InterfaceInstance interfaceInstance;

  @Column(nullable = true, name = "attachment_point")
  private String attachmentPoint;

  @Column(nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP", name = "date_created")
  @Temporal(TemporalType.TIMESTAMP)
  private Date dateCreated;

  @Column(nullable = false, name = "last_modified")
  private Date lastModified;

  public SlicePlacementAttachmentPoint() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public SlicePlacement getSlicePlacement() {
    return slicePlacement;
  }

  public void setSlicePlacement(SlicePlacement slicePlacement) {
    this.slicePlacement = slicePlacement;
  }

  public GraphLinkNodeInstance getGraphLinkNodeInstance() {
    return graphLinkNodeInstance;
  }

  public void setGraphLinkNodeInstance(GraphLinkNodeInstance graphLinkNodeInstance) {
    this.graphLinkNodeInstance = graphLinkNodeInstance;
  }

  public InterfaceInstance getInterfaceInstance() {
    return interfaceInstance;
  }

  public void setInterfaceInstance(InterfaceInstance interfaceInstance) {
    this.interfaceInstance = interfaceInstance;
  }

  public String getAttachmentPoint() {
    return attachmentPoint;
  }

  public void setAttachmentPoint(String attachmentPoint) {
    this.attachmentPoint = attachmentPoint;
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

  @Override
  public String toString() {
    return "SlicePlacementAttachmentPoint{" +
            "id=" + id +
            ", attachmentPoint='" + attachmentPoint + '\'' +
            '}';
  }
}
