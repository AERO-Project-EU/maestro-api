package eu.orchestrator.transfer.entities.spider.puzzletransfer;

import java.io.Serializable;


public class ExpertSystemResponseDto implements Serializable {

    private String id;

    private String name;
    

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
