package eu.orchestrator.backend.service.k8s.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.fabric8.kubernetes.api.model.networking.v1.NetworkPolicy;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

@org.springframework.stereotype.Service
public class NetworkPolicyService {

    private static final Logger logger = LoggerFactory.getLogger(NetworkPolicyService.class);

    public boolean applyNetworkPolicy(String namespace, NetworkPolicy networkPolicy, Config config) {
        try (final KubernetesClient client = new DefaultKubernetesClient(config)) {

            //toYaml(networkPolicy);

            client.network().v1().networkPolicies().inNamespace(namespace).createOrReplace(networkPolicy);
            return Boolean.TRUE;
        } catch (Exception e) {
            logger.error("Apply NetworkPolicy error: {}", e.getMessage());
            return Boolean.FALSE;
        }
    }

    private void toYaml(NetworkPolicy networkPolicy) {
        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        try {
            om.writeValue(new File("/tmp/" + networkPolicy.getMetadata().getName() + ".yaml"), networkPolicy);
        } catch (Exception e) {
            logger.error("YAML writer error: ", e);
        }
    }
}
