package eu.orchestrator.transfer.entities.soc;

import java.io.Serializable;

public class KafkaExpression implements Serializable {

  private String inputTopic;
  private Operand operand;
  private Logical logical;
  private String fieldName;
  private String context;

  public enum Operand {
    // contains for strings
    LIKE,

    // >
    GREATER_THAN,

    // <
    LESS_THAN,

    // >=
    GREATER_OR_EQUAL_THAN,

    // <=
    LESS_OR_EQUAL_THAN;
  }

  public enum Logical {
    AND,
    OR,
    OPEN_PARETHESIS,
    CLOSE_PARETHESIS;
  }

  public String getInputTopic() {
    return inputTopic;
  }

  public void setInputTopic(String inputTopic) {
    this.inputTopic = inputTopic;
  }

  public Operand getOperand() {
    return operand;
  }

  public void setOperand(Operand operand) {
    this.operand = operand;
  }

  public Logical getLogical() {
    return logical;
  }

  public void setLogical(Logical logical) {
    this.logical = logical;
  }

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  public String getContext() {
    return context;
  }

  public void setContext(String context) {
    this.context = context;
  }
}
