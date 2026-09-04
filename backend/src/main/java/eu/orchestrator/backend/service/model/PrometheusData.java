package eu.orchestrator.backend.service.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PrometheusData implements Serializable {

    private String resultType;
    private List<PrometheusResult> result;


    public String getResultType() {
        return resultType;
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    public List<PrometheusResult> getResult() {
        if (result == null) {
            result = new ArrayList<>();
        }
        return result;
    }

    public void setResult(List<PrometheusResult> result) {
        this.result = result;
    }
}
