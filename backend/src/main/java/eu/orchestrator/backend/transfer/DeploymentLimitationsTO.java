package eu.orchestrator.backend.transfer;

import eu.orchestrator.transfer.entities.orchestrator.OrchestratorApplicationInstance;

import java.io.Serializable;
import java.util.List;

public class DeploymentLimitationsTO implements Serializable {

    private String businessGoal;

    private DeploymentLimitationsBudgetRequirementTO budgetRequirement;

    private List<DeploymentLimitationsLocationTO> collocated;

    private List<DeploymentLimitationsLocationTO> antiAffinity;

    private List<DeploymentLimitationsRegionTO> excludeRegions;

    private List<DeploymentLimitationsRegionTO> preferredRegions;

    private List<DeploymentLimitationsOptimizationVariablesTO> optimizationVariables;

    private OrchestratorApplicationInstance jsonDeployment;

    private String organizationName;

    public String getBusinessGoal() {
        return businessGoal;
    }

    public void setBusinessGoal(String businessGoal) {
        this.businessGoal = businessGoal;
    }

    public DeploymentLimitationsBudgetRequirementTO getBudgetRequirement() {
        return budgetRequirement;
    }

    public void setBudgetRequirement(
            DeploymentLimitationsBudgetRequirementTO budgetRequirement) {
        this.budgetRequirement = budgetRequirement;
    }

    public List<DeploymentLimitationsLocationTO> getCollocated() {
        return collocated;
    }

    public void setCollocated(
            List<DeploymentLimitationsLocationTO> collocated) {
        this.collocated = collocated;
    }

    public List<DeploymentLimitationsLocationTO> getAntiAffinity() {
        return antiAffinity;
    }

    public void setAntiAffinity(
            List<DeploymentLimitationsLocationTO> antiAffinity) {
        this.antiAffinity = antiAffinity;
    }

    public List<DeploymentLimitationsRegionTO> getExcludeRegions() {
        return excludeRegions;
    }

    public void setExcludeRegions(
            List<DeploymentLimitationsRegionTO> excludeRegions) {
        this.excludeRegions = excludeRegions;
    }

    public List<DeploymentLimitationsRegionTO> getPreferredRegions() {
        return preferredRegions;
    }

    public void setPreferredRegions(
            List<DeploymentLimitationsRegionTO> preferredRegions) {
        this.preferredRegions = preferredRegions;
    }

    public OrchestratorApplicationInstance getJsonDeployment() {
        return jsonDeployment;
    }

    public void setJsonDeployment(OrchestratorApplicationInstance jsonDeployment) {
        this.jsonDeployment = jsonDeployment;
    }

    public List<DeploymentLimitationsOptimizationVariablesTO> getOptimizationVariables() {
        return optimizationVariables;
    }

    public void setOptimizationVariables(
            List<DeploymentLimitationsOptimizationVariablesTO> optimizationVariables) {
        this.optimizationVariables = optimizationVariables;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }
}
