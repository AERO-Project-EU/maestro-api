package eu.orchestrator.repository.domain;

import eu.orchestrator.repository.domain.ProviderType.ProviderName;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "provider")
public class Provider implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long providerID;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "provider_type", nullable = true)
    private ProviderType providerType;

    @Column(nullable = true, name = "endpoint")
    private String endpoint; //RAINBOW masterUrl

    @Column(nullable = true, name = "proxy")
    private String proxy;

    @Column(nullable = true, name = "enabled")
    private Boolean enabled = Boolean.TRUE;

    @Column(nullable = true, name = "username", length = 2000)
    private String username; //RAINBOW certificate-authority-data, change length to default

    @Transient
    private Boolean providerCredentialsChange = false;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = true, name = "password")
    private String password;

    @Column(nullable = true, name = "public_key", length = 2000)
    private String publicKey; //RAINBOW client-certificate-data, change length to default

    @Column(nullable = true, name = "private_key", length = 2500)
    private String privateKey; //RAINBOW client-key-data, change length to default, 5G OSS token field

    @Column(nullable = true, name = "image_id")
    private String imageID;

    @Column(nullable = true, name = "network_id")
    private String networkID;

    @Column(nullable = true, name = "external_network_id")
    private String externalNetworkID;

    @Column(nullable = true, name = "domain")
    private String domain;

    @Column(nullable = true, name = "mesh_identifier")
    private String meshIdentifier;

    @Column(nullable = true, name = "project")
    private String project;

    @Column(nullable = true, name = "public_network")
    private String publicNetwork;

    @Column(nullable = true, name = "is_internal", columnDefinition = "tinyint(1) default 0")
    private Boolean internalProvider;

    @Column(nullable = true, name = "is_default", columnDefinition = "tinyint(1) default 0")
    private Boolean defaultProvider;

//    @OrderBy(value = "name")
//    @ManyToMany(fetch = FetchType.LAZY)
//    @JoinTable(
//            name = "provider_region",
//            joinColumns = @JoinColumn(name = "provider_id", referencedColumnName = "id"),
//            inverseJoinColumns = @JoinColumn(name = "region_id", referencedColumnName = "id")
//    )
////    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
//    private SortedSet<Region> regions = new TreeSet<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "provider")
    private List<Region> regions;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "user", nullable = true)
    private User user;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "organization", nullable = true)
    private Organization organization;

    @Column(nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP", name = "date_created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;

    @Column(nullable = false, name = "last_modified")
    private Date lastModified;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "provider")
    private ProviderQuota providerQuota;

    @Column(nullable = true, name = "network_mode_host")
    private Boolean networkModeHost = Boolean.FALSE;


    public Provider() {
    }

    public Long getProviderID() {
        return providerID;
    }

    public void setProviderID(Long providerID) {
        this.providerID = providerID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public ProviderType getProviderType() {
        return providerType;
    }

    public void setProviderType(ProviderType providerType) {
        this.providerType = providerType;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public String getPublicNetwork() {
        return publicNetwork;
    }

    public void setPublicNetwork(String publicNetwork) {
        this.publicNetwork = publicNetwork;
    }

    public List<Region> getRegions() {
        return regions;
    }

    public void setRegions(List<Region> regions) {
        this.regions = regions;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public Boolean getInternalProvider() {
        return internalProvider;
    }

    public void setInternalProvider(Boolean internalProvider) {
        this.internalProvider = internalProvider;
    }

    public Boolean getDefaultProvider() {
        return defaultProvider;
    }

    public void setDefaultProvider(Boolean defaultProvider) {
        this.defaultProvider = defaultProvider;
    }

    public String getMeshIdentifier() {
        return meshIdentifier;
    }

    public void setMeshIdentifier(String meshIdentifier) {
        this.meshIdentifier = meshIdentifier;
    }

    public ProviderQuota getProviderQuota() {
        return providerQuota;
    }

    public void setProviderQuota(ProviderQuota providerQuota) {
        this.providerQuota = providerQuota;
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

    public String getExternalNetworkID() {
        return externalNetworkID;
    }

    public void setExternalNetworkID(String externalNetworkID) {
        this.externalNetworkID = externalNetworkID;
    }

    public Boolean getProviderCredentialsChange() {
        return providerCredentialsChange;
    }

    public void setProviderCredentialsChange(Boolean providerCredentialsChange) {
        this.providerCredentialsChange = providerCredentialsChange;
    }

    public List<Long> getRegionIDs() {

        List<Long> regionIDs = new ArrayList<>();

        if (null != this.regions && !this.regions.isEmpty()) {

            this.regions
                    .stream()
                    .forEach(
                            region -> {
                                regionIDs.add(region.getRegionID());
                            });
        }

        return regionIDs;
    }

    public Boolean hasEditAllowance(User user) {
        if (user.isAdmin() || (user.isOrganizationAdmin() && user.getOrganization()
                .equals(getOrganization()))) {
            return true;
        }
        return false;
    }

    public Boolean hasDeleteAllowance(User user) {
        if (user.isAdmin() || (user.isOrganizationAdmin() && user.getOrganization()
                .equals(getOrganization()))) {
            return true;
        }
        return false;
    }

    public Boolean getNetworkModeHost() {
        return networkModeHost;
    }

    public void setNetworkModeHost(Boolean networkModeHost) {
        this.networkModeHost = networkModeHost;
    }

    public boolean hasType(ProviderName providerName) {
        return providerType.hasName(providerName);
    }

    @Override
    public String toString() {
        return "Provider{" +
                "providerID=" + providerID +
                ", name='" + name + '\'' +
                ", providerType=" + providerType +
                ", endpoint='" + endpoint + '\'' +
                ", enabled=" + enabled +
                ", internalProvider=" + internalProvider +
                ", defaultProvider=" + defaultProvider +
                ", dateCreated=" + dateCreated +
                '}';
    }
}
