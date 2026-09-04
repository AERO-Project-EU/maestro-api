package eu.orchestrator.repository.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "slice_placement")
public class SlicePlacement implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "slice", nullable = true)
  private Slice slice;

  //    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "provider", nullable = true)
  private Provider provider;

  //    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "component_node_instance", nullable = true)
  private ComponentNodeInstance componentNodeInstance;

  @Column(nullable = true, name = "flavor_id")
  private String flavorID;

  @Column(nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP", name = "date_created")
  @Temporal(TemporalType.TIMESTAMP)
  private Date dateCreated;

  @Column(nullable = false, name = "last_modified")
  private Date lastModified;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "slicePlacement")
  private List<SlicePlacementAttachmentPoint> attachmentPoints;

  public SlicePlacement() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Slice getSlice() {
    return slice;
  }

  public void setSlice(Slice slice) {
    this.slice = slice;
  }

  public Provider getProvider() {
    return provider;
  }

  public void setProvider(Provider provider) {
    this.provider = provider;
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

  public ComponentNodeInstance getComponentNodeInstance() {
    return componentNodeInstance;
  }

  public void setComponentNodeInstance(ComponentNodeInstance componentNodeInstance) {
    this.componentNodeInstance = componentNodeInstance;
  }

  public List<SlicePlacementAttachmentPoint> getAttachmentPoints() {
    return attachmentPoints;
  }

  public void setAttachmentPoints(List<SlicePlacementAttachmentPoint> attachmentPoints) {
    this.attachmentPoints = attachmentPoints;
  }

  public String getFlavorID() {
    return flavorID;
  }

  public void setFlavorID(String flavorID) {
    this.flavorID = flavorID;
  }

  @Override
  public String toString() {
    return "SlicePlacement{" +
            "id=" + id +
            ", dateCreated=" + dateCreated +
            '}';
  }
}
