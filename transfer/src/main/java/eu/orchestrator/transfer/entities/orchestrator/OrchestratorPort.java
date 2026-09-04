package eu.orchestrator.transfer.entities.orchestrator;

import java.io.Serializable;

public class OrchestratorPort implements Serializable {

    String target; //if -1 means that is not specified
    String published; //if null means that is not specified
    String protocol; //if null means that is not specified
    String mode; //if null means that is not specified
    String vna;
    String type;
    String networkAttachmentPoint;

    //Copied from the Interface Object of backend module
    public enum InterfaceType {

        CORE("Core"),
        ACCESS("Access");

        private String friendlyName;

        InterfaceType(String friendlyName) {
            this.friendlyName = friendlyName;
        }

        public String getFriendlyName() {
            return friendlyName;
        }
    }

    public OrchestratorPort() {
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getPublished() {
        return published;
    }

    public void setPublished(String published) {
        this.published = published;
    }

    public String getVna() {
        return vna;
    }

    public void setVna(String vna) {
        this.vna = vna;
    }

    public String getNetworkAttachmentPoint() {
        return networkAttachmentPoint;
    }

    public void setNetworkAttachmentPoint(String networkAttachmentPoint) {
        this.networkAttachmentPoint = networkAttachmentPoint;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
