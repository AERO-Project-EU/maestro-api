package eu.orchestrator.backend.service.metrics;

import eu.orchestrator.transfer.entities.metricExporter.MetricModel;
import eu.orchestrator.transfer.entities.metricExporter.requests.ComponentInstance;
import eu.orchestrator.transfer.entities.metricExporter.requests.MetricsRequestTO;
import eu.orchestrator.transfer.entities.metricExporter.requests.MetricsSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class MetricExporterService {

    private static final Logger logger = Logger.getLogger(MetricExporterService.class.getName());
    private static final RestTemplate restTemplate = new RestTemplate();

    @Value("${exporter.server.url}")
    private String graphExporterURL;

    public MetricModel getMetrics(
            String name,
            String applicationHexId,
            String applicationInstanceHexId,
            String componentHexId,
            String componentInstanceHexId,
            String type,
            String url
    ) {
        String metricExporterUrl = graphExporterURL + "/api/v1/metrics";

        MetricsRequestTO metricsRequestTO = getMetricsRequestTO(
                name,
                applicationHexId,
                applicationInstanceHexId,
                componentHexId,
                componentInstanceHexId,
                type,
                url
        );

        try {
            ResponseEntity<MetricModel> responseEntity = restTemplate
                    .exchange(
                            RequestEntity
                                    .post(URI.create(metricExporterUrl))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .body(metricsRequestTO),
                            MetricModel.class
                    );

            if (responseEntity.getStatusCode() != HttpStatus.OK || responseEntity.getBody() == null) {
                String message = "Graphs for application instance: " + applicationHexId + " have not been fetched successfully!";
                logger.log(Level.SEVERE, message);
                throw new RuntimeException(message);
            }

            return responseEntity.getBody();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }

    }

    private MetricsRequestTO getMetricsRequestTO(
            String name,
            String applicationHexId,
            String applicationInstanceHexId,
            String componentHexId,
            String componentInstanceHexId,
            String type,
            String url
    ) {
        MetricsSource metricsSource = new MetricsSource();
        metricsSource.setType(type);
        metricsSource.setUrl(url);

        ComponentInstance componentInstance = new ComponentInstance();
        componentInstance.setName(name);
        componentInstance.setComponentNodeInstanceHexId(componentInstanceHexId);
        componentInstance.setComponentNodeHexId(componentHexId);
        componentInstance.setApplicationInstanceHexId(applicationInstanceHexId);
        componentInstance.setApplicationHexId(applicationHexId);

        MetricsRequestTO metricsRequestTO = new MetricsRequestTO();
        metricsRequestTO.setInstance(componentInstance);
        metricsRequestTO.setSource(metricsSource);

        return metricsRequestTO;
    }

}
