package eu.orchestrator.transfer.entities.soc;

import java.io.Serializable;
import java.util.List;

public class SocRuleModel implements Serializable {

  private String hexID ;

  private String kafkaRuleExpression;

  private List<InputKafkaStream> inputStreamList;

  private List<KafkaExpression> kafkaExpressionList;

  private List<DroolsExpression> droolsExpressionList;

  private Integer droolsInertiaPeriodInSecond;

  private List<OutputDroolsAction> outputDroolsActionList;

  private KafkaConfig kafkaConfig;

  private CrudOperation crudOperation;

  private String callbackURLManager;

  private String callbackURLDrools;

  public enum CrudOperation {
    CREATE,
    DELETE;
  }


  public String getHexID() {
    return hexID;
  }

  public void setHexID(String hexID) {
    this.hexID = hexID;
  }

  public String getKafkaRuleExpression() {
    return kafkaRuleExpression;
  }

  public void setKafkaRuleExpression(String kafkaRuleExpression) {
    this.kafkaRuleExpression = kafkaRuleExpression;
  }

  public List<InputKafkaStream> getInputStreamList() {
    return inputStreamList;
  }

  public void setInputStreamList(
      List<InputKafkaStream> inputStreamList) {
    this.inputStreamList = inputStreamList;
  }

  public List<KafkaExpression> getKafkaExpressionList() {
    return kafkaExpressionList;
  }

  public void setKafkaExpressionList(
      List<KafkaExpression> kafkaExpressionList) {
    this.kafkaExpressionList = kafkaExpressionList;
  }

  public List<DroolsExpression> getDroolsExpressionList() {
    return droolsExpressionList;
  }

  public void setDroolsExpressionList(
      List<DroolsExpression> droolsExpressionList) {
    this.droolsExpressionList = droolsExpressionList;
  }

  public Integer getDroolsInertiaPeriodInSecond() {
    return droolsInertiaPeriodInSecond;
  }

  public void setDroolsInertiaPeriodInSecond(Integer droolsInertiaPeriodInSecond) {
    this.droolsInertiaPeriodInSecond = droolsInertiaPeriodInSecond;
  }

  public List<OutputDroolsAction> getOutputDroolsActionList() {
    return outputDroolsActionList;
  }

  public void setOutputDroolsActionList(
      List<OutputDroolsAction> outputDroolsActionList) {
    this.outputDroolsActionList = outputDroolsActionList;
  }

  public KafkaConfig getKafkaConfig() {
    return kafkaConfig;
  }

  public void setKafkaConfig(
      KafkaConfig kafkaConfig) {
    this.kafkaConfig = kafkaConfig;
  }

  public CrudOperation getCrudOperation() {
    return crudOperation;
  }

  public void setCrudOperation(CrudOperation crudOperation) {
    this.crudOperation = crudOperation;
  }

  public String getCallbackURLManager() {
    return callbackURLManager;
  }

  public void setCallbackURLManager(String callbackURLManager) {
    this.callbackURLManager = callbackURLManager;
  }

  public String getCallbackURLDrools() {
    return callbackURLDrools;
  }

  public void setCallbackURLDrools(String callbackURLDrools) {
    this.callbackURLDrools = callbackURLDrools;
  }
}

