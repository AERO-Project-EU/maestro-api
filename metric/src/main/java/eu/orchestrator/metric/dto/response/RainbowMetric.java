package eu.orchestrator.metric.dto.response;

import java.io.Serializable;

public class RainbowMetric implements Serializable {

    private String node;
    private String metricID;
    private String entityID;
    private String entityType;
    private String name;
    private String units;
    private String desc;
    private String group;
    private double minVal;
    private double maxVal;
    private boolean higherIsBetter;
    private RainbowMetricPod pod;
    private RainbowMetricContainer container;

    
    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public String getMetricID() {
        return metricID;
    }

    public void setMetricID(String metricID) {
        this.metricID = metricID;
    }

    public String getEntityID() {
        return entityID;
    }

    public void setEntityID(String entityID) {
        this.entityID = entityID;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public double getMinVal() {
        return minVal;
    }

    public void setMinVal(double minVal) {
        this.minVal = minVal;
    }

    public double getMaxVal() {
        return maxVal;
    }

    public void setMaxVal(double maxVal) {
        this.maxVal = maxVal;
    }

    public boolean isHigherIsBetter() {
        return higherIsBetter;
    }

    public void setHigherIsBetter(boolean higherIsBetter) {
        this.higherIsBetter = higherIsBetter;
    }

    public RainbowMetricPod getPod() {
        return pod;
    }

    public void setPod(RainbowMetricPod pod) {
        this.pod = pod;
    }

    public RainbowMetricContainer getContainer() {
        return container;
    }

    public void setContainer(RainbowMetricContainer container) {
        this.container = container;
    }
}
