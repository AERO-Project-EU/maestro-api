package eu.orchestrator.transfer.entities.orchestrator;

import java.io.Serializable;

/**
 * @author Konstantinos Theodosiou
 */
public class OrchestratorElasticity implements Serializable {

    String profile;
    String type;

    public OrchestratorElasticity() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }
}
