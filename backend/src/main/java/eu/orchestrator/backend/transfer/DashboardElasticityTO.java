package eu.orchestrator.backend.transfer;

import java.util.List;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 18/4/2019
 */
public class DashboardElasticityTO {

    Long applicationInstanceID;
    String applicationInstanceName;
    List<DashboardElasticityComponentTO> dashboardElasticityComponentTOList;

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

    public List<DashboardElasticityComponentTO> getDashboardElasticityComponentTOList() {
        return dashboardElasticityComponentTOList;
    }

    public void setDashboardElasticityComponentTOList(List<DashboardElasticityComponentTO> dashboardElasticityComponentTOList) {
        this.dashboardElasticityComponentTOList = dashboardElasticityComponentTOList;
    }
}
