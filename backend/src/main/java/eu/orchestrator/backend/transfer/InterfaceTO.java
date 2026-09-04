package eu.orchestrator.backend.transfer;

import java.io.Serializable;
import java.util.Date;

public class InterfaceTO implements Serializable {

    private Long interfaceID;
    private String name;
    private String port;
    private String interfaceType;
    private String transmissionProtocol;
    private Long componentID;
    private Date dateCreated;
    private Date lastModified;

    public InterfaceTO() {
    }

    public Long getInterfaceID() {
        return interfaceID;
    }

    public void setInterfaceID(Long interfaceID) {
        this.interfaceID = interfaceID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getInterfaceType() {
        return interfaceType;
    }

    public void setInterfaceType(String interfaceType) {
        this.interfaceType = interfaceType;
    }

    public String getTransmissionProtocol() {
        return transmissionProtocol;
    }

    public void setTransmissionProtocol(String transmissionProtocol) {
        this.transmissionProtocol = transmissionProtocol;
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

    public Long getComponentID() {
        return componentID;
    }

    public void setComponentID(Long componentID) {
        this.componentID = componentID;
    }

}
