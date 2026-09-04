package eu.orchestrator.backend.transfer;

import java.io.Serializable;

public class ConstraintTO implements Serializable {

    private Long constraintID;
    private String constraintCategory;
    private String constraintType;
    private String constraintMetric;
    private String constraintUnit;
    private String constraintValue;
    private RadioServiceTypeTO radioServiceType;
    private String resourceType;
    private CountryTO country;
    private QITO qi;
    private Integer allocationRetentionPriorityProfile;
    private Double minimumGuaranteedBandwidth;
    private Double maximumRequiredBandwidth;

    // Relations
    private ComponentNodeInstanceTO componentNodeInstance;
    private GraphLinkNodeInstanceTO graphLinkNodeInstance;
    private InterfaceInstanceTO interfaceInstance;
    private Boolean deletableConstraint = true;

    public ConstraintTO() {
    }

    public Long getConstraintID() {
        return constraintID;
    }

    public void setConstraintID(Long constraintID) {
        this.constraintID = constraintID;
    }

    public String getConstraintCategory() {
        return constraintCategory;
    }

    public void setConstraintCategory(String constraintCategory) {
        this.constraintCategory = constraintCategory;
    }

    public String getConstraintType() {
        return constraintType;
    }

    public void setConstraintType(String constraintType) {
        this.constraintType = constraintType;
    }

    public String getConstraintMetric() {
        return constraintMetric;
    }

    public void setConstraintMetric(String constraintMetric) {
        this.constraintMetric = constraintMetric;
    }

    public String getConstraintUnit() {
        return constraintUnit;
    }

    public void setConstraintUnit(String constraintUnit) {
        this.constraintUnit = constraintUnit;
    }

    public String getConstraintValue() {
        return constraintValue;
    }

    public void setConstraintValue(String constraintValue) {
        this.constraintValue = constraintValue;
    }

    public RadioServiceTypeTO getRadioServiceType() {
        return radioServiceType;
    }

    public void setRadioServiceType(RadioServiceTypeTO radioServiceType) {
        this.radioServiceType = radioServiceType;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public CountryTO getCountry() {
        return country;
    }

    public void setCountry(CountryTO country) {
        this.country = country;
    }

    public QITO getQi() {
        return qi;
    }

    public void setQi(QITO qi) {
        this.qi = qi;
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

    public ComponentNodeInstanceTO getComponentNodeInstance() {
        return componentNodeInstance;
    }

    public void setComponentNodeInstance(
            ComponentNodeInstanceTO componentNodeInstance) {
        this.componentNodeInstance = componentNodeInstance;
    }

    public GraphLinkNodeInstanceTO getGraphLinkNodeInstance() {
        return graphLinkNodeInstance;
    }

    public void setGraphLinkNodeInstance(GraphLinkNodeInstanceTO graphLinkNodeInstance) {
        this.graphLinkNodeInstance = graphLinkNodeInstance;
    }

    public InterfaceInstanceTO getInterfaceInstance() {
        return interfaceInstance;
    }

    public void setInterfaceInstance(
            InterfaceInstanceTO interfaceInstance) {
        this.interfaceInstance = interfaceInstance;
    }

    public Boolean getDeletableConstraint() {
        return deletableConstraint;
    }

    public void setDeletableConstraint(Boolean deletableConstraint) {
        this.deletableConstraint = deletableConstraint;
    }
}
