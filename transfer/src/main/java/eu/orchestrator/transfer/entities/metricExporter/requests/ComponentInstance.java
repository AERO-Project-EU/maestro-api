package eu.orchestrator.transfer.entities.metricExporter.requests;

import java.io.Serializable;

public class ComponentInstance implements Serializable {

    private String name;

    private String componentNodeInstanceHexId;

    private String componentNodeHexId;

    private String applicationInstanceHexId;

    private String applicationHexId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComponentNodeInstanceHexId() {
        return componentNodeInstanceHexId;
    }

    public void setComponentNodeInstanceHexId(String componentNodeInstanceHexId) {
        this.componentNodeInstanceHexId = componentNodeInstanceHexId;
    }

    public String getComponentNodeHexId() {
        return componentNodeHexId;
    }

    public void setComponentNodeHexId(String componentNodeHexId) {
        this.componentNodeHexId = componentNodeHexId;
    }

    public String getApplicationInstanceHexId() {
        return applicationInstanceHexId;
    }

    public void setApplicationInstanceHexId(String applicationInstanceHexId) {
        this.applicationInstanceHexId = applicationInstanceHexId;
    }

    public String getApplicationHexId() {
        return applicationHexId;
    }

    public void setApplicationHexId(String applicationHexId) {
        this.applicationHexId = applicationHexId;
    }
}
