package eu.orchestrator.transfer.entities.rainbowk8s;

import java.io.Serializable;

public class LinkType implements Serializable {

    private String protocol;
    private String minQualityClass;


    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getMinQualityClass() {
        return minQualityClass;
    }

    public void setMinQualityClass(String minQualityClass) {
        this.minQualityClass = minQualityClass;
    }
}
