package eu.orchestrator.transfer.entities.orchestrator;

import java.io.Serializable;

public class OrchestratorScalingRequest implements Serializable {

    private String graphID;
    private String graphHexID;
    private String graphName;
    private String graphInstanceID;
    private String graphInstanceHexID;
    private String graphInstanceName;
    private String componentNodeInstanceID;
    private String componentNodeInstanceHexID;
    private String componentNodeInstanceName;
    private String componentNodeID;
    private String componentNodeHexID;
    private String componentNodeName;
    private Integer numberOfWorkers;

    public OrchestratorScalingRequest() {
    }

    public String getGraphID() {
        return graphID;
    }

    public void setGraphID(String graphID) {
        this.graphID = graphID;
    }

    public String getGraphName() {
        return graphName;
    }

    public void setGraphName(String graphName) {
        this.graphName = graphName;
    }

    public String getGraphInstanceID() {
        return graphInstanceID;
    }

    public void setGraphInstanceID(String graphInstanceID) {
        this.graphInstanceID = graphInstanceID;
    }

    public String getGraphInstanceName() {
        return graphInstanceName;
    }

    public void setGraphInstanceName(String graphInstanceName) {
        this.graphInstanceName = graphInstanceName;
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

    public String getComponentNodeID() {
        return componentNodeID;
    }

    public void setComponentNodeID(String componentNodeID) {
        this.componentNodeID = componentNodeID;
    }

    public String getComponentNodeName() {
        return componentNodeName;
    }

    public void setComponentNodeName(String componentNodeName) {
        this.componentNodeName = componentNodeName;
    }

    public Integer getNumberOfWorkers() {
        return numberOfWorkers;
    }

    public void setNumberOfWorkers(Integer numberOfWorkers) {
        this.numberOfWorkers = numberOfWorkers;
    }

    public String getGraphHexID() {
        return graphHexID;
    }

    public void setGraphHexID(String graphHexID) {
        this.graphHexID = graphHexID;
    }

    public String getGraphInstanceHexID() {
        return graphInstanceHexID;
    }

    public void setGraphInstanceHexID(String graphInstanceHexID) {
        this.graphInstanceHexID = graphInstanceHexID;
    }

    public String getComponentNodeInstanceHexID() {
        return componentNodeInstanceHexID;
    }

    public void setComponentNodeInstanceHexID(String componentNodeInstanceHexID) {
        this.componentNodeInstanceHexID = componentNodeInstanceHexID;
    }

    public String getComponentNodeHexID() {
        return componentNodeHexID;
    }

    public void setComponentNodeHexID(String componentNodeHexID) {
        this.componentNodeHexID = componentNodeHexID;
    }
}
