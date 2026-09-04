package eu.orchestrator.policy.engine.model;

/**
 * @author Panagiotis Parthenis
 */
public class PrometheusMetricAlert extends DroolsFact {

  private String graphHexID;

  private String graphInstanceHexID;

  private String alertName;

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

  public String getAlertName() {
    return alertName;
  }

  public void setAlertName(String alertName) {
    this.alertName = alertName;
  }

  @Override
  public String toString() {
    return "PrometheusMetricAlert{" +
        "graphHexID='" + graphHexID + '\'' +
        ", graphInstanceHexID='" + graphInstanceHexID + '\'' +
        ", alertName='" + alertName + '\'' +
        '}';
  }
}
