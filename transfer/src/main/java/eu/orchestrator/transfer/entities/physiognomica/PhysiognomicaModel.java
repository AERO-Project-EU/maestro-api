package eu.orchestrator.transfer.entities.physiognomica;

import java.io.Serializable;
import java.util.List;

public class PhysiognomicaModel implements Serializable {

  private List<PhysiognomicaPeriodModel> periods;

  private String name;
  private String step;
  private String vendor;
  private List<String> metrics;

  private String callback;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getStep() {
    return step;
  }

  public void setStep(String step) {
    this.step = step;
  }

  public String getVendor() {
    return vendor;
  }

  public void setVendor(String vendor) {
    this.vendor = vendor;
  }

  public List<String> getMetrics() {
    return metrics;
  }

  public void setMetrics(List<String> metrics) {
    this.metrics = metrics;
  }

  public String getCallback() {
    return callback;
  }

  public void setCallback(String callback) {
    this.callback = callback;
  }

  public List<PhysiognomicaPeriodModel> getPeriods() {
    return periods;
  }

  public void setPeriods(
      List<PhysiognomicaPeriodModel> periods) {
    this.periods = periods;
  }

  @Override
  public String toString() {
    return "PhysiognomicaModel{" +

        ", name='" + name + '\'' +
        ", step='" + step + '\'' +
        ", vendor='" + vendor + '\'' +
        ", metrics=" + metrics +
        ", callback='" + callback + '\'' +
        '}';
  }
}
