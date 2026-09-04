package eu.orchestrator.elasticity.adapter.traefikLB.model;

import eu.orchestrator.elasticity.spi.model.orchestrator.ControllerMetadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 25/7/2019
 */
public class LoadBalancerMetadata extends ControllerMetadata {
    //Map with server name and ip
    private Map<String, String> servers;

    //list with all exposed ports
    private ArrayList<String> ports;

    private String uiPort;

    public LoadBalancerMetadata(){
        servers = new HashMap<>();
        ports = new ArrayList<>();
    }

    public Map<String, String> getServers() {
        return servers;
    }

    public void setServers(Map<String, String> servers) {
        this.servers = servers;
    }

    public ArrayList<String> getPorts() {
        return ports;
    }

    public void setPorts(ArrayList<String> ports) {
        this.ports = ports;
    }

    public String getUiPort() {
        return uiPort;
    }

    public void setUiPort(String uiPort) {
        this.uiPort = uiPort;
    }
}
