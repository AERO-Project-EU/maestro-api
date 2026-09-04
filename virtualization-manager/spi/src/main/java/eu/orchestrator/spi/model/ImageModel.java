package eu.orchestrator.spi.model;

import java.io.Serializable;

public class ImageModel implements Serializable {

    private String id;

    private String payloadURL;
    private String containerFormat;
    private String diskFormat;

    private String name;

    private String imageLocation;
    private String imageType;

    private String architecture;
    private String creationDate;

    private Boolean publicValue;

    private String kernelId;
    private String platform;

    private String ramdiskId;
    private String state;

    private String description;
    private Boolean enaSupport;
    private String hypervisor;
    private String imageOwnerAlias;

    private String rootDeviceName;
    private String rootDeviceType;
    private String sriovNetSupport;
    private String virtualizationType;

    public ImageModel() {
    }

    public String getPayloadURL() {
        return payloadURL;
    }

    public void setPayloadURL(String payloadURL) {
        this.payloadURL = payloadURL;
    }

    public String getContainerFormat() {
        return containerFormat;
    }

    public void setContainerFormat(String containerFormat) {
        this.containerFormat = containerFormat;
    }

    public String getDiskFormat() {
        return diskFormat;
    }

    public void setDiskFormat(String diskFormat) {
        this.diskFormat = diskFormat;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageLocation() {
        return imageLocation;
    }

    public void setImageLocation(String imageLocation) {
        this.imageLocation = imageLocation;
    }

    public String getImageType() {
        return imageType;
    }

    public void setImageType(String imageType) {
        this.imageType = imageType;
    }

    public String getArchitecture() {
        return architecture;
    }

    public void setArchitecture(String architecture) {
        this.architecture = architecture;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public Boolean getPublicValue() {
        return publicValue;
    }

    public void setPublicValue(Boolean publicValue) {
        this.publicValue = publicValue;
    }

    public String getKernelId() {
        return kernelId;
    }

    public void setKernelId(String kernelId) {
        this.kernelId = kernelId;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getRamdiskId() {
        return ramdiskId;
    }

    public void setRamdiskId(String ramdiskId) {
        this.ramdiskId = ramdiskId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getEnaSupport() {
        return enaSupport;
    }

    public void setEnaSupport(Boolean enaSupport) {
        this.enaSupport = enaSupport;
    }

    public String getHypervisor() {
        return hypervisor;
    }

    public void setHypervisor(String hypervisor) {
        this.hypervisor = hypervisor;
    }

    public String getImageOwnerAlias() {
        return imageOwnerAlias;
    }

    public void setImageOwnerAlias(String imageOwnerAlias) {
        this.imageOwnerAlias = imageOwnerAlias;
    }

    public String getRootDeviceName() {
        return rootDeviceName;
    }

    public void setRootDeviceName(String rootDeviceName) {
        this.rootDeviceName = rootDeviceName;
    }

    public String getRootDeviceType() {
        return rootDeviceType;
    }

    public void setRootDeviceType(String rootDeviceType) {
        this.rootDeviceType = rootDeviceType;
    }

    public String getSriovNetSupport() {
        return sriovNetSupport;
    }

    public void setSriovNetSupport(String sriovNetSupport) {
        this.sriovNetSupport = sriovNetSupport;
    }

    public String getVirtualizationType() {
        return virtualizationType;
    }

    public void setVirtualizationType(String virtualizationType) {
        this.virtualizationType = virtualizationType;
    }
}
