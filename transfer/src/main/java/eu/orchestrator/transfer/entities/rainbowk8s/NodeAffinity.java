package eu.orchestrator.transfer.entities.rainbowk8s;

import java.io.Serializable;

public class NodeAffinity implements Serializable {

    private RequiredDuringSchedulingIgnoredDuringExecution requiredDuringSchedulingIgnoredDuringExecution;


    public RequiredDuringSchedulingIgnoredDuringExecution getRequiredDuringSchedulingIgnoredDuringExecution() {
        return requiredDuringSchedulingIgnoredDuringExecution;
    }

    public void setRequiredDuringSchedulingIgnoredDuringExecution(RequiredDuringSchedulingIgnoredDuringExecution requiredDuringSchedulingIgnoredDuringExecution) {
        this.requiredDuringSchedulingIgnoredDuringExecution = requiredDuringSchedulingIgnoredDuringExecution;
    }


    public static class Builder {
        private RequiredDuringSchedulingIgnoredDuringExecution requiredDuringSchedulingIgnoredDuringExecution;

        public NodeAffinity.Builder withRequiredDuringSchedulingIgnoredDuringExecution(RequiredDuringSchedulingIgnoredDuringExecution requiredDuringSchedulingIgnoredDuringExecution) {
            this.requiredDuringSchedulingIgnoredDuringExecution = requiredDuringSchedulingIgnoredDuringExecution;
            return this;
        }

        public NodeAffinity build() {
            NodeAffinity nodeAffinity = new NodeAffinity();

            nodeAffinity.setRequiredDuringSchedulingIgnoredDuringExecution(this.requiredDuringSchedulingIgnoredDuringExecution);

            return nodeAffinity;
        }
    }
}
