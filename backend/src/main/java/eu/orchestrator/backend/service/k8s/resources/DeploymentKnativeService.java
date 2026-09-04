package eu.orchestrator.backend.service.k8s.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.fabric8.knative.client.DefaultKnativeClient;
import io.fabric8.knative.client.KnativeClient;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

@org.springframework.stereotype.Service
public class DeploymentKnativeService {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentKnativeService.class);

    public boolean createKnativeDeploymentService(String namespace, io.fabric8.knative.serving.v1.Service knativeService, Config config) {
        try (final KnativeClient client = new DefaultKnativeClient(config)) {

            //toYaml(deployment);

            client.services().inNamespace(namespace).create(knativeService);
            return Boolean.TRUE;
        } catch (Exception e) {
            logger.error("Create Knative Deployment error: {}", e.getMessage());
            return Boolean.FALSE;
        }
    }

    public boolean updateKnativeDeploymentServiceScalingProperties(String namespace, String knativeServiceName, Map<String, String> annotations, Config config) {
        try (final KnativeClient client = new DefaultKnativeClient(config)) {

            // Fetch the Knative Service
            io.fabric8.knative.serving.v1.Service knativeService = client.services().inNamespace(namespace).withName(knativeServiceName).get();

            // Check if knative service really exists
            if (knativeService != null) {

                // Modify the autoscaling annotations
                knativeService.getSpec().getTemplate().getMetadata().getAnnotations().putAll(annotations);

                // Update the knative service
                client.services().inNamespace(namespace).withName(knativeServiceName).replace(knativeService);

                return Boolean.TRUE;
            } else {
                throw new IllegalArgumentException("Knative Deployment service " + knativeServiceName + " not found");
            }
        } catch (Exception e) {
            logger.error("Update Knative Deployment error: {}", e.getMessage());
            return Boolean.FALSE;
        }
    }

    public boolean deleteKnativeDeploymentService(String namespace, String name, Config config) {
        try (final KnativeClient client = new DefaultKnativeClient(config)) {
            return !client.services().inNamespace(namespace).withName(name).delete().isEmpty();
        } catch (Exception e) {
            logger.error("Delete Knative Deployment error: {}", e.getMessage());
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
