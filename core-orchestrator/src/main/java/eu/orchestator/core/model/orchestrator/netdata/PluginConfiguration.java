package eu.orchestator.core.model.orchestrator.netdata;

/**
 * @author Konstantinos Theodosiou.
 */
public class PluginConfiguration {
    private String name;
    private String moduleName;
    private String downloadUrl;
    private String configuration;
    private String pluginType; // HTTP, SOCKET, DOWNLOAD_CONF

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

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    public String getPluginType() {
        return pluginType;
    }

    public void setPluginType(String pluginType) {
        this.pluginType = pluginType;
    }
}
