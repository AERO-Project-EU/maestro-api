package eu.orchestrator.transfer.entities.backend.repository.component;

import eu.orchestrator.transfer.entities.backend.repository.OrganizationTo;
import eu.orchestrator.transfer.entities.backend.repository.UserTo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 24/1/22
 */
public class PluginTo implements Serializable, Comparable<PluginTo> {

    private Long pluginID;
    private String name;
    private String moduleName;
    private String downloadURL;
    private String pluginType;
    private String port;
    private String endpoint;
    private Boolean publicPlugin;
    private Boolean immutablePlugin;
    private Boolean defaultPlugin;
    private UserTo user;
    private OrganizationTo organization;
    private Date dateCreated;
    private Date lastModified;
    private List<MetricTo> metrics;
    private Integer metricsCounter;

    public PluginTo() {
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

    public String getDownloadURL() {
        return downloadURL;
    }

    public void setDownloadURL(String downloadURL) {
        this.downloadURL = downloadURL;
    }

    public String getPluginType() {
        return pluginType;
    }

    public void setPluginType(String pluginType) {
        this.pluginType = pluginType;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
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

    public List<MetricTo> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<MetricTo> metrics) {
        this.metrics = metrics;
    }

    public Integer getMetricsCounter() {
        return metricsCounter;
    }

    public void setMetricsCounter(Integer metricsCounter) {
        this.metricsCounter = metricsCounter;
    }

    @Override
    public int compareTo(PluginTo pluginTo) {
        return (int)(this.pluginID - pluginTo.getPluginID());

    }
}
