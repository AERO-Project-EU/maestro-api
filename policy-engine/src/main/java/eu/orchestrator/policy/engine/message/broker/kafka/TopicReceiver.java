package eu.orchestrator.policy.engine.message.broker.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.orchestrator.policy.engine.configuration.DroolsConfiguration;
import eu.orchestrator.policy.engine.configuration.KafkaConfiguration;
import eu.orchestrator.policy.engine.configuration.PrometheusConfiguration;
import eu.orchestrator.policy.engine.service.DroolsService;
import eu.orchestrator.policy.engine.utils.DroolsRules;
import eu.orchestrator.policy.engine.utils.PrometheusRules;
import eu.orchestrator.transfer.entities.policyEngine.PolicyModel;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * @author Panagiotis Parthenis
 */
@Service
public class TopicReceiver {

  private static final Logger LOGGER = Logger.getLogger(TopicReceiver.class.getName());

  @Autowired
  private KafkaConfiguration kafkaConfiguration;


  @Autowired
  private PrometheusConfiguration prometheusConfiguration;

  @Autowired
  private DroolsConfiguration droolsConfiguration;

  @Autowired
  private DroolsService droolsService;

  @KafkaListener(topics = "${system.kafka.backendTopicName}")
  public void listenForReports(@Payload String message) {
    LOGGER.log(Level.INFO, "****** {0} Reserved ******", kafkaConfiguration.getBackendTopicName());
    LOGGER.log(Level.INFO, " {0} ", message);
    LOGGER.log(Level.INFO, "***********************************");

    ObjectMapper mapper = new ObjectMapper();

    try {
      PolicyModel policyModel = mapper.readValue(message, PolicyModel.class);

      boolean success = false;
      if (policyModel.isCreation()) {
        success = createRulesLogic(policyModel);
      } else {
        if (policyModel.getPolicyHexID() == null) {
          success = deleteAllRuleLogic(policyModel);
        } else {
          success = deleteRuleLogic(policyModel);
        }
      }

      if (success) {

        if (policyModel.isCreation()) {

          try {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.postForLocation(policyModel.getCallbackURL() + "/SUCCESS", String.class);
            LOGGER.log(Level.INFO, "SUCCESS");

          } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Cannot execute rest call back");
          }

        }

      } else {
        LOGGER.info("procces is not complete success the msg is not commit from topic");

        if (policyModel.isCreation()) {

          try {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.postForLocation(policyModel.getCallbackURL() + "/ERROR", String.class);
            LOGGER.log(Level.INFO, "FAIL");

          } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Cannot execute rest call back");
          }

        }
      }

    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Something get Wrong with policy-cruder");
    }

  }

  private boolean createRulesLogic(PolicyModel policyModel) {
    try {
      if (DroolsRules.checkIfContainsRulesName(droolsConfiguration, policyModel)) {
        LOGGER.warning("This policy ID exist");
        return false;
      } else {
        PrometheusRules.createRule(prometheusConfiguration, policyModel);
        PrometheusRules.updateConfigurationCreation(prometheusConfiguration, policyModel);
        updatePrometheus();

        String filePath = DroolsRules
            .createRule(droolsConfiguration, kafkaConfiguration, policyModel);
        droolsService.initDroolsEngine(filePath,
            policyModel.getGraphHexID() + ":" + policyModel.getGraphInstanceHexID() + ":"
                + policyModel.getPolicyHexID());

        return true;
      }

    } catch (Exception e) {
      LOGGER.warning("I/O Exception on file operation : " + e);
      return false;
    }
  }

  private boolean deleteAllRuleLogic(PolicyModel policyModel) {
    try {
      PrometheusRules.deleteAllRule(prometheusConfiguration, policyModel);
      PrometheusRules.updateConfigurationDeletion(prometheusConfiguration, policyModel);
      updatePrometheus();

      List<String> results = new ArrayList<>();

      File[] files = new File(droolsConfiguration.getFilePath()).listFiles();

      for (File file : files) {
        if (file.isFile()) {
          results.add(file.getName());
        }
      }

      for (int i = 0; i < results.size(); i++) {
        String filePath = results.get(i);
        int startAt = filePath.lastIndexOf('/') + 1;
        int endAt = filePath.length() - 4;
        String id = filePath.substring(startAt, endAt);

        if (!id.contains("_")) {
          continue;
        }

        id = id.replace("_", ":");

        if (id
            .startsWith(policyModel.getGraphHexID() + ":" + policyModel.getGraphInstanceHexID())) {
          droolsService.removeKieSessionWithId(id);
          DroolsRules.deleteAllRule(filePath);
        }

      }
      return true;
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "I/O Exception on file operation : {0}", e);
      return false;
    }
  }

  private boolean deleteRuleLogic(PolicyModel policyModel) {
    try {
      PrometheusRules.deleteRule(prometheusConfiguration, policyModel);
      updatePrometheus();

      droolsService.removeKieSessionWithId(
          policyModel.getGraphHexID() + ":" + policyModel.getGraphInstanceHexID() + ":"
              + policyModel.getPolicyHexID());
      DroolsRules.deleteAllRule(
          droolsConfiguration.getFilePath() + policyModel.getGraphHexID() + "_" + policyModel
              .getGraphInstanceHexID() + "_" + policyModel.getPolicyHexID() + ".drl");

      return true;

    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "I/O Exception on file operation : {0}", e);
      return false;
    }
  }

  private void updatePrometheus() {
    RestTemplate restTemplate = new RestTemplate();
    restTemplate.postForLocation("http://" + prometheusConfiguration.getUrl() + ":" + prometheusConfiguration.getPort() + "/-/reload", String.class);
  }
}
