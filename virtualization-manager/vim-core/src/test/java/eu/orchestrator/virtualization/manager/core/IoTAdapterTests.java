package eu.orchestrator.virtualization.manager.core;

import eu.orchestrator.transfer.entities.orchestrator.OrchestratorProviderAuthenticationDetails;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

/**
 * @author Konstantinos Theodosiou
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {eu.orchestrator.virtualization.manager.core.Application.class})
public class IoTAdapterTests {
    private static String vimURI = "http://127.0.0.1:8070/api/v1";

    private static String iotGatewayURI = System.getProperty("iot.gateway.uri", "http://localhost:8080/api/v1/");

    private OrchestratorProviderAuthenticationDetails getIoTCredentials(){
        OrchestratorProviderAuthenticationDetails authenticationDetails = new OrchestratorProviderAuthenticationDetails();
        authenticationDetails.setId("IoTGatewayID");
        authenticationDetails.setName("checkIoTCredential");
        authenticationDetails.setAdapterImplementation("eu.orchestrator.adapter.iot.IoTAdapter");
        authenticationDetails.setAdapterType("IoT");
        authenticationDetails.setEndpoint(iotGatewayURI);
        authenticationDetails.setUsername(System.getProperty("openstack.username", ""));
        authenticationDetails.setPassword(System.getProperty("openstack.password", ""));
        authenticationDetails.setPublicKey("");
        authenticationDetails.setPrivateKey("");
        authenticationDetails.setRegion("");
        authenticationDetails.setDomain("");
        authenticationDetails.setProject("");
        authenticationDetails.setPublicNetwork("");

        return authenticationDetails;
    }

    @Test
//    @Ignore
    public void validateCredentials(){
        OrchestratorProviderAuthenticationDetails authenticationDetails = getIoTCredentials();

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity entity = new HttpEntity(authenticationDetails);
        ResponseEntity<String>
                responseEntity = restTemplate.exchange(vimURI+"/credential/validate", HttpMethod.POST, entity, String.class);

        if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {

            JSONObject callbackJSON = null;
            try {
                callbackJSON = new JSONObject(responseEntity.getBody());
            } catch (JSONException e) {
                System.out.println("checkIoTCredential FAILED, can't create JSONObject");
                e.printStackTrace();
            }

            try {
                if (callbackJSON.getString("code").equals("SUCCESS")) {
                    System.out.println("checkIoTCredential PASSED");
                    return;
                }
            } catch (JSONException e) {
                System.out.println("checkIoTCredential FAILED, can't stringify the callbackJSON");
                e.printStackTrace();
            }
        }
        System.out.println("checkIoTCredential FAILED");
    }

    @Test
//    @Ignore
    public void getResources(){
        OrchestratorProviderAuthenticationDetails authenticationDetails = getIoTCredentials();

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity entity = new HttpEntity(authenticationDetails);
        ResponseEntity<String>
                responseEntity = restTemplate.exchange(vimURI+"/provider/resources", HttpMethod.POST, entity, String.class);

        if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {

            JSONObject callbackJSON = null;
            try {
                callbackJSON = new JSONObject(responseEntity.getBody());
            } catch (JSONException e) {
                System.out.println("checkIoTResources FAILED, can't create JSONObject");
                e.printStackTrace();
            }

            try {
                if (callbackJSON.getString("code").equals("SUCCESS")) {
                    System.out.println("checkIoTResources PASSED");
                    System.out.println(callbackJSON);
                    return;
                }
            } catch (JSONException e) {
                System.out.println("checkIoTResources FAILED, can't stringify the callbackJSON");
                e.printStackTrace();
            }
        }
        System.out.println("checkIoTResources FAILED");
    }

    @Test
//    @Ignore
    public void getTopology(){
        OrchestratorProviderAuthenticationDetails authenticationDetails = getIoTCredentials();

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity entity = new HttpEntity(authenticationDetails);
        ResponseEntity<String>
                responseEntity = restTemplate.exchange(vimURI+"/provider/topology", HttpMethod.POST, entity, String.class);

        if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {

            JSONObject callbackJSON = null;
            try {
                callbackJSON = new JSONObject(responseEntity.getBody());
            } catch (JSONException e) {
                System.out.println("checkIoTTopologyFAILED, can't create JSONObject");
                e.printStackTrace();
            }

            try {
                if (callbackJSON.getString("code").equals("SUCCESS")) {
                    System.out.println("checkIoTTopology PASSED");
                    System.out.println(callbackJSON);
                    return;
                }
            } catch (JSONException e) {
                System.out.println("checkIoTTopology FAILED, can't stringify the callbackJSON");
                e.printStackTrace();
            }
        }
        System.out.println("checkIoTTopology FAILED");
    }
}
