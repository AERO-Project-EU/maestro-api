package eu.orchestrator.backend.transfer;

import java.util.List;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 18/4/2019
 */
public class DashboardSecurityPolicyTO {

    private Long applicationInstanceID;
    private String applicationInstanceName;
    private List<DashboardSecurityPolicyComponentTO> componentInstanceList;

    public Long getApplicationInstanceID() {
        return applicationInstanceID;
    }

    public void setApplicationInstanceID(Long applicationInstanceID) {
        this.applicationInstanceID = applicationInstanceID;
    }

    public String getApplicationInstanceName() {
        return applicationInstanceName;
    }

    public void setApplicationInstanceName(String applicationInstanceName) {
        this.applicationInstanceName = applicationInstanceName;
    }

    public List<DashboardSecurityPolicyComponentTO> getComponentInstanceList() {
        return componentInstanceList;
    }

    public void setComponentInstanceList(List<DashboardSecurityPolicyComponentTO> componentInstanceList) {
        this.componentInstanceList = componentInstanceList;
    }
}
