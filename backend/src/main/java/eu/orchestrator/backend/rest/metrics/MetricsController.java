package eu.orchestrator.backend.rest.metrics;

import eu.orchestrator.backend.service.metrics.MetricsService;

import eu.orchestrator.backend.transfer.RestResponseSPA;
import eu.orchestrator.common.enums.GenericMessage;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/metrics")
public class MetricsController {

    private static final Logger logger = Logger.getLogger(MetricsController.class.getName());

    private final MetricsService metricsService;

    @Autowired
    public MetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @GetMapping(path = "/node/{nodeId}/subscribe")
    public ResponseEntity<Serializable> startMetricsStreaming(@PathVariable(value = "nodeId") Long nodeId, HttpServletRequest request) {
        try {
            logger.info("Backend - MetricsController - startMetricsStreaming NodeID: " + nodeId);
            String topic = metricsService.start(nodeId);
            return ResponseEntity.status(HttpStatus.OK).body(topic);
        } catch (Exception e) {
            return ResponseEntity.status(
                    HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
    }

    @DeleteMapping(path = "/node/{nodeId}/unsubscribe")
    public ResponseEntity<Serializable> stopMetricsStreaming(@PathVariable(value = "nodeId") Long nodeId, HttpServletRequest request) {
        try {
            metricsService.stop(nodeId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            return ResponseEntity.status(
                    HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
    }

    @DeleteMapping(path = "/node/unsubscribe-all")
    public ResponseEntity<Serializable> stopAllStreams(HttpServletRequest request) {
        try {
            metricsService.stopAll();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(
                    HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
    }

}
