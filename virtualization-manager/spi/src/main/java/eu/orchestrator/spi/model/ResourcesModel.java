package eu.orchestrator.spi.model;

import java.io.Serializable;

/**
 * @author Konstantinos Theodosiou
 */
public class ResourcesModel implements Serializable {
    private String id;

    private Integer runningInstances;
    private Integer maxInstances;
    private Double instancesUtilization;

    private Integer usedVCpus;
    private Integer maxVCpus;
    private Double vCpuUtilization;

    private Integer usedRam;
    private Integer maxRam;
    private Double ramUtilization;

    private Integer usedFloatingIPs;
    private Integer claimedFloatingIPs;
    private Double floatingIPsUtilization;

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public Integer getUsedVCpus() {
        return usedVCpus;
    }

    public void setUsedVCpus(Integer usedVCpus) {
        this.usedVCpus = usedVCpus;
    }

    public Integer getMaxVCpus() {
        return maxVCpus;
    }

    public void setMaxVCpus(Integer maxVCpus) {
        this.maxVCpus = maxVCpus;
    }

    public Double getvCpuUtilization() {
        return vCpuUtilization;
    }

    public void setvCpuUtilization(Double vCpuUtilization) {
        this.vCpuUtilization = vCpuUtilization;
    }

    public Integer getUsedRam() {
        return usedRam;
    }

    public void setUsedRam(Integer usedRam) {
        this.usedRam = usedRam;
    }

    public Integer getMaxRam() {
        return maxRam;
    }

    public void setMaxRam(Integer maxRam) {
        this.maxRam = maxRam;
    }

    public Double getRamUtilization() {
        return ramUtilization;
    }

    public void setRamUtilization(Double ramUtilization) {
        this.ramUtilization = ramUtilization;
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

    public Double getFloatingIPsUtilization() {
        return floatingIPsUtilization;
    }

    public void setFloatingIPsUtilization(Double floatingIPsUtilization) {
        this.floatingIPsUtilization = floatingIPsUtilization;
    }
}
