package eu.orchestrator.elasticity.dto;

import java.io.Serializable;

public class OrClauseTo implements Serializable {

    private String computationName;

    private Integer targetValue;

    private Integer tolerance;

    private boolean higherIsBetter;

    public String getComputationName() {
        return computationName;
    }

    public void setComputationName(String computationName) {
        this.computationName = computationName;
    }

    public Integer getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(Integer targetValue) {
        this.targetValue = targetValue;
    }

    public Integer getTolerance() {
        return tolerance;
    }

    public void setTolerance(Integer tolerance) {
        this.tolerance = tolerance;
    }

    public boolean isHigherIsBetter() {
        return higherIsBetter;
    }

    public void setHigherIsBetter(boolean higherIsBetter) {
        this.higherIsBetter = higherIsBetter;
    }
}
