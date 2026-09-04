package eu.orchestrator.transfer.entities.soc;

import java.io.Serializable;

public class FieldModel implements Serializable {

  private FieldType fieldType;
  private String fieldName;

  public enum FieldType {
    BOOLEAN,
    INTEGER,
    BIGINT,
    DOUBLE,
    VARCHAR,
    STRING
    ;
  }

  public FieldType getFieldType() {
    return fieldType;
  }

  public void setFieldType(FieldType fieldType) {
    this.fieldType = fieldType;
  }

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }
}
