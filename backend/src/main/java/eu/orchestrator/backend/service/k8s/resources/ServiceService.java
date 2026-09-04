package eu.orchestrator.backend.service.k8s.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

@org.springframework.stereotype.Service
public class ServiceService {

    private static final Logger logger = LoggerFactory.getLogger(ServiceService.class);

    public boolean createServiceService(String namespace, Service service, Config config) {
        try (final KubernetesClient client = new DefaultKubernetesClient(config)) {

            //toYaml(service);

            client.services().inNamespace(namespace).create(service);
            return Boolean.TRUE;
        } catch (Exception e) {
            logger.error("Create Deployment error: {}", e.getMessage());
            return Boolean.FALSE;
        }
    }

    public boolean deleteServiceService(String namespace, String name, Config config) {
        try (final KubernetesClient client = new DefaultKubernetesClient(config)) {
            return !client.services().inNamespace(namespace).withName(name).delete().isEmpty();
        } catch (Exception e) {
            logger.error("Delete Deployment error: {}", e.getMessage());
            return Boolean.FALSE;
        }
    }

    public Service getServiceService(String namespace, String name, Config config) {
        try (final KubernetesClient client = new DefaultKubernetesClient(config)) {
            return client.services().inNamespace(namespace).withName(name).get();
        } catch (Exception e) {
            logger.error("Delete Deployment error: {}", e.getMessage());
            return null;
        }
    }

    private void toYaml(Service service) {
        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        try {
            om.writeValue(new File("/tmp/" + service.getMetadata().getName() + ".yaml"), service);
        } catch (Exception e) {
            logger.error("YAML writer error: ", e);
        }
    }
}
