package eu.orchestrator.transfer.entities.iotstack;

import java.io.Serializable;

/**
 * @author Konstantinos Theodosiou
 */
public class IoTBootRequest implements Serializable {

    // Machine id returned by the provider
    private String id;

    // Credentials
    private Credentials credentials;

    //Agent arguments
    private String graphID;
    private String graphInstanceID;
    private String componentNodeInstanceID;
    private String componentNodeID;
    private Boolean flag;
    private Boolean lb;
    private Boolean ipv6;

    //Consul agruments
    private String masterIPv6;

    //CJDNS arguments
    private String masterIPv4;
    private String port;
    private String login;
    private String password;
    private String publicKey;
    private String peerName;

    // Flavor
    private Integer vCPUs;
    private Integer ram;
    private Integer storage;

    private String nexusIPv6;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    public String getGraphID() {
        return graphID;
    }

    public void setGraphID(String graphID) {
        this.graphID = graphID;
    }

    public String getGraphInstanceID() {
        return graphInstanceID;
    }

    public void setGraphInstanceID(String graphInstanceID) {
        this.graphInstanceID = graphInstanceID;
    }

    public String getComponentNodeInstanceID() {
        return componentNodeInstanceID;
    }

    public void setComponentNodeInstanceID(String componentNodeInstanceID) {
        this.componentNodeInstanceID = componentNodeInstanceID;
    }

    public String getComponentNodeID() {
        return componentNodeID;
    }

    public void setComponentNodeID(String componentNodeID) {
        this.componentNodeID = componentNodeID;
    }

    public Boolean getFlag() {
        return flag;
    }

    public void setFlag(Boolean flag) {
        this.flag = flag;
    }

    public Boolean getLb() {
        return lb;
    }

    public void setLb(Boolean lb) {
        this.lb = lb;
    }

    public Boolean getIpv6() {
        return ipv6;
    }

    public void setIpv6(Boolean ipv6) {
        this.ipv6 = ipv6;
    }

    public String getMasterIPv6() {
        return masterIPv6;
    }

    public void setMasterIPv6(String masterIPv6) {
        this.masterIPv6 = masterIPv6;
    }

    public String getMasterIPv4() {
        return masterIPv4;
    }

    public void setMasterIPv4(String masterIPv4) {
        this.masterIPv4 = masterIPv4;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPeerName() {
        return peerName;
    }

    public void setPeerName(String peerName) {
        this.peerName = peerName;
    }

    public Integer getvCPUs() {
        return vCPUs;
    }

    public void setvCPUs(Integer vCPUs) {
        this.vCPUs = vCPUs;
    }

    public Integer getRam() {
        return ram;
    }

    public void setRam(Integer ram) {
        this.ram = ram;
    }

    public Integer getStorage() {
        return storage;
    }

    public void setStorage(Integer storage) {
        this.storage = storage;
    }

    public String getNexusIPv6() {
        return nexusIPv6;
    }

    public void setNexusIPv6(String nexusIPv6) {
        this.nexusIPv6 = nexusIPv6;
    }
}
