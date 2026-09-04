package eu.orchestrator.backend.transfer;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 18/4/2019
 */
public class DashboardSecurityPolicyComponentTO {

    private Long componentNodeInstanceID;
    private String componentNodeInstanceName;
    private Long count;

    public Long getComponentNodeInstanceID() {
        return componentNodeInstanceID;
    }

    public void setComponentNodeInstanceID(Long componentNodeInstanceID) {
        this.componentNodeInstanceID = componentNodeInstanceID;
    }

    public String getComponentNodeInstanceName() {
        return componentNodeInstanceName;
    }

    public void setComponentNodeInstanceName(String componentNodeInstanceName) {
        this.componentNodeInstanceName = componentNodeInstanceName;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
