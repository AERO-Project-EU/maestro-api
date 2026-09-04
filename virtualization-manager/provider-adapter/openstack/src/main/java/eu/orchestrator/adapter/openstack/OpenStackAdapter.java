package eu.orchestrator.adapter.openstack;

import com.google.gson.Gson;
import eu.orchestrator.spi.adapter.ProviderAdapter;
import eu.orchestrator.spi.model.*;
import eu.orchestrator.spi.response.BasicResponseCode;
import eu.orchestrator.spi.response.SPIResponse;
import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient;
import org.openstack4j.core.transport.Config;
import org.openstack4j.core.transport.ProxyHost;
import org.openstack4j.model.common.ActionResponse;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.model.common.Payload;
import org.openstack4j.model.common.Payloads;
import org.openstack4j.model.compute.*;
import org.openstack4j.model.compute.builder.BlockDeviceMappingBuilder;
import org.openstack4j.model.image.DiskFormat;
import org.openstack4j.model.image.ContainerFormat;
import org.openstack4j.model.image.builder.ImageBuilder;
import org.openstack4j.model.storage.block.Volume;
import org.openstack4j.openstack.OSFactory;
import org.openstack4j.openstack.compute.domain.NovaAddresses;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;



public class OpenStackAdapter implements ProviderAdapter {

    private static final Logger logger = Logger.getLogger(OpenStackAdapter.class.getName());
    private static DecimalFormat decimalFormat = new DecimalFormat(".##");

    public static void main(String[] args) {

    }

    @Override
    public SPIResponse validateCredentials(CredentialsModel credentials) {
        try {

            OSClient.OSClientV3 os = authenticate(credentials);

            if (null != os) {

                if (null != os.identity().credentials()) {

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

        try {

            OSClient.OSClientV3 os = authenticate(credentials);

            if (null != os) {

                if (null != os.compute().flavors().list() && !os.compute().flavors().list().isEmpty()) {

                    if (os.compute().flavors().list().stream().filter(flv -> ((Flavor) flv).getName().equals(flavor.getName())).collect(Collectors.toList()).isEmpty()) {

                        Flavor newFlavor = Builders.flavor()
                                .name(flavor.getName())
                                .ram(Integer.valueOf(flavor.getRam()))
                                .vcpus(Integer.valueOf(flavor.getvCPU()))
                                .disk(Integer.valueOf(flavor.getStorage()))
                                .build();

                        newFlavor = os.compute().flavors().create(newFlavor);

                        if (null != newFlavor && null != newFlavor.getId() && !newFlavor.getId().isEmpty()) {

                            flavor.setId(newFlavor.getId());

                            logger.info(new Gson().toJson(flavor));

                            return new SPIResponse(BasicResponseCode.SUCCESS, "Flavor has been created successfully", flavor);

                        } else {
                            return new SPIResponse(BasicResponseCode.EXCEPTION, "Flavor has not been created successfully");
                        }

                    }


                }

            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
        }

        return new SPIResponse(BasicResponseCode.EXCEPTION, "Error occurred. Please try again!");

    }

    @Override
    public SPIResponse getFlavors(CredentialsModel credentials) {

        List<FlavorModel> flavors = new ArrayList<>();

        try {

            OSClient.OSClientV3 os = authenticate(credentials);

            if (null != os) {

                if (null != os.compute().flavors().list() && !os.compute().flavors().list().isEmpty()) {

                    logger.info("Flavors: " + os.compute().flavors().list().size());

                    os.compute().flavors().list().stream().forEach(osFlavor -> {

                        if (flavors.stream().filter(flv -> flv.getId().equals(osFlavor.getId())).collect(Collectors.toList()).isEmpty()) {

                            FlavorModel flavorModel = new FlavorModel();
                            flavorModel.setId(((Flavor) osFlavor).getId());
                            flavorModel.setName(((Flavor) osFlavor).getName());
                            flavorModel.setvCPU(String.valueOf(((Flavor) osFlavor).getVcpus()));
                            flavorModel.setStorage(String.valueOf(((Flavor) osFlavor).getDisk()));
                            flavorModel.setRam(String.valueOf(((Flavor) osFlavor).getRam()));

                            flavors.add(flavorModel);
                        }
                    });
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

            OSClient.OSClientV3 os = authenticate(credentials);

            if (null != os) {

                if (null != os.compute().images().list() && !os.compute().images().list().isEmpty()) {

                    logger.info("Images: " + os.compute().images().list().size());

                    os.compute().images().list().stream().forEach(osImage -> {

                        if (images.stream().filter(img -> img.getId().equals(osImage.getId())).collect(Collectors.toList()).isEmpty()) {

                            ImageModel imageModel = new ImageModel();
                            imageModel.setId(((Image) osImage).getId());
                            imageModel.setName(((Image) osImage).getName());
                            imageModel.setState(((Image) osImage).getStatus().name());

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
        try {

            boolean createImage = false;

            OSClient.OSClientV3 os = authenticate(credentials);

            if (null != os && null != image) {


                if (null != os.compute().images().list() && !os.compute().images().list().isEmpty()
                        && os.compute().images().list().stream().filter(img -> ((Image) img).getName().equals(image.getName())).collect(Collectors.toList()).isEmpty()) {
                    createImage = true;
                } else {
                    createImage = true;
                }

                if (createImage) {

                    Payload<URL> payload = Payloads.create(new URL(image.getPayloadURL()));

                    ImageBuilder imageBuilder = Builders.image()
                            .name(image.getName())
                            .isPublic(true)
                            .containerFormat(ContainerFormat.valueOf(image.getContainerFormat()))
                            .diskFormat(DiskFormat.valueOf(image.getDiskFormat()));

                    org.openstack4j.model.image.Image newImage = os.images().create(imageBuilder.build(), payload);

                    TimeUnit.SECONDS.sleep(30);

                    if (null != newImage) {

                        image.setId(newImage.getId());

                        return new SPIResponse(BasicResponseCode.SUCCESS, "Image has been created successfully", image);

                    }

                }

            } else {
                return new SPIResponse(BasicResponseCode.EXCEPTION, "Image has not been created successfully");
            }


        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
        }

        return new SPIResponse(BasicResponseCode.EXCEPTION, "Error occurred. Please try again!");
    }

    @Override
    public SPIResponse removeImage(CredentialsModel credentials, ImageModel image) {
        try {

            OSClient.OSClientV3 os = authenticate(credentials);

            if (null != os && null != image) {

                if (null != os.compute().images().list() && !os.compute().images().list().isEmpty()
                        && !os.compute().images().list().stream().filter(img -> ((Image) img).getName().equals(image.getName())).collect(Collectors.toList()).isEmpty()) {

                    os.images().delete(image.getId());

                    TimeUnit.SECONDS.sleep(30);

                    return new SPIResponse(BasicResponseCode.SUCCESS, "Image has been created successfully", image);


                } else {
                    return new SPIResponse(BasicResponseCode.EXCEPTION, "Image has not been removed successfully");
                }

            }


        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
        }

        return new SPIResponse(BasicResponseCode.EXCEPTION, "Error occurred. Please try again!");
    }

    @Override
    public SPIResponse getInstances(CredentialsModel credentials) {
        List<InstanceModel> instances = new ArrayList<>();

        try {

            OSClient.OSClientV3 os = authenticate(credentials);

            if (null != os) {

                if (null != os.compute().servers().list() && !os.compute().servers().list().isEmpty()) {

                    logger.info("Instances: " + os.compute().servers().list().size());

                    os.compute().servers().list().stream().forEach(osInstance -> {

                        if (instances.stream().filter(inst -> inst.getId().equals(osInstance.getId())).collect(Collectors.toList()).isEmpty()) {

                            InstanceModel instanceModel = new InstanceModel();
                            instanceModel.setId(osInstance.getId());
                            instanceModel.setName(((Server) osInstance).getStatus().name());
                            instanceModel.setState(osInstance.getStatus().name());
                            instanceModel.setImageID(((Server) osInstance).getImageId());
                            instanceModel.setInstanceType(((Server) osInstance).getFlavorId());

                            Addresses addresses = ((Server) osInstance).getAddresses();

                            HashMap<String, String> ipList = new HashMap<>();

                            Map addressesMap = addresses.getAddresses();
                            Iterator iterator = addressesMap.keySet().iterator();

                            //TODO currently we take the first fixed ip
                            // we must to rethink about it
                            while (iterator.hasNext()) {
                                String networkName = (String) iterator.next();
                                List list = (List) addressesMap.get(networkName);
                                Iterator listIterator = list.iterator();

                                while (listIterator.hasNext()) {
                                    NovaAddresses.NovaAddress address = (NovaAddresses.NovaAddress) listIterator.next();

                                    if (address.getType().equals("fixed")) {
                                        ipList.put(networkName, address.getAddr());
                                        if(instanceModel.getPrivateIP()==null || instanceModel.getPrivateIP().isEmpty()) {
                                            instanceModel.setPrivateIP(address.getAddr());
                                            break;
                                        }
                                    }
                                }
                            }

                            instanceModel.setIpList(ipList);
//                            System.out.println(addressesMap);
//                            System.out.println(addressesMap.values());
                            instances.add(instanceModel);

                        }

                    });

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

            boolean createInstance = false;

            OSClient.OSClientV3 os = authenticate(credentials);

            if (null != os) {

                if (null != os.compute().servers().list() && !os.compute().servers().list().isEmpty()
                        && os.compute().servers().list().stream().filter(inst -> ((Server) inst).getName().equals(instanceModel.getName())).collect(Collectors.toList()).isEmpty()) {
                    createInstance = true;
                } else {
                    createInstance = true;
                }

                if (createInstance) {
                    ServerCreate sc = Builders.server().name(instanceModel.getName())
                            .flavor(flavorModel.getId())
                            .image(imageModel.getId())
                            .networks(instanceModel.getNetworkIDList())
                            .userData(instanceModel.getUserData())
                            .keypairName(instanceModel.getKeyPairName())
                            .build();

                    if (null != sc) {

                        Server server = os.compute().servers().boot(sc);

//                        TimeUnit.SECONDS.sleep(30);

                        instanceModel.setId(server.getId());
                        instanceModel.setState("Active");

                        logger.info(new Gson().toJson(instanceModel));

                        return new SPIResponse(BasicResponseCode.SUCCESS, "Instance has been booted successfully", instanceModel);

                    } else {
                        return new SPIResponse(BasicResponseCode.EXCEPTION, "Instance has not been booted successfully");
                    }
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
        try {

            boolean createInstance = false;

            OSClient.OSClientV3 os = authenticate(credentials);

            if (null != os) {

                if (null != os.compute().servers().list() && !os.compute().servers().list().isEmpty()
                    && os.compute().servers().list().stream().filter(inst -> ((Server) inst).getName().equals(instance.getName())).collect(Collectors.toList()).isEmpty()) {
                    createInstance = true;
                } else {
                    createInstance = true;
                }

                if (createInstance) {

                    BlockDeviceMappingBuilder blockDeviceMappingBuilder = Builders.blockDeviceMapping()
                        .uuid(volumeBootable.getId())
                        .deviceName("/dev/vda")
                        .bootIndex(0);

                    ServerCreate sc = Builders.server().name(instance.getName())
                        .flavor(flavor.getId())
                        .networks(instance.getNetworkIDList())
                        .userData(instance.getUserData())
                        .keypairName(instance.getKeyPairName())
                        .blockDevice(blockDeviceMappingBuilder.build())
                        .build();

                    if (null != sc) {

                        Server server = os.compute().servers().boot(sc);

                        TimeUnit.SECONDS.sleep(30);

                        instance.setId(server.getId());
                        instance.setState("Active");

                        logger.info(new Gson().toJson(instance));

                        return new SPIResponse(BasicResponseCode.SUCCESS, "Instance has been booted successfully", instance);

                    } else {
                        return new SPIResponse(BasicResponseCode.EXCEPTION, "Instance has not been booted successfully");
                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
        }

        return new SPIResponse(BasicResponseCode.EXCEPTION, "Error occurred. Please try again!");
    }

    @Override
    public SPIResponse bootInstanceWithAttachedVolume(CredentialsModel credentials, ImageModel imageModel, FlavorModel flavor, InstanceModel instance, VolumeModel attachedVolume) {
        return new SPIResponse(BasicResponseCode.NOT_SUPPORTED, "Error occurred. This method doesn't supported!");
    }

    @Override
    public SPIResponse bootInstanceWithBootableVolumeAndAttachedVolumes(CredentialsModel credentials, FlavorModel flavor, InstanceModel instance, VolumeModel volumeBootable, VolumeModel attachedVolume) {
        try {

            boolean createInstance = false;

            OSClient.OSClientV3 os = authenticate(credentials);

            if (null != os) {

                if (null != os.compute().servers().list() && !os.compute().servers().list().isEmpty()
                    && os.compute().servers().list().stream().filter(inst -> ((Server) inst).getName().equals(instance.getName())).collect(Collectors.toList()).isEmpty()) {
                    createInstance = true;
                } else {
                    createInstance = true;
                }

                if (createInstance) {

                    BlockDeviceMappingBuilder blockDeviceMappingBuilder = Builders.blockDeviceMapping()
                        .uuid(volumeBootable.getId())
                        .deviceName("/dev/vda")
                        .bootIndex(0);

                    BlockDeviceMappingBuilder blockDeviceMappingBuilder2 = Builders.blockDeviceMapping()
                        .uuid(attachedVolume.getId())
                        .deviceName("/dev/vdb");


                    ServerCreate sc =  Builders.server().name(instance.getName())
                        .flavor(flavor.getId())
                        .networks(instance.getNetworkIDList())
                        .userData(instance.getUserData())
                        .keypairName(instance.getKeyPairName())
                        .blockDevice(blockDeviceMappingBuilder.build())
                        .blockDevice(blockDeviceMappingBuilder2.build())
                        .build();

                    if (null != sc) {

                        Server server = os.compute().servers().boot(sc);

                        TimeUnit.SECONDS.sleep(30);

                        instance.setId(server.getId());
                        instance.setState("Active");

                        logger.info(new Gson().toJson(instance));

                        return new SPIResponse(BasicResponseCode.SUCCESS, "Instance has been booted successfully", instance);

                    } else {
                        return new SPIResponse(BasicResponseCode.EXCEPTION, "Instance has not been booted successfully");
                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
        }

        return new SPIResponse(BasicResponseCode.EXCEPTION, "Error occurred. Please try again!");
    }


    @Override
    public SPIResponse removeInstance(CredentialsModel credentials, InstanceModel instanceModel) {
        try {

            OSClient.OSClientV3 os = authenticate(credentials);

            if (null != os) {

                if (null != os.compute().servers().list() && !os.compute().servers().list().isEmpty()) {

                    if (!os.compute().servers().list().stream().filter(inst -> ((Server) inst).getId().equals(instanceModel.getId())).collect(Collectors.toList()).isEmpty()) {

                        Server server = os.compute().servers().get(instanceModel.getId());

                        if (null != server) {

                            ActionResponse response = os.compute().servers().delete(server.getId());

                            if (null != response && response.getCode() == 200) {

                                return new SPIResponse(BasicResponseCode.SUCCESS, "Instance has been removed successfully");

                            } else {
                                return new SPIResponse(BasicResponseCode.EXCEPTION, "Instance has not been removed successfully");
                            }

                        } else {
                            return new SPIResponse(BasicResponseCode.EXCEPTION, "Instance has not been removed successfully");
                        }

                    }

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
        try {

            OSClient.OSClientV3 os = authenticate(credentials);

            if (null != os) {

                if (os.compute() != null) {
                    if (os.compute().floatingIps() != null && os.compute().floatingIps().list() != null) {
                        List<? extends FloatingIP> floatingIPS = os.compute().floatingIps().list();
                        logger.info("Number of Floating IPs: " + floatingIPS.size());

                        Integer usedFloatingIPs = 0;
                        if (!floatingIPS.isEmpty()) {
                            for (FloatingIP floatingIP : floatingIPS) {
                                if (floatingIP.getInstanceId() != null && !floatingIP.getInstanceId().isEmpty()) {
                                    usedFloatingIPs++;
                                }
                            }
                        }

                        if (os.compute().quotaSets() != null && os.compute().quotaSets().limits() != null &&
                                os.compute().quotaSets().limits().getAbsolute() != null) {

                            AbsoluteLimit absoluteLimit = os.compute().quotaSets().limits().getAbsolute();
                            logger.info("Quota: " + absoluteLimit);

                            ResourcesModel resourcesModel = new ResourcesModel();
                            resourcesModel.setId(credentials.getId());

                            resourcesModel.setUsedFloatingIPs(usedFloatingIPs);
                            resourcesModel.setClaimedFloatingIPs(floatingIPS.size());
                            if (!floatingIPS.isEmpty()) {
                                resourcesModel.setFloatingIPsUtilization(
                                        ((usedFloatingIPs * 1.0) / floatingIPS.size()) * 100);
                            } else {
                                resourcesModel.setFloatingIPsUtilization(100.0);
                            }

                            resourcesModel.setUsedVCpus(absoluteLimit.getTotalCoresUsed());
                            resourcesModel.setMaxVCpus(absoluteLimit.getMaxTotalCores());

                            double cpuUtilization = (Double.valueOf(absoluteLimit.getTotalCoresUsed()) / absoluteLimit.getMaxTotalCores()) * 100;

                            resourcesModel.setvCpuUtilization(Double.valueOf(decimalFormat.format(cpuUtilization)));

                            resourcesModel.setUsedRam(absoluteLimit.getTotalRAMUsed());
                            resourcesModel.setMaxRam(absoluteLimit.getMaxTotalRAMSize());

                            double ramUtilization = (Double.valueOf(absoluteLimit.getTotalRAMUsed()) / absoluteLimit.getMaxTotalRAMSize()) * 100;

                            resourcesModel.setRamUtilization(Double.valueOf(decimalFormat.format(ramUtilization)));

                            resourcesModel.setRunningInstances(absoluteLimit.getTotalInstancesUsed());
                            resourcesModel.setMaxInstances(absoluteLimit.getMaxTotalInstances());

                            double instancesUtilization = (Double.valueOf(absoluteLimit.getTotalInstancesUsed()) / absoluteLimit.getMaxTotalInstances()) * 100;

                            resourcesModel.setInstancesUtilization(Double.valueOf(decimalFormat.format(instancesUtilization)));

                            return new SPIResponse(BasicResponseCode.SUCCESS, "Resources have been gotten successfully!",
                                    resourcesModel);
                        } else {
                            logger.log(Level.SEVERE, "QuotaSets are null");
                            return new SPIResponse(BasicResponseCode.EXCEPTION, "Error occurred. Please try again!");
                        }
                    } else {
                        logger.log(Level.SEVERE, "Floating IPs are null");
                        return new SPIResponse(BasicResponseCode.EXCEPTION, "Error occurred. Please try again!");
                    }
                }
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
        }
        return new SPIResponse(BasicResponseCode.EXCEPTION, "Error occurred. Please try again!");
    }

    @Override
    public SPIResponse getTopology(CredentialsModel credentials) {
        return new SPIResponse(BasicResponseCode.NOT_SUPPORTED, "Error occurred. This method doesn't supported!");
    }

    @Override
    public SPIResponse getVolumes(CredentialsModel credentials) {

        List<VolumeModel> volumes = new ArrayList<>();
        try {

            OSClient.OSClientV3 os = authenticate(credentials);

            if (null != os) {

                os.blockStorage().volumes().list().stream().forEach(volume->{

                    VolumeModel volumeModel = new VolumeModel();
                    volumeModel.setId(((Volume) volume).getId());
                    volumeModel.setName(((Volume) volume).getName());
                    volumeModel.setDescription(((Volume) volume).getDescription());
                    volumeModel.setSize(((Volume) volume).getSize()+"");

                    volumes.add(volumeModel);

                });
            }

            if (null != volumes && !volumes.isEmpty()) {
                logger.info(new Gson().toJson(volumes));
                return new SPIResponse(BasicResponseCode.SUCCESS, "Volumes have been fetched successfully", volumes);
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
    public SPIResponse createVolumeBootable(CredentialsModel credentials, VolumeModel volume, ImageModel image) {

        try {

            OSClient.OSClientV3 os = authenticate(credentials);

            if (null != os) {

                Volume volumeBuilder = os.blockStorage().volumes()
                        .create(Builders.volume()
                                .name(volume.getName())
                                .description(volume.getDescription())
                                .size(Integer.valueOf(volume.getSize()))
                                .imageRef(image.getId())
                                .volumeType(volume.getType())
                                .bootable(true)
                                .build()
                        );

                TimeUnit.SECONDS.sleep(30);

                if(volumeBuilder!=null){
                    volume.setId(volumeBuilder.getId());
                    return new SPIResponse(BasicResponseCode.SUCCESS, "Volume has been created successfully", volume);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
        }

        return new SPIResponse(BasicResponseCode.EXCEPTION, "Error occurred. Please try again!");
    }

    @Override
    public SPIResponse createVolumeNoBootable(CredentialsModel credentials, VolumeModel volume) {

        try {

            OSClient.OSClientV3 os = authenticate(credentials);

            if (null != os) {

                Volume volumeBuilder = os.blockStorage().volumes()
                    .create(Builders.volume()
                        .name(volume.getName())
                        .description(volume.getDescription())
                        .size(Integer.valueOf(volume.getSize()))
                        .volumeType(volume.getType())
                        .build()
                    );

                TimeUnit.SECONDS.sleep(30);

                if(volumeBuilder!=null){
                    volume.setId(volumeBuilder.getId());
                    return new SPIResponse(BasicResponseCode.SUCCESS, "Volume has been created successfully", volume);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
        }

        return new SPIResponse(BasicResponseCode.EXCEPTION, "Error occurred. Please try again!");
    }

    @Override
    public SPIResponse removeVolume(CredentialsModel credentials, VolumeModel volume) {

        try {

            OSClient.OSClientV3 os = authenticate(credentials);

            if (null != os && null != volume) {
                os.blockStorage().volumes().delete(volume.getId());
                TimeUnit.SECONDS.sleep(30);
                return new SPIResponse(BasicResponseCode.SUCCESS, "Volume has been deleted successfully", volume);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
        }

        return new SPIResponse(BasicResponseCode.EXCEPTION, "Error occurred. Please try again!");
    }

    @Override
    public SPIResponse attachFloatingIP(CredentialsModel credentials, InstanceModel instanceModel) {
        try {

            OSClient.OSClientV3 os = authenticate(credentials);

            if (null != os) {
                List<? extends FloatingIP> floatingIPList = os.compute().floatingIps().list();
                FloatingIP availableFloating = null;
                for(FloatingIP ip : floatingIPList){
                    if(ip.getInstanceId()==null || ip.getInstanceId().isEmpty()) {
                        availableFloating = ip;
                        break;
                    }
                }

                if(availableFloating == null){
                    FloatingIP floatingIP = os.compute().floatingIps().allocateIP(instanceModel.getFloatingPool());
                    availableFloating = floatingIP;
                }

                if(availableFloating != null){
                    Server server = os.compute().servers().get(instanceModel.getId());

                    ActionResponse response = os.compute().floatingIps().addFloatingIP(server,availableFloating.getFloatingIpAddress());

                    if (null != response && response.getCode() == 200) {
                        instanceModel.setFloatingIP(availableFloating.getFloatingIpAddress());

                        return new SPIResponse(BasicResponseCode.SUCCESS, "Floating IP attached!", instanceModel);

                    } else {
                        return new SPIResponse(BasicResponseCode.EXCEPTION, "Error occurred. Couldn't attach the floating IP!");
                    }
                }else {

                    return new SPIResponse(BasicResponseCode.EXCEPTION, "Error occurred. Currently there are any available floatings IPs!");
                }
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
        }

        return new SPIResponse(BasicResponseCode.EXCEPTION, "Error occurred. Please try again!");
    }

    @Override
    public SPIResponse detachFloatingIP(CredentialsModel credentials, InstanceModel instanceModel) {
        try {

            OSClient.OSClientV3 os = authenticate(credentials);

            if (null != os) {

                if(instanceModel.getFloatingIP() != null && !instanceModel.getFloatingIP().isEmpty()){
                    Server server = os.compute().servers().get(instanceModel.getId());

                    ActionResponse response = os.compute().floatingIps().removeFloatingIP(server,instanceModel.getFloatingIP());
                    if (null != response && response.getCode() == 200) {
                        List<? extends FloatingIP> floatingIPList = os.compute().floatingIps().list();
                        FloatingIP floatingIPToDeallocate = null;
                        for(FloatingIP floatingIP: floatingIPList){
                            if(((FloatingIP) floatingIP).getFloatingIpAddress().equals(instanceModel.getFloatingIP())){
                                floatingIPToDeallocate = floatingIP;
                                break;
                            }

                        }
                        if(null != floatingIPToDeallocate){
                            os.compute().floatingIps().deallocateIP(floatingIPToDeallocate.getId());
                            return new SPIResponse(BasicResponseCode.SUCCESS, "Floating IP detached!", instanceModel);
                        }

                        return new SPIResponse(BasicResponseCode.EXCEPTION, "Error occurred. Couldn't deallocate the floating IP!");
                    } else {
                        return new SPIResponse(BasicResponseCode.EXCEPTION, "Error occurred. Couldn't detach the floating IP!");
                    }
                }else {
                    return new SPIResponse(BasicResponseCode.EXCEPTION, "Error occurred. The instance hasn't floating IP!");
                }
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
        }

        return new SPIResponse(BasicResponseCode.EXCEPTION, "Error occurred. Please try again!");
    }

    public OSClient.OSClientV3 authenticate(CredentialsModel credentials){
        OSClient.OSClientV3 os = null;
        try {
            boolean withSslVerification = true;
            if(credentials.getEndpoint().contains("http://") || credentials.getEndpoint().contains("192.168")
                    || credentials.getEndpoint().contains("10.") || credentials.getEndpoint().contains("172")){
                withSslVerification = false;
            }

            Config config = withSslVerification
                            ? Config.newConfig()
                            : Config.newConfig().withSSLVerificationDisabled();


            if(credentials.getProxyURL() == null || credentials.getProxyURL().isEmpty()) {


                os = OSFactory.builderV3()
                        .endpoint(credentials.getEndpoint())
                        .withConfig(config)
                        .credentials(credentials.getUsername(), credentials.getPassword(),
                                Identifier.byName(credentials.getDomain()))
                        .scopeToProject(Identifier.byName(credentials.getProject()),
                                Identifier.byName(credentials.getDomain()))
                        .authenticate();
            }else{
                os = OSFactory.builderV3()
                        .endpoint(credentials.getEndpoint())
                        .withConfig(config)
                        .credentials(credentials.getUsername(), credentials.getPassword(),
                                Identifier.byName(credentials.getDomain()))
                        .withConfig(Config.newConfig().withProxy(ProxyHost.of(credentials.getProxyURL(), credentials.getProxyPort())))
                        .scopeToProject(Identifier.byName(credentials.getProject()),
                                Identifier.byName(credentials.getDomain()))
                        .authenticate();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
        }

        return os;
    }
}
