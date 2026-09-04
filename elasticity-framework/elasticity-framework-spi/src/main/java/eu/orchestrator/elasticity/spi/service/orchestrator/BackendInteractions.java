package eu.orchestrator.elasticity.spi.service.orchestrator;

import eu.orchestrator.elasticity.spi.model.metricModel.Elasticity;
import eu.orchestrator.elasticity.spi.model.orchestrator.ScaleOutTO;
import eu.orchestrator.elasticity.spi.model.orchestrator.OrchestratorScalingTO;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorApplicationInstance;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorScalingRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.json.JSONObject;

import java.io.IOException;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 19/7/2019
 */
public class BackendInteractions {
    private static final Logger logger = LogManager.getLogger(BackendInteractions.class);
    private ObjectMapper objectMapper = new ObjectMapper();

    public ScaleOutTO scaleOutRequest(ScaleOutTO scaleOutTO, OrchestratorScalingTO scalingObject){

        scalingObject.getMetricCollector().logMetric(scalingObject.getElasticityDimension(), Elasticity.ScaleOutElasticity.getStatus());

        //Construct the Orchestrator Scaling Request for the backend Microservice
        OrchestratorScalingRequest scalingRequest = new OrchestratorScalingRequest();
        scalingRequest.setGraphID(scalingObject.getComposeObject().getGraphID());
        scalingRequest.setGraphHexID(scalingObject.getComposeObject().getGraphHexID());
        scalingRequest.setGraphName(scalingObject.getComposeObject().getGraphName());
        scalingRequest.setGraphInstanceID(scalingObject.getComposeObject().getGraphInstanceID());
        scalingRequest.setGraphInstanceHexID(scalingObject.getComposeObject().getGraphInstanceHexID());
        scalingRequest.setGraphInstanceName(scalingObject.getComposeObject().getGraphInstanceName());

        scalingRequest.setComponentNodeID(scaleOutTO.getServiceToScale().getComponentNodeID());
        scalingRequest.setComponentNodeHexID(scaleOutTO.getServiceToScale().getComponentNodeHexID());
        scalingRequest.setComponentNodeName(scaleOutTO.getServiceToScale().getComponentNodeName());
        scalingRequest.setComponentNodeInstanceID(scaleOutTO.getServiceToScale().getComponentNodeInstanceID());
        scalingRequest.setComponentNodeInstanceHexID(scaleOutTO.getServiceToScale().getComponentNodeInstanceHexID());
        scalingRequest.setComponentNodeInstanceName(scaleOutTO.getServiceToScale().getComponentNodeInstanceName());

        scalingRequest.setNumberOfWorkers(scaleOutTO.getWorkersToRequest());

        //Make rest call to the backend in order to get the composeObject for the scaling action
        String scaleUpURI = "http://" + scalingObject.getBackendURL() + ":" + scalingObject.getBackendPORT() + "/api/v1/scaling/up";
        OrchestratorApplicationInstance scalingObjectResponse = null;
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity entity = new HttpEntity(scalingRequest);


        ResponseEntity<String> responseEntity =
                restTemplate.exchange(scaleUpURI, HttpMethod.POST, entity, String.class);
        //Check if the responseEntity is proper
        if (null != responseEntity) {
            if (responseEntity.getStatusCode() == HttpStatus.OK) {

                logger.debug("Response: " + responseEntity.getBody());
                JSONObject callbackJSON = new JSONObject(responseEntity.getBody());

                if (callbackJSON.getString("code").equals("SUCCESS")) {

                    try {
                        scalingObjectResponse = objectMapper.readValue(callbackJSON.optString("returnobject"), OrchestratorApplicationInstance.class);
                        if (scalingObjectResponse==null){
                            logger.warn("Backend scaling up endpoint respond with null returnObject");
                            scaleOutTO.setProceedWithScaling(false);
                            return scaleOutTO;
                        }
                    } catch (IOException e) {
                        logger.warn("Couldn't cast it to Compose Object");
                        scaleOutTO.setProceedWithScaling(false);
                        return scaleOutTO;
                    }
                } else {
                    logger.warn("Backend scaling up endpoint respond with callback code: " +
                            callbackJSON.getString("code"));
                    scaleOutTO.setProceedWithScaling(false);
                    return scaleOutTO;
                }
            } else {
                logger.warn("Backend scaling up endpoint respond with HttpStatus: " + responseEntity.getStatusCode());
                scaleOutTO.setProceedWithScaling(false);
                return scaleOutTO;
            }
        }else{
            logger.error("Backend scaling up endpoint return with empty responseEntity");
            scaleOutTO.setProceedWithScaling(false);
            return scaleOutTO;
        }

        scaleOutTO.setProceedWithScaling(true);
        scaleOutTO.setScalingComposeObject(scalingObjectResponse);

        scalingObject.getMetricCollector().logMetric(scalingObject.getElasticityDimension(), Elasticity.ScaleOutBackendReply.getStatus());

        return scaleOutTO;
    }

    void scaleInRequest(){

    }
}
