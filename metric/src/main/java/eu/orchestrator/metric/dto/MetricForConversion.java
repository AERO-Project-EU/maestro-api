package eu.orchestrator.metric.dto;

import eu.orchestrator.repository.domain.Analytic;

import java.io.Serializable;

public class MetricForConversion implements Serializable {

    private Analytic analytic;
    //Extra fields
    private String namespace;


    public Analytic getAnalytic() {
        return analytic;
    }

    public void setAnalytic(Analytic analytic) {
        this.analytic = analytic;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
