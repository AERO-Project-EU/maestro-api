package eu.orchestrator.elasticity.spi.model.orchestrator;

import java.io.Serializable;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 26/7/2019
 */
public class ControllerMetadata implements Serializable {

    private ControllerStatus controllerStatus;

    public ControllerMetadata() {
        this.controllerStatus = ControllerStatus.START_CONTROLLER_STATUS;
    }

    public ControllerStatus getControllerStatus() {
        return controllerStatus;
    }

    public void setControllerStatus(ControllerStatus controllerStatus) {
        this.controllerStatus = controllerStatus;
    }
}
