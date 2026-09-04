package eu.orchestrator.transfer.entities.spider.puzzletransfer;

import java.util.List;


public class DeletePoliciesDto {

    private List<String> policies;
    

    public List<String> getPolicies() {
        return policies;
    }

    public void setPolicies(List<String> policies) {
        this.policies = policies;
    }
}
