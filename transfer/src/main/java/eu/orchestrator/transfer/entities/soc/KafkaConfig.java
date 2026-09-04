package eu.orchestrator.transfer.entities.soc;

import java.io.Serializable;

public class KafkaConfig implements Serializable {

  private String kafkaHost;
  private String kafkaKSQLPort;
  private String kafkaBrokerPort;

  public String getKafkaHost() {
    return kafkaHost;
  }

  public void setKafkaHost(String kafkaHost) {
    this.kafkaHost = kafkaHost;
  }

  public String getKafkaKSQLPort() {
    return kafkaKSQLPort;
  }

  public void setKafkaKSQLPort(String kafkaKSQLPort) {
    this.kafkaKSQLPort = kafkaKSQLPort;
  }

  public String getKafkaBrokerPort() {
    return kafkaBrokerPort;
  }

  public void setKafkaBrokerPort(String kafkaBrokerPort) {
    this.kafkaBrokerPort = kafkaBrokerPort;
  }
}
