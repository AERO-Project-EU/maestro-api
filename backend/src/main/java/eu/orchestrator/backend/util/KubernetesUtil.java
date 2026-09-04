package eu.orchestrator.backend.util;

import eu.orchestrator.backend.service.model.KubernetesGraph;
import eu.orchestrator.common.util.NullCheckUtil;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.Component;
import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.repository.domain.ComponentNodeInstanceAffinity;
import eu.orchestrator.repository.domain.EnvironmentalVariableInstance;
import eu.orchestrator.repository.domain.Interface.InterfaceType;
import eu.orchestrator.repository.domain.InterfaceInstance;
import eu.orchestrator.repository.domain.Provider;
import eu.orchestrator.transfer.entities.kubernetes.ClusterLabelsDto;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorProviderAuthenticationDetails;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentSpec;
import io.fabric8.kubernetes.api.model.networking.v1.IPBlock;
import io.fabric8.kubernetes.api.model.networking.v1.NetworkPolicy;
import io.fabric8.kubernetes.api.model.networking.v1.NetworkPolicyIngressRule;
import io.fabric8.kubernetes.api.model.networking.v1.NetworkPolicyPeer;
import io.fabric8.kubernetes.api.model.networking.v1.NetworkPolicyPort;
import io.fabric8.kubernetes.api.model.networking.v1.NetworkPolicySpec;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.util.logging.Level;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.Logger;

public class KubernetesUtil {
    private static final Logger logger = Logger.getLogger(KubernetesUtil.class.getName());

    private static final String LABELS_APP = "app";
    private static final String SERVICE_NODE_PORT = "NodePort";
    private static final String SERVICE_CLUSTER_IP = "ClusterIP";
    private static final String MAESTRO_SERVICE_CORE = "CORE";
    private static final String KUBERNETES_SERVICE_SUFFIX = ".svc.cluster.local";
    private static final String SERVICE_SUFFIX = "-service";
    private static final String DEPLOYMENT_SUFFIX = "-deployment";
    private static final String SECRET_SUFFIX = "-secret";
    private static final String ALL_IPS = "0.0.0.0/0";
    private static final String TCP = "TCP";
    private static final String SERVICE_EXTERNAL_TRAFFIC_POLICY_LOCAL = "Local";

    private static List<String> componentNames = new ArrayList<>();
    private static String namespace_global = null;

    public static Config configProvider(ApplicationInstance applicationInstance) {
        final Provider provider = applicationInstance.getProvider();

        if (NullCheckUtil.isNotEmpty(applicationInstance.getProvider())
                && NullCheckUtil.isNotEmpty(provider.getEndpoint())
                && NullCheckUtil.isNotEmpty(provider.getUsername())
                && NullCheckUtil.isNotEmpty(provider.getPublicKey())
                && NullCheckUtil.isNotEmpty(provider.getPrivateKey())
        ) {

            return configProvider(
                    provider.getEndpoint(),
                    provider.getUsername(),
                    provider.getPublicKey(),
                    provider.getPrivateKey()
            );
        }

        return null;
    }

    public static Config configProvider(Provider provider) {

        if (NullCheckUtil.isNotEmpty(provider)
                && NullCheckUtil.isNotEmpty(provider.getEndpoint())
                && NullCheckUtil.isNotEmpty(provider.getUsername())
                && NullCheckUtil.isNotEmpty(provider.getPublicKey())
                && NullCheckUtil.isNotEmpty(provider.getPrivateKey())
        ) {

            return configProvider(
                    provider.getEndpoint(),
                    provider.getUsername(),
                    provider.getPublicKey(),
                    provider.getPrivateKey()
            );
        }

        return null;
    }

    public static Config configProvider(OrchestratorProviderAuthenticationDetails orchestratorProviderAuthenticationDetails) {

        if (NullCheckUtil.isNotEmpty(orchestratorProviderAuthenticationDetails.getEndpoint())
                && NullCheckUtil.isNotEmpty(orchestratorProviderAuthenticationDetails.getUsername())
                && NullCheckUtil.isNotEmpty(orchestratorProviderAuthenticationDetails.getPublicKey())
                && NullCheckUtil.isNotEmpty(orchestratorProviderAuthenticationDetails.getPrivateKey())) {

            return configProvider(
                    orchestratorProviderAuthenticationDetails.getEndpoint(),
                    orchestratorProviderAuthenticationDetails.getUsername(),
                    orchestratorProviderAuthenticationDetails.getPublicKey(),
                    orchestratorProviderAuthenticationDetails.getPrivateKey()
            );
        }

        return null;
    }

    private static Config configProvider(String masterUrl, String caCertData, String clientCertData, String clientKeyData) {
        final String keyAlgorithm = Config.getKeyAlgorithm(null, clientKeyData);

        return new ConfigBuilder()
                .withMasterUrl(masterUrl)
                .withCaCertData(caCertData)
                .withClientCertData(clientCertData)
                .withClientKeyData(clientKeyData)
                .withClientKeyAlgo(keyAlgorithm)
                .build();
    }

    public static String namespaceProvider(ApplicationInstance applicationInstance) {

        String namespace = null;
        if (NullCheckUtil.isNotEmpty(applicationInstance.getApplication())
                && NullCheckUtil.isNotEmpty(applicationInstance.getApplication().getName())
                && NullCheckUtil.isNotEmpty(applicationInstance.getName())
                && NullCheckUtil.isNotEmpty(applicationInstance.getHexID())) {
            namespace = applicationInstance.getApplication().getName().toLowerCase(Locale.ROOT)
                    + "-" + applicationInstance.getName().toLowerCase(Locale.ROOT)
                    + "-" + applicationInstance.getHexID().toLowerCase(Locale.ROOT);
        }

        return namespace;
    }

    public static String createDockerSecret(String dockerRegistry, String dockerUsername, String dockerPassword) {

        dockerPassword = dockerPassword.replace("\n", "").replace("\r", "");
        String dockerConfigStr = "{\"auths\":{\"%s\":{\"username\":\"%s\",\"password\":\"%s\",\"auth\":\"%s\"}}}";
        String tempAuths = dockerUsername + ":" + dockerPassword;
        String auths = Base64.getEncoder().encodeToString(tempAuths.getBytes());
        return Base64.getEncoder().encodeToString(String.format(dockerConfigStr, dockerRegistry, dockerUsername, dockerPassword, auths).getBytes());

    }

    public static String componentInstanceNameProvider(ComponentNodeInstance componentNodeInstance) {

        String name = null;
        if (NullCheckUtil.isNotEmpty(componentNodeInstance.getApplicationInstance())
                && NullCheckUtil.isNotEmpty(componentNodeInstance.getApplicationInstance().getName()) && NullCheckUtil.isNotEmpty(
                componentNodeInstance.getName()) && NullCheckUtil.isNotEmpty(componentNodeInstance.getHexID())) {
            name = componentNodeInstance.getApplicationInstance().getName().toLowerCase(Locale.ROOT) + "-" + componentNodeInstance.getName()
                    .toLowerCase(Locale.ROOT) + "-" + componentNodeInstance.getHexID().toLowerCase(Locale.ROOT);
        }

        return name;
    }

    public static String serviceNameProvider(ComponentNodeInstance componentNodeInstance) {
        String name = componentInstanceNameProvider(componentNodeInstance) + SERVICE_SUFFIX;
        return name;
    }

    public static KubernetesGraph applicationInstanceToK8s(ApplicationInstance applicationInstance, String namespace, Boolean networkModeHost,
            boolean k8sResourcesEnable, Map<Long, ComponentNodeInstanceAffinity> cniAffinityMap) {

        KubernetesGraph kubernetesGraph = new KubernetesGraph();
        componentNames = new ArrayList<>();
        if (NullCheckUtil.isNotEmpty(applicationInstance.getComponentNodeInstances())) {
            SortedSet<ComponentNodeInstance> componentNodeInstances = applicationInstance.getComponentNodeInstances();

            namespace_global = namespace;

            //for service discovery
            componentNodeInstances.forEach(x -> componentNames.add(componentInstanceNameProvider(x)));

            for (ComponentNodeInstance cni : componentNodeInstances) {
                Deployment cniDeployment = k8sDeployment(cni, networkModeHost, k8sResourcesEnable);
                if (cniAffinityMap != null && cniAffinityMap.containsKey(cni.getComponentNodeInstanceID())) {
                    setK8sDeploymentAffinity(cniDeployment, cniAffinityMap.get(cni.getComponentNodeInstanceID()));
                }
                kubernetesGraph.getDeployment().add(cniDeployment);
                Service service = k8sService(cni);
                if (NullCheckUtil.isNotEmpty(service)) {
                    kubernetesGraph.getService().add(service);
                }
            }
        }

        return kubernetesGraph;
    }

    public static NetworkPolicy k8sNetworkPolicy(ComponentNodeInstance componentNodeInstance, List<String> blockedIps, Integer port) {

        String name = componentInstanceNameProvider(componentNodeInstance);

        NetworkPolicy networkPolicy = new NetworkPolicy();
        //Metadata
        ObjectMeta metadata = new ObjectMeta();
        metadata.setName(name);

        networkPolicy.setMetadata(metadata);

        NetworkPolicySpec networkPolicySpec = new NetworkPolicySpec();
        LabelSelector labelSelector = new LabelSelector();
        Map<String, String> matchLabels = new HashMap<>();
        matchLabels.put(LABELS_APP, name);
        labelSelector.setMatchLabels(matchLabels);

        networkPolicySpec.setPodSelector(labelSelector);

        List<NetworkPolicyIngressRule> ingress = new ArrayList<>();
        NetworkPolicyIngressRule networkPolicyIngressRule = new NetworkPolicyIngressRule();

        List<NetworkPolicyPeer> from = new ArrayList<>();
        NetworkPolicyPeer networkPolicyPeer = new NetworkPolicyPeer();

        IPBlock ipBlock = new IPBlock();
        ipBlock.setCidr(ALL_IPS);
        ipBlock.setExcept(blockedIps);

        networkPolicyPeer.setIpBlock(ipBlock);
        from.add(networkPolicyPeer);

        List<NetworkPolicyPort> ports = new ArrayList<>();
        NetworkPolicyPort networkPolicyPort = new NetworkPolicyPort();
        networkPolicyPort.setProtocol(TCP);
        networkPolicyPort.setPort(new IntOrString(port));
        ports.add(networkPolicyPort);

        networkPolicyIngressRule.setFrom(from);
        networkPolicyIngressRule.setPorts(ports);

        ingress.add(networkPolicyIngressRule);

        networkPolicySpec.setIngress(ingress);
        networkPolicy.setSpec(networkPolicySpec);

        return networkPolicy;
    }

    static Service k8sService(ComponentNodeInstance componentNodeInstance) {
        Service service = null;
        if (NullCheckUtil.isNotEmpty(componentNodeInstance.getInterfaceInstances())) {

            service = new Service();
            String name = componentInstanceNameProvider(componentNodeInstance);
            //Metadata
            String svcName = serviceNameProvider(componentNodeInstance);
            ObjectMeta metadata = new ObjectMeta();
            metadata.setName(svcName);
            metadata.setNamespace(namespace_global);

            service.setMetadata(metadata);

            //Spec
            ServiceSpec spec = new ServiceSpec();

            //selector
            Map<String, String> selectorMap = new HashMap<>();
            selectorMap.put(LABELS_APP, name);
            spec.setSelector(selectorMap);

            //TODO better way to decide type
            String serviceType = SERVICE_CLUSTER_IP;
            //Ports
            List<ServicePort> ports = new ArrayList<>();
            int portName = 0;
            for (InterfaceInstance ii : componentNodeInstance.getInterfaceInstances()) {
                ServicePort servicePort = new ServicePort();
                servicePort.setName("http-" + portName);
                portName++;
                if (NullCheckUtil.isNotEmpty(ii.getPort())) {
                    servicePort.setPort(Integer.parseInt(ii.getPort()));
                    //TODO add targetPort support to maestro
                    servicePort.setTargetPort(new IntOrString(Integer.parseInt(ii.getPort())));
                }
                if (NullCheckUtil.isNotEmpty(ii.getInterfaceType())) {
                    if (InterfaceType.ACCESS.name().equalsIgnoreCase(ii.getInterfaceType())) {
                        serviceType = SERVICE_NODE_PORT;
                    }

                }
                ports.add(servicePort);
            }
            spec.setPorts(ports);

            //Type
            spec.setType(serviceType);
            if (serviceType.equalsIgnoreCase(SERVICE_NODE_PORT)) {
                spec.setExternalTrafficPolicy(SERVICE_EXTERNAL_TRAFFIC_POLICY_LOCAL);
            }

            service.setSpec(spec);
        }

        return service;
    }

    private static Deployment k8sDeployment(ComponentNodeInstance componentNodeInstance, Boolean networkModeHost, boolean k8sResourcesEnable) {

        String name = componentInstanceNameProvider(componentNodeInstance);

        Deployment deployment = new Deployment();

        //Metadata
        ObjectMeta metadata = new ObjectMeta();
        metadata.setName(name + DEPLOYMENT_SUFFIX);
        Map<String, String> metadataMap = new HashMap<>();
        metadataMap.put(LABELS_APP, name);
        metadata.setLabels(metadataMap);

        deployment.setMetadata(metadata);

        //Spec
        DeploymentSpec spec = new DeploymentSpec();
        spec.setReplicas(1);
        LabelSelector labelSelector = new LabelSelector();
        Map<String, String> labelSelectorMap = new HashMap<>();
        labelSelectorMap.put(LABELS_APP, name);
        labelSelector.setMatchLabels(labelSelectorMap);
        spec.setSelector(labelSelector);
        PodTemplateSpec template = new PodTemplateSpec();
        ObjectMeta templateMetadata = new ObjectMeta();
        Map<String, String> templateMetadataMap = new HashMap<>();
        templateMetadataMap.put(LABELS_APP, name);
        templateMetadata.setLabels(templateMetadataMap);
        template.setMetadata(templateMetadata);

        PodSpec podSpec = new PodSpec();
        //hostNetwork
        if (NullCheckUtil.isNotEmpty(componentNodeInstance.getComponentNode())
                && NullCheckUtil.isNotEmpty(componentNodeInstance.getComponentNode().getComponent())
                && NullCheckUtil.isNotEmpty(componentNodeInstance.getComponentNode().getComponent().getNetworkModeHost())) {
            if (Boolean.TRUE.equals(networkModeHost)) {
                podSpec.setHostNetwork(Boolean.TRUE);
            } else {
                podSpec.setHostNetwork(componentNodeInstance.getComponentNode().getComponent().getNetworkModeHost());
            }
        }
        //container
        Container container = new Container();
        container.setName(name);
        container.setImage(dockerImageNameProvider(componentNodeInstance));

        //resources
        if (k8sResourcesEnable && NullCheckUtil.isNotEmpty(componentNodeInstance.getFlavorInstance())) {
            ResourceRequirements resourceRequirements = new ResourceRequirements();
            Map<String, Quantity> limits = new HashMap<>();
            Map<String, Quantity> requests = new HashMap<>();

            if (NullCheckUtil.isNotEmpty(componentNodeInstance.getFlavorInstance().getvCPUs())) {
                Quantity quantity = new Quantity();
                quantity.setAmount(componentNodeInstance.getFlavorInstance().getvCPUs().toString());
                limits.put("cpu", quantity);
                requests.put("cpu", quantity);
            }

            if (NullCheckUtil.isNotEmpty(componentNodeInstance.getFlavorInstance().getRam())) {
                Quantity quantity = new Quantity();
                String ram = componentNodeInstance.getFlavorInstance().getRam().toString() + "Mi";
                quantity.setAmount(ram);
                limits.put("memory", quantity);
                requests.put("memory", quantity);
            }

            if (componentNodeInstance.getComponentNode().getComponent().getRequirement().getGpuRequired()) {
                Quantity quantity = new Quantity();
                quantity.setAmount("1");
                limits.put("nvidia.com/gpu", quantity);
                requests.put("nvidia.com/gpu", quantity);
            }

            if (NullCheckUtil.isNotEmpty(limits) || NullCheckUtil.isNotEmpty(requests)) {
                if (NullCheckUtil.isNotEmpty(limits)) {
                    resourceRequirements.setLimits(limits);
                }
                if (NullCheckUtil.isNotEmpty(requests)) {
                    resourceRequirements.setRequests(requests);
                }
                container.setResources(resourceRequirements);
            }


        }

        //command
        if (componentNodeInstance.getCommand() != null && !componentNodeInstance.getCommand().isEmpty()) {
            List<String> command = new ArrayList<>();
            command.add(componentNodeInstance.getCommand());
            container.setCommand(command);
        }
        //TODO REVIEW
        //ports
        if (NullCheckUtil.isNotEmpty(componentNodeInstance.getInterfaceInstances())) {
            ContainerPort containerPort = new ContainerPort();
            containerPort.setContainerPort(Integer.parseInt(componentNodeInstance.getInterfaceInstances().get(0).getPort()));
            container.getPorts().add(containerPort);
        }
        //env
        if (NullCheckUtil.isNotEmpty(componentNodeInstance.getEnvironmentalVariableInstances())) {
            container.setEnv(environmentalVariableInstancesToEnv
                    (componentNodeInstance.getEnvironmentalVariableInstances()));
        }
        //security context
        if (NullCheckUtil.isNotEmpty(componentNodeInstance.getComponentNode())
                && NullCheckUtil.isNotEmpty(componentNodeInstance.getComponentNode().getComponent())
                && NullCheckUtil.isNotEmpty(componentNodeInstance.getComponentNode().getComponent().getPrivilege())) {
            SecurityContext securityContext = new SecurityContext();
            securityContext.setPrivileged(componentNodeInstance.getComponentNode().getComponent().getPrivilege());
            container.setSecurityContext(securityContext);
        }

        podSpec.getContainers().add(container);

        if (NullCheckUtil.isNotEmpty(componentNodeInstance.getKubernetesRuntimeClassName())) {
            podSpec.setRuntimeClassName(componentNodeInstance.getKubernetesRuntimeClassName()); // e.g., "kata-fc"
        }

        //image pull secrets
        LocalObjectReference imagePullSecret = new LocalObjectReference();
        imagePullSecret.setName(name + SECRET_SUFFIX);
        podSpec.getImagePullSecrets().add(imagePullSecret);
        template.setSpec(podSpec);

        spec.setTemplate(template);

        deployment.setSpec(spec);

        return deployment;
    }

    private static void setK8sDeploymentAffinity(Deployment deployment, ComponentNodeInstanceAffinity cniAffinity){

        Map<String, String> labelsMaps = new HashMap<>();
        cniAffinity.getAffinityLabels().forEach( label ->{
            String[] lSplit = label.split("=");
            labelsMaps.put(lSplit[0], lSplit[1]);
        });

        deployment.getSpec().getTemplate().getSpec().setNodeSelector(labelsMaps);

    }
    private static Deployment fetchK8sDeployment(Config config, String namespace, String deploymentName) {

        try (final KubernetesClient client = new DefaultKubernetesClient(Objects.requireNonNull(config))) {

            return client.apps().deployments().inNamespace(namespace).withName(deploymentName).get();
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Fetch Deployment {0} has failed. Error: {1}",
                    new Object[]{deploymentName, e.getMessage()});
            return null;
        }
    }

    public static Integer fetchReplicasFromK8sDeployment(Config config, String namespace, String deploymentName) {
        Deployment deployment = fetchK8sDeployment(config, namespace, deploymentName);

        if (deployment != null) return deployment.getSpec().getReplicas();

        return 0;
    }

    public static void scaleK8sDeployment(Config config, String namespace, String deploymentName, Integer replicas){
        try (final KubernetesClient client = new DefaultKubernetesClient(Objects.requireNonNull(config))) {

            client.apps().deployments().inNamespace(namespace).withName(deploymentName).scale(replicas);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Scaling Deployment {0} has failed. Error: {1}",
                    new Object[]{deploymentName, e.getMessage()});
        }
    }

    static String dockerImageNameProvider(ComponentNodeInstance componentNodeInstance) {

        String name = null;

        if (NullCheckUtil.isNotEmpty(componentNodeInstance.getComponentNode()) && NullCheckUtil.isNotEmpty(
                componentNodeInstance.getComponentNode().getComponent())) {
            Component component = componentNodeInstance.getComponentNode().getComponent();
            if (NullCheckUtil.isNotEmpty(component.getDockerRegistry()) && NullCheckUtil.isNotEmpty(component.getDockerImage())) {
                name = component.getDockerRegistry() + "/" + component.getDockerImage();
            } else if (NullCheckUtil.isNotEmpty(component.getDockerImage())) {
                name = component.getDockerImage();
            }
        }

        return name;
    }

//    static List<EnvVar> environmentalVariableInstancesToEnv(List<EnvironmentalVariableInstance> environmentalVariableInstances) {
//
//        List<EnvVar> envVars = new ArrayList<>();
//
//        // Regex pattern for port identification
//        String regex = ":(\\d+)$";
//
//        for (EnvironmentalVariableInstance en : environmentalVariableInstances) {
//            if (en.getValue().startsWith("@")) {
//                String value = en.getValue().replaceFirst("@", "").toLowerCase(Locale.ROOT);
//
//                // Check for port number existence (:portNumber)
//                String portNumber = "";
//                Pattern pattern = Pattern.compile(regex);
//                Matcher portMatcher = pattern.matcher(value);
//                if (portMatcher.find()) {
//                    portNumber = portMatcher.group(1);
//                    value = value.replaceFirst(":.*", "").toLowerCase(Locale.ROOT);
//                }
//
//                for (String nm : componentNames) {
//                    if (nm.contains(value)) {
//                        nm = nm + SERVICE_SUFFIX + "." + namespace_global + KUBERNETES_SERVICE_SUFFIX;
//                        if (!Objects.equals(portNumber, "")) {
//                            nm = nm + ":" + portNumber;
//                        }
//                        EnvVar envVar = new EnvVar();
//                        envVar.setName(en.getKey());
//                        envVar.setValue(nm);
//                        envVars.add(envVar);
//                    }
//                }
//
//            } else {
//                EnvVar envVar = new EnvVar();
//                envVar.setName(en.getKey());
//                envVar.setValue(en.getValue());
//                envVars.add(envVar);
//            }
//
//        }
//
//        return envVars;
//    }

    static List<EnvVar> environmentalVariableInstancesToEnv(List<EnvironmentalVariableInstance> environmentalVariableInstances) {

        List<EnvVar> envVars = new ArrayList<>();

        // Pattern to find @service or @service:port
        Pattern atPattern = Pattern.compile("@([a-zA-Z0-9-_]+)(?::(\\d+))?");

        for (EnvironmentalVariableInstance en : environmentalVariableInstances) {
            String originalValue = en.getValue();
            Matcher matcher = atPattern.matcher(originalValue);
            StringBuffer resolvedValue = new StringBuffer();

            while (matcher.find()) {
                String serviceName = matcher.group(1).toLowerCase(Locale.ROOT);
                String portNumber = matcher.group(2); // could be null

                String resolvedService = null;

                for (String component : componentNames) {
                    if (component.contains(serviceName)) {
                        resolvedService = component + SERVICE_SUFFIX + "." + namespace_global + KUBERNETES_SERVICE_SUFFIX;
                        if (portNumber != null) {
                            resolvedService += ":" + portNumber;
                        }
                        break; // Only replace the first matching component
                    }
                }

                // If no matching service, leave the original @... intact
                if (resolvedService == null) {
                    resolvedService = matcher.group(0);
                }

                matcher.appendReplacement(resolvedValue, Matcher.quoteReplacement(resolvedService));
            }

            matcher.appendTail(resolvedValue);

            EnvVar envVar = new EnvVar();
            envVar.setName(en.getKey());
            envVar.setValue(resolvedValue.toString());
            envVars.add(envVar);
        }

        return envVars;
    }

    //TODO review
    public static Integer fetchPortFromService(Service service) {

        if (NullCheckUtil.isNotEmpty(service) && NullCheckUtil.isNotEmpty(service.getSpec()) && NullCheckUtil.isNotEmpty(service.getSpec().getPorts())
                && NullCheckUtil.isNotEmpty(service.getSpec().getPorts().get(0))) {
            return service.getSpec().getPorts().get(0).getPort();
        }

        return null;
    }

    public static String getCleanK8sEndpoint(ApplicationInstance applicationInstance) {
        String endpoint = applicationInstance.getProvider().getEndpoint();
        Pattern regDitchPrefix = Pattern.compile("(?:https*://)*(.+)");
        Matcher matcher = regDitchPrefix.matcher(endpoint);
        if (matcher.find()) {
            endpoint = matcher.group(1);
            Pattern regDitchSuffix = Pattern.compile("(.+)(?::\\d+)");
            matcher = regDitchSuffix.matcher(endpoint);
            if (matcher.find()) {
                endpoint = matcher.group(1);
            }
            return endpoint;
        } else {
            return null;
        }

    }

    public static SortedSet<String> fetchNodeLabels(Config config, List<String> filterLabels) {
        try (final KubernetesClient client = new DefaultKubernetesClient(Objects.requireNonNull(config))) {

            SortedSet<String> labelKeys = new TreeSet<>();
            SortedSet<String> labels = new TreeSet<>();

            List<Node> nodes = client.nodes().list().getItems();
            Boolean skipFilter;

            if (filterLabels != null && !filterLabels.isEmpty() && !filterLabels.contains("all")) {
                skipFilter = false;
                filterLabels.forEach(key -> {
                    labelKeys.add(key);
                });
            } else {
                skipFilter = true;
            }

            nodes.stream().forEach(node -> {
                Map<String, String> nodeLabels = node.getMetadata().getLabels();
                nodeLabels.forEach((key, value) -> {
                    if (labelKeys.contains(key)){
                        labels.add(key + "=" + value);
                    }else{
                        if (skipFilter){
                            labelKeys.add(key);
                            labels.add(key + "=" + value);
                        }
                    }
                });
            });

            return labels;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Fetch Node Labels has failed. Error: ", e.getMessage());
        }
        return null;
    }
}
