package eu.orchestrator.backend.service.model;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class KubernetesGraph implements Serializable {

    List<Deployment> deployment;
    List<Service> service;
    List<io.fabric8.knative.serving.v1.Service> knativeService;

    public List<Deployment> getDeployment() {
        if (deployment == null) {
            deployment = new ArrayList<>();
        }
        return deployment;
    }

    public void setDeployment(List<Deployment> deployment) {
        this.deployment = deployment;
    }

    public List<Service> getService() {
        if (service == null) {
            service = new ArrayList<>();
        }
        return service;
    }

    public void setService(List<Service> service) {
        this.service = service;
    }

    public List<io.fabric8.knative.serving.v1.Service> getKnativeService() {
        if (knativeService == null) {
            knativeService = new ArrayList<>();
        }
        return knativeService;
    }

    public void setKnativeService(List<io.fabric8.knative.serving.v1.Service> knativeService) {
        this.knativeService = knativeService;
    }
}
