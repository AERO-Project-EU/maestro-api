package eu.orchestrator.transfer.entities.kubernetes;

import java.io.Serializable;

public class NamespaceDto implements Serializable {

    private String namespace;


    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
