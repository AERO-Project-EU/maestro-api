package eu.orchestrator.elasticity.spi.model.backend;

import eu.orchestrator.repository.domain.Application;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorApplicationInstance;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorComponentNodeInstance;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 27/8/2019
 */
public class ScalingObjects {

    private Application application;
    private ApplicationInstance applicationInstance;
    private Boolean proceedWithScaling;
    private ComponentNodeInstance componentNodeInstanceWorker;
    private int numberOfWorkers;
    private int existingWorkers;
    private List<OrchestratorComponentNodeInstance> serviceList;
    private List<ComponentNodeInstance> componentNodeInstanceList;
    private OrchestratorApplicationInstance orchestratorApplicationInstance;

    public ScalingObjects() {
        this.proceedWithScaling = false;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public Boolean getProceedWithScaling() {
        return proceedWithScaling;
    }

    public void setProceedWithScaling(Boolean proceedWithScaling) {
        this.proceedWithScaling = proceedWithScaling;
    }

    public ComponentNodeInstance getComponentNodeInstanceWorker() {
        return componentNodeInstanceWorker;
    }

    public void setComponentNodeInstanceWorker(ComponentNodeInstance componentNodeInstanceWorker) {
        this.componentNodeInstanceWorker = componentNodeInstanceWorker;
    }

    public ApplicationInstance getApplicationInstance() {
        return applicationInstance;
    }

    public void setApplicationInstance(ApplicationInstance applicationInstance) {
        this.applicationInstance = applicationInstance;
    }

    public int getNumberOfWorkers() {
        return numberOfWorkers;
    }

    public void setNumberOfWorkers(int numberOfWorkers) {
        this.numberOfWorkers = numberOfWorkers;
    }

    public List<OrchestratorComponentNodeInstance> getServiceList() {
        return serviceList;
    }

    public void setServiceList(List<OrchestratorComponentNodeInstance> serviceList) {
        this.serviceList = serviceList;
    }

    public List<ComponentNodeInstance> getComponentNodeInstanceList() {
        return componentNodeInstanceList;
    }

    public void setComponentNodeInstanceList(List<ComponentNodeInstance> componentNodeInstanceList) {
        this.componentNodeInstanceList = componentNodeInstanceList;
    }

    public int getExistingWorkers() {
        return existingWorkers;
    }

    public void setExistingWorkers(int existingWorkers) {
        this.existingWorkers = existingWorkers;
    }

    public OrchestratorApplicationInstance getOrchestratorApplicationInstance() {
        return orchestratorApplicationInstance;
    }

    public void setOrchestratorApplicationInstance(OrchestratorApplicationInstance orchestratorApplicationInstance) {
        this.orchestratorApplicationInstance = orchestratorApplicationInstance;
    }
}
