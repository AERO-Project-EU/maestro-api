package eu.orchestrator.backend.transfer;

import java.io.Serializable;

public class DeploymentLimitationsRegionTO implements Serializable {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "DeploymentLimitationsRegionTO{" +
                "name='" + name + '\'' +
                '}';
    }
}