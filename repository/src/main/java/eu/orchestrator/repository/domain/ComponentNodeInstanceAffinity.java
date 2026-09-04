package eu.orchestrator.repository.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 7/9/23
 */

@Entity
@Table(name = "component_node_instance_affinity")
public class ComponentNodeInstanceAffinity implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long componentNodeInstanceAffinityID;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "application_instance", nullable = true)
    private ApplicationInstance applicationInstance;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "component_node_instance", nullable = true)
    private ComponentNodeInstance componentNodeInstance;

    @ElementCollection(fetch = FetchType.LAZY)
    private List<String> affinityLabels;

    @Column(nullable = false, columnDefinition = "timestamp default CURRENT_TIMESTAMP", name = "date_created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;

    @Column(nullable = false, name = "last_modified")
    private Date lastModified;

    public ComponentNodeInstanceAffinity() {
    }

    public Long getComponentNodeInstanceAffinityID() {
        return componentNodeInstanceAffinityID;
    }

    public void setComponentNodeInstanceAffinityID(Long componentNodeInstanceAffinityID) {
        this.componentNodeInstanceAffinityID = componentNodeInstanceAffinityID;
    }

    public ApplicationInstance getApplicationInstance() {
        return applicationInstance;
    }

    public void setApplicationInstance(ApplicationInstance applicationInstance) {
        this.applicationInstance = applicationInstance;
    }

    public ComponentNodeInstance getComponentNodeInstance() {
        return componentNodeInstance;
    }

    public void setComponentNodeInstance(ComponentNodeInstance componentNodeInstance) {
        this.componentNodeInstance = componentNodeInstance;
    }

    public List<String> getAffinityLabels() {
        return affinityLabels;
    }

    public void setAffinityLabels(List<String> affinityLabels) {
        this.affinityLabels = affinityLabels;
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
}
