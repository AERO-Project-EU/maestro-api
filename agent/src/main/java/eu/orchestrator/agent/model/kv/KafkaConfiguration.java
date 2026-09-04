package eu.orchestrator.agent.model.kv;

/**
 * @author Panagiotis Parthenis.
 */
public class KafkaConfiguration {

    private String kafkaServer;
    private String kafkaServerSoc;
    private String topicNameIps;
    private String topicNameIds;
    private String topicNameIdsAlert;
    private String securityConfigTopic;
    private String securityConfigResultTopic;
    private String socConfigTopic;

    public String getKafkaServer() {
        return kafkaServer;
    }

    public void setKafkaServer(String kafkaServer) {
        this.kafkaServer = kafkaServer;
    }

    public String getKafkaServerSoc() {
        return kafkaServerSoc;
    }

    public void setKafkaServerSoc(String kafkaServerSoc) {
        this.kafkaServerSoc = kafkaServerSoc;
    }

    public String getTopicNameIps() {
        return topicNameIps;
    }

    public void setTopicNameIps(String topicNameIps) {
        this.topicNameIps = topicNameIps;
    }

    public String getTopicNameIds() {
        return topicNameIds;
    }

    public void setTopicNameIds(String topicNameIds) {
        this.topicNameIds = topicNameIds;
    }

    public String getTopicNameIdsAlert() {
        return topicNameIdsAlert;
    }

    public void setTopicNameIdsAlert(String topicNameIdsAlert) {
        this.topicNameIdsAlert = topicNameIdsAlert;
    }

    public String getSecurityConfigTopic() {
        return securityConfigTopic;
    }

    public void setSecurityConfigTopic(String securityConfigTopic) {
        this.securityConfigTopic = securityConfigTopic;
    }

    public String getSecurityConfigResultTopic() {
        return securityConfigResultTopic;
    }

    public void setSecurityConfigResultTopic(String securityConfigResultTopic) {
        this.securityConfigResultTopic = securityConfigResultTopic;
    }

    public String getSocConfigTopic() {
        return socConfigTopic;
    }

    public void setSocConfigTopic(String socConfigTopic) {
        this.socConfigTopic = socConfigTopic;
    }
}
