package eu.orchestrator.transfer.entities.rainbowk8s;

import java.io.Serializable;
import java.util.List;

public class SloTargetState implements Serializable {

    private List<DisjunctList> conjuncts;

    public List<DisjunctList> getConjuncts() {
        return conjuncts;
    }

    public void setConjuncts(List<DisjunctList> conjuncts) {
        this.conjuncts = conjuncts;
    }
}
