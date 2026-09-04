package eu.orchestator.core.util;

import eu.orchestator.core.model.orchestrator.netdata.NetdataConfiguration;
import eu.orchestator.core.model.orchestrator.netdata.PluginConfiguration;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorComponentNodeInstance;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorPlugin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @author Konstantinos Theodosiou.
 */
public class MetricsConfigurator {

    private static final Logger logger = LogManager.getLogger(MetricsConfigurator.class);

    public static NetdataConfiguration createConfiguration(List<OrchestratorPlugin> plugins, String prefix, OrchestratorComponentNodeInstance service) {
        NetdataConfiguration netdataConfiguration = new NetdataConfiguration();
        HashMap<String, List<OrchestratorPlugin>> hashMap = new HashMap<>();
        String netdataConfig = null;
        String pythonConfig = null;

        List<String> pluginsList = new ArrayList<>(
                Arrays.asList("proc", "diskspace", "cgroups", "tc", "idlejitter", "apps", "node.d", "charts.d", "python.d", "fping"));

        // Make a hashmap with the plugins and modules of them
        if (null == plugins || plugins.isEmpty()) {
            netdataConfiguration.setNetdataConfig("");
            return netdataConfiguration;
        }

        plugins.stream().forEach(plugin -> {
            if (!hashMap.containsKey(plugin.getName())) {
                hashMap.put(plugin.getName(), new ArrayList<>());
            }
            hashMap.get(plugin.getName()).add(plugin);
        });

        //Enable all the plugins that are needed
        netdataConfig = "[plugins]\n";
        for (String plugin : hashMap.keySet()) {
            if (pluginsList.contains(plugin)) {
                netdataConfig += "\t" + plugin + " = yes\n";
            }
        }

        //Add the node prefix
        netdataConfig += "\n[backend]\n";
        netdataConfig += "\tprefix = netdata:" + prefix + "\n";

        //Enable all the proc modules if any is needed
        if (hashMap.keySet().contains("proc")) {
            List<OrchestratorPlugin> proc = hashMap.get("proc");
            netdataConfig += "\n[plugin:proc]\n";
            for (OrchestratorPlugin module : proc) {
                //Check if the module is disabled and isn't immutable
                if (module.isDisabledPlugin() && !module.isImmutablePlugin()) {
                    //if so, check if it is default and then disable it
                    if (module.isDefaultPlugin()) {
                        netdataConfig += "\t" + module.getModuleName() + " = no\n";
                    }
                } else {
                    netdataConfig += "\t" + module.getModuleName() + " = yes\n";
                }
            }
        }

        //Dis/Enable the statsd plugin
        if (hashMap.keySet().contains("statsd")) {
            List<OrchestratorPlugin> statsd = hashMap.get("statsd");
            netdataConfig += "\n[statsd]\n";
            for (OrchestratorPlugin module : statsd) {
                //TODO now we enable and disable in statsd level no stats level
                //Check if the module is disabled and isn't immutable
                if (module.isDisabledPlugin() && !module.isImmutablePlugin()) {
                    //if so, check if it is default and then disable it
                    if (module.isDefaultPlugin()) {
                        netdataConfig += "\tenabled = no\n";
                    }
                } else {
                    netdataConfig += "\tenabled = yes\n";
                }
            }
        }

        //Construct the python.d.conf file if it is needed
        if (hashMap.keySet().contains("python.d")) {
            Boolean addPythonConfig = false;
            pythonConfig = "gc_run: yes\n";
            pythonConfig += "gc_interval: 300\n";
            pythonConfig += "apache_cache: no\n";
            pythonConfig += "chrony: no\n";
            pythonConfig += "example: no\n";
            pythonConfig += "gunicorn_log: no\n";
            pythonConfig += "go_expvar: no\n";
            pythonConfig += "logind: no\n";
            pythonConfig += "nginx_log: no\n";
            pythonConfig += "unbound: no\n";

            List<OrchestratorPlugin> pythond = hashMap.get("python.d");
            for (OrchestratorPlugin module : pythond) {
                //Check if the module is disabled and isn't immutable
                if (module.isDisabledPlugin() && !module.isImmutablePlugin()) {
                    //if so, check if it is default and then disable it
                    if (module.isDefaultPlugin()) {
                        pythonConfig += module.getModuleName() + ": no\n";
                    }
                } else {
                    addPythonConfig = true;
                    pythonConfig += module.getModuleName() + ": yes\n";

                    PluginConfiguration pluginConfiguration = new PluginConfiguration();
                    pluginConfiguration.setName(module.getName());
                    pluginConfiguration.setModuleName(module.getModuleName());
                    pluginConfiguration.setDownloadUrl(module.getDownloadURL());
                    pluginConfiguration.setPluginType(module.getPluginType());

                    String configuration = getModuleConfiguration(module, service);
                    pluginConfiguration.setConfiguration(configuration);
                    netdataConfiguration.getPluginConfigurationList().add(pluginConfiguration);
                }
            }
            if (!addPythonConfig) {
                pythonConfig = null;
            }
        }

        netdataConfiguration.setNetdataConfig(netdataConfig);
        netdataConfiguration.setPythonConfig(pythonConfig);

        return netdataConfiguration;
    }

    public static String getModuleConfiguration(OrchestratorPlugin module, OrchestratorComponentNodeInstance service) {
        String configuration = "";
        if (module.getPluginType().equals("HTTP")) {
            configuration = "job_name:\n";
            configuration += "    name: " + module.getModuleName() + "\n";
            configuration += "    update_every: 1\n";
            configuration += "    priority: 60000\n";
            configuration += "    retries: 5000\n";
            configuration += "    autodetection_retry: 10\n";

            if (module.getModuleName().equals("mysql")) {
                //TODO find a way to make it more dynamically
                configuration += "    host: \'localhost\'\n";
                configuration += "    port: " + module.getPort() + "\n";
                configuration += "    user: \'root\'\n";
                configuration += "    pass: \'" + service.getEnvironmentalVariables().get("MYSQL_ROOT_PASSWORD") + "\'\n";

            } else if (module.getModuleName().equals("mongodb")) {
                //TODO find a way to make it more dynamically
                configuration += "    host: \'localhost\'\n";
                configuration += "    port: " + module.getPort() + "\n";
                //configuration += "    user: \'root\'\n";
                //configuration += "    pass: \'" + service.getEnvironmentalVariables().get("MONGO_PASSWORD") + "\'\n";

            } else if (module.getEndpoint().startsWith("/")) {
                configuration += "    url: http://localhost:" + module.getPort() + module.getEndpoint() + "\n";
            } else {
                configuration += "    url: http://localhost:" + module.getPort() + "/" + module.getEndpoint() + "\n";
            }
        } else if (module.getPluginType().equals("SOCKET")) {
            configuration = "job_name:\n";
            configuration += "    name: " + module.getModuleName() + "\n";
            configuration += "    update_every: 1\n";
            configuration += "    priority: 60000\n";
            configuration += "    retries: 60\n";
            configuration += "    autodetection_retry: 0\n";
            configuration += "    host: 'localhost'\n";
            configuration += "    port: " + module.getPort() + "\n";

        } else if (module.getPluginType().equals("DOWNLOAD_CONF")) {
            configuration = module.getEndpoint();
        } else {
            logger.warn("This configuration type is unrecognized!");
        }

        return configuration;
    }

    public static String getCustomPluginConfiguration(NetdataConfiguration netdataConfiguration) {
        String pluginConfig = "";
        // Adds the line which installs the python.d.conf if needed
        if (netdataConfiguration.getPythonConfig() != null && !netdataConfiguration.getPythonConfig().isEmpty()) {
            pluginConfig = "sudo echo \"" + netdataConfiguration.getPythonConfig() + "\" > /opt/netdata/netdata-configs/python.d.conf\n";
        }

        //TODO if node, chart etc
        for (PluginConfiguration moduleConfiguration : netdataConfiguration.getPluginConfigurationList()) {
            if (moduleConfiguration.getName().equals("python.d")) {
                if (moduleConfiguration.getModuleName().equals("mysql")) {
                    //TODO find a way to make it more dynamically
                    pluginConfig += "sudo apt-get install -y python-pymysql\n";
                } else if (moduleConfiguration.getModuleName().equals("mongodb")) {
                    //TODO find a way to make it more dynamically
                    pluginConfig += "sudo apt-get install -y python-pymongo\n";
                } else {
                    // Adds the line in order to download the module and place it in the right path
                    pluginConfig += "sudo wget " + moduleConfiguration.getDownloadUrl() + " -O /opt/netdata/netdata-plugins/python.d/" + moduleConfiguration
                            .getModuleName() + ".chart.py\n";
                }

                // HTTP, SOCKET, DOWNLOAD_CONF
                if (moduleConfiguration.getPluginType().equals("HTTP") || moduleConfiguration.getPluginType().equals("SOCKET")) {
                    pluginConfig +=
                            "sudo echo \"" + moduleConfiguration.getConfiguration() + "\" > /opt/netdata/netdata-configs/python.d/" + moduleConfiguration
                                    .getModuleName() + ".conf\n";
                } else if (moduleConfiguration.getPluginType().equals("DOWNLOAD_CONF")) {
                    pluginConfig += "sudo wget " + moduleConfiguration.getConfiguration() + " -O /opt/netdata/netdata-configs/python.d/" + moduleConfiguration
                            .getModuleName() + ".conf\n";

                }
                pluginConfig += "sudo chmod 644 /opt/netdata/netdata-configs/python.d/" + moduleConfiguration.getModuleName() + ".conf\n";
            } else {
                //TODO if node, chart etc
            }
        }

        return pluginConfig;
    }
}
