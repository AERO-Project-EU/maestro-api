package eu.orchestrator.backend.service.k8s;


import eu.orchestrator.backend.config.K8sConfig;
import eu.orchestrator.backend.service.applicationinstance.ApplicationInstanceService;
import eu.orchestrator.backend.service.applicationinstance.ComponentNodeInstanceService;
import eu.orchestrator.backend.service.k8s.resources.DeploymentKnativeService;
import eu.orchestrator.backend.service.k8s.resources.ServiceService;
import eu.orchestrator.backend.service.model.KubernetesGraph;
import eu.orchestrator.backend.util.KnativeUtil;
import eu.orchestrator.backend.util.KubernetesUtil;
import eu.orchestrator.common.util.NullCheckUtil;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.repository.domain.ComponentNodeInstanceAffinity;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.client.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import jakarta.transaction.Transactional;

import static java.util.logging.Level.SEVERE;

@org.springframework.stereotype.Service
@Transactional
public class KnativeService {

    private static final Logger LOGGER = Logger.getLogger(KubernetesService.class.getName());
    private static final String SERVICE_SUFFIX = "-service";
    private static final String DEPLOYMENT_SUFFIX = "-deployment";

    @Autowired
    private ApplicationInstanceService applicationInstanceService;

    @Autowired
    private ComponentNodeInstanceService componentNodeInstanceService;

    @Autowired
    private DeploymentKnativeService deploymentKnativeService;

    @Autowired
    private ServiceService serviceService;

    @Value("${kubernetes.consider-resources}")
    private boolean k8sResourcesEnable;

    @Autowired
    K8sConfig k8sConfig;

    @Autowired
    KubernetesService kubernetesService;


    public boolean knativeDeployment(Long applicationInstanceID, Boolean networkModeHost) {

        ApplicationInstance applicationInstance = applicationInstanceService.fetchApplicationInstanceById(applicationInstanceID);
        if (null == applicationInstance) {
            LOGGER.log(SEVERE, "Aborting Knative deployment as application instance [id={0}] was not found", applicationInstanceID);
            return Boolean.FALSE;
        }

        Config config = kubernetesService.getKubernetesConfig(applicationInstance);
        if (NullCheckUtil.isEmpty(config)) {
            LOGGER.log(SEVERE, "Aborting Knative deployment of application instance [id={0}]: failure creating K8s client config", applicationInstanceID);
            return Boolean.FALSE;
        }

        // Namespace creation
        final Optional<String> namespaceMaybe = kubernetesService.provideNamespace(applicationInstance);
        if (!namespaceMaybe.isPresent()) {
            LOGGER.log(SEVERE, "Aborting Knative deployment of application instance [id={0}]: failure generating namespace name", applicationInstanceID);
            return Boolean.FALSE;
        }
        final String namespace = namespaceMaybe.get();

        if (!kubernetesService.namespaceDeployment(namespace, config)) {
            LOGGER.log(SEVERE, "Aborting Knative deployment of application instance [id={0}]: failure creating namespace", applicationInstanceID);
            return Boolean.FALSE;
        }

        //secrets creation
        if (!kubernetesService.secretDeployment(applicationInstance, namespace, config)) {
            LOGGER.log(SEVERE, "Aborting Knative deployment of application instance [id={0}]: failure creating secrets", applicationInstanceID);
            kubernetesService.secretsUndeployment(applicationInstance, namespace, config);
            kubernetesService.namespaceUndeployment(namespace, config);
            return Boolean.FALSE;
        }

        Map<Long, ComponentNodeInstanceAffinity> cniAffinities = componentNodeInstanceService.fetchCNIAffinityMapByApplicationInstance(applicationInstance);
        KubernetesGraph kubernetesGraph = KnativeUtil.applicationInstanceToKnative(applicationInstance, namespace, networkModeHost, k8sResourcesEnable, cniAffinities);
        if (!knativeGraphDeployment(kubernetesGraph, namespace, config)) {
            LOGGER.log(SEVERE, "Aborting Knative deployment of application instance [id={0}]: failure deploying K8s graph", applicationInstanceID);
            knativeGraphUnDeployment(applicationInstance, namespace, config);
            kubernetesService.secretsUndeployment(applicationInstance, namespace, config);
            kubernetesService.namespaceUndeployment(namespace, config);
        }

        return Boolean.TRUE;
    }

    protected boolean knativeGraphDeployment(KubernetesGraph kubernetesGraph, String namespace, Config config) {

        if (NullCheckUtil.isNotEmpty(kubernetesGraph.getKnativeService())) {
            for (io.fabric8.knative.serving.v1.Service sv : kubernetesGraph.getKnativeService()) {
                if (!deploymentKnativeService.createKnativeDeploymentService(namespace, sv, config)) {
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

    public boolean knativeUndeployment(Long applicationInstanceID) {

        ApplicationInstance applicationInstance = applicationInstanceService.fetchApplicationInstanceById(applicationInstanceID);
        if (null == applicationInstance) {
            return Boolean.FALSE;
        }

        Config config = kubernetesService.getKubernetesConfig(applicationInstance);
        if (NullCheckUtil.isEmpty(config)) {
            return Boolean.FALSE;
        }

        boolean undeploymentSuccess = Boolean.TRUE;
        String namespace = KubernetesUtil.namespaceProvider(applicationInstance);

        if (!knativeGraphUnDeployment(applicationInstance, namespace, config)) {
            undeploymentSuccess = Boolean.FALSE;
        }
        if (!kubernetesService.secretsUndeployment(applicationInstance, namespace, config)) {
            undeploymentSuccess = Boolean.FALSE;
        }
        if (!kubernetesService.namespaceUndeployment(namespace, config)) {
            undeploymentSuccess = Boolean.FALSE;
        }

        return undeploymentSuccess;
    }

    protected boolean knativeGraphUnDeployment(ApplicationInstance applicationInstance, String namespace, Config config) {

        boolean knativeGraphUnDeploymentSuccess = Boolean.TRUE;
        if (NullCheckUtil.isNotEmpty(applicationInstance.getComponentNodeInstances())) {
            for (ComponentNodeInstance cni : applicationInstance.getComponentNodeInstances()) {
                String cname = KubernetesUtil.componentInstanceNameProvider(cni);
                if (!deploymentKnativeService.deleteKnativeDeploymentService(namespace, cname + DEPLOYMENT_SUFFIX, config)) {
                    knativeGraphUnDeploymentSuccess = Boolean.FALSE;
                }
                if (!serviceService.deleteServiceService(namespace, cname + SERVICE_SUFFIX, config)) {
                    knativeGraphUnDeploymentSuccess = Boolean.FALSE;
                }
            }
        }

        return knativeGraphUnDeploymentSuccess;
    }

    public boolean updateKnativeServiceScalingProperties(ApplicationInstance applicationInstance, String knativeServiceName, Map<String, String> scalingPropertiesAnnotations) {
        Config config = kubernetesService.getKubernetesConfig(applicationInstance);
        if (NullCheckUtil.isEmpty(config)) {
            LOGGER.log(SEVERE, "Aborting Updating Knative Service Scaling Properties: failure retrieving K8s client config");
            return Boolean.FALSE;
        }

        // Get Namespace
        final Optional<String> namespaceMaybe = kubernetesService.provideNamespace(applicationInstance);
        if (!namespaceMaybe.isPresent()) {
            LOGGER.log(SEVERE, "Aborting Updating Knative Service Scaling Properties: failure retrieving namespace name");
            return Boolean.FALSE;
        }
        final String namespace = namespaceMaybe.get();

        return deploymentKnativeService.updateKnativeDeploymentServiceScalingProperties(namespace, knativeServiceName, scalingPropertiesAnnotations, config);
    }
}
