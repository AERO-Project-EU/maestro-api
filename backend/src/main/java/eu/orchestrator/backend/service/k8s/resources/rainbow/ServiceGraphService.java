package eu.orchestrator.backend.service.k8s.resources.rainbow;

import eu.orchestrator.backend.util.RainbowUtil;
import eu.orchestrator.transfer.entities.rainbowk8s.ServiceGraph;


import com.google.gson.Gson;
import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.utils.Serialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

import org.springframework.stereotype.Service;


@Service
public class ServiceGraphService {

    private static final Logger logger = LoggerFactory.getLogger(ServiceGraphService.class);

    private static final String RAINBOW_GROUP = "fogapps.k8s.rainbow-h2020.eu";
    private static final String RAINBOW_SERVICE_GRAPHS = "servicegraphs";
    private static final String NAMESPACED = "Namespaced";
    private static final String VERSION = "v1";


    public boolean createOrReplaceServiceGraph(String namespace, String serviceGraphYaml, Config config) {
        try (final KubernetesClient client = new DefaultKubernetesClient(config)) {

            CustomResourceDefinitionContext crdContext = new CustomResourceDefinitionContext.Builder()
                    .withGroup(RAINBOW_GROUP)
                    .withPlural(RAINBOW_SERVICE_GRAPHS)
                    .withScope(NAMESPACED)
                    .withVersion(VERSION)
                    .build();

            InputStream targetStream = new ByteArrayInputStream(serviceGraphYaml.getBytes());
            client.genericKubernetesResources(crdContext).inNamespace(namespace).load(targetStream).createOrReplace();
            return Boolean.TRUE;

        } catch (Exception e) {
            logger.error("Create ServiceGraph error: {}", e.getMessage());
            return Boolean.FALSE;
        }
    }

    public boolean deleteServiceGraph(String namespace, String name, Config config) {
        try (final KubernetesClient client = new DefaultKubernetesClient(config)) {

            CustomResourceDefinitionContext crdContext = new CustomResourceDefinitionContext.Builder()
                    .withGroup(RAINBOW_GROUP)
                    .withPlural(RAINBOW_SERVICE_GRAPHS)
                    .withScope(NAMESPACED)
                    .withVersion(VERSION)
                    .build();

            return !client.genericKubernetesResources(crdContext).inNamespace(namespace).withName(name).delete().isEmpty();
        } catch (Exception ex) {
            logger.error("Create ServiceGraph errror: {}", ex.getMessage());
            return Boolean.FALSE;
        }
    }

    public ServiceGraph getServiceGraph(String namespace, Config config) {
        try (final KubernetesClient client = new DefaultKubernetesClient(config)) {
            logger.info("Backend - ServiceGraphService - Start getServiceGraph");
            CustomResourceDefinitionContext crdContext = new CustomResourceDefinitionContext.Builder()
                    .withGroup(RAINBOW_GROUP)
                    .withPlural(RAINBOW_SERVICE_GRAPHS)
                    .withScope(NAMESPACED)
                    .withVersion(VERSION)
                    .build();

            GenericKubernetesResource resource = client.genericKubernetesResources(crdContext)
                    .inNamespace(namespace).withName(namespace).get();
            Map<String, Object> crd = new Gson().fromJson(Serialization.asJson(resource), Map.class);
            logger.info("Backend - ServiceGraphService - getServiceGraph Fetched");
            return RainbowUtil.k8sCrdToServiceGraph(crd);
        } catch (Exception ex) {
            logger.error("Fetch Service Graph error: {}", ex.getMessage());
            return null;
        }
    }

}
