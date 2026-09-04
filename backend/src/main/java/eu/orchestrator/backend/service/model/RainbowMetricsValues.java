package eu.orchestrator.backend.service.model;

import java.io.Serializable;

public class RainbowMetricsValues implements Serializable {

    private String metricID;
    private double val;
    private Long timestamp;
    private String entityID;
    private String entityType;
    private String name;
    private String units;
    private String desc;
    private String group;
    private String minVal;
    private String maxVal;
    private String higherIsBetter;


    public String getMetricID() {
        return metricID;
    }

    public void setMetricID(String metricID) {
        this.metricID = metricID;
    }

    public double getVal() {
        return val;
    }

    public void setVal(double val) {
        this.val = val;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
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

    public String getMinVal() {
        return minVal;
    }

    public void setMinVal(String minVal) {
        this.minVal = minVal;
    }

    public String getMaxVal() {
        return maxVal;
    }

    public void setMaxVal(String maxVal) {
        this.maxVal = maxVal;
    }

    public String getHigherIsBetter() {
        return higherIsBetter;
    }

    public void setHigherIsBetter(String higherIsBetter) {
        this.higherIsBetter = higherIsBetter;
    }
}
