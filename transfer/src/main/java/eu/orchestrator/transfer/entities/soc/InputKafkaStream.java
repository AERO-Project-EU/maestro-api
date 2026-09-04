package eu.orchestrator.transfer.entities.soc;

import java.io.Serializable;
import java.util.List;

public class InputKafkaStream implements Serializable {

  private String inputTopic;
  private List<FieldModel> fieldModelList;

  public String getInputTopic() {
    return inputTopic;
  }

  public void setInputTopic(String inputTopic) {
    this.inputTopic = inputTopic;
  }

  public List<FieldModel> getFieldModelList() {
    return fieldModelList;
  }

  public void setFieldModelList(List<FieldModel> fieldModelList) {
    this.fieldModelList = fieldModelList;
  }
}
