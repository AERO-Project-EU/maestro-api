package eu.orchestrator.backend.transfer;

import java.io.Serializable;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 24/4/2019
 */
public class DashboardOverviewTO implements Serializable {

    private Long usedInstances;
    private Long totalInstances;
    private Long applications;
    private Long components;
    private Long totalCPUs;
    private Long totalRam;

    public Long getUsedInstances() {
        return usedInstances;
    }

    public void setUsedInstances(Long usedInstances) {
        this.usedInstances = usedInstances;
    }

    public Long getTotalInstances() {
        return totalInstances;
    }

    public void setTotalInstances(Long totalInstances) {
        this.totalInstances = totalInstances;
    }

    public Long getApplications() {
        return applications;
    }

    public void setApplications(Long applications) {
        this.applications = applications;
    }

    public Long getComponents() {
        return components;
    }

    public void setComponents(Long components) {
        this.components = components;
    }

    public Long getTotalCPUs() {
        return totalCPUs;
    }

    public void setTotalCPUs(Long totalCPUs) {
        this.totalCPUs = totalCPUs;
    }

    public Long getTotalRam() {
        return totalRam;
    }

    public void setTotalRam(Long totalRam) {
        this.totalRam = totalRam;
    }
}
