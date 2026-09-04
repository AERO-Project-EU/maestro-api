package eu.orchestrator.elasticity.spi.model.metricModel;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 16/9/2019
 */
public enum Elasticity {

    ScaleOutPolicyRequest(1),
    ScaleOutElasticity(3),
    ScaleOutBackendReply(5),
    ScaleOutOrchestrator(7),
    ScaleOutTotal(9),

    ScaleInPolicyRequest(8),
    ScaleInPreElasticity(6),
    ScaleInOrchestrator(4),
    ScaleInPostElasticity(2),
    ScaleInTotal(0);

    private final int status;

    Elasticity(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
