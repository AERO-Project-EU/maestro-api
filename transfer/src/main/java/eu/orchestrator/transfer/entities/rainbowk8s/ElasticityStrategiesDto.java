package eu.orchestrator.transfer.entities.rainbowk8s;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ElasticityStrategiesDto implements Serializable {

    private List<ElasticityStrategyDto> elasticityStrategies;


    public List<ElasticityStrategyDto> getElasticityStrategies() {
        if (elasticityStrategies == null) {
            elasticityStrategies = new ArrayList<>();
        }
        return elasticityStrategies;
    }

    public void setElasticityStrategies(List<ElasticityStrategyDto> elasticityStrategies) {
        this.elasticityStrategies = elasticityStrategies;
    }
}
