package eu.orchestrator.transfer.entities.rainbowk8s.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RainbowMonitoringDto implements Serializable {

    private String node;
    private List<RainbowMonitoringDataDto> data;


    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public List<RainbowMonitoringDataDto> getData() {
        if (data == null) {
            data = new ArrayList<>();
        }
        return data;
    }

    public void setData(List<RainbowMonitoringDataDto> data) {
        this.data = data;
    }

}
