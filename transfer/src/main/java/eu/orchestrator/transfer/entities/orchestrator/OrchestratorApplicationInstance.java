package eu.orchestrator.transfer.entities.orchestrator;

import java.io.Serializable;
import java.util.List;

public class OrchestratorApplicationInstance implements Serializable {

    private String graphID;
    private String graphHexID;
    private String graphName;
    private String graphInstanceID;
    private String graphInstanceHexID;
    private String graphInstanceName;
    private Boolean ipv6Enabled;
    private Boolean telco5GEnabled;
    private String callbackURL;
    private OrchestratorKafkaConfig orchestratorKafkaConfig;
    private List<OrchestratorProviderAuthenticationDetails> providerAuthenticationDetails;
    private List<OrchestratorComponentNodeInstance> services;

    public OrchestratorApplicationInstance() {
        this.telco5GEnabled = false;
    }

    public String getGraphID() {
        return graphID;
    }

    public void setGraphID(String graphID) {
        this.graphID = graphID;
    }

    public String getGraphHexID() {
        return graphHexID;
    }

    public void setGraphHexID(String graphHexID) {
        this.graphHexID = graphHexID;
    }

    public String getGraphName() {
        return graphName;
    }

    public void setGraphName(String graphName) {
        this.graphName = graphName;
    }

    public String getGraphInstanceID() {
        return graphInstanceID;
    }

    public void setGraphInstanceID(String graphInstanceID) {
        this.graphInstanceID = graphInstanceID;
    }

    public String getGraphInstanceHexID() {
        return graphInstanceHexID;
    }

    public void setGraphInstanceHexID(String graphInstanceHexID) {
        this.graphInstanceHexID = graphInstanceHexID;
    }

    public String getGraphInstanceName() {
        return graphInstanceName;
    }

    public void setGraphInstanceName(String graphInstanceName) {
        this.graphInstanceName = graphInstanceName;
    }

    public Boolean getIpv6Enabled() {
        return ipv6Enabled;
    }

    public void setIpv6Enabled(Boolean ipv6Enabled) {
        this.ipv6Enabled = ipv6Enabled;
    }

    public Boolean getTelco5GEnabled() {
        return telco5GEnabled;
    }

    public void setTelco5GEnabled(Boolean telco5GEnabled) {
        this.telco5GEnabled = telco5GEnabled;
    }

    public String getCallbackURL() {
        return callbackURL;
    }

    public void setCallbackURL(String callbackURL) {
        this.callbackURL = callbackURL;
    }

    public OrchestratorKafkaConfig getOrchestratorKafkaConfig() {
        return orchestratorKafkaConfig;
    }

    public void setOrchestratorKafkaConfig(OrchestratorKafkaConfig orchestratorKafkaConfig) {
        this.orchestratorKafkaConfig = orchestratorKafkaConfig;
    }

    public List<OrchestratorProviderAuthenticationDetails> getProviderAuthenticationDetails() {
        return providerAuthenticationDetails;
    }

    public void setProviderAuthenticationDetails(
            List<OrchestratorProviderAuthenticationDetails> providerAuthenticationDetails) {
        this.providerAuthenticationDetails = providerAuthenticationDetails;
    }

    public List<OrchestratorComponentNodeInstance> getServices() {
        return services;
    }

    public void setServices(List<OrchestratorComponentNodeInstance> services) {
        this.services = services;
    }
}
