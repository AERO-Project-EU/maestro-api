package eu.orchestrator.transfer.entities.oss;

import java.io.Serializable;

public class ComponentNodeInstanceTO implements Serializable {

    private String componentNodeInstanceID;
    private String componentNodeInstanceHexID;
    private String componentNodeInstanceName;
//    private String componentNodeID;
//    private String componentNodeName;
//    private String componentID;
//    private String componentName;

    public ComponentNodeInstanceTO() {
    }

    public String getComponentNodeInstanceID() {
        return componentNodeInstanceID;
    }

    public void setComponentNodeInstanceID(String componentNodeInstanceID) {
        this.componentNodeInstanceID = componentNodeInstanceID;
    }

    public String getComponentNodeInstanceName() {
        return componentNodeInstanceName;
    }

    public void setComponentNodeInstanceName(String componentNodeInstanceName) {
        this.componentNodeInstanceName = componentNodeInstanceName;
    }

    public String getComponentNodeInstanceHexID() {
        return componentNodeInstanceHexID;
    }

    public void setComponentNodeInstanceHexID(String componentNodeInstanceHexID) {
        this.componentNodeInstanceHexID = componentNodeInstanceHexID;
    }
}
