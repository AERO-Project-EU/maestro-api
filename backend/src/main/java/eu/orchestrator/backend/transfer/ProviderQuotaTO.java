package eu.orchestrator.backend.transfer;

import java.io.Serializable;
import java.util.Date;

public class ProviderQuotaTO implements Serializable {

    private Long id;
    private Integer runningInstances;
    private Integer maxInstances;
    private Double instancesUtilization;
    private Integer usedVirtualCPUs;
    private Integer maxVirtualCPUs;
    private Double virtualCPUsUtilization;
    private Integer usedMemory;
    private Integer maxMemory;
    private Double memoryUtilization;
    private Integer usedFloatingIPs;
    private Integer claimedFloatingIPs;
    private Double floatingIPsConsumption;
    private Date dateCreated;
    private Date lastModified;

    public ProviderQuotaTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
