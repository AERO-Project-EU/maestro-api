package eu.orchestrator.transfer.entities.spider;

import java.io.Serializable;

public class ScoreDto implements Serializable {

    private String sgi;
    private String type;
    private Integer points;
    private Integer step;
    private String timestamp;


    public ScoreDto() {
        this.timestamp = "@TIMESTAMP";
    }

    public String getSgi() {
        return sgi;
    }

    public void setSgi(String sgi) {
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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
