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
public class ProviderRestControllerTests {

    private static String providerResourcesURI = "http://127.0.0.1:8070/api/v1/provider";

    @Test
//    @Ignore
    public void checkGetProviderResources(){
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


        RestTemplate restTemplate = new RestTemplate();
        HttpEntity entity = new HttpEntity(authenticationDetails);
        ResponseEntity<String>
                responseEntity = restTemplate.exchange(providerResourcesURI + "/resources", HttpMethod.POST, entity, String.class);

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
}
