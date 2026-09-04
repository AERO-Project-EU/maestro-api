package eu.orchestrator.backend.transfer;

import java.io.Serializable;
import java.util.Date;

public class RuntimePolicyActionTO implements Serializable {

    private Long id;
    private String componentName;
    private String componentHexID;
    private String type;
    private Integer workers;
    private String context;
    private String topicURL;
    private String topicPort;
    private String topicName;
    private Date dateCreated;
    private Date lastModified;

    public RuntimePolicyActionTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getComponentHexID() {
        return componentHexID;
    }

    public void setComponentHexID(String componentHexID) {
        this.componentHexID = componentHexID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getWorkers() {
        return workers;
    }

    public void setWorkers(Integer workers) {
        this.workers = workers;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getTopicURL() {
        return topicURL;
    }

    public void setTopicURL(String topicURL) {
        this.topicURL = topicURL;
    }

    public String getTopicPort() {
        return topicPort;
    }

    public void setTopicPort(String topicPort) {
        this.topicPort = topicPort;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
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
