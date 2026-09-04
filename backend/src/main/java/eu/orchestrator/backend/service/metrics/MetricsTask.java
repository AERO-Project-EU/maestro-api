package eu.orchestrator.backend.service.metrics;

import eu.orchestrator.transfer.entities.metricExporter.MetricModel;

import com.google.gson.Gson;
import org.springframework.messaging.simp.SimpMessagingTemplate;

public class MetricsTask implements Runnable {

    private final String topic;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final MetricExporterService metricExporterService;
    private final String applicationHexId;
    private final String applicationInstanceHexId;
    private final String name;
    private final String componentHexId;
    private final String componentInstanceHexId;
    private final String type;
    private final String url;

    public MetricsTask(String topic, SimpMessagingTemplate simpMessagingTemplate,
            MetricExporterService metricExporterService, String name, String applicationHexId, String applicationInstanceHexId, String componentHexId,
            String componentInstanceHexId, String type, String url) {
        this.topic = topic;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.metricExporterService = metricExporterService;
        this.name = name;
        this.applicationHexId = applicationHexId;
        this.applicationInstanceHexId = applicationInstanceHexId;
        this.componentHexId = componentHexId;
        this.componentInstanceHexId = componentInstanceHexId;
        this.type = type;
        this.url = url;
    }

    @Override
    public void run() {
        MetricModel metricModel = metricExporterService.getMetrics(
                this.name,
                this.applicationHexId,
                this.applicationInstanceHexId,
                this.componentHexId,
                this.componentInstanceHexId,
                this.type,
                this.url
        );
        Gson gson = new Gson();
        String json = gson.toJson(metricModel);
        simpMessagingTemplate.convertAndSend(topic, json);
    }

}
