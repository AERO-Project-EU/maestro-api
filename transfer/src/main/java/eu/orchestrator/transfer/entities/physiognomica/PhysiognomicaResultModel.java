package eu.orchestrator.transfer.entities.physiognomica;

import java.io.Serializable;
import java.util.List;

public class PhysiognomicaResultModel implements Serializable {

  private String analyticServiceName;
  private String callbackid;
  private String executionDate;
  private String executionMessage;
  private String id;
  private String status;
  private List<ResultModel> resultModels;

  public String getAnalyticServiceName() {
    return analyticServiceName;
  }

  public void setAnalyticServiceName(String analyticServiceName) {
    this.analyticServiceName = analyticServiceName;
  }

  public String getCallbackid() {
    return callbackid;
  }

  public void setCallbackid(String callbackid) {
    this.callbackid = callbackid;
  }

  public String getExecutionDate() {
    return executionDate;
  }

  public void setExecutionDate(String executionDate) {
    this.executionDate = executionDate;
  }

  public String getExecutionMessage() {
    return executionMessage;
  }

  public void setExecutionMessage(String executionMessage) {
    this.executionMessage = executionMessage;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public List<ResultModel> getResultModels() {
    return resultModels;
  }

  public void setResultModels(
      List<ResultModel> resultModels) {
    this.resultModels = resultModels;
  }

  class ResultModel {
    private String result;
    private String type;

    public String getResult() {
      return result;
    }

    public void setResult(String result) {
      this.result = result;
    }

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }
  }
}
