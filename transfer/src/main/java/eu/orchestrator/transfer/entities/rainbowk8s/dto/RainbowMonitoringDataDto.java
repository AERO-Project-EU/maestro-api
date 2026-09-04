package eu.orchestrator.transfer.entities.rainbowk8s.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RainbowMonitoringDataDto implements Serializable {

    private String metricID;
    private String entityID;
    private List<RainbowMonitoringValueDto> values;
    private String entityType;
    private String name;
    private String units;
    private String desc;
    private String group;
    private double minVal;
    private double maxVal;
    private boolean higherIsBetter;
    private RainbowMonitoringPodDto pod;
    private RainbowMonitoringContainerDto container;


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

    public List<RainbowMonitoringValueDto> getValues() {
        if (values == null) {
            values = new ArrayList<>();
        }
        return values;
    }

    public void setValues(List<RainbowMonitoringValueDto> values) {
        this.values = values;
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

    public RainbowMonitoringPodDto getPod() {
        return pod;
    }

    public void setPod(RainbowMonitoringPodDto pod) {
        this.pod = pod;
    }

    public RainbowMonitoringContainerDto getContainer() {
        return container;
    }

    public void setContainer(RainbowMonitoringContainerDto container) {
        this.container = container;
    }
}
