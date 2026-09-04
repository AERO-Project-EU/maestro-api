package eu.orchestrator.backend.service.k8s.resources;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@org.springframework.stereotype.Service
public class NamespaceService {

    private static final Logger logger = LoggerFactory.getLogger(NamespaceService.class);

    public boolean createNamespace(String name, Config config) {
        try (final KubernetesClient client = new DefaultKubernetesClient(config)) {
            Namespace ns = new NamespaceBuilder().withNewMetadata().withName(name).endMetadata().build();
            client.namespaces().create(ns);
            return Boolean.TRUE;
        } catch (Exception e) {
            logger.error("Create Namespace error: {}", e.getMessage());
            return Boolean.FALSE;
        }

    }

    public boolean doesNamespaceExist(String name, Config config) {
        try (final KubernetesClient client = new DefaultKubernetesClient(config)) {
            // Indirectly check whether namespace exists, as the supplied config might not have access to retrieve namespaces.
            client.pods().inNamespace(name).list();
            return true;
        } catch (Exception exception) {
            logger.error(String.format("Failed to retrieve (pods within) namespace: \"%s\"", name), exception);
            return false;
        }
    }

    public boolean deleteNamespace(String name, Config config) {
        try (final KubernetesClient client = new DefaultKubernetesClient(config)) {
            return !client.namespaces().withName(name).delete().isEmpty();
        } catch (Exception e) {
            logger.error("Delete Namespace error: {}", e.getMessage());
            return Boolean.FALSE;
        }
    }
}
