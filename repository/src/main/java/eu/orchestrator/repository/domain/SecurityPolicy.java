package eu.orchestrator.repository.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 15/3/2019
 */


@Entity
@Table(name = "security_policy")
public class SecurityPolicy implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, name = "hex_id", unique = true)
    private String hexID;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "application_instance", nullable = true)
    private ApplicationInstance applicationInstance;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "securityPolicies")
    private List<ComponentNodeInstance> componentNodeInstances;

    @Column(nullable = false, name = "ip_address")
    private String ipAddress;

    @Column(nullable = false, name = "rule")
    private String rule;

    @Column(nullable = true)
    private String status;

    @Column(nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP", name = "date_created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;

    @Column(nullable = false, name = "last_modified")
    private Date lastModified;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "user", nullable = true)
    private User user;

    public SecurityPolicy() {
    }

    public enum SecurityPolicyStatus {

        PENDING("Pending"),
        APPLIED("Applied"),
        ERROR_OCCURRED("Error Occurred");

        private String friendlyName;

        SecurityPolicyStatus(String friendlyName) {
            this.friendlyName = friendlyName;
        }

        public String getFriendlyName() {
            return friendlyName;
        }
    }

    public enum RuleType {

        Blacklist("/home/ubuntu/xdp/xdp_ddos01_blacklist_cmdline");

        private String friendlyName;

        RuleType(String friendlyName) {
            this.friendlyName = friendlyName;
        }

        public String getFriendlyName() {
            return friendlyName;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHexID() {
        return hexID;
    }

    public void setHexID(String hexID) {
        this.hexID = hexID;
    }

    public ApplicationInstance getApplicationInstance() {
        return applicationInstance;
    }

    public void setApplicationInstance(ApplicationInstance applicationInstance) {
        this.applicationInstance = applicationInstance;
    }

    public List<ComponentNodeInstance> getComponentNodeInstances() {
        return componentNodeInstances;
    }

    public void setComponentNodeInstances(List<ComponentNodeInstance> componentNodeInstances) {
        this.componentNodeInstances = componentNodeInstances;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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
}
