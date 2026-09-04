package eu.orchestrator.transfer.entities.orchestrator;

import java.io.Serializable;

/**
 * @author Konstantinos Theodosiou
 */
public class OrchestratorApplicationInstanceUsedResources implements Serializable {

    private Integer vCPUs;
    private Integer ram;
    private Integer storage;

    public OrchestratorApplicationInstanceUsedResources(){

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
