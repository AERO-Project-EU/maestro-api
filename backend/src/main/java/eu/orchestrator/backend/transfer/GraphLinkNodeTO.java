package eu.orchestrator.backend.transfer;

import java.io.Serializable;

public class GraphLinkNodeTO implements Serializable {

    private Long graphLinkNodeID;
    private ComponentNodeTO componentNodeFrom;
    private ComponentNodeTO componentNodeTo;
    private GraphLinkTO graphLink;

    public GraphLinkNodeTO() {
    }

    public Long getGraphLinkNodeID() {
        return graphLinkNodeID;
    }

    public void setGraphLinkNodeID(Long graphLinkNodeID) {
        this.graphLinkNodeID = graphLinkNodeID;
    }

    public ComponentNodeTO getComponentNodeFrom() {
        return componentNodeFrom;
    }

    public void setComponentNodeFrom(ComponentNodeTO componentNodeFrom) {
        this.componentNodeFrom = componentNodeFrom;
    }

    public ComponentNodeTO getComponentNodeTo() {
        return componentNodeTo;
    }

    public void setComponentNodeTo(ComponentNodeTO componentNodeTo) {
        this.componentNodeTo = componentNodeTo;
    }

    public GraphLinkTO getGraphLink() {
        return graphLink;
    }

    public void setGraphLink(GraphLinkTO graphLink) {
        this.graphLink = graphLink;
    }
}
