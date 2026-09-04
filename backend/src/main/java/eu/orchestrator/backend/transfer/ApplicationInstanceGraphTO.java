package eu.orchestrator.backend.transfer;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class ApplicationInstanceGraphTO implements Serializable {

    private Long applicationInstanceID;
    private String hexID;
    private String name;
    private String description;
    private Boolean overlay;
    private Long deploymentTimestamp;
    private ApplicationGraphTO application;
    private ProviderTO provider;
    private String grafanaUUID;
    private boolean showGrafanaLink = true;
    private boolean showPrometheusLink = true;
    private boolean showVulnerabilitiesLink = true;
    private boolean showKibanaLink = true;
    private List<ComponentNodeInstanceTO> componentNodeInstances;
    private List<ConstraintTO> constraints;
    private List<GraphLinkNodeInstanceTO> graphLinkNodeInstances;
    private String status;
    private Date dateDeployed;

    public ApplicationInstanceGraphTO() {
    }

    public Long getApplicationInstanceID() {
        return applicationInstanceID;
    }

    public void setApplicationInstanceID(Long applicationInstanceID) {
        this.applicationInstanceID = applicationInstanceID;
    }

    public String getHexID() {
        return hexID;
    }

    public void setHexID(String hexID) {
        this.hexID = hexID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ApplicationGraphTO getApplication() {
        return application;
    }

    public void setApplication(ApplicationGraphTO application) {
        this.application = application;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getOverlay() {
        return overlay;
    }

    public void setOverlay(Boolean overlay) {
        this.overlay = overlay;
    }

    public Long getDeploymentTimestamp() {
        return deploymentTimestamp;
    }

    public void setDeploymentTimestamp(Long deploymentTimestamp) {
        this.deploymentTimestamp = deploymentTimestamp;
    }

    public ProviderTO getProvider() {
        return provider;
    }

    public void setProvider(ProviderTO provider) {
        this.provider = provider;
    }

    public List<ComponentNodeInstanceTO> getComponentNodeInstances() {
        return componentNodeInstances;
    }

    public void setComponentNodeInstances(List<ComponentNodeInstanceTO> componentNodeInstances) {
        this.componentNodeInstances = componentNodeInstances;
    }

    public List<ConstraintTO> getConstraints() {
        return constraints;
    }

    public void setConstraints(List<ConstraintTO> constraints) {
        this.constraints = constraints;
    }

    public List<GraphLinkNodeInstanceTO> getGraphLinkNodeInstances() {
        return graphLinkNodeInstances;
    }

    public void setGraphLinkNodeInstances(
            List<GraphLinkNodeInstanceTO> graphLinkNodeInstances) {
        this.graphLinkNodeInstances = graphLinkNodeInstances;
    }

    public String getGrafanaUUID() {
        return grafanaUUID;
    }

    public void setGrafanaUUID(String grafanaUUID) {
        this.grafanaUUID = grafanaUUID;
    }

    public boolean isShowGrafanaLink() {
        return showGrafanaLink;
    }

    public void setShowGrafanaLink(boolean showGrafanaLink) {
        this.showGrafanaLink = showGrafanaLink;
    }

    public boolean isShowPrometheusLink() {
        return showPrometheusLink;
    }

    public void setShowPrometheusLink(boolean showPrometheusLink) {
        this.showPrometheusLink = showPrometheusLink;
    }

    public boolean isShowVulnerabilitiesLink() {
        return showVulnerabilitiesLink;
    }

    public void setShowVulnerabilitiesLink(boolean showVulnerabilitiesLink) {
        this.showVulnerabilitiesLink = showVulnerabilitiesLink;
    }

    public boolean isShowKibanaLink() {
        return showKibanaLink;
    }

    public void setShowKibanaLink(boolean showKibanaLink) {
        this.showKibanaLink = showKibanaLink;
    }

    public Date getDateDeployed() {
        return dateDeployed;
    }

    public void setDateDeployed(Date dateDeployed) {
        this.dateDeployed = dateDeployed;
    }
}
