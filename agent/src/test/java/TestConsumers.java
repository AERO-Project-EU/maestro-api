import eu.orchestrator.agent.model.topics.ActionsCommands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

/**
 * @author Panagiotis Parthenis
 */
public class TestConsumers {

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

        ActionsCommands actionsCommands = new ActionsCommands();
        actionsCommands.setCommand("mkdir /home/ubuntu/pptest123/");
        //xy5ESdri89-nHNSGXw5TL-zAazHkjsLD-EBw5KC7Y3b
        actionsCommands.setId("xy5ESdri89:nHNSGXw5TL:zAazHkjsLD:EBw5KC7Y3b");

        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = "";
        try {
            jsonInString = mapper.writeValueAsString(actionsCommands);
        } catch (JsonProcessingException exception) {
            exception.printStackTrace();
        }

        System.out.println();
        int key = (int) Math.random();

        // push transactional into kafka topic
        producer.initTransactions();
        try {
            producer.beginTransaction();
            producer.send(new ProducerRecord<String, String>("agent-ips-configuration", key + "", jsonInString)).get();
            producer.commitTransaction();

            System.out.println("Complete");
            //System.out.println("Complete + --->" + producer.flush(););
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
        } finally {
            producer.close();
        }

    }

}
