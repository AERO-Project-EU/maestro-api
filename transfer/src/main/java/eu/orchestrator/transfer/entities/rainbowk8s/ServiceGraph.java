package eu.orchestrator.transfer.entities.rainbowk8s;

import java.io.Serializable;
import java.util.Map;

public class ServiceGraph implements Serializable {

    private String apiVersion;
    private String kind;
    private Map<String, String> metadata;
    private Spec spec;


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

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public Spec getSpec() {
        return spec;
    }

    public void setSpec(Spec spec) {
        this.spec = spec;
    }


    public static class Builder {
        private String apiVersion;
        private Map<String, String> metadata;
        private Spec spec;

        public ServiceGraph.Builder withApiVersion(String apiVersion) {
            this.apiVersion = apiVersion;
            return this;
        }

        public ServiceGraph.Builder withMetadata(Map<String, String> metadata) {
            this.metadata = metadata;
            return this;
        }

        public ServiceGraph.Builder withSpec(Spec spec) {
            this.spec = spec;
            return this;
        }

        public ServiceGraph build() {
            ServiceGraph serviceGraph = new ServiceGraph();

            serviceGraph.setApiVersion(this.apiVersion);
            serviceGraph.setKind("ServiceGraph");
            serviceGraph.setMetadata(this.metadata);
            serviceGraph.setSpec(this.spec);

            return serviceGraph;
        }
    }

}
