package eu.orchestrator.transfer.entities.iotstack;

import java.io.Serializable;

/**
 * @author Konstantinos Theodosiou
 */
public class IoTRemoveInstance implements Serializable {

    // Machine id returned by the provider
    private String id;

    // Credentials
    private Credentials credentials;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }
}
