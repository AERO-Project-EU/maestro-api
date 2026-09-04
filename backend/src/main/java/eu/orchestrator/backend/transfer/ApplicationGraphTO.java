package eu.orchestrator.backend.transfer;

import java.io.Serializable;
import java.util.List;

public class ApplicationGraphTO implements Serializable {

    private Long id;
    private String hexID;
    private String name;
    private Boolean publicApplication;
    private List<ComponentNodeTO> componentNodes;
    private List<GraphLinkNodeTO> graphLinkNodes;
    private String organization;

    public ApplicationGraphTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHexID() {
        return hexID;
    }

    public void setHexID(String hexID) {
        this.hexID = hexID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getPublicApplication() {
        return publicApplication;
    }

    public void setPublicApplication(Boolean publicApplication) {
        this.publicApplication = publicApplication;
    }

    public List<ComponentNodeTO> getComponentNodes() {
        return componentNodes;
    }

    public void setComponentNodes(List<ComponentNodeTO> componentNodes) {
        this.componentNodes = componentNodes;
    }

    public List<GraphLinkNodeTO> getGraphLinkNodes() {
        return graphLinkNodes;
    }

    public void setGraphLinkNodes(List<GraphLinkNodeTO> graphLinkNodes) {
        this.graphLinkNodes = graphLinkNodes;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }
}
