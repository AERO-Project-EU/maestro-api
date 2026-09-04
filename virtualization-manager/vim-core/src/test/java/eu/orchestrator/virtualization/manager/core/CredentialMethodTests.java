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
public class CredentialMethodTests {
    private static String credentialsURI = "http://127.0.0.1:8070/api/v1/credential";

    @Test
//    @Ignore
    public void checkOpenStackCredential() {
        OrchestratorProviderAuthenticationDetails authenticationDetails = new OrchestratorProviderAuthenticationDetails();
        authenticationDetails.setId("fakeOScred");
        authenticationDetails.setName("checkOpenStackCredential");
        authenticationDetails.setAdapterImplementation("eu.orchestrator.adapter.openstack.OpenStackAdapter");
        authenticationDetails.setAdapterType("Openstack");
        authenticationDetails.setEndpoint(System.getProperty("openstack.endpoint", ""));
        authenticationDetails.setUsername(System.getProperty("openstack.username", ""));
        authenticationDetails.setPassword(System.getProperty("openstack.password", ""));
        authenticationDetails.setPublicKey("");
        authenticationDetails.setPrivateKey("");
        authenticationDetails.setRegion("");
        authenticationDetails.setDomain("default");
        authenticationDetails.setProject("maestro");
        authenticationDetails.setPublicNetwork("provider");

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity entity = new HttpEntity(authenticationDetails);
        ResponseEntity<String> responseEntity = restTemplate.exchange(credentialsURI+"/validate", HttpMethod.POST, entity, String.class);

        if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {


            JSONObject callbackJSON = null;
            try {
                callbackJSON = new JSONObject(responseEntity.getBody());
            } catch (JSONException e) {
                System.out.println("checkOpenStackCredential FAILED, can't create JSONObject");
                e.printStackTrace();
            }

            try {
                if (callbackJSON.getString("code").equals("SUCCESS")) {
                    System.out.println("checkOpenStackCredential PASSED");
                    return;
                }
            } catch (JSONException e) {
                System.out.println("checkOpenStackCredential FAILED, can't stringify the callbackJSON");
                e.printStackTrace();
            }
        }
        System.out.println("checkOpenStackCredential FAILED");
    }

}
