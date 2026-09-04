package eu.orchestrator.backend.rest.kubernetes;

import eu.orchestrator.backend.service.k8s.RainbowService;
import eu.orchestrator.backend.transfer.RestResponseSPA;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.repository.domain.Application;
import eu.orchestrator.transfer.entities.rainbowk8s.ElasticityStrategiesDto;


import eu.orchestrator.transfer.entities.rainbowk8s.ServiceGraph;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/rainbow")
public class RainbowController {

    private static final Logger logger = Logger.getLogger(RainbowController.class.getName());

    @Autowired
    private RainbowService rainbowService;

    @GetMapping(path = "/elasticitystrategies/applicationInstanceId/{applicationInstanceId}")
    public ResponseEntity<RestResponseSPA<ElasticityStrategiesDto>> fetchNamespaceByApplicationInstance(@PathVariable Long applicationInstanceId,
            HttpServletRequest request) {
        boolean requiredFields = null != applicationInstanceId && applicationInstanceId != 0;
        if (requiredFields) {
            try {
                return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.ELASTICITY_STRATEGIES_FETCHED.getCode(),
                        GenericMessage.ELASTICITY_STRATEGIES_FETCHED.getMessage(request), rainbowService.getElasticityStrategies(applicationInstanceId)));
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

    @GetMapping(path = "/servicegraph/applicationInstanceId/{applicationInstanceId}")
    public ResponseEntity<RestResponseSPA<ServiceGraph>> fetchServiceGraphByApplicationInstance(@PathVariable Long applicationInstanceId,
            HttpServletRequest request) {
        logger.info("Backend - RainbowController - ApplicationInstanceId : " + applicationInstanceId);
        boolean requiredFields = null != applicationInstanceId && applicationInstanceId != 0;
        if (!requiredFields) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new RestResponseSPA<>(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(),
                    GenericMessage.REQUIRED_FIELDS_MISSING.getMessage(request)));
        }

        try {
            return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.SERVICE_GRAPH_FETCHED.getCode(),
                    GenericMessage.SERVICE_GRAPH_FETCHED.getMessage(request), rainbowService.getServiceGraph(applicationInstanceId)));
        } catch (GenericBusinessException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
        }
    }

    @PostMapping(path = "/servicegraph/applicationInstanceId/{applicationInstanceId}/apply")
    public ResponseEntity<RestResponseSPA<String>> applyServiceGraph(@PathVariable Long applicationInstanceId, @RequestBody ServiceGraph serviceGraph,
            HttpServletRequest request) {
        boolean requiredFields = null != applicationInstanceId && applicationInstanceId != 0;
        if (!requiredFields) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new RestResponseSPA<>(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(),
                    GenericMessage.REQUIRED_FIELDS_MISSING.getMessage(request)));
        }
        try {
            rainbowService.applyServiceGraph(applicationInstanceId, serviceGraph);
            return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.SERVICE_GRAPH_APPLIED.getCode(),
                    GenericMessage.SERVICE_GRAPH_APPLIED.getMessage(request), ""));
        } catch (GenericBusinessException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
        }
    }

}
