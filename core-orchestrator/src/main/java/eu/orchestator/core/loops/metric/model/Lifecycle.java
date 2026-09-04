package eu.orchestator.core.loops.metric.model;

/**
 * @author Konstantinos Theodosiou.
 * @email konstheodosiou@gmail.com
 * @date 16/9/2019
 */
public enum Lifecycle {
    RequestDeploy(1),
    Deployed(3),
    RequestUndeploy(2),
    Undeployed(0);

    private final int status;

    Lifecycle(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
