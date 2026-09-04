package eu.orchestrator.backend.service.k8s.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

@org.springframework.stereotype.Service
public class DeploymentK8sService {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentK8sService.class);

    public boolean createDeploymentService(String namespace, Deployment deployment, Config config) {
        try (final KubernetesClient client = new DefaultKubernetesClient(config)) {

            //toYaml(deployment);

            client.apps().deployments().inNamespace(namespace).create(deployment);
            return Boolean.TRUE;
        } catch (Exception e) {
            logger.error("Create Deployment error: {}", e.getMessage());
            return Boolean.FALSE;
        }
    }

    public boolean deleteDeploymentService(String namespace, String name, Config config) {
        try (final KubernetesClient client = new DefaultKubernetesClient(config)) {
            return !client.apps().deployments().inNamespace(namespace).withName(name).delete().isEmpty();
        } catch (Exception e) {
            logger.error("Delete Deployment error: {}", e.getMessage());
            return Boolean.FALSE;
        }
    }

    private void toYaml(Deployment deployment) {
        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        try {
            om.writeValue(new File("/tmp/" + deployment.getMetadata().getName() + ".yaml"), deployment);
        } catch (Exception e) {
            logger.error("YAML writer error: ", e);
        }
    }
}
