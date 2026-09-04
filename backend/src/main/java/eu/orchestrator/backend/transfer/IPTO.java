package eu.orchestrator.backend.transfer;

import java.io.Serializable;
import java.util.Date;

public class IPTO implements Serializable {

    private Long componentNodeInstanceIPID;
    private Long componentNodeInstanceID;
    private Long applicationInstanceID;
    private String graphInstanceID;
    private String componentNodeInstanceName;
    private String ip;
    private String type;
    private String network;
    private Date dateCreated;
    private Date lastModified;

    public IPTO() {
    }

    public Long getComponentNodeInstanceIPID() {
        return componentNodeInstanceIPID;
    }

    public void setComponentNodeInstanceIPID(Long componentNodeInstanceIPID) {
        this.componentNodeInstanceIPID = componentNodeInstanceIPID;
    }

    public Long getComponentNodeInstanceID() {
        return componentNodeInstanceID;
    }

    public void setComponentNodeInstanceID(Long componentNodeInstanceID) {
        this.componentNodeInstanceID = componentNodeInstanceID;
    }

    public Long getApplicationInstanceID() {
        return applicationInstanceID;
    }

    public void setApplicationInstanceID(Long applicationInstanceID) {
        this.applicationInstanceID = applicationInstanceID;
    }

    public String getGraphInstanceID() {
        return graphInstanceID;
    }

    public void setGraphInstanceID(String graphInstanceID) {
        this.graphInstanceID = graphInstanceID;
    }

    public String getComponentNodeInstanceName() {
        return componentNodeInstanceName;
    }

    public void setComponentNodeInstanceName(String componentNodeInstanceName) {
        this.componentNodeInstanceName = componentNodeInstanceName;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }
}
