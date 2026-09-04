package eu.orchestrator.spi.model;

import java.io.Serializable;

public class FlavorModel implements Serializable {

    private String id;

    private String name;

    private String vCPU;

    private String ram;

    private String storage;

    public FlavorModel() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getvCPU() {
        return vCPU;
    }

    public void setvCPU(String vCPU) {
        this.vCPU = vCPU;
    }

    public String getRam() {
        return ram;
    }

    public void setRam(String ram) {
        this.ram = ram;
    }

    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }

}
