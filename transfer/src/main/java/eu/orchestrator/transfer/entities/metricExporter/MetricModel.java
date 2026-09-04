package eu.orchestrator.transfer.entities.metricExporter;

import java.util.List;

/**
 * @author Panagiotis Parthenis
 */
public class MetricModel {

    private String graphId;
    private String graphHexId;
    private String graphName;

    private String graphInstanceId;
    private String graphInstanceHexId;
    private String graphInstanceName;

    private String componentNodeInstanceId;
    private String componentNodeInstanceHexId;
    private String componentNodeInstanceName;

    private String componentNodeId;
    private String componentNodeHexId;
    private String componentNodeName;

    private double percentageUsedCPU; //percentage
    private double diskAvailable;   // GB
    private double percentageUsedRAM;   //percentage
    private double networkTrafficPacketsSend; //packets
    private double networkTrafficPacketsReceived; //packets
    private double diskThroughputRead; //kilobytes
    private double diskThroughputWrite; //kilobytes
    private List<ProxyMetricModel> proxyMetricModels;
    private Long timestamp;

    public String getGraphId() {
        return graphId;
    }

    public void setGraphId(String graphId) {
        this.graphId = graphId;
    }

    public String getGraphName() {
        return graphName;
    }

    public void setGraphName(String graphName) {
        this.graphName = graphName;
    }

    public String getGraphInstanceId() {
        return graphInstanceId;
    }

    public void setGraphInstanceId(String graphInstanceId) {
        this.graphInstanceId = graphInstanceId;
    }

    public String getGraphInstanceName() {
        return graphInstanceName;
    }

    public void setGraphInstanceName(String graphInstanceName) {
        this.graphInstanceName = graphInstanceName;
    }

    public String getComponentNodeInstanceId() {
        return componentNodeInstanceId;
    }

    public void setComponentNodeInstanceId(String componentNodeInstanceId) {
        this.componentNodeInstanceId = componentNodeInstanceId;
    }

    public String getComponentNodeInstanceName() {
        return componentNodeInstanceName;
    }

    public void setComponentNodeInstanceName(String componentNodeInstanceName) {
        this.componentNodeInstanceName = componentNodeInstanceName;
    }

    public String getComponentNodeId() {
        return componentNodeId;
    }

    public void setComponentNodeId(String componentNodeId) {
        this.componentNodeId = componentNodeId;
    }

    public String getComponentNodeName() {
        return componentNodeName;
    }

    public void setComponentNodeName(String componentNodeName) {
        this.componentNodeName = componentNodeName;
    }

    public double getPercentageUsedCPU() {
        return percentageUsedCPU;
    }

    public void setPercentageUsedCPU(double percentageUsedCPU) {
        this.percentageUsedCPU = percentageUsedCPU;
    }

    public double getDiskAvailable() {
        return diskAvailable;
    }

    public void setDiskAvailable(double diskAvailable) {
        this.diskAvailable = diskAvailable;
    }

    public double getPercentageUsedRAM() {
        return percentageUsedRAM;
    }

    public void setPercentageUsedRAM(double percentageUsedRAM) {
        this.percentageUsedRAM = percentageUsedRAM;
    }

    public double getNetworkTrafficPacketsSend() {
        return networkTrafficPacketsSend;
    }

    public void setNetworkTrafficPacketsSend(double networkTrafficPacketsSend) {
        this.networkTrafficPacketsSend = networkTrafficPacketsSend;
    }

    public double getNetworkTrafficPacketsReceived() {
        return networkTrafficPacketsReceived;
    }

    public void setNetworkTrafficPacketsReceived(double networkTrafficPacketsReceived) {
        this.networkTrafficPacketsReceived = networkTrafficPacketsReceived;
    }

    public double getDiskThroughputRead() {
        return diskThroughputRead;
    }

    public void setDiskThroughputRead(double diskThroughputRead) {
        this.diskThroughputRead = diskThroughputRead;
    }

    public double getDiskThroughputWrite() {
        return diskThroughputWrite;
    }

    public void setDiskThroughputWrite(double diskThroughputWrite) {
        this.diskThroughputWrite = diskThroughputWrite;
    }

    public List<ProxyMetricModel> getProxyMetricModels() {
        return proxyMetricModels;
    }

    public void setProxyMetricModels(List<ProxyMetricModel> proxyMetricModels) {
        this.proxyMetricModels = proxyMetricModels;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getGraphHexId() {
        return graphHexId;
    }

    public void setGraphHexId(String graphHexId) {
        this.graphHexId = graphHexId;
    }

    public String getGraphInstanceHexId() {
        return graphInstanceHexId;
    }

    public void setGraphInstanceHexId(String graphInstanceHexId) {
        this.graphInstanceHexId = graphInstanceHexId;
    }

    public String getComponentNodeInstanceHexId() {
        return componentNodeInstanceHexId;
    }

    public void setComponentNodeInstanceHexId(String componentNodeInstanceHexId) {
        this.componentNodeInstanceHexId = componentNodeInstanceHexId;
    }

    public String getComponentNodeHexId() {
        return componentNodeHexId;
    }

    public void setComponentNodeHexId(String componentNodeHexId) {
        this.componentNodeHexId = componentNodeHexId;
    }

    @Override
    public String toString() {
        return "MetricModel{" +
            "graphId='" + graphId + '\'' +
            ", graphHexId='" + graphHexId + '\'' +
            ", graphName='" + graphName + '\'' +
            ", graphInstanceId='" + graphInstanceId + '\'' +
            ", graphInstanceHexId='" + graphInstanceHexId + '\'' +
            ", graphInstanceName='" + graphInstanceName + '\'' +
            ", componentNodeInstanceId='" + componentNodeInstanceId + '\'' +
            ", componentNodeInstanceHexId='" + componentNodeInstanceHexId + '\'' +
            ", componentNodeInstanceName='" + componentNodeInstanceName + '\'' +
            ", componentNodeId='" + componentNodeId + '\'' +
            ", componentNodeHexId='" + componentNodeHexId + '\'' +
            ", componentNodeName='" + componentNodeName + '\'' +
            ", percentageUsedCPU=" + percentageUsedCPU +
            ", diskAvailable=" + diskAvailable +
            ", percentageUsedRAM=" + percentageUsedRAM +
            ", networkTrafficPacketsSend=" + networkTrafficPacketsSend +
            ", networkTrafficPacketsReceived=" + networkTrafficPacketsReceived +
            ", diskThroughputRead=" + diskThroughputRead +
            ", diskThroughputWrite=" + diskThroughputWrite +
            ", proxyMetricModels=" + proxyMetricModels +
            ", timestamp=" + timestamp +
            '}';
    }
}
