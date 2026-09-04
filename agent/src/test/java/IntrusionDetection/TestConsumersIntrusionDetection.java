package IntrusionDetection;

import eu.orchestrator.agent.model.topics.IntrusionDetectionConfiguration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Arrays;
import java.util.Properties;

/**
 * @author Panagiotis Parthenis
 */
public class TestConsumersIntrusionDetection {

    public static void main(String[] args) {

        //String topicName = "prometheus-results";
        pushIntoPolicyExpression();

    }

    private static void pushIntoPolicyExpression() {
        // kafka topic configuration
        Properties props = new Properties();
        props.put("bootstrap.servers", System.getProperty("kafka.bootstrap.servers", "localhost:9092"));
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("transactional.id", "test-transactional-id");
        Producer<String, String> producer = new KafkaProducer<>(props);

        //xy5ESdri89:nHNSGXw5TL:zAazHkjsLD:EBw5KC7Y3b

        IntrusionDetectionConfiguration intrusionDetectionConfiguration = new IntrusionDetectionConfiguration();
        intrusionDetectionConfiguration.setGraphHexId("xy5ESdri89");
        intrusionDetectionConfiguration.setGraphInstanceHexId("nHNSGXw5TL");
        intrusionDetectionConfiguration.setComponentNodeHexId("zAazHkjsLD");
        intrusionDetectionConfiguration.setComponentNodeInstanceHexId("EBw5KC7Y3b");
        intrusionDetectionConfiguration.setRules(Arrays.asList("alert icmp $EXTERNAL_NET any -> $HOME_NET any (msg:\"Pinging...\";sid:1000004;)"));

        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = "";
        try {
            jsonInString = mapper.writeValueAsString(intrusionDetectionConfiguration);
        } catch (JsonProcessingException exception) {
            exception.printStackTrace();
        }

        System.out.println();
        int key = (int) Math.random();

        // push transactional into kafka topic
        producer.initTransactions();
        try {
            producer.beginTransaction();
            producer.send(new ProducerRecord<String, String>("agent-ids-configuration", key + "", jsonInString)).get();
            producer.commitTransaction();

            System.out.println("Complete");
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
        } finally {
            producer.close();
        }

    }

}
