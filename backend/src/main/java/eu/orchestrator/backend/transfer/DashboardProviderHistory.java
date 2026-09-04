package eu.orchestrator.backend.transfer;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 24/4/2019
 */
public class DashboardProviderHistory {

    private Long vCPUs;
    private Long ram;
    private Long timestamp;

    public Long getvCPUs() {
        return vCPUs;
    }

    public void setvCPUs(Long vCPUs) {
        this.vCPUs = vCPUs;
    }

    public Long getRam() {
        return ram;
    }

    public void setRam(Long ram) {
        this.ram = ram;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
