package eu.orchestrator.agent;

import eu.orchestrator.agent.model.DockerConfigurationForHashing;
import eu.orchestrator.agent.model.topics.SecurityConfiguration;
import eu.orchestrator.agent.model.topics.SecurityConfiguration.SecurityConfigurationType;
import eu.orchestrator.agent.service.ForensicService;
import eu.orchestrator.agent.service.IntegrityService;

import com.ecwid.consul.v1.ConsulClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Panagiotis Parthenis.
 */
public class SecurityConfigurationExecution extends Thread {

    //Arguments
    private String graphHexId;
    private String graphInstanceHexId;
    private String componentNodeHexId;
    private String componentNodeInstanceHexId;
    private String kafkaServer;
    private String topicName;
    private String outputTopicName;
    private DockerConfigurationForHashing dockerConfigurationForHashing;
    private ConsulClient consulClient;

    //LOGGER
    private static final Logger LOGGER = Logger.getLogger(SecurityConfigurationExecution.class.getName());

    public SecurityConfigurationExecution(String graphHexId, String graphInstanceHexId, String componentNodeHexId, String componentNodeInstanceHexId,
            String kafkaServer, String topicName, String outputTopicName, DockerConfigurationForHashing dockerConfigurationForHashing,
            ConsulClient consulClient) {
        this.graphHexId = graphHexId;
        this.graphInstanceHexId = graphInstanceHexId;
        this.componentNodeHexId = componentNodeHexId;
        this.componentNodeInstanceHexId = componentNodeInstanceHexId;
        this.kafkaServer = kafkaServer;
        this.topicName = topicName;
        this.outputTopicName = outputTopicName;
        this.dockerConfigurationForHashing = dockerConfigurationForHashing;
        this.consulClient = consulClient;
    }

    @Override
    public void run() {
        LOGGER.log(Level.INFO, "Security Configuration thread started !!!");

        Properties props = new Properties();
        props.put("bootstrap.servers", kafkaServer);
        props.put("group.id", graphHexId + ":" + graphInstanceHexId + ":" + componentNodeHexId + ":" + componentNodeInstanceHexId);
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset", "earliest");
        props.put("enable.auto.commit", false);

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {

            consumer.subscribe(Arrays.asList(topicName));

            ObjectMapper mapper = new ObjectMapper();
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(10000);
                for (ConsumerRecord<String, String> record : records) {
                    try {
                        SecurityConfiguration securityConfiguration = mapper.readValue(record.value(), SecurityConfiguration.class);

                        if ((securityConfiguration.getGraphHexId().compareTo(graphHexId) == 0)
                                && (securityConfiguration.getGraphInstanceHexId().compareTo(graphInstanceHexId) == 0)
                                && (securityConfiguration.getComponentNodeHexId().compareTo(componentNodeHexId) == 0)
                                && (securityConfiguration.getComponentNodeInstanceHexId().compareTo(componentNodeInstanceHexId) == 0)) {

                            if (securityConfiguration.getSecurityConfigurationType().name()
                                    .compareTo(SecurityConfigurationType.CONFIGURATION_INTEGRITY_VERIFICATION.name()) == 0
                                    || securityConfiguration.getSecurityConfigurationType().name()
                                    .compareTo(SecurityConfigurationType.RUNTIME_FILE_INTEGRITY.name()) == 0) {
                                LOGGER.log(Level.INFO,
                                        "i handle : " + securityConfiguration.getSecurityConfigurationType().name());

                                IntegrityService integrityService = new IntegrityService(kafkaServer, outputTopicName,
                                        securityConfiguration, dockerConfigurationForHashing);

                            } else if (securityConfiguration.getSecurityConfigurationType().name()
                                    .compareTo(SecurityConfigurationType.FORENSIC_ACTIVATION.name()) == 0) {

                                LOGGER.log(Level.INFO, "i handle : " + securityConfiguration.getSecurityConfigurationType().name());

                                ForensicService forensicServiceCurr = new ForensicService(graphHexId, graphInstanceHexId, componentNodeHexId,
                                        componentNodeInstanceHexId, consulClient);
                                forensicServiceCurr.run();
                            }
                        }
                        consumer.commitSync();
                    } catch (IOException exception) {
                        LOGGER.log(Level.SEVERE, "IOException of retrieve topic object: {0}", exception);

                    } catch (ArrayIndexOutOfBoundsException exception) {
                        LOGGER.log(Level.SEVERE, "Error on id field: {0}" + exception);

                    }
                }
            }
        }

    }
}
