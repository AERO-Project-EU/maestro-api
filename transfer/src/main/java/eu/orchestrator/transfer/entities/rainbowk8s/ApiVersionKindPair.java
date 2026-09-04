package eu.orchestrator.transfer.entities.rainbowk8s;

import java.io.Serializable;

public class ApiVersionKindPair implements Serializable {

    private String apiVersion;
    private String kind;

    public ApiVersionKindPair() {

    }

    public ApiVersionKindPair(String apiVersion, String kind) {
        this.apiVersion = apiVersion;
        this.kind = kind;
    }


    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }
}
