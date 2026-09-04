package eu.orchestrator.transfer.entities.rainbowk8s.dto;

import java.io.Serializable;
import java.util.List;

public class RainbowMonitoringRequestDto implements Serializable {

    private List<String> metricID;

    private List<String> podName;

    private List<String> entityType;

    private List<String> nodes;

    public RainbowMonitoringRequestDto() {
    }

    public RainbowMonitoringRequestDto(List<String> metricID, List<String> podName,
            List<String> entityType, List<String> nodes) {
        this.metricID = metricID;
        this.podName = podName;
        this.entityType = entityType;
        this.nodes = nodes;
    }

    public List<String> getMetricID() {
        return metricID;
    }

    public void setMetricID(List<String> metricID) {
        this.metricID = metricID;
    }

    public List<String> getPodName() {
        return podName;
    }

    public void setPodName(List<String> podName) {
        this.podName = podName;
    }

    public List<String> getEntityType() {
        return entityType;
    }

    public void setEntityType(List<String> entityType) {
        this.entityType = entityType;
    }

    public List<String> getNodes() {
        return nodes;
    }

    public void setNodes(List<String> nodes) {
        this.nodes = nodes;
    }
}
