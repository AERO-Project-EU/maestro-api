package topics;


import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * @author Panagiotis Parthenis
 */
public class TestProducer {

  private static final Logger LOGGER = Logger.getLogger(TestProducer.class.getName());

  public static void main(String[] args) {

    String topicName = "graph-triggered-actions";
    pushIntoPolicyExpression(topicName);

  }

  private static void pushIntoPolicyExpression(String topicName) {
    String group = "group1";
    Properties props = new Properties();
    props.put("bootstrap.servers", "localhost:9092");
    props.put("group.id", group);
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    props.put("auto.offset.reset", "earliest");

    // subscribe consumer into a topic
    KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
    consumer.subscribe(Arrays.asList(topicName));
    LOGGER.info("Just start consumer subscribed to topic: " + topicName);

    while (true) {
      ConsumerRecords<String, String> records = consumer.poll(10000);
      for (ConsumerRecord<String, String> record : records) {
        LOGGER.info("****** " + topicName + " ******");
        LOGGER.info("offset = " + record.offset() + " , key = " + record.key() + " , value = " + record.value());
       // consumer.commitSync();
      }
    }
  }

}
