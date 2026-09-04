package eu.orchestrator.backend.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class Sender {

    private static final Logger logger = Logger.getLogger(Sender.class.getName());

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.topic.elasticity-policies}")
    private String elasticityPoliciesTopic;

    @Value("${kafka.topic.soc-policies}")
    private String socPoliciesTopic;

    @Value("${kafka.topic.agent-ips-configuration}")
    private String securityPoliciesTopic;

    @Value("${kafka.topic.agent-ids-configuration}")
    private String agentIDSConfigurationTopic;

    @Value("${kafka.topic.agent-security-configuration}")
    private String agentSecurityConfigurationTopic;

    @Value("${kafka.topic.orchestrator}")
    private String orchestratorTopic;

    @Value("${kafka.topic.orchestrator-deployment-topology}")
    private String orchestratorDeploymentTopology;

    @Value("${kafka.topic.external-orchestrator-deployment-topology}")
    private String externalOrchestratorDeploymentTopology;

    @Value("${kafka.topic.application-instance-ip-topic}")
    private String applicationInstanceIpTopic;

    public void sendElasticityPolicy(String message) {
        logger.info("Sending message: '" + message + "' to topic: '" + elasticityPoliciesTopic + "'");
        kafkaTemplate.send(elasticityPoliciesTopic, "5", message);
    }

    public void sendSecurityPolicy(String message) {
        logger.info("Sending message: '" + message + "' to topic: '" + securityPoliciesTopic + "'");
        kafkaTemplate.send(securityPoliciesTopic, "5", message);
    }

    public void sendSocPolicy(String message) {
        logger.info("Sending message: '" + message + "' to topic: '" + socPoliciesTopic + "'");
        kafkaTemplate.send(socPoliciesTopic, "5", message);
    }

    public void sendToOrchestrator(String message) {
        logger.info("Sending message: '" + message + "' to topic: '" + orchestratorTopic + "'");
        kafkaTemplate.send(orchestratorTopic, "5", message);
    }

    public void sendIDRulesToAgent(String message) {
        logger
                .info("Sending message: '" + message + "' to topic: '" + agentIDSConfigurationTopic + "'");
        kafkaTemplate.send(agentIDSConfigurationTopic, "5", message);
    }

    public void sendSecurityConfigurationToAgent(String message) {
        logger
                .info("Sending message: '" + message + "' to topic: '" + agentSecurityConfigurationTopic + "'");
        kafkaTemplate.send(agentSecurityConfigurationTopic, "5", message);
    }

    public void sendOrchestratorDeploymentTopology(String message) {
        logger
                .info("Sending message: '" + message + "' to topic: '" + orchestratorDeploymentTopology + "'");
        kafkaTemplate.send(orchestratorDeploymentTopology, "5", message);
    }

    public void sendExternalOrchestratorDeploymentTopology(String message) {
        logger
                .info("Sending message: '" + message + "' to topic: '" + externalOrchestratorDeploymentTopology + "'");
        kafkaTemplate.send(externalOrchestratorDeploymentTopology, "5", message);
    }

    public void sendApplicationInstanceIPs(String message) {
        logger.info("Sending message: '" + message + "' to topic: '" + applicationInstanceIpTopic + "'");
        kafkaTemplate.send(applicationInstanceIpTopic, "5", message);
    }
}
