package eu.orchestrator.transfer.entities.backend;

import java.util.List;

/**
 * @author Panagiotis Parthenis.
 */
public class SecurityConfiguration {

    private String graphHexId;
    private String graphInstanceHexId;
    private String componentNodeHexId;
    private String componentNodeInstanceHexId;
    private String resultHexId;
    private SecurityConfigurationType securityConfigurationType;
    private String certificate;
    private List<SecurityConfigurationResult> securityConfigurationResultList;

    public String getGraphHexId() {
        return graphHexId;
    }

    public void setGraphHexId(String graphHexId) {
        this.graphHexId = graphHexId;
    }

    public String getGraphInstanceHexId() {
        return graphInstanceHexId;
    }

    public void setGraphInstanceHexId(String graphInstanceHexId) {
        this.graphInstanceHexId = graphInstanceHexId;
    }

    public String getComponentNodeHexId() {
        return componentNodeHexId;
    }

    public void setComponentNodeHexId(String componentNodeHexId) {
        this.componentNodeHexId = componentNodeHexId;
    }

    public String getComponentNodeInstanceHexId() {
        return componentNodeInstanceHexId;
    }

    public void setComponentNodeInstanceHexId(String componentNodeInstanceHexId) {
        this.componentNodeInstanceHexId = componentNodeInstanceHexId;
    }

    public String getResultHexId() {
        return resultHexId;
    }

    public void setResultHexId(String resultHexId) {
        this.resultHexId = resultHexId;
    }

    public SecurityConfigurationType getSecurityConfigurationType() {
        return securityConfigurationType;
    }

    public void setSecurityConfigurationType(SecurityConfigurationType securityConfigurationType) {
        this.securityConfigurationType = securityConfigurationType;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public List<SecurityConfigurationResult> getSecurityConfigurationResultList() {
        return securityConfigurationResultList;
    }

    public void setSecurityConfigurationResultList(List<SecurityConfigurationResult> securityConfigurationResultList) {
        this.securityConfigurationResultList = securityConfigurationResultList;
    }

    public enum SecurityConfigurationType {
        CONFIGURATION_INTEGRITY_VERIFICATION,
        RUNTIME_FILE_INTEGRITY,
        FORENSIC_ACTIVATION,
        TSS_TRACING,
        BOOT_TIME_FUZZY;
    }

    @Override
    public String toString() {
        return "SecurityConfiguration{"
                + "graphHexId='" + graphHexId + '\''
                + ", graphInstanceHexId='" + graphInstanceHexId + '\''
                + ", componentNodeHexId='" + componentNodeHexId + '\''
                + ", componentNodeInstanceHexId='" + componentNodeInstanceHexId + '\''
                + ", securityConfigurationType=" + securityConfigurationType
                + '}';
    }
}
