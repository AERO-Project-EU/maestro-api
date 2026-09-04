package eu.orchestrator.repository.domain;

import eu.orchestrator.repository.domain.ProviderType.ProviderName;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.orchestrator.repository.domain.rainbow.Slo;
import java.util.Set;
import jakarta.persistence.Convert;
import org.hibernate.type.NumericBooleanConverter;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

@Entity
@Table(name = "application_instance")
public class ApplicationInstance implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long applicationInstanceID;

    @Column(nullable = false)
    private String name;

    @Column(nullable = true, name = "hex_id", unique = true)
    private String hexID;

    @Column(nullable = true)
    private String description;

    //    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "application", nullable = true)
    private Application application;

    //    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "provider", nullable = true)
    private Provider provider;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "applicationInstance")
    private List<RuntimePolicy> runtimePolicies;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "applicationInstance")
    private List<SecurityPolicy> securityPolicies;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "applicationInstance")
    private List<SecurityConfiguration> securityConfigurations;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "applicationInstance")
    private List<SocPolicy> socPolicies;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "applicationInstance")
    private List<Profile> profiles;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "applicationInstance")
    private Set<Slo> slos;

//    @ManyToMany(fetch = FetchType.LAZY)
//    @JoinTable(
//            name = "application_instance_component_node_instance",
//            joinColumns = @JoinColumn(name = "application_instance_id", referencedColumnName = "id"),
//            inverseJoinColumns = @JoinColumn(name = "component_node_instance_id", referencedColumnName = "id")
//    )
//    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "applicationInstance")
    private SortedSet<ComponentNodeInstance> componentNodeInstances = new TreeSet<>();

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "slice", nullable = true)
    private Slice slice;

    //    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "applicationInstance")
    private List<Constraint> constraints;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "user", nullable = true)
    private User user;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "organization", nullable = true)
    private Organization organization;

    @Column(nullable = true, name = "overlay", columnDefinition = "TINYINT(1) UNSIGNED default '0'")
    @Convert(converter = NumericBooleanConverter.class)
    private Boolean overlay = false; // IPv4, IPv6

    @Column(nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP", name = "date_created")
    private Date dateCreated;

    @Column(nullable = false, name = "last_modified")
    private Date lastModified;

    @Column(nullable = true, name = "date_deployed")
    private Date dateDeployed;

    @Column(nullable = true, name = "deployment_timestamp")
    private Long deploymentTimestamp;

    @Column(nullable = true)
    private String status;

    @Column(nullable = true, name = "grafana_bashboard_uuid")
    private String grafanaDashboardUUID;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "applicationInstance")
    private ApplicationInstanceQuota applicationInstanceQuota;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "applicationInstance")
    private List<GraphLinkNodeInstance> graphLinkNodeInstances;

    //TODO remove after spider
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "applicationInstance")
    private List<SpiderRule> spiderRules;

    @Transient
    private String applicationName; // todo - need delete??


    public ApplicationInstance() {
    }

    public enum ApplicationInstanceStatus {

        PENDING("Pending"),
        ERROR_OCCURRED("Error Occurred"),
        WAITING_OSS("Waiting response from OSS"),
        WAITING_ORCHESTRATOR("Waiting response from Orchestrator"),
        WAITING_CONFIRMATION("Waiting confirmation"),
        UNDEPLOYING("Un-deploying"),
        DEPLOYING("Deploying"),
        DEPLOYED("Deployed"),
        UNDEPLOYED("Un-deployed");

        private final String friendlyName;

        private ApplicationInstanceStatus(String friendlyName) {
            this.friendlyName = friendlyName;
        }

        public String getFriendlyName() {
            return friendlyName;
        }
    }

    public Long getApplicationInstanceID() {
        return applicationInstanceID;
    }

    public void setApplicationInstanceID(Long applicationInstanceID) {
        this.applicationInstanceID = applicationInstanceID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public Slice getSlice() {
        return slice;
    }

    public void setSlice(Slice slice) {
        this.slice = slice;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public Date getDateDeployed() {
        return dateDeployed;
    }

    public void setDateDeployed(Date dateDeployed) {
        this.dateDeployed = dateDeployed;
    }

    public SortedSet<ComponentNodeInstance> getComponentNodeInstances() {
        return componentNodeInstances;
    }

    public void setComponentNodeInstances(SortedSet<ComponentNodeInstance> componentNodeInstances) {
        this.componentNodeInstances = componentNodeInstances;
    }

    public List<Constraint> getConstraints() {
        return constraints;
    }

    public void setConstraints(List<Constraint> constraints) {
        this.constraints = constraints;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGrafanaDashboardUUID() {
        return grafanaDashboardUUID;
    }

    public void setGrafanaDashboardUUID(String grafanaDashboardUUID) {
        this.grafanaDashboardUUID = grafanaDashboardUUID;
    }

    public List<RuntimePolicy> getRuntimePolicies() {
        return runtimePolicies;
    }

    public void setRuntimePolicies(List<RuntimePolicy> runtimePolicies) {
        this.runtimePolicies = runtimePolicies;
    }

    public List<SecurityPolicy> getSecurityPolicies() {
        return securityPolicies;
    }

    public void setSecurityPolicies(List<SecurityPolicy> securityPolicies) {
        this.securityPolicies = securityPolicies;
    }

    public List<Profile> getProfiles() {
        return profiles;
    }

    public void setProfiles(List<Profile> profiles) {
        this.profiles = profiles;
    }

    public Set<Slo> getSlos() {
        return slos;
    }

    public void setSlos(Set<Slo> slos) {
        this.slos = slos;
    }

    public List<SocPolicy> getSocPolicies() {
        return socPolicies;
    }

    public void setSocPolicies(List<SocPolicy> socPolicies) {
        this.socPolicies = socPolicies;
    }

    public List<SecurityConfiguration> getSecurityConfigurations() {
        return securityConfigurations;
    }

    public void setSecurityConfigurations(List<SecurityConfiguration> securityConfigurations) {
        this.securityConfigurations = securityConfigurations;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public ApplicationInstanceQuota getApplicationInstanceQuota() {
        return applicationInstanceQuota;
    }

    public void setApplicationInstanceQuota(ApplicationInstanceQuota applicationInstanceQuota) {
        this.applicationInstanceQuota = applicationInstanceQuota;
    }

    public List<GraphLinkNodeInstance> getGraphLinkNodeInstances() {
        return graphLinkNodeInstances;
    }

    public void setGraphLinkNodeInstances(List<GraphLinkNodeInstance> graphLinkNodeInstances) {
        this.graphLinkNodeInstances = graphLinkNodeInstances;
    }

    public Boolean getOverlay() {
        return overlay;
    }

    public void setOverlay(Boolean overlay) {
        this.overlay = overlay;
    }

    public String getHexID() {
        return hexID;
    }

    public void setHexID(String hexID) {
        this.hexID = hexID;
    }

    public Long getDeploymentTimestamp() {
        return deploymentTimestamp;
    }

    public void setDeploymentTimestamp(Long deploymentTimestamp) {
        this.deploymentTimestamp = deploymentTimestamp;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public List<SpiderRule> getSpiderRules() {
        if (spiderRules == null) {
            spiderRules = new ArrayList<>();
        }
        return spiderRules;
    }

    public void setSpiderRules(List<SpiderRule> spiderRules) {
        this.spiderRules = spiderRules;
    }

    public Boolean hasEditAllowance(User user) {
        if (user != null && (getUser().equals(user) ||
                user.isAdmin() ||
                (user.isOrganizationAdmin() && user.getOrganization()
                        .equals(getUser().getOrganization())))) {
            return true;
        }
        return false;
    }

    public Boolean hasDeleteAllowance(User user) {
        if (user != null && (getUser().equals(user) ||
                user.isAdmin() || (user.isOrganizationAdmin() && user.getOrganization()
                .equals(getUser().getOrganization())))) {
            return true;
        }
        return false;
    }

    public boolean hasProvider(ProviderName providerName) {
        return provider.hasType(providerName);
    }

    public boolean hasStatus(ApplicationInstanceStatus status) {
        if (this.status == null) {
            return false;
        }

        return this.status.equals(status.name());
    }

    @Override
    public String toString() {
        return "ApplicationInstance{" +
                "applicationInstanceID=" + applicationInstanceID +
                ", name='" + name + '\'' +
                ", hexID='" + hexID + '\'' +
                '}';
    }
}
