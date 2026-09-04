package eu.orchestrator.backend.util;

import eu.orchestrator.common.util.NullCheckUtil;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.Component;
import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.repository.domain.Device;
import eu.orchestrator.repository.domain.EnvironmentalVariableInstance;
import eu.orchestrator.repository.domain.FlavorInstance;
import eu.orchestrator.repository.domain.GraphLinkNodeInstance;
import eu.orchestrator.repository.domain.Interface.TransmissionProtocol;
import eu.orchestrator.repository.domain.InterfaceInstance;
import eu.orchestrator.repository.domain.VolumeInstance;
import eu.orchestrator.transfer.entities.rainbowk8s.Affinity;
import eu.orchestrator.transfer.entities.rainbowk8s.ApiVersionKindPair;
import eu.orchestrator.transfer.entities.rainbowk8s.Container;
import eu.orchestrator.transfer.entities.rainbowk8s.ContainerPortSection;
import eu.orchestrator.transfer.entities.rainbowk8s.CpuInfo;
import eu.orchestrator.transfer.entities.rainbowk8s.Disjunct;
import eu.orchestrator.transfer.entities.rainbowk8s.DisjunctList;
import eu.orchestrator.transfer.entities.rainbowk8s.DnsConfig;
import eu.orchestrator.transfer.entities.rainbowk8s.ExposedPort;
import eu.orchestrator.transfer.entities.rainbowk8s.HostPath;
import eu.orchestrator.transfer.entities.rainbowk8s.ImagePullSecret;
import eu.orchestrator.transfer.entities.rainbowk8s.KeyOperatorPair;
import eu.orchestrator.transfer.entities.rainbowk8s.Limits;
import eu.orchestrator.transfer.entities.rainbowk8s.Link;
import eu.orchestrator.transfer.entities.rainbowk8s.NameMountPathPair;
import eu.orchestrator.transfer.entities.rainbowk8s.NameValuePair;
import eu.orchestrator.transfer.entities.rainbowk8s.Node;
import eu.orchestrator.transfer.entities.rainbowk8s.NodeAffinity;
import eu.orchestrator.transfer.entities.rainbowk8s.NodeHardware;
import eu.orchestrator.transfer.entities.rainbowk8s.NodeSelectorTerms;
import eu.orchestrator.transfer.entities.rainbowk8s.PortSection;
import eu.orchestrator.transfer.entities.rainbowk8s.Replicas;
import eu.orchestrator.transfer.entities.rainbowk8s.RequiredDuringSchedulingIgnoredDuringExecution;
import eu.orchestrator.transfer.entities.rainbowk8s.Resources;
import eu.orchestrator.transfer.entities.rainbowk8s.SecurityContext;
import eu.orchestrator.transfer.entities.rainbowk8s.ServiceGraph;
import eu.orchestrator.transfer.entities.rainbowk8s.SloTo;
import eu.orchestrator.transfer.entities.rainbowk8s.SloConfig;
import eu.orchestrator.transfer.entities.rainbowk8s.SloTargetState;
import eu.orchestrator.transfer.entities.rainbowk8s.Spec;
import eu.orchestrator.transfer.entities.rainbowk8s.StabilizationWindow;
import eu.orchestrator.transfer.entities.rainbowk8s.Volume;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

public class RainbowUtil {

    private static final Logger logger = LoggerFactory.getLogger(RainbowUtil.class.getName());

    private static final String METADATA_NAME = "name";
    private static final String METADATA_NAMESPACE = "namespace";
    private static final String RAINBOW_API_VERSION = "fogapps.k8s.rainbow-h2020.eu/v1";
    private static final String KUBERNETES_APP_NAME = "app.kubernetes.io/name";
    private static final String KUBERNETES_APP_VERSION = "app.kubernetes.io/version";
    private static final String KUBERNETES_APP_COMPONENT = "app.kubernetes.io/component";
    private static final String KUBERNETES_APP_INSTANCE = "app.kubernetes.io/instance";
    private static final String NONE_CAPS = "NONE";
    private static final String NONE = "None";
    private static final String CORE = "CORE";
    private static final String RAINBOW_PORT_TYPE_CLUSTER_INTERNAL = "ClusterInternal";
    private static final String RAINBOW_PORT_TYPE_NODE_EXTERNAL = "NodeExternal";
    private static final String KUBERNETES_SERVICE_SUFFIX = ".svc.cluster.local";
    private static final String DEVICE_CAMERA_KEY = "/dev/video0";
    private static final String DEVICE_NVIDIA = "/opt/dev/nvidia0";
    private static final String EXISTS_OPERATOR = "Exists";

    private static List<String> componentNames = new ArrayList<>();
    private static String NAMESPACE_GLOBAL = null;

    public static String nodeNameProvider(ComponentNodeInstance componentNodeInstance) {

        String name = null;
        if (NullCheckUtil.isNotEmpty(componentNodeInstance.getApplicationInstance())
                && NullCheckUtil.isNotEmpty(componentNodeInstance.getApplicationInstance().getName())
                && NullCheckUtil.isNotEmpty(componentNodeInstance.getName())
                && NullCheckUtil.isNotEmpty(componentNodeInstance.getHexID())) {
            name = componentNodeInstance.getApplicationInstance().getName().toLowerCase(Locale.ROOT)
                    + "-" + componentNodeInstance.getName().toLowerCase(Locale.ROOT)
                    + "-" + componentNodeInstance.getHexID().toLowerCase(Locale.ROOT);
        }

        return name;
    }

    public static String serviceGraphToYaml(ServiceGraph serviceGraph) {

        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        om.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        String tempSg = null;
        try {
            tempSg = om.writeValueAsString(serviceGraph);
            om.writeValue(new File("/tmp/serviceg.yaml"), serviceGraph);
        } catch (Exception e) {
            //logger.error("YAML writer error: ", e);
        }

        return tempSg;
    }

    /*
     * Application instance to Service Graph convertion functions
     * */
    public static ServiceGraph applicationInstanceToServiceGraph(ApplicationInstance applicationInstance, Boolean networkModeHost) {

        componentNames = new ArrayList<>();
        String name = KubernetesUtil.namespaceProvider(applicationInstance);
        NAMESPACE_GLOBAL = name;

        Map<String, String> metadata = new HashMap<>();
        if (NullCheckUtil.isNotEmpty(name)) {
            metadata.put(METADATA_NAME, name);
            metadata.put(METADATA_NAMESPACE, name);
        }

        ServiceGraph.Builder serviceGraphBuilder = new ServiceGraph.Builder();

        serviceGraphBuilder.withApiVersion(RAINBOW_API_VERSION);

        if (NullCheckUtil.isNotEmpty(metadata)) {
            serviceGraphBuilder.withMetadata(metadata);
        }

        serviceGraphBuilder.withSpec(applicationToSpec(applicationInstance, networkModeHost));

        return serviceGraphBuilder.build();
    }

    public static Spec applicationToSpec(ApplicationInstance applicationInstance, Boolean networkModeHost) {

        Spec.Builder specBuilder = new Spec.Builder();

        if (NullCheckUtil.isNotEmpty(applicationInstance.getComponentNodeInstances())) {
            specBuilder.withNodes(componentNodeInstancesToNodes(applicationInstance.getComponentNodeInstances(), networkModeHost));
        }

        if (NullCheckUtil.isNotEmpty(applicationInstance.getGraphLinkNodeInstances())) {
            specBuilder.withLinks(graphLinkNodeInstanceToLinks(applicationInstance.getGraphLinkNodeInstances()));
        }

        if (NullCheckUtil.isNotEmpty(applicationInstance.getProvider())
                && NullCheckUtil.isNotEmpty(applicationInstance.getProvider().getEndpoint())) {

            String nameserver = applicationInstance.getProvider().getEndpoint();
            String tmp = nameserver.substring(nameserver.indexOf("[") + 1);
            nameserver = tmp.trim();
            nameserver = nameserver.substring(0, nameserver.indexOf("]"));

            List<String> nameservers = new ArrayList<>();
            nameservers.add(nameserver);
            DnsConfig dnsConfig = new DnsConfig.Builder()
                    .withDnsPolicy(NONE)
                    .withNameservers(nameservers)
                    .build();
            specBuilder.withDnsConfig(dnsConfig);
        }

        return specBuilder.build();
    }

    public static List<Node> componentNodeInstancesToNodes(SortedSet<ComponentNodeInstance> componentNodeInstances, Boolean networkModeHost) {

        List<Node> nodes = new ArrayList<>();

        componentNodeInstances.forEach(x -> componentNames.add(nodeNameProvider(x)));
        componentNodeInstances.forEach(x -> nodes.add(componentNodeInstanceToNode(x, networkModeHost)));

        return nodes;
    }

    public static Node componentNodeInstanceToNode(ComponentNodeInstance componentNodeInstance, Boolean networkModeHost) {

        //TODO containers - add multiple support
        List<Container> containerList = new ArrayList<>();
        containerList.add(componentNodeInstanceToContainer(componentNodeInstance));

        String name = nodeNameProvider(componentNodeInstance);

        Map<String, String> labels = new HashMap<>();
        if (NullCheckUtil.isNotEmpty(componentNodeInstance.getApplicationInstance())
                && NullCheckUtil.isNotEmpty(componentNodeInstance.getApplicationInstance().getName())
                && NullCheckUtil.isNotEmpty(componentNodeInstance.getApplicationInstance().getHexID())) {

            labels.put(KUBERNETES_APP_NAME, componentNodeInstance.getApplicationInstance().getName().toLowerCase(Locale.ROOT)
                    + "-" + componentNodeInstance.getApplicationInstance().getHexID().toLowerCase(Locale.ROOT));
        }
        labels.put(KUBERNETES_APP_VERSION, "0.0.1");
        if (NullCheckUtil.isNotEmpty(name)) {
            labels.put(KUBERNETES_APP_COMPONENT, name);
            labels.put(KUBERNETES_APP_INSTANCE, name);
        }

        Node.Builder nodeBuilder = new Node.Builder();

        if (NullCheckUtil.isNotEmpty(name)) {
            nodeBuilder.withName(name);
        }

        if (NullCheckUtil.isNotEmpty(labels)) {
            nodeBuilder.withLabels(labels);
        }

        //TODO REVIEW Affinity
        if (NullCheckUtil.isNotEmpty(componentNodeInstance.getComponentNode())
                && NullCheckUtil.isNotEmpty(componentNodeInstance.getComponentNode().getComponent())
                && NullCheckUtil.isNotEmpty(componentNodeInstance.getComponentNode().getComponent().getDevices())) {

            List<Device> devicesList = componentNodeInstance.getComponentNode().getComponent().getDevices();
            for (Device dc : devicesList) {
                if (dc.getKey().equalsIgnoreCase(DEVICE_CAMERA_KEY)
                        || dc.getKey().equalsIgnoreCase(DEVICE_NVIDIA)) {
                    String tmpKey = dc.getKey();
                    tmpKey = tmpKey.replaceFirst("/", "");
                    tmpKey = tmpKey.replace("/", ".");
                    Set<KeyOperatorPair> keyOperatorPairSet = new HashSet<>();
                    KeyOperatorPair keyOperatorPair = new KeyOperatorPair();
                    keyOperatorPair.setKey(tmpKey);
                    keyOperatorPair.setOperator(EXISTS_OPERATOR);
                    keyOperatorPairSet.add(keyOperatorPair);

                    NodeSelectorTerms nodeSelectorTerms = new NodeSelectorTerms();
                    nodeSelectorTerms.setMatchExpressions(keyOperatorPairSet);

                    RequiredDuringSchedulingIgnoredDuringExecution requiredDuringSchedulingIgnoredDuringExecution =
                            new RequiredDuringSchedulingIgnoredDuringExecution();
                    requiredDuringSchedulingIgnoredDuringExecution.getNodeSelectorTerms().add(nodeSelectorTerms);

                    NodeAffinity nodeAffinity = new NodeAffinity();
                    nodeAffinity.setRequiredDuringSchedulingIgnoredDuringExecution(requiredDuringSchedulingIgnoredDuringExecution);

                    Affinity affinity = new Affinity();
                    affinity.setNodeAffinity(nodeAffinity);

                    nodeBuilder.withAffinity(affinity);

                }
            }
        }

        if (NullCheckUtil.isNotEmpty(containerList)) {
            nodeBuilder.withContainers(containerList);
        }

        //TODO move to container
        if (NullCheckUtil.isNotEmpty(componentNodeInstance.getComponentNode())
                && NullCheckUtil.isNotEmpty(componentNodeInstance.getComponentNode().getComponent())) {
            Component component = componentNodeInstance.getComponentNode().getComponent();
            if (NullCheckUtil.isNotEmpty(component.getDockerRegistry())
                    && NullCheckUtil.isNotEmpty(component.getDockerImage())) {

                List<ImagePullSecret> imagePullSecretList = new ArrayList<>();
                ImagePullSecret imagePullSecret = new ImagePullSecret.Builder()
                        .withName(nodeNameProvider(componentNodeInstance) + "-secret").build();
                imagePullSecretList.add(imagePullSecret);
                nodeBuilder.withImagePullSecrets(imagePullSecretList);
            }
        }

        //TODO volumes
        //.withVolumes()

        //TODO REVIEW volumes for device
        if (NullCheckUtil.isNotEmpty(componentNodeInstance.getComponentNode())
                && NullCheckUtil.isNotEmpty(componentNodeInstance.getComponentNode().getComponent())
                && NullCheckUtil.isNotEmpty(componentNodeInstance.getComponentNode().getComponent().getDevices())) {
            List<Device> devicesList = componentNodeInstance.getComponentNode().getComponent().getDevices();
            for (Device dc : devicesList) {
                if (dc.getKey().equalsIgnoreCase(DEVICE_CAMERA_KEY)
                        || dc.getKey().equalsIgnoreCase(DEVICE_NVIDIA)) {
                    String tmpName = dc.getKey();
                    tmpName = tmpName.replaceFirst("/", "");
                    tmpName = tmpName.replace("/", "-");
                    List<Volume> volumes = new ArrayList<>();
                    HostPath hostPath = new HostPath();
                    hostPath.setPath(dc.getKey());
                    Volume volume = new Volume();
                    volume.setName(tmpName);
                    volume.setHostPath(hostPath);
                    volumes.add(volume);
                    nodeBuilder.withVolumes(volumes);
                }
            }
        }

        nodeBuilder.withReplicas(componentInstancesWorkersToReplicas(componentNodeInstance));

        if (NullCheckUtil.isNotEmpty(componentNodeInstance.getInterfaceInstances())) {
            nodeBuilder.withExposedPorts(interfaceInstanceToExposedPorts(componentNodeInstance.getInterfaceInstances()));
        }

        if (Boolean.TRUE.equals(networkModeHost)) {
            nodeBuilder.withHostNetwork(Boolean.TRUE);
        } else {
            nodeBuilder.withHostNetwork(componentNodeInstance.getComponentNode().getComponent().getNetworkModeHost());
        }

        if (NullCheckUtil.isNotEmpty(componentNodeInstance.getComponentNode())
                && NullCheckUtil.isNotEmpty(componentNodeInstance.getComponentNode().getComponent())
                && NullCheckUtil.isNotEmpty(componentNodeInstance.getComponentNode().getComponent().getArchitecture())) {

            List<String> architectures = new ArrayList<>();
            architectures.add(componentNodeInstance.getComponentNode().getComponent().getArchitecture());

            CpuInfo cpuInfo = new CpuInfo.Builder().withArchitectures(architectures).build();
            NodeHardware nodeHardware = new NodeHardware.Builder().withCpuInfo(cpuInfo).build();
            nodeBuilder.withNodeHardware(nodeHardware);
        }

        return nodeBuilder.build();
    }

    public static Container componentNodeInstanceToContainer(ComponentNodeInstance componentNodeInstance) {

        Container.Builder containerBuilder = new Container.Builder();

        if (NullCheckUtil.isNotEmpty(componentNodeInstance.getApplicationInstance())
                && NullCheckUtil.isNotEmpty(componentNodeInstance.getName())
                && NullCheckUtil.isNotEmpty(componentNodeInstance.getHexID())) {

            containerBuilder.withName(componentNodeInstance.getApplicationInstance().getName().toLowerCase(Locale.ROOT)
                    + "-" + componentNodeInstance.getName().toLowerCase(Locale.ROOT)
                    + "-" + componentNodeInstance.getHexID().toLowerCase(Locale.ROOT));

        }

        if (NullCheckUtil.isNotEmpty(componentNodeInstance.getComponentNode())
                && NullCheckUtil.isNotEmpty(componentNodeInstance.getComponentNode().getComponent())) {
            Component component = componentNodeInstance.getComponentNode().getComponent();
            if (NullCheckUtil.isNotEmpty(component.getDockerRegistry())
                    && NullCheckUtil.isNotEmpty(component.getDockerImage())
                //TODO FIX BOOLEAN
                //&& Boolean.TRUE.equals(component.getDockerCustomRegistry())
            ) {
                containerBuilder.withImage(component.getDockerRegistry() + "/" + component.getDockerImage());
            } else if (NullCheckUtil.isNotEmpty(component.getDockerImage())
                //TODO FIX BOOLEAN
                //&& Boolean.FALSE.equals(component.getDockerCustomRegistry())
            ) {
                containerBuilder.withImage(component.getDockerImage());
            }
        }

        if (NullCheckUtil.isNotEmpty(componentNodeInstance.getFlavorInstance())) {
            containerBuilder.withResources(flavorInstanceToResources(componentNodeInstance.getFlavorInstance()));
        }

        if (NullCheckUtil.isNotEmpty(componentNodeInstance.getEnvironmentalVariableInstances())) {
            containerBuilder.withEnv(environmentalVariableInstancesToEnv(componentNodeInstance.getEnvironmentalVariableInstances()));
        }

        //TODO volumes
//        if(NullCheckUtil.isNotEmpty(componentNodeInstance.getVolumeInstances())) {
//            containerBuilder.withVolumeMounts(volumeInstancesToVolumes(componentNodeInstance.getVolumeInstances()));
//        }

        //TODO REVIEW: we add volumeMountHereFor the Devices
        if (NullCheckUtil.isNotEmpty(componentNodeInstance.getComponentNode())
                && NullCheckUtil.isNotEmpty(componentNodeInstance.getComponentNode().getComponent())
                && NullCheckUtil.isNotEmpty(componentNodeInstance.getComponentNode().getComponent().getDevices())) {

            List<Device> devicesList = componentNodeInstance.getComponentNode().getComponent().getDevices();
            for (Device dc : devicesList) {
                if (dc.getKey().equalsIgnoreCase(DEVICE_CAMERA_KEY)
                        || dc.getKey().equalsIgnoreCase(DEVICE_NVIDIA)) {
                    String tmpName = dc.getKey();
                    tmpName = tmpName.replaceFirst("/", "");
                    tmpName = tmpName.replace("/", "-");
                    Set<NameMountPathPair> nameMountPathPairSet = new HashSet<>();
                    NameMountPathPair nameMountPathPair = new NameMountPathPair();
                    nameMountPathPair.setMountPath(dc.getKey());
                    nameMountPathPair.setName(tmpName);
                    nameMountPathPairSet.add(nameMountPathPair);
                    containerBuilder.withVolumeMounts(nameMountPathPairSet);
                }
            }
        }

        if (NullCheckUtil.isNotEmpty(componentNodeInstance.getComponentNode())
                && NullCheckUtil.isNotEmpty(componentNodeInstance.getComponentNode().getComponent())
                && NullCheckUtil.isNotEmpty(componentNodeInstance.getComponentNode().getComponent().getPrivilege())) {
            SecurityContext securityContext = new SecurityContext.Builder()
                    .withPrivileged(componentNodeInstance.getComponentNode().getComponent().getPrivilege()).build();
            containerBuilder.withSecurityContext(securityContext);
        }

        if (NullCheckUtil.isNotEmpty(componentNodeInstance.getInterfaceInstances())) {
            List<InterfaceInstance> interfaceInstances = componentNodeInstance.getInterfaceInstances();
            List<ContainerPortSection> portList = new ArrayList<>();
            for (InterfaceInstance ii : interfaceInstances) {
                if (NullCheckUtil.isNotEmpty(ii.getName()) && NullCheckUtil.isNotEmpty(ii.getPort()) && NullCheckUtil.isNotEmpty(ii.getInterfaceObj())) {
                    if (NullCheckUtil.isNotEmpty(ii.getInterfaceObj().getTransmissionProtocol())) {
                        String transmissionProtocol = ii.getInterfaceObj().getTransmissionProtocol();
                        if (TransmissionProtocol.BOTH.getFriendlyName().equalsIgnoreCase(transmissionProtocol)) {
                            String name = ii.getName().toLowerCase(Locale.ROOT);
                            name = name.substring(0, Math.min(name.length(), 10)) + ii.getInterfaceInstanceID();
                            portList.add(createContainerPortSection(name + "t",
                                    TransmissionProtocol.TCP.getFriendlyName(), Integer.parseInt(ii.getPort())));
                            portList.add(createContainerPortSection(name + "u",
                                    TransmissionProtocol.UDP.getFriendlyName(), Integer.parseInt(ii.getPort())));
                        } else {
                            String name = ii.getName().toLowerCase(Locale.ROOT);
                            name = name.substring(0, Math.min(name.length(), 10)) + ii.getInterfaceInstanceID();
                            portList.add(
                                    createContainerPortSection(name, ii.getInterfaceObj().getTransmissionProtocol(), Integer.parseInt(ii.getPort())));
                        }
                    } else {
                        //fallback to default
                        String name = ii.getName().toLowerCase(Locale.ROOT);
                        name = name.substring(0, Math.min(name.length(), 10)) + ii.getInterfaceInstanceID();
                        portList.add(createContainerPortSection(name, TransmissionProtocol.TCP.getFriendlyName(), Integer.parseInt(ii.getPort())));
                    }
                }
            }
            containerBuilder.withPorts(portList);
        }

        return containerBuilder.build();
    }

    private static ContainerPortSection createContainerPortSection(String name, String transmissionProtocol, int port) {
        ContainerPortSection.Builder containerPortSectionBuilder = new ContainerPortSection.Builder();
        containerPortSectionBuilder.withName(name);
        containerPortSectionBuilder.withProtocol(transmissionProtocol);
        containerPortSectionBuilder.withContainerPort(port);
        return containerPortSectionBuilder.build();
    }

    public static Resources flavorInstanceToResources(FlavorInstance flavorInstance) {

        Limits.Builder limitsBuilder = new Limits.Builder();

        if (NullCheckUtil.isNotEmpty(flavorInstance.getvCPUs())) {
            limitsBuilder.withCpu(flavorInstance.getvCPUs() * 1000 + "m");
        }

        if (NullCheckUtil.isNotEmpty(flavorInstance.getRam())) {
            limitsBuilder.withMemory(flavorInstance.getRam() + "Mi");
        }

        Limits limits = limitsBuilder.build();

        Resources.Builder resourcesBuilder = new Resources.Builder();

        if (NullCheckUtil.isNotEmpty(limits)) {
            resourcesBuilder.withLimits(limits);
        }

        return resourcesBuilder.build();

    }

    public static Set<NameValuePair> environmentalVariableInstancesToEnv(List<EnvironmentalVariableInstance> environmentalVariableInstances) {

        Set<NameValuePair> nameValuePairSet = new HashSet<>();

        for (EnvironmentalVariableInstance env : environmentalVariableInstances) {
            if (env.getValue().startsWith("@")) {
                String value = env.getValue().replaceFirst("@", "").toLowerCase(Locale.ROOT);
                for (String nm : componentNames) {
                    if (nm.contains(value)) {
                        nm = nm + "." + NAMESPACE_GLOBAL + KUBERNETES_SERVICE_SUFFIX;
                        nameValuePairSet.add(new NameValuePair(env.getKey(), nm));
                    }
                }
            } else {
                nameValuePairSet.add(new NameValuePair(env.getKey(), env.getValue()));
            }

        }

        return nameValuePairSet;
    }

    public static Set<NameMountPathPair> volumeInstancesToVolumes(List<VolumeInstance> volumeInstances) {

        Set<NameMountPathPair> nameMountPathPairSet = new HashSet<>();

        volumeInstances.forEach(x -> nameMountPathPairSet.add(new NameMountPathPair(x.getVolumeInstanceID().toString(), x.getDockerPath())));

        return nameMountPathPairSet;
    }

    public static Replicas componentInstancesWorkersToReplicas(ComponentNodeInstance componentNodeInstance) {

        Replicas.Builder replicasBuilder = new Replicas.Builder();

        if (NullCheckUtil.isNotEmpty(componentNodeInstance.getComponentNode())
                && NullCheckUtil.isNotEmpty(componentNodeInstance.getComponentNode().getComponent())
                && (NullCheckUtil.isEmpty(componentNodeInstance.getComponentNode().getComponent().getElasticityController())
                || componentNodeInstance.getComponentNode().getComponent().getElasticityController().equalsIgnoreCase(NONE_CAPS))) {
            replicasBuilder.withMin(1);
            replicasBuilder.withMax(1);
            return replicasBuilder.build();
        }

        if (NullCheckUtil.isNotEmpty(componentNodeInstance.getMinimumWorkers())) {
            replicasBuilder.withMin(componentNodeInstance.getMinimumWorkers());
        } else {
            replicasBuilder.withMin(1);
        }

        if (NullCheckUtil.isNotEmpty(componentNodeInstance.getMaximumWorkers())) {
            replicasBuilder.withMax(componentNodeInstance.getMaximumWorkers());
        } else {
            replicasBuilder.withMax(1);
        }

        return replicasBuilder.build();
    }

    public static ExposedPort interfaceInstanceToExposedPorts(List<InterfaceInstance> interfaceInstances) {

        ExposedPort.Builder exposedPortBuilder = new ExposedPort.Builder();

        boolean portType = Boolean.FALSE;
        List<PortSection> portList = new ArrayList<>();
        for (InterfaceInstance ii : interfaceInstances) {
            if (NullCheckUtil.isNotEmpty(ii.getName()) && NullCheckUtil.isNotEmpty(ii.getPort()) && NullCheckUtil.isNotEmpty(ii.getInterfaceObj())) {
                if (NullCheckUtil.isNotEmpty(ii.getInterfaceObj().getTransmissionProtocol())) {
                    String transmissionProtocol = ii.getInterfaceObj().getTransmissionProtocol();
                    if (TransmissionProtocol.BOTH.getFriendlyName().equalsIgnoreCase(transmissionProtocol)) {
                        String name = ii.getName().toLowerCase(Locale.ROOT);
                        name = name.substring(0, Math.min(name.length(), 10)) + ii.getInterfaceInstanceID();
                        portList.add(createPortSection(name + "t", TransmissionProtocol.TCP.getFriendlyName(), Integer.parseInt(ii.getPort())));
                        portList.add(createPortSection(name + "u", TransmissionProtocol.UDP.getFriendlyName(), Integer.parseInt(ii.getPort())));
                    } else {
                        String name = ii.getName().toLowerCase(Locale.ROOT);
                        name = name.substring(0, Math.min(name.length(), 10)) + ii.getInterfaceInstanceID();
                        portList.add(createPortSection(name, ii.getInterfaceObj().getTransmissionProtocol(), Integer.parseInt(ii.getPort())));
                    }
                } else {
                    //fallback to default
                    String name = ii.getName().toLowerCase(Locale.ROOT);
                    name = name.substring(0, Math.min(name.length(), 10)) + ii.getInterfaceInstanceID();
                    portList.add(createPortSection(name, TransmissionProtocol.TCP.getFriendlyName(), Integer.parseInt(ii.getPort())));
                }
            }

            if (CORE.equalsIgnoreCase(ii.getInterfaceType())) {
                portType = Boolean.TRUE;
            }
        }

        if (portType) {
            exposedPortBuilder.withType(RAINBOW_PORT_TYPE_CLUSTER_INTERNAL);
        } else {
            exposedPortBuilder.withType(RAINBOW_PORT_TYPE_NODE_EXTERNAL);
        }

        exposedPortBuilder.withPorts(portList);

        return exposedPortBuilder.build();
    }

    private static PortSection createPortSection(String name, String transmissionProtocol, int port) {
        PortSection.Builder portsBuilder = new PortSection.Builder();
        portsBuilder.withName(name);
        portsBuilder.withProtocol(transmissionProtocol);
        portsBuilder.withPort(port);
        return portsBuilder.build();
    }

    public static List<Link> graphLinkNodeInstanceToLinks(List<GraphLinkNodeInstance> graphLinkNodeInstances) {

        List<Link> links = new ArrayList<>();

        graphLinkNodeInstances.forEach(x -> links.add(graphLinkNodeInstanceToLink(x)));

        return links;
    }

    public static Link graphLinkNodeInstanceToLink(GraphLinkNodeInstance graphLinkNodeInstance) {

        Link.Builder linkBuilder = new Link.Builder();

        if (NullCheckUtil.isNotEmpty(graphLinkNodeInstance.getComponentNodeInstanceFrom())
                && NullCheckUtil.isNotEmpty(graphLinkNodeInstance.getComponentNodeInstanceFrom().getApplicationInstance())
                && NullCheckUtil.isNotEmpty(graphLinkNodeInstance.getComponentNodeInstanceFrom().getApplicationInstance().getName())
                && NullCheckUtil.isNotEmpty(graphLinkNodeInstance.getComponentNodeInstanceFrom().getName())
                && NullCheckUtil.isNotEmpty(graphLinkNodeInstance.getComponentNodeInstanceFrom().getHexID())) {
            linkBuilder.withSource(graphLinkNodeInstance.getComponentNodeInstanceFrom().getApplicationInstance().getName().toLowerCase(Locale.ROOT)
                    + "-" + graphLinkNodeInstance.getComponentNodeInstanceFrom().getName().toLowerCase(Locale.ROOT)
                    + "-" + graphLinkNodeInstance.getComponentNodeInstanceFrom().getHexID().toLowerCase(Locale.ROOT));
        }

        if (NullCheckUtil.isNotEmpty(graphLinkNodeInstance.getComponentNodeInstanceTo())
                && NullCheckUtil.isNotEmpty(graphLinkNodeInstance.getComponentNodeInstanceFrom().getApplicationInstance())
                && NullCheckUtil.isNotEmpty(graphLinkNodeInstance.getComponentNodeInstanceFrom().getApplicationInstance().getName())
                && NullCheckUtil.isNotEmpty(graphLinkNodeInstance.getComponentNodeInstanceFrom().getName())
                && NullCheckUtil.isNotEmpty(graphLinkNodeInstance.getComponentNodeInstanceFrom().getHexID())) {
            linkBuilder.withTarget(graphLinkNodeInstance.getComponentNodeInstanceTo().getApplicationInstance().getName().toLowerCase(Locale.ROOT)
                    + "-" + graphLinkNodeInstance.getComponentNodeInstanceTo().getName().toLowerCase(Locale.ROOT)
                    + "-" + graphLinkNodeInstance.getComponentNodeInstanceTo().getHexID().toLowerCase(Locale.ROOT));
        }

        //TODO
        //.withQosRequirements()

        return linkBuilder.build();
    }

    public static ServiceGraph k8sCrdToServiceGraph(Map<String, Object> crd) {
        try {
            ServiceGraph serviceGraph = new ServiceGraph();

            String apiVersion = (String) crd.get("apiVersion");
            serviceGraph.setApiVersion(apiVersion);
            String kind = (String) crd.get("kind");
            serviceGraph.setKind(kind);

            LinkedHashMap<String, Object> k8sMetadata = (LinkedHashMap<String, Object>) crd.get("metadata");
            Map<String, String> metadata = new HashMap<>();
            metadata.put("name", (String) k8sMetadata.get("name"));
            metadata.put("namespace", (String) k8sMetadata.get("namespace"));
            metadata.put("resourceVersion", (String) k8sMetadata.get("resourceVersion"));
            serviceGraph.setMetadata(metadata);

            LinkedHashMap<String, Object> k8sSpec = (LinkedHashMap<String, Object>) crd.get("spec");
            Spec spec = new Spec();

            ArrayList<Object> k8sNodes = (ArrayList<Object>) k8sSpec.get("nodes");
            List<Node> nodes = new ArrayList<>();
            for (Object nd : k8sNodes) {
                LinkedHashMap<String, Object> tmpNode = (LinkedHashMap<String, Object>) nd;
                Node node = new Node();
                node.setName((String) tmpNode.get("name"));
                node.setHostNetwork((Boolean) tmpNode.get("hostNetwork"));

                LinkedHashMap<String, Object> k8sLabels = (LinkedHashMap<String, Object>) tmpNode.get("labels");
                Map<String, String> labels = new HashMap<>();
                labels.put("app.kubernetes.io/component", (String) k8sLabels.get("app.kubernetes.io/component"));
                labels.put("app.kubernetes.io/instance", (String) k8sLabels.get("app.kubernetes.io/instance"));
                labels.put("app.kubernetes.io/name", (String) k8sLabels.get("app.kubernetes.io/name"));
                labels.put("app.kubernetes.io/version", (String) k8sLabels.get("app.kubernetes.io/version"));
                node.setLabels(labels);

                ArrayList<Object> k8sContainers = (ArrayList<Object>) tmpNode.get("containers");
                List<Container> containers = new ArrayList<>();
                for (Object cn : k8sContainers) {
                    LinkedHashMap<String, Object> tmpContainer = (LinkedHashMap<String, Object>) cn;
                    Container container = new Container();
                    container.setName((String) tmpContainer.get("name"));
                    container.setImage((String) tmpContainer.get("image"));

                    LinkedHashMap<String, Object> k8sResources = (LinkedHashMap<String, Object>) tmpContainer.get("resources");
                    LinkedHashMap<String, Object> k8sLimits = (LinkedHashMap<String, Object>) k8sResources.get("limits");
                    Resources resources = new Resources();
                    Limits limits = new Limits();
                    limits.setCpu((String) k8sLimits.get("cpu"));
                    limits.setMemory((String) k8sLimits.get("memory"));
                    resources.setLimits(limits);
                    container.setResources(resources);

                    ArrayList<Object> k8sEnv = (ArrayList<Object>) tmpContainer.get("env");
                    Set<NameValuePair> env = new HashSet<>();
                    if (NullCheckUtil.isNotEmpty(k8sEnv)) {
                        for (Object ev : k8sEnv) {
                            LinkedHashMap<String, String> tmpEnv = (LinkedHashMap<String, String>) ev;
                            NameValuePair nameValuePair = new NameValuePair();
                            nameValuePair.setName(tmpEnv.get("name"));
                            nameValuePair.setValue(tmpEnv.get("value"));
                            env.add(nameValuePair);
                        }
                        container.setEnv(env);
                    }

                    LinkedHashMap<String, Object> k8sSecurityContext = (LinkedHashMap<String, Object>) tmpContainer.get("securityContext");
                    Boolean k8sPrivileged = (Boolean) k8sSecurityContext.get("privileged");
                    SecurityContext securityContext = new SecurityContext();
                    securityContext.setPrivileged(k8sPrivileged);
                    container.setSecurityContext(securityContext);

                    containers.add(container);
                }
                node.setContainers(containers);

                ArrayList<Object> k8sImagePullSecrets = (ArrayList<Object>) tmpNode.get("imagePullSecrets");
                List<ImagePullSecret> imagePullSecrets = new ArrayList<>();
                for (Object sc : k8sImagePullSecrets) {
                    LinkedHashMap<String, String> tmpImagePullSecret = (LinkedHashMap<String, String>) sc;
                    ImagePullSecret imagePullSecret = new ImagePullSecret();
                    imagePullSecret.setName(tmpImagePullSecret.get("name"));
                    imagePullSecrets.add(imagePullSecret);
                }
                node.setImagePullSecrets(imagePullSecrets);

                LinkedHashMap<String, Object> k8sReplicas = (LinkedHashMap<String, Object>) tmpNode.get("replicas");
                Replicas replicas = new Replicas();
                replicas.setMax((Integer) k8sReplicas.get("max"));
                replicas.setMin((Integer) k8sReplicas.get("min"));
                node.setReplicas(replicas);

                LinkedHashMap<String, Object> k8sExposedPorts = (LinkedHashMap<String, Object>) tmpNode.get("exposedPorts");
                ExposedPort exposedPort = new ExposedPort();
                if (NullCheckUtil.isNotEmpty(k8sExposedPorts)) {
                    ArrayList<Object> k8sPorts = (ArrayList<Object>) k8sExposedPorts.get("ports");
                    List<PortSection> ports = new ArrayList<>();
                    for (Object pr : k8sPorts) {
                        LinkedHashMap<String, Object> tmpPort = (LinkedHashMap<String, Object>) pr;
                        PortSection port = new PortSection();
                        port.setName((String) tmpPort.get("name"));
                        port.setPort((Integer) tmpPort.get("port"));
                        port.setProtocol((String) tmpPort.get("protocol"));
                        ports.add(port);
                    }
                    exposedPort.setPorts(ports);
                    exposedPort.setType((String) k8sExposedPorts.get("type"));
                    node.setExposedPorts(exposedPort);
                }

                LinkedHashMap<String, Object> k8sNodeHardware = (LinkedHashMap<String, Object>) tmpNode.get("nodeHardware");
                NodeHardware nodeHardware = new NodeHardware();
                LinkedHashMap<String, Object> k8sCpuInfo = (LinkedHashMap<String, Object>) k8sNodeHardware.get("cpuInfo");
                CpuInfo cpuInfo = new CpuInfo();
                ArrayList<Object> k8sArchitectures = (ArrayList<Object>) k8sCpuInfo.get("architectures");
                List<String> architectures = new ArrayList<>();
                for (Object ar : k8sArchitectures) {
                    architectures.add((String) ar);
                }
                cpuInfo.setArchitectures(architectures);
                nodeHardware.setCpuInfo(cpuInfo);
                node.setNodeHardware(nodeHardware);

                List<Object> k8sSlos = (List<Object>) tmpNode.get("slos");
                List<SloTo> sloTos = new ArrayList<>();
                for (Object k8sSlo : k8sSlos) {
                    SloTo sloTo = new SloTo();

                    // SLO Name
                    Map<String, Object> tmpK8sSlo = (Map<String, Object>) k8sSlo;
                    sloTo.setName((String) tmpK8sSlo.get("name"));

                    // SLO Type
                    Map<String, String> k8sSloType = (Map<String, String>) tmpK8sSlo.get("sloType");
                    String sloTypeApiVersion = k8sSloType.get("apiVersion");
                    String sloTypekind = k8sSloType.get("kind");
                    ApiVersionKindPair sloType = new ApiVersionKindPair(sloTypeApiVersion, sloTypekind);
                    sloTo.setSloType(sloType);

                    // SLO Elasticity Strategy
                    Map<String, String> k8sElasticityStrategy = (Map<String, String>) tmpK8sSlo.get("elasticityStrategy");
                    String elasticityStrategyApiVersion = k8sElasticityStrategy.get("apiVersion");
                    String elasticityStrategyKind = k8sElasticityStrategy.get("kind");
                    ApiVersionKindPair elasticityStrategy = new ApiVersionKindPair(elasticityStrategyApiVersion, elasticityStrategyKind);
                    sloTo.setElasticityStrategy(elasticityStrategy);

                    // SLO Config
                    SloConfig sloConfig = new SloConfig();
                    Map<String, Object> k8sSloConfig = (Map<String, Object>) tmpK8sSlo.get("sloConfig");

                    // SLO Config > Streams
                    Map<String, String> streams = (Map<String, String>) k8sSloConfig.get("streams");
                    sloConfig.setStreams(streams);

                    // SLO Config > Insights
                    Map<String, String> insights = (Map<String, String>) k8sSloConfig.get("insights");
                    sloConfig.setInsights(insights);

                    // SLO Config > Target State
                    SloTargetState sloTargetState = new SloTargetState();

                    Map<String, Object> k8sTargetState = (Map<String, Object>) k8sSloConfig.get("targetState");

                    List<DisjunctList> conjuncts = new ArrayList<>();

                    List<Object> k8sConjuncts = (List<Object>) k8sTargetState.get("conjuncts");
                    for (Object k8sConjunct : k8sConjuncts) {

                        Map<String, List<Object>> k8sConjunctDisjuncts = (Map<String, List<Object>>) k8sConjunct;
                        List<Object> k8sDisjuncts = k8sConjunctDisjuncts.get("disjuncts");
                        DisjunctList disjunctList = new DisjunctList();

                        List<Disjunct> disjuncts = new ArrayList<>();
                        for (Object k8sDisjunct : k8sDisjuncts) {
                            Map<String, Object> tmpK8sDisjunct = (Map<String, Object>) k8sDisjunct;
                            Disjunct disjunct = new Disjunct();
                            disjunct.setInsight((String) tmpK8sDisjunct.get("insight"));
                            disjunct.setTargetValue((Integer) tmpK8sDisjunct.get("targetValue"));
                            disjunct.setTolerance((Integer) tmpK8sDisjunct.get("tolerance"));
                            disjunct.setHigherIsBetter((Boolean) tmpK8sDisjunct.get("higherIsBetter"));
                            disjuncts.add(disjunct);
                        }
                        disjunctList.setDisjuncts(disjuncts);

                        conjuncts.add(disjunctList);
                    }

                    sloTargetState.setConjuncts(conjuncts);
                    sloConfig.setTargetState(sloTargetState);

                    // SLO Config > Elasticity Strategy Tolerance
                    sloConfig.setElasticityStrategyTolerance((Integer) k8sSloConfig.get("elasticityStrategyTolerance"));
                    sloTo.setSloConfig(sloConfig);

                    // SLO Static Elasticity Strategy Config
                    Map<String, Affinity> staticElasticityStrategy = new HashMap<>();
                    if (tmpK8sSlo.containsKey("staticElasticityStrategyConfig")) {
                        Map<String, Object> k8sStaticElasticityStrategy =
                                (Map<String, Object>) tmpK8sSlo.get("staticElasticityStrategyConfig");

                        // SLO Static Elasticity Strategy Config > Base Affinity
                        Map<String, Object> k8sBaseAffinity =
                                (Map<String, Object>) k8sStaticElasticityStrategy.get("baseAffinity");
                        Affinity baseAffinity = new Affinity();

                        Map<String, Object> k8sBaseNodeAffinity =
                                (Map<String, Object>) k8sBaseAffinity.get("nodeAffinity");
                        NodeAffinity baseNodeAffinity = new NodeAffinity();

                        Map<String, Object> k8sBaseRequiredDuringSchedulingIgnoredDuringExecution =
                                (Map<String, Object>)
                                        k8sBaseNodeAffinity.get("requiredDuringSchedulingIgnoredDuringExecution");
                        RequiredDuringSchedulingIgnoredDuringExecution
                                baseRequiredDuringSchedulingIgnoredDuringExecution =
                                new RequiredDuringSchedulingIgnoredDuringExecution();

                        List<Object> k8sBaseNodeSelectorTerms =
                                (List<Object>)
                                        k8sBaseRequiredDuringSchedulingIgnoredDuringExecution.get("nodeSelectorTerms");
                        List<NodeSelectorTerms> baseNodeSelectorTermsList = new ArrayList<>();

                        for (Object k8sBaseNodeSelectorTerm : k8sBaseNodeSelectorTerms) {
                            Map<String, Object> tmpK8sBaseNodeSelectorTerm =
                                    (Map<String, Object>) k8sBaseNodeSelectorTerm;

                            List<Object> tmpK8sBaseNodeSelectorTermMatchExpressions =
                                    (List<Object>) tmpK8sBaseNodeSelectorTerm.get("matchExpressions");

                            Set<KeyOperatorPair> keyOperatorPairSet = new HashSet<>();
                            for (Object k8sBaseNodeSelectorTermMatchExpression :
                                    tmpK8sBaseNodeSelectorTermMatchExpressions) {
                                KeyOperatorPair keyOperatorPair = new KeyOperatorPair();

                                Map<String, Object> tmpK8sBaseNodeSelectorTermMatchExpression =
                                        (Map<String, Object>) k8sBaseNodeSelectorTermMatchExpression;
                                String key = (String) tmpK8sBaseNodeSelectorTermMatchExpression.get("key");
                                keyOperatorPair.setKey(key);

                                String operator = (String) tmpK8sBaseNodeSelectorTermMatchExpression.get("operator");
                                keyOperatorPair.setOperator(operator);

                                List<String> values =
                                        (List<String>) tmpK8sBaseNodeSelectorTermMatchExpression.get("values");
                                keyOperatorPair.setValues(values);
                                keyOperatorPairSet.add(keyOperatorPair);
                            }

                            NodeSelectorTerms nodeSelectorTerms = new NodeSelectorTerms();
                            nodeSelectorTerms.setMatchExpressions(keyOperatorPairSet);

                            baseNodeSelectorTermsList.add(nodeSelectorTerms);
                        }
                        baseRequiredDuringSchedulingIgnoredDuringExecution.setNodeSelectorTerms(
                                baseNodeSelectorTermsList);
                        baseNodeAffinity.setRequiredDuringSchedulingIgnoredDuringExecution(
                                baseRequiredDuringSchedulingIgnoredDuringExecution);
                        baseAffinity.setNodeAffinity(baseNodeAffinity);
                        staticElasticityStrategy.put("baseAffinity", baseAffinity);

                        // SLO Static Elasticity Strategy Config > Alternative Affinity
                        Map<String, Object> k8sAlternativeAffinity =
                                (Map<String, Object>) k8sStaticElasticityStrategy.get("alternativeAffinity");
                        Affinity alternativeAffinity = new Affinity();

                        Map<String, Object> k8sAlternativeNodeAffinity =
                                (Map<String, Object>) k8sAlternativeAffinity.get("nodeAffinity");
                        NodeAffinity alternativeNodeAffinity = new NodeAffinity();

                        Map<String, Object> k8sAlternativeRequiredDuringSchedulingIgnoredDuringExecution =
                                (Map<String, Object>)
                                        k8sAlternativeNodeAffinity.get("requiredDuringSchedulingIgnoredDuringExecution");
                        RequiredDuringSchedulingIgnoredDuringExecution
                                alternativeRequiredDuringSchedulingIgnoredDuringExecution =
                                new RequiredDuringSchedulingIgnoredDuringExecution();

                        List<Object> k8sAlternativeNodeSelectorTerms =
                                (List<Object>)
                                        k8sAlternativeRequiredDuringSchedulingIgnoredDuringExecution.get(
                                                "nodeSelectorTerms");
                        List<NodeSelectorTerms> alternativeNodeSelectorTermsList = new ArrayList<>();

                        for (Object k8sAlternativeNodeSelectorTerm : k8sAlternativeNodeSelectorTerms) {
                            Map<String, Object> tmpK8sAlternativeNodeSelectorTerm =
                                    (Map<String, Object>) k8sAlternativeNodeSelectorTerm;

                            List<Object> tmpK8sAlternativeNodeSelectorTermMatchExpressions =
                                    (List<Object>) tmpK8sAlternativeNodeSelectorTerm.get("matchExpressions");

                            Set<KeyOperatorPair> keyOperatorPairSet = new HashSet<>();
                            for (Object k8sAlternativeNodeSelectorTermMatchExpression :
                                    tmpK8sAlternativeNodeSelectorTermMatchExpressions) {
                                KeyOperatorPair keyOperatorPair = new KeyOperatorPair();

                                Map<String, Object> tmpK8sAlternativeNodeSelectorTermMatchExpression =
                                        (Map<String, Object>) k8sAlternativeNodeSelectorTermMatchExpression;
                                String key = (String) tmpK8sAlternativeNodeSelectorTermMatchExpression.get("key");
                                keyOperatorPair.setKey(key);

                                String operator =
                                        (String) tmpK8sAlternativeNodeSelectorTermMatchExpression.get("operator");
                                keyOperatorPair.setOperator(operator);

                                List<String> values =
                                        (List<String>) tmpK8sAlternativeNodeSelectorTermMatchExpression.get("values");
                                keyOperatorPair.setValues(values);
                                keyOperatorPairSet.add(keyOperatorPair);
                            }

                            NodeSelectorTerms nodeSelectorTerms = new NodeSelectorTerms();
                            nodeSelectorTerms.setMatchExpressions(keyOperatorPairSet);

                            alternativeNodeSelectorTermsList.add(nodeSelectorTerms);
                        }
                        alternativeRequiredDuringSchedulingIgnoredDuringExecution.setNodeSelectorTerms(
                                alternativeNodeSelectorTermsList);
                        alternativeNodeAffinity.setRequiredDuringSchedulingIgnoredDuringExecution(
                                alternativeRequiredDuringSchedulingIgnoredDuringExecution);
                        alternativeAffinity.setNodeAffinity(alternativeNodeAffinity);
                        staticElasticityStrategy.put("alternativeAffinity", alternativeAffinity);

                        sloTo.setStaticElasticityStrategyConfig(staticElasticityStrategy);
                    }

                    // SLO Stabilization Window
                    Map<String, Integer> k8sStabilizationWindow = (Map<String, Integer>) tmpK8sSlo.get("stabilizationWindow");
                    StabilizationWindow stabilizationWindow = new StabilizationWindow();
                    stabilizationWindow.setScaleUpSeconds(k8sStabilizationWindow.get("scaleUpSeconds"));
                    stabilizationWindow.setScaleDownSeconds(k8sStabilizationWindow.get("scaleDownSeconds"));
                    sloTo.setStabilizationWindow(stabilizationWindow);

                    // Add slo to slo list
                    sloTos.add(sloTo);
                }
                node.setSlos(sloTos);

                nodes.add(node);
            }
            spec.setNodes(nodes);

            ArrayList<Object> k8slinks = (ArrayList<Object>) k8sSpec.get("links");
            List<Link> links = new ArrayList<>();
            for (Object lk : k8slinks) {
                LinkedHashMap<String, String> tmpLink = (LinkedHashMap<String, String>) lk;
                Link link = new Link();
                link.setSource(tmpLink.get("source"));
                link.setTarget(tmpLink.get("target"));
                links.add(link);
            }
            spec.setLinks(links);

            LinkedHashMap<String, Object> k8sDnsConfig = (LinkedHashMap<String, Object>) k8sSpec.get("dnsConfig");
            DnsConfig dnsConfig = new DnsConfig();
            String dnsPolicy = (String) k8sDnsConfig.get("dnsPolicy");
            ArrayList<String> nameservers = (ArrayList<String>) k8sDnsConfig.get("nameservers");
            dnsConfig.setDnsPolicy(dnsPolicy);
            dnsConfig.setNameservers(nameservers);
            spec.setDnsConfig(dnsConfig);

            serviceGraph.setSpec(spec);

            return serviceGraph;
        } catch (Exception ex) {
            logger.error("Rainbow Util error: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

}
