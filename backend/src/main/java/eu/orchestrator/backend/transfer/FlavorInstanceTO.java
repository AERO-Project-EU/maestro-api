package eu.orchestrator.backend.transfer;

import java.io.Serializable;

public class FlavorInstanceTO implements Serializable {

    private Long flavorID;
    private Integer vCPUs;
    private Integer ram;
    private Integer storage;
    private Boolean serverlessEnabled;

    public FlavorInstanceTO() {
    }

    public Long getFlavorID() {
        return flavorID;
    }

    public void setFlavorID(Long flavorID) {
        this.flavorID = flavorID;
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

    public Boolean getServerlessEnabled() {
        return serverlessEnabled;
    }

    public void setServerlessEnabled(Boolean serverlessEnabled) {
        this.serverlessEnabled = serverlessEnabled;
    }
}
