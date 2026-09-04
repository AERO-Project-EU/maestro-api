package eu.orchestrator.transfer.entities.oss;

import java.io.Serializable;

public class ConstraintTO implements Serializable {

    private String constraintID;
    private String category; // COMPONENT_HOSTING, GRAPH_LINK, ACCESS
    private String componentNodeInstanceID; // Component
    private String componentNodeInstanceHexID;
    private String interfaceInstanceID; // ACCESS
    private String graphLinkNodeInstanceID; // GRAPH_LINK
    private String type; // HARD, SOFT

    private String constraintMetric;
    private String constraintValue;
    private String constraintUnit;

    private String location;
    private String qi;
    private String radioServiceType;
    private String resourceType;
    private Integer allocationRetentionPriorityProfile;
    private Double minimumGuaranteedBandwidth;
    private Double maximumRequiredBandwidth;

    public ConstraintTO() {
    }

    public String getComponentNodeInstanceHexID() {
        return componentNodeInstanceHexID;
    }

    public void setComponentNodeInstanceHexID(String componentNodeInstanceHexID) {
        this.componentNodeInstanceHexID = componentNodeInstanceHexID;
    }

    public String getConstraintID() {
        return constraintID;
    }

    public void setConstraintID(String constraintID) {
        this.constraintID = constraintID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getComponentNodeInstanceID() {
        return componentNodeInstanceID;
    }

    public void setComponentNodeInstanceID(String componentNodeInstanceID) {
        this.componentNodeInstanceID = componentNodeInstanceID;
    }

    public String getInterfaceInstanceID() {
        return interfaceInstanceID;
    }

    public void setInterfaceInstanceID(String interfaceInstanceID) {
        this.interfaceInstanceID = interfaceInstanceID;
    }

    public String getGraphLinkNodeInstanceID() {
        return graphLinkNodeInstanceID;
    }

    public void setGraphLinkNodeInstanceID(String graphLinkNodeInstanceID) {
        this.graphLinkNodeInstanceID = graphLinkNodeInstanceID;
    }

    public String getConstraintMetric() {
        return constraintMetric;
    }

    public void setConstraintMetric(String constraintMetric) {
        this.constraintMetric = constraintMetric;
    }

    public String getConstraintValue() {
        return constraintValue;
    }

    public void setConstraintValue(String constraintValue) {
        this.constraintValue = constraintValue;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getQi() {
        return qi;
    }

    public void setQi(String qi) {
        this.qi = qi;
    }

    public String getRadioServiceType() {
        return radioServiceType;
    }

    public void setRadioServiceType(String radioServiceType) {
        this.radioServiceType = radioServiceType;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public Integer getAllocationRetentionPriorityProfile() {
        return allocationRetentionPriorityProfile;
    }

    public void setAllocationRetentionPriorityProfile(Integer allocationRetentionPriorityProfile) {
        this.allocationRetentionPriorityProfile = allocationRetentionPriorityProfile;
    }

    public Double getMinimumGuaranteedBandwidth() {
        return minimumGuaranteedBandwidth;
    }

    public void setMinimumGuaranteedBandwidth(Double minimumGuaranteedBandwidth) {
        this.minimumGuaranteedBandwidth = minimumGuaranteedBandwidth;
    }

    public Double getMaximumRequiredBandwidth() {
        return maximumRequiredBandwidth;
    }

    public void setMaximumRequiredBandwidth(Double maximumRequiredBandwidth) {
        this.maximumRequiredBandwidth = maximumRequiredBandwidth;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getConstraintUnit() {
        return constraintUnit;
    }

    public void setConstraintUnit(String constraintUnit) {
        this.constraintUnit = constraintUnit;
    }
}
