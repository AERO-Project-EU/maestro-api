package eu.orchestrator.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author Konstantinos Theodosiou.
 * @email konstheodosiou@gmail.com
 * @date 4/5/20
 */

@Configuration
public class KafkaTopicConfig {

    @Value("${kafka.server.url}")
    private String kafkaServer;

    @Value("${kafka.group}")
    private String kafkaGroup;

    @Value("${kafka.topic.reporting}")
    private String kafkaReportingTopic;

    @Value("${kafka.topic.agent-ids-alerting}")
    private String agentIdsAlertTopic;

    @Value("${kafka.topic.agent-ids-configuration}")
    private String agentIdsConfigTopic;

    @Value("${kafka.topic.agent-ips-configuration}")
    private String agentIpsConfigTopic;

    @Value("${kafka.topic.agent-security-configuration}")
    private String agentSecurityConfigTopic;

    @Value("${kafka.topic.agent-security-configuration-result}")
    private String agentSecurityConfigResultTopic;

    @Value("${kafka.topic.agent-soc-configuration}")
    private String agentSocConfigTopic;

    @Value("${kafka.topic.orchestrator-security-configuration-results}")
    private String orchestratorSecurityConfigResultTopic;

    @Value("${kafka.topic.soc-policies}")
    private String socConfigTopic;

    @Value("${kafka.topic.orchestrator}")
    private String orchestratorRequestsConfigTopic;

    @Value("${kafka.topic.policy-engine-info-msgs}")
    private String policyEngineInfoConfigTopic;

    @Value("${kafka.topic.orchestrator-deployment-topology}")
    private String orchestratorDeploymentConfigTopic;

    @Value("${kafka.topic.external-orchestrator-deployment-topology}")
    private String orchestratorExternalDeploymentConfigTopic;

    public String getKafkaServer() {
        return kafkaServer;
    }

    public void setKafkaServer(String kafkaServer) {
        this.kafkaServer = kafkaServer;
    }

    public String getKafkaGroup() {
        return kafkaGroup;
    }

    public void setKafkaGroup(String kafkaGroup) {
        this.kafkaGroup = kafkaGroup;
    }

    public String getKafkaReportingTopic() {
        return kafkaReportingTopic;
    }

    public void setKafkaReportingTopic(String kafkaReportingTopic) {
        this.kafkaReportingTopic = kafkaReportingTopic;
    }

    public String getAgentIdsAlertTopic() {
        return agentIdsAlertTopic;
    }

    public void setAgentIdsAlertTopic(String agentIdsAlertTopic) {
        this.agentIdsAlertTopic = agentIdsAlertTopic;
    }

    public String getAgentIdsConfigTopic() {
        return agentIdsConfigTopic;
    }

    public void setAgentIdsConfigTopic(String agentIdsConfigTopic) {
        this.agentIdsConfigTopic = agentIdsConfigTopic;
    }

    public String getAgentIpsConfigTopic() {
        return agentIpsConfigTopic;
    }

    public void setAgentIpsConfigTopic(String agentIpsConfigTopic) {
        this.agentIpsConfigTopic = agentIpsConfigTopic;
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

    public String getSocConfigTopic() {
        return socConfigTopic;
    }

    public void setSocConfigTopic(String socConfigTopic) {
        this.socConfigTopic = socConfigTopic;
    }

    public String getOrchestratorRequestsConfigTopic() {
        return orchestratorRequestsConfigTopic;
    }

    public void setOrchestratorRequestsConfigTopic(String orchestratorRequestsConfigTopic) {
        this.orchestratorRequestsConfigTopic = orchestratorRequestsConfigTopic;
    }

    public String getPolicyEngineInfoConfigTopic() {
        return policyEngineInfoConfigTopic;
    }

    public void setPolicyEngineInfoConfigTopic(String policyEngineInfoConfigTopic) {
        this.policyEngineInfoConfigTopic = policyEngineInfoConfigTopic;
    }

    public String getOrchestratorDeploymentConfigTopic() {
        return orchestratorDeploymentConfigTopic;
    }

    public void setOrchestratorDeploymentConfigTopic(String orchestratorDeploymentConfigTopic) {
        this.orchestratorDeploymentConfigTopic = orchestratorDeploymentConfigTopic;
    }

    public String getOrchestratorExternalDeploymentConfigTopic() {
        return orchestratorExternalDeploymentConfigTopic;
    }

    public void setOrchestratorExternalDeploymentConfigTopic(String orchestratorExternalDeploymentConfigTopic) {
        this.orchestratorExternalDeploymentConfigTopic = orchestratorExternalDeploymentConfigTopic;
    }
}
