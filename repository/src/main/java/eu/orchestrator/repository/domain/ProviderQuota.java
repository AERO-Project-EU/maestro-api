package eu.orchestrator.repository.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "provider_quota")
public class ProviderQuota implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @OneToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "provider", nullable = true)
  private Provider provider;

  @Column(nullable = true, name = "running_instances")
  private Integer runningInstances;

  @Column(nullable = true, name = "max_instances")
  private Integer maxInstances;

  @Column(nullable = true, name = "instances_utilization")
  private Double instancesUtilization;

  @Column(nullable = true, name = "used_virtual_cpus")
  private Integer usedVirtualCPUs;

  @Column(nullable = true, name = "max_virtual_cpus")
  private Integer maxVirtualCPUs;

  @Column(nullable = true, name = "vcpu_utilization")
  private Double virtualCPUsUtilization;

  @Column(nullable = true, name = "used_memory")
  private Integer usedMemory;

  @Column(nullable = true, name = "max_memory")
  private Integer maxMemory;

  @Column(nullable = true, name = "memory_utilization")
  private Double memoryUtilization;

  @Column(nullable = true, name = "used_floating_ips")
  private Integer usedFloatingIPs;

  @Column(nullable = true, name = "claimed_floating_ips")
  private Integer claimedFloatingIPs;

  @Column(nullable = true, name = "floating_ips_consumption")
  private Double floatingIPsConsumption;

  @Column(nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP", name = "date_created")
  @Temporal(TemporalType.TIMESTAMP)
  private Date dateCreated;

  @Column(nullable = false, name = "last_modified")
  private Date lastModified;

  public ProviderQuota() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Provider getProvider() {
    return provider;
  }

  public void setProvider(Provider provider) {
    this.provider = provider;
  }

  public Integer getRunningInstances() {
    return runningInstances;
  }

  public void setRunningInstances(Integer runningInstances) {
    this.runningInstances = runningInstances;
  }

  public Integer getMaxInstances() {
    return maxInstances;
  }

  public void setMaxInstances(Integer maxInstances) {
    this.maxInstances = maxInstances;
  }

  public Double getInstancesUtilization() {
    return instancesUtilization;
  }

  public void setInstancesUtilization(Double instancesUtilization) {
    this.instancesUtilization = instancesUtilization;
  }

  public Integer getUsedVirtualCPUs() {
    return usedVirtualCPUs;
  }

  public void setUsedVirtualCPUs(Integer usedVirtualCPUs) {
    this.usedVirtualCPUs = usedVirtualCPUs;
  }

  public Integer getMaxVirtualCPUs() {
    return maxVirtualCPUs;
  }

  public void setMaxVirtualCPUs(Integer maxVirtualCPUs) {
    this.maxVirtualCPUs = maxVirtualCPUs;
  }

  public Double getVirtualCPUsUtilization() {
    return virtualCPUsUtilization;
  }

  public void setVirtualCPUsUtilization(Double virtualCPUsUtilization) {
    this.virtualCPUsUtilization = virtualCPUsUtilization;
  }

  public Integer getUsedMemory() {
    return usedMemory;
  }

  public void setUsedMemory(Integer usedMemory) {
    this.usedMemory = usedMemory;
  }

  public Integer getMaxMemory() {
    return maxMemory;
  }

  public void setMaxMemory(Integer maxMemory) {
    this.maxMemory = maxMemory;
  }

  public Double getMemoryUtilization() {
    return memoryUtilization;
  }

  public void setMemoryUtilization(Double memoryUtilization) {
    this.memoryUtilization = memoryUtilization;
  }

  public Integer getUsedFloatingIPs() {
    return usedFloatingIPs;
  }

  public void setUsedFloatingIPs(Integer usedFloatingIPs) {
    this.usedFloatingIPs = usedFloatingIPs;
  }

  public Integer getClaimedFloatingIPs() {
    return claimedFloatingIPs;
  }

  public void setClaimedFloatingIPs(Integer claimedFloatingIPs) {
    this.claimedFloatingIPs = claimedFloatingIPs;
  }

  public Double getFloatingIPsConsumption() {
    return floatingIPsConsumption;
  }

  public void setFloatingIPsConsumption(Double floatingIPsConsumption) {
    this.floatingIPsConsumption = floatingIPsConsumption;
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
