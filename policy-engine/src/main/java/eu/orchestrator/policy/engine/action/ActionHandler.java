package eu.orchestrator.policy.engine.action;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.orchestrator.policy.engine.message.broker.kafka.TopicProducer;
import eu.orchestrator.policy.engine.message.broker.rabbitmq.QueueProducer;
import eu.orchestrator.transfer.entities.policyEngine.ActionType;
import eu.orchestrator.transfer.entities.policyEngine.TriggerActionModel;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

/**
 * @author Panagiotis Parthenis
 */
public class ActionHandler {

  private static final Logger LOGGER = Logger.getLogger(ActionHandler.class.getName());

  private String topicUrl;
  private String topicPort;
  private String topicName;
  private String componentName;
  private String context;
  private String ruleAction;

  public ActionHandler(String topicUrl, String topicPort, String topicName, String componentName, String ruleAction, String context) {
    this.topicUrl = topicUrl;
    this.topicPort = topicPort;
    this.topicName = topicName;
    this.componentName = componentName;
    this.context = context;
    this.ruleAction = ruleAction;
    elasticityActionOrchestrator();
  }

  /**
   * This method is the elasticity action handler create a proposal object and push
   * it into topic
   */
  private void elasticityActionOrchestrator() {
    String[] split = componentName.split(":");

    TriggerActionModel triggerActionModel = new TriggerActionModel();
    triggerActionModel.setGraphHexID(split[0]);
    triggerActionModel.setGraphInstanceHexID(split[1]);
    triggerActionModel.setComponentNodeHexID(split[2]);

    if (ruleAction.compareTo("scaleOut") == 0) {
      triggerActionModel.setAction(ActionType.scaleOut);
      triggerActionModel.setActionAmount(Integer.parseInt(context));

    } else if (ruleAction.compareTo("scaleIn") == 0) {
      triggerActionModel.setAction(ActionType.scaleIn);
      triggerActionModel.setActionAmount(Integer.parseInt(context));

    } else if (ruleAction.compareTo("info") == 0) {
      triggerActionModel.setAction(ActionType.info);
      triggerActionModel.setContext(context);

    } else if (ruleAction.compareTo("pushIntoKafkaTopic") == 0) {
      triggerActionModel.setAction(ActionType.pushIntoKafkaTopic);
      triggerActionModel.setContext(context);

    } else if (ruleAction.compareTo("pushIntoRabbitMQQueue") == 0) {
      triggerActionModel.setAction(ActionType.pushIntoRabbitMQQueue);
      triggerActionModel.setContext(context);
    }

    LOGGER.info(triggerActionModel.toString());
    ObjectMapper objectMapper = new ObjectMapper();
    String jsonInString = "";

    if (triggerActionModel.getAction() != ActionType.pushIntoRabbitMQQueue) {
      try {
        jsonInString = objectMapper.writeValueAsString(triggerActionModel);
        TopicProducer.pushIntoKafkaTopic(topicName, jsonInString, topicUrl, topicPort);

      } catch (JsonProcessingException e) {
        LOGGER.info("Error parsing object" + e);
      }
    } else {
      try {
        jsonInString = objectMapper.writeValueAsString(triggerActionModel);
        QueueProducer.pushIntoRabbitMQQueue(topicName, jsonInString, topicUrl, topicPort);

      } catch (JsonProcessingException e) {
        LOGGER.info("Error parsing object" + e);

      } catch (TimeoutException e) {
        LOGGER.info("Error on push into rabbit mq " + e);

      } catch (IOException e) {
        LOGGER.info("Error on push into rabbit mq " + e);
      }
    }
  }

}
