package eu.orchestrator.elasticity.dto;

import java.io.Serializable;
import java.util.List;

public class ComputationTo implements Serializable {

    private String name;

    private Integer every;

    private List<NestedDataTo> nestedData;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getEvery() {
        return every;
    }

    public void setEvery(Integer every) {
        this.every = every;
    }

    public List<NestedDataTo> getNestedData() {
        return nestedData;
    }

    public void setNestedData(List<NestedDataTo> nestedData) {
        this.nestedData = nestedData;
    }
}
