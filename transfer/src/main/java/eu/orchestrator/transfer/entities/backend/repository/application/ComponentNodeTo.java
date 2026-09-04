package eu.orchestrator.transfer.entities.backend.repository.application;

import eu.orchestrator.transfer.entities.backend.repository.component.ComponentTo;

import java.io.Serializable;

/**
 * @author Vasileios Matsoukas
 * @email billmats96@hotmail.com
 * @date 7/12/22
 */
public class ComponentNodeTo implements Serializable {

    private Long componentNodeID;
    private String hexID;
    private String name;
    private ComponentTo componentTo;

    public ComponentNodeTo() {
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

    public ComponentTo getComponentTo() {
        return componentTo;
    }

    public void setComponentTo(ComponentTo componentTo) {
        this.componentTo = componentTo;
    }
}
