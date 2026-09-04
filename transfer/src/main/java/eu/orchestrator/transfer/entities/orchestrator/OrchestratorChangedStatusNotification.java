package eu.orchestrator.transfer.entities.orchestrator;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author Konstantinos Theodosiou
 */
public class OrchestratorChangedStatusNotification implements Serializable {

    private String graphID;
    private String graphHexID;
    private String graphName;
    private String graphInstanceID;
    private String graphInstanceHexID;
    private String graphInstanceName;

    private String componentNodeInstanceID;
    private String componentNodeInstanceHexID;
    private String componentNodeInstanceName;
    private String componentNodeID;
    private String componentNodeHexID;
    private String componentNodeName;

    private String reportedChange;
    private String message;
    private String status;

    private String provider;

    private List<OrchestratorIP> orchestratorIPs;
    private String publicIP;

    private String changeType;

    private String workerStatus; // NULL, NEW, REMOVE
    private Date dateCreated;
    private String loadBalancedBy;

    private String vCPUs;
    private String memory;
    private String storage;

    public enum ChangeType {

        AgentStatusChange(0),
        GraphStatusChange(1),
        QuotasChange(2);

        private Integer value;

        ChangeType(Integer value) {
            this.value = value;
        }

        public Integer getValue() {
            return value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }
    }

    public String getGraphHexID() {
        return graphHexID;
    }

    public void setGraphHexID(String graphHexID) {
        this.graphHexID = graphHexID;
    }

    public String getGraphInstanceHexID() {
        return graphInstanceHexID;
    }

    public void setGraphInstanceHexID(String graphInstanceHexID) {
        this.graphInstanceHexID = graphInstanceHexID;
    }

    public String getComponentNodeInstanceHexID() {
        return componentNodeInstanceHexID;
    }

    public void setComponentNodeInstanceHexID(String componentNodeInstanceHexID) {
        this.componentNodeInstanceHexID = componentNodeInstanceHexID;
    }

    public String getComponentNodeHexID() {
        return componentNodeHexID;
    }

    public void setComponentNodeHexID(String componentNodeHexID) {
        this.componentNodeHexID = componentNodeHexID;
    }

    public String getGraphName() {
        return graphName;
    }

    public void setGraphName(String graphName) {
        this.graphName = graphName;
    }

    public String getGraphInstanceID() {
        return graphInstanceID;
    }

    public void setGraphInstanceID(String graphInstanceID) {
        this.graphInstanceID = graphInstanceID;
    }

    public String getGraphInstanceName() {
        return graphInstanceName;
    }

    public void setGraphInstanceName(String graphInstanceName) {
        this.graphInstanceName = graphInstanceName;
    }

    public String getComponentNodeInstanceID() {
        return componentNodeInstanceID;
    }

    public void setComponentNodeInstanceID(String componentNodeInstanceID) {
        this.componentNodeInstanceID = componentNodeInstanceID;
    }

    public String getComponentNodeInstanceName() {
        return componentNodeInstanceName;
    }

    public void setComponentNodeInstanceName(String componentNodeInstanceName) {
        this.componentNodeInstanceName = componentNodeInstanceName;
    }

    public String getComponentNodeID() {
        return componentNodeID;
    }

    public void setComponentNodeID(String componentNodeID) {
        this.componentNodeID = componentNodeID;
    }

    public String getComponentNodeName() {
        return componentNodeName;
    }

    public void setComponentNodeName(String componentNodeName) {
        this.componentNodeName = componentNodeName;
    }

    public String getGraphID() {

        return graphID;
    }

    public void setGraphID(String graphID) {
        this.graphID = graphID;
    }

    public String getReportedChange() {
        return reportedChange;
    }

    public void setReportedChange(String reportedChange) {
        this.reportedChange = reportedChange;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getWorkerStatus() {
        return workerStatus;
    }

    public void setWorkerStatus(String workerStatus) {
        this.workerStatus = workerStatus;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLoadBalancedBy() {
        return loadBalancedBy;
    }

    public void setLoadBalancedBy(String loadBalancedBy) {
        this.loadBalancedBy = loadBalancedBy;
    }

    public String getvCPUs() {
        return vCPUs;
    }

    public void setvCPUs(String vCPUs) {
        this.vCPUs = vCPUs;
    }

    public String getMemory() {
        return memory;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }

    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }

    public List<OrchestratorIP> getOrchestratorIPs() {
        return orchestratorIPs;
    }

    public void setOrchestratorIPs(List<OrchestratorIP> orchestratorIPs) {
        this.orchestratorIPs = orchestratorIPs;
    }

    public String getPublicIP() {
        return publicIP;
    }

    public void setPublicIP(String publicIP) {
        this.publicIP = publicIP;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}
