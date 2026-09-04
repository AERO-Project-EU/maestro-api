package eu.orchestrator.virtualization.manager.core;

import eu.orchestrator.transfer.entities.orchestrator.OrchestratorFlavor;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorInstance;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorProviderAuthenticationDetails;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorVolume;
import eu.orchestrator.transfer.entities.virtualizationManager.VirtulizationManagerRequest;
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
import java.util.logging.Logger;

/**
 * @author Konstantinos Theodosiou
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {Application.class})
public class VolumesRestControllerTests {

  private static String volumeURI = "http://127.0.0.1:8070/api/v1/volume";
  private static String intanceURI = "http://127.0.0.1:8070/api/v1/instance";

  private static final Logger logger = Logger.getLogger(VolumesRestControllerTests.class.getName());

  @Test
//    @Ignore
  public void getVolume() {

    OrchestratorProviderAuthenticationDetails authenticationDetails = new OrchestratorProviderAuthenticationDetails();
    authenticationDetails.setId("");
    authenticationDetails.setName("");
    authenticationDetails.setAdapterImplementation("eu.orchestrator.adapter.openstack.OpenStackAdapter");
    authenticationDetails.setAdapterType("Openstack");
    authenticationDetails.setEndpoint(System.getProperty("openstack.endpoint", ""));
    authenticationDetails.setUsername(System.getProperty("openstack.username", ""));
    authenticationDetails.setPassword(System.getProperty("openstack.password", ""));
    authenticationDetails.setPublicKey("");
    authenticationDetails.setPrivateKey("");
    authenticationDetails.setRegion("");
    authenticationDetails.setDomain("default");
    authenticationDetails.setProject("demo");
    authenticationDetails.setPublicNetwork("provider");

    RestTemplate restTemplate = new RestTemplate();
    HttpEntity entity = new HttpEntity(authenticationDetails);
    ResponseEntity<String> responseEntity = restTemplate.exchange(volumeURI + "/get", HttpMethod.POST, entity, String.class);

    if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {
      logger.info(responseEntity.getBody());
    } else {
      logger.info("empty response");
    }

  }

  @Test
  public void createVolumeBootable() {

    OrchestratorProviderAuthenticationDetails authenticationDetails = new OrchestratorProviderAuthenticationDetails();
    authenticationDetails.setId("");
    authenticationDetails.setName("");
    authenticationDetails.setAdapterImplementation("eu.orchestrator.adapter.openstack.OpenStackAdapter");
    authenticationDetails.setAdapterType("Openstack");
    authenticationDetails.setEndpoint(System.getProperty("openstack.endpoint", ""));
    authenticationDetails.setUsername(System.getProperty("openstack.username", ""));
    authenticationDetails.setPassword(System.getProperty("openstack.password", ""));
    authenticationDetails.setPublicKey("");
    authenticationDetails.setPrivateKey("");
    authenticationDetails.setRegion("");
    authenticationDetails.setDomain("default");
    authenticationDetails.setProject("demo");
    authenticationDetails.setImageID("8528d050-3e6e-4c79-ac00-ae8c9da2dfdd");
    authenticationDetails.setPublicNetwork("provider");

    OrchestratorVolume orchestratorVolume = new OrchestratorVolume();
    orchestratorVolume.setName("baseImageVolumeII");
    orchestratorVolume.setDescription("vim's testing");
    orchestratorVolume.setSize("20");
    orchestratorVolume.setType("lvmdriver-1");

    VirtulizationManagerRequest virtulizationManagerRequest = new VirtulizationManagerRequest();
    virtulizationManagerRequest.setAuthDetails(authenticationDetails);
    virtulizationManagerRequest.setBootableVolume(orchestratorVolume);

    RestTemplate restTemplate = new RestTemplate();
    HttpEntity entity = new HttpEntity(virtulizationManagerRequest);
    ResponseEntity<String> responseEntity = restTemplate.exchange(volumeURI + "/create/bootable", HttpMethod.POST, entity, String.class);

    if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {
      logger.info(responseEntity.getBody());
    } else {
      logger.info("empty response");
    }

  }

  @Test
  public void createVolumeNoBootable() {

    OrchestratorProviderAuthenticationDetails authenticationDetails = new OrchestratorProviderAuthenticationDetails();
    authenticationDetails.setId("");
    authenticationDetails.setName("");
    authenticationDetails.setAdapterImplementation("eu.orchestrator.adapter.openstack.OpenStackAdapter");
    authenticationDetails.setAdapterType("Openstack");
    authenticationDetails.setEndpoint(System.getProperty("openstack.endpoint", ""));
    authenticationDetails.setUsername(System.getProperty("openstack.username", ""));
    authenticationDetails.setPassword(System.getProperty("openstack.password", ""));
    authenticationDetails.setPublicKey("");
    authenticationDetails.setPrivateKey("");
    authenticationDetails.setRegion("");
    authenticationDetails.setDomain("default");
    authenticationDetails.setProject("demo");
    authenticationDetails.setImageID("8528d050-3e6e-4c79-ac00-ae8c9da2dfdd");
    authenticationDetails.setPublicNetwork("provider");

    OrchestratorVolume orchestratorVolume = new OrchestratorVolume();
    orchestratorVolume.setName("customVolume");
    orchestratorVolume.setDescription("vim-test");
    orchestratorVolume.setSize("20");
    orchestratorVolume.setType("lvmdriver-1");

    VirtulizationManagerRequest virtulizationManagerRequest = new VirtulizationManagerRequest();
    virtulizationManagerRequest.setAuthDetails(authenticationDetails);
    virtulizationManagerRequest.setAttachedVolume(orchestratorVolume);

    RestTemplate restTemplate = new RestTemplate();
    HttpEntity entity = new HttpEntity(virtulizationManagerRequest);
    ResponseEntity<String> responseEntity = restTemplate.exchange(volumeURI + "/create/attached", HttpMethod.POST, entity, String.class);

    if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {
      logger.info(responseEntity.getBody());
    } else {
      logger.info("empty response");
    }

  }

  @Test
  public void removeVolume() {

    OrchestratorProviderAuthenticationDetails authenticationDetails = new OrchestratorProviderAuthenticationDetails();
    authenticationDetails.setId("");
    authenticationDetails.setName("");
    authenticationDetails.setAdapterImplementation("eu.orchestrator.adapter.openstack.OpenStackAdapter");
    authenticationDetails.setAdapterType("Openstack");
    authenticationDetails.setEndpoint(System.getProperty("openstack.endpoint", ""));
    authenticationDetails.setUsername(System.getProperty("openstack.username", ""));
    authenticationDetails.setPassword(System.getProperty("openstack.password", ""));
    authenticationDetails.setPublicKey("");
    authenticationDetails.setPrivateKey("");
    authenticationDetails.setRegion("");
    authenticationDetails.setDomain("default");
    authenticationDetails.setProject("demo");
    authenticationDetails.setImageID("8528d050-3e6e-4c79-ac00-ae8c9da2dfdd");
    authenticationDetails.setPublicNetwork("provider");

    OrchestratorVolume orchestratorVolume = new OrchestratorVolume();
    orchestratorVolume.setId("6641b56c-8d61-4a40-8a87-69f25028bb75");

    VirtulizationManagerRequest virtulizationManagerRequest = new VirtulizationManagerRequest();
    virtulizationManagerRequest.setAuthDetails(authenticationDetails);
    virtulizationManagerRequest.setBootableVolume(orchestratorVolume);

    //virtulizationManagerRequest.setBootableVolume(orchestratorVolume);

    RestTemplate restTemplate = new RestTemplate();
    HttpEntity entity = new HttpEntity(virtulizationManagerRequest);
    ResponseEntity<String> responseEntity = restTemplate.exchange(volumeURI + "/remove/bootable", HttpMethod.POST, entity, String.class);

    if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {
      logger.info(responseEntity.getBody());
    } else {
      logger.info("empty response");
    }

  }

  @Test
  public void bootInstanceWithBootableVolume() {

    OrchestratorProviderAuthenticationDetails authenticationDetails = new OrchestratorProviderAuthenticationDetails();
    authenticationDetails.setId("");
    authenticationDetails.setName("");
    authenticationDetails.setAdapterImplementation("eu.orchestrator.adapter.openstack.OpenStackAdapter");
    authenticationDetails.setAdapterType("Openstack");
    authenticationDetails.setEndpoint(System.getProperty("openstack.endpoint", ""));
    authenticationDetails.setUsername(System.getProperty("openstack.username", ""));
    authenticationDetails.setPassword(System.getProperty("openstack.password", ""));
    authenticationDetails.setPublicKey("");
    authenticationDetails.setPrivateKey("");
    authenticationDetails.setRegion("");
    authenticationDetails.setDomain("default");
    authenticationDetails.setProject("demo");
   // authenticationDetails.setImageID("8528d050-3e6e-4c79-ac00-ae8c9da2dfdd");
    authenticationDetails.setPublicNetwork("provider");

    OrchestratorVolume orchestratorVolume = new OrchestratorVolume();
    orchestratorVolume.setId("5891e9d5-8c7b-4a1e-92ac-7f3029388ca1");

    OrchestratorFlavor orchestratorFlavor = new OrchestratorFlavor();
    orchestratorFlavor.setId("e406d456-724e-4b3f-bb6e-d1bc7995a509");

    OrchestratorInstance orchestratorInstance = new OrchestratorInstance();
    orchestratorInstance.setUserData("");
    orchestratorInstance.setName("BootWithVolume");
    orchestratorInstance.setUserData("");
    orchestratorInstance.setKeyPair("maestro");
    orchestratorInstance.setNetworkIDList(Arrays.asList("09486a0a-c992-44d8-b028-f875aefe063b"));

    VirtulizationManagerRequest virtulizationManagerRequest = new VirtulizationManagerRequest();
    virtulizationManagerRequest.setAuthDetails(authenticationDetails);
    virtulizationManagerRequest.setBootableVolume(orchestratorVolume);
    virtulizationManagerRequest.setFlavor(orchestratorFlavor);
    virtulizationManagerRequest.setInstance(orchestratorInstance);

    RestTemplate restTemplate = new RestTemplate();
    HttpEntity entity = new HttpEntity(virtulizationManagerRequest);
    ResponseEntity<String> responseEntity = restTemplate.exchange(intanceURI + "/boot/bootable/volume", HttpMethod.POST, entity, String.class);

    if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {
      logger.info(responseEntity.getBody());
    } else {
      logger.info("empty response");
    }

  }

  @Test
  public void bootInstanceWithAttachedVolume() {

    OrchestratorProviderAuthenticationDetails authenticationDetails = new OrchestratorProviderAuthenticationDetails();
    authenticationDetails.setId("");
    authenticationDetails.setName("");
    authenticationDetails.setAdapterImplementation("eu.orchestrator.adapter.openstack.OpenStackAdapter");
    authenticationDetails.setAdapterType("Openstack");
    authenticationDetails.setEndpoint(System.getProperty("openstack.endpoint", ""));
    authenticationDetails.setUsername(System.getProperty("openstack.username", ""));
    authenticationDetails.setPassword(System.getProperty("openstack.password", ""));
    authenticationDetails.setPublicKey("");
    authenticationDetails.setPrivateKey("");
    authenticationDetails.setRegion("");
    authenticationDetails.setDomain("default");
    authenticationDetails.setProject("demo");
    //authenticationDetails.setImageID("8528d050-3e6e-4c79-ac00-ae8c9da2dfdd");
    authenticationDetails.setPublicNetwork("provider");

    OrchestratorVolume orchestratorVolume = new OrchestratorVolume();
    orchestratorVolume.setId("40bd868c-5358-442e-85ae-4fb4788fccb5");

    OrchestratorFlavor orchestratorFlavor = new OrchestratorFlavor();
    orchestratorFlavor.setId("e406d456-724e-4b3f-bb6e-d1bc7995a509");


    OrchestratorInstance orchestratorInstance = new OrchestratorInstance();
    orchestratorInstance.setUserData("");
    orchestratorInstance.setName("bootWithAttachedVolume");
    orchestratorInstance.setUserData("");
    orchestratorInstance.setKeyPair("maestro");
    orchestratorInstance.setImageID("8528d050-3e6e-4c79-ac00-ae8c9da2dfdd");
    orchestratorInstance.setNetworkIDList(Arrays.asList("09486a0a-c992-44d8-b028-f875aefe063b"));

    VirtulizationManagerRequest virtulizationManagerRequest = new VirtulizationManagerRequest();
    virtulizationManagerRequest.setAuthDetails(authenticationDetails);
    virtulizationManagerRequest.setAttachedVolume(orchestratorVolume);
    virtulizationManagerRequest.setFlavor(orchestratorFlavor);
    virtulizationManagerRequest.setInstance(orchestratorInstance);

    RestTemplate restTemplate = new RestTemplate();
    HttpEntity entity = new HttpEntity(virtulizationManagerRequest);
    ResponseEntity<String> responseEntity = restTemplate.exchange(intanceURI + "/boot/attached/volume", HttpMethod.POST, entity, String.class);

    if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {
      logger.info(responseEntity.getBody());
    } else {
      logger.info("empty response");
    }

  }

  @Test
  public void bootInstanceWithNBootableAndAttacheVolumes() {

    OrchestratorProviderAuthenticationDetails authenticationDetails = new OrchestratorProviderAuthenticationDetails();
    authenticationDetails.setId("");
    authenticationDetails.setName("");
    authenticationDetails.setAdapterImplementation("eu.orchestrator.adapter.openstack.OpenStackAdapter");
    authenticationDetails.setAdapterType("Openstack");
    authenticationDetails.setEndpoint(System.getProperty("openstack.endpoint", ""));
    authenticationDetails.setUsername(System.getProperty("openstack.username", ""));
    authenticationDetails.setPassword(System.getProperty("openstack.password", ""));
    authenticationDetails.setPublicKey("");
    authenticationDetails.setPrivateKey("");
    authenticationDetails.setRegion("");
    authenticationDetails.setDomain("default");
    authenticationDetails.setProject("demo");
    authenticationDetails.setImageID("8528d050-3e6e-4c79-ac00-ae8c9da2dfdd");
    authenticationDetails.setPublicNetwork("provider");

    OrchestratorVolume bootableVolume = new OrchestratorVolume();
    bootableVolume.setId("9e307fd5-1298-4d46-ab0d-8ce4f94bbb8c");

    OrchestratorVolume attachedVolume = new OrchestratorVolume();
    attachedVolume.setId("9b2d92d7-bf2a-42a3-9b28-4a896aa97326");

    OrchestratorFlavor orchestratorFlavor = new OrchestratorFlavor();
    orchestratorFlavor.setId("e406d456-724e-4b3f-bb6e-d1bc7995a509");

    OrchestratorInstance orchestratorInstance = new OrchestratorInstance();
    orchestratorInstance.setUserData("");
    orchestratorInstance.setName("BootWithVolumeANDAttachedVolumes");
    orchestratorInstance.setUserData("");
    orchestratorInstance.setKeyPair("maestro");
    orchestratorInstance.setNetworkIDList(Arrays.asList("09486a0a-c992-44d8-b028-f875aefe063b"));

    VirtulizationManagerRequest virtulizationManagerRequest = new VirtulizationManagerRequest();
    virtulizationManagerRequest.setAuthDetails(authenticationDetails);
    virtulizationManagerRequest.setBootableVolume(bootableVolume);
    virtulizationManagerRequest.setAttachedVolume(attachedVolume);
    virtulizationManagerRequest.setFlavor(orchestratorFlavor);
    virtulizationManagerRequest.setInstance(orchestratorInstance);

    RestTemplate restTemplate = new RestTemplate();
    HttpEntity entity = new HttpEntity(virtulizationManagerRequest);
    ResponseEntity<String> responseEntity = restTemplate.exchange(intanceURI + "/boot/bootable/attached/volume", HttpMethod.POST, entity, String.class);

    if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {
      logger.info(responseEntity.getBody());
    } else {
      logger.info("empty response");
    }

  }
}
