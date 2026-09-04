package eu.orchestrator.backend.transfer;

import java.io.Serializable;

public class ProfilingResultTO implements Serializable {

    String startTime;
    String endTime;
    String algorithm;
    String outputURl;
    String step;

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getOutputURl() {
        return outputURl;
    }

    public void setOutputURl(String outputURl) {
        this.outputURl = outputURl;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }
}
