package eu.orchestrator.spi.adapter;

import eu.orchestrator.spi.model.*;
import eu.orchestrator.spi.response.SPIResponse;

import java.util.List;

public interface ProviderAdapter {

    SPIResponse validateCredentials(CredentialsModel credentials);

    SPIResponse createFlavor(CredentialsModel credentials, FlavorModel flavor);

    SPIResponse getFlavors(CredentialsModel credentials);

    SPIResponse getImages(CredentialsModel credentials);

    SPIResponse createImage(CredentialsModel credentials, ImageModel image);

    SPIResponse removeImage(CredentialsModel credentials, ImageModel image);

    SPIResponse getInstances(CredentialsModel credentials);

    SPIResponse bootInstance(CredentialsModel credentials, ImageModel image, FlavorModel flavor, InstanceModel instance);

    SPIResponse bootInstanceWithBootableVolume(CredentialsModel credentials, FlavorModel flavor, InstanceModel instance, VolumeModel volumeBootable);

    SPIResponse bootInstanceWithAttachedVolume(CredentialsModel credentials,  ImageModel image, FlavorModel flavor, InstanceModel instance, VolumeModel attachedVolume);

    SPIResponse bootInstanceWithBootableVolumeAndAttachedVolumes(CredentialsModel credentials, FlavorModel flavor, InstanceModel instance, VolumeModel volumeBootable, VolumeModel attachedVolume);

    SPIResponse removeInstance(CredentialsModel credentials, InstanceModel instance);

    SPIResponse getResources(CredentialsModel credentials);

    SPIResponse getTopology(CredentialsModel credentials);

    SPIResponse getVolumes(CredentialsModel credentials);

    SPIResponse createVolumeBootable(CredentialsModel credentials, VolumeModel volume, ImageModel image);

    SPIResponse createVolumeNoBootable(CredentialsModel credentials, VolumeModel volume);

    SPIResponse removeVolume(CredentialsModel credentials, VolumeModel volume);

    SPIResponse attachFloatingIP(CredentialsModel credentials, InstanceModel instanceModel);

    SPIResponse detachFloatingIP(CredentialsModel credentials, InstanceModel instanceModel);
}
