package eu.orchestrator.backend.transfer;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 18/4/2019
 */
public class DashboardProviderTO {

    private String name;
    private String type;
    private Boolean enabled;
    private Integer usedCPUs;
    private Integer usedRam;
    private Integer totalCPUs;
    private Integer totalRam;
    private Integer usedInstances;
    private Integer totalInstances;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getUsedCPUs() {
        return usedCPUs;
    }

    public void setUsedCPUs(Integer usedCPUs) {
        this.usedCPUs = usedCPUs;
    }

    public Integer getUsedRam() {
        return usedRam;
    }

    public void setUsedRam(Integer usedRam) {
        this.usedRam = usedRam;
    }

    public Integer getTotalCPUs() {
        return totalCPUs;
    }

    public void setTotalCPUs(Integer totalCPUs) {
        this.totalCPUs = totalCPUs;
    }

    public Integer getTotalRam() {
        return totalRam;
    }

    public void setTotalRam(Integer totalRam) {
        this.totalRam = totalRam;
    }

    public Integer getUsedInstances() {
        return usedInstances;
    }

    public void setUsedInstances(Integer usedInstances) {
        this.usedInstances = usedInstances;
    }

    public Integer getTotalInstances() {
        return totalInstances;
    }

    public void setTotalInstances(Integer totalInstances) {
        this.totalInstances = totalInstances;
    }
}
