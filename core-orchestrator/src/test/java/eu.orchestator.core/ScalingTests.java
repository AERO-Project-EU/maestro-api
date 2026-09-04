package eu.orchestator.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.orchestator.core.configuration.*;
import eu.orchestrator.transfer.entities.policyEngine.ActionType;
import eu.orchestrator.transfer.entities.policyEngine.TriggerActionModel;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * @author Konstantinos Theodosiou
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {eu.orchestator.core.Application.class})
public class ScalingTests {
    private String policyEngineTopic = "graph-triggered-actions";

    @Autowired
    ConsulConfig consulConfig;

    @Autowired
    IntrusionDetectionConfig idsConfig;

    @Autowired
    VirtualizationManagerConfig virtualizationManagerConfig;

    @Autowired
    KafkaConfig kafkaConfig;
    

    @Test
//    @Ignore
    public void scaleOut(){
        String bootstapServer = System.getProperty("kafka.bootstrap.servers", "localhost:9092");
        System.out.println("\n\n\n"+bootstapServer);
        Properties props = new Properties();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstapServer);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "test-transactional-id");

        Producer<String, String> producer = new KafkaProducer<>(props);

        producer.initTransactions();

        ObjectMapper objectMapper = new ObjectMapper();
        TriggerActionModel triggerActionModel = new TriggerActionModel();
        triggerActionModel.setGraphHexID("euJBhdpFAY");
        triggerActionModel.setGraphInstanceHexID("HgWFEVWGPR");
        triggerActionModel.setComponentNodeHexID("pgjs7xTHte");
        triggerActionModel.setAction(ActionType.scaleOut);
        triggerActionModel.setActionAmount(2);

        String jsonInString = "";
        try {
            jsonInString = objectMapper.writeValueAsString(triggerActionModel);
        } catch (JsonProcessingException ex) {
            System.out.println("Couldn't construct the json string of the reporting-status: " +  ex);
            return;
        }


        producer.beginTransaction();
        try {
            producer.send(new ProducerRecord<String, String>(policyEngineTopic, "0", jsonInString)).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        producer.commitTransaction();
        producer.close();
    }

    @Test
//    @Ignore
    public void scaleIn(){
        String bootstapServer = System.getProperty("kafka.bootstrap.servers", "localhost:9092");
        System.out.println("\n\n\n"+bootstapServer);
        Properties props = new Properties();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstapServer);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "konsteacd");

        Producer<String, String> producer = new KafkaProducer<>(props);

        producer.initTransactions();

        ObjectMapper objectMapper = new ObjectMapper();

        TriggerActionModel triggerActionModel = new TriggerActionModel();
        triggerActionModel.setGraphHexID("euJBhdpFAY");
        triggerActionModel.setGraphInstanceHexID("HgWFEVWGPR");
        triggerActionModel.setComponentNodeHexID("pgjs7xTHte");
        triggerActionModel.setAction(ActionType.scaleIn);
        triggerActionModel.setActionAmount(1);

        String jsonInString = "";
        try {
            jsonInString = objectMapper.writeValueAsString(triggerActionModel);
        } catch (JsonProcessingException ex) {
            System.out.println("Couldn't construct the json string of the reporting-status: " +  ex);
            return;
        }

        producer.beginTransaction();
        try {
            producer.send(new ProducerRecord<String, String>(policyEngineTopic, "0", jsonInString)).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        producer.commitTransaction();
        producer.close();
    }

}
