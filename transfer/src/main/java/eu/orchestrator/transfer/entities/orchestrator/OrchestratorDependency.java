package eu.orchestrator.transfer.entities.orchestrator;

import java.io.Serializable;

public class OrchestratorDependency implements Serializable {

    private String dependency;
    private String vna;
    private String networkAttachmentPoint;

    public OrchestratorDependency() {
    }

    public OrchestratorDependency(String dependency) {
        this.dependency = dependency;
        this.networkAttachmentPoint = null;
    }

    public OrchestratorDependency(String dependency, String networkAttachmentPoint, String vna) {
        this.dependency = dependency;
        this.networkAttachmentPoint = networkAttachmentPoint;
        this.vna = vna;
    }

    public String getVna() {
        return vna;
    }

    public void setVna(String vna) {
        this.vna = vna;
    }

    public String getDependency() {
        return dependency;
    }

    public void setDependency(String dependency) {
        this.dependency = dependency;
    }

    public String getNetworkAttachmentPoint() {
        return networkAttachmentPoint;
    }

    public void setNetworkAttachmentPoint(String networkAttachmentPoint) {
        this.networkAttachmentPoint = networkAttachmentPoint;
    }
}
