package eu.orchestrator.transfer.entities.rainbowk8s;

import java.io.Serializable;

public class QosRequirements implements Serializable {

    private LinkType linkType;
    private Latency latency;


    public LinkType getLinkType() {
        return linkType;
    }

    public void setLinkType(LinkType linkType) {
        this.linkType = linkType;
    }

    public Latency getLatency() {
        return latency;
    }

    public void setLatency(Latency latency) {
        this.latency = latency;
    }
}
