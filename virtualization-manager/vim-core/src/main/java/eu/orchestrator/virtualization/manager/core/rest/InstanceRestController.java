package eu.orchestrator.virtualization.manager.core.rest;

import eu.orchestrator.spi.adapter.ProviderAdapter;
import eu.orchestrator.spi.model.*;
import eu.orchestrator.spi.response.SPIResponse;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorProviderAuthenticationDetails;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorVolume;
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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Konstantinos Theodosiou
 */
@RestController
@RequestMapping("/api/v1/instance")
public class InstanceRestController {
    private static final Logger logger = Logger.getLogger(InstanceRestController.class.getName());

    @Autowired
    private ImageConfig imageConfig;

    @Resource(name = "providerAdapters")
    List providerAdapters;

    @RequestMapping(value = "/boot", method = RequestMethod.POST)
    public RestResponse bootInstance(@RequestBody VirtulizationManagerRequest vimRequest) {
        ProviderAdapter providerAdapter = (ProviderAdapter) ((List) providerAdapters.stream().filter(adapter -> adapter.getClass().getName().equals(vimRequest.getAuthDetails().getAdapterImplementation())).collect(Collectors.toList())).get(0);
        if (providerAdapter == null) {
            return new RestResponse(BasicResponseCode.EXCEPTION, InstanceRestController.Message.PROVIDER_DOES_NOT_EXIST_ERROR);
        }

        CredentialsModel credentialsModel = TranslateModel.createCredentialModel(vimRequest.getAuthDetails());
        InstanceModel instanceModel = TranslateModel.createInstanceModel(vimRequest.getInstance());

        //TODO we need to find the appropriate Flavor and Image in order to create the appropriate models
        FlavorModel flavorModel = TranslateModel.createFlavorModel(vimRequest.getFlavor());
        if(flavorModel.getId()==null || flavorModel.getId().isEmpty()) {
            flavorModel.setId("1124eae1-76b5-44ad-9014-2c388b815077");
        }

        //ImageModel imageModel = TranslateModel.crateImageModel();
        ImageModel imageModel = new ImageModel();
        if(instanceModel.getImageID()==null || instanceModel.getImageID().isEmpty()) {
            imageModel.setId(imageConfig.getId());
        }else{
            imageModel.setId(instanceModel.getImageID());
        }

        SPIResponse response = providerAdapter.bootInstance(credentialsModel, imageModel, flavorModel, instanceModel);

        return new RestResponse(response.getCode(), response.getMessage(), response.getReturnobject());
    }

    @RequestMapping(value = "/boot/bootable/volume", method = RequestMethod.POST)
    public RestResponse bootInstanceWithBootalbeVolume(@RequestBody VirtulizationManagerRequest vimRequest) {
        ProviderAdapter providerAdapter = (ProviderAdapter) ((List) providerAdapters.stream().filter(adapter -> adapter.getClass().getName().equals(vimRequest.getAuthDetails().getAdapterImplementation())).collect(Collectors.toList())).get(0);
        if (providerAdapter == null) {
            return new RestResponse(BasicResponseCode.EXCEPTION, InstanceRestController.Message.PROVIDER_DOES_NOT_EXIST_ERROR);
        }

        CredentialsModel credentialsModel = TranslateModel.createCredentialModel(vimRequest.getAuthDetails());
        InstanceModel instanceModel = TranslateModel.createInstanceModel(vimRequest.getInstance());
        VolumeModel bootableVolume = TranslateModel.createVolumeModel(vimRequest.getBootableVolume());

        //TODO we need to find the appropriate Flavor and Image in order to create the appropriate models
        FlavorModel flavorModel = TranslateModel.createFlavorModel(vimRequest.getFlavor());
        if(flavorModel.getId()==null || flavorModel.getId().isEmpty()) {
            flavorModel.setId("1124eae1-76b5-44ad-9014-2c388b815077");
        }


        SPIResponse response = providerAdapter.bootInstanceWithBootableVolume(credentialsModel, flavorModel, instanceModel, bootableVolume);

        return new RestResponse(response.getCode(), response.getMessage(), response.getReturnobject());
    }

    @RequestMapping(value = "/boot/attached/volume", method = RequestMethod.POST)
    public RestResponse bootInstanceWithAttachedVolume(@RequestBody VirtulizationManagerRequest vimRequest) {
        ProviderAdapter providerAdapter = (ProviderAdapter) ((List) providerAdapters.stream().filter(adapter -> adapter.getClass().getName().equals(vimRequest.getAuthDetails().getAdapterImplementation())).collect(Collectors.toList())).get(0);
        if (providerAdapter == null) {
            return new RestResponse(BasicResponseCode.EXCEPTION, InstanceRestController.Message.PROVIDER_DOES_NOT_EXIST_ERROR);
        }

        CredentialsModel credentialsModel = TranslateModel.createCredentialModel(vimRequest.getAuthDetails());
        InstanceModel instanceModel = TranslateModel.createInstanceModel(vimRequest.getInstance());
        VolumeModel attachedVolume = TranslateModel.createVolumeModel(vimRequest.getAttachedVolume());

        //TODO we need to find the appropriate Flavor and Image in order to create the appropriate models
        FlavorModel flavorModel = TranslateModel.createFlavorModel(vimRequest.getFlavor());
        if(flavorModel.getId()==null || flavorModel.getId().isEmpty()) {
            flavorModel.setId("1124eae1-76b5-44ad-9014-2c388b815077");
        }

        ImageModel imageModel = new ImageModel();
        if(instanceModel.getImageID()==null || instanceModel.getImageID().isEmpty()) {
          imageModel.setId(imageConfig.getId());
        }else{
          imageModel.setId(instanceModel.getImageID());
        }

        SPIResponse response = providerAdapter.bootInstanceWithAttachedVolume(credentialsModel, imageModel, flavorModel, instanceModel, attachedVolume);

        return new RestResponse(response.getCode(), response.getMessage(), response.getReturnobject());
    }

    @RequestMapping(value = "/boot/bootable/attached/volume", method = RequestMethod.POST)
    public RestResponse bootInstanceWithBootableAndAttachedVolume(@RequestBody VirtulizationManagerRequest vimRequest) {
        ProviderAdapter providerAdapter = (ProviderAdapter) ((List) providerAdapters.stream().filter(adapter -> adapter.getClass().getName().equals(vimRequest.getAuthDetails().getAdapterImplementation())).collect(Collectors.toList())).get(0);
        if (providerAdapter == null) {
            return new RestResponse(BasicResponseCode.EXCEPTION, InstanceRestController.Message.PROVIDER_DOES_NOT_EXIST_ERROR);
        }

        CredentialsModel credentialsModel = TranslateModel.createCredentialModel(vimRequest.getAuthDetails());
        InstanceModel instanceModel = TranslateModel.createInstanceModel(vimRequest.getInstance());
        VolumeModel bootableVolume = TranslateModel.createVolumeModel(vimRequest.getBootableVolume());
        VolumeModel attachedVolume = TranslateModel.createVolumeModel(vimRequest.getAttachedVolume());


        //TODO we need to find the appropriate Flavor and Image in order to create the appropriate models
        FlavorModel flavorModel = TranslateModel.createFlavorModel(vimRequest.getFlavor());
        if(flavorModel.getId()==null || flavorModel.getId().isEmpty()) {
            flavorModel.setId("1124eae1-76b5-44ad-9014-2c388b815077");
        }

        SPIResponse response = providerAdapter.bootInstanceWithBootableVolumeAndAttachedVolumes(credentialsModel, flavorModel, instanceModel, bootableVolume, attachedVolume);

        return new RestResponse(response.getCode(), response.getMessage(), response.getReturnobject());
    }

    @RequestMapping(value = "/attach/floating", method = RequestMethod.POST)
    public RestResponse attachFloatingIP(@RequestBody VirtulizationManagerRequest vimRequest) {
        ProviderAdapter providerAdapter = (ProviderAdapter) ((List) providerAdapters.stream().filter(adapter -> adapter.getClass().getName().equals(vimRequest.getAuthDetails().getAdapterImplementation())).collect(Collectors.toList())).get(0);
        if (providerAdapter == null) {
            return new RestResponse(BasicResponseCode.EXCEPTION, InstanceRestController.Message.PROVIDER_DOES_NOT_EXIST_ERROR);
        }

        CredentialsModel credentialsModel = TranslateModel.createCredentialModel(vimRequest.getAuthDetails());
        InstanceModel instanceModel = TranslateModel.createInstanceModel(vimRequest.getInstance());

        SPIResponse attachFloatingIPResponse = providerAdapter.attachFloatingIP(credentialsModel, instanceModel);
        if (attachFloatingIPResponse.getCode().toString().equals(BasicResponseCode.SUCCESS.toString())) {
            InstanceModel attachInstanceModel = (InstanceModel) attachFloatingIPResponse.getReturnobject();

            instanceModel.setFloatingIP(attachInstanceModel.getFloatingIP());
            logger.info("Floating is attached successfully!");
            return new RestResponse(attachFloatingIPResponse.getCode(), attachFloatingIPResponse.getMessage(), instanceModel);
        } else {
            logger.severe("Error couldn't attach the floating properly!");
            return new RestResponse(BasicResponseCode.EXCEPTION, "Error couldn't attach the floating IP properly!", attachFloatingIPResponse.getReturnobject());
        }

    }

    @RequestMapping(value = "/detach/floating", method = RequestMethod.POST)
    public RestResponse detachFloatingIP(@RequestBody VirtulizationManagerRequest vimRequest) {
        ProviderAdapter providerAdapter = (ProviderAdapter) ((List) providerAdapters.stream().filter(adapter -> adapter.getClass().getName().equals(vimRequest.getAuthDetails().getAdapterImplementation())).collect(Collectors.toList())).get(0);
        if (providerAdapter == null) {
            return new RestResponse(BasicResponseCode.EXCEPTION, InstanceRestController.Message.PROVIDER_DOES_NOT_EXIST_ERROR);
        }

        CredentialsModel credentialsModel = TranslateModel.createCredentialModel(vimRequest.getAuthDetails());
        InstanceModel instanceModel = TranslateModel.createInstanceModel(vimRequest.getInstance());

        SPIResponse detachFloatingIPResponse = providerAdapter.detachFloatingIP(credentialsModel, instanceModel);
        if (detachFloatingIPResponse.getCode().toString().equals(BasicResponseCode.SUCCESS.toString())) {
            InstanceModel attachInstanceModel = (InstanceModel) detachFloatingIPResponse.getReturnobject();

            instanceModel.setFloatingIP(attachInstanceModel.getFloatingIP());
            logger.info("Floating is detached successfully!");
            return new RestResponse(detachFloatingIPResponse.getCode(), detachFloatingIPResponse.getMessage(), instanceModel);
        } else {
            logger.severe(detachFloatingIPResponse.getMessage());
            logger.severe("Error couldn't detached the floating properly!");
            return new RestResponse(BasicResponseCode.EXCEPTION, "Error couldn't detach the floating IP properly!", detachFloatingIPResponse.getReturnobject());
        }

    }

    @RequestMapping(value = "/get", method = RequestMethod.POST)
    public RestResponse getInstances(@RequestBody OrchestratorProviderAuthenticationDetails orchestratorProviderAuthenticationDetails) {
        ProviderAdapter providerAdapter = (ProviderAdapter) ((List) providerAdapters.stream().filter(adapter -> adapter.getClass().getName().equals(orchestratorProviderAuthenticationDetails.getAdapterImplementation())).collect(Collectors.toList())).get(0);
        if (providerAdapter == null) {
            return new RestResponse(BasicResponseCode.EXCEPTION, InstanceRestController.Message.PROVIDER_DOES_NOT_EXIST_ERROR);
        }

        CredentialsModel credentialsModel = TranslateModel.createCredentialModel(orchestratorProviderAuthenticationDetails);

        SPIResponse response = providerAdapter.getInstances(credentialsModel);
        return new RestResponse(response.getCode(), response.getMessage(), response.getReturnobject());
    }

    @RequestMapping(value = "/remove", method = RequestMethod.POST)
    public RestResponse removeInstance(@RequestBody VirtulizationManagerRequest vimRequest) {
        ProviderAdapter providerAdapter = (ProviderAdapter) ((List) providerAdapters.stream().filter(adapter -> adapter.getClass().getName().equals(vimRequest.getAuthDetails().getAdapterImplementation())).collect(Collectors.toList())).get(0);

        if (providerAdapter == null) {
            return new RestResponse(BasicResponseCode.EXCEPTION, InstanceRestController.Message.PROVIDER_DOES_NOT_EXIST_ERROR);
        }

        CredentialsModel credentialsModel = TranslateModel.createCredentialModel(vimRequest.getAuthDetails());
        InstanceModel instanceModel = TranslateModel.createInstanceModel(vimRequest.getInstance());

        SPIResponse response = providerAdapter.removeInstance(credentialsModel, instanceModel);
        return new RestResponse(response.getCode(), response.getMessage(), response.getReturnobject());
    }

    @RequestMapping(value = "/get/instance", method = RequestMethod.POST)
    public RestResponse getSingleInstances(@RequestBody VirtulizationManagerRequest vimRequest) {
        ProviderAdapter providerAdapter = (ProviderAdapter) ((List) providerAdapters.stream().filter(adapter -> adapter.getClass().getName().equals(vimRequest.getAuthDetails().getAdapterImplementation())).collect(Collectors.toList())).get(0);
        if (providerAdapter == null) {
            return new RestResponse(BasicResponseCode.EXCEPTION, InstanceRestController.Message.PROVIDER_DOES_NOT_EXIST_ERROR);
        }

        CredentialsModel credentialsModel = TranslateModel.createCredentialModel(vimRequest.getAuthDetails());
        InstanceModel instanceModel = TranslateModel.createInstanceModel(vimRequest.getInstance());

        SPIResponse response = providerAdapter.getInstances(credentialsModel);

        if (response.getCode() == eu.orchestrator.spi.response.BasicResponseCode.SUCCESS) {
            List<InstanceModel> instances = (List<InstanceModel>) response.getReturnobject();
            InstanceModel requestedInstance = null;

            for (InstanceModel instance : instances) {
                if (instance.getId().equals(instanceModel.getId())) {
                    requestedInstance = instance;
                    break;
                }
            }

            if (requestedInstance != null) {
                response.setReturnobject(requestedInstance);
            } else {
                response.setCode(BasicResponseCode.EXCEPTION);
                response.setMessage("Couldn't find the instance with the specified id");
            }
        }

        return new RestResponse(response.getCode(), response.getMessage(), response.getReturnobject());
    }

    private final static class Message {

        final static String PROVIDER_DOES_NOT_EXIST_ERROR = "Provider adapter doesn't exist";
    }
}