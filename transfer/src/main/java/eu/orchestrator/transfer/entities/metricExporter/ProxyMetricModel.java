package eu.orchestrator.transfer.entities.metricExporter;

/**
 * @author Panagiotis Parthenis
 */
public class ProxyMetricModel {
  private String functionName;
  private double invocationsResponseTime; //seconds
  private double invocationsPerSec; //number of request

  public String getFunctionName() {
    return functionName;
  }

  public void setFunctionName(String functionName) {
    this.functionName = functionName;
  }

  public double getInvocationsResponseTime() {
    return invocationsResponseTime;
  }

  public void setInvocationsResponseTime(double invocationsResponseTime) {
    this.invocationsResponseTime = invocationsResponseTime;
  }

  public double getInvocationsPerSec() {
    return invocationsPerSec;
  }

  public void setInvocationsPerSec(double invocationsPerSec) {
    this.invocationsPerSec = invocationsPerSec;
  }
}

