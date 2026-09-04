package eu.orchestrator.policy.engine.message.broker.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;
import java.util.logging.Logger;

/**
 * @author Panagiotis Parthenis
 */
public class TopicProducer {

  private static final Logger LOGGER = Logger.getLogger(TopicProducer.class.getName());

  /**
   * This method push into kafka topic the proposal object for orchestrator
   *
   * @param topicName
   * @param message
   * @param kafkaUrl
   * @param kafkaPort
   */
  public static void pushIntoKafkaTopic(String topicName, String message, String kafkaUrl, String kafkaPort) {
    // kafca producer configurations
    Properties props = new Properties();
    props.put("bootstrap.servers", kafkaUrl + ":" + kafkaPort);
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    props.put("transactional.id", "test-transactional-id");
    Producer<String, String> producer = new KafkaProducer<>(props);

    producer.initTransactions();
    try {
      producer.beginTransaction();
      producer.send(new ProducerRecord<String, String>(topicName, "2", message)).get();
      producer.commitTransaction();

      LOGGER.info("just push into: "+ topicName);
    } catch (Exception ex) {
      ex.printStackTrace(System.out);
    } finally {
      producer.close();
    }
  }

}
