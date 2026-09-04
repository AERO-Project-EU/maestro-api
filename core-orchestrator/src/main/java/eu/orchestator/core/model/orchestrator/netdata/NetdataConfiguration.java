package eu.orchestator.core.model.orchestrator.netdata;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Konstantinos Theodosiou.
 */
public class NetdataConfiguration {

    String netdataConfig;
    String pythonConfig;
    List<PluginConfiguration> pluginConfigurationList;

    public String getNetdataConfig() {
        return netdataConfig;
    }

    public void setNetdataConfig(String netdataConfig) {
        this.netdataConfig = netdataConfig;
    }

    public String getPythonConfig() {
        return pythonConfig;
    }

    public void setPythonConfig(String pythonConfig) {
        this.pythonConfig = pythonConfig;
    }

    public List<PluginConfiguration> getPluginConfigurationList() {
        if (pluginConfigurationList == null) {
            pluginConfigurationList = new ArrayList<>();
        }
        return pluginConfigurationList;
    }

    public void setPluginConfigurationList(List<PluginConfiguration> pluginConfigurationList) {
        this.pluginConfigurationList = pluginConfigurationList;
    }

}
