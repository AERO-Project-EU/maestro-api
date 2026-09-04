package eu.orchestrator.backend.transfer;

import java.io.Serializable;


public class DeploymentLimitationsLocationTO implements Serializable {

    private String componentNodeHexIDA;
    private String componentNodeHexIDB;

    public String getComponentNodeHexIDA() {
        return componentNodeHexIDA;
    }

    public void setComponentNodeHexIDA(String componentNodeHexIDA) {
        this.componentNodeHexIDA = componentNodeHexIDA;
    }

    public String getComponentNodeHexIDB() {
        return componentNodeHexIDB;
    }

    public void setComponentNodeHexIDB(String componentNodeHexIDB) {
        this.componentNodeHexIDB = componentNodeHexIDB;
    }

    @Override
    public String toString() {
        return "DeploymentLimitationsLocationTO{" +
                "componentNodeHexIDA='" + componentNodeHexIDA + '\'' +
                ", componentNodeHexIDB='" + componentNodeHexIDB + '\'' +
                '}';
    }
}