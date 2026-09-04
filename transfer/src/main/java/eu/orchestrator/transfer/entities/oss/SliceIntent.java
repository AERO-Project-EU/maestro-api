package eu.orchestrator.transfer.entities.oss;

import eu.orchestrator.transfer.entities.orchestrator.OrchestratorApplicationInstance;

import java.io.Serializable;
import java.util.List;

public class SliceIntent implements Serializable {

    private String sliceIntentID;
    private String applicationInstanceHexID;
    private String applicationInstanceID;
    private String name;
    private String callbackURL;
    private String description;
    private InfrastructureType infrastructureType;
    private SliceOrchestrator sliceOrchestrator;
    private OSSAuthenticationDetails authenticationDetails;
    private List<ComponentNodeInstanceTO> componentNodeInstances;
    private List<ConstraintTO> constraints;
    private List<GraphLinkNodeInstanceTO> graphLinkNodeInstances;
    private OrchestratorApplicationInstance orchestratorApplicationInstance;

    public SliceIntent() {
    }

    public String getApplicationInstanceHexID() {
        return applicationInstanceHexID;
    }

    public void setApplicationInstanceHexID(String applicationInstanceHexID) {
        this.applicationInstanceHexID = applicationInstanceHexID;
    }

    public String getSliceIntentID() {
        return sliceIntentID;
    }

    public void setSliceIntentID(String sliceIntentID) {
        this.sliceIntentID = sliceIntentID;
    }

    public String getApplicationInstanceID() {
        return applicationInstanceID;
    }

    public void setApplicationInstanceID(String applicationInstanceID) {
        this.applicationInstanceID = applicationInstanceID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public InfrastructureType getInfrastructureType() {
        return infrastructureType;
    }

    public void setInfrastructureType(InfrastructureType infrastructureType) {
        this.infrastructureType = infrastructureType;
    }

    public SliceOrchestrator getSliceOrchestrator() {
        return sliceOrchestrator;
    }

    public void setSliceOrchestrator(SliceOrchestrator sliceOrchestrator) {
        this.sliceOrchestrator = sliceOrchestrator;
    }

    public OSSAuthenticationDetails getAuthenticationDetails() {
        return authenticationDetails;
    }

    public void setAuthenticationDetails(OSSAuthenticationDetails authenticationDetails) {
        this.authenticationDetails = authenticationDetails;
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

    public void setGraphLinkNodeInstances(List<GraphLinkNodeInstanceTO> graphLinkNodeInstances) {
        this.graphLinkNodeInstances = graphLinkNodeInstances;
    }

    public String getCallbackURL() {
        return callbackURL;
    }

    public void setCallbackURL(String callbackURL) {
        this.callbackURL = callbackURL;
    }

    public OrchestratorApplicationInstance getOrchestratorApplicationInstance() {
        return orchestratorApplicationInstance;
    }

    public void setOrchestratorApplicationInstance(OrchestratorApplicationInstance orchestratorApplicationInstance) {
        this.orchestratorApplicationInstance = orchestratorApplicationInstance;
    }
}
