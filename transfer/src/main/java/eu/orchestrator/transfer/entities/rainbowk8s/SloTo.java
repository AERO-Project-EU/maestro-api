package eu.orchestrator.transfer.entities.rainbowk8s;

import java.io.Serializable;
import java.util.Map;

public class SloTo implements Serializable {

    private String name;
    private ApiVersionKindPair sloType;
    private ApiVersionKindPair elasticityStrategy;
    private SloConfig sloConfig;
    private Map<String, Affinity> staticElasticityStrategyConfig;
    private StabilizationWindow stabilizationWindow;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ApiVersionKindPair getSloType() {
        return sloType;
    }

    public void setSloType(ApiVersionKindPair sloType) {
        this.sloType = sloType;
    }

    public ApiVersionKindPair getElasticityStrategy() {
        return elasticityStrategy;
    }

    public void setElasticityStrategy(
            ApiVersionKindPair elasticityStrategy) {
        this.elasticityStrategy = elasticityStrategy;
    }

    public SloConfig getSloConfig() {
        return sloConfig;
    }

    public void setSloConfig(SloConfig sloConfig) {
        this.sloConfig = sloConfig;
    }

    public Map<String, Affinity> getStaticElasticityStrategyConfig() {
        return staticElasticityStrategyConfig;
    }

    public void setStaticElasticityStrategyConfig(
            Map<String, Affinity> staticElasticityStrategyConfig) {
        this.staticElasticityStrategyConfig = staticElasticityStrategyConfig;
    }

    public StabilizationWindow getStabilizationWindow() {
        return stabilizationWindow;
    }

    public void setStabilizationWindow(
            StabilizationWindow stabilizationWindow) {
        this.stabilizationWindow = stabilizationWindow;
    }
}
