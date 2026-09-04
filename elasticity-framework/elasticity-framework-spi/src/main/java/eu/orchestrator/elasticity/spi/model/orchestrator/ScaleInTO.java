package eu.orchestrator.elasticity.spi.model.orchestrator;

import eu.orchestrator.transfer.entities.orchestrator.internal.ServiceStatus;

import java.util.List;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 19/7/2019
 */
public class ScaleInTO {

    private Integer workersToRemove;
    private Boolean proceedWithScaling;
    private List<ServiceStatus> serviceStatusList;
    private List<ServiceStatus> qualifiedForRemoval;

    public Integer getWorkersToRemove() {
        return workersToRemove;
    }

    public void setWorkersToRemove(Integer workersToRemove) {
        this.workersToRemove = workersToRemove;
    }

    public Boolean getProceedWithScaling() {
        return proceedWithScaling;
    }

    public void setProceedWithScaling(Boolean proceedWithScaling) {
        this.proceedWithScaling = proceedWithScaling;
    }

    public List<ServiceStatus> getServiceStatusList() {
        return serviceStatusList;
    }

    public void setServiceStatusList(List<ServiceStatus> serviceStatusList) {
        this.serviceStatusList = serviceStatusList;
    }

    public List<ServiceStatus> getQualifiedForRemoval() {
        return qualifiedForRemoval;
    }

    public void setQualifiedForRemoval(List<ServiceStatus> qualifiedForRemoval) {
        this.qualifiedForRemoval = qualifiedForRemoval;
    }
}
