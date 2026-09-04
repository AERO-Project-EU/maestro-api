package eu.orchestrator.transfer.entities.rainbowk8s;

import java.io.Serializable;

public class Resources implements Serializable {

    private Limits limits;


    public Limits getLimits() {
        return limits;
    }

    public void setLimits(Limits limits) {
        this.limits = limits;
    }


    public static class Builder {
        private Limits limits;

        public Resources.Builder withLimits(Limits limits) {
            this.limits = limits;
            return this;
        }

        public Resources build() {
            Resources resources = new Resources();

            resources.setLimits(this.limits);

            return resources;
        }
    }
}
