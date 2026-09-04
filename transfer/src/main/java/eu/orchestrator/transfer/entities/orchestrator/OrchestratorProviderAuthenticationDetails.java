package eu.orchestrator.transfer.entities.orchestrator;

import java.io.Serializable;

public class OrchestratorProviderAuthenticationDetails implements Serializable {

    private String id;
    private String name;
    private String adapterType;
    private String adapterImplementation;
    private String endpoint;
    private String username;
    private String password;
    private String publicKey;
    private String privateKey;
    private String meshIdentifier;
    private String imageID;
    private String networkID;
    private String proxy;
    private String region;
    private String domain;
    private String project;
    private String publicNetwork;


    public OrchestratorProviderAuthenticationDetails() {
    }

    public String getAdapterType() {
        return adapterType;
    }

    public void setAdapterType(String adapterType) {
        this.adapterType = adapterType;
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

    public String getAdapterImplementation() {
        return adapterImplementation;
    }

    public void setAdapterImplementation(String adapterImplementation) {
        this.adapterImplementation = adapterImplementation;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
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

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
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

    public String getMeshIdentifier() {
        return meshIdentifier;
    }

    public void setMeshIdentifier(String meshIdentifier) {
        this.meshIdentifier = meshIdentifier;
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

    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    public String getPublicNetwork() {
        return publicNetwork;
    }

    public void setPublicNetwork(String publicNetwork) {
        this.publicNetwork = publicNetwork;
    }
}
