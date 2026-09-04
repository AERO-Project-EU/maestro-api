package eu.orchestrator.transfer.entities.orchestrator;

import java.io.Serializable;
import java.util.List;

/**
 * @author Konstantinos Theodosiou
 */
public class OrchestratorInstance implements Serializable {

    String name;
    String userData;
    List<String> networkIDList;
    String keyPair;
    String id;
    String imageID;
    String instanceType;
    String floatingIP;
    String floatingPool;

    public OrchestratorInstance(){

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserData() {
        return userData;
    }

    public void setUserData(String userData) {
        this.userData = userData;
    }

    public List<String> getNetworkIDList() {
        return networkIDList;
    }

    public void setNetworkIDList(List<String> networkIDList) {
        this.networkIDList = networkIDList;
    }

    public String getKeyPair() {
        return keyPair;
    }

    public void setKeyPair(String keyPair) {
        this.keyPair = keyPair;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
