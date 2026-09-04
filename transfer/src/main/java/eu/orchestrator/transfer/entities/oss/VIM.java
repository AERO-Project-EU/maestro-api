package eu.orchestrator.transfer.entities.oss;

import java.io.Serializable;
import java.util.List;

public class VIM implements Serializable {

    private String vimID;
    private String domain;
    private String project;
    private String username;
    private String password;
    private String caCert;
    private String token;
    private String endpoint;
    private String imageID;
    private String networkID;
    private String externalNetworkID;
    private String nexusIP;
    private List<Metadata> listOfMetadata;

    public VIM() {
    }

    public String getVimID() {
        return vimID;
    }

    public void setVimID(String vimID) {
        this.vimID = vimID;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public List<Metadata> getListOfMetadata() {
        return listOfMetadata;
    }

    public void setListOfMetadata(List<Metadata> listOfMetadata) {
        this.listOfMetadata = listOfMetadata;
    }

    public String getImageID() {
        return imageID;
    }

    public void setImageID(String imageID) {
        this.imageID = imageID;
    }

    public String getNetworkID() {
        return networkID;
    }

    public void setNetworkID(String networkID) {
        this.networkID = networkID;
    }

    public String getNexusIP() {
        return nexusIP;
    }

    public void setNexusIP(String nexusIP) {
        this.nexusIP = nexusIP;
    }

    public String getExternalNetworkID() {
        return externalNetworkID;
    }

    public void setExternalNetworkID(String externalNetworkID) {
        this.externalNetworkID = externalNetworkID;
    }

    public String getCaCert() {
        return caCert;
    }

    public void setCaCert(String caCert) {
        this.caCert = caCert;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
