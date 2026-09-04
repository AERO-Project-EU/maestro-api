package eu.orchestrator.backend.service.model;

import java.io.Serializable;

public class PrometheusResponse implements Serializable {

    private String status;
    private PrometheusData data;


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public PrometheusData getData() {
        return data;
    }

    public void setData(PrometheusData data) {
        this.data = data;
    }
}
