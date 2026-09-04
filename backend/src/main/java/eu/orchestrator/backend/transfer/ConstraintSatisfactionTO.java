package eu.orchestrator.backend.transfer;

import java.io.Serializable;
import java.util.List;

public class ConstraintSatisfactionTO implements Serializable {

    String category;
    List<String> subConstraints;
    String type;
    String satisfied;

    public ConstraintSatisfactionTO() {
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getSubConstraints() {
        return subConstraints;
    }

    public void setSubConstraints(List<String> subConstraints) {
        this.subConstraints = subConstraints;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSatisfied() {
        return satisfied;
    }

    public void setSatisfied(String satisfied) {
        this.satisfied = satisfied;
    }
}
