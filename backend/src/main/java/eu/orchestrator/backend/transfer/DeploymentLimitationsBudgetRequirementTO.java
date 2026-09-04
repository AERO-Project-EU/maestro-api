package eu.orchestrator.backend.transfer;

import java.io.Serializable;


public class DeploymentLimitationsBudgetRequirementTO implements Serializable {

    private int timePeriod;
    private int costThreshold;

    public int getTimePeriod() {
        return timePeriod;
    }

    public void setTimePeriod(int timePeriod) {
        this.timePeriod = timePeriod;
    }

    public int getCostThreshold() {
        return costThreshold;
    }

    public void setCostThreshold(int costThreshold) {
        this.costThreshold = costThreshold;
    }
}
