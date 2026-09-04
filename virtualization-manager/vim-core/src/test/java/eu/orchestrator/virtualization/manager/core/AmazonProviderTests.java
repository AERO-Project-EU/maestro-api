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

import java.io.*;
import java.util.Base64;

/**
 * @author Konstantinos Theodosiou
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {eu.orchestrator.virtualization.manager.core.Application.class})
public class AmazonProviderTests {
    private static String credentialsURI = "http://127.0.0.1:8070/api/v1/credential";
    private static String instanceURI = "http://127.0.0.1:8070/api/v1/instance";


    private OrchestratorProviderAuthenticationDetails getCredentialsObject(){
        OrchestratorProviderAuthenticationDetails authenticationDetails = new OrchestratorProviderAuthenticationDetails();
        authenticationDetails.setId("fakeOScred");
        authenticationDetails.setName("checkAmazonCredential");
        authenticationDetails.setAdapterImplementation("eu.orchestrator.adapter.amazon.AmazonAdapter");
        authenticationDetails.setAdapterType("Amazon");
        authenticationDetails.setPublicKey(System.getProperty("aws.accessKeyId", ""));
        authenticationDetails.setPrivateKey(System.getProperty("aws.secretKey", ""));
        authenticationDetails.setRegion("sa-east-1");

        return authenticationDetails;
    }

    private VirtulizationManagerRequest getSpawnVimRequestObject(Boolean simple){

        String graphId = "CMSApp";
        String graphInstanceId = "konstheo";
        String componentNodeId = "phpMyAdmin";
        String componentNodeInstanceId = "phpMyAdmin";

        OrchestratorProviderAuthenticationDetails authenticationDetails = getCredentialsObject();

        OrchestratorInstance instance = new OrchestratorInstance();
        instance.setName(graphId + "_" + graphInstanceId + "_" + componentNodeId + "_" + componentNodeInstanceId);
        if(simple) {
            instance.setUserData("");
        }else{
            String fileInput = "";
            String current_dir = System.getProperty("user.dir");

            File file = new File(current_dir + "/src/test/java/eu/orchestrator/virtualization/manager/core/test.sh");

            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(file.getAbsoluteFile().toString()));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            try {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    sb.append("\n");
                    line = br.readLine();

                }
                fileInput = sb.toString();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            byte[] encodedBytes = Base64.getEncoder().encode(fileInput.getBytes());
            instance.setUserData(new String(encodedBytes));
        }
        instance.setImageID("ami-01780b2002f0fb3c8");
        instance.setInstanceType("t2.micro");

        VirtulizationManagerRequest vimRequest = new VirtulizationManagerRequest();
        vimRequest.setAuthDetails(authenticationDetails);
        vimRequest.setInstance(instance);
        OrchestratorFlavor flavor = new OrchestratorFlavor();
        flavor.setvCPUs(4);
        flavor.setRam(4096);
        flavor.setStorage(100);
        vimRequest.setFlavor(flavor);

        return vimRequest;
    }

    @Test
    @Ignore
    public void checkAmazonCredential() {
        OrchestratorProviderAuthenticationDetails authenticationDetails = getCredentialsObject();

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity entity = new HttpEntity(authenticationDetails);
        ResponseEntity<String>
                responseEntity = restTemplate.exchange(credentialsURI+"/validate", HttpMethod.POST, entity, String.class);

        if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {


            JSONObject callbackJSON = null;
            try {
                callbackJSON = new JSONObject(responseEntity.getBody());
            } catch (JSONException e) {
                System.out.println("checkAmazonCredential FAILED, can't create JSONObject");
                e.printStackTrace();
            }

            try {
                if (callbackJSON.getString("code").equals("SUCCESS")) {
                    System.out.println("checkAmazonCredential PASSED");
                    return;
                }
            } catch (JSONException e) {
                System.out.println("checkAmazonCredential FAILED, can't stringify the callbackJSON");
                e.printStackTrace();
            }
        }
        System.out.println("checkAmazonCredential FAILED");
    }

    @Test
//    @Ignore
    public void spawnAmazonSimpleInstance() {
        VirtulizationManagerRequest vimRequest = getSpawnVimRequestObject(true);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity entity = new HttpEntity(vimRequest);
        ResponseEntity<String> responseEntity = restTemplate.exchange(instanceURI + "/boot", HttpMethod.POST, entity, String.class);

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
    public void spawnAmazonInstance() {
        VirtulizationManagerRequest vimRequest = getSpawnVimRequestObject(false);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity entity = new HttpEntity(vimRequest);
        ResponseEntity<String> responseEntity = restTemplate.exchange(instanceURI + "/boot", HttpMethod.POST, entity, String.class);

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
    public void checkAmazonRemoveInstance() {
        String graphId = "CMSApp";
        String graphInstanceId = "konstheo";
        String componentNodeId = "phpMyAdmin";
        String componentNodeInstanceId = "phpMyAdmin";

        OrchestratorProviderAuthenticationDetails authenticationDetails = getCredentialsObject();

        OrchestratorInstance instance = new OrchestratorInstance();
        instance.setName(graphId + "_" + graphInstanceId + "_" + componentNodeId + "_" + componentNodeInstanceId);
        instance.setId("i-0c22d358d90f67936");

        VirtulizationManagerRequest vimRequest = new VirtulizationManagerRequest();
        vimRequest.setAuthDetails(authenticationDetails);
        vimRequest.setInstance(instance);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity entity = new HttpEntity(vimRequest);
        ResponseEntity<String> responseEntity = restTemplate.exchange(instanceURI + "/remove", HttpMethod.POST, entity, String.class);

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
    @Ignore
    public void checkAmazonGetInstances() {

    }
}
