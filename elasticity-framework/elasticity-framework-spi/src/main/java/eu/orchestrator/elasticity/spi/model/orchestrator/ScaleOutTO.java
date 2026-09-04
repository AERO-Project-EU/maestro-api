package eu.orchestrator.elasticity.spi.model.orchestrator;

import eu.orchestrator.transfer.entities.orchestrator.OrchestratorApplicationInstance;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorComponentNodeInstance;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 19/7/2019
 */
public class ScaleOutTO {

    private OrchestratorApplicationInstance scalingComposeObject;
    private OrchestratorComponentNodeInstance serviceToScale;
    private Boolean proceedWithScaling;
    private Integer workersToRequest;
    private Integer runningWorkers;

    public ScaleOutTO() {
        runningWorkers = 0;
        serviceToScale = null;
    }

    public OrchestratorApplicationInstance getScalingComposeObject() {
        return scalingComposeObject;
    }

    public void setScalingComposeObject(OrchestratorApplicationInstance scalingComposeObject) {
        this.scalingComposeObject = scalingComposeObject;
    }

    public OrchestratorComponentNodeInstance getServiceToScale() {
        return serviceToScale;
    }

    public void setServiceToScale(OrchestratorComponentNodeInstance serviceToScale) {
        this.serviceToScale = serviceToScale;
    }

    public Boolean getProceedWithScaling() {
        return proceedWithScaling;
    }

    public void setProceedWithScaling(Boolean proceedWithScaling) {
        this.proceedWithScaling = proceedWithScaling;
    }

    public Integer getWorkersToRequest() {
        return workersToRequest;
    }

    public void setWorkersToRequest(Integer workersToRequest) {
        this.workersToRequest = workersToRequest;
    }

    public Integer getRunningWorkers() {
        return runningWorkers;
    }

    public void setRunningWorkers(Integer runningWorkers) {
        this.runningWorkers = runningWorkers;
    }
}
