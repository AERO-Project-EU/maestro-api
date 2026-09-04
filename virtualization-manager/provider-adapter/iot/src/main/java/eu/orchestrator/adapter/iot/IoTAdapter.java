package eu.orchestrator.adapter.iot;

import eu.orchestrator.transfer.entities.iotstack.Credentials;
import eu.orchestrator.spi.adapter.ProviderAdapter;
import eu.orchestrator.spi.model.*;
import eu.orchestrator.spi.response.BasicResponseCode;
import eu.orchestrator.spi.response.SPIResponse;
import eu.orchestrator.transfer.entities.iotstack.IoTBootRequest;
import eu.orchestrator.transfer.entities.iotstack.IoTRemoveInstance;
import eu.orchestrator.transfer.entities.iotstack.Topology;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Konstantinos Theodosiou
 */
public class IoTAdapter implements ProviderAdapter {
    private static final Logger logger = Logger.getLogger(IoTAdapter.class.getName());
    private static final String validateCredentialsEndpoint = "validatecredentials";
    private static final String resourcesEndpoint = "resources";
    private static final String topologyEndpoint = "topology";
    private static final String bootInstance = "instance";
    private static final String removeInstance = "deleteinstance";

    public static void main(String[] args) {

    }

    @Override
    public SPIResponse validateCredentials(CredentialsModel credentials) {
        String iotGatewayURI = credentials.getEndpoint();
        if(iotGatewayURI == null || iotGatewayURI.isEmpty()){
            return  new SPIResponse(BasicResponseCode.EXCEPTION, "Error occurred. Endpoint doesn't have been specified!");
        }

        if(iotGatewayURI.endsWith("/")){
            iotGatewayURI += validateCredentialsEndpoint;
        }else{
            iotGatewayURI += "/" + validateCredentialsEndpoint;
        }

        RestTemplate restTemplate = new RestTemplate();

        Credentials iotCredentials = new Credentials();
        iotCredentials.setUsername(credentials.getUsername());
        iotCredentials.setPassword(credentials.getPassword());

        HttpEntity entity = new HttpEntity(iotCredentials, null);
        ResponseEntity<String> responseEntity = null;

        try {
            responseEntity = restTemplate.exchange(iotGatewayURI, HttpMethod.POST, entity, String.class);
        }catch (RestClientException e){
            return new SPIResponse(BasicResponseCode.EXCEPTION, "Error occurred. Please try again!");
        }

        if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {

            logger.info("Response: " + responseEntity.getBody());

            JSONObject callbackJSON = new JSONObject(responseEntity.getBody());

            if (callbackJSON.getString("rescode").equals("SUCCESS")) {

                return new SPIResponse(BasicResponseCode.SUCCESS, "Credentials are valid!");
            }
        }

        return new SPIResponse(BasicResponseCode.EXCEPTION, "Credentials are invalid!");
    }

    @Override
    public SPIResponse createFlavor(CredentialsModel credentials, FlavorModel flavor) {
        return new SPIResponse(BasicResponseCode.NOT_SUPPORTED, "Error occurred. This method doesn't supported!");
    }

    @Override
    public SPIResponse getFlavors(CredentialsModel credentials) {
        return new SPIResponse(BasicResponseCode.NOT_SUPPORTED, "Error occurred. This method doesn't supported!");
    }

    @Override
    public SPIResponse getImages(CredentialsModel credentials) {
        return new SPIResponse(BasicResponseCode.NOT_SUPPORTED, "Error occurred. This method doesn't supported!");
    }

    @Override
    public SPIResponse createImage(CredentialsModel credentials, ImageModel image) {
        return new SPIResponse(BasicResponseCode.NOT_SUPPORTED, "Error occurred. This method doesn't supported!");
    }

    @Override
    public SPIResponse removeImage(CredentialsModel credentials, ImageModel image) {
        return new SPIResponse(BasicResponseCode.NOT_SUPPORTED, "Error occurred. This method doesn't supported!");
    }

    @Override
    public SPIResponse getInstances(CredentialsModel credentials) {
        return new SPIResponse(BasicResponseCode.EXCEPTION, "Error occurred. This method doesn't supported!");
    }

    @Override
    public SPIResponse bootInstance(CredentialsModel credentials, ImageModel image, FlavorModel flavor, InstanceModel instance) {
//        user data = flag lb ipv6 masterIPv6 masterIPv4 port login password publicKey peerName
//        instance name graphId + "_" + graphInstanceId + "_" + componentNodeId + "_" + componentNodeInstanceId

        String iotGatewayURI = credentials.getEndpoint();
        if(iotGatewayURI == null || iotGatewayURI.isEmpty()){
            return  new SPIResponse(BasicResponseCode.EXCEPTION, "Error occurred. Endpoint doesn't have been specified!");
        }

        if(iotGatewayURI.endsWith("/")){
            iotGatewayURI += bootInstance;
        }else{
            iotGatewayURI += "/" + bootInstance;
        }

        IoTBootRequest ioTBootRequest = new IoTBootRequest();

        //Set credentials
        Credentials iotCredentials = new Credentials();
        iotCredentials.setUsername(credentials.getUsername());
        iotCredentials.setPassword(credentials.getPassword());
        ioTBootRequest.setCredentials(iotCredentials);

        String[] agentIDs = instance.getName().split("_");
        String[] metadata = instance.getUserData().split(" ");


        //Agent arguments
        ioTBootRequest.setGraphID(agentIDs[0]);
        ioTBootRequest.setGraphInstanceID(agentIDs[1]);
        ioTBootRequest.setComponentNodeID(agentIDs[2]);
        ioTBootRequest.setComponentNodeInstanceID(agentIDs[3]);

        ioTBootRequest.setFlag(Boolean.parseBoolean(metadata[0]));
        ioTBootRequest.setLb(Boolean.parseBoolean(metadata[1]));
        ioTBootRequest.setIpv6(Boolean.parseBoolean(metadata[2]));

        //Consul agruments
        ioTBootRequest.setMasterIPv6(metadata[3]);

        //CJDNS arguments
        ioTBootRequest.setMasterIPv4(metadata[4]);
        ioTBootRequest.setPort(metadata[5]);
        ioTBootRequest.setLogin(metadata[6]);
        ioTBootRequest.setPassword(metadata[7]);
        ioTBootRequest.setPublicKey(metadata[8]);
        ioTBootRequest.setPeerName(metadata[9]);
        ioTBootRequest.setNexusIPv6(metadata[10]);


        // Flavor
        ioTBootRequest.setvCPUs(Integer.parseInt(flavor.getvCPU()));
        ioTBootRequest.setRam(Integer.parseInt(flavor.getRam()));
        ioTBootRequest.setStorage(Integer.parseInt(flavor.getStorage()));

        RestTemplate restTemplate = new RestTemplate();

        HttpEntity entity = new HttpEntity(ioTBootRequest, null);
        ResponseEntity<String> responseEntity = null;

        try {
            responseEntity = restTemplate.exchange(iotGatewayURI, HttpMethod.POST, entity, String.class);
        }catch (RestClientException e){
            System.out.println("Error occurred. Please try again!  172");
            return new SPIResponse(BasicResponseCode.EXCEPTION, "Error occurred. Please try again!");
        }

        if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {

            logger.info("Response: " + responseEntity.getBody());

            JSONObject callbackJSON = new JSONObject(responseEntity.getBody());

            if (callbackJSON.getString("rescode").equals("SUCCESS")) {

                if(null != callbackJSON.get("resobject")) {

                    instance.setId(callbackJSON.get("resobject").toString());
                    System.out.println("Instance has been booted successfully  186");

                    return new SPIResponse(BasicResponseCode.SUCCESS, "Instance has been booted successfully",
                            instance);
                }else{
                    System.out.println("Error occurred. Couldn't get the machine id. Please try again! 192");

                    return new SPIResponse(BasicResponseCode.EXCEPTION, "Error occurred. Couldn't get the machine id. Please try again!");
                }
            }
        }else{
            System.out.println("Instance has not been booted successfully! 198");
            return new SPIResponse(BasicResponseCode.EXCEPTION, "Instance has not been booted successfully");
        }
        System.out.println("Error occurred. Please try again! 201");

        return new SPIResponse(BasicResponseCode.EXCEPTION, "Error occurred. Please try again!");
    }

    @Override
    public SPIResponse bootInstanceWithBootableVolume(CredentialsModel credentials, FlavorModel flavor, InstanceModel instance, VolumeModel volumeBootable) {
      return new SPIResponse(BasicResponseCode.NOT_SUPPORTED, "Error occurred. This method doesn't supported!");
    }

    @Override
    public SPIResponse bootInstanceWithAttachedVolume(CredentialsModel credentials, ImageModel image, FlavorModel flavor, InstanceModel instance, VolumeModel attachedVolume) {
      return new SPIResponse(BasicResponseCode.NOT_SUPPORTED, "Error occurred. This method doesn't supported!");
    }

    @Override
    public SPIResponse bootInstanceWithBootableVolumeAndAttachedVolumes(CredentialsModel credentials, FlavorModel flavor, InstanceModel instance, VolumeModel volumeBootable, VolumeModel attachedVolume) {
      return new SPIResponse(BasicResponseCode.NOT_SUPPORTED, "Error occurred. This method doesn't supported!");
    }

    @Override
    public SPIResponse removeInstance(CredentialsModel credentials, InstanceModel instance) {

        String iotGatewayURI = credentials.getEndpoint();
        if(iotGatewayURI == null || iotGatewayURI.isEmpty()){
            return  new SPIResponse(BasicResponseCode.EXCEPTION, "Error occurred. Endpoint doesn't have been specified!");
        }

        if(iotGatewayURI.endsWith("/")){
            iotGatewayURI += removeInstance;
        }else{
            iotGatewayURI += "/" + bootInstance;
        }

        IoTRemoveInstance ioTRemoveInstance = new IoTRemoveInstance();

        //Set credentials
        Credentials iotCredentials = new Credentials();
        iotCredentials.setUsername(credentials.getUsername());
        iotCredentials.setPassword(credentials.getPassword());

        ioTRemoveInstance.setCredentials(iotCredentials);
        ioTRemoveInstance.setId(instance.getId());


        RestTemplate restTemplate = new RestTemplate();

        HttpEntity entity = new HttpEntity(ioTRemoveInstance, null);
        ResponseEntity<String> responseEntity = null;

        try {
            responseEntity = restTemplate.exchange(iotGatewayURI, HttpMethod.DELETE, entity, String.class);
        }catch (RestClientException e){
            System.out.println("Error occurred. Please try again!  243");
            return new SPIResponse(BasicResponseCode.EXCEPTION, "Error occurred. Please try again!");
        }

        if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {

            logger.info("Response: " + responseEntity.getBody());

            JSONObject callbackJSON = new JSONObject(responseEntity.getBody());

            if (callbackJSON.getString("rescode").equals("SUCCESS")) {

                System.out.println("Instance has been removed successfully  255");

                    return new SPIResponse(BasicResponseCode.SUCCESS, "Instance has been booted successfully",
                            instance);
            }
        }

        System.out.println("Error occurred. Please try again! 262");
        return new SPIResponse(BasicResponseCode.EXCEPTION, "Error occurred. Please try again!");
    }

    @Override
    public SPIResponse getResources(CredentialsModel credentials) {
        String iotGatewayURI = credentials.getEndpoint();
        if(iotGatewayURI == null || iotGatewayURI.isEmpty()){
            return  new SPIResponse(BasicResponseCode.EXCEPTION, "Error occurred. Endpoint doesn't have been specified!");
        }

        if(iotGatewayURI.endsWith("/")){
            iotGatewayURI += resourcesEndpoint;
        }else{
            iotGatewayURI += "/" + resourcesEndpoint;
        }

        RestTemplate restTemplate = new RestTemplate();

        Credentials iotCredentials = new Credentials();
        iotCredentials.setUsername(credentials.getUsername());
        iotCredentials.setPassword(credentials.getPassword());


        HttpEntity entity = new HttpEntity(iotCredentials, null);
        ResponseEntity<String> responseEntity = null;

        try{
            responseEntity = restTemplate.exchange( iotGatewayURI, HttpMethod.POST, entity, String.class);
        }catch(RestClientException e){
            return new SPIResponse(BasicResponseCode.EXCEPTION, "Error occurred. Please try again!");
        }


        if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {

            logger.info("Response: " + responseEntity.getBody());

            JSONObject callbackJSON = new JSONObject(responseEntity.getBody());

            if (callbackJSON.getString("rescode").equals("SUCCESS")) {
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    ResourcesModel resourcesModel = objectMapper.readValue(callbackJSON.optString("resobject"), ResourcesModel.class);
                    if (resourcesModel==null){
                        logger.warning("The resourceModel is emtpy");
                        return new SPIResponse(BasicResponseCode.EXCEPTION, "Error occurred. Please try again!");
                    }else{
                        resourcesModel.setId(credentials.getId());
                        return new SPIResponse(BasicResponseCode.SUCCESS, "Resources have been gotten successfully!", resourcesModel);
                    }
                } catch (IOException e) {
                    logger.warning("Exception occurred: " +  e);
                    return new SPIResponse(BasicResponseCode.EXCEPTION, "Error occurred. Please try again!");
                }
            }
        }

        return new SPIResponse(BasicResponseCode.EXCEPTION, "Error occurred. Please try again!");
    }

    @Override
    public SPIResponse getTopology(CredentialsModel credentials) {
        String iotGatewayURI = credentials.getEndpoint();
        if(iotGatewayURI == null || iotGatewayURI.isEmpty()){
            return  new SPIResponse(BasicResponseCode.EXCEPTION, "Error occurred. Endpoint doesn't have been specified!");
        }

        if(iotGatewayURI.endsWith("/")){
            iotGatewayURI += topologyEndpoint;
        }else{
            iotGatewayURI += "/" + topologyEndpoint;
        }

        RestTemplate restTemplate = new RestTemplate();

        Credentials iotCredentials = new Credentials();
        iotCredentials.setUsername(credentials.getUsername());
        iotCredentials.setPassword(credentials.getPassword());

        HttpEntity entity = new HttpEntity(iotCredentials, null);
        ResponseEntity<String> responseEntity = null;

        try{
            responseEntity = restTemplate.exchange( iotGatewayURI, HttpMethod.POST, entity, String.class);
        }catch(RestClientException e){
            return new SPIResponse(BasicResponseCode.EXCEPTION, "Error occurred. Please try again!");
        }

        if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {

            logger.info("Response: " + responseEntity.getBody());

            JSONObject callbackJSON = new JSONObject(responseEntity.getBody());

            if (callbackJSON.getString("rescode").equals("SUCCESS")) {
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    Topology topology = objectMapper.readValue(callbackJSON.optString("resobject"), Topology.class);
                    if (topology==null){
                        logger.warning("The topology is emtpy");
                        return new SPIResponse(BasicResponseCode.EXCEPTION, "Error occurred. Please try again!");
                    }else{
                        topology.setId(credentials.getId());
                        return new SPIResponse(BasicResponseCode.SUCCESS, "Topology have been gotten successfully!", topology);
                    }
                } catch (IOException e) {
                    logger.warning("Exception occurred: " +  e);
                    return new SPIResponse(BasicResponseCode.EXCEPTION, "Error occurred. Please try again!");
                }
            }
        }

        return new SPIResponse(BasicResponseCode.EXCEPTION, "Error occurred. Please try again!");
    }

    @Override
    public SPIResponse getVolumes(CredentialsModel credentials) {
        return new SPIResponse(BasicResponseCode.NOT_SUPPORTED, "Error occurred. This method doesn't supported!");
    }

    @Override
    public SPIResponse createVolumeBootable(CredentialsModel credentials, VolumeModel volume, ImageModel image) {
        return new SPIResponse(BasicResponseCode.NOT_SUPPORTED, "Error occurred. This method doesn't supported!");
    }

    @Override
    public SPIResponse createVolumeNoBootable(CredentialsModel credentials, VolumeModel volume) {
        return new SPIResponse(BasicResponseCode.NOT_SUPPORTED, "Error occurred. This method doesn't supported!");
    }

    @Override
    public SPIResponse removeVolume(CredentialsModel credentials, VolumeModel volume) {
        return new SPIResponse(BasicResponseCode.NOT_SUPPORTED, "Error occurred. This method doesn't supported!");
    }

    @Override
    public SPIResponse attachFloatingIP(CredentialsModel credentials, InstanceModel instanceModel) {
        return new SPIResponse(BasicResponseCode.NOT_SUPPORTED, "Error occurred. This method doesn't supported!");
    }

    @Override
    public SPIResponse detachFloatingIP(CredentialsModel credentials, InstanceModel instanceModel) {
        return new SPIResponse(BasicResponseCode.NOT_SUPPORTED, "Error occurred. This method doesn't supported!");
    }
}
