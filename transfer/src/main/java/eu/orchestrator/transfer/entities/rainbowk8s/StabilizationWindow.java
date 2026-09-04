package eu.orchestrator.transfer.entities.rainbowk8s;

import java.io.Serializable;

public class StabilizationWindow implements Serializable {

    private int scaleUpSeconds;

    private int scaleDownSeconds;

    public int getScaleUpSeconds() {
        return scaleUpSeconds;
    }

    public void setScaleUpSeconds(int scaleUpSeconds) {
        this.scaleUpSeconds = scaleUpSeconds;
    }

    public int getScaleDownSeconds() {
        return scaleDownSeconds;
    }

    public void setScaleDownSeconds(int scaleDownSeconds) {
        this.scaleDownSeconds = scaleDownSeconds;
    }
}
