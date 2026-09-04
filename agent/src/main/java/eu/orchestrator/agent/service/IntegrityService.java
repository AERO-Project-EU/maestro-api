package eu.orchestrator.agent.service;

import eu.orchestrator.agent.model.DockerConfigurationForHashing;
import eu.orchestrator.agent.model.topics.SecurityConfiguration;
import eu.orchestrator.agent.model.topics.SecurityConfigurationResult;
import eu.orchestrator.agent.model.topics.SecurityConfigurationResult.HashType;
import eu.orchestrator.agent.util.CommandLineExecutor;
import eu.orchestrator.agent.util.Encryption;
import eu.orchestrator.agent.util.TopicProducer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Panagiotis Parthenis.
 */
public class IntegrityService {

    //LOGGER
    private static final Logger LOGGER = Logger.getLogger(IntegrityService.class.getName());

    private String kafkaServer;
    private String topicName;
    private SecurityConfiguration securityConfiguration;
    private DockerConfigurationForHashing dockerConfigurationForHashing;
    private String dockerContainerServiceId;
    private List<SecurityConfigurationResult> securityConfigurationResultList = new ArrayList<>();
    private ObjectMapper mapper = new ObjectMapper();

    public IntegrityService(String kafkaServer, String topicName, SecurityConfiguration securityConfiguration,
            DockerConfigurationForHashing dockerConfigurationForHashing) {
        this.kafkaServer = kafkaServer;
        this.topicName = topicName;
        this.securityConfiguration = securityConfiguration;
        this.dockerConfigurationForHashing = dockerConfigurationForHashing;

        handleDockerImageHash();
        handleDockerCredentialHash();

        String[] cmd = {
                "/bin/sh",
                "-c",
                "docker ps -a | grep " + dockerConfigurationForHashing.getImage() + " | awk '{print $1}'"
        };
        String output = CommandLineExecutor.multiLine(cmd);
        this.dockerContainerServiceId = output.replace("\n", "");

        handleDockerPortHash();
        handleDockerEnvHash();
        handleCertificate();
        sendSecurityConfigurationResult();
    }

    private void sendSecurityConfigurationResult() {
        securityConfiguration.setSecurityConfigurationResultList(this.securityConfigurationResultList);
        String securityConfigurationAsJson = "";
        try {
            securityConfigurationAsJson = mapper.writeValueAsString(securityConfiguration);
        } catch (JsonProcessingException exception) {
            LOGGER.log(Level.SEVERE, "Exception on push alert: {0}", exception);
        }

        LOGGER.log(Level.INFO, "RESULT --->:" + securityConfigurationAsJson);
        String securityConfigurationEncrypt = Encryption.encrypt(securityConfigurationAsJson);
        LOGGER.log(Level.INFO, "RESULT (Encrypt)--->:" + securityConfigurationEncrypt);

        TopicProducer.pushIntoKafkaTopic(topicName, securityConfigurationEncrypt, kafkaServer);
    }

    private void handleDockerImageHash() {
        int position = dockerConfigurationForHashing.getImage().indexOf(":");
        String dockerImageName = dockerConfigurationForHashing.getImage().substring(0, position);
        String[] cmd = {
                "/bin/sh",
                "-c",
                "docker images --digests | grep " + dockerImageName + " | awk '{print $3}'"
        };
        String output = CommandLineExecutor.multiLine(cmd);
        output = output.replace("sha256:", "");
        output = output.replaceAll(" ", "");
        output = output.replaceAll("\n", "");
        output = output.replaceAll("\"", "");
        output = output.replaceAll(",", "");

        SecurityConfigurationResult securityConfigurationResultDockerImage = new SecurityConfigurationResult();
        securityConfigurationResultDockerImage.setHashType(HashType.DOCKER_IMAGE);
        securityConfigurationResultDockerImage.setValue(output);
        securityConfigurationResultList.add(securityConfigurationResultDockerImage);

    }

    private void handleDockerCredentialHash() {
        JsonObject dockerCredential = new JsonObject();
        dockerCredential.addProperty("dockerRegistry", dockerConfigurationForHashing.getRegistry());
        dockerCredential.addProperty("dockerUsername", dockerConfigurationForHashing.getUsername());
        dockerCredential.addProperty("dockerPassword", dockerConfigurationForHashing.getPassword());
        String dockerCredentialAsString = dockerCredential.toString();
        String dockerCredentialHashing = Encryption.stringHash(dockerCredentialAsString);

        SecurityConfigurationResult securityConfigurationResultDockerCredentials = new SecurityConfigurationResult();
        securityConfigurationResultDockerCredentials.setHashType(HashType.DOCKER_CREDENTIALS);
        securityConfigurationResultDockerCredentials.setValue(dockerCredentialHashing);
        securityConfigurationResultList.add(securityConfigurationResultDockerCredentials);
    }

    private void handleDockerPortHash() {
        String[] cmd = {
                "/bin/sh",
                "-c",
                "docker inspect " + this.dockerContainerServiceId
        };
        String output = CommandLineExecutor.multiLine(cmd);

        Gson gson = new Gson();
        List<String> dockerPortList = new ArrayList<>();
        JsonArray dockerPortListJson = new JsonArray();
        JsonArray jsonArray = gson.fromJson(output, JsonArray.class);
        JsonObject baseObject = jsonArray.get(0).getAsJsonObject();
        JsonObject networkObject = baseObject.getAsJsonObject("NetworkSettings");
        JsonObject portsObject = networkObject.getAsJsonObject("Ports");

        for (Map.Entry<String, JsonElement> entry : portsObject.entrySet()) {

            if (!entry.getValue().isJsonNull()) {
                String dockerPort = entry.getKey();
                JsonElement portJsonElement = entry.getValue();
                JsonArray portJsonArray = portJsonElement.getAsJsonArray();
                JsonObject portElement = portJsonArray.get(0).getAsJsonObject();
                String hostPort = portElement.get("HostPort").getAsString();
                dockerPortList.add(hostPort + ":" + dockerPort);
            }
        }
        dockerPortList.sort(Comparator.comparing(String::toString));

        for (String entry : dockerPortList) {
            dockerPortListJson.add(entry);
        }

        String dockerPortListAsString = "";
        //TODO handle network mode host
        if (dockerPortList.size() == 0) {
            //TODO handle network mode host
        } else {
            dockerPortListAsString = dockerPortListJson.toString();
        }
        LOGGER.info("Docker port List as json :" + dockerPortListAsString);
        String dockerPortHashing = Encryption.stringHash(dockerPortListAsString);

        SecurityConfigurationResult securityConfigurationResultDockerPort = new SecurityConfigurationResult();
        securityConfigurationResultDockerPort.setHashType(HashType.DOCKER_PORT);
        securityConfigurationResultDockerPort.setValue(dockerPortHashing);
        securityConfigurationResultList.add(securityConfigurationResultDockerPort);
    }

    private void handleDockerEnvHash() {
        JsonArray dockerEnvList = new JsonArray();
        for (String key : dockerConfigurationForHashing.getEnvironmentVariableKey()) {

            System.out.println("---------------" + key);
            String[] cmd = {
                    "/bin/sh",
                    "-c",
                    "docker inspect " + this.dockerContainerServiceId + " | grep " + key
            };

            String output = CommandLineExecutor.multiLine(cmd);
            output = output.replaceAll(" ", "");
            output = output.replaceAll("\n", "");
            output = output.replaceAll("\"", "");
            output = output.replaceAll(",", "");
            int startPointAt = output.indexOf("=");
            String value = output.substring(startPointAt + 1);

            JsonObject dockerEnv = new JsonObject();
            dockerEnv.addProperty(key, value);
            dockerEnvList.add(dockerEnv);
        }
        String dockerEnvListAsString = dockerEnvList.toString();
        LOGGER.info("Docker env List as json :" + dockerEnvListAsString);
        String dockerEnvHashing = Encryption.stringHash(dockerEnvListAsString);

        SecurityConfigurationResult securityConfigurationResultDockerEnv = new SecurityConfigurationResult();
        securityConfigurationResultDockerEnv.setHashType(HashType.DOCKER_ENV);
        securityConfigurationResultDockerEnv.setValue(dockerEnvHashing);
        securityConfigurationResultList.add(securityConfigurationResultDockerEnv);
    }

    private void handleCertificate() {
        securityConfiguration.setCertificate("1111");
    }


}
