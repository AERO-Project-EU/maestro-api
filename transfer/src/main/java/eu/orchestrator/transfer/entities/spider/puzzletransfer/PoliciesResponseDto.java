package eu.orchestrator.transfer.entities.spider.puzzletransfer;

import java.io.Serializable;
import java.util.List;

public class PoliciesResponseDto implements Serializable {

    private List<ExpertSystemResponseDto> policies;


    public List<ExpertSystemResponseDto> getPolicies() {
        return policies;
    }

    public void setPolicies(List<ExpertSystemResponseDto> policies) {
        this.policies = policies;
    }
}
