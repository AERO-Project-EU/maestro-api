package eu.orchestrator.spi.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class InstanceModel implements Serializable {

    private String id;

    private String name;

    private String imageID;

    private String instanceType;

    private String state;

    private String monitoringState;

    private String providerID;

    private List<String> networkIDList;

    private String KeyPairName;

    private String userData;

    private String privateIP;

    private String floatingIP;

    private String floatingPool;

    // key: networkName , value: IP
    private HashMap<String, String> ipList;

    public InstanceModel() {
    }

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

    public String getImageID() {
        return imageID;
    }

    public void setImageID(String imageID) {
        this.imageID = imageID;
    }

    public String getInstanceType() {
        return instanceType;
    }

    public void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getMonitoringState() {
        return monitoringState;
    }

    public void setMonitoringState(String monitoringState) {
        this.monitoringState = monitoringState;
    }

    public String getProviderID() {
        return providerID;
    }

    public void setProviderID(String providerID) {
        this.providerID = providerID;
    }

    public List<String> getNetworkIDList() {
        return networkIDList;
    }

    public void setNetworkIDList(List<String> networkIDList) {
        this.networkIDList = networkIDList;
    }

    public String getKeyPairName() {
        return KeyPairName;
    }

    public void setKeyPairName(String keyPairName) {
        KeyPairName = keyPairName;
    }

    public String getUserData() {
        return userData;
    }

    public void setUserData(String userData) {
        this.userData = userData;
    }

    public String getPrivateIP() {
        return privateIP;
    }

    public void setPrivateIP(String privateIP) {
        this.privateIP = privateIP;
    }

    public HashMap<String, String> getIpList() {
        return ipList;
    }

    public void setIpList(HashMap<String, String> ipList) {
        this.ipList = ipList;
    }

    public String getFloatingIP() {
        return floatingIP;
    }

    public void setFloatingIP(String floatingIP) {
        this.floatingIP = floatingIP;
    }

    public String getFloatingPool() {
        return floatingPool;
    }

    public void setFloatingPool(String floatingPool) {
        this.floatingPool = floatingPool;
    }
}
