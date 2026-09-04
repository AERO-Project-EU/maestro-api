package eu.orchestrator.agent;

import eu.orchestrator.agent.configuration.AgentConfiguration;
import eu.orchestrator.agent.configuration.ErrorStatus;
import eu.orchestrator.agent.model.kv.IntrusionDetectionParameters;
import eu.orchestrator.agent.model.topics.IntrusionDetectionAlert;
import eu.orchestrator.agent.model.topics.IntrusionDetectionConfiguration;
import eu.orchestrator.agent.service.IntrusionDetectionService;
import eu.orchestrator.agent.util.CommandLineExecutor;
import eu.orchestrator.agent.util.Encryption;
import eu.orchestrator.agent.util.TopicProducer;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.NewService;
import com.ecwid.consul.v1.kv.model.GetValue;
import com.fasterxml.jackson.core.JsonProcessingException;
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
public class IntrusionDetectionExecution extends Thread {

    //LOGGER
    private static final Logger LOGGER = Logger.getLogger(IntrusionDetectionExecution.class.getName());

    //Arguments
    private String graphHexId;
    private String graphInstanceHexId;
    private String componentNodeHexId;
    private String componentNodeInstanceHexId;
    private String kafkaServer;
    private String topicNameInput;
    private String topicNameOutput;
    private ConsulClient consulClient;

    //IDS Status
    private int intrusionDetectionStatus = 1;

    private IntrusionDetectionParameters intrusionDetectionParameters;

    public IntrusionDetectionExecution(String graphHexId, String graphInstanceHexId, String componentNodeHexId, String componentNodeInstanceHexId,
            String kafkaServer, String topicNameInput, String topicNameOutput, ConsulClient consulClient) {
        this.graphHexId = graphHexId;
        this.graphInstanceHexId = graphInstanceHexId;
        this.componentNodeHexId = componentNodeHexId;
        this.componentNodeInstanceHexId = componentNodeInstanceHexId;
        this.kafkaServer = kafkaServer;
        this.topicNameInput = topicNameInput;
        this.topicNameOutput = topicNameOutput;
        this.consulClient = consulClient;
    }

    @Override
    public void run() {

        LOGGER.log(Level.INFO, "Intrusion Detection Service thread started !!!");

        // initialize IDS parameters
        Agent.startMrClock();
        if (intrusionDetectionStatus > 0) {
            for (int i = 0; i < AgentConfiguration.MAX_ITERATIONS; i++) {
                if (initialize()) {
                    //Agent.pushMetric("phase7_IDS_initialize");
                    LOGGER.log(Level.INFO, "Successfully initialize object for snort");
                    break;

                } else {
                    Agent.relaxTime();

                }
            }
        }

        // initialize IDS parameters
        Agent.startMrClock();
        if (intrusionDetectionStatus > 0) {
            for (int i = 0; i < AgentConfiguration.MAX_ITERATIONS; i++) {
                if (loginDockerForIds()) {
                    //Agent.pushMetric("phase7_IDS_login_nexus");
                    LOGGER.log(Level.INFO, "Docker login successfully for Snort");
                    break;

                } else {
                    Agent.relaxTime();

                }
            }
        }

        Agent.startMrClock();
        if (intrusionDetectionStatus > 0) {
            for (int i = 0; i < AgentConfiguration.MAX_ITERATIONS; i++) {
                if (downloadDockerImageSnort()) {
                    //Agent.pushMetric("phase3_download_docker_images");
                    LOGGER.log(Level.INFO, "Snort downloaded successfully");
                    break;

                } else {
                    Agent.relaxTime();

                }
            }
        }

        //docker pull
        startContainerSnort();

        //register snort
        registerSnort();

        if (intrusionDetectionStatus > 0) {
            listenKafkaTopic();
        }
    }

    // login docker for IDS Service
    private boolean initialize() {

        ObjectMapper mapper = new ObjectMapper();
        Response<GetValue> idsParametersJson = consulClient
                .getKVValue(graphHexId + "/" + graphInstanceHexId + "/" + componentNodeHexId + "/configurations/intrusionDetectionParameters");

        if (idsParametersJson.getValue() == null) {
            //Agent.setStatus(AgentConfiguration.STATUS_TERMINATED_DUE_TO_BAD_KV_PARSING);
            intrusionDetectionStatus = ErrorStatus.STATUS_TERMINATED_DUE_TO_BAD_KV_PARSING.getValue();
            LOGGER.log(Level.INFO, "Error on KV parsing: object is empty");
            return false;
        }

        try {
            intrusionDetectionParameters = mapper.readValue(idsParametersJson.getValue().getDecodedValue(), IntrusionDetectionParameters.class);
            intrusionDetectionParameters.setPassword(Encryption.decryption(intrusionDetectionParameters.getPassword()));

        } catch (IOException exception) {
            intrusionDetectionStatus = ErrorStatus.STATUS_TERMINATED_DUE_TO_BAD_KV_PARSING.getValue();
            LOGGER.log(Level.INFO, "Error on KV parsing: object is empty");
            return false;
        }

        return true;
    }

    // login docker for IDS Service
    private boolean loginDockerForIds() {
        String[] cmd = {
                "/bin/sh",
                "-c",
                "docker login " + intrusionDetectionParameters.getUrl() + " -u " + intrusionDetectionParameters.getUserName() + " -p"
                        + intrusionDetectionParameters.getPassword()
        };

        String output = CommandLineExecutor.multiLine(cmd);

        if (output.contains("Login Succeeded")) {
            return true;

        } else {
            //Agent.setStatus(intrusionDetectionStatus = AgentConfiguration.STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_INITIALIZATION_OF_LOGIN_DOCKER);
            intrusionDetectionStatus = ErrorStatus.STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_INITIALIZATION_OF_LOGIN_DOCKER.getValue();
            return false;

        }
    }

    //download image
    private boolean downloadDockerImageSnort() {
        CommandLineExecutor.singleLine("docker pull " + intrusionDetectionParameters.getUrl() + "/" + intrusionDetectionParameters.getIdsDockerImage());

        String[] cmd = {
                "/bin/sh",
                "-c",
                "docker images"
        };

        String output = CommandLineExecutor.multiLine(cmd);

        String imageName = intrusionDetectionParameters.getIdsDockerImage().split(":")[0];

        if (!output.contains(intrusionDetectionParameters.getUrl() + "/" + imageName)) {
            intrusionDetectionStatus = ErrorStatus.STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_IMAGE_NOT_FOUND.getValue();
            LOGGER.log(Level.INFO, "Error on KV parsing: object parsing");
            return false;
        }

        return true;
    }

    // trigger container to start
    private void startContainerSnort() {
        CommandLineExecutor.singleLine("docker rm snort --force");
        CommandLineExecutor.singleLine("rm " + AgentConfiguration.SNORT_ALERT_FILE + " ");
        CommandLineExecutor.singleLine("touch " + AgentConfiguration.SNORT_ALERT_FILE + " ");

        //CommandLineExecutor.singleLine("docker rm " + componentNodeHexId + " --force");
        StringBuilder dockerCommand = new StringBuilder();
        dockerCommand = dockerCommand.append("docker run -d");
        dockerCommand = dockerCommand.append(" --name snort");
        for (String capabilities : intrusionDetectionParameters.getCapabilities()) {
            dockerCommand = dockerCommand.append(" " + capabilities);
        }

        for (String volume : intrusionDetectionParameters.getVolumes()) {
            dockerCommand = dockerCommand.append(" -v " + volume);

        }

        dockerCommand = dockerCommand.append(" " + intrusionDetectionParameters.getUrl() + "/" + intrusionDetectionParameters.getIdsDockerImage());

        LOGGER.log(Level.INFO, "{0}", dockerCommand.toString());
        CommandLineExecutor.singleLine(dockerCommand.toString());

    }

    // registerSnort
    private void registerSnort() {
        NewService newService = new NewService();
        newService.setId("snortID");
        newService.setName("snort");
        //newService.setAddress(privateIpAddressIPv6);

        /*
        NewService.Check serviceCheck = new NewService.Check();
        serviceCheck.setHttp("http://" + privateIpAddressIPv6 + ":" + AgentConfiguration.METRIC_EXPORTER_PORT + "/metrics");
        serviceCheck.setInterval("5s");
        serviceCheck.setDeregisterCriticalServiceAfter("5m");
        newService.setCheck(serviceCheck);
        */

        consulClient.agentServiceRegister(newService);
    }

    //listen into topic
    private void listenKafkaTopic() {

        Properties props = new Properties();
        props.put("bootstrap.servers", kafkaServer);
        props.put("group.id", graphHexId + ":" + graphInstanceHexId + ":" + componentNodeHexId + ":" + componentNodeInstanceHexId);
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset", "earliest");
        props.put("enable.auto.commit", false);

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Arrays.asList(topicNameInput));

            ObjectMapper mapper = new ObjectMapper();
            IntrusionDetectionService intrusionDetectionService = new IntrusionDetectionService(intrusionDetectionParameters.getAlertPath(),
                    IntrusionDetectionService.AlertMode.FAST);

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(10000);

                for (ConsumerRecord<String, String> record : records) {
                    try {
                        IntrusionDetectionConfiguration intrusionDetectionConfiguration = mapper
                                .readValue(record.value(), IntrusionDetectionConfiguration.class);

                        if ((intrusionDetectionConfiguration.getGraphHexId().compareTo(graphHexId) == 0)
                                && (intrusionDetectionConfiguration.getGraphInstanceHexId().compareTo(graphInstanceHexId) == 0)
                                && (intrusionDetectionConfiguration.getComponentNodeHexId().compareTo(componentNodeHexId) == 0)
                                && (intrusionDetectionConfiguration.getComponentNodeInstanceHexId().compareTo(componentNodeInstanceHexId) == 0)) {
                            LOGGER.log(Level.INFO, "Received: {0}", intrusionDetectionConfiguration);
                            if (intrusionDetectionService
                                    .updateRuleSetFile(intrusionDetectionParameters.getRuleSetPath(), intrusionDetectionConfiguration.getRules())) {
                                consumer.commitSync();
                                CommandLineExecutor.singleLine("docker restart snort");
                            }

                        } else {

                            LOGGER.log(Level.INFO, "i am not interesting for this Intrusion Detection rule");
                            consumer.commitSync();
                        }

                    } catch (IOException exception) {
                        LOGGER.log(Level.SEVERE, "Exception on listening ID: {0}", exception);

                    }
                }

                try {
                    String temp = intrusionDetectionService.readNextAlert();

                    while (temp != null) {

                        IntrusionDetectionAlert intrusionDetectionAlert = new IntrusionDetectionAlert();
                        intrusionDetectionAlert.setGraphHexId(graphHexId);
                        intrusionDetectionAlert.setGraphInstanceHexId(graphInstanceHexId);
                        intrusionDetectionAlert.setComponentNodeHexId(componentNodeHexId);
                        intrusionDetectionAlert.setComponentNodeInstanceHexId(componentNodeInstanceHexId);
                        intrusionDetectionAlert.setAlert(temp);

                        String intrusionDetectionAlertJson = "";
                        try {
                            intrusionDetectionAlertJson = mapper.writeValueAsString(intrusionDetectionAlert);
                        } catch (JsonProcessingException exception) {
                            LOGGER.log(Level.SEVERE, "Exception on push alert: {0}", exception);
                        }

                        TopicProducer.pushIntoKafkaTopic(topicNameOutput, intrusionDetectionAlertJson, kafkaServer);
                        temp = intrusionDetectionService.readNextAlert();

                    }

                } catch (Exception exception) {
                    LOGGER.log(Level.SEVERE, "Exception on recourse ID: {0}", exception);
                }

            }

        }
    }

}
