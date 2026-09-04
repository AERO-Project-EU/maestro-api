package eu.orchestrator.transfer.entities.orchestrator;

/**
 * @author Konstantinos Theodosiou.
 * @email konstheodosiou@gmail.com
 * @date 4/5/20
 */
public class OrchestratorKafkaConfig {

    private String kafkaServer;
    private String agentIpsConfigTopic;
    private String agentIdsConfigTopic;
    private String agentIdsAlertTopic;
    private String agentSecurityConfigTopic;
    private String agentSecurityConfigResultTopic;
    private String agentSocConfigTopic;
    private String orchestratorSecurityConfigResultTopic;

    public OrchestratorKafkaConfig() {
    }

    public String getKafkaServer() {
        return kafkaServer;
    }

    public void setKafkaServer(String kafkaServer) {
        this.kafkaServer = kafkaServer;
    }

    public String getAgentIpsConfigTopic() {
        return agentIpsConfigTopic;
    }

    public void setAgentIpsConfigTopic(String agentIpsConfigTopic) {
        this.agentIpsConfigTopic = agentIpsConfigTopic;
    }

    public String getAgentIdsConfigTopic() {
        return agentIdsConfigTopic;
    }

    public void setAgentIdsConfigTopic(String agentIdsConfigTopic) {
        this.agentIdsConfigTopic = agentIdsConfigTopic;
    }

    public String getAgentIdsAlertTopic() {
        return agentIdsAlertTopic;
    }

    public void setAgentIdsAlertTopic(String agentIdsAlertTopic) {
        this.agentIdsAlertTopic = agentIdsAlertTopic;
    }

    public String getAgentSecurityConfigTopic() {
        return agentSecurityConfigTopic;
    }

    public void setAgentSecurityConfigTopic(String agentSecurityConfigTopic) {
        this.agentSecurityConfigTopic = agentSecurityConfigTopic;
    }

    public String getAgentSecurityConfigResultTopic() {
        return agentSecurityConfigResultTopic;
    }

    public void setAgentSecurityConfigResultTopic(String agentSecurityConfigResultTopic) {
        this.agentSecurityConfigResultTopic = agentSecurityConfigResultTopic;
    }

    public String getAgentSocConfigTopic() {
        return agentSocConfigTopic;
    }

    public void setAgentSocConfigTopic(String agentSocConfigTopic) {
        this.agentSocConfigTopic = agentSocConfigTopic;
    }

    public String getOrchestratorSecurityConfigResultTopic() {
        return orchestratorSecurityConfigResultTopic;
    }

    public void setOrchestratorSecurityConfigResultTopic(String orchestratorSecurityConfigResultTopic) {
        this.orchestratorSecurityConfigResultTopic = orchestratorSecurityConfigResultTopic;
    }
}
