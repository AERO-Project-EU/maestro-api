package eu.orchestrator.metric.dto.response;

import java.io.Serializable;

public class RainbowMetricContainer implements Serializable {

    private String id;
    private String name;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
