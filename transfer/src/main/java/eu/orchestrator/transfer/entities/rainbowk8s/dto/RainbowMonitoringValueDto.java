package eu.orchestrator.transfer.entities.rainbowk8s.dto;

import java.io.Serializable;

public class RainbowMonitoringValueDto implements Serializable {

    private String timestamp;
    private String val;


    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }
}
