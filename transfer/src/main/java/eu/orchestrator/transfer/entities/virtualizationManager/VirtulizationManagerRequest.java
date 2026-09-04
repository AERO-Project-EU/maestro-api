package eu.orchestrator.transfer.entities.virtualizationManager;

import eu.orchestrator.transfer.entities.orchestrator.OrchestratorFlavor;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorInstance;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorProviderAuthenticationDetails;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorVolume;

import java.io.Serializable;
import java.util.List;

/**
 * @author Konstantinos Theodosiou
 */
public class VirtulizationManagerRequest implements Serializable {

    OrchestratorProviderAuthenticationDetails authDetails;
    OrchestratorFlavor flavor;
    OrchestratorInstance instance;
    OrchestratorVolume bootableVolume;
    OrchestratorVolume attachedVolume;

    public VirtulizationManagerRequest(){

    }

    public OrchestratorProviderAuthenticationDetails getAuthDetails() {
        return authDetails;
    }

    public void setAuthDetails(OrchestratorProviderAuthenticationDetails authDetails) {
        this.authDetails = authDetails;
    }

    public OrchestratorFlavor getFlavor() {
        return flavor;
    }

    public void setFlavor(OrchestratorFlavor flavor) {
        this.flavor = flavor;
    }

    public OrchestratorInstance getInstance() {
        return instance;
    }

    public void setInstance(OrchestratorInstance instance) {
        this.instance = instance;
    }

    public OrchestratorVolume getBootableVolume() {
        return bootableVolume;
    }

    public void setBootableVolume(OrchestratorVolume bootableVolume) {
        this.bootableVolume = bootableVolume;
    }

    public OrchestratorVolume getAttachedVolume() {
        return attachedVolume;
    }

    public void setAttachedVolume(OrchestratorVolume attachedVolume) {
        this.attachedVolume = attachedVolume;
    }
}
