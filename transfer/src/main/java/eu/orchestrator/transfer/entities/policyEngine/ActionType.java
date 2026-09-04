package eu.orchestrator.transfer.entities.policyEngine;

/**
 * @author Panagiotis Parthenis
 */
public enum ActionType {
  scaleOut,
  scaleIn,
  scaleUp,
  scaleDown,
  info,
  pushIntoKafkaTopic,
  pushIntoRabbitMQQueue

}
