package eu.orchestrator.agent;

import eu.orchestrator.agent.configuration.AgentConfiguration;
import eu.orchestrator.agent.model.topics.ActionsCommands;
import eu.orchestrator.agent.util.CommandLineExecutor;

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
public class IntrusionPreventionExecution extends Thread {

    //Arguments
    private String graphHexId;
    private String graphInstanceHexId;
    private String componentNodeHexId;
    private String componentNodeInstanceHexId;
    private String kafkaServer;
    private String topicName;
    private static ConsulClient consulClient;

    //LOGGER
    private static final Logger LOGGER = Logger.getLogger(IntrusionPreventionExecution.class.getName());

    public IntrusionPreventionExecution(String graphHexId, String graphInstanceHexId, String componentNodeHexId, String componentNodeInstanceHexId,
            String kafkaServer, String topicName, ConsulClient consulClient) {
        this.graphHexId = graphHexId;
        this.graphInstanceHexId = graphInstanceHexId;
        this.componentNodeHexId = componentNodeHexId;
        this.componentNodeInstanceHexId = componentNodeInstanceHexId;
        this.kafkaServer = kafkaServer;
        this.topicName = topicName;
        this.consulClient = consulClient;
    }

    @Override
    public void run() {
        LOGGER.log(Level.INFO, "Intrusion Prevention Service thread started !!!");

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

            CommandLineExecutor.singleLine(AgentConfiguration.HOST_WORKING_DIRECTORY + "/xdp/xdp_ddos01_blacklist --dev "
                    + AgentConfiguration.XDP_NETWORK_INTERFACE + " --owner root");
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(10000);
                for (ConsumerRecord<String, String> record : records) {
                    try {
                        ActionsCommands actionsCommands = mapper.readValue(record.value(), ActionsCommands.class);

                        String graph = actionsCommands.getId().split(":")[0];
                        String graphInstance = actionsCommands.getId().split(":")[1];
                        String componentNode = actionsCommands.getId().split(":")[2];
                        String componentNodeInstance = actionsCommands.getId().split(":")[3];

                        boolean executeCommand = false;
                        if ((graph.compareTo(graphHexId) == 0)
                                && (graphInstance.compareTo(graphInstanceHexId) == 0)
                                && (componentNode.compareTo(componentNodeHexId) == 0)
                                && (componentNodeInstance.compareTo(componentNodeInstanceHexId) == 0)) {
                            executeCommand = true;
                        } else if ((graph.compareTo(graphHexId) == 0)
                                && (graphInstance.compareTo(graphInstanceHexId) == 0)
                                && (componentNode.compareTo(componentNodeHexId) == 0)
                                && (componentNodeInstance.compareTo("*") == 0)) {
                            executeCommand = true;
                        } else if ((graph.compareTo(graphHexId) == 0)
                                && (graphInstance.compareTo(graphInstanceHexId) == 0)
                                && (componentNode.compareTo("*") == 0)
                                && (componentNodeInstance.compareTo("*") == 0)) {
                            executeCommand = true;
                        } else if ((graph.compareTo(graphHexId) == 0)
                                && (graphInstance.compareTo("*") == 0)
                                && (componentNode.compareTo("*") == 0)
                                && (componentNodeInstance.compareTo("*") == 0)) {
                            executeCommand = true;
                        } else if ((graph.compareTo("*") == 0)
                                && (graphInstance.compareTo("*") == 0)
                                && (componentNode.compareTo("*") == 0)
                                && (componentNodeInstance.compareTo("*") == 0)) {
                            executeCommand = true;
                        } else {
                            executeCommand = false;
                        }

                        if (executeCommand) {
                            if (actionsCommands.getCommand().compareTo("EXIT") == 0) {
                                //revertLogic();
                                LOGGER.log(Level.INFO, "Agent's services deregister from consul and the running container just killed and removed");

                            } else {
                                CommandLineExecutor.singleLine(actionsCommands.getCommand());
                                LOGGER.log(Level.INFO, "i execute: " + actionsCommands.getCommand());

                            }
                        } else {
                            LOGGER.log(Level.INFO, "i am not interesting for this command");

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


    //    private void revertLogic() {
    //        Response<Map<String, Service>> mapResponse = consulClient.getAgentServices();
    //
    //        for (String key : mapResponse.getValue().keySet()) {
    //            Service service = mapResponse.getValue().get(key);
    //            consulClient.agentServiceDeregister(service.getId());
    //
    //        }
    //
    //        CommandLineExecutor.singleLine("docker kill " + componentNodeHexId);
    //        CommandLineExecutor.singleLine("docker rm " + componentNodeHexId);
    //    }
}
