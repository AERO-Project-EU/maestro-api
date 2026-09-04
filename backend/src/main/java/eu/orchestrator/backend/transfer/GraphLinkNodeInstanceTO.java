package eu.orchestrator.backend.transfer;

import java.io.Serializable;

public class GraphLinkNodeInstanceTO implements Serializable {

    private Long graphLinkNodeInstanceID;
    private ComponentNodeInstanceTO componentNodeInstanceFrom;
    private ComponentNodeInstanceTO componentNodeInstanceTo;
    private GraphLinkNodeTO graphLinkNode;

    public GraphLinkNodeInstanceTO() {
    }

    public Long getGraphLinkNodeInstanceID() {
        return graphLinkNodeInstanceID;
    }

    public void setGraphLinkNodeInstanceID(Long graphLinkNodeInstanceID) {
        this.graphLinkNodeInstanceID = graphLinkNodeInstanceID;
    }

    public ComponentNodeInstanceTO getComponentNodeInstanceFrom() {
        return componentNodeInstanceFrom;
    }

    public void setComponentNodeInstanceFrom(
            ComponentNodeInstanceTO componentNodeInstanceFrom) {
        this.componentNodeInstanceFrom = componentNodeInstanceFrom;
    }

    public ComponentNodeInstanceTO getComponentNodeInstanceTo() {
        return componentNodeInstanceTo;
    }

    public void setComponentNodeInstanceTo(
            ComponentNodeInstanceTO componentNodeInstanceTo) {
        this.componentNodeInstanceTo = componentNodeInstanceTo;
    }

    public GraphLinkNodeTO getGraphLinkNode() {
        return graphLinkNode;
    }

    public void setGraphLinkNode(GraphLinkNodeTO graphLinkNode) {
        this.graphLinkNode = graphLinkNode;
    }
}
