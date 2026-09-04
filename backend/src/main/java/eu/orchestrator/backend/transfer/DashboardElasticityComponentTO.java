package eu.orchestrator.backend.transfer;

import java.util.Date;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 18/4/2019
 */
public class DashboardElasticityComponentTO {

    private Long componentNodeID;
    private String componentNodeName;
    private Long activeWorkers;
    private Long workersCount;
    private String status;
    private Date dateCreated;
    private long timestamp;

    public Long getComponentNodeID() {
        return componentNodeID;
    }

    public void setComponentNodeID(Long componentNodeID) {
        this.componentNodeID = componentNodeID;
    }

    public String getComponentNodeName() {
        return componentNodeName;
    }

    public void setComponentNodeName(String componentNodeName) {
        this.componentNodeName = componentNodeName;
    }

    public Long getActiveWorkers() {
        return activeWorkers;
    }

    public void setActiveWorkers(Long activeWorkers) {
        this.activeWorkers = activeWorkers;
    }

    public Long getWorkersCount() {
        return workersCount;
    }

    public void setWorkersCount(Long workersCount) {
        this.workersCount = workersCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
