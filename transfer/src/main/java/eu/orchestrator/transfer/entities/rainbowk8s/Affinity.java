package eu.orchestrator.transfer.entities.rainbowk8s;

import java.io.Serializable;

public class Affinity implements Serializable {

    private NodeAffinity nodeAffinity;


    public NodeAffinity getNodeAffinity() {
        return nodeAffinity;
    }

    public void setNodeAffinity(NodeAffinity nodeAffinity) {
        this.nodeAffinity = nodeAffinity;
    }


    public static class Builder {
        private NodeAffinity nodeAffinity;

        public Affinity.Builder withNodeAffinity(NodeAffinity nodeAffinity) {
            this.nodeAffinity = nodeAffinity;
            return this;
        }

        public Affinity build() {
            Affinity affinity = new Affinity();

            affinity.setNodeAffinity(this.nodeAffinity);

            return affinity;
        }
    }
}
