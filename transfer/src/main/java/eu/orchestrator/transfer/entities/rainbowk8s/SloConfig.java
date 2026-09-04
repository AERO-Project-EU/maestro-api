package eu.orchestrator.transfer.entities.rainbowk8s;

import java.io.Serializable;
import java.util.Map;

public class SloConfig implements Serializable {

    private Map<String, String> streams;
    private Map<String, String> insights;
    private SloTargetState targetState;
    private int elasticityStrategyTolerance;

    public Map<String, String> getStreams() {
        return streams;
    }

    public void setStreams(Map<String, String> streams) {
        this.streams = streams;
    }

    public Map<String, String> getInsights() {
        return insights;
    }

    public void setInsights(Map<String, String> insights) {
        this.insights = insights;
    }

    public SloTargetState getTargetState() {
        return targetState;
    }

    public void setTargetState(SloTargetState targetState) {
        this.targetState = targetState;
    }

    public int getElasticityStrategyTolerance() {
        return elasticityStrategyTolerance;
    }

    public void setElasticityStrategyTolerance(int elasticityStrategyTolerance) {
        this.elasticityStrategyTolerance = elasticityStrategyTolerance;
    }
}
