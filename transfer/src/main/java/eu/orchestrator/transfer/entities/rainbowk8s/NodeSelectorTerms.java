package eu.orchestrator.transfer.entities.rainbowk8s;

import java.io.Serializable;
import java.util.Set;

public class NodeSelectorTerms implements Serializable {

    private Set<KeyOperatorPair> matchExpressions;

    public Set<KeyOperatorPair> getMatchExpressions() {
        return matchExpressions;
    }

    public void setMatchExpressions(Set<KeyOperatorPair> matchExpressions) {
        this.matchExpressions = matchExpressions;
    }
}
