package eu.orchestrator.transfer.entities.backend.repository.component;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 24/1/22
 */
public class InterfaceTo implements Serializable {

    private Long interfaceID;
    private String name;
    private String port;
    private String vna;
    private String interfaceType;
    private String transmissionProtocol;
    private Date dateCreated;
    private Date lastModified;

    public InterfaceTo() {
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

    public String getVna() {
        return vna;
    }

    public void setVna(String vna) {
        this.vna = vna;
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
}
