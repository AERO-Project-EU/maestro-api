package eu.orchestrator.transfer.entities.oss;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class ComponentPlacement implements Serializable {

    String vimID;
    String componentNodeInstanceID;
    String componentNodeInstanceHexID;
    String flavorID;
    List<AttachmentPoint> attachmentPoints;

    public ComponentPlacement() {
    }

    public String getVimID() {
        return vimID;
    }

    public void setVimID(String vimID) {
        this.vimID = vimID;
    }

    public String getComponentNodeInstanceID() {
        return componentNodeInstanceID;
    }

    public void setComponentNodeInstanceID(String componentNodeInstanceID) {
        this.componentNodeInstanceID = componentNodeInstanceID;
    }

    public List<AttachmentPoint> getAttachmentPoints() {
        return attachmentPoints != null ? attachmentPoints : Collections.emptyList();
    }

    public void setAttachmentPoints(List<AttachmentPoint> attachmentPoints) {
        this.attachmentPoints = attachmentPoints;
    }

    public String getComponentNodeInstanceHexID() {
        return componentNodeInstanceHexID;
    }

    public void setComponentNodeInstanceHexID(String componentNodeInstanceHexID) {
        this.componentNodeInstanceHexID = componentNodeInstanceHexID;
    }

    public String getFlavorID() {
        return flavorID;
    }

    public void setFlavorID(String flavorID) {
        this.flavorID = flavorID;
    }
}
