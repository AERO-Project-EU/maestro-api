package eu.orchestrator.backend.service.k8s.resources;

import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@org.springframework.stereotype.Service
public class PodService {

    private static final Logger logger = LoggerFactory.getLogger(PodService.class);

    public PodList getPodsService(String namespace, Config config) {
        try (final KubernetesClient client = new DefaultKubernetesClient(config)) {
            return client.pods().inNamespace(namespace).list();

        } catch (Exception e) {
            logger.error("Create Deployment error: {}", e.getMessage());
            return null;
        }
    }

    public PodList getPodsByComponentService(String namespace, String componentName, Config config) {
        try (final KubernetesClient client = new DefaultKubernetesClient(config)) {
            PodList podList = client.pods().inNamespace(namespace).withLabel(componentName).list();
            return podList;
        } catch (Exception e) {
            logger.error("Fetch pods error: {}", e.getMessage());
            return null;
        }
    }
}
