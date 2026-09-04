package eu.orchestrator.elasticity.spi.service.orchestrator;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.health.model.HealthService;
import eu.orchestrator.elasticity.spi.model.orchestrator.ConsulConfigTO;
import eu.orchestrator.elasticity.spi.model.orchestrator.DiscoveryServiceMap;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 6/8/2019
 */
public class DiscoveryServiceInteractions {
    private ConsulClient consulClient;

    public DiscoveryServiceInteractions(ConsulConfigTO consulConfigTO) {
        consulClient = new ConsulClient(consulConfigTO.getUrl());

    }

    public List<DiscoveryServiceMap> getHealthServices(String graphHexID, String graphInstanceHexID, String componentNodeHexID){
        Response<List<HealthService>> response = consulClient.getHealthServices(graphHexID + ":" + graphInstanceHexID + ":" + componentNodeHexID, true, QueryParams.DEFAULT);

        //Check if the response has any values, if it's emtpy
        // means that has no running service
        if (response == null || response.getValue() == null || response.getValue().isEmpty()) {
            return null;
        }

        List<DiscoveryServiceMap> discoveryServiceMapList = new ArrayList<>();

        //Search for the IPs of all the nodes that having that service running healthy
        List<HealthService> healthServiceList = response.getValue();
        for (HealthService healthService : healthServiceList) {
            DiscoveryServiceMap discoveryServiceMap = new DiscoveryServiceMap();
            discoveryServiceMap.setServerName(healthService.getNode().getNode());
            discoveryServiceMap.setServerIP("" + healthService.getService().getAddress());

            discoveryServiceMapList.add(discoveryServiceMap);
        }

        return discoveryServiceMapList;
    }
}
