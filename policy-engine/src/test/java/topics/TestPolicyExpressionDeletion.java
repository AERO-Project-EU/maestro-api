package topics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.orchestrator.transfer.entities.policyEngine.PolicyModel;
import eu.orchestrator.transfer.entities.policyEngine.PolicyType;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

/**
 * @author Panagiotis Parthenis
 */

public class TestPolicyExpressionDeletion {

  public static void main(String[] args) {

    //String topicName = "prometheus-results";
    pushIntoPolicyExpression();

  }

  private static void pushIntoPolicyExpression() {
    String topicName = "policy-expression-cruder";

    // kafka topic configuration
    Properties props = new Properties();
    props.put("bootstrap.servers", "localhost:9092");
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    props.put("transactional.id", "test-transactional-id");
    Producer<String, String> producer = new KafkaProducer<>(props);

    //create policy model
    PolicyModel policyModel = new PolicyModel();
    policyModel.setPolicyHexID("w8bOIMPtM2");
    policyModel.setPolicyName("Some Rule");
    policyModel.setGraphHexID("f00sFvg7rO");
    policyModel.setGraphInstanceHexID("YUnuH7tm3t");
    policyModel.setCreation(false);

    ObjectMapper mapper = new ObjectMapper();
    String jsonInString = "";
    try {
      jsonInString = mapper.writeValueAsString(policyModel);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }

    System.out.println();
    int key = (int) Math.random();

    // push transactional into kafka topic
    producer.initTransactions();
    try {
      producer.beginTransaction();
      producer.send(new ProducerRecord<String, String>(topicName, key + "", jsonInString)).get();
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
