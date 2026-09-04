package eu.orchestrator.backend.service.k8s.resources.rainbow;

import eu.orchestrator.transfer.entities.rainbowk8s.ElasticityStrategiesDto;
import eu.orchestrator.transfer.entities.rainbowk8s.ElasticityStrategyDto;

import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionList;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@org.springframework.stereotype.Service
public class ElasticityRainbowService {

    private static final Logger logger = LoggerFactory.getLogger(ElasticityRainbowService.class);
    private static final String ELASTICITY_RAINBOW = "elasticity.k8s.rainbow-h2020.eu";
    private static final String ELASTICITY_POLARIS = "elasticity.polaris-slo-cloud.github.io";

    public ElasticityStrategiesDto fetchElasticityStrategiesRainbow(Config config) {
        try (final KubernetesClient client = new DefaultKubernetesClient(config)) {
            CustomResourceDefinitionList crds = client.apiextensions().v1().customResourceDefinitions().list();
            List<CustomResourceDefinition> crdsItems = crds.getItems();
            ElasticityStrategiesDto elasticityStrategies = new ElasticityStrategiesDto();
            for (CustomResourceDefinition cr : crdsItems) {
                if (cr.getSpec() != null && cr.getMetadata() != null && cr.getSpec().getNames() != null && cr.getSpec().getNames().getKind() != null
                        && cr.getMetadata().getName() != null && cr.getSpec().getVersions() != null && cr.getSpec().getVersions().get(0) != null
                        && cr.getSpec().getVersions().get(0).getName() != null && cr.getSpec().getGroup() != null
                        && (cr.getMetadata().getName().contains(ELASTICITY_RAINBOW) || cr.getMetadata().getName().contains(ELASTICITY_POLARIS))) {
                    ElasticityStrategyDto elasticityStrategyDto = new ElasticityStrategyDto();
                    elasticityStrategyDto.setKind(cr.getSpec().getNames().getKind());
                    elasticityStrategyDto.setVersion(cr.getSpec().getGroup() + "/" + cr.getSpec().getVersions().get(0).getName());
                    elasticityStrategies.getElasticityStrategies().add(elasticityStrategyDto);
                }
            }
            return elasticityStrategies;
        } catch (Exception ex) {
            logger.error("Fetch Service Graph error: {}", ex.getMessage());
            return null;
        }
    }
}
