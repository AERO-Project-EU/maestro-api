package eu.orchestrator.elasticity.dto;

import java.io.Serializable;
import java.util.List;

public class ExpressionTo implements Serializable {

    private List<OrClauseTo> orClauses;

    public List<OrClauseTo> getOrClauses() {
        return orClauses;
    }

    public void setOrClauses(List<OrClauseTo> orClauses) {
        this.orClauses = orClauses;
    }
}
