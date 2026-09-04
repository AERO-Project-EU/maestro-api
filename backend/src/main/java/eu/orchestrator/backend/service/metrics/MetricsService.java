package eu.orchestrator.backend.service.metrics;

import eu.orchestrator.repository.dao.ComponentNodeInstanceDAO;
import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.repository.domain.ProviderType.ProviderName;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

@Service
public class MetricsService {

    private static final Logger logger = Logger.getLogger(MetricsService.class.getName());

    private static final Long POLLING_PERIOD = 10000L;

    @Value("${prometheus.server.type}")
    private String prometheusType;
    @Value("${prometheus.server.url}")
    private String prometheusUrl;

    @Value("${prometheus.k8s.type}")
    private String k8sPrometheusType;
    @Value("${prometheus.k8s.url}")
    private String k8sPrometheusUrl;

    private Map<Long, ScheduledFuture<?>> scheduledTasks = new HashMap<>();

    private final ThreadPoolTaskScheduler threadPoolTaskScheduler;
    private final MetricExporterService metricExporterService;
    private final SimpMessagingTemplate wsTemplate;
    private final ComponentNodeInstanceDAO componentNodeInstanceDAO;

    @Autowired
    public MetricsService(ThreadPoolTaskScheduler threadPoolTaskScheduler, MetricExporterService metricExporterService,
            SimpMessagingTemplate wsTemplate, ComponentNodeInstanceDAO componentNodeInstanceDAO) {
        this.threadPoolTaskScheduler = threadPoolTaskScheduler;
        this.metricExporterService = metricExporterService;
        this.wsTemplate = wsTemplate;
        this.componentNodeInstanceDAO = componentNodeInstanceDAO;
    }

    public String start(Long nodeId) {
        logger.info("Backend - MetricsService - start: " + nodeId);
        String topic = "/metrics/" + nodeId;

        Optional<ComponentNodeInstance> componentNodeInstanceOptional = componentNodeInstanceDAO.findById(nodeId);

        if (!componentNodeInstanceOptional.isPresent()) {
            throw new RuntimeException("component not found");
        }
        ComponentNodeInstance componentNodeInstance = componentNodeInstanceOptional.get();

        String providerName = componentNodeInstance.getApplicationInstance().getProvider().getProviderType().getName();

        String type;
        String url;

        if (
                ProviderName.KUBERNETES.name().equals(providerName) ||
                        ProviderName.FIVE_G_INDUCE_SLICE.name().equals(providerName) ||
                        ProviderName.FIFTH_GENERATION_OSS_KUBERNETES_PROVIDER.name().equals(providerName)
        ) {
            type = k8sPrometheusType;
            url = k8sPrometheusUrl;
        } else if (ProviderName.OPENSTACK.name().equals(providerName)) {
            type = prometheusType;
            url = prometheusUrl;
        } else if (ProviderName.RAINBOW_KUBERNETES.name().equals(providerName)
                || ProviderName.RAINBOW_KUBERNETES.getFriendlyName().equals(providerName)) {
            logger.info("Backend - MetricsService - Rainbow ");
            type = "rainbow_k8s";
            url = componentNodeInstance.getComponentNodeInstanceIPs().first().getIp();
        } else {
            throw new NotImplementedException("Not implemented");
        }

        String componentName = componentNodeInstance.getName().toLowerCase() + "-" + componentNodeInstance.getHexID();
        String componentNodeInstanceHexId = componentNodeInstance.getHexID();
        String componentNodeHexId = componentNodeInstance.getComponentNode().getHexID();
        String applicationInstanceHexId = componentNodeInstance.getApplicationInstance().getHexID();
        String applicationHexId = componentNodeInstance.getApplicationInstance().getApplication().getHexID();

        ScheduledFuture<?> task = threadPoolTaskScheduler.scheduleAtFixedRate(
                new MetricsTask(
                        topic,
                        wsTemplate,
                        metricExporterService,
                        componentName,
                        applicationHexId,
                        applicationInstanceHexId,
                        componentNodeHexId,
                        componentNodeInstanceHexId,
                        type,
                        url),
                POLLING_PERIOD
        );

        scheduledTasks.put(nodeId, task);
        return topic;
    }

    public void stop(Long nodeId) {
        if (scheduledTasks.containsKey(nodeId)) {
            ScheduledFuture<?> task = scheduledTasks.get(nodeId);
            task.cancel(true);
            scheduledTasks.remove(nodeId);
        }
    }

    public void stopAll() {
        for (Map.Entry<Long, ScheduledFuture<?>> entry : scheduledTasks.entrySet()) {
            ScheduledFuture<?> task = scheduledTasks.get(entry.getKey());
            task.cancel(true);
        }
        scheduledTasks.clear();
    }

}
