package eu.orchestrator.virtualization.manager.core.rest;

import eu.orchestrator.spi.adapter.ProviderAdapter;
import eu.orchestrator.spi.model.CredentialsModel;
import eu.orchestrator.spi.model.ImageModel;
import eu.orchestrator.spi.model.VolumeModel;
import eu.orchestrator.spi.response.SPIResponse;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorProviderAuthenticationDetails;
import eu.orchestrator.transfer.entities.virtualizationManager.VirtulizationManagerRequest;
import eu.orchestrator.transfer.response.BasicResponseCode;
import eu.orchestrator.transfer.response.RestResponse;
import eu.orchestrator.virtualization.manager.core.configuration.ImageConfig;
import eu.orchestrator.virtualization.manager.core.util.TranslateModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Panagiotis Parthenis
 */
@RestController
@RequestMapping("/api/v1/volume")
public class VolumeRestController {
  private static final Logger logger = Logger.getLogger(VolumeRestController.class.getName());

  @Resource(name = "providerAdapters")
  List providerAdapters;

  @Autowired
  private ImageConfig imageConfig;

  @RequestMapping(value = "/create/bootable", method = RequestMethod.POST)
  public RestResponse createVolumeBootable(@RequestBody VirtulizationManagerRequest vimRequest) {
    ProviderAdapter providerAdapter = (ProviderAdapter) ((List) providerAdapters.stream().filter(adapter -> adapter.getClass().getName().equals(vimRequest.getAuthDetails().getAdapterImplementation())).collect(Collectors.toList())).get(0);
    if (providerAdapter == null) {
      return new RestResponse(BasicResponseCode.EXCEPTION, VolumeRestController.Message.PROVIDER_DOES_NOT_EXIST_ERROR);
    }

    CredentialsModel credentialsModel = TranslateModel.createCredentialModel(vimRequest.getAuthDetails());
    VolumeModel volumeModel = TranslateModel.createVolumeModel(vimRequest.getBootableVolume());

    //TODO get image id from VirtulizationManagerRequest
    ImageModel imageModel = new ImageModel();
    if ((vimRequest.getAuthDetails().getImageID() == null) || (vimRequest.getAuthDetails().getImageID().compareTo("") == 0)) {
      imageModel.setId(imageConfig.getId());
    }else{
      imageModel.setId(vimRequest.getAuthDetails().getImageID());
    }

    SPIResponse response = providerAdapter.createVolumeBootable(credentialsModel, volumeModel, imageModel);

    return new RestResponse(response.getCode(), response.getMessage(), response.getReturnobject());
  }

  @RequestMapping(value = "/create/attached", method = RequestMethod.POST)
  public RestResponse createVolumeAttached(@RequestBody VirtulizationManagerRequest vimRequest) {
    ProviderAdapter providerAdapter = (ProviderAdapter) ((List) providerAdapters.stream().filter(adapter -> adapter.getClass().getName().equals(vimRequest.getAuthDetails().getAdapterImplementation())).collect(Collectors.toList())).get(0);
    if (providerAdapter == null) {
      return new RestResponse(BasicResponseCode.EXCEPTION, VolumeRestController.Message.PROVIDER_DOES_NOT_EXIST_ERROR);
    }

    CredentialsModel credentialsModel = TranslateModel.createCredentialModel(vimRequest.getAuthDetails());
    VolumeModel volumeModel = TranslateModel.createVolumeModel(vimRequest.getAttachedVolume());

    SPIResponse response = providerAdapter.createVolumeNoBootable(credentialsModel, volumeModel);

    return new RestResponse(response.getCode(), response.getMessage(), response.getReturnobject());
  }

  @RequestMapping(value = "/get", method = RequestMethod.POST)
  public RestResponse getVolumes(@RequestBody OrchestratorProviderAuthenticationDetails orchestratorProviderAuthenticationDetails) {
    ProviderAdapter providerAdapter = (ProviderAdapter) ((List) providerAdapters.stream().filter(adapter -> adapter.getClass().getName().equals(orchestratorProviderAuthenticationDetails.getAdapterImplementation())).collect(Collectors.toList())).get(0);
    if (providerAdapter == null) {
      return new RestResponse(BasicResponseCode.EXCEPTION, VolumeRestController.Message.PROVIDER_DOES_NOT_EXIST_ERROR);
    }

    CredentialsModel credentialsModel = TranslateModel.createCredentialModel(orchestratorProviderAuthenticationDetails);

    SPIResponse response = providerAdapter.getVolumes(credentialsModel);
    return new RestResponse(response.getCode(), response.getMessage(), response.getReturnobject());
  }

  @RequestMapping(value = "/remove/bootable", method = RequestMethod.POST)
  public RestResponse removeVolumeBootable(@RequestBody VirtulizationManagerRequest vimRequest) {
    ProviderAdapter providerAdapter = (ProviderAdapter) ((List) providerAdapters.stream().filter(adapter -> adapter.getClass().getName().equals(vimRequest.getAuthDetails().getAdapterImplementation())).collect(Collectors.toList())).get(0);
    if (providerAdapter == null) {
      return new RestResponse(BasicResponseCode.EXCEPTION, VolumeRestController.Message.PROVIDER_DOES_NOT_EXIST_ERROR);
    }

    CredentialsModel credentialsModel = TranslateModel.createCredentialModel(vimRequest.getAuthDetails());
    VolumeModel volumeModel = TranslateModel.createVolumeModel(vimRequest.getBootableVolume());

    SPIResponse response = providerAdapter.removeVolume(credentialsModel, volumeModel);

    return new RestResponse(response.getCode(), response.getMessage(), response.getReturnobject());
  }

  @RequestMapping(value = "/remove/attached", method = RequestMethod.POST)
  public RestResponse removeVolumeAttached(@RequestBody VirtulizationManagerRequest vimRequest) {
    ProviderAdapter providerAdapter = (ProviderAdapter) ((List) providerAdapters.stream().filter(adapter -> adapter.getClass().getName().equals(vimRequest.getAuthDetails().getAdapterImplementation())).collect(Collectors.toList())).get(0);
    if (providerAdapter == null) {
      return new RestResponse(BasicResponseCode.EXCEPTION, VolumeRestController.Message.PROVIDER_DOES_NOT_EXIST_ERROR);
    }

    CredentialsModel credentialsModel = TranslateModel.createCredentialModel(vimRequest.getAuthDetails());
    VolumeModel volumeModel = TranslateModel.createVolumeModel(vimRequest.getAttachedVolume());

    SPIResponse response = providerAdapter.removeVolume(credentialsModel, volumeModel);

    return new RestResponse(response.getCode(), response.getMessage(), response.getReturnobject());
  }

  private final static class Message {

    final static String PROVIDER_DOES_NOT_EXIST_ERROR = "Provider adapter doesn't exist";
  }
}
