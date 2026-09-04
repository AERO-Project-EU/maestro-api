package eu.orchestrator.virtualization.manager.core;

import eu.orchestrator.transfer.entities.orchestrator.OrchestratorFlavor;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorInstance;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorProviderAuthenticationDetails;
import eu.orchestrator.transfer.entities.virtualizationManager.VirtulizationManagerRequest;
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

import java.util.Arrays;

/**
 * @author Konstantinos Theodosiou
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {eu.orchestrator.virtualization.manager.core.Application.class})
public class InstanceRestTests {
    private static String credentialsURI = "http://127.0.0.1:8070/api/v1/instance";

    @Test
    @Ignore
    public void checkRemoveInstance(){
        String graphId = "CMSApp";
        String graphInstanceId = "k4";
        String componentNodeId = "phpMyAdmin";
        String componentNodeInstanceId = "phpMyAdmin";

        OrchestratorProviderAuthenticationDetails authenticationDetails = new OrchestratorProviderAuthenticationDetails();
        authenticationDetails.setId("284");
        authenticationDetails.setAdapterType("OPENSTACK");
        authenticationDetails.setAdapterImplementation("eu.orchestrator.adapter.openstack.OpenStackAdapter");
        authenticationDetails.setEndpoint(System.getProperty("openstack.endpoint", ""));
        authenticationDetails.setUsername(System.getProperty("openstack.username", ""));
        authenticationDetails.setPassword(System.getProperty("openstack.password", ""));
        authenticationDetails.setDomain("default");
        authenticationDetails.setProject("maestro");
        authenticationDetails.setPublicNetwork("provider");


        OrchestratorInstance instance = new OrchestratorInstance();
        instance.setName(graphId + "_" + graphInstanceId + "_" + componentNodeId + "_" + componentNodeInstanceId);
        instance.setId("24fd81eb-5fbc-4fa2-aa70-257d7fa82983");
        VirtulizationManagerRequest vimRequest = new VirtulizationManagerRequest();
        vimRequest.setAuthDetails(authenticationDetails);
        vimRequest.setInstance(instance);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity entity = new HttpEntity(vimRequest);
        ResponseEntity<String> responseEntity = restTemplate.exchange(credentialsURI + "/remove", HttpMethod.POST, entity, String.class);

        if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {


            JSONObject callbackJSON = null;
            try {
                callbackJSON = new JSONObject(responseEntity.getBody());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            System.out.println(callbackJSON);
        }else{
//            System.out.println(responseEntity.getStatusCode());
//            System.out.println(responseEntity.getStatusCodeValue());
        }
    }

    @Test
//    @Ignore
    public void checkSpawnInstance(){
        String graphId = "CMSApp";
        String graphInstanceId = "konstheo";
        String componentNodeId = "phpMyAdmin";
        String componentNodeInstanceId = "phpMyAdmin";

        OrchestratorProviderAuthenticationDetails authenticationDetails = new OrchestratorProviderAuthenticationDetails();
        authenticationDetails.setId("284");
        authenticationDetails.setAdapterType("OPENSTACK");
        authenticationDetails.setAdapterImplementation("eu.orchestrator.adapter.openstack.OpenStackAdapter");
        authenticationDetails.setEndpoint(System.getProperty("openstack.endpoint", ""));
        authenticationDetails.setUsername(System.getProperty("openstack.username", ""));
        authenticationDetails.setPassword(System.getProperty("openstack.password", ""));
        authenticationDetails.setDomain("default");
        authenticationDetails.setProject("maestro");
        authenticationDetails.setPublicNetwork("provider");


        OrchestratorInstance instance = new OrchestratorInstance();
        instance.setName(graphId + "_" + graphInstanceId + "_" + componentNodeId + "_" + componentNodeInstanceId);
        instance.setUserData("");
        instance.setNetworkIDList(Arrays.asList("12963275-4bd6-4dd4-a9eb-0e6429655556"));
        instance.setKeyPair("maestrokey");

        VirtulizationManagerRequest vimRequest = new VirtulizationManagerRequest();
        vimRequest.setAuthDetails(authenticationDetails);
        vimRequest.setInstance(instance);
        OrchestratorFlavor flavor = new OrchestratorFlavor();
        flavor.setvCPUs(4);
        flavor.setRam(4096);
        flavor.setStorage(100);
        vimRequest.setFlavor(flavor);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity entity = new HttpEntity(vimRequest);
        ResponseEntity<String> responseEntity = restTemplate.exchange(credentialsURI + "/boot", HttpMethod.POST, entity, String.class);

        if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {


            JSONObject callbackJSON = null;
            try {
                callbackJSON = new JSONObject(responseEntity.getBody());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            System.out.println(callbackJSON);
        }else{
//            System.out.println(responseEntity.getStatusCode());
//            System.out.println(responseEntity.getStatusCodeValue());
        }
    }

    @Test
//    @Ignore
    public void checkgetInstances(){

        String graphId = "CMSApp";
        String graphInstanceId = "konstheo";
        String componentNodeId = "phpMyAdmin";
        String componentNodeInstanceId = "phpMyAdmin";

        OrchestratorProviderAuthenticationDetails authenticationDetails = new OrchestratorProviderAuthenticationDetails();
        authenticationDetails.setId("284");
        authenticationDetails.setAdapterType("OPENSTACK");
        authenticationDetails.setAdapterImplementation("eu.orchestrator.adapter.openstack.OpenStackAdapter");
        authenticationDetails.setEndpoint(System.getProperty("openstack.endpoint", ""));
        authenticationDetails.setUsername(System.getProperty("openstack.username", ""));
        authenticationDetails.setPassword(System.getProperty("openstack.password", ""));
        authenticationDetails.setDomain("default");
        authenticationDetails.setProject("maestro");
        authenticationDetails.setPublicNetwork("provider");


        OrchestratorInstance instance = new OrchestratorInstance();
        instance.setName(graphId + "_" + graphInstanceId + "_" + componentNodeId + "_" + componentNodeInstanceId);
        instance.setUserData("");
        instance.setNetworkIDList(Arrays.asList("b5d56487-0851-4725-ad59-26222945192d"));
        instance.setKeyPair("maestrokey");
        instance.setId("f1ce2234-0624-4224-bdec-894911f2a760");

        VirtulizationManagerRequest vimRequest = new VirtulizationManagerRequest();
        vimRequest.setAuthDetails(authenticationDetails);
        vimRequest.setInstance(instance);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity entity = new HttpEntity(vimRequest);
        ResponseEntity<String> responseEntity = restTemplate.exchange(credentialsURI + "/get/instance", HttpMethod.POST, entity, String.class);

        if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {


            JSONObject callbackJSON = null;
            try {
                callbackJSON = new JSONObject(responseEntity.getBody());
                JSONObject returnobject = (JSONObject) callbackJSON.get("returnobsject");
                String privateIP = (String) returnobject.get("privateIP");
                System.out.print(privateIP);
            } catch (JSONException e) {
//                e.printStackTrace();
            }

        }else{
//            System.out.println(responseEntity.getStatusCode());
//            System.out.println(responseEntity.getStatusCodeValue());
        }

    }

}
