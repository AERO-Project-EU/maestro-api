package eu.orchestrator.virtualization.manager.core;

import eu.orchestrator.transfer.entities.orchestrator.OrchestratorFlavor;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorProviderAuthenticationDetails;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
public class FlavorRestControllerTests {

    private OrchestratorProviderAuthenticationDetails getCredentialsObject(){
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

        return authenticationDetails;
    }

    @Test
//    @Ignore
    public void getFlavorID(){
        String spawnInstanceURI = "http://localhost:8070"  + "/api/v1/flavor/get";
        OrchestratorProviderAuthenticationDetails authenticationDetails = getCredentialsObject();
        OrchestratorFlavor flavor  = new OrchestratorFlavor();
        flavor.setRam(4097);
        flavor.setStorage(20);
        flavor.setvCPUs(8);

        OrchestratorFlavor currflavor  = new OrchestratorFlavor();
        Boolean firstCheck = true;


        RestTemplate restTemplate = new RestTemplate();
        HttpEntity entity = new HttpEntity(authenticationDetails);
        ResponseEntity<String> responseEntity = restTemplate.exchange(spawnInstanceURI, HttpMethod.POST, entity, String.class);

        if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {

//            logger.debug("Response: " + responseEntity.getBody());

            JSONObject callbackJSON = null;
            try {
                callbackJSON = new JSONObject(responseEntity.getBody());
                if (callbackJSON.getString("code").equals("SUCCESS")) {

                    JSONArray returnobject = (JSONArray) callbackJSON.get("returnobject");
                    long size = returnobject.length();
                    for(int i = 0; i<size; i++){
                        JSONObject object = returnobject.getJSONObject(i);
                        Integer vCPU = Integer.parseInt(object.get("vCPU").toString());
                        Integer ram = Integer.parseInt(object.get("ram").toString());
                        Integer storage = Integer.parseInt(object.get("storage").toString());

                        if (vCPU >= flavor.getvCPUs() && ram >= flavor.getRam() && storage >= flavor.getStorage()) {
                            if (!firstCheck) {
                                if (vCPU <= currflavor.getvCPUs() && ram <= currflavor.getRam() && storage <= currflavor.getStorage()) {
                                    currflavor.setvCPUs(vCPU);
                                    currflavor.setRam(ram);
                                    currflavor.setStorage(storage);
                                    currflavor.setId(object.get("id").toString());
                                    firstCheck = false;
                                }

                            } else {
                                    currflavor.setvCPUs(vCPU);
                                    currflavor.setRam(ram);
                                    currflavor.setStorage(storage);
                                    currflavor.setId(object.get("id").toString());
                                    firstCheck = false;
                                }
                            }
                        }
                    }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }



        System.out.println(currflavor.getId());

    }
}
