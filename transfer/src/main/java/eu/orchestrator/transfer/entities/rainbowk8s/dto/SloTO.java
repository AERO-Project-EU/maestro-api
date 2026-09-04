package eu.orchestrator.transfer.entities.rainbowk8s.dto;

import java.io.Serializable;

public class SloTO implements Serializable {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
