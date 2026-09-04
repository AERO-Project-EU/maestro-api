package eu.orchestrator.backend.transfer;

import java.io.Serializable;

public class PolicyEngineKubernetesConfigTO implements Serializable {
    private String masterUrl;
    private String certificateAuthorityData;
    private String clientCertificateData;
    private String clientKeyData;
    private String configMapNamespace;
    private  String configMapName;

    public PolicyEngineKubernetesConfigTO() {}

    public String getMasterUrl() {
        return masterUrl;
    }

    public void setMasterUrl(String masterUrl) {
        this.masterUrl = masterUrl;
    }

    public String getCertificateAuthorityData() {
        return certificateAuthorityData;
    }

    public void setCertificateAuthorityData(String certificateAuthorityData) {
        this.certificateAuthorityData = certificateAuthorityData;
    }

    public String getClientCertificateData() {
        return clientCertificateData;
    }

    public void setClientCertificateData(String clientCertificateData) {
        this.clientCertificateData = clientCertificateData;
    }

    public String getClientKeyData() {
        return clientKeyData;
    }

    public void setClientKeyData(String clientKeyData) {
        this.clientKeyData = clientKeyData;
    }

    public String getConfigMapNamespace() {
        return configMapNamespace;
    }

    public void setConfigMapNamespace(String configMapNamespace) {
        this.configMapNamespace = configMapNamespace;
    }

    public String getConfigMapName() {
        return configMapName;
    }

    public void setConfigMapName(String configMapName) {
        this.configMapName = configMapName;
    }
}
