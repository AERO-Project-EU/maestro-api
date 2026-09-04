package eu.orchestrator.transfer.entities.rainbowk8s;

import java.io.Serializable;

public class Link implements Serializable {

    private String source;
    private String target;
    private QosRequirements qosRequirements;


    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public QosRequirements getQosRequirements() {
        return qosRequirements;
    }

    public void setQosRequirements(QosRequirements qosRequirements) {
        this.qosRequirements = qosRequirements;
    }


    public static class Builder {
        private String source;
        private String target;
        private QosRequirements qosRequirements;

        public Link.Builder withSource(String source) {
            this.source = source;
            return this;
        }

        public Link.Builder withTarget(String target) {
            this.target = target;
            return this;
        }

        public Link.Builder withQosRequirements(QosRequirements qosRequirements) {
            this.qosRequirements = qosRequirements;
            return this;
        }

        public Link build() {
            Link link = new Link();

            link.setSource(this.source);
            link.setTarget(this.target);
            link.setQosRequirements(this.qosRequirements);

            return link;
        }
    }
}
