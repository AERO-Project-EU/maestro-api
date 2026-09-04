package eu.orchestrator.metric.exporter.model;

public class InstanceDetails {

    private String url;

    private String type;

    private String componentName;

    private String componentNodeInstanceHexId;

    private String componentNodeHexId;

    private String applicationInstanceHexId;

    private String applicationHexId;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
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
