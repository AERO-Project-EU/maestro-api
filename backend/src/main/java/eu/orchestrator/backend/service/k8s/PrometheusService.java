package eu.orchestrator.backend.service.k8s;

import eu.orchestrator.backend.service.applicationinstance.ApplicationInstanceService;
import eu.orchestrator.backend.service.model.PrometheusResponse;
import eu.orchestrator.backend.transfer.PolicyEnginePrometheusConfigTO;
import eu.orchestrator.common.util.NullCheckUtil;

import eu.orchestrator.repository.domain.ApplicationInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PrometheusService {

    private static final String URL_PREFIX = "http://";
    private static final String URL_PATH = "/api/v1/query?query={query}";
    private static final String QUERY_KEY_NAME = "query";
    // Prometheus the k8s metric queries go to (host:port)
    @Value("${prometheus.k8s.host:localhost:30000}")
    private String prometheusUrl;

    @Autowired
    private KubernetesService kubernetesService;

    @Autowired
    ApplicationInstanceService applicationInstanceService;

    @Value("${policy-engine.prometheus-config.prometheus-ip}")
    private String policyEnginePrometheusConfigIp;

    @Value("${policy-engine.prometheus-config.prometheus-port}")
    private String policyEnginePrometheusConfigPort;


    public List<String> retrieveMetricsForCNI(String hexId) {
        Map<String, String> uriParams = new HashMap<>();
        String query = "{chart=~\".*" + hexId + ".*\"}";
        uriParams.put(QUERY_KEY_NAME, query);
        RestTemplate restTemplate = new RestTemplate();

        PrometheusResponse result = restTemplate.getForObject(URL_PREFIX + prometheusUrl + URL_PATH, PrometheusResponse.class, uriParams);
        Set<String> metrics = new HashSet<>();
        if (NullCheckUtil.isNotEmpty(result) && NullCheckUtil.isNotEmpty(result.getData()) && NullCheckUtil.isNotEmpty(result.getData().getResult())) {
            metrics = result.getData().getResult().stream().map(x -> x.getMetric().get__name__()).collect(Collectors.toSet());
        }
        return new ArrayList<>(metrics);
    }

    public List<String> retrieveDimensionsForMetricAndCNI(String metricName, String hexId) {
        Map<String, String> uriParams = new HashMap<>();
        String query = "{chart=~\".*" + hexId + ".*\"}" + "+and+" + metricName;
        uriParams.put(QUERY_KEY_NAME, query);
        RestTemplate restTemplate = new RestTemplate();
        PrometheusResponse result = restTemplate.getForObject(URL_PREFIX + prometheusUrl + URL_PATH, PrometheusResponse.class, uriParams);
        Set<String> dimensions = new HashSet<>();
        if (NullCheckUtil.isNotEmpty(result) && NullCheckUtil.isNotEmpty(result.getData()) && NullCheckUtil.isNotEmpty(result.getData().getResult())) {
            dimensions = result.getData().getResult().stream().map(x -> x.getMetric().getDimension()).collect(Collectors.toSet());
        }
        return new ArrayList<>(dimensions);
    }

    public PolicyEnginePrometheusConfigTO fetchPolicyEnginePrometheusConfigByApplicationInstanceHexId(String applicationInstanceHexId) {
        PolicyEnginePrometheusConfigTO prometheusConfig = new PolicyEnginePrometheusConfigTO();
        prometheusConfig.setPrometheusIp(policyEnginePrometheusConfigIp);
        prometheusConfig.setPrometheusPort(policyEnginePrometheusConfigPort);

        ApplicationInstance applicationInstance = applicationInstanceService.fetchApplicationInstanceByHexId(applicationInstanceHexId);
        prometheusConfig.setNamespace(kubernetesService.fetchNamespaceByApplicationInstance(applicationInstance.getApplicationInstanceID()).getNamespace());

        return prometheusConfig;
    }
}
