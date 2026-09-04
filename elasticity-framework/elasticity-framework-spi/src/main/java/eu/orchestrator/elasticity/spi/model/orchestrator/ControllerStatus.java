package eu.orchestrator.elasticity.spi.model.orchestrator;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 26/7/2019
 */
public enum ControllerStatus {

    START_CONTROLLER_STATUS (1),
    FIRST_CONTROLLER_CONFIG (2),
    EMPTY_CONTROLLER_CONFIG (3),
    NO_CONTROLLER_UPDATE_NEEDED (4),
    CONTROLLER_CONFIG_UPDATED (5);

    private Integer status;

    ControllerStatus(Integer status) {
        this.status = status;
    }

    public Integer getStatus() {
        return status;
    }

    protected void setStatus(Integer status) {
        this.status = status;
    }

    public static ControllerStatus nameOf(Integer status){
        for(ControllerStatus e : ControllerStatus.values()){
            if(status == e.getStatus()) return e;
        }
        return null;
    }
}
