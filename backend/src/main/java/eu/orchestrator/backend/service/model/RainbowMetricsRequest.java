package eu.orchestrator.backend.service.model;

import java.io.Serializable;

public class RainbowMetricsRequest implements Serializable {


    private boolean latest;


    public boolean isLatest() {
        return latest;
    }

    public void setLatest(boolean latest) {
        this.latest = latest;
    }
}
