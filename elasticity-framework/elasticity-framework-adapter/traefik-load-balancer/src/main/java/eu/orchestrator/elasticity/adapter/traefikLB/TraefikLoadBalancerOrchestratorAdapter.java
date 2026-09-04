package eu.orchestrator.elasticity.adapter.traefikLB;

import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.kv.model.GetValue;
import eu.orchestrator.elasticity.adapter.traefikLB.model.LoadBalancerMetadata;
import eu.orchestrator.elasticity.adapter.traefikLB.util.TraefikConfiguration;
import eu.orchestrator.elasticity.spi.adapter.ElasticityFrameworkOrchestrator;
import eu.orchestrator.elasticity.spi.model.orchestrator.*;
import eu.orchestrator.elasticity.spi.service.orchestrator.BackendInteractions;
import eu.orchestrator.elasticity.spi.service.orchestrator.DiscoveryServiceInteractions;
import eu.orchestrator.elasticity.spi.service.orchestrator.MetadataConverter;
import eu.orchestrator.elasticity.spi.service.orchestrator.OrchestratorInteractions;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorComponentNodeInstance;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorPort;
import eu.orchestrator.transfer.entities.orchestrator.internal.ServiceStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 19/7/2019
 */
public class TraefikLoadBalancerOrchestratorAdapter implements ElasticityFrameworkOrchestrator {
    private static final Logger logger = LogManager.getLogger(TraefikLoadBalancerOrchestratorAdapter.class);

    private OrchestratorInteractions orchestratorInteractions;
    private BackendInteractions backendInteractions;
    private static MetadataConverter<LoadBalancerMetadata> metadataConverter;
    private static final int TRAEFIK_UI_PORT = 15568;


    public static void main(String[] args) {

    }

    public TraefikLoadBalancerOrchestratorAdapter() {
        orchestratorInteractions = new OrchestratorInteractions();
        backendInteractions = new BackendInteractions();
        metadataConverter = new MetadataConverter<>();
    }

    @Override
    public LoadBalancerMetadata initControllerMetadata(List<OrchestratorComponentNodeInstance> componentList) {
        LoadBalancerMetadata loadBalancerMetadata = new LoadBalancerMetadata();

        return loadBalancerMetadata;
    }

    @Override
    public LoadBalancerMetadata getHealthyWorkers(OrchestratorScalingTO scalingObject, OrchestratorComponentNodeInstance component, HashMap hashMapServerName) {
        DiscoveryServiceInteractions discoveryServiceInteractions = new DiscoveryServiceInteractions(scalingObject.getConsulConfig());
        String graphHexID = scalingObject.getComposeObject().getGraphHexID();
        String graphInstanceHexID = scalingObject.getComposeObject().getGraphInstanceHexID();

        LoadBalancerMetadata currentMetadata = metadataConverter.convertMetadata(component.getControllerMetadata(), LoadBalancerMetadata.class);

        LoadBalancerMetadata loadBalancerMetadata = new LoadBalancerMetadata();
        loadBalancerMetadata.setControllerStatus(currentMetadata.getControllerStatus());

//        String serviceName = lbService.getComponentNodeHexID().substring(0, lbService.getComponentNodeHexID().length() - 2);
        String serviceName = component.getDependsOn().get(0).getDependency();

        List<DiscoveryServiceMap> discoveryServiceMapList = discoveryServiceInteractions.getHealthServices(graphHexID, graphInstanceHexID, serviceName);
        if(null == discoveryServiceMapList || discoveryServiceMapList.isEmpty()){
            return loadBalancerMetadata;
        }


        loadBalancerMetadata.setUiPort("" + TRAEFIK_UI_PORT);

        //Iterate through lbService ports and assign all the ports except the TRAEFIK_UI_PORT
        for (OrchestratorPort port : component.getPorts()) {
            if (port.getPublished().equals("" + TRAEFIK_UI_PORT)) {
                continue;
            }
            loadBalancerMetadata.getPorts().add("" + port.getPublished());
        }

        //Search for the IPs of all the nodes that having that service running healthy
        for (DiscoveryServiceMap discoveryServiceMap : discoveryServiceMapList) {
            String serverName = discoveryServiceMap.getServerName();
            String serverIP = discoveryServiceMap.getServerIP();

            //Check if serverName contained in the known server nodes
            if(hashMapServerName.get(serverName)!=null) {
                //Check if the node name exist in the loadBalancer metadata
                if (loadBalancerMetadata.getServers().containsKey(serverName)) {
                    //Check if the value is changed update it
                    String oldServerIP = loadBalancerMetadata.getServers().get(serverName);
                    if (!oldServerIP.equals(serverIP)) {
                        loadBalancerMetadata.getServers().replace(serverName, oldServerIP, serverIP);
                    }
                }else{ //if the node name doesn't exist apply it
                    //if the serverIP already exist update the node name
                    if (loadBalancerMetadata.getServers().containsValue(serverIP)) {
                        for(String key : loadBalancerMetadata.getServers().keySet()){
                            if(loadBalancerMetadata.getServers().get(key).equals(serverIP)){
                                logger.warn("Find a duplicated serverIP: " + serverIP + " oldServerName: " + key + " newServerName: " + serverName);

                                //TODO delete the consul key value path of the LoadBalancer
                                //TODO delete the old loadBalancerMetadata entity
                                //TODO put the new one entity
                                break;
                            }
                        }
                    } else { //just put the new node name and serverIP
                        loadBalancerMetadata.getServers().put(serverName, serverIP);
                    }
                }
            }
        }

        return loadBalancerMetadata;
    }

    @Override
    public UpdateControllerTO updateElasticityController(OrchestratorScalingTO scalingObject, OrchestratorComponentNodeInstance component, Object controllerNewMetadata) {
        ConsulClient consulClient = new ConsulClient(scalingObject.getConsulConfig().getUrl());
        String graphHexID = scalingObject.getComposeObject().getGraphHexID();
        String graphInstanceHexID = scalingObject.getComposeObject().getGraphInstanceHexID();
        Boolean ipv6Enabled = scalingObject.getComposeObject().getIpv6Enabled();

        UpdateControllerTO updateControllerTO = new UpdateControllerTO();
        updateControllerTO.setControllerMetadata(null);

        LoadBalancerMetadata loadBalancerMetadata = metadataConverter.convertMetadata(controllerNewMetadata, LoadBalancerMetadata.class);

        //Check if the loadBalancerConfig has any values
        //TODO this means that we will not have anytime less than ONE worker
        if (loadBalancerMetadata == null || loadBalancerMetadata.getServers() == null || loadBalancerMetadata.getServers().isEmpty()) {
            updateControllerTO.setControllerStatus(ControllerStatus.EMPTY_CONTROLLER_CONFIG);
            return updateControllerTO;
        }

        Boolean init = false;
        if (loadBalancerMetadata.getControllerStatus().getStatus() == ControllerStatus.START_CONTROLLER_STATUS.getStatus()) {
            init = true;
        }

        String componentNodeHexID = component.getComponentNodeHexID();
        Integer status = ControllerStatus.NO_CONTROLLER_UPDATE_NEEDED.getStatus();

        LoadBalancerMetadata currLoadBalancerMetadata = metadataConverter.convertMetadata(component.getControllerMetadata(), LoadBalancerMetadata.class);

        Boolean changed = false;
        //Check if current Load Balancer config has any value that means the new one has
        if (currLoadBalancerMetadata == null || currLoadBalancerMetadata.getServers() == null || currLoadBalancerMetadata.getServers().isEmpty()) {
            changed = true;
        } else {
            //Checks if all serverName are in currentLoadBalancer
            //if not the config changed
            for (String serverName : loadBalancerMetadata.getServers().keySet()) {
                if (!currLoadBalancerMetadata.getServers().containsKey(serverName)) {
                    changed = true;
                }
            }

            //Checks if all currServerNames are in newLoadBalancer
            //if not the config changed
            for (String currServerName : currLoadBalancerMetadata.getServers().keySet()) {
                if (!loadBalancerMetadata.getServers().containsKey(currServerName)) {
                    changed = true;
                }
            }

            //Checks if all serverIPs are in currentLoadBalancer
            //if not the config changed
            for (String serverIP : loadBalancerMetadata.getServers().values()) {
                if (!currLoadBalancerMetadata.getServers().containsValue(serverIP)) {
                    changed = true;
                }
            }

            //Checks if all currServerIPs are in newLoadBalancer
            //if not the config changed
            for (String currServerIP : currLoadBalancerMetadata.getServers().values()) {
                if (!loadBalancerMetadata.getServers().containsValue(currServerIP)) {
                    changed = true;
                }
            }
        }

        //TODO check that the KV updated correctly
        //if changed then we must update the KV Consul module
        if (changed) {
            //If is the first time we initialize the Load Balancer, push the Static Configuration
            if (init) {
                TraefikConfiguration.pushStaticConfig(scalingObject.getConsulConfig(), graphHexID, graphInstanceHexID, componentNodeHexID, loadBalancerMetadata);

                logger.debug("Push Static Config for the Load Balancer: " + componentNodeHexID);
                status = ControllerStatus.FIRST_CONTROLLER_CONFIG.getStatus();
            } else {
                status = ControllerStatus.CONTROLLER_CONFIG_UPDATED.getStatus();
            }

            updateControllerTO.setControllerMetadata(loadBalancerMetadata);

            TraefikConfiguration
                    .pushDynamicConfig(scalingObject.getConsulConfig(), graphHexID, graphInstanceHexID, componentNodeHexID, loadBalancerMetadata, ipv6Enabled);

            logger.debug("Push Dynamic Config for the Load Balancer: " + componentNodeHexID);

            //If is the first time we initialize the Load Balancer, change the lbStatus to startLBStatus
            if (init) {
                consulClient.setKVValue(graphHexID + "/" + graphInstanceHexID + "/" + componentNodeHexID
                        + "/" + component.getComponentNodeInstanceHexID() + "/" + "lbStatus", "" + ControllerStatus.START_CONTROLLER_STATUS.getStatus());
            }
        }


        updateControllerTO.setControllerStatus(ControllerStatus.nameOf(status));
        return updateControllerTO;
    }

    @Override
    public ScaleOutTO scaleOut(OrchestratorScalingTO scalingObject) {
        ScaleOutTO scaleOutTO = orchestratorInteractions.calculateRunningWorkers(scalingObject);
        if(!scaleOutTO.getProceedWithScaling()){
            return scaleOutTO;
        }

        scaleOutTO = orchestratorInteractions.calculateWorkersToRequest(scaleOutTO, scalingObject);
        if(!scaleOutTO.getProceedWithScaling()){
            return scaleOutTO;
        }

        scaleOutTO =  backendInteractions.scaleOutRequest(scaleOutTO, scalingObject);
        if(!scaleOutTO.getProceedWithScaling()){
            return scaleOutTO;
        }

        //here you can do custom things if you want

        return scaleOutTO;
    }

    @Override
    public ScaleInTO findWorkersForRemoval(OrchestratorScalingTO scalingObject) {
        ScaleInTO scaleInTO = orchestratorInteractions.calculateWorkersToRemove(scalingObject);
        if(!scaleInTO.getProceedWithScaling()){
            return scaleInTO;
        }

        scaleInTO = orchestratorInteractions.findWorkersTORemove(scaleInTO);
        if(!scaleInTO.getProceedWithScaling()){
            return scaleInTO;
        }

        return scaleInTO;
    }

    @Override
    public ScaleInTO removeWorkersFromController(ScaleInTO scaleInTO, OrchestratorScalingTO scalingObject) {
        String graphHexID = scalingObject.getComposeObject().getGraphHexID();
        String graphInstanceHexID = scalingObject.getComposeObject().getGraphInstanceHexID();
        ConsulClient consulClient = new ConsulClient(scalingObject.getConsulConfig().getUrl());
        String componentNodeHexId = "";

        //Construct the consul client in order to query the lb configuration
        String kvLBConfigPath = "";
        String pattern = "";

        //TODO mind this when pods are used
        //            componentNodeHexId = triggerActionModel.getComponentNodeHexID()+"LB";
        for (OrchestratorComponentNodeInstance service: scalingObject.getComposeObject().getServices()) {

            if(service.getLoadBalancer()){
                componentNodeHexId = service.getComponentNodeHexID();
            }
        }
        kvLBConfigPath = graphHexID + "/" + graphInstanceHexID + "/" + componentNodeHexId + "/" + "lbConfiguration" + "/backends";
        pattern = "(" + kvLBConfigPath + "/backend[0-9]+/servers/" ;

        logger.info("We will remove servers from a Load Balancer");

        Response<List<GetValue>> kvResponseBinary = consulClient.getKVValues(kvLBConfigPath);

        //Construct an regular expression to identify all the server
        // that need to be removed from the load balancer configuration
        List<String> keysToDelete = new ArrayList<>();

        //TODO remake the sequence of the steps

        //TODO further test and rethinking needed about the deletion of the keys
        // a corner case to not find the IP in the lb configuration for some reason maybe?
        //TODO check if is horizontal scalable or lambda function
        for(ServiceStatus serviceStatus : scaleInTO.getQualifiedForRemoval()) {

            //Identify which paths need deletion
            String nodeName = serviceStatus.getNodeName();
            //TODO change pattern of backend[0-9]
            String nodePattern = pattern + nodeName + "/)";
            Pattern r = Pattern.compile(nodePattern);
            if(kvResponseBinary!=null && kvResponseBinary.getValue() != null && ! kvResponseBinary.getValue().isEmpty()) {
                for(GetValue value : kvResponseBinary.getValue()){
                    Matcher m = r.matcher(value.getKey());
                    if(m.find()){
                        if(!keysToDelete.contains(m.group(0))) {
                            keysToDelete.add(m.group());
                        }
                    }
                }
            }else{
                break;
            }
        }

        //Delete all the server needed from the load balancer configuration key value
        if(!keysToDelete.isEmpty()) {
            for (String keyToDelete : keysToDelete) {
                consulClient.deleteKVValues(keyToDelete);
            }
        }

        scaleInTO.setProceedWithScaling(true);
        return scaleInTO;

    }

    @Override
    public Object postScaleInControllerMetadataClean(ScaleInTO scaleInTO, Object controllerMetadata){
        LoadBalancerMetadata loadBalancerMetadata = metadataConverter.convertMetadata(controllerMetadata, LoadBalancerMetadata.class);

        //if we correctly found an Load Balancer remove the IPs
        for (ServiceStatus serviceStatus : scaleInTO.getQualifiedForRemoval()) {
            //Identify which paths need deletion
            if (loadBalancerMetadata.getServers().containsKey(serviceStatus.getNodeName())) {
                logger.info("Remove loadBalancerConfig ServerName: " + serviceStatus.getNodeName());
                loadBalancerMetadata.getServers().remove(serviceStatus.getNodeName());
            }
        }

        return loadBalancerMetadata;
    }








}
