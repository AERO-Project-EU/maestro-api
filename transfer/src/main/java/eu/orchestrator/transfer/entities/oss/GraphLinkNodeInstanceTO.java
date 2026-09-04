package eu.orchestrator.transfer.entities.oss;

import java.io.Serializable;

public class GraphLinkNodeInstanceTO implements Serializable {

    String graphLinkNodeInstanceID;
    String graphLinkNodeID;
    String graphLinkID;
    String friendlyName;
    String fromComponentNodeInstanceHexID;
    String fromComponentNodeInstanceID;
    String toComponentNodeInstanceID;
    String toComponentNodeInstanceHexID;
    String type; // CORE, ACCESS

    public GraphLinkNodeInstanceTO() {
    }

    public String getGraphLinkNodeID() {
        return graphLinkNodeID;
    }

    public void setGraphLinkNodeID(String graphLinkNodeID) {
        this.graphLinkNodeID = graphLinkNodeID;
    }

    public String getGraphLinkID() {
        return graphLinkID;
    }

    public void setGraphLinkID(String graphLinkID) {
        this.graphLinkID = graphLinkID;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public String getFromComponentNodeInstanceID() {
        return fromComponentNodeInstanceID;
    }

    public void setFromComponentNodeInstanceID(String fromComponentNodeInstanceID) {
        this.fromComponentNodeInstanceID = fromComponentNodeInstanceID;
    }

    public String getToComponentNodeInstanceID() {
        return toComponentNodeInstanceID;
    }

    public void setToComponentNodeInstanceID(String toComponentNodeInstanceID) {
        this.toComponentNodeInstanceID = toComponentNodeInstanceID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFromComponentNodeInstanceHexID() {
        return fromComponentNodeInstanceHexID;
    }

    public void setFromComponentNodeInstanceHexID(String fromComponentNodeInstanceHexID) {
        this.fromComponentNodeInstanceHexID = fromComponentNodeInstanceHexID;
    }

    public String getToComponentNodeInstanceHexID() {
        return toComponentNodeInstanceHexID;
    }

    public void setToComponentNodeInstanceHexID(String toComponentNodeInstanceHexID) {
        this.toComponentNodeInstanceHexID = toComponentNodeInstanceHexID;
    }

    public String getGraphLinkNodeInstanceID() {
        return graphLinkNodeInstanceID;
    }

    public void setGraphLinkNodeInstanceID(String graphLinkNodeInstanceID) {
        this.graphLinkNodeInstanceID = graphLinkNodeInstanceID;
    }
}
