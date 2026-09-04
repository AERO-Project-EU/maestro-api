package eu.orchestrator.transfer.entities.rainbowk8s.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RainbowMonitoringResponseDto implements Serializable {

    private List<RainbowMonitoringDto> monitoring;


    public List<RainbowMonitoringDto> getMonitoring() {
        if (monitoring == null) {
            monitoring = new ArrayList<>();
        }
        return monitoring;
    }

    public void setMonitoring(List<RainbowMonitoringDto> monitoring) {
        this.monitoring = monitoring;
    }
}
