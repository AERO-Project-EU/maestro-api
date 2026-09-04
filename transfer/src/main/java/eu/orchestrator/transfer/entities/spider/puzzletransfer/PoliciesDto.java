package eu.orchestrator.transfer.entities.spider.puzzletransfer;


import java.io.Serializable;
import java.util.List;


public class PoliciesDto implements Serializable {

    private List<ExpertSystemDto> policies;


    public List<ExpertSystemDto> getPolicies() {
        return policies;
    }

    public void setPolicies(List<ExpertSystemDto> policies) {
        this.policies = policies;
    }
}
