package eu.orchestrator.transfer.entities.agent;

/**
 * @author Konstantinos Theodosiou.
 */
public class IDSAlert {
    
    String graphHexId;
    String graphInstanceHexId;
    String componentNodeHexId;
    String componentNodeInstanceHexId;

    String alert;

    public IDSAlert() {
    }

    public String getGraphHexId() {
        return graphHexId;
    }

    public void setGraphHexId(String graphHexId) {
        this.graphHexId = graphHexId;
    }

    public String getGraphInstanceHexId() {
        return graphInstanceHexId;
    }

    public void setGraphInstanceHexId(String graphInstanceHexId) {
        this.graphInstanceHexId = graphInstanceHexId;
    }

    public String getComponentNodeHexId() {
        return componentNodeHexId;
    }

    public void setComponentNodeHexId(String componentNodeHexId) {
        this.componentNodeHexId = componentNodeHexId;
    }

    public String getComponentNodeInstanceHexId() {
        return componentNodeInstanceHexId;
    }

    public void setComponentNodeInstanceHexId(String componentNodeInstanceHexId) {
        this.componentNodeInstanceHexId = componentNodeInstanceHexId;
    }

    public String getAlert() {
        return alert;
    }

    public void setAlert(String alert) {
        this.alert = alert;
    }
}
