package eu.orchestrator.transfer.entities.backend.repository.component;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 24/1/22
 */
public class GraphLinkTo implements Serializable {

    private Long graphLinkID;
    private String friendlyName;
    private InterfaceTo interfaceObj;
    private Date dateCreated;
    private Date lastModified;

    public GraphLinkTo() {
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

    public InterfaceTo getInterfaceObj() {
        return interfaceObj;
    }

    public void setInterfaceObj(InterfaceTo interfaceObj) {
        this.interfaceObj = interfaceObj;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }
}
