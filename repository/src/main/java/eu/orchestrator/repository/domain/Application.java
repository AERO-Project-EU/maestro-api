package eu.orchestrator.repository.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Convert;
import org.hibernate.type.NumericBooleanConverter;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "application")
public class Application implements Serializable {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = true, name = "hex_id", unique = true)
    private String hexID;

//    @OrderBy(value = "name")
//    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "application")
    private List<ComponentNode> componentNodes;

//    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "application")
    private List<GraphLinkNode> graphLinkNodes;

    @Column(nullable = true, name = "is_public", columnDefinition = "TINYINT(1) UNSIGNED default '0'")
    @Convert(converter = NumericBooleanConverter.class)
    private Boolean publicApplication = false;

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

    public Application() {
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

    public List<ComponentNode> getComponentNodes() {
        return componentNodes;
    }

    public void setComponentNodes(List<ComponentNode> componentNodes) {
        this.componentNodes = componentNodes;
    }

    public List<GraphLinkNode> getGraphLinkNodes() {
        return graphLinkNodes;
    }

    public void setGraphLinkNodes(List<GraphLinkNode> graphLinkNodes) {
        this.graphLinkNodes = graphLinkNodes;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Boolean getPublicApplication() {
        return publicApplication;
    }

    public void setPublicApplication(Boolean publicApplication) {
        this.publicApplication = publicApplication;
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

    public String getHexID() {
        return hexID;
    }

    public void setHexID(String hexID) {
        this.hexID = hexID;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
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
