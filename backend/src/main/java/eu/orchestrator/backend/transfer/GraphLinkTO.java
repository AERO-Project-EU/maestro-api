package eu.orchestrator.backend.transfer;

import java.io.Serializable;

public class GraphLinkTO implements Serializable {

    private Long graphLinkID;
    private String friendlyName;
    private InterfaceTO interfaceObj;

    public GraphLinkTO() {
    }

    public Long getGraphLinkID() {
        return graphLinkID;
    }

    public void setGraphLinkID(Long graphLinkID) {
        this.graphLinkID = graphLinkID;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public InterfaceTO getInterfaceObj() {
        return interfaceObj;
    }

    public void setInterfaceObj(InterfaceTO interfaceObj) {
        this.interfaceObj = interfaceObj;
    }
}
