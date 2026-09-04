package eu.orchestrator.adapter.gcc;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.inject.Injector;
import eu.orchestrator.spi.model.*;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.Image;
import eu.orchestrator.spi.adapter.ProviderAdapter;
import eu.orchestrator.spi.response.BasicResponseCode;
import eu.orchestrator.spi.response.SPIResponse;
import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.domain.Credentials;
import org.jclouds.googlecloud.GoogleCredentialsFromJson;
import com.google.common.base.Supplier;
import org.jclouds.googlecloud.domain.ListPage;
import org.jclouds.googlecomputeengine.GoogleComputeEngineApi;
import org.jclouds.googlecomputeengine.GoogleComputeEngineProviderMetadata;
import org.jclouds.googlecomputeengine.domain.*;
import org.jclouds.googlecomputeengine.features.InstanceApi;
import org.jclouds.googlecomputeengine.features.OperationApi;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class GCCAdapter implements ProviderAdapter {

    private static final Logger logger = Logger.getLogger(GCCAdapter.class.getName());

    public static void main(String[] args) {

    }

    @Override
    public SPIResponse validateCredentials(CredentialsModel credentials) {

        try {

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("client_email", credentials.getUsername());
            jsonObject.addProperty("private_key", credentials.getPrivateKey());

            Supplier<Credentials> credentialsSupplier = new GoogleCredentialsFromJson(jsonObject.toString());

            ComputeService compute = ContextBuilder.newBuilder("google-compute-engine")
                    .credentialsSupplier(credentialsSupplier)
                    .buildView(ComputeServiceContext.class)
                    .getComputeService();

            if (null != compute) {

                if (null != compute.listImages() && !compute.listImages().isEmpty()) {

                    return new SPIResponse(BasicResponseCode.SUCCESS, "Credentials are valid!");

                }

            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
        }

        return new SPIResponse(BasicResponseCode.EXCEPTION, "Credentials are invalid!");
    }

    @Override
    public SPIResponse createFlavor(CredentialsModel credentials, FlavorModel flavor) {
        return new SPIResponse(BasicResponseCode.EXCEPTION, "This functionality is not implemented yet!");
    }

    @Override
    public SPIResponse getFlavors(CredentialsModel credentials) {
        List<FlavorModel> flavors = new ArrayList<>();

        try {

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("client_email", credentials.getUsername());
            jsonObject.addProperty("private_key", credentials.getPrivateKey());

            Supplier<Credentials> credentialsSupplier = new GoogleCredentialsFromJson(jsonObject.toString());

            ComputeService compute = ContextBuilder.newBuilder("google-compute-engine")
                    .credentialsSupplier(credentialsSupplier)
                    .buildView(ComputeServiceContext.class)
                    .getComputeService();

            if (null != compute) {

                Credentials creds = credentialsSupplier.get();

                ContextBuilder contextBuilder = ContextBuilder.newBuilder(GoogleComputeEngineProviderMetadata.builder().build())
                        .credentials(creds.identity, creds.credential);
                Injector injector = contextBuilder.buildInjector();

                GoogleComputeEngineApi googleApi = injector.getInstance(GoogleComputeEngineApi.class);

                String project_name = googleApi.project().get().name();

                logger.info("Successfully Authenticated to project: " + project_name);

                if (null != googleApi.machineTypesInZone(credentials.getRegion())) {

                    Iterator<ListPage<MachineType>> machineTypePages = googleApi.machineTypesInZone(credentials.getRegion()).list();

                    while (machineTypePages.hasNext()) {

                        ListPage<MachineType> pageOfMachineTypes = machineTypePages.next();
                        pageOfMachineTypes.stream().forEach(machineType -> {

                            if (flavors.stream().filter(flv -> flv.getId().equals(machineType.id())).collect(Collectors.toList()).isEmpty()) {

                                FlavorModel flavorModel = new FlavorModel();

                                flavorModel.setId(machineType.selfLink().toString());
                                flavorModel.setName(machineType.name());
                                flavorModel.setRam(String.valueOf(machineType.memoryMb()));
                                flavorModel.setStorage(String.valueOf(machineType.imageSpaceGb()));
                                flavorModel.setvCPU(String.valueOf(machineType.guestCpus()));

                                flavors.add(flavorModel);

                            }

                        });
                    }

                }

            }

            if (null != flavors && !flavors.isEmpty()) {

                logger.info(new Gson().toJson(flavors));

                return new SPIResponse(BasicResponseCode.SUCCESS, "Flavors have been fetched successfully", flavors);

            } else {
                return new SPIResponse(BasicResponseCode.SUCCESS, "There are no flavors available");
            }


        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
        }

        return new SPIResponse(BasicResponseCode.EXCEPTION, "Error occurred. Please try again!");
    }

    @Override
    public SPIResponse getImages(CredentialsModel credentials) {

        List<ImageModel> images = new ArrayList<>();

        try {

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("client_email", credentials.getUsername());
            jsonObject.addProperty("private_key", credentials.getPrivateKey());

            Supplier<Credentials> credentialsSupplier = new GoogleCredentialsFromJson(jsonObject.toString());

            ComputeService compute = ContextBuilder.newBuilder("google-compute-engine")
                    .credentialsSupplier(credentialsSupplier)
                    .buildView(ComputeServiceContext.class)
                    .getComputeService();

            if (null != compute) {

                if (null != compute.listImages() && !compute.listImages().isEmpty()) {

                    logger.info("Images: " + compute.listImages().size());

                    compute.listImages().stream().forEach(gcImage -> {

                        if (images.stream().filter(img -> ((Image) gcImage).getName().equals(img.getName())).collect(Collectors.toList()).isEmpty()) {

                            ImageModel imageModel = new ImageModel();
                            imageModel.setId(((Image) gcImage).getId());
                            imageModel.setName(((Image) gcImage).getName());
                            imageModel.setKernelId(((Image) gcImage).getOperatingSystem().getName());
                            imageModel.setState(((Image) gcImage).getStatus().name());
                            imageModel.setDescription(((Image) gcImage).getDescription());

                            images.add(imageModel);

                        }
                    });

                }

            }

            if (null != images && !images.isEmpty()) {

                logger.info(new Gson().toJson(images));

                return new SPIResponse(BasicResponseCode.SUCCESS, "Images have been fetched successfully", images);

            } else {
                return new SPIResponse(BasicResponseCode.SUCCESS, "There are no images available");
            }


        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
        }

        return new SPIResponse(BasicResponseCode.EXCEPTION, "Error occurred. Please try again!");
    }

    @Override
    public SPIResponse createImage(CredentialsModel credentials, ImageModel image) {
        return new SPIResponse(BasicResponseCode.EXCEPTION, "This functionality is not implemented yet!");
    }

    @Override
    public SPIResponse removeImage(CredentialsModel credentials, ImageModel image) {
        return new SPIResponse(BasicResponseCode.EXCEPTION, "This functionality is not implemented yet!");
    }

    @Override
    public SPIResponse getInstances(CredentialsModel credentials) {
        List<InstanceModel> instances = new ArrayList<>();

        try {

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("client_email", credentials.getUsername());
            jsonObject.addProperty("private_key", credentials.getPrivateKey());

            Supplier<Credentials> credentialsSupplier = new GoogleCredentialsFromJson(jsonObject.toString());

            ComputeService compute = ContextBuilder.newBuilder("google-compute-engine")
                    .credentialsSupplier(credentialsSupplier)
                    .buildView(ComputeServiceContext.class)
                    .getComputeService();

            if (null != compute) {

                Credentials creds = credentialsSupplier.get();

                ContextBuilder contextBuilder = ContextBuilder.newBuilder(GoogleComputeEngineProviderMetadata.builder().build())
                        .credentials(creds.identity, creds.credential);
                Injector injector = contextBuilder.buildInjector();

                GoogleComputeEngineApi googleApi = injector.getInstance(GoogleComputeEngineApi.class);

                String project_name = googleApi.project().get().name();

                logger.info("Successfully Authenticated to project: " + project_name);

                InstanceApi instanceApi = googleApi.instancesInZone(credentials.getRegion());


                if (null != instanceApi && null != instanceApi.list()) {

                    Iterator<ListPage<Instance>> instancePages = instanceApi.list();

                    while (instancePages.hasNext()) {

                        ListPage<Instance> pageOfInstancess = instancePages.next();
                        pageOfInstancess.stream().forEach(gcInstance -> {

                            if (instances.stream().filter(inst -> gcInstance.name().equals(inst.getName())).collect(Collectors.toList()).isEmpty()) {

                                InstanceModel instanceModel = new InstanceModel();
                                instanceModel.setId(gcInstance.id());
                                instanceModel.setName(gcInstance.name());
                                instanceModel.setImageID(gcInstance.machineType().toString());
                                instanceModel.setState(gcInstance.status().name());

                                instances.add(instanceModel);

                            }

                        });
                    }

                }

            }

            if (null != instances && !instances.isEmpty()) {

                logger.info(new Gson().toJson(instances));

                return new SPIResponse(BasicResponseCode.SUCCESS, "Instances have been fetched successfully", instances);

            } else {
                return new SPIResponse(BasicResponseCode.SUCCESS, "There are no instances available");
            }


        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
        }

        return new SPIResponse(BasicResponseCode.EXCEPTION, "Error occurred. Please try again!");
    }

    @Override
    public SPIResponse bootInstance(CredentialsModel credentials, ImageModel imageModel, FlavorModel flavorModel, InstanceModel instanceModel) {
        try {

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("client_email", credentials.getUsername());
            jsonObject.addProperty("private_key", credentials.getPrivateKey());

            Supplier<Credentials> credentialsSupplier = new GoogleCredentialsFromJson(jsonObject.toString());

            ComputeService compute = ContextBuilder.newBuilder("google-compute-engine")
                    .credentialsSupplier(credentialsSupplier)
                    .buildView(ComputeServiceContext.class)
                    .getComputeService();

            if (null != compute) {

                Credentials creds = credentialsSupplier.get();

                ContextBuilder contextBuilder = ContextBuilder.newBuilder(GoogleComputeEngineProviderMetadata.builder().build())
                        .credentials(creds.identity, creds.credential);
                Injector injector = contextBuilder.buildInjector();

                GoogleComputeEngineApi googleApi = injector.getInstance(GoogleComputeEngineApi.class);

                String project_name = googleApi.project().get().name();

                logger.info("Successfully Authenticated to project: " + project_name);

                InstanceApi instanceApi = googleApi.instancesInZone(credentials.getRegion());

                if (null != instanceApi) {

                    OperationApi operationsApi = googleApi.operations();

                    URI networkURL = googleApi.networks().get("default").selfLink();

                    NewInstance gcInstance = new NewInstance.Builder(
                            instanceModel.getName(), URI.create(flavorModel.getId()), networkURL, null, URI.create(imageModel.getId())
                    ).build();

                    Operation operation = instanceApi.create(gcInstance);

                    int timeout = 60; // seconds
                    int time = 0;

                    while (operation.status() != Operation.Status.DONE) {
                        if (time >= timeout) {
                            return new SPIResponse(BasicResponseCode.EXCEPTION, "Instance has not been booted successfully");
                        }
                        time++;
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        operation = operationsApi.get(operation.selfLink());
                    }

                    instanceModel.setId(operation.targetLink().toString());

                    logger.info("Instance: " + new Gson().toJson(instanceModel));

                    return new SPIResponse(BasicResponseCode.SUCCESS, "Instance has been booted successfully", instanceModel);

                }

            }


        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
        }

        return new SPIResponse(BasicResponseCode.EXCEPTION, "Error occurred. Please try again!");
    }

    @Override
    public SPIResponse bootInstanceWithBootableVolume(CredentialsModel credentials, FlavorModel flavor, InstanceModel instance, VolumeModel volumeBootable) {
      return new SPIResponse(BasicResponseCode.NOT_SUPPORTED, "Error occurred. This method doesn't supported!");
    }

    @Override
    public SPIResponse bootInstanceWithAttachedVolume(CredentialsModel credentials, ImageModel image, FlavorModel flavor, InstanceModel instance, VolumeModel attachedVolume) {
      return new SPIResponse(BasicResponseCode.NOT_SUPPORTED, "Error occurred. This method doesn't supported!");
    }

    @Override
    public SPIResponse bootInstanceWithBootableVolumeAndAttachedVolumes(CredentialsModel credentials, FlavorModel flavor, InstanceModel instance, VolumeModel volumeBootable, VolumeModel attachedVolume) {
      return new SPIResponse(BasicResponseCode.NOT_SUPPORTED, "Error occurred. This method doesn't supported!");
    }

    @Override
    public SPIResponse removeInstance(CredentialsModel credentials, InstanceModel instance) {

        try {

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("client_email", credentials.getUsername());
            jsonObject.addProperty("private_key", credentials.getPrivateKey());

            Supplier<Credentials> credentialsSupplier = new GoogleCredentialsFromJson(jsonObject.toString());

            ComputeService compute = ContextBuilder.newBuilder("google-compute-engine")
                    .credentialsSupplier(credentialsSupplier)
                    .buildView(ComputeServiceContext.class)
                    .getComputeService();

            if (null != compute) {

                Credentials creds = credentialsSupplier.get();

                ContextBuilder contextBuilder = ContextBuilder.newBuilder(GoogleComputeEngineProviderMetadata.builder().build())
                        .credentials(creds.identity, creds.credential);
                Injector injector = contextBuilder.buildInjector();

                GoogleComputeEngineApi googleApi = injector.getInstance(GoogleComputeEngineApi.class);

                String project_name = googleApi.project().get().name();

                logger.info("Successfully Authenticated to project: " + project_name);

                InstanceApi instanceApi = googleApi.instancesInZone(credentials.getRegion());

                if (null != instanceApi) {

                    OperationApi operationsApi = googleApi.operations();
                    Operation operation = instanceApi.delete(instance.getName());

                    int timeout = 60; // seconds
                    int time = 0;

                    while (operation.status() != Operation.Status.DONE) {
                        if (time >= timeout) {
                            return new SPIResponse(BasicResponseCode.EXCEPTION, "Instance has not been removed successfully");
                        }
                        time++;
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        operation = operationsApi.get(operation.selfLink());
                    }

                    return new SPIResponse(BasicResponseCode.SUCCESS, "Instance has been removed successfully");

                }

            }


        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
        }

        return new SPIResponse(BasicResponseCode.EXCEPTION, "Error occurred. Please try again!");
    }

    @Override
    public SPIResponse getResources(CredentialsModel credentials) {
        return new SPIResponse(BasicResponseCode.NOT_SUPPORTED, "Error occurred. Doesn't supported yet!");
    }

    @Override
    public SPIResponse getTopology(CredentialsModel credentials) {
        return new SPIResponse(BasicResponseCode.NOT_SUPPORTED, "Error occurred. This method doesn't supported!");
    }

    @Override
    public SPIResponse getVolumes(CredentialsModel credentials) {
        return new SPIResponse(BasicResponseCode.NOT_SUPPORTED, "Error occurred. This method doesn't supported!");
    }

    @Override
    public SPIResponse createVolumeBootable(CredentialsModel credentials, VolumeModel volume, ImageModel image) {
        return new SPIResponse(BasicResponseCode.NOT_SUPPORTED, "Error occurred. This method doesn't supported!");
    }

    @Override
    public SPIResponse createVolumeNoBootable(CredentialsModel credentials, VolumeModel volume) {
        return new SPIResponse(BasicResponseCode.NOT_SUPPORTED, "Error occurred. This method doesn't supported!");
    }

    @Override
    public SPIResponse removeVolume(CredentialsModel credentials, VolumeModel volume) {
        return new SPIResponse(BasicResponseCode.NOT_SUPPORTED, "Error occurred. This method doesn't supported!");
    }

    @Override
    public SPIResponse attachFloatingIP(CredentialsModel credentials, InstanceModel instanceModel) {
        return new SPIResponse(BasicResponseCode.NOT_SUPPORTED, "Error occurred. This method doesn't supported!");
    }

    @Override
    public SPIResponse detachFloatingIP(CredentialsModel credentials, InstanceModel instanceModel) {
        return new SPIResponse(BasicResponseCode.NOT_SUPPORTED, "Error occurred. This method doesn't supported!");
    }
}
