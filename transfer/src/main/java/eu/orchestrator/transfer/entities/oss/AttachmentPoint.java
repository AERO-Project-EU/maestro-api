package eu.orchestrator.transfer.entities.oss;

import java.io.Serializable;

public class AttachmentPoint implements Serializable {

    private String graphLinkNodeInstanceID;
    private String attachmentPointIdentifier;

    public AttachmentPoint() {
    }

    public String getGraphLinkNodeInstanceID() {
        return graphLinkNodeInstanceID;
    }

    public void setGraphLinkNodeInstanceID(String graphLinkNodeInstanceID) {
        this.graphLinkNodeInstanceID = graphLinkNodeInstanceID;
    }

    public String getAttachmentPointIdentifier() {
        return attachmentPointIdentifier;
    }

    public void setAttachmentPointIdentifier(String attachmentPointIdentifier) {
        this.attachmentPointIdentifier = attachmentPointIdentifier;
    }
}
