package eu.orchestrator.transfer.entities.ui;

import java.io.Serializable;

public class UIEnum implements Serializable {

    private String name;
    private String friendlyName;

    public UIEnum() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }
}
