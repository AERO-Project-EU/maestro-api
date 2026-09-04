package eu.orchestrator.metric.dto.response;

import java.io.Serializable;

public class MetricResponse implements Serializable {

    private String name;
    private String description;
    private String units;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }
}
