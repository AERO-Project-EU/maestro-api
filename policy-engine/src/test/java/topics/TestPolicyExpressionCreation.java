package topics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.orchestrator.transfer.entities.policyEngine.ActionType;
import eu.orchestrator.transfer.entities.policyEngine.DroolsAction;
import eu.orchestrator.transfer.entities.policyEngine.PolicyModel;
import eu.orchestrator.transfer.entities.policyEngine.PolicyType;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.ArrayList;
import java.util.Properties;

/**
 * @author Panagiotis Parthenis
 */
public class TestPolicyExpressionCreation {

  static int i = 1;

  public static void main(String[] args) {

    //while (true) {
      //String topicName = "prometheus-results";
      pushIntoPolicyExpression();
      i++;
    //}
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
    policyModel.setPolicyHexID("rule" + i);
    policyModel.setPolicyName("SomeRule9");
    policyModel.setGraphHexID("f00sFvg7r111111111118");
    policyModel.setGraphInstanceHexID("YUnuH7tm3t");
    policyModel.setPolicyType(PolicyType.elasticity);
    policyModel.setPrometheusPolicyExpression("alertmanager_cluster_failed_peers" + i + " > 10");
    policyModel.setPrometheusPolicyPeriod("20");
    policyModel.setDroolsInertialPeriod("4");
    policyModel.setCallbackURL("localhost:9090");
    policyModel.setCreation(true);

    ArrayList<DroolsAction> droolsActions = new ArrayList<>();

    DroolsAction droolsAction = new DroolsAction();
    droolsAction.setComponentName("SumFunc");
    droolsAction.setComponentHexID("E5MlYSExbL");
    droolsAction.setContext("1");
    droolsAction.setRuleAction(ActionType.scaleOut);

    droolsActions.add(droolsAction);

 /* Push into Rabbit MQ Queue

    DroolsAction droolsAction2 = new DroolsAction();
    droolsAction2.setComponentName("SumFunc");
    droolsAction2.setComponentHexID("E5MlYSExbL");
    droolsAction2.setContext("1");
    droolsAction2.setRuleAction(ActionType.pushIntoRabbitMQQueue);
    droolsAction2.setTopicName("random-rabbit");
    droolsAction2.setTopicPort("5672");
    droolsAction2.setTopicUrl("localhost");

    droolsActions.add(droolsAction2);*/





  /*  PolicyModel policyModel = new PolicyModel();
    policyModel.setGraphId("LambdaApp");
    policyModel.setGraphInstanceId("myTesting");
    policyModel.setPolicyType(PolicyType.elasticity);
    policyModel.setPolicyName("rule67");
    // or
    // policyModel.setPrometheusPolicyExpression("((traefik_backend_request_duration_seconds_sum{backend='SumFunc',code='200'} / traefik_backend_request_duration_seconds_count{backend='SumFunc',code='200'} < 1) or (AVG(netdata:lambdaapp:mytesting:sumfunc_cpu_cpu_percentage_average{dimension='idle'}) > 90))");
    // and
    // policyModel.setPrometheusPolicyExpression("(AVG(netdata:lambdaapp:mytesting:sumfunc_cpu_cpu_percentage_average{dimension='idle'}) >80) and (AVG(netdata:lambdaapp:mytesting:sumfunc_cpu_cpu_percentage_average{dimension='idle'}) < 99)")
    policyModel.setPrometheusPolicyPeriod("20s");
    policyModel.setDroolsInertialPeriod("4m");
    policyModel.setCreation(true);

    ArrayList<DroolsAction> droolsActions = new ArrayList<>();

    DroolsAction droolsAction = new DroolsAction();
    droolsAction.setComponentName("SumFunc");
    droolsAction.setContext("1");
    droolsAction.setRuleAction(ActionType.scaleUp);
    droolsActions.add(droolsAction);*/

    //policyModel.setDroolsActions(droolsActions);
   /* droolsAction = new DroolsAction();
    droolsAction.setComponentName("componet2");
    droolsAction.setContext("1");
    droolsAction.setRuleAction(ActionType.scaleDown);
    droolsActions.add(droolsAction);

    droolsAction = new DroolsAction();
    droolsAction.setComponentName("componet3");
    droolsAction.setContext("fdsagfdsagdsgds");
    droolsAction.setRuleAction(ActionType.info);
    droolsActions.add(droolsAction);


    droolsAction.setComponentName("componet4");
    droolsAction.setContext("this is a object into");
    droolsAction.setRuleAction(ActionType.pushIntoRabbitMQQueue);
    droolsAction.setTopicName("rabbit");
    droolsAction.setTopicPort("5672");
    droolsAction.setTopicUrl("localhost");
    droolsActions.add(droolsAction);

  *//*  DroolsAction *//*
    droolsAction = new DroolsAction();
    droolsAction.setComponentName("componet5");
    droolsAction.setContext("this is a object into");
    droolsAction.setRuleAction(ActionType.pushIntoKafkaTopic);
    droolsAction.setTopicName("sssssssssssssss");
    droolsAction.setTopicPort("9092");
    droolsAction.setTopicUrl("localhost");
    droolsActions.add(droolsAction);*/

/*
    droolsAction = new DroolsAction();
    droolsAction.setComponentName("componet3");
    droolsAction.setContext("this is a object into");
    droolsAction.setRuleAction(ActionType.pushIntoKafkaTopic);
    droolsAction.setTopicName("this-test");
    droolsAction.setTopicPort("9092");
    droolsAction.setTopicUrl("localhost");
    droolsActions.add(droolsAction);

    droolsAction = new DroolsAction();
    droolsAction.setComponentName("componet4");
    droolsAction.setRuleAction(ActionType.info);
    droolsAction.setContext("this is info action");
    droolsActions.add(droolsAction);*/

    policyModel.setDroolsActions(droolsActions);

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
