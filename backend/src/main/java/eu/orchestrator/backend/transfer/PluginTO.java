package eu.orchestrator.backend.transfer;

import java.io.Serializable;
import java.util.Date;

public class PluginTO implements Serializable {

    private Long pluginID;
    private String name;
    private String moduleName;
    private Boolean publicPlugin;
    private Boolean immutablePlugin;
    private Boolean defaultPlugin;
    private Date dateCreated;
    private Date lastModified;
    private Integer metricsCounter;
    private Boolean allowEdit;
    private Boolean allowDelete;
    private String organization;

    public PluginTO() {
    }

    public Long getPluginID() {
        return pluginID;
    }

    public void setPluginID(Long pluginID) {
        this.pluginID = pluginID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public Boolean getPublicPlugin() {
        return publicPlugin;
    }

    public void setPublicPlugin(Boolean publicPlugin) {
        this.publicPlugin = publicPlugin;
    }

    public Boolean getImmutablePlugin() {
        return immutablePlugin;
    }

    public void setImmutablePlugin(Boolean immutablePlugin) {
        this.immutablePlugin = immutablePlugin;
    }

    public Boolean getDefaultPlugin() {
        return defaultPlugin;
    }

    public void setDefaultPlugin(Boolean defaultPlugin) {
        this.defaultPlugin = defaultPlugin;
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

    public Integer getMetricsCounter() {
        return metricsCounter;
    }

    public void setMetricsCounter(Integer metricsCounter) {
        this.metricsCounter = metricsCounter;
    }

    public Boolean getAllowEdit() {
        return allowEdit;
    }

    public void setAllowEdit(Boolean allowEdit) {
        this.allowEdit = allowEdit;
    }

    public Boolean getAllowDelete() {
        return allowDelete;
    }

    public void setAllowDelete(Boolean allowDelete) {
        this.allowDelete = allowDelete;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }
}
