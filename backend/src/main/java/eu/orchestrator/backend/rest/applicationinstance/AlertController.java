package eu.orchestrator.backend.rest.applicationinstance;

import eu.orchestrator.common.exception.NotAuthorizedException;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.security.AuthService;
import eu.orchestrator.backend.transfer.AlertTO;
import eu.orchestrator.backend.transfer.RestResponseSPA;
import eu.orchestrator.backend.util.Util;
import eu.orchestrator.common.util.NullCheckUtil;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.backend.service.applicationinstance.AlertService;
import eu.orchestrator.backend.service.applicationinstance.ApplicationInstanceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/api/v1/applicationinstance")
@SuppressWarnings("Duplicates")
public class AlertController {

    private static final Logger logger = Logger.getLogger(AlertController.class.getName());

    @DateTimeFormat(iso = ISO.DATE_TIME)

    @Autowired
    private AuthService authService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private ApplicationInstanceService applicationInstanceService;


    @GetMapping(value = "/{id}/alerts")
    public ResponseEntity<RestResponseSPA<Serializable>> fetchAlertsById(@PathVariable Long id, Pageable pageable, HttpServletRequest request) {
        try {
            ApplicationInstance applicationInstance = applicationInstanceService.fetchApplicationInstanceById(id);
            if (NullCheckUtil.isNotEmpty(applicationInstance)) {
                return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.ENTITIES_FETCHED.getCode(),
                        GenericMessage.ENTITIES_FETCHED.getMessage(request),
                        (Serializable) alertService.fetchAlertsById(applicationInstance, pageable, authService.getAuthenticatedUser())));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        } catch (NotAuthorizedException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(),
                    ex.getGenericMessage().getMessage(request)));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
    }

    @GetMapping(value = "/{id}/alerts/limited")
    public ResponseEntity<RestResponseSPA<Serializable>> fetchAlertsLimitedById(@PathVariable Long id, HttpServletRequest request) {
        try {
            ApplicationInstance applicationInstance = applicationInstanceService.fetchApplicationInstanceById(id);
            if (NullCheckUtil.isNotEmpty(applicationInstance)) {
                List<AlertTO> alertTOs = alertService.fetchAlertsLimitedById(applicationInstance, authService.getAuthenticatedUser());
                if (NullCheckUtil.isNotEmpty(alertTOs)) {
                    return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.ENTITIES_FETCHED.getCode(),
                            GenericMessage.ENTITIES_FETCHED.getMessage(request), (Serializable) alertTOs));
                }
                return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.ENTITIES_FETCHED.getCode(),
                        GenericMessage.ENTITIES_FETCHED.getMessage(request), new ArrayList<>()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        } catch (NotAuthorizedException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(),
                    ex.getGenericMessage().getMessage(request)));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
    }
}

