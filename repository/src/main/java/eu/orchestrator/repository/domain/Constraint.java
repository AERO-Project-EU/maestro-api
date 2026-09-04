package eu.orchestrator.repository.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Convert;
import org.hibernate.type.NumericBooleanConverter;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "application_instance_constraint")
public class Constraint implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long constraintID;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "application_instance", nullable = true)
  private ApplicationInstance applicationInstance;

  @Column(nullable = true, name = "constraint_category")
  private String constraintCategory;

  @Column(nullable = true, name = "constraint_type")
  private String constraintType;

  @Column(nullable = true, name = "constraint_metric")
  private String constraintMetric;

  @Column(nullable = true, name = "constraint_unit")
  private String constraintUnit;

  @Column(nullable = true, name = "constraint_value")
  private String constraintValue;

  // Access Constraints
  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "radio_service_type", nullable = true)
  private RadioServiceType radioServiceType;

  @Column(nullable = true, name = "resource_type")
  private String resourceType;

  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "country", nullable = true)
  private Country country;

  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "qi", nullable = true)
  private QI qi;

  @Column(nullable = true, name = "allocation_retention_priority_profile")
  private Integer allocationRetentionPriorityProfile;

  @Column(nullable = true, name = "minimum_guaranteed_bandwidth")
  private Double minimumGuaranteedBandwidth;

  @Column(nullable = true, name = "maximum_required_bandwidth")
  private Double maximumRequiredBandwidth;

  // Relations
  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "component_node_instance", nullable = true)
  private ComponentNodeInstance componentNodeInstance;

  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "graph_link_node_instance", nullable = true)
  private GraphLinkNodeInstance graphLinkNodeInstance;

  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "interface_instance", nullable = true)
  private InterfaceInstance interfaceInstance;

  @Column(nullable = true, name = "is_deletable", columnDefinition = "TINYINT(1) UNSIGNED default '1'")
  @Convert(converter = NumericBooleanConverter.class)
  private Boolean deletableConstraint = true;

  @Column(nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP", name = "date_created")
  @Temporal(TemporalType.TIMESTAMP)
  private Date dateCreated;

  @Column(nullable = false, name = "last_modified")
  private Date lastModified;

  public Constraint() {
  }

  public enum ConstraintCategory {

    COMPONENT_HOSTING("Component Hosting Constraint"),
    GRAPH_LINK("Graph Link Constraint"),
    ACCESS("Access Constraint");

    private String friendlyName;

    ConstraintCategory(String friendlyName) {
      this.friendlyName = friendlyName;
    }

    public String getFriendlyName() {
      return friendlyName;
    }
  }

  public enum ResourceType {

    DELAY_CRITICAL_GBR("Delay Critical GBR"),
    GBR("GBR"),
    NON_GBR("Non GBR");

    private String friendlyName;

    ResourceType(String friendlyName) {
      this.friendlyName = friendlyName;
    }

    public String getFriendlyName() {
      return friendlyName;
    }
  }

  public enum ConstraintType {

    HARD("Hard"),
    SOFT("Soft");

    private String friendlyName;

    ConstraintType(String friendlyName) {
      this.friendlyName = friendlyName;
    }

    public String getFriendlyName() {
      return friendlyName;
    }
  }

  public enum ConstraintMetric {

    REGION("Region", ConstraintCategory.COMPONENT_HOSTING.name(), ""),
    MIN_WORKERS("Minimum Workers", ConstraintCategory.COMPONENT_HOSTING.name(), ""),
    MAX_WORKERS("Maximum Workers", ConstraintCategory.COMPONENT_HOSTING.name(), ""),
    DELAY("Delay", ConstraintCategory.GRAPH_LINK.name(),
        "The element represents the maximum Delay that can be tolerated"),
    JITTER("Jitter", ConstraintCategory.GRAPH_LINK.name(),
        "The element represents the maximum Jitter that can be tolerated"),
    PACKET_LOSS("Packet loss", ConstraintCategory.GRAPH_LINK.name(),
        "The element represents the maximum Packet Loss that can be tolerated"),
    THROUGHPUT("Throughput", ConstraintCategory.GRAPH_LINK.name(),
        "The element represents the minimum guaranteed Throughput"),
    MIN_V_CPU("Min vCPUs", ConstraintCategory.COMPONENT_HOSTING.name(), ""),
    MAX_V_CPU("Max vCPUs", ConstraintCategory.COMPONENT_HOSTING.name(), ""),
    MIN_STORAGE("Min Storage", ConstraintCategory.COMPONENT_HOSTING.name(), ""),
    MAX_STORAGE("Max Storage", ConstraintCategory.COMPONENT_HOSTING.name(), ""),
    MIN_RAM("Min RAM", ConstraintCategory.COMPONENT_HOSTING.name(), ""),
    MAX_RAM("Max RAM", ConstraintCategory.COMPONENT_HOSTING.name(), "");

    private String friendlyName;
    private String constraintCategory;
    private String description;

    ConstraintMetric(String friendlyName, String constraintCategory, String description) {
      this.friendlyName = friendlyName;
      this.constraintCategory = constraintCategory;
      this.description = description;
    }

    public String getFriendlyName() {
      return friendlyName;
    }

    public String getConstraintCategory() {
      return constraintCategory;
    }

    public String getDescription() {
      return description;
    }
  }

  public Long getConstraintID() {
    return constraintID;
  }

  public void setConstraintID(Long constraintID) {
    this.constraintID = constraintID;
  }

  public ApplicationInstance getApplicationInstance() {
    return applicationInstance;
  }

  public void setApplicationInstance(ApplicationInstance applicationInstance) {
    this.applicationInstance = applicationInstance;
  }

  public ComponentNodeInstance getComponentNodeInstance() {
    return componentNodeInstance;
  }

  public void setComponentNodeInstance(ComponentNodeInstance componentNodeInstance) {
    this.componentNodeInstance = componentNodeInstance;
  }

  public String getConstraintCategory() {
    return constraintCategory;
  }

  public void setConstraintCategory(String constraintCategory) {
    this.constraintCategory = constraintCategory;
  }

  public String getConstraintType() {
    return constraintType;
  }

  public void setConstraintType(String constraintType) {
    this.constraintType = constraintType;
  }

  public String getConstraintMetric() {
    return constraintMetric;
  }

  public void setConstraintMetric(String constraintMetric) {
    this.constraintMetric = constraintMetric;
  }

  public String getConstraintValue() {
    return constraintValue;
  }

  public void setConstraintValue(String constraintValue) {
    this.constraintValue = constraintValue;
  }

  public Country getCountry() {
    return country;
  }

  public void setCountry(Country country) {
    this.country = country;
  }

  public QI getQi() {
    return qi;
  }

  public void setQi(QI qi) {
    this.qi = qi;
  }

  public RadioServiceType getRadioServiceType() {
    return radioServiceType;
  }

  public void setRadioServiceType(RadioServiceType radioServiceType) {
    this.radioServiceType = radioServiceType;
  }

  public String getResourceType() {
    return resourceType;
  }

  public void setResourceType(String resourceType) {
    this.resourceType = resourceType;
  }

  public Integer getAllocationRetentionPriorityProfile() {
    return allocationRetentionPriorityProfile;
  }

  public void setAllocationRetentionPriorityProfile(Integer allocationRetentionPriorityProfile) {
    this.allocationRetentionPriorityProfile = allocationRetentionPriorityProfile;
  }

  public Double getMinimumGuaranteedBandwidth() {
    return minimumGuaranteedBandwidth;
  }

  public void setMinimumGuaranteedBandwidth(Double minimumGuaranteedBandwidth) {
    this.minimumGuaranteedBandwidth = minimumGuaranteedBandwidth;
  }

  public Double getMaximumRequiredBandwidth() {
    return maximumRequiredBandwidth;
  }

  public void setMaximumRequiredBandwidth(Double maximumRequiredBandwidth) {
    this.maximumRequiredBandwidth = maximumRequiredBandwidth;
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

  public Boolean getDeletableConstraint() {
    return deletableConstraint;
  }

  public void setDeletableConstraint(Boolean deletableConstraint) {
    this.deletableConstraint = deletableConstraint;
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

  public String getConstraintUnit() {
    return constraintUnit;
  }

  public void setConstraintUnit(String constraintUnit) {
    this.constraintUnit = constraintUnit;
  }

  @Override
  public String toString() {
    return "Constraint{" +
            "constraintID=" + constraintID +
            ", constraintCategory='" + constraintCategory + '\'' +
            ", constraintType='" + constraintType + '\'' +
            ", constraintMetric='" + constraintMetric + '\'' +
            ", constraintUnit='" + constraintUnit + '\'' +
            ", constraintValue='" + constraintValue + '\'' +
            '}';
  }
}
