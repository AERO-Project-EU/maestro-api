package eu.orchestrator.transfer.entities.spider.puzzletransfer;

import java.io.Serializable;

public class ExpertSystemDto implements Serializable {

    private String name;
    private ConditionDto condition;
    private ActionDto action;

    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ConditionDto getCondition() {
        return condition;
    }

    public void setCondition(ConditionDto condition) {
        this.condition = condition;
    }

    public ActionDto getAction() {
        return action;
    }

    public void setAction(ActionDto action) {
        this.action = action;
    }
}
