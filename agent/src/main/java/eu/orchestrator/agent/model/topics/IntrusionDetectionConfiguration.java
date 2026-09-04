package eu.orchestrator.agent.model.topics;

import java.util.List;

/**
 * @author Konstantinos Theodosiou.
 */
public class IntrusionDetectionConfiguration {

    String graphHexId;
    String graphInstanceHexId;
    String componentNodeHexId;
    String componentNodeInstanceHexId;

    List<String> rules;

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

    public List<String> getRules() {
        return rules;
    }

    public void setRules(List<String> rules) {
        this.rules = rules;
    }

}
