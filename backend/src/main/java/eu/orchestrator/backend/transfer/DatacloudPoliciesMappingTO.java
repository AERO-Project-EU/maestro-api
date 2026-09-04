package eu.orchestrator.backend.transfer;

import java.io.Serializable;

public class DatacloudPoliciesMappingTO implements Serializable {

    private String k8sNamespace;
    private String k8sAppName;

    public String getK8sNamespace() {
        return k8sNamespace;
    }

    public void setK8sNamespace(String k8sNamespace) {
        this.k8sNamespace = k8sNamespace;
    }

    public String getK8sAppName() {
        return k8sAppName;
    }

    public void setK8sAppName(String k8sAppName) {
        this.k8sAppName = k8sAppName;
    }
}
