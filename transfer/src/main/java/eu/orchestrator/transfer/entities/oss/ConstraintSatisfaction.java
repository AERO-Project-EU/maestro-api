package eu.orchestrator.transfer.entities.oss;

import java.io.Serializable;

public class ConstraintSatisfaction implements Serializable {

    private String constraintID;
    private boolean satisfied;
    private String constraintType;

    public ConstraintSatisfaction() {
    }

    public String getConstraintID() {
        return constraintID;
    }

    public void setConstraintID(String constraintID) {
        this.constraintID = constraintID;
    }

    public boolean isSatisfied() {
        return satisfied;
    }

    public void setSatisfied(boolean satisfied) {
        this.satisfied = satisfied;
    }

    public String getConstraintType() {
        return constraintType;
    }

    public void setConstraintType(String constraintType) {
        this.constraintType = constraintType;
    }
}
