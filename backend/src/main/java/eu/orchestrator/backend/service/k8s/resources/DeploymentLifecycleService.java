package eu.orchestrator.backend.service.k8s.resources;

import eu.orchestrator.common.util.NullCheckUtil;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentStatus;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Service
public class DeploymentLifecycleService {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentLifecycleService.class);

    public boolean checkDeploymentLifecycle(String namespace, Config config) {

        try (final KubernetesClient client = new DefaultKubernetesClient(config)) {
            //client
            List<Deployment> deploymentList = client.apps().deployments().inNamespace(namespace).list().getItems();

            boolean status = Boolean.FALSE;
            for (Deployment dp : deploymentList) {
                DeploymentStatus deploymentStatus = dp.getStatus();
                if (deploymentStatus.getReadyReplicas() == deploymentStatus.getReplicas()) {
                    status = Boolean.TRUE;
                } else {
                    status = Boolean.FALSE;
                    break;
                }
            }

            return status;

        } catch (Exception e) {
            logger.error(e.getMessage());
            return Boolean.FALSE;
        }
    }


    public boolean checkLogin(Config config) {
        try (final KubernetesClient client = new DefaultKubernetesClient(config)) {
            //just to check credentials
            client.pods().inNamespace("default").list().getItems();
            return Boolean.TRUE;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return Boolean.FALSE;
        }
    }

    public Map<String, String> getPodsIp(String namespace, Config config) {
        Map<String, String> podsIp = new HashMap<>();
        try (final KubernetesClient client = new DefaultKubernetesClient(config)) {
            List<Pod> pods = client.pods().inNamespace(namespace).list().getItems();
            if (NullCheckUtil.isNotEmpty(pods)) {
                for (Pod pd : pods) {
                    podsIp.put(pd.getMetadata().getName(), pd.getStatus().getPodIP());
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return podsIp;
    }

}
