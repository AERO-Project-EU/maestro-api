package eu.orchestrator.backend.service.k8s.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collections;

@org.springframework.stereotype.Service
public class SecretService {

    private static final Logger logger = LoggerFactory.getLogger(SecretService.class);

    private static final String KUBERNETES_APP_SECRET_DOCKER_CONFIG = "kubernetes.io/dockerconfigjson";
    private static final String SECRET_DOCKER_CONFIG_JSON = ".dockerconfigjson";


    public boolean createSecretService(String namespace, String name, String dockerConfig, Config config) {
        try (final KubernetesClient client = new DefaultKubernetesClient(config)) {
            Secret secret = new SecretBuilder()
                    .withNewMetadata().withName(name).endMetadata()
                    .withType(KUBERNETES_APP_SECRET_DOCKER_CONFIG)
                    .withData(
                            Collections.singletonMap(SECRET_DOCKER_CONFIG_JSON, dockerConfig))
                    .build();

            //toYaml(secret,name);

            client.secrets().inNamespace(namespace).create(secret);
            return Boolean.TRUE;

        } catch (Exception e) {
            logger.error("Create Secret error: {}", e.getMessage());
            return Boolean.FALSE;
        }
    }

    public boolean deleteSecretService(String namespace, String name, Config config) {
        try (final KubernetesClient client = new DefaultKubernetesClient(config)) {
            return !client.secrets().inNamespace(namespace).withName(name).delete().isEmpty();
        } catch (Exception e) {
            logger.error("Delete Secret error: {}", e.getMessage());
            return Boolean.FALSE;
        }
    }

    private void toYaml(Secret secret, String name) {
        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        try {
            om.writeValue(new File("/tmp/" + name + ".yaml"), secret);
        } catch (Exception e) {
            logger.error("YAML writer error: ", e);
        }
    }
}
