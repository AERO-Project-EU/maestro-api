package eu.orchestrator.backend.transfer;

import java.io.Serializable;
import java.util.List;

public class ApplicationInstanceComponentsTO implements Serializable {

    private Long id;
    private String name;
    private List<ComponentIPTO> components;

    public ApplicationInstanceComponentsTO() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ComponentIPTO> getComponents() {
        return components;
    }

    public void setComponents(List<ComponentIPTO> components) {
        this.components = components;
    }

}
