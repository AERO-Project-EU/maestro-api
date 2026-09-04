package eu.orchestrator.transfer.entities.orchestrator;

import java.io.Serializable;

public class OrchestratorFlavor implements Serializable {

    private String id;
    private Integer vCPUs;
    private Integer ram;
    private Integer storage;

    public OrchestratorFlavor() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
}
