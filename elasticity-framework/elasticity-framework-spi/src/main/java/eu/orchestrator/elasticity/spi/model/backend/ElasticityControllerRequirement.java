package eu.orchestrator.elasticity.spi.model.backend;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 10/7/2019
 */
public class ElasticityControllerRequirement {

    private Integer vCPUs;
    private Integer ram;
    private Integer storage;
    private String hypervisorType;
    private Boolean gpuRequired;


    public enum HypervisorType {

        ESXI("ESXI"),
        KVM("KVM"),
        KEN("KEN");

        private String friendlyName;

        HypervisorType(String friendlyName) {
            this.friendlyName = friendlyName;
        }

        public String getFriendlyName() {
            return friendlyName;
        }
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
}
