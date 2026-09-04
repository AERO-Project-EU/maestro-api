package eu.orchestrator.transfer.entities.backend.repository.application;

import eu.orchestrator.transfer.entities.backend.repository.component.GraphLinkTo;

import java.io.Serializable;

/**
 * @author Vasileios Matsoukas
 * @email billmats96@hotmail.com
 * @date 7/12/22
 */

public class GraphLinkNodeTo implements Serializable {

    private Long graphLinkNodeID;
    private ComponentNodeTo componentNodeFrom;
    private ComponentNodeTo componentNodeTo;
    private GraphLinkTo graphLink;

    public GraphLinkNodeTo() {
    }

    public Long getGraphLinkNodeID() {
        return graphLinkNodeID;
    }

    public void setGraphLinkNodeID(Long graphLinkNodeID) {
        this.graphLinkNodeID = graphLinkNodeID;
    }

    public ComponentNodeTo getComponentNodeFrom() {
        return componentNodeFrom;
    }

    public void setComponentNodeFrom(ComponentNodeTo componentNodeFrom) {
        this.componentNodeFrom = componentNodeFrom;
    }

    public ComponentNodeTo getComponentNodeTo() {
        return componentNodeTo;
    }

    public void setComponentNodeTo(ComponentNodeTo componentNodeTo) {
        this.componentNodeTo = componentNodeTo;
    }

    public GraphLinkTo getGraphLink() {
        return graphLink;
    }

    public void setGraphLink(GraphLinkTo graphLink) {
        this.graphLink = graphLink;
    }
}
