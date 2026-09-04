package eu.orchestrator.elasticity.spi.service.orchestrator;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.catalog.model.CatalogNode;
import eu.orchestrator.elasticity.spi.model.orchestrator.OrchestratorScalingTO;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorIP;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorPort;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 11/10/2019
 */
public class OrchestratorComponentService {

    public String getComponentPrivateIP(OrchestratorScalingTO orchestratorScalingTO){
        String privateIP = "";
        if(orchestratorScalingTO.getElasticityControllerStatus().getIpList() != null) {
            for (OrchestratorIP orchestratorIP : orchestratorScalingTO.getElasticityControllerStatus().getIpList()) {
                if (orchestratorIP.getInterfaceType() == null || orchestratorIP.getInterfaceType().isEmpty()) {
                    privateIP = orchestratorIP.getIp();
                    break;
                }
            }
        }
        return privateIP;
    }

    public String getComponentIPv6Address(OrchestratorScalingTO orchestratorScalingTO){
        ConsulClient consulClient = new ConsulClient(orchestratorScalingTO.getConsulConfig().getUrl());
        String nodeName = orchestratorScalingTO.getElasticityControllerStatus().getNodeName();
        String ipv6Address = null;

        Response<CatalogNode> response = consulClient.getCatalogNode(nodeName, QueryParams.DEFAULT);
        if(response!=null && response.getValue() != null && response.getValue().getNode() !=null && response.getValue().getNode().getAddress() != null){
            ipv6Address = response.getValue().getNode().getAddress();
        }

        return ipv6Address;
    }

    public String getComponentPublicIP(OrchestratorScalingTO orchestratorScalingTO){
        String publicIP = orchestratorScalingTO.getElasticityControllerStatus().getFloatingIP();
        if(publicIP == null || publicIP.isEmpty()){
            publicIP = "";
            if(orchestratorScalingTO.getElasticityControllerStatus().getIpList() != null) {
                for (OrchestratorIP orchestratorIP : orchestratorScalingTO.getElasticityControllerStatus().getIpList()) {
                    if (orchestratorIP.getInterfaceType() != null && orchestratorIP.getInterfaceType().equals(OrchestratorPort.InterfaceType.ACCESS.name())) {
                        publicIP = orchestratorIP.getIp();
                        break;
                    }
                }
            }
        }
        return publicIP;
    }
}
