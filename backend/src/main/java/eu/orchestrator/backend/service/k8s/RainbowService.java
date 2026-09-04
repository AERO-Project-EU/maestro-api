package eu.orchestrator.backend.service.k8s;

import eu.orchestrator.backend.service.k8s.resources.rainbow.ElasticityRainbowService;
import eu.orchestrator.backend.util.KubernetesUtil;
import eu.orchestrator.backend.util.RainbowUtil;
import eu.orchestrator.backend.util.Util;
import eu.orchestrator.common.util.NullCheckUtil;
import eu.orchestrator.repository.dao.ApplicationInstanceDAO;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.transfer.entities.rainbowk8s.ElasticityStrategiesDto;
import eu.orchestrator.transfer.entities.rainbowk8s.ServiceGraph;
import eu.orchestrator.backend.service.k8s.resources.NamespaceService;
import eu.orchestrator.backend.service.k8s.resources.SecretService;
import eu.orchestrator.backend.service.k8s.resources.rainbow.ServiceGraphService;

import io.fabric8.kubernetes.client.Config;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Optional;
import jakarta.transaction.Transactional;


@org.springframework.stereotype.Service
@Transactional
public class RainbowService {

    private static final Logger logger = Logger.getLogger(RainbowService.class.getName());

    private static final String SECRET_SUFFIX = "-secret";

    @Autowired
    ApplicationInstanceDAO applicationInstanceDAO;

    @Autowired
    NamespaceService namespaceService;

    @Autowired
    SecretService secretService;

    @Autowired
    ServiceGraphService serviceGraphService;

    @Autowired
    KubernetesService kubernetesService;

    @Autowired
    ElasticityRainbowService elasticityRainbowService;

    @Value("${token.signer.secret}")
    private String secretToken;


    public boolean rainbowDeployment(Long applicationInstanceID, Boolean networkModeHost) {

        Optional<ApplicationInstance> applicationInstanceOptional = applicationInstanceDAO.findById(applicationInstanceID);
        ApplicationInstance applicationInstance;
        if (applicationInstanceOptional.isPresent()) {
            applicationInstance = applicationInstanceOptional.get();
        } else {
            return Boolean.FALSE;
        }

        Config config = KubernetesUtil.configProvider(applicationInstance);
        if (NullCheckUtil.isEmpty(config)) {
            return Boolean.FALSE;
        }

        //namespace creation
        String namespace = KubernetesUtil.namespaceProvider(applicationInstance);
        if (!namespaceService.createNamespace(namespace, config)) {
            return Boolean.FALSE;
        }

        //secrets creation
        boolean secretsCreationSuccess = Boolean.TRUE;
        if (NullCheckUtil.isNotEmpty(applicationInstance.getComponentNodeInstances())) {
            for (ComponentNodeInstance cni : applicationInstance.getComponentNodeInstances()) {
                if (NullCheckUtil.isNotEmpty(cni.getComponentNode().getComponent())
                        //&& Boolean.TRUE.equals(cni.getComponentNode().getComponent().getDockerCustomRegistry())
                        && NullCheckUtil.isNotEmpty(cni.getComponentNode().getComponent().getDockerRegistry())
                        && NullCheckUtil.isNotEmpty(cni.getComponentNode().getComponent().getDockerUsername())
                        && NullCheckUtil.isNotEmpty(cni.getComponentNode().getComponent().getDockerPassword())
                ) {
                    if (!secretService.createSecretService(namespace, RainbowUtil.nodeNameProvider(cni) + SECRET_SUFFIX,
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

        //rollback
        if (!secretsCreationSuccess) {
            kubernetesService.secretsUndeployment(applicationInstance, namespace, config);
            kubernetesService.namespaceUndeployment(namespace, config);
            return Boolean.FALSE;
        }

        //service graph creation
        ServiceGraph serviceGraph = RainbowUtil.applicationInstanceToServiceGraph(applicationInstance, networkModeHost);
        String serviceGraphYaml = RainbowUtil.serviceGraphToYaml(serviceGraph);
        if (!serviceGraphService.createOrReplaceServiceGraph(namespace, serviceGraphYaml, config)) {
            //rollback
            kubernetesService.secretsUndeployment(applicationInstance, namespace, config);
            kubernetesService.namespaceUndeployment(namespace, config);
            return Boolean.FALSE;
        }

        return Boolean.TRUE;
    }


    public boolean rainbowUndeployment(Long applicationInstanceID) {

        Optional<ApplicationInstance> applicationInstanceOptional = applicationInstanceDAO.findById(applicationInstanceID);
        ApplicationInstance applicationInstance;
        if (applicationInstanceOptional.isPresent()) {
            applicationInstance = applicationInstanceOptional.get();
        } else {
            return Boolean.FALSE;
        }

        Config config = KubernetesUtil.configProvider(applicationInstance);
        if (NullCheckUtil.isEmpty(config)) {
            return Boolean.FALSE;
        }

        String namespace = KubernetesUtil.namespaceProvider(applicationInstance);

        if (!serviceGraphUndeployment(namespace, namespace, config)) {
            return Boolean.FALSE;
        }
        if (!kubernetesService.secretsUndeployment(applicationInstance, namespace, config)) {
            return Boolean.FALSE;
        }
        if (!kubernetesService.namespaceUndeployment(namespace, config)) {
            return Boolean.FALSE;
        }

        return Boolean.TRUE;

    }

    public ServiceGraph getServiceGraph(Long applicationInstanceID) {

        Optional<ApplicationInstance> applicationInstanceOptional = applicationInstanceDAO.findById(applicationInstanceID);
        ApplicationInstance applicationInstance;
        if (applicationInstanceOptional.isPresent()) {
            applicationInstance = applicationInstanceOptional.get();
        } else {
            return null;
        }
        logger.info("Backend - Rainbow Service - Application instance HEX ID: " + applicationInstance.getHexID());

        Config config = KubernetesUtil.configProvider(applicationInstance);
        if (NullCheckUtil.isEmpty(config)) {
            return null;
        }
        logger.info("Backend - Rainbow Service - Kubernetes config ok");

        String namespace = KubernetesUtil.namespaceProvider(applicationInstance);
        logger.info("Backend - Rainbow Service - Kubernetes namespace : " + namespace);
        return serviceGraphService.getServiceGraph(namespace, config);
    }

    public void applyServiceGraph(Long applicationInstanceId, ServiceGraph serviceGraph) {
        Optional<ApplicationInstance> applicationInstanceOptional = applicationInstanceDAO.findById(applicationInstanceId);
        ApplicationInstance applicationInstance;
        if (applicationInstanceOptional.isPresent()) {
            applicationInstance = applicationInstanceOptional.get();
            Config config = KubernetesUtil.configProvider(applicationInstance);
            if (!NullCheckUtil.isEmpty(config)) {
                String namespace = KubernetesUtil.namespaceProvider(applicationInstance);
                String serviceGraphYaml = RainbowUtil.serviceGraphToYaml(serviceGraph);
                serviceGraphService.createOrReplaceServiceGraph(namespace, serviceGraphYaml, config);
            }
        }
    }

    public ElasticityStrategiesDto getElasticityStrategies(Long applicationInstanceID) {
        Optional<ApplicationInstance> applicationInstanceOptional = applicationInstanceDAO.findById(applicationInstanceID);
        ApplicationInstance applicationInstance;
        if (applicationInstanceOptional.isPresent()) {
            applicationInstance = applicationInstanceOptional.get();
        } else {
            return null;
        }

        Config config = KubernetesUtil.configProvider(applicationInstance);
        if (NullCheckUtil.isEmpty(config)) {
            return null;
        }

        return elasticityRainbowService.fetchElasticityStrategiesRainbow(config);
    }

    private boolean serviceGraphUndeployment(String namespace, String name, Config config) {
        return serviceGraphService.deleteServiceGraph(namespace, name, config);
    }

}
