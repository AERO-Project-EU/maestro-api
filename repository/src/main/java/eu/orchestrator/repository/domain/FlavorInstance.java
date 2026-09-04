package eu.orchestrator.repository.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Convert;
import org.hibernate.type.NumericBooleanConverter;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "flavor_instance")
public class FlavorInstance implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long flavorID;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @OneToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "component_node_instance", nullable = true)
  private ComponentNodeInstance componentNodeInstance;

  @Column(nullable = true)
  private Integer vCPUs;

  @Column(nullable = true)
  private Integer ram;

  @Column(nullable = true)
  private Integer storage;

  @Column(nullable = true, name = "serverless_enabled", columnDefinition = "TINYINT(1) UNSIGNED default '0'")
  @Convert(converter = NumericBooleanConverter.class)
  private Boolean serverlessEnabled;

  @Column(nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP", name = "date_created")
  @Temporal(TemporalType.TIMESTAMP)
  private Date dateCreated;

  @Column(nullable = false, name = "last_modified")
  private Date lastModified;

  public FlavorInstance() {
  }

  public Long getFlavorID() {
    return flavorID;
  }

  public void setFlavorID(Long flavorID) {
    this.flavorID = flavorID;
  }

  public ComponentNodeInstance getComponentNodeInstance() {
    return componentNodeInstance;
  }

  public void setComponentNodeInstance(ComponentNodeInstance componentNodeInstance) {
    this.componentNodeInstance = componentNodeInstance;
  }

  public Integer getvCPUs() {
    return vCPUs;
  }

  public void setvCPUs(Integer vCPUs) {
    this.vCPUs = vCPUs;
  }

  public Integer getRam() {
    return ram;
  }

  public void setRam(Integer ram) {
    this.ram = ram;
  }

  public Integer getStorage() {
    return storage;
  }

  public void setStorage(Integer storage) {
    this.storage = storage;
  }

  public Boolean getServerlessEnabled() {
    return serverlessEnabled;
  }

  public void setServerlessEnabled(Boolean serverlessEnabled) {
    this.serverlessEnabled = serverlessEnabled;
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
