package eu.orchestrator.adapter.amazon;

import eu.orchestrator.spi.adapter.ProviderAdapter;
import eu.orchestrator.spi.model.*;
import eu.orchestrator.spi.response.BasicResponseCode;
import eu.orchestrator.spi.response.SPIResponse;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeImagesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeImagesResponse;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.InstanceStateChange;
import software.amazon.awssdk.services.ec2.model.InstanceType;
import software.amazon.awssdk.services.ec2.model.RunInstancesRequest;
import software.amazon.awssdk.services.ec2.model.RunInstancesResponse;
import software.amazon.awssdk.services.ec2.model.TerminateInstancesRequest;
import software.amazon.awssdk.services.ec2.model.TerminateInstancesResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class AmazonAdapter implements ProviderAdapter {

    private static final Logger logger = Logger.getLogger(AmazonAdapter.class.getName());

    public static void main(String[] args) {

    }

    private Ec2Client ec2Client(CredentialsModel credentialsModel, Region region) {
        return Ec2Client.builder()
                .region(region)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(credentialsModel.getPublicKey(), credentialsModel.getPrivateKey())))
                .build();
    }

    @Override
    public SPIResponse validateCredentials(CredentialsModel credentialsModel) {

        try (Ec2Client ec2Client = ec2Client(credentialsModel, Region.of(credentialsModel.getRegion()))) {

            if (null != ec2Client.describePrefixLists()) {
                logger.info("Credentials validated successfully!");
                return new SPIResponse(BasicResponseCode.SUCCESS, "Credentials validated successfully!");
            } else {
                logger.info("Credentials are invalid!");
                return new SPIResponse(BasicResponseCode.EXCEPTION, "Credentials are invalid!");
            }

        } catch (SdkException ex) {
            logger.severe(ex.getMessage());
            return new SPIResponse(BasicResponseCode.EXCEPTION, "Credentials are invalid!");
        }

    }

    @Override
    public SPIResponse createFlavor(CredentialsModel credentials, FlavorModel flavor) {
        return new SPIResponse(BasicResponseCode.EXCEPTION, "This functionality is not implemented yet!");
    }

    @Override
    public SPIResponse getImages(CredentialsModel credentialsModel) {

        List<ImageModel> images = new ArrayList<>();

        try (Ec2Client ec2Client = ec2Client(credentialsModel, Region.EU_WEST_1)) {

            DescribeImagesRequest.Builder requestBuilder = DescribeImagesRequest.builder();
            if (null != credentialsModel.getId() && !credentialsModel.getId().isEmpty()) {
                requestBuilder.owners(credentialsModel.getId());
            }

            DescribeImagesResponse response = ec2Client.describeImages(requestBuilder.build());

            if (response.hasImages()) {

                logger.info("Images: " + response.images().size());

                response.images().forEach(image -> {

                    if (images.stream().noneMatch(awsImg -> awsImg.getId().equals(image.imageId()))) {

                        ImageModel awsImage = new ImageModel();
                        awsImage.setId(image.imageId());
                        awsImage.setName(image.name());
                        awsImage.setImageLocation(image.imageLocation());
                        awsImage.setImageType(image.imageTypeAsString());
                        awsImage.setArchitecture(image.architectureAsString());
                        awsImage.setCreationDate(image.creationDate());
                        awsImage.setPublicValue(image.publicLaunchPermissions());

                        awsImage.setKernelId(image.kernelId());

                        awsImage.setPlatform(image.platformAsString());
                        awsImage.setRamdiskId(image.ramdiskId());

                        awsImage.setState(image.stateAsString());
                        awsImage.setDescription(image.description());
                        awsImage.setEnaSupport(image.enaSupport());
                        awsImage.setHypervisor(image.hypervisorAsString());
                        awsImage.setImageOwnerAlias(image.imageOwnerAlias());

                        awsImage.setRootDeviceName(image.rootDeviceName());
                        awsImage.setRootDeviceType(image.rootDeviceTypeAsString());
                        awsImage.setSriovNetSupport(image.sriovNetSupport());
                        awsImage.setVirtualizationType(image.virtualizationTypeAsString());

                        images.add(awsImage);
                    }
                });
            }

            if (!images.isEmpty()) {
                return new SPIResponse(BasicResponseCode.SUCCESS, "Images have been fetched successfully", images);
            } else {
                return new SPIResponse(BasicResponseCode.SUCCESS, "There are no images available!");
            }

        } catch (SdkException ex) {
            logger.severe(ex.getMessage());
            return new SPIResponse(BasicResponseCode.EXCEPTION, "Error occurred! Please try again!");
        }
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
    public SPIResponse getFlavors(CredentialsModel credentials) {

        List<String> flavors = new ArrayList<>();

        for (InstanceType flavor : InstanceType.values()) {
            if (flavor == InstanceType.UNKNOWN_TO_SDK_VERSION) {
                continue;
            }
            if (!flavors.contains(flavor.toString())) {
                flavors.add(flavor.toString());
                logger.info("Flavor: " + flavor.toString());
            }
        }

        return new SPIResponse(BasicResponseCode.SUCCESS, "Flavors have been fetched successfully!", flavors);

    }

    @Override
    public SPIResponse bootInstance(CredentialsModel credentialsModel, ImageModel image, FlavorModel flavor, InstanceModel instance) {

        try (Ec2Client ec2Client = ec2Client(credentialsModel, Region.of(credentialsModel.getRegion()))) {

            if (null != image && null != flavor) {

                RunInstancesRequest request = RunInstancesRequest.builder()
                        .instanceType(instance.getInstanceType())
                        .imageId(instance.getImageID())
                        .minCount(1)
                        .maxCount(1)
                        .userData(instance.getUserData())
                        .build();

                RunInstancesResponse result = ec2Client.runInstances(request);

                if (result.hasInstances() && !result.instances().isEmpty()) {
                    instance.setId(result.instances().get(0).instanceId());
                    return new SPIResponse(BasicResponseCode.SUCCESS, "Instance has been booted successfully!", instance);
                } else {
                    return new SPIResponse(BasicResponseCode.EXCEPTION, "Instance has not been booted successfully!");
                }
            }

        } catch (SdkException ex) {
            logger.severe(ex.getMessage());
            return new SPIResponse(BasicResponseCode.EXCEPTION, "Error occurred! Please try again!");
        }

        return new SPIResponse(BasicResponseCode.EXCEPTION, "Error occurred! Please try again!");
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
    public SPIResponse getInstances(CredentialsModel credentialsModel) {

        List<InstanceModel> instances = new ArrayList<>();

        try (Ec2Client ec2Client = ec2Client(credentialsModel, Region.EU_WEST_1)) {

            String nextToken = null;

            do {
                DescribeInstancesResponse response = ec2Client.describeInstances(
                        DescribeInstancesRequest.builder().nextToken(nextToken).build());

                response.reservations().forEach(reservation ->
                    reservation.instances().forEach(instance -> {
                        if (instances.stream().noneMatch(awsInst -> awsInst.getId().equals(instance.instanceId()))) {
                            InstanceModel awsInstance = new InstanceModel();
                            awsInstance.setId(instance.instanceId());
                            awsInstance.setImageID(instance.imageId());
                            awsInstance.setInstanceType(instance.instanceTypeAsString());
                            awsInstance.setState(instance.state().nameAsString());
                            awsInstance.setMonitoringState(instance.monitoring().stateAsString());
                            instances.add(awsInstance);
                        }
                    }));

                nextToken = response.nextToken();
            } while (nextToken != null);

            if (!instances.isEmpty()) {
                return new SPIResponse(BasicResponseCode.SUCCESS, "Instances have been fetched successfully", instances);
            } else {
                return new SPIResponse(BasicResponseCode.SUCCESS, "There are no instances available!");
            }

        } catch (SdkException ex) {
            logger.severe(ex.getMessage());
            return new SPIResponse(BasicResponseCode.EXCEPTION, "Error occurred! Please try again!");
        }
    }

    @Override
    public SPIResponse removeInstance(CredentialsModel credentialsModel, InstanceModel instance) {

        try (Ec2Client ec2Client = ec2Client(credentialsModel, Region.of(credentialsModel.getRegion()))) {

            if (null != instance && null != instance.getId() && !instance.getId().isEmpty()) {

                TerminateInstancesRequest request = TerminateInstancesRequest.builder()
                        .instanceIds(instance.getId())
                        .build();

                TerminateInstancesResponse result = ec2Client.terminateInstances(request);

                InstanceStateChange stateChange = result.terminatingInstances().stream()
                        .filter(instanceStateChange -> instanceStateChange.instanceId().equals(instance.getId()))
                        .findFirst().orElse(null);

                if (null != stateChange) {
                    instance.setState(stateChange.currentState().nameAsString());
                    return new SPIResponse(BasicResponseCode.SUCCESS, "Instance has been terminated successfully!", instance);
                } else {
                    return new SPIResponse(BasicResponseCode.EXCEPTION, "Instance has not been terminated successfully!");
                }
            }

        } catch (SdkException ex) {
            logger.severe(ex.getMessage());
            return new SPIResponse(BasicResponseCode.EXCEPTION, "Error occurred! Please try again!");
        }

        return new SPIResponse(BasicResponseCode.EXCEPTION, "Error occurred! Please try again!");
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
