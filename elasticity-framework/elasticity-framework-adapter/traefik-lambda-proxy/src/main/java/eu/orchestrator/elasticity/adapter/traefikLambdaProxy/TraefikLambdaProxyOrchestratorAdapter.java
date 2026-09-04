package eu.orchestrator.elasticity.adapter.traefikLambdaProxy;

import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.kv.model.GetValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.orchestrator.elasticity.adapter.traefikLambdaProxy.model.LambdaProxyBackend;
import eu.orchestrator.elasticity.adapter.traefikLambdaProxy.model.LambdaProxyMetadata;
import eu.orchestrator.elasticity.adapter.traefikLambdaProxy.util.TraefikConfiguration;
import eu.orchestrator.elasticity.spi.adapter.ElasticityFrameworkOrchestrator;
import eu.orchestrator.elasticity.spi.model.orchestrator.*;
import eu.orchestrator.elasticity.spi.service.orchestrator.*;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorComponentNodeInstance;
import eu.orchestrator.transfer.entities.orchestrator.internal.ServiceStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 19/7/2019
 */
public class TraefikLambdaProxyOrchestratorAdapter implements ElasticityFrameworkOrchestrator {
    private static final Logger logger = LogManager.getLogger(TraefikLambdaProxyOrchestratorAdapter.class);

    private OrchestratorInteractions orchestratorInteractions;
    private BackendInteractions backendInteractions;
    private OrchestratorComponentService orchestratorComponentService;
    private static MetadataConverter<LambdaProxyMetadata> metadataConverter;

    private String graphHexID;
    private String graphInstanceHexID;
    private String backendName;
    private static final int TRAEFIK_UI_PORT = 15568;


    public static void main(String[] args) {

    }

    public TraefikLambdaProxyOrchestratorAdapter() {
        orchestratorInteractions = new OrchestratorInteractions();
        backendInteractions = new BackendInteractions();
        orchestratorComponentService = new OrchestratorComponentService();
        metadataConverter = new MetadataConverter<>();
    }

    @Override
    public LambdaProxyMetadata initControllerMetadata(List<OrchestratorComponentNodeInstance> componentList) {
        LambdaProxyMetadata lambdaProxyMetadata = new LambdaProxyMetadata();

        //Initialize the backends for the lambdaProxy
        //We assume that only one exist in each graph
        lambdaProxyMetadata.setUiPort("" + TRAEFIK_UI_PORT);
        //Iterate through all the services in order to find which of them are lambda function
        for (OrchestratorComponentNodeInstance component : componentList) {

            if(component.getMonitoringElasticity().getProfile().equals(ElasticityFrameworkType.LAMBDA_FUNCTION.name())){

                //LambdaProxy backend name <-- lambda function component name
                if(!lambdaProxyMetadata.getLambdaProxyBackends().containsKey(component.getComponentNodeHexID())){
                    LambdaProxyBackend lambdaProxyBackend = new LambdaProxyBackend();
                    lambdaProxyBackend.setBackendName(component.getComponentNodeHexID());

                    //get the first port with the assumption that everytime exist exact one port
                    lambdaProxyBackend.setBackendPort("" + component.getPorts().get(0).getPublished());
                    lambdaProxyBackend.setBalanceType(LambdaProxyBackend.BalanceType.valueOf(component.getMonitoringElasticity().getType()));

                    lambdaProxyMetadata.getLambdaProxyBackends().put(component.getComponentNodeHexID(), lambdaProxyBackend);
                }
            }
        }

        ObjectMapper mapper = new ObjectMapper();
        String json = null;
        try {
            json = mapper.writeValueAsString(lambdaProxyMetadata);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        System.out.println("InitControllerMetadata: " + json + "\n\n");

        return lambdaProxyMetadata;
    }

    @Override
    public LambdaProxyMetadata getHealthyWorkers(OrchestratorScalingTO scalingObject, OrchestratorComponentNodeInstance component, HashMap hashMapServerName) {
        DiscoveryServiceInteractions discoveryServiceInteractions = new DiscoveryServiceInteractions(scalingObject.getConsulConfig());
        String graphHexID = scalingObject.getComposeObject().getGraphHexID();
        String graphInstanceHexID = scalingObject.getComposeObject().getGraphInstanceHexID();

        LambdaProxyMetadata currentMetadata = metadataConverter.convertMetadata(component.getControllerMetadata(), LambdaProxyMetadata.class);

        LambdaProxyMetadata lambdaProxyMetadata = new LambdaProxyMetadata();
        lambdaProxyMetadata.setControllerStatus(currentMetadata.getControllerStatus());
        lambdaProxyMetadata.setUiPort(currentMetadata.getUiPort());

        Map<String, LambdaProxyBackend> lambdaProxyBackends = currentMetadata.getLambdaProxyBackends();

        for(String functionName : lambdaProxyBackends.keySet()){


            List<DiscoveryServiceMap> discoveryServiceMapList = discoveryServiceInteractions.getHealthServices(graphHexID, graphInstanceHexID, functionName);
            if(null == discoveryServiceMapList || discoveryServiceMapList.isEmpty()){
                lambdaProxyMetadata.getLambdaProxyBackends().put(functionName, lambdaProxyBackends.get(functionName));
                continue;
            }

            LambdaProxyBackend backend = new LambdaProxyBackend();
            backend.setBackendPort(lambdaProxyBackends.get(functionName).getBackendPort());
            backend.setBalanceType(lambdaProxyBackends.get(functionName).getBalanceType());
            backend.setBackendName(functionName);

            //Search for the IPs of all the nodes that having that service running healthy
            for (DiscoveryServiceMap discoveryServiceMap : discoveryServiceMapList) {
                String serverName = discoveryServiceMap.getServerName();
                String serverIP = discoveryServiceMap.getServerIP();

                //Check if serverName contained in the known server nodes
                if(hashMapServerName.get(serverName)!=null) {
                    //Check if the node name exist in the lambdaProxyBackend metadata
                    if (backend.getServers().containsKey(serverName)) {
                        //Check if the value is changed update it
                        String oldServerIP = backend.getServers().get(serverName);
                        if (!oldServerIP.equals(serverIP)) {
                            backend.getServers().replace(serverName, oldServerIP, serverIP);
                        }
                    }else{ //if the node name doesn't exist apply it
                        //if the serverIP already exist update the node name
                        if (backend.getServers().containsValue(serverIP)) {
                            for(String key : backend.getServers().keySet()){
                                if(backend.getServers().get(key).equals(serverIP)){
                                    logger.warn("Find a duplicated serverIP: " + serverIP + " oldServerName: " + key + " newServerName: " + serverName);

                                    //TODO delete the consul key value path of the LoadBalancer
                                    //TODO delete the old loadBalancerMetadata entity
                                    //TODO put the new one entity
                                    break;
                                }
                            }
                        } else { //just put the new node name and serverIP
                            backend.getServers().put(serverName, serverIP);
                        }
                    }
                }
            }
            lambdaProxyMetadata.getLambdaProxyBackends().put(functionName, backend);
        }
        return lambdaProxyMetadata;
    }

    @Override
    public UpdateControllerTO updateElasticityController(OrchestratorScalingTO scalingObject, OrchestratorComponentNodeInstance component, Object controllerNewMetadata) {
        ConsulClient consulClient = new ConsulClient(scalingObject.getConsulConfig().getUrl());
        String graphHexID = scalingObject.getComposeObject().getGraphHexID();
        String graphInstanceHexID = scalingObject.getComposeObject().getGraphInstanceHexID();
        Boolean ipv6Enabled = scalingObject.getComposeObject().getIpv6Enabled();

        UpdateControllerTO updateControllerTO = new UpdateControllerTO();
        updateControllerTO.setControllerMetadata(null);

        LambdaProxyMetadata lambdaProxyMetadata = metadataConverter.convertMetadata(controllerNewMetadata, LambdaProxyMetadata.class);
        //Check if the lambda proxy config has any values
        //TODO this means that we will not have anytime less than ONE worker
        if (lambdaProxyMetadata == null || lambdaProxyMetadata.getLambdaProxyBackends() == null ||
                lambdaProxyMetadata.getLambdaProxyBackends().isEmpty()) {

            updateControllerTO.setControllerStatus(ControllerStatus.EMPTY_CONTROLLER_CONFIG);
            return updateControllerTO;
        }

        Boolean init = false;
        if (lambdaProxyMetadata.getControllerStatus().getStatus() == ControllerStatus.START_CONTROLLER_STATUS.getStatus()) {
            init = true;
        }

        String componentNodeHexID = component.getComponentNodeHexID();
        Integer status = ControllerStatus.NO_CONTROLLER_UPDATE_NEEDED.getStatus();

        LambdaProxyMetadata currLamdaProxyMetadata = metadataConverter.convertMetadata(component.getControllerMetadata(), LambdaProxyMetadata.class);
        String privateIP = null;
        String ipv6Address = null;
        if(currLamdaProxyMetadata==null){
            privateIP = orchestratorComponentService.getComponentPrivateIP(scalingObject);
            ipv6Address = orchestratorComponentService.getComponentIPv6Address(scalingObject);
            currLamdaProxyMetadata = new LambdaProxyMetadata();
            currLamdaProxyMetadata.setPrivateIP(privateIP);
            lambdaProxyMetadata.setPrivateIP(privateIP);
            currLamdaProxyMetadata.setIpv6Address(ipv6Address);
            lambdaProxyMetadata.setIpv6Address(ipv6Address);
        }else {
            if(currLamdaProxyMetadata.getPrivateIP()==null || currLamdaProxyMetadata.getPrivateIP().isEmpty()){
                privateIP = orchestratorComponentService.getComponentPrivateIP(scalingObject);
                currLamdaProxyMetadata.setPrivateIP(privateIP);
                lambdaProxyMetadata.setPrivateIP(privateIP);
            }else{
                lambdaProxyMetadata.setPrivateIP(currLamdaProxyMetadata.getPrivateIP());
            }

            if(currLamdaProxyMetadata.getIpv6Address()==null || currLamdaProxyMetadata.getIpv6Address().isEmpty()){
                ipv6Address = orchestratorComponentService.getComponentIPv6Address(scalingObject);
                currLamdaProxyMetadata.setIpv6Address(ipv6Address);
                lambdaProxyMetadata.setIpv6Address(ipv6Address);
            }else{
                lambdaProxyMetadata.setIpv6Address(currLamdaProxyMetadata.getIpv6Address());
            }

        }


        Boolean changed = false;
        //Check if current Load Balancer config has no values that means the new one has
        if (currLamdaProxyMetadata == null || currLamdaProxyMetadata.getLambdaProxyBackends() == null ||
                currLamdaProxyMetadata.getLambdaProxyBackends().isEmpty()) {
            changed = true;
        } else {

            for (String functionName : lambdaProxyMetadata.getLambdaProxyBackends().keySet()) {
                LambdaProxyBackend currLambdaProxyBackend =  currLamdaProxyMetadata.getLambdaProxyBackends().get(functionName);
                LambdaProxyBackend lambdaProxyBackend =  lambdaProxyMetadata.getLambdaProxyBackends().get(functionName);

                //Checks if all serverName are in currentLoadBalancer
                //if not the config changed
                for (String serverName : lambdaProxyBackend.getServers().keySet()) {
                    if (!currLambdaProxyBackend.getServers().containsKey(serverName)) {
                        changed = true;
                        break;
                    }
                }

                if(changed){
                    break;
                }

                //Checks if all currServerNames are in newLoadBalancer
                //if not the config changed
                for (String currServerName : currLambdaProxyBackend.getServers().keySet()) {
                    if (!lambdaProxyBackend.getServers().containsKey(currServerName)) {
                        changed = true;
                        break;
                    }
                }

                if(changed){
                    break;
                }

                //Checks if all serverIPs are in currentLoadBalancer
                //if not the config changed
                for (String serverIP : lambdaProxyBackend.getServers().values()) {
                    if (!currLambdaProxyBackend.getServers().containsValue(serverIP)) {
                        changed = true;
                        break;
                    }
                }

                if(changed){
                    break;
                }

                //Checks if all currServerIPs are in newLoadBalancer
                //if not the config changed
                for (String currServerIP : currLambdaProxyBackend.getServers().values()) {
                    if (!lambdaProxyBackend.getServers().containsValue(currServerIP)) {
                        changed = true;
                        break;
                    }
                }

                if(changed){
                    break;
                }
            }
        }

        //TODO check that the KV updated correctly
        //if changed then we must update the KV Consul module
        if (changed) {
            //If is the first time we initialize the Load Balancer, push the Static Configuration
            if (init) {
                TraefikConfiguration.pushLambdaProxyStaticConfig( scalingObject.getConsulConfig(),
                        graphHexID, graphInstanceHexID, componentNodeHexID,lambdaProxyMetadata);

                logger.debug("Push Static Config for the Load Balancer: " + componentNodeHexID);
                status = ControllerStatus.FIRST_CONTROLLER_CONFIG.getStatus();
            } else {
                status = ControllerStatus.CONTROLLER_CONFIG_UPDATED.getStatus();
            }

            updateControllerTO.setControllerMetadata(lambdaProxyMetadata);


            TraefikConfiguration.pushLambdaProxyDynamicConfig( scalingObject.getConsulConfig(),
                    graphHexID, graphInstanceHexID, componentNodeHexID, lambdaProxyMetadata, ipv6Enabled);

            logger.debug("Push Dynamic Config for the Load Balancer: " + componentNodeHexID);

            //If is the first time we initialize the Load Balancer, change the lbStatus to startLBStatus
            if (init) {
                consulClient.setKVValue(graphHexID + "/" + graphInstanceHexID + "/" + componentNodeHexID
                        + "/" + component.getComponentNodeInstanceHexID() + "/" + "lbStatus", "" + ControllerStatus.START_CONTROLLER_STATUS.getStatus());
            }
        }else{
            updateControllerTO.setControllerMetadata(currLamdaProxyMetadata);
        }

        updateControllerTO.setControllerStatus(ControllerStatus.nameOf(status));
        return updateControllerTO;
    }

    @Override
    public ScaleOutTO scaleOut(OrchestratorScalingTO scalingObject) {
        ScaleOutTO scaleOutTO = orchestratorInteractions.calculateRunningWorkers(scalingObject);
        if(!scaleOutTO.getProceedWithScaling()){
            logger.error("Scale out calculateRunningWorkers failed");
            return scaleOutTO;
        }

        scaleOutTO = orchestratorInteractions.calculateWorkersToRequest(scaleOutTO, scalingObject);
        if(!scaleOutTO.getProceedWithScaling()){
            logger.error("Scale out calculateWorkersToRequest failed");
            return scaleOutTO;
        }

        scaleOutTO =  backendInteractions.scaleOutRequest(scaleOutTO, scalingObject);
        if(!scaleOutTO.getProceedWithScaling()){
            logger.error("Scale out scaleOutRequest failed");
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
        graphHexID = scalingObject.getComposeObject().getGraphHexID();
        graphInstanceHexID = scalingObject.getComposeObject().getGraphInstanceHexID();
        ConsulClient consulClient = new ConsulClient(scalingObject.getConsulConfig().getUrl());
        String componentNodeHexId = scaleInTO.getQualifiedForRemoval().get(0).getBalancedByComponentNodeHexID();

        //Construct the consul client in order to query the lb configuration
        String kvLBConfigPath = "";
        String pattern = "";
        backendName = "";

        backendName = scaleInTO.getQualifiedForRemoval().get(0).getComponentNodeHexID();
//        for(OrchestratorComponentNodeInstance service : scalingObject.getComposeObject().getServices()){
//            if(service.getLambdaProxy()){
//                componentNodeHexId = service.getComponentNodeHexID();
//                break;
//            }
//        }
        if(componentNodeHexId==null || componentNodeHexId.isEmpty()){
            scaleInTO.setProceedWithScaling(false);
            return scaleInTO;
        }

        kvLBConfigPath = graphHexID + "/" + graphInstanceHexID + "/" + componentNodeHexId + "/" + "lbConfiguration" + "/backends";
        pattern = "(" + kvLBConfigPath + "/" + backendName + "/servers/";

        logger.info("We will remove servers from the Lambda Proxy for function: " + backendName);

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
    public LambdaProxyMetadata postScaleInControllerMetadataClean(ScaleInTO scaleInTO, Object controllerMetadata){
        LambdaProxyMetadata lambdaProxyMetadata = metadataConverter.convertMetadata(controllerMetadata, LambdaProxyMetadata.class);

        LambdaProxyBackend lambdaProxyBackend = lambdaProxyMetadata.getLambdaProxyBackends().get(backendName);
        logger.info("BackendName: " + backendName);
        if(lambdaProxyBackend == null){
            logger.warn("Couldn't find a Lambda Proxy Backend for function: " + backendName +
                    " in the graph " + graphHexID + ":" + graphInstanceHexID);
        }else{
            //if we correctly found an Lambda Proxy Backend remove the IPs
            for (ServiceStatus serviceStatus : scaleInTO.getQualifiedForRemoval()) {
                //Identify which paths need deletion
                logger.info("NodeName: " + serviceStatus.getNodeName());
                if (lambdaProxyBackend.getServers().containsKey(serviceStatus.getNodeName())) {
                    logger.info("Remove ServerName: " + serviceStatus.getNodeName() + " from function: " + backendName);
                    lambdaProxyBackend.getServers().remove(serviceStatus.getComponentNodeInstanceHexID());
                }
            }
            lambdaProxyMetadata.getLambdaProxyBackends().replace(backendName, lambdaProxyBackend);
        }

        return lambdaProxyMetadata;
    }





}
