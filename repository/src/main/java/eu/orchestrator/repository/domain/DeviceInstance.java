package eu.orchestrator.repository.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "device_instance")
public class DeviceInstance implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long deviceInstanceID;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "component_node_instance", nullable = true)
  private ComponentNodeInstance componentNodeInstance;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "device", nullable = true)
  private Device device;

  @Column(nullable = true, name = "key_variable")
  private String key;

  @Column(nullable = true)
  private String value;

  @Column(nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP", name = "date_created")
  @Temporal(TemporalType.TIMESTAMP)
  private Date dateCreated;

  @Column(nullable = false, name = "last_modified")
  private Date lastModified;

  public DeviceInstance() {
  }

  public Long getDeviceInstanceID() {
    return deviceInstanceID;
  }

  public void setDeviceInstanceID(Long deviceInstanceID) {
    this.deviceInstanceID = deviceInstanceID;
  }

  public ComponentNodeInstance getComponentNodeInstance() {
    return componentNodeInstance;
  }

  public void setComponentNodeInstance(ComponentNodeInstance componentNodeInstance) {
    this.componentNodeInstance = componentNodeInstance;
  }

  public Device getDevice() {
    return device;
  }

  public void setDevice(Device device) {
    this.device = device;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
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

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }
}
