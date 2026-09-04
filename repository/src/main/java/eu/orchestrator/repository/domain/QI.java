package eu.orchestrator.repository.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "qi")
public class QI implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, name = "qi_value")
  private String qiValue;

  @Column(nullable = false, name = "resource_type")
  private String resourceType;

  @Column(nullable = true, name = "default_priority_level")
  private Integer defaultPriorityLevel;

  @Column(nullable = true, name = "packet_delay_budget")
  private Double packetDelayBudget;

  @Column(nullable = true, name = "packet_error_rate")
  private Double packetErrorRate;

  @Column(nullable = true, name = "default_maximum_data_burst_volume")
  private String defaultMaximumDataBurstVolume;

  @Column(nullable = true, name = "default_averaging_window")
  private String defaultAveragingWindow;

  @Column(nullable = false, name = "example_services")
  private String services;

  @Column(nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP", name = "date_created")
  @Temporal(TemporalType.TIMESTAMP)
  private Date dateCreated;

  public QI() {
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

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getQiValue() {
    return qiValue;
  }

  public void setQiValue(String qiValue) {
    this.qiValue = qiValue;
  }

  public String getResourceType() {
    return resourceType;
  }

  public void setResourceType(String resourceType) {
    this.resourceType = resourceType;
  }

  public Integer getDefaultPriorityLevel() {
    return defaultPriorityLevel;
  }

  public void setDefaultPriorityLevel(Integer defaultPriorityLevel) {
    this.defaultPriorityLevel = defaultPriorityLevel;
  }

  public Double getPacketDelayBudget() {
    return packetDelayBudget;
  }

  public void setPacketDelayBudget(Double packetDelayBudget) {
    this.packetDelayBudget = packetDelayBudget;
  }

  public Double getPacketErrorRate() {
    return packetErrorRate;
  }

  public void setPacketErrorRate(Double packetErrorRate) {
    this.packetErrorRate = packetErrorRate;
  }

  public String getDefaultMaximumDataBurstVolume() {
    return defaultMaximumDataBurstVolume;
  }

  public void setDefaultMaximumDataBurstVolume(String defaultMaximumDataBurstVolume) {
    this.defaultMaximumDataBurstVolume = defaultMaximumDataBurstVolume;
  }

  public String getDefaultAveragingWindow() {
    return defaultAveragingWindow;
  }

  public void setDefaultAveragingWindow(String defaultAveragingWindow) {
    this.defaultAveragingWindow = defaultAveragingWindow;
  }

  public String getServices() {
    return services;
  }

  public void setServices(String services) {
    this.services = services;
  }

  public Date getDateCreated() {
    return dateCreated;
  }

  public void setDateCreated(Date dateCreated) {
    this.dateCreated = dateCreated;
  }
}
