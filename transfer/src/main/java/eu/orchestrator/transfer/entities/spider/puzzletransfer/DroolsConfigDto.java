package eu.orchestrator.transfer.entities.spider.puzzletransfer;

import java.io.Serializable;


public class DroolsConfigDto implements Serializable {

    private int inertiaTime;
    

    public int getInertiaTime() {
        return inertiaTime;
    }

    public void setInertiaTime(int inertiaTime) {
        this.inertiaTime = inertiaTime;
    }
}
