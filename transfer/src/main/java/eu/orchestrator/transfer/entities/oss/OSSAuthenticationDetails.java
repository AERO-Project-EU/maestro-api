package eu.orchestrator.transfer.entities.oss;

import java.io.Serializable;

public class OSSAuthenticationDetails implements Serializable {

    private String clientToken;
    private String clientKey;

    public OSSAuthenticationDetails() {
    }

    public String getClientToken() {
        return clientToken;
    }

    public void setClientToken(String clientToken) {
        this.clientToken = clientToken;
    }

    public String getClientKey() {
        return clientKey;
    }

    public void setClientKey(String clientKey) {
        this.clientKey = clientKey;
    }
}
