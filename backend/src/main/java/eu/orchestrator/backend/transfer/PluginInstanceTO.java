package eu.orchestrator.backend.transfer;

import java.io.Serializable;

public class PluginInstanceTO implements Serializable {

    private Long pluginInstanceID;
    private PluginTO plugin;
    private String name;
    private String moduleName;
    private Boolean immutablePlugin = false;
    private Boolean deletedPlugin = false;

    public PluginInstanceTO() {
    }

    public Boolean getImmutablePlugin() {
        return immutablePlugin;
    }

    public void setImmutablePlugin(Boolean immutablePlugin) {
        this.immutablePlugin = immutablePlugin;
    }

    public Long getPluginInstanceID() {
        return pluginInstanceID;
    }

    public void setPluginInstanceID(Long pluginInstanceID) {
        this.pluginInstanceID = pluginInstanceID;
    }

    public PluginTO getPlugin() {
        return plugin;
    }

    public void setPlugin(PluginTO plugin) {
        this.plugin = plugin;
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

    public Boolean getDeletedPlugin() {
        return deletedPlugin;
    }

    public void setDeletedPlugin(Boolean deletedPlugin) {
        this.deletedPlugin = deletedPlugin;
    }
}
