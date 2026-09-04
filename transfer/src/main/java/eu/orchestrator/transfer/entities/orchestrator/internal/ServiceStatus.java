package eu.orchestrator.transfer.entities.orchestrator.internal;

import eu.orchestrator.transfer.entities.orchestrator.OrchestratorIP;

import java.util.List;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 19/7/2019
 */
public class ServiceStatus {

    private String componentNodeInstanceID;
    private String componentNodeInstanceHexID;
    private String componentNodeInstanceName;
    private String componentNodeID;
    private String componentNodeHexID;
    private String componentNodeName;

    private String reportedChange;
    private String changeType;
    private Boolean firstInfo;
    private Integer status;

    private Long healthCheckTimeLimit;  // How many seconds the loop must wait in order to put error status
    private Long healthCheckFirstDate;  // When is the first date that the agent stated that the container is up and running

    private String vmIP;
    private String vmID;
    private String nodeName;
    private String floatingIP;
    private Boolean accessInterface;
    private String privateIP;
    private String privateIPv6;
    private List<OrchestratorIP> ipList;

    private ScalabilityType scalability;
    private String balancedByComponentNodeHexID;

    public enum ScalabilityType {
        HORIZONTAL("HORIZONTAL"),
        VERTICAL("VERTICAL"),
        DIAGONAL("DIAGONAL"),
        LAMBDA_FUNCTION("LAMBDA_FUNCTION"),
        NONE("NONE");

        private String scalabilityType;

        ScalabilityType(String scalabilyType) {
            this.scalabilityType = scalabilityType;
        }

        public String getScalabilityType() {
            return scalabilityType;
        }

        protected void setScalabilityType(String scalabilityType) {
            this.scalabilityType = scalabilityType;
        }
    }

    public enum ChangeType {

        AgentStatusChange(0),

        GraphStatusChange(1);

        private Integer changeType;

        ChangeType(Integer type) {
            this.changeType = type;
        }

        public Integer getChangeType() {
            return changeType;
        }

        protected void setChangeType(Integer type) {
            this.changeType = type;
        }
    }

    public String getComponentNodeInstanceID() {
        return componentNodeInstanceID;
    }

    public void setComponentNodeInstanceID(String componentNodeInstanceID) {
        this.componentNodeInstanceID = componentNodeInstanceID;
    }

    public String getComponentNodeInstanceHexID() {
        return componentNodeInstanceHexID;
    }

    public void setComponentNodeInstanceHexID(String componentNodeInstanceHexID) {
        this.componentNodeInstanceHexID = componentNodeInstanceHexID;
    }

    public String getComponentNodeInstanceName() {
        return componentNodeInstanceName;
    }

    public void setComponentNodeInstanceName(String componentNodeInstanceName) {
        this.componentNodeInstanceName = componentNodeInstanceName;
    }

    public String getComponentNodeID() {
        return componentNodeID;
    }

    public void setComponentNodeID(String componentNodeID) {
        this.componentNodeID = componentNodeID;
    }

    public String getComponentNodeHexID() {
        return componentNodeHexID;
    }

    public void setComponentNodeHexID(String componentNodeHexID) {
        this.componentNodeHexID = componentNodeHexID;
    }

    public String getComponentNodeName() {
        return componentNodeName;
    }

    public void setComponentNodeName(String componentNodeName) {
        this.componentNodeName = componentNodeName;
    }

    public String getReportedChange() {
        return reportedChange;
    }

    public void setReportedChange(String reportedChange) {
        this.reportedChange = reportedChange;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public Boolean getFirstInfo() {
        return firstInfo;
    }

    public void setFirstInfo(Boolean firstInfo) {
        this.firstInfo = firstInfo;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getHealthCheckTimeLimit() {
        return healthCheckTimeLimit;
    }

    public void setHealthCheckTimeLimit(Long healthCheckTimeLimit) {
        this.healthCheckTimeLimit = healthCheckTimeLimit;
    }

    public Long getHealthCheckFirstDate() {
        return healthCheckFirstDate;
    }

    public void setHealthCheckFirstDate(Long healthCheckFirstDate) {
        this.healthCheckFirstDate = healthCheckFirstDate;
    }

    public String getVmIP() {
        return vmIP;
    }

    public void setVmIP(String vmIP) {
        this.vmIP = vmIP;
    }

    public String getVmID() {
        return vmID;
    }

    public void setVmID(String vmID) {
        this.vmID = vmID;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getFloatingIP() {
        return floatingIP;
    }

    public void setFloatingIP(String floatingIP) {
        this.floatingIP = floatingIP;
    }

    public Boolean getAccessInterface() {
        return accessInterface;
    }

    public void setAccessInterface(Boolean accessInterface) {
        this.accessInterface = accessInterface;
    }

    public String getPrivateIP() {
        return privateIP;
    }

    public void setPrivateIP(String privateIP) {
        this.privateIP = privateIP;
    }

    public String getPrivateIPv6() {
        return privateIPv6;
    }

    public void setPrivateIPv6(String privateIPv6) {
        this.privateIPv6 = privateIPv6;
    }

    public List<OrchestratorIP> getIpList() {
        return ipList;
    }

    public void setIpList(List<OrchestratorIP> ipList) {
        this.ipList = ipList;
    }

    public ScalabilityType getScalability() {
        return scalability;
    }

    public void setScalability(ScalabilityType scalability) {
        this.scalability = scalability;
    }

    public String getBalancedByComponentNodeHexID() {
        return balancedByComponentNodeHexID;
    }

    public void setBalancedByComponentNodeHexID(String balancedByComponentNodeHexID) {
        this.balancedByComponentNodeHexID = balancedByComponentNodeHexID;
    }
}