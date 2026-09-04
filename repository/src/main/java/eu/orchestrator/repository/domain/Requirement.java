package eu.orchestrator.repository.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Convert;
import org.hibernate.type.NumericBooleanConverter;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "requirement")
public class Requirement implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long requirementID;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @OneToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "component", nullable = true)
  private Component component;

  @Column(nullable = true)
  private Integer vCPUs;

  @Column(nullable = true)
  private Integer ram;

  @Column(nullable = true)
  private Integer storage;

  @Column(nullable = true, name = "hypervisor_type")
  private String hypervisorType;

  @Column(nullable = true, name = "gpu_required", columnDefinition = "TINYINT(1) UNSIGNED default '0'")
  @Convert(converter = NumericBooleanConverter.class)
  private Boolean gpuRequired;

  @Column(nullable = true, name = "serverless_enabled", columnDefinition = "TINYINT(1) UNSIGNED default '0'")
  @Convert(converter = NumericBooleanConverter.class)
  private Boolean serverlessEnabled;

  @Column(nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP", name = "date_created")
  @Temporal(TemporalType.TIMESTAMP)
  private Date dateCreated;

  @Column(nullable = false, name = "last_modified")
  private Date lastModified;

  public Requirement() {
  }

  public enum HypervisorType {

    ESXI("ESXI"),
    KVM("KVM"),
    KEN("KEN");

    private String friendlyName;

    HypervisorType(String friendlyName) {
      this.friendlyName = friendlyName;
    }

    public String getFriendlyName() {
      return friendlyName;
    }
  }

  public Long getRequirementID() {
    return requirementID;
  }

  public void setRequirementID(Long requirementID) {
    this.requirementID = requirementID;
  }

  public Component getComponent() {
    return component;
  }

  public void setComponent(Component component) {
    this.component = component;
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

  public String getHypervisorType() {
    return hypervisorType;
  }

  public void setHypervisorType(String hypervisorType) {
    this.hypervisorType = hypervisorType;
  }

  public Boolean getGpuRequired() {
    return gpuRequired;
  }

  public void setGpuRequired(Boolean gpuRequired) {
    this.gpuRequired = gpuRequired;
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
