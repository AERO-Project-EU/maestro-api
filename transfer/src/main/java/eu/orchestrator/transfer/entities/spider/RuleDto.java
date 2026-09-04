package eu.orchestrator.transfer.entities.spider;

import java.io.Serializable;

public class RuleDto implements Serializable {

    //Application Instance ID
    private Long sgi;
    private String type;
    private Integer points;
    private Integer step;
    private String rule;


    public Long getSgi() {
        return sgi;
    }

    public void setSgi(Long sgi) {
        this.sgi = sgi;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public Integer getStep() {
        return step;
    }

    public void setStep(Integer step) {
        this.step = step;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }
}
