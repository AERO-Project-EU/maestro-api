package eu.orchestrator.transfer.entities.backend.repository.application;

import eu.orchestrator.transfer.entities.backend.repository.OrganizationTo;
import eu.orchestrator.transfer.entities.backend.repository.UserTo;

import java.io.Serializable;
import java.util.List;

/**
 * @author Vasileios Matsoukas
 * @email billmats96@hotmail.com
 * @date 7/12/22
 */

public class ApplicationGraphTo implements Serializable {

    private Long id;
    private String name;
    private String hexID;
    private List<ComponentNodeTo> componentNodes;
    private List<GraphLinkNodeTo> graphLinkNodes;
    private Boolean publicApplication = false;
    private UserTo user;
    private OrganizationTo organization;

    public ApplicationGraphTo() {
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

    public List<ComponentNodeTo> getComponentNodes() {
        return componentNodes;
    }

    public void setComponentNodes(List<ComponentNodeTo> componentNodes) {
        this.componentNodes = componentNodes;
    }

    public List<GraphLinkNodeTo> getGraphLinkNodes() {
        return graphLinkNodes;
    }

    public void setGraphLinkNodes(List<GraphLinkNodeTo> graphLinkNodes) {
        this.graphLinkNodes = graphLinkNodes;
    }

    public Boolean getPublicApplication() {
        return publicApplication;
    }

    public void setPublicApplication(Boolean publicApplication) {
        this.publicApplication = publicApplication;
    }

    public UserTo getUser() {
        return user;
    }

    public void setUser(UserTo user) {
        this.user = user;
    }

    public OrganizationTo getOrganization() {
        return organization;
    }

    public void setOrganization(OrganizationTo organization) {
        this.organization = organization;
    }

}
