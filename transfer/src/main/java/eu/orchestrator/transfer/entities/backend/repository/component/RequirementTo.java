package eu.orchestrator.transfer.entities.backend.repository.component;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 24/1/22
 */
public class RequirementTo implements Serializable {

    private Long requirementID;
    private Integer vCPUs;
    private Integer ram;
    private Integer storage;
    private String hypervisorType;
    private Boolean gpuRequired;
    private Date dateCreated;
    private Date lastModified;

    public RequirementTo() {
    }

    public Long getRequirementID() {
        return requirementID;
    }

    public void setRequirementID(Long requirementID) {
        this.requirementID = requirementID;
    }

    public Integer getvCPUs() {
        return vCPUs;
    }

    public void setvCPUs(Integer vCPUs) {
        this.vCPUs = vCPUs;
    }

    public Integer getRam() {
        return ram;
    }

    public void setRam(Integer ram) {
        this.ram = ram;
    }

    public Integer getStorage() {
        return storage;
    }

    public void setStorage(Integer storage) {
        this.storage = storage;
    }

    public String getHypervisorType() {
        return hypervisorType;
    }

    public void setHypervisorType(String hypervisorType) {
        this.hypervisorType = hypervisorType;
    }

    public Boolean getGpuRequired() {
        return gpuRequired;
    }

    public void setGpuRequired(Boolean gpuRequired) {
        this.gpuRequired = gpuRequired;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }
}
