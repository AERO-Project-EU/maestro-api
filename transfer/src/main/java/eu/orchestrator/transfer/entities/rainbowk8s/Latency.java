package eu.orchestrator.transfer.entities.rainbowk8s;

import java.io.Serializable;

public class Latency implements Serializable {

    private int maxPacketDelayMsec;


    public int getMaxPacketDelayMsec() {
        return maxPacketDelayMsec;
    }

    public void setMaxPacketDelayMsec(int maxPacketDelayMsec) {
        this.maxPacketDelayMsec = maxPacketDelayMsec;
    }
}
