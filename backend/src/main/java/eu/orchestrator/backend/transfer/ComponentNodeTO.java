package eu.orchestrator.backend.transfer;

import java.io.Serializable;

public class ComponentNodeTO implements Serializable {

    private Long componentNodeID;
    private String hexID;
    private String name;
    private ComponentTO component;

    public ComponentNodeTO() {
    }

    public Long getComponentNodeID() {
        return componentNodeID;
    }

    public void setComponentNodeID(Long componentNodeID) {
        this.componentNodeID = componentNodeID;
    }

    public String getHexID() {
        return hexID;
    }

    public void setHexID(String hexID) {
        this.hexID = hexID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ComponentTO getComponent() {
        return component;
    }

    public void setComponent(ComponentTO component) {
        this.component = component;
    }
}
