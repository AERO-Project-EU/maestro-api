package eu.orchestrator.agent.util;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Panagiotis Parthenis.
 */
public class TopicProducer {

    private static final Logger LOGGER = Logger.getLogger(TopicProducer.class.getName());

    private TopicProducer() {

    }

    /**
     * This method push into kafka topic the proposal object for orchestrator.
     */
    public static void pushIntoKafkaTopic(String topicName, String message, String kafkaServer) {

        boolean retry = true;
        int timeRetries = 10;
        while (retry && timeRetries > 0) {
            // kafka producer configurations
            Properties props = new Properties();
            props.put("bootstrap.servers", kafkaServer);
            props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            long seconds = System.currentTimeMillis() / 1000L;
            props.put("transactional.id", "test-transactional-id" + seconds);
            Producer<String, String> producer = new KafkaProducer<>(props);
            System.out.println("Message to producer: " + message);
            producer.initTransactions();

            timeRetries -= 1;
            try {
                producer.beginTransaction();
                producer.send(new ProducerRecord<String, String>(topicName, "1", message)).get();
                producer.commitTransaction();
                retry = false;
            } catch (Exception exception) {
                LOGGER.log(Level.SEVERE, "Exception on push alert: {0}", exception);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            } finally {
                producer.close();
            }
        }
    }

}
