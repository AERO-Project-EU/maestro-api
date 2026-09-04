package eu.orchestrator.transfer.entities.orchestrator.internal;

import java.util.List;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 19/7/2019
 */
public class ComposeStatusObject {
    private String graphID;
    private String graphHexID;
    private String graphName;
    private String graphInstanceID;
    private String graphInstanceHexID;
    private String graphInstanceName;
    private List<ServiceStatus> serviceStatusList;

    private String reportedChange;
    private Boolean firstInfo;
    private Integer status;

    public String getGraphID() {
        return graphID;
    }

    public void setGraphID(String graphID) {
        this.graphID = graphID;
    }

    public String getGraphHexID() {
        return graphHexID;
    }

    public void setGraphHexID(String graphHexID) {
        this.graphHexID = graphHexID;
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

    public String getGraphInstanceHexID() {
        return graphInstanceHexID;
    }

    public void setGraphInstanceHexID(String graphInstanceHexID) {
        this.graphInstanceHexID = graphInstanceHexID;
    }

    public String getGraphInstanceName() {
        return graphInstanceName;
    }

    public void setGraphInstanceName(String graphInstanceName) {
        this.graphInstanceName = graphInstanceName;
    }

    public List<ServiceStatus> getServiceStatusList() {
        return serviceStatusList;
    }

    public void setServiceStatusList(List<ServiceStatus> serviceStatusList) {
        this.serviceStatusList = serviceStatusList;
    }

    public String getReportedChange() {
        return reportedChange;
    }

    public void setReportedChange(String reportedChange) {
        this.reportedChange = reportedChange;
    }

    public Boolean getFirstInfo() {
        return firstInfo;
    }

    public void setFirstInfo(Boolean firstInfo) {
        this.firstInfo = firstInfo;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
