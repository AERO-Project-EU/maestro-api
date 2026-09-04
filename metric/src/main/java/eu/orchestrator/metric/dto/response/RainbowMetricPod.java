package eu.orchestrator.metric.dto.response;

import java.io.Serializable;

public class RainbowMetricPod implements Serializable {

    private String uuid;
    private String name;
    private String namespace;

    
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
