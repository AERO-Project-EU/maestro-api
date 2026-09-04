package eu.orchestrator.transfer.entities.rainbowk8s;

import java.io.Serializable;
import java.util.List;

public class DisjunctList implements Serializable {

    private List<Disjunct> disjuncts;

    public List<Disjunct> getDisjuncts() {
        return disjuncts;
    }

    public void setDisjuncts(List<Disjunct> disjuncts) {
        this.disjuncts = disjuncts;
    }

}
