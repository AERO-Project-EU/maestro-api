package eu.orchestrator.backend.transfer;

import java.io.Serializable;

public class InterfaceInstanceTO implements Serializable {

    private Long interfaceInstanceID;
    private InterfaceTO interfaceObj;
    private String name;
    private String port;

    public InterfaceInstanceTO() {
    }

    public Long getInterfaceInstanceID() {
        return interfaceInstanceID;
    }

    public void setInterfaceInstanceID(Long interfaceInstanceID) {
        this.interfaceInstanceID = interfaceInstanceID;
    }

    public InterfaceTO getInterfaceObj() {
        return interfaceObj;
    }

    public void setInterfaceObj(InterfaceTO interfaceObj) {
        this.interfaceObj = interfaceObj;
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
}
