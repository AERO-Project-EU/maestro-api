package eu.orchestrator.elasticity.spi.model.orchestrator;

import eu.orchestrator.elasticity.spi.model.orchestrator.ControllerStatus;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 26/7/2019
 */
public class UpdateControllerTO {


    private ControllerStatus controllerStatus;
    private Object controllerMetadata;

    public ControllerStatus getControllerStatus() {
        return controllerStatus;
    }

    public void setControllerStatus(ControllerStatus controllerStatus) {
        this.controllerStatus = controllerStatus;
    }

    public Object getControllerMetadata() {
        return controllerMetadata;
    }

    public void setControllerMetadata(Object controllerMetadata) {
        this.controllerMetadata = controllerMetadata;
    }
}
