package eu.orchestrator.transfer.entities.policyEngine;

import java.io.Serializable;

/**
 * @author Panagiotis Parthenis
 */
public class TriggerActionModel implements Serializable {

  private String graphHexID;
  private String graphInstanceHexID;
  private String componentNodeHexID;
  private ActionType action;
  private String context;
  private int actionAmount;

  public ActionType getAction() {
    return action;
  }

  public void setAction(ActionType action) {
    this.action = action;
  }

  public int getActionAmount() {
    return actionAmount;
  }

  public void setActionAmount(int actionAmount) {
    this.actionAmount = actionAmount;
  }

  public String getContext() {
    return context;
  }

  public void setContext(String context) {
    this.context = context;
  }

  public String getGraphHexID() {
    return graphHexID;
  }

  public void setGraphHexID(String graphHexID) {
    this.graphHexID = graphHexID;
  }

  public String getGraphInstanceHexID() {
    return graphInstanceHexID;
  }

  public void setGraphInstanceHexID(String graphInstanceHexID) {
    this.graphInstanceHexID = graphInstanceHexID;
  }

  public String getComponentNodeHexID() {
    return componentNodeHexID;
  }

  public void setComponentNodeHexID(String componentNodeHexID) {
    this.componentNodeHexID = componentNodeHexID;
  }

  @Override
  public String toString() {
    return "TriggerActionModel{" +
        ", graphHexID='" + graphHexID + '\'' +
        ", graphInstanceHexID='" + graphInstanceHexID + '\'' +
        ", componentNodeHexID='" + componentNodeHexID + '\'' +
        ", action=" + action +
        ", context='" + context + '\'' +
        ", actionAmount=" + actionAmount +
        '}';
  }
}