package eu.orchestrator.transfer.entities.policyEngine;

import java.io.Serializable;

/**
 * @author Panagiotis Parthenis
 */
public class DroolsAction implements Serializable {

  private ActionType ruleAction;
  private String componentName;
  private String componentHexID;
  private String context;
  private String topicUrl;
  private String topicName;
  private String topicPort;

  public ActionType getRuleAction() {
    return ruleAction;
  }

  public void setRuleAction(ActionType ruleAction) {
    this.ruleAction = ruleAction;
  }

  public String getComponentName() {
    return componentName;
  }

  public void setComponentName(String componentName) {
    this.componentName = componentName;
  }

  public String getContext() {
    return context;
  }

  public void setContext(String context) {
    this.context = context;
  }

  public String getTopicUrl() {
    return topicUrl;
  }

  public void setTopicUrl(String topicUrl) {
    this.topicUrl = topicUrl;
  }

  public String getTopicName() {
    return topicName;
  }

  public void setTopicName(String topicName) {
    this.topicName = topicName;
  }

  public String getTopicPort() {
    return topicPort;
  }

  public void setTopicPort(String topicPort) {
    this.topicPort = topicPort;
  }

  public String getComponentHexID() {
    return componentHexID;
  }

  public void setComponentHexID(String componentHexID) {
    this.componentHexID = componentHexID;
  }
}
