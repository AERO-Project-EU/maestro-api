package eu.orchestrator.metric.rest;

import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.metric.dto.MetricTo;
import eu.orchestrator.metric.dto.RequestNameDto;
import eu.orchestrator.metric.dto.RestResponseSpa;
import eu.orchestrator.metric.service.MetricService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/metric")
public class MetricController {

    private static final Logger logger = Logger.getLogger(MetricController.class.getName());

    @Autowired
    MetricService metricService;


    @GetMapping(value = "/{id}/applicationinstance/{applicationInstanceId}")
    public ResponseEntity<RestResponseSpa<MetricTo>> fetchById(@PathVariable Long id, @PathVariable Long applicationInstanceId, HttpServletRequest request) {
        boolean requiredFields = null != id && id != 0 && null != applicationInstanceId && applicationInstanceId != 0;
        if (requiredFields) {
            try {
                return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSpa<>(GenericMessage.METRIC_FETCHED.getCode(),
                        GenericMessage.METRIC_FETCHED.getMessage(request), metricService.fetchMetricToById(id, applicationInstanceId)));
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
    public ResponseEntity<RestResponseSpa<Serializable>> fetchMetricsByApplicationInstanceId(@PathVariable Long applicationInstanceId,
            RequestNameDto requestNameDto, Pageable pageable,
            HttpServletRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSpa<>(GenericMessage.ENTITIES_FETCHED.getCode(),
                    GenericMessage.ENTITIES_FETCHED.getMessage(request),
                    (Serializable) metricService.fetchMetricsByApplicationInstanceId(applicationInstanceId, requestNameDto.getName(), pageable)));
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
    public ResponseEntity<RestResponseSpa<Serializable>> createMetricsByApplicationInstanceId(@PathVariable Long applicationInstanceId,
            @RequestBody MetricTo metricTo,
            HttpServletRequest request) {
        boolean requiredFields = null != applicationInstanceId && applicationInstanceId != 0
                && null != metricTo && null != metricTo.getName() && null != metricTo.getType();
        if (requiredFields) {
            try {
                metricService.createMetricByApplicationInstanceId(applicationInstanceId, metricTo, extractAuthToken(request));
                return ResponseEntity.status(HttpStatus.CREATED).build();
            } catch (GenericBusinessException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSpa<>(ex.getMessage(),
                        ex.getGenericMessage().getMessage(request)));
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

    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity<RestResponseSpa<Serializable>> deleteMetricByApplicationInstanceId(@PathVariable Long id,
            HttpServletRequest request) {
        try {
            metricService.deleteMetricByApplicationInstanceId(id);
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

    @GetMapping(value = "/applicationinstance/{applicationInstanceId}/componentnode/{componentNodeHexID}/metrics")
    public ResponseEntity<RestResponseSpa<Serializable>> fetchMetricsByComponentNodeHexId(@PathVariable Long applicationInstanceId,
            @PathVariable(value = "componentNodeHexID") String componentNodeHexId, HttpServletRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSpa<>(GenericMessage.METRIC_FETCHED.getCode(),
                    GenericMessage.METRIC_FETCHED.getMessage(request),
                    (Serializable) metricService.fetchMetricsByComponentNodeHexID(applicationInstanceId, componentNodeHexId)));
        } catch (GenericBusinessException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSpa<>(ex.getMessage(),
                    ex.getGenericMessage().getMessage(request)));
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