package eu.orchestrator.elasticity.rest;

import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.elasticity.dto.ElasticityTo;
import eu.orchestrator.elasticity.dto.RestResponseSpa;

import eu.orchestrator.elasticity.service.ElasticityQueriesService;
import eu.orchestrator.repository.domain.rainbow.Slo;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/elasticity")
public class ElasticityQueriesController {

    private static final Logger logger = Logger.getLogger(ElasticityQueriesController.class.getName());

    private final ElasticityQueriesService elasticityQueriesService;

    @Autowired
    public ElasticityQueriesController(ElasticityQueriesService elasticityQueriesService) {
        this.elasticityQueriesService = elasticityQueriesService;
    }

    @GetMapping(value = "/applicationinstance/{applicationInstanceId}/slo/{sloId}")
    public ResponseEntity<RestResponseSpa<Serializable>> show(@PathVariable Long sloId, @PathVariable Long applicationInstanceId,
            HttpServletRequest request) {
        boolean requiredFields = null != sloId && sloId > 0 && null != applicationInstanceId && applicationInstanceId != 0;
        if (requiredFields) {
            try {
                return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSpa<>(GenericMessage.ELASTICITY_STRATEGIES_FETCHED.getCode(),
                        GenericMessage.ELASTICITY_STRATEGIES_FETCHED.getMessage(request),
                        elasticityQueriesService.getSlo(sloId, applicationInstanceId, extractAuthToken(request))));
            } catch (Exception ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSpa<>(GenericMessage.GENERIC_ERROR.getCode(),
                        GenericMessage.GENERIC_ERROR.getMessage(request)));
            }
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSpa<>(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(),
                    GenericMessage.REQUIRED_FIELDS_MISSING.getMessage(request)));
        }
    }

    @PostMapping(value = "/applicationinstance/{applicationInstanceId}/list")
    public ResponseEntity<RestResponseSpa<Serializable>> list(
            Pageable pageable, @RequestBody(required = false) ElasticityTo elasticityTo, @PathVariable Long applicationInstanceId,
            HttpServletRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSpa<>(GenericMessage.ELASTICITY_STRATEGIES_FETCHED.getCode(),
                    GenericMessage.ELASTICITY_STRATEGIES_FETCHED.getMessage(request),
                    (Serializable) elasticityQueriesService.getSlosByApplicationInstanceId(elasticityTo, pageable, applicationInstanceId, extractAuthToken(request))));
        } catch (GenericBusinessException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSpa<>(ex.getMessage(),
                    ex.getGenericMessage().getMessage(request)));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSpa<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
    }

    @PutMapping(value = "/applicationinstance/{applicationInstanceId}/slo/{sloId}")
    public ResponseEntity<RestResponseSpa<Serializable>> edit(@PathVariable Long applicationInstanceId, @RequestBody
            ElasticityTo elasticityTo, HttpServletRequest request) {
        boolean requiredFields = null != applicationInstanceId && applicationInstanceId != 0
                && null != elasticityTo && null != elasticityTo.getName();
        if (!requiredFields) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSpa<>(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(),
                    GenericMessage.REQUIRED_FIELDS_MISSING.getMessage(request)));
        }
        try {
            elasticityQueriesService.createSloByApplicationInstanceId(applicationInstanceId, elasticityTo, extractAuthToken(request));
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (GenericBusinessException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSpa<>(ex.getMessage(),
                    ex.getGenericMessage().getMessage(request)));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSpa<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
    }

    @PostMapping(value = "/create/{applicationInstanceId}")
    public ResponseEntity<RestResponseSpa<Serializable>> create(@PathVariable Long applicationInstanceId, @RequestBody
            ElasticityTo elasticityTo, HttpServletRequest request) {
        boolean requiredFields = null != applicationInstanceId && applicationInstanceId != 0
                && null != elasticityTo && null != elasticityTo.getName();
        if (!requiredFields) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSpa<>(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(),
                    GenericMessage.REQUIRED_FIELDS_MISSING.getMessage(request)));
        }
        try {
            elasticityQueriesService.createSloByApplicationInstanceId(applicationInstanceId, elasticityTo, extractAuthToken(request));
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (GenericBusinessException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSpa<>(ex.getMessage(),
                    ex.getGenericMessage().getMessage(request)));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSpa<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
    }

    @DeleteMapping(value = "/applicationinstance/{applicationInstanceId}/slo/{sloId}")
    public ResponseEntity<RestResponseSpa<Serializable>> delete(@PathVariable Long sloId, @PathVariable Long applicationInstanceId,
            HttpServletRequest request) {
        try {
            elasticityQueriesService.deleteSloByApplicationInstanceId(applicationInstanceId, sloId, extractAuthToken(request));
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (GenericBusinessException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSpa<>(ex.getMessage(),
                    ex.getGenericMessage().getMessage(request)));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSpa<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
    }

    private String extractAuthToken(HttpServletRequest request) {
        String auth = null;
        for (Cookie ck : request.getCookies()) {
            if (ck.getName().equals("auth_token")) {
                auth = ck.getName() + "=" + ck.getValue();
            }
        }
        return auth;
    }

}
