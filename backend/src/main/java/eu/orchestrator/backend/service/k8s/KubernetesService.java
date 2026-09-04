package eu.orchestrator.backend.service.k8s;

import eu.orchestrator.backend.config.K8sConfig;
import eu.orchestrator.backend.service.applicationinstance.ApplicationInstanceService;
import eu.orchestrator.backend.service.applicationinstance.ComponentNodeInstanceService;
import eu.orchestrator.backend.service.k8s.resources.DeploymentK8sService;
import eu.orchestrator.backend.service.k8s.resources.DeploymentLifecycleService;
import eu.orchestrator.backend.service.k8s.resources.NamespaceService;
import eu.orchestrator.backend.service.k8s.resources.NetworkPolicyService;
import eu.orchestrator.backend.service.k8s.resources.PodService;
import eu.orchestrator.backend.service.k8s.resources.SecretService;
import eu.orchestrator.backend.service.k8s.resources.ServiceService;
import eu.orchestrator.backend.service.model.KubernetesGraph;
import eu.orchestrator.backend.service.resourceprovider.ProviderService;
import eu.orchestrator.backend.service.security.SecurityConfigurationService;
import eu.orchestrator.backend.transfer.PolicyEngineKubernetesConfigTO;
import eu.orchestrator.backend.util.KubernetesUtil;
import eu.orchestrator.backend.util.Util;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.common.util.NullCheckUtil;
import eu.orchestrator.repository.dao.NetworkRuleDAO;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.ApplicationInstance.ApplicationInstanceStatus;
import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.repository.domain.ComponentNodeInstanceAffinity;
import eu.orchestrator.repository.domain.ComponentNodeInstanceHash;
import eu.orchestrator.repository.domain.ComponentNodeInstanceIP;
import eu.orchestrator.repository.domain.ComponentNodeInstanceStatus;
import eu.orchestrator.repository.domain.Interface.InterfaceType;
import eu.orchestrator.repository.domain.NetworkRule;
import eu.orchestrator.repository.domain.Provider;
import eu.orchestrator.repository.domain.ProviderType;
import eu.orchestrator.repository.domain.ProviderType.ProviderName;
import eu.orchestrator.repository.domain.SecurityConfigurationResult;
import eu.orchestrator.transfer.entities.kubernetes.ClusterLabelsDto;
import eu.orchestrator.transfer.entities.kubernetes.NamespaceDto;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorChangedStatusNotification;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorProviderAuthenticationDetails;
import eu.orchestrator.transfer.util.Encryption;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.networking.v1.NetworkPolicy;
import io.fabric8.kubernetes.client.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import jakarta.transaction.Transactional;

import static eu.orchestrator.backend.util.KubernetesUtil.getCleanK8sEndpoint;
import static eu.orchestrator.backend.util.KubernetesUtil.serviceNameProvider;
import static eu.orchestrator.repository.domain.ProviderType.ProviderName.*;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;

@org.springframework.stereotype.Service
@Transactional
public class KubernetesService {

    private static final Logger LOGGER = Logger.getLogger(KubernetesService.class.getName());
    private static final String SERVICE_SUFFIX = "-service";
    private static final String DEPLOYMENT_SUFFIX = "-deployment";
    private static final String SECRET_SUFFIX = "-secret";

    @Autowired
    private NetworkRuleDAO networkRuleDAO;

    @Autowired
    private ApplicationInstanceService applicationInstanceService;

    @Autowired
    private ComponentNodeInstanceService componentNodeInstanceService;

    @Autowired
    private ProviderService providerService;

    @Autowired
    private SecurityConfigurationService securityConfigurationService;

    @Autowired
    private NamespaceService namespaceService;

    @Autowired
    private SecretService secretService;

    @Autowired
    private DeploymentK8sService deploymentK8sService;

    @Autowired
    private ServiceService serviceService;

    @Autowired
    private DeploymentLifecycleService deploymentLifecycleService;

    @Autowired
    private NetworkPolicyService networkPolicyService;

    @Autowired
    private PodService podService;

    @Value("${token.signer.secret}")
    private String secretToken;

    @Value("${kubernetes.consider-resources}")
    private boolean k8sResourcesEnable;

    @Autowired
    K8sConfig k8sConfig;

    @Value("${policy-engine.kubernetes-config.config-map-namespace}")
    private String kubernetesConfigMapNamespace;

    @Value("${policy-engine.kubernetes-config.config-map-name}")
    private String kubernetesConfigMapName;


    public NamespaceDto fetchNamespaceByApplicationInstance(Long applicationInstanceId) {
        ApplicationInstance applicationInstance = applicationInstanceService.fetchApplicationInstanceById(applicationInstanceId);
        if (null == applicationInstance) {
            throw new GenericBusinessException(GenericMessage.APPLICATION_INSTANCE_NOT_EXIST.getCode(), GenericMessage.APPLICATION_INSTANCE_NOT_EXIST);
        }
        NamespaceDto namespaceDto = new NamespaceDto();
        namespaceDto.setNamespace(KubernetesUtil.namespaceProvider(applicationInstance));
        return namespaceDto;
    }

    public PolicyEngineKubernetesConfigTO fetchPolicyEngineK8sConfigByApplicationInstanceHexId(String applicationInstanceHexId) {
        ApplicationInstance applicationInstance = applicationInstanceService.fetchApplicationInstanceByHexId(applicationInstanceHexId);
        Provider provider = applicationInstance.getProvider();

        PolicyEngineKubernetesConfigTO kubernetesConfig = new PolicyEngineKubernetesConfigTO();
        kubernetesConfig.setMasterUrl(provider.getEndpoint());
        kubernetesConfig.setCertificateAuthorityData(provider.getUsername());
        kubernetesConfig.setClientCertificateData(provider.getPublicKey());
        kubernetesConfig.setClientKeyData(provider.getPrivateKey());
        kubernetesConfig.setConfigMapNamespace(kubernetesConfigMapNamespace);
        kubernetesConfig.setConfigMapName(kubernetesConfigMapName);

        return kubernetesConfig;
    }

    public boolean deployment(Long applicationInstanceID, Boolean networkModeHost) {

        ApplicationInstance applicationInstance = applicationInstanceService.fetchApplicationInstanceById(applicationInstanceID);
        if (null == applicationInstance) {
            LOGGER.log(SEVERE, "Aborting K8s deployment as application instance [id={0}] was not found", applicationInstanceID);
            return Boolean.FALSE;
        }

        Config config = getKubernetesConfig(applicationInstance);
        if (NullCheckUtil.isEmpty(config)) {
            LOGGER.log(SEVERE, "Aborting K8s deployment of application instance [id={0}]: failure creating K8s client config", applicationInstanceID);
            return Boolean.FALSE;
        }

        // Namespace creation
        final Optional<String> namespaceMaybe = provideNamespace(applicationInstance);
        if (!namespaceMaybe.isPresent()) {
            LOGGER.log(SEVERE, "Aborting K8s deployment of application instance [id={0}]: failure generating namespace name", applicationInstanceID);
            return Boolean.FALSE;
        }
        final String namespace = namespaceMaybe.get();

        if (!namespaceDeployment(namespace, config)) {
            LOGGER.log(SEVERE, "Aborting K8s deployment of application instance [id={0}]: failure creating namespace", applicationInstanceID);
            return Boolean.FALSE;
        }

        //secrets creation
        if (!secretDeployment(applicationInstance, namespace, config)) {
            LOGGER.log(SEVERE, "Aborting K8s deployment of application instance [id={0}]: failure creating secrets", applicationInstanceID);
            secretsUndeployment(applicationInstance, namespace, config);
            namespaceUndeployment(namespace, config);
            return Boolean.FALSE;
        }

        Map<Long, ComponentNodeInstanceAffinity> cniAffinities = componentNodeInstanceService.fetchCNIAffinityMapByApplicationInstance(applicationInstance);
        KubernetesGraph kubernetesGraph = KubernetesUtil.applicationInstanceToK8s(applicationInstance, namespace, networkModeHost, k8sResourcesEnable, cniAffinities);
        if (!k8sGraphDeployment(kubernetesGraph, namespace, config)) {
            LOGGER.log(SEVERE, "Aborting K8s deployment of application instance [id={0}]: failure deploying K8s graph", applicationInstanceID);
            k8sGraphUnDeployment(applicationInstance, namespace, config);
            secretsUndeployment(applicationInstance, namespace, config);
            namespaceUndeployment(namespace, config);
        }

        return Boolean.TRUE;
    }

    protected Config getKubernetesConfig(ApplicationInstance applicationInstance) {
        return KubernetesUtil.configProvider(applicationInstance);
    }

    public boolean undeployment(Long applicationInstanceID) {

        ApplicationInstance applicationInstance = applicationInstanceService.fetchApplicationInstanceById(applicationInstanceID);
        if (null == applicationInstance) {
            return Boolean.FALSE;
        }

        Config config = getKubernetesConfig(applicationInstance);
        if (NullCheckUtil.isEmpty(config)) {
            return Boolean.FALSE;
        }

        boolean undeploymentSuccess = Boolean.TRUE;
        String namespace = KubernetesUtil.namespaceProvider(applicationInstance);

        if (!k8sGraphUnDeployment(applicationInstance, namespace, config)) {
            undeploymentSuccess = Boolean.FALSE;
        }
        if (!secretsUndeployment(applicationInstance, namespace, config)) {
            undeploymentSuccess = Boolean.FALSE;
        }
        if (!namespaceUndeployment(namespace, config)) {
            undeploymentSuccess = Boolean.FALSE;
        }

        return undeploymentSuccess;
    }

    public boolean checkLogin(OrchestratorProviderAuthenticationDetails orchestratorProviderAuthenticationDetails) {

        Config config = KubernetesUtil.configProvider(orchestratorProviderAuthenticationDetails);
        if (NullCheckUtil.isEmpty(config)) {
            return Boolean.FALSE;
        }

        return deploymentLifecycleService.checkLogin(config);
    }

    protected Optional<String> provideNamespace(ApplicationInstance applicationInstance) {
        final String namespace = KubernetesUtil.namespaceProvider(applicationInstance);

        return namespace != null ? Optional.of(namespace) : Optional.empty();
    }

    protected boolean namespaceDeployment(String namespace, Config config) {
        return namespaceService.createNamespace(namespace, config);
    }

    protected boolean secretDeployment(ApplicationInstance applicationInstance, String namespace, Config config) {
        //TODO reduce FOR by adding it to the general loop
        boolean secretsCreationSuccess = Boolean.TRUE;
        if (NullCheckUtil.isNotEmpty(applicationInstance.getComponentNodeInstances())) {
            for (ComponentNodeInstance cni : applicationInstance.getComponentNodeInstances()) {
                if (NullCheckUtil.isNotEmpty(cni.getComponentNode().getComponent())
                        //&& Boolean.TRUE.equals(cni.getComponentNode().getComponent().getDockerCustomRegistry())
                        && NullCheckUtil.isNotEmpty(cni.getComponentNode().getComponent().getDockerRegistry())
                        && NullCheckUtil.isNotEmpty(cni.getComponentNode().getComponent().getDockerUsername())
                        && NullCheckUtil.isNotEmpty(cni.getComponentNode().getComponent().getDockerPassword())
                ) {
                    if (!secretService.createSecretService(namespace, KubernetesUtil.componentInstanceNameProvider(cni) + SECRET_SUFFIX,
                            KubernetesUtil.createDockerSecret(cni.getComponentNode().getComponent().getDockerRegistry(),
                                    cni.getComponentNode().getComponent().getDockerUsername(),
                                    Util.decrypt(cni.getComponentNode().getComponent().getDockerPassword(), secretToken)),
                            config)) {
                        secretsCreationSuccess = Boolean.FALSE;
                        break;
                    }
                }
            }
        }

        return secretsCreationSuccess;
    }

    protected boolean k8sGraphDeployment(KubernetesGraph kubernetesGraph, String namespace, Config config) {

        if (NullCheckUtil.isNotEmpty(kubernetesGraph.getDeployment())) {
            for (Deployment dp : kubernetesGraph.getDeployment()) {
                if (!deploymentK8sService.createDeploymentService(namespace, dp, config)) {
                    return Boolean.FALSE;
                }
            }
        }

        if (NullCheckUtil.isNotEmpty(kubernetesGraph.getService())) {
            for (Service sv : kubernetesGraph.getService()) {
                if (!serviceService.createServiceService(namespace, sv, config)) {
                    return Boolean.FALSE;
                }
            }
        }
        return Boolean.TRUE;
    }

    protected boolean k8sGraphUnDeployment(ApplicationInstance applicationInstance, String namespace, Config config) {

        boolean k8sGraphUnDeploymentSuccess = Boolean.TRUE;
        if (NullCheckUtil.isNotEmpty(applicationInstance.getComponentNodeInstances())) {
            for (ComponentNodeInstance cni : applicationInstance.getComponentNodeInstances()) {
                String cname = KubernetesUtil.componentInstanceNameProvider(cni);
                if (!deploymentK8sService.deleteDeploymentService(namespace, cname + DEPLOYMENT_SUFFIX, config)) {
                    k8sGraphUnDeploymentSuccess = Boolean.FALSE;
                }
                if (!serviceService.deleteServiceService(namespace, cname + SERVICE_SUFFIX, config)) {
                    k8sGraphUnDeploymentSuccess = Boolean.FALSE;
                }
            }
        }

        return k8sGraphUnDeploymentSuccess;
    }

    protected boolean secretsUndeployment(ApplicationInstance applicationInstance, String namespace, Config config) {
        boolean secretsDeletionSuccess = Boolean.TRUE;
        if (NullCheckUtil.isNotEmpty(applicationInstance.getComponentNodeInstances())) {
            for (ComponentNodeInstance cni : applicationInstance.getComponentNodeInstances()) {
                if (NullCheckUtil.isNotEmpty(cni.getComponentNode().getComponent())
                        //&& Boolean.TRUE.equals(cni.getComponentNode().getComponent().getDockerCustomRegistry())
                        && NullCheckUtil.isNotEmpty(cni.getComponentNode().getComponent().getDockerRegistry())
                        && NullCheckUtil.isNotEmpty(cni.getComponentNode().getComponent().getDockerUsername())
                        && NullCheckUtil.isNotEmpty(cni.getComponentNode().getComponent().getDockerPassword())
                ) {
                    if (!secretService.deleteSecretService(namespace, KubernetesUtil.componentInstanceNameProvider(cni) + SECRET_SUFFIX, config)) {
                        secretsDeletionSuccess = Boolean.FALSE;
                    }
                }
            }
        }
        return secretsDeletionSuccess;
    }

    protected boolean namespaceUndeployment(String name, Config config) {
        return namespaceService.deleteNamespace(name, config);
    }

    public boolean networkPolicyApply(String componentNodeInstanceHexId, String blockedIp) {
        ComponentNodeInstance componentNodeInstance = componentNodeInstanceService.fetchComponentNodeInstanceByHexId(componentNodeInstanceHexId);
        if (componentNodeInstance == null) {
            return Boolean.FALSE;
        }
        ApplicationInstance applicationInstance = componentNodeInstance.getApplicationInstance();
        Config config = getKubernetesConfig(applicationInstance);
        if (NullCheckUtil.isEmpty(config)) {
            return Boolean.FALSE;
        }

        String namespace = KubernetesUtil.namespaceProvider(applicationInstance);
        String componentName = KubernetesUtil.componentInstanceNameProvider(componentNodeInstance);

        List<String> blockedIps = retrieveBlockIps(componentNodeInstanceHexId, blockedIp);

        Service service = serviceService.getServiceService(namespace, componentName + SERVICE_SUFFIX, config);
        if (NullCheckUtil.isEmpty(service)) {
            return Boolean.FALSE;
        }
        //TODO REVIEW
        Integer port = KubernetesUtil.fetchPortFromService(service);

        if (NullCheckUtil.isEmpty(port)) {
            return Boolean.FALSE;
        }

        NetworkPolicy networkPolicy = KubernetesUtil.k8sNetworkPolicy(componentNodeInstance, blockedIps, port);

        if (NullCheckUtil.isEmpty(networkPolicy)) {
            return Boolean.FALSE;
        }

        if (networkPolicyService.applyNetworkPolicy(namespace, networkPolicy, config)) {
            return Boolean.TRUE;
        } else {
            networkRuleDAO.deleteByComponentNodeInstanceHexIdAndIp(componentNodeInstanceHexId, blockedIp);
            return Boolean.FALSE;
        }
    }

    public boolean applyNetworkPolicyWithNodeCheck(String componentNodeInstanceHexId, String blockedIp, String nodeName) {
        ComponentNodeInstance componentNodeInstance = componentNodeInstanceService.fetchComponentNodeInstanceByHexId(componentNodeInstanceHexId);
        if (componentNodeInstance == null) {
            return Boolean.FALSE;
        }

        ApplicationInstance applicationInstance = componentNodeInstance.getApplicationInstance();

        Config config = getKubernetesConfig(applicationInstance);
        if (NullCheckUtil.isEmpty(config)) {
            return Boolean.FALSE;
        }

        String namespace = KubernetesUtil.namespaceProvider(applicationInstance);

        PodList pods = podService.getPodsService(namespace, config);
        if (NullCheckUtil.isNotEmpty(pods) && NullCheckUtil.isNotEmpty(pods.getItems())) {
            List<Pod> podList = pods.getItems();
            for (Pod pd : podList) {
                if (pd.getMetadata().getName().contains(componentNodeInstanceHexId)
                        && pd.getSpec().getNodeName().equalsIgnoreCase(nodeName)) {
                    return networkPolicyApply(componentNodeInstanceHexId, blockedIp);
                }
            }
        }

        //TODO fix because it will be always success
        return Boolean.TRUE;
    }

    private List<String> retrieveBlockIps(String componentNodeInstanceHexId, String ip) {

        List<NetworkRule> networkRules =
                networkRuleDAO.findAllByComponentNodeInstanceHexId(componentNodeInstanceHexId);

        List<String> blockedIps = new ArrayList<>();
        if (NullCheckUtil.isNotEmpty(networkRules)) {
            if (networkRules.stream().anyMatch(x -> x.getIp().equalsIgnoreCase(ip))) {
                blockedIps = networkRules.stream().map(NetworkRule::getIp).collect(Collectors.toList());
            } else {
                blockedIps = networkRules.stream().map(NetworkRule::getIp).collect(Collectors.toList());
                NetworkRule networkRule = new NetworkRule();
                networkRule.setComponentNodeInstanceHexId(componentNodeInstanceHexId);
                networkRule.setIp(ip);
                networkRuleDAO.save(networkRule);
                blockedIps.add(ip);
            }
        } else {
            NetworkRule networkRule = new NetworkRule();
            networkRule.setComponentNodeInstanceHexId(componentNodeInstanceHexId);
            networkRule.setIp(ip);
            networkRuleDAO.save(networkRule);
            blockedIps.add(ip);
        }

        return blockedIps;
    }

    //TODO REVIEW / remove
    public void updateK8sDeploymentStatus() {
        List<ApplicationInstance> applicationInstances
                = applicationInstanceService.fetchApplicationInstancesByStatus(ApplicationInstance.ApplicationInstanceStatus.WAITING_ORCHESTRATOR.name());

        if (NullCheckUtil.isEmpty(applicationInstances)) {
            return;
        }

        applicationInstances = applicationInstances.stream().filter(this::isApplicationInstanceProviderSupported).collect(Collectors.toList());

        for (ApplicationInstance ai : applicationInstances) {
            if (NullCheckUtil.isNotEmpty(ai.getProvider()) && NullCheckUtil.isNotEmpty(ai.getProvider().getProviderType())) {
                final String providerName = ai.getProvider().getProviderType().getName();
                final String providerRawEndpoint = getCleanK8sEndpoint(ai);

                final Optional<String> maybeNamespace = provideNamespace(ai);
                if (!maybeNamespace.isPresent()) {
                    LOGGER.log(SEVERE, "Cannot retrieve namespace for application instance [id={0}]. Aborting deployment status update...",
                            ai.getApplicationInstanceID());
                    continue;
                }
                final String namespace = maybeNamespace.get();

                Config config = getKubernetesConfig(ai);
                if (NullCheckUtil.isNotEmpty(config) && deploymentLifecycleService.checkDeploymentLifecycle(namespace, config)) {
                    Map<String, String> podsIp = deploymentLifecycleService.getPodsIp(namespace, config);

                    ai.setStatus(ApplicationInstanceStatus.DEPLOYED.name());
                    applicationInstanceService.saveApplicationInstance(ai);

                    LOGGER.log(INFO, "Setting application instance status [id={0}] to {1}",
                            new Object[]{ai.getApplicationInstanceID(), ApplicationInstanceStatus.DEPLOYED});

                    if (NullCheckUtil.isNotEmpty(ai.getComponentNodeInstances())) {
                        for (ComponentNodeInstance cni : ai.getComponentNodeInstances()) {
                            ComponentNodeInstanceStatus componentNodeInstanceStatus = new ComponentNodeInstanceStatus();
                            componentNodeInstanceStatus.setReportedChange(
                                    OrchestratorChangedStatusNotification.ChangeType.GraphStatusChange.name());
                            componentNodeInstanceStatus.setLastModified(new Date());
                            componentNodeInstanceStatus.setDateCreated(new Date());
                            componentNodeInstanceStatus.setComponentNodeInstance(cni);
                            componentNodeInstanceStatus.setMessage("Component is healthy, up and running");
                            componentNodeInstanceStatus.setStatus("SUCCESS");
                            componentNodeInstanceStatus.setApplicationInstance(ai);
                            componentNodeInstanceService.saveCNIInstanceStatus(componentNodeInstanceStatus);

                            Boolean hasAccessInter = cni.getInterfaceInstances().stream().anyMatch(
                                    inter -> inter.getInterfaceType().equals(InterfaceType.ACCESS.name())
                            );

                            List<String> nodePorts = new ArrayList<>();

                            if (hasAccessInter) {

                                Service interService = serviceService.getServiceService(namespace, serviceNameProvider(cni), config);
                                cni.getInterfaceInstances().forEach(inter -> {
                                    if (inter.getInterfaceType().equals(InterfaceType.ACCESS.name())) {
                                        ServicePort servicePort = interService.getSpec().getPorts().stream()
                                                .filter(svcPort -> String.valueOf(svcPort.getPort()).equals(inter.getPort()))
                                                .findFirst()
                                                .orElse(null);
                                        if (servicePort != null) {
                                            nodePorts.add(String.valueOf(servicePort.getNodePort()));
                                            inter.setPort(String.valueOf(servicePort.getNodePort()));
                                            componentNodeInstanceService.saveCNIInterfaceInstance(inter);
                                        }
                                    }
                                });
                            }

                            //set IPs
                            if (NullCheckUtil.isNotEmpty(podsIp)) {
                                for (Map.Entry<String, String> entry : podsIp.entrySet()) {
                                    if (entry.getKey().contains(cni.getHexID().toLowerCase(Locale.ROOT))) {

                                        String componentsNetworkName = getComponentsNetworkName(providerName);
                                        saveComponentNodeInstanceIP(cni, ai, entry.getValue(), componentsNetworkName);

                                        if (hasAccessInter && providerRawEndpoint != null) {
                                            nodePorts.forEach(port -> {
                                                String networkName = "accessUrl" + port;
                                                String cniEndpoint = providerRawEndpoint + ":" + port;
                                                saveComponentNodeInstanceIP(cni, ai, cniEndpoint, networkName);
                                            });
                                        }

                                        LOGGER.log(INFO, "Setting component node instance \"{0}\" IP to \"{1}\" and network name to \"{2}\"",
                                                new Object[]{entry.getKey(), entry.getValue(), componentsNetworkName});
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }
    }

    private void saveComponentNodeInstanceIP(ComponentNodeInstance cni, ApplicationInstance ai, String ipValue, String networkName) {
        ComponentNodeInstanceIP componentNodeInstanceIP = new ComponentNodeInstanceIP();

        componentNodeInstanceIP.setComponentNodeInstance(cni);
        componentNodeInstanceIP.setApplicationInstance(ai);
        componentNodeInstanceIP.setIp(ipValue);

        final String componentsNetworkName = networkName;
        componentNodeInstanceIP.setNetwork(componentsNetworkName);

        final Date now = new Date();
        componentNodeInstanceIP.setDateCreated(now);
        componentNodeInstanceIP.setLastModified(now);

        componentNodeInstanceService.saveCNIIp(componentNodeInstanceIP);
    }

    protected String getComponentsNetworkName(String providerName) {
        if (providerName.equalsIgnoreCase(RAINBOW_KUBERNETES.name())) {
            return "RainbowMeshNet";
        } else if (providerName.equalsIgnoreCase(KUBERNETES.name()) || providerName.equalsIgnoreCase(FIVE_G_INDUCE_SLICE.name())) {
            return "KubernetesNet";
        } else {
            return null;
        }
    }

    protected List<ProviderName> getSupportedProviders() {
        return Arrays.asList(
                FIVE_G_INDUCE_SLICE,
                KUBERNETES,
                KUBERNETES_KNATIVE,
                RAINBOW_KUBERNETES
        );
    }

    private boolean isApplicationInstanceProviderSupported(ApplicationInstance applicationInstance) {
        final Provider provider = applicationInstance.getProvider();
        if (NullCheckUtil.isEmpty(provider)) {
            return false;
        }

        final ProviderType providerType = provider.getProviderType();
        if (NullCheckUtil.isEmpty(providerType)) {
            return false;
        }

        try {
            return getSupportedProviders().contains(providerType.getProviderName());

        } catch (IllegalArgumentException exception) {
            LOGGER.log(SEVERE, "Failed to convert application instance [id={0}] provider type \"{1}\" to provider name enum: {2}",
                    new Object[]{applicationInstance.getApplicationInstanceID(), providerType.getName(), exception.getMessage()});

            return false;
        }

    }

    public ClusterLabelsDto fetchClusterLabels(Long providerId){
        final Provider provider = providerService.findById(providerId);
        if (NullCheckUtil.isEmpty(provider)) {
            return null;
        } else if (NullCheckUtil.isEmpty(provider.getProviderType())) {
            return null;
        } else if ( !provider.getProviderType().getProviderName().equals(ProviderType.ProviderName.valueOf(String.valueOf(KUBERNETES)))
                && !provider.getProviderType().getProviderName().equals(ProviderType.ProviderName.valueOf(String.valueOf(KUBERNETES_KNATIVE))) ){
            return null;
        }

        Config config = KubernetesUtil.configProvider(provider);
        SortedSet<String> nodeLabels = KubernetesUtil.fetchNodeLabels(config, k8sConfig.k8sLabels());

        System.out.println(k8sConfig.k8sLabels().get(0));
        return new ClusterLabelsDto(nodeLabels);
    }

    //TODO REMOVE
    private String astridEnvsHash(ApplicationInstance applicationInstance, String componentNodeInstanceHexId) {
        String envListString = null;
        String namespace = KubernetesUtil.namespaceProvider(applicationInstance);
        KubernetesGraph kubernetesGraph = KubernetesUtil.applicationInstanceToK8s(applicationInstance, namespace, Boolean.FALSE, k8sResourcesEnable, null);

        List<Deployment> deploymentList = kubernetesGraph.getDeployment();
        for (Deployment dp : deploymentList) {
            if (dp.getMetadata().getName().contains(componentNodeInstanceHexId)
                    && NullCheckUtil.isNotEmpty(dp.getSpec()) && NullCheckUtil.isNotEmpty(dp.getSpec().getTemplate())
                    && NullCheckUtil.isNotEmpty(dp.getSpec().getTemplate().getSpec())
                    && NullCheckUtil.isNotEmpty(dp.getSpec().getTemplate().getSpec().getContainers())
                    && NullCheckUtil.isNotEmpty(dp.getSpec().getTemplate().getSpec().getContainers().get(0))
                    && NullCheckUtil.isNotEmpty(dp.getSpec().getTemplate().getSpec().getContainers().get(0).getEnv())) {

                List<EnvVar> envVarList = dp.getSpec().getTemplate().getSpec().getContainers().get(0).getEnv();

                Map<String, String> envVariables = new HashMap<>();
                envVariables = envVarList.stream().collect(Collectors.toMap(EnvVar::getName, EnvVar::getValue));
                //sort the envs
                //TreeMap<String, String> envVariablesSorted = new TreeMap<>();
                //envVariablesSorted.putAll(envVariables);

                JsonArray envList = new JsonArray();
                if (NullCheckUtil.isNotEmpty(envVariables)) {
                    for (String envKey : envVariables.keySet()) {
                        JsonObject env = new JsonObject();
                        String envValue = envVariables.get(envKey);
                        env.addProperty(envKey, envValue);
                        envList.add(env);
                    }
                }

                envListString = envList.toString();

            }
        }

        return Encryption.encrypt(envListString, secretToken);
    }

    //TODO REMOVE
    private String astridImageHash(ApplicationInstance applicationInstance, String componentNodeInstanceHexId) {
        String imageHash = null;

        Config config = getKubernetesConfig(applicationInstance);
        if (NullCheckUtil.isEmpty(config)) {
            return null;
        }

        String namespace = KubernetesUtil.namespaceProvider(applicationInstance);

        PodList podList = podService.getPodsService(namespace, config);
        List<Pod> pods = podList.getItems();
        Pod pod = null;
        for (Pod pd : pods) {
            if (pd.getMetadata().getName().contains(componentNodeInstanceHexId)) {
                pod = pd;
                break;
            }
        }

        if (NullCheckUtil.isNotEmpty(pod.getStatus()) && NullCheckUtil.isNotEmpty(pod.getStatus().getContainerStatuses())) {
            String imageID = pod.getStatus().getContainerStatuses().get(0).getImageID();
            imageHash = imageID.substring(imageID.lastIndexOf(":") + 1);
        }

        return imageHash;
    }


    //TODO REVIEW OR REMOVE ATTESTATION
    public void attestationCheck(ComponentNodeInstance cni, SecurityConfigurationResult scr) {

        String imageHashFromK8s = fetchImageHash(cni.getHexID());
        String envHashFromK8s = fetchEnvHash(cni.getHexID());

        String imageHashFromDatabase = "";
        String envHashFromDatabase = "";

        List<ComponentNodeInstanceHash> componentNodeInstanceHashList = cni.getComponentNodeInstanceHashList();
        if (NullCheckUtil.isNotEmpty(componentNodeInstanceHashList)) {
            for (ComponentNodeInstanceHash cn : componentNodeInstanceHashList) {
                if (cn.getType().equals(ComponentNodeInstanceHash.HashType.DOCKER_IMAGE.name())) {
                    imageHashFromDatabase = cn.getValue();
                } else if (cn.getType().equals(ComponentNodeInstanceHash.HashType.DOCKER_ENV.name())) {
                    envHashFromDatabase = cn.getValue();
                }
            }
        }

        if (imageHashFromDatabase.equals(imageHashFromK8s) && envHashFromDatabase.equals(envHashFromK8s)) {
            scr.setStatus(SecurityConfigurationResult.SecurityConfigurationResultStatus.COMPLETE.name());
            scr.setDescription(ComponentNodeInstanceHash.HashType.DOCKER_IMAGE.name() + ", "
                    + ComponentNodeInstanceHash.HashType.DOCKER_ENV.name() + " (OK)");
        } else if (imageHashFromDatabase.equals(imageHashFromK8s) && !envHashFromDatabase.equals(envHashFromK8s)) {
            scr.setStatus(SecurityConfigurationResult.SecurityConfigurationResultStatus.ERROR_OCCURRED.name());
            scr.setDescription(ComponentNodeInstanceHash.HashType.DOCKER_ENV.name() + " (FAILED)");
        } else if (!imageHashFromDatabase.equals(imageHashFromK8s) && envHashFromDatabase.equals(envHashFromK8s)) {
            scr.setStatus(SecurityConfigurationResult.SecurityConfigurationResultStatus.ERROR_OCCURRED.name());
            scr.setDescription(ComponentNodeInstanceHash.HashType.DOCKER_IMAGE.name() + " (FAILED)");
        } else {
            scr.setStatus(SecurityConfigurationResult.SecurityConfigurationResultStatus.ERROR_OCCURRED.name());
            scr.setDescription(ComponentNodeInstanceHash.HashType.DOCKER_IMAGE.name() + ", "
                    + ComponentNodeInstanceHash.HashType.DOCKER_ENV.name() + " (FAILED)");
        }

        securityConfigurationService.saveSecurityConfigurationResult(scr);
    }

    //attestation
    private String fetchEnvHash(String componentNodeInstanceHexId) {
        ComponentNodeInstance componentNodeInstance = componentNodeInstanceService.fetchComponentNodeInstanceByHexId(componentNodeInstanceHexId);
        if (componentNodeInstance == null) {
            return null;
        }

        ApplicationInstance applicationInstance = componentNodeInstance.getApplicationInstance();

        Config config = getKubernetesConfig(applicationInstance);
        if (NullCheckUtil.isEmpty(config)) {
            return null;
        }

        String namespace = KubernetesUtil.namespaceProvider(applicationInstance);

        PodList podList = podService.getPodsService(namespace, config);
        List<Pod> pods = podList.getItems();
        Pod pod = null;
        for (Pod pd : pods) {
            if (pd.getMetadata().getName().contains(componentNodeInstanceHexId)) {
                pod = pd;
                break;
            }
        }

        //envs
        Map<String, String> envVariables = new HashMap<>();
        if (NullCheckUtil.isNotEmpty(pod.getSpec()) && NullCheckUtil.isNotEmpty(pod.getSpec().getContainers())) {
            //TODO iterate the list if multiple containers, for now get the first
            if (NullCheckUtil.isNotEmpty(pod.getSpec().getContainers().get(0).getEnv())) {
                List<EnvVar> envVars = pod.getSpec().getContainers().get(0).getEnv();
                envVariables = envVars.stream().collect(Collectors.toMap(EnvVar::getName, EnvVar::getValue));
                //sort the envs
                //TreeMap<String, String> envVariablesSorted = new TreeMap<>();
                //envVariablesSorted.putAll(envVariables);
            }
        }

        JsonArray envList = new JsonArray();
        if (NullCheckUtil.isNotEmpty(envVariables)) {
            for (String envKey : envVariables.keySet()) {
                JsonObject env = new JsonObject();
                String envValue = envVariables.get(envKey);
                env.addProperty(envKey, envValue);
                envList.add(env);
            }
        }

        return Encryption.encrypt(envList.toString(), secretToken);
    }

    //attestation
    private String fetchImageHash(String componentNodeInstanceHexId) {
        ComponentNodeInstance componentNodeInstance = componentNodeInstanceService.fetchComponentNodeInstanceByHexId(componentNodeInstanceHexId);
        if (componentNodeInstance == null) {
            return null;
        }
        ApplicationInstance applicationInstance = componentNodeInstance.getApplicationInstance();
        Config config = getKubernetesConfig(applicationInstance);
        if (NullCheckUtil.isEmpty(config)) {
            return null;
        }
        String namespace = KubernetesUtil.namespaceProvider(applicationInstance);
        PodList podList = podService.getPodsService(namespace, config);
        List<Pod> pods = podList.getItems();
        Pod pod = null;
        for (Pod pd : pods) {
            if (pd.getMetadata().getName().contains(componentNodeInstanceHexId)) {
                pod = pd;
                break;
            }
        }
        String hashImage = null;
        if (NullCheckUtil.isNotEmpty(pod.getStatus()) && NullCheckUtil.isNotEmpty(pod.getStatus().getContainerStatuses())) {
            String imageID = pod.getStatus().getContainerStatuses().get(0).getImageID();
            hashImage = imageID.substring(imageID.lastIndexOf(":") + 1);
        }
        return hashImage;
    }
    //TODO END OF ATTESTATION

}
