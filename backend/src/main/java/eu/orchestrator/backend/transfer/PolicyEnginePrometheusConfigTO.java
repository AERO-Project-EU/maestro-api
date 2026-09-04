package eu.orchestrator.backend.transfer;

import java.io.Serializable;

public class PolicyEnginePrometheusConfigTO implements Serializable {
    private String prometheusIp;
    private String prometheusPort;
    private String namespace;

    public PolicyEnginePrometheusConfigTO() {}

    public String getPrometheusIp() {
        return prometheusIp;
    }

    public void setPrometheusIp(String prometheusIp) {
        this.prometheusIp = prometheusIp;
    }

    public String getPrometheusPort() {
        return prometheusPort;
    }

    public void setPrometheusPort(String prometheusPort) {
        this.prometheusPort = prometheusPort;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
