package eu.orchestrator.transfer.entities.rainbowk8s;

import java.io.Serializable;

public class ElasticityStrategyDto implements Serializable {

    private String kind;
    private String version;

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
