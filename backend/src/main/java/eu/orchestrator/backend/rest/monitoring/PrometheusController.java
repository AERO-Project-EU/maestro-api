package eu.orchestrator.backend.rest.monitoring;

import eu.orchestrator.backend.service.k8s.PrometheusService;
import eu.orchestrator.backend.transfer.PolicyEnginePrometheusConfigTO;
import eu.orchestrator.backend.transfer.RestResponseSPA;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.common.exception.GenericBusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/v1/monitoring")
public class PrometheusController {
    private static final Logger logger = Logger.getLogger(PrometheusController.class.getName());

    @Autowired
    private PrometheusService prometheusService;

    @GetMapping(path = "/prometheus/applicationInstanceHexId/{applicationInstanceHexId}")
    public ResponseEntity<RestResponseSPA<PolicyEnginePrometheusConfigTO>> fetchPolicyEnginePrometheusConfigByApplicationInstanceHexId(@PathVariable String applicationInstanceHexId,
                                                                                                                                  HttpServletRequest request) {
        boolean requiredFields = null != applicationInstanceHexId && !applicationInstanceHexId.isEmpty();
        if (requiredFields) {
            try {
                return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.NAMESPACE_FETCHED.getCode(),
                        GenericMessage.NAMESPACE_FETCHED.getMessage(request), prometheusService.fetchPolicyEnginePrometheusConfigByApplicationInstanceHexId(applicationInstanceHexId)));
            } catch (GenericBusinessException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new RestResponseSPA<>(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(),
                    GenericMessage.REQUIRED_FIELDS_MISSING.getMessage(request)));
        }

    }
}
