package eu.orchestrator.transfer.entities.orchestrator;

import java.io.Serializable;

public class OrchestratorPlugin implements Serializable {

    private String id;
    private String name;
    private String moduleName;
    private String downloadURL;
    private String pluginType; // HTTP, SOCKET
    private String port;
    private String endpoint;
    private boolean defaultPlugin;
    private boolean immutablePlugin;
    private boolean disabledPlugin;

    public OrchestratorPlugin() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public boolean isDefaultPlugin() {
        return defaultPlugin;
    }

    public void setDefaultPlugin(boolean defaultPlugin) {
        this.defaultPlugin = defaultPlugin;
    }

    public boolean isImmutablePlugin() {
        return immutablePlugin;
    }

    public void setImmutablePlugin(boolean immutablePlugin) {
        this.immutablePlugin = immutablePlugin;
    }

    public boolean isDisabledPlugin() {
        return disabledPlugin;
    }

    public void setDisabledPlugin(boolean disabledPlugin) {
        this.disabledPlugin = disabledPlugin;
    }
}
