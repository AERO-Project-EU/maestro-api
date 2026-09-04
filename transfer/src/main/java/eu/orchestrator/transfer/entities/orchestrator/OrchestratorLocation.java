package eu.orchestrator.transfer.entities.orchestrator;

import java.io.Serializable;

public class OrchestratorLocation implements Serializable {

    private String country;
    private String region;

    public OrchestratorLocation() {
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}
