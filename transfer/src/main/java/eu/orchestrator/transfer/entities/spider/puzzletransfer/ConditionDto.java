package eu.orchestrator.transfer.entities.spider.puzzletransfer;

import java.io.Serializable;


public class ConditionDto implements Serializable {

    private StreamDto stream;

    private DroolsConfigDto drools;


    public StreamDto getStream() {
        return stream;
    }

    public void setStream(StreamDto stream) {
        this.stream = stream;
    }

    public DroolsConfigDto getDrools() {
        return drools;
    }

    public void setDrools(DroolsConfigDto drools) {
        this.drools = drools;
    }
}
