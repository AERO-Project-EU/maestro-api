package eu.orchestrator.transfer.entities.spider;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RulesDto implements Serializable {

    private List<RuleDto> rules;


    public List<RuleDto> getRules() {
        if (rules == null) {
            rules = new ArrayList<>();
        }
        return rules;
    }

    public void setRules(List<RuleDto> rules) {
        this.rules = rules;
    }
}
