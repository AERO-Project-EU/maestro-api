package eu.orchestrator.backend.rest.dashboard;

import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.common.exception.NotAuthorizedException;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.security.AuthService;
import eu.orchestrator.backend.transfer.RestResponseSPA;
import eu.orchestrator.backend.transfer.StatusTO;
import eu.orchestrator.backend.service.dashboard.DashboardService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/api/v1/graph")
public class DashboardGraphsController {

    private static final Logger logger = Logger.getLogger(DashboardGraphsController.class.getName());

    @Autowired
    private AuthService authService;

    @Autowired
    private DashboardService dashboardService;


    @GetMapping(value = "/{type}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RestResponseSPA<Serializable>> fetchDashboardGraphs(@PathVariable String type, HttpServletRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new RestResponseSPA<>(GenericMessage.GENERIC_CHART_FETCHED.getCode(), GenericMessage.GENERIC_CHART_FETCHED.getMessage(request),
                            (Serializable) dashboardService.fetchDashboardGraphs(type, authService.getAuthenticatedUser())));
        } catch (NotAuthorizedException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
    }

    @GetMapping(value = "/overview", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RestResponseSPA<Serializable>> fetchOverview(HttpServletRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.GENERIC_CHART_FETCHED.getCode(),
                    GenericMessage.GENERIC_CHART_FETCHED.getMessage(request), dashboardService.fetchOverview(authService.getAuthenticatedUser())));
        } catch (NotAuthorizedException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(),
                    ex.getGenericMessage().getMessage(request)));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
    }

    @GetMapping(value = "/provider", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RestResponseSPA<Serializable>> fetchProviderGraphs(HttpServletRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new RestResponseSPA<>(GenericMessage.GENERIC_CHART_FETCHED.getCode(), GenericMessage.GENERIC_CHART_FETCHED.getMessage(request),
                            (Serializable) dashboardService.fetchProviderGraphs(authService.getAuthenticatedUser())));
        } catch (NotAuthorizedException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
    }

    @GetMapping(value = "/provider/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RestResponseSPA<Serializable>> fetchProviderHistoryGraphs(HttpServletRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.GENERIC_CHART_FETCHED.getCode(),
                    GenericMessage.GENERIC_CHART_FETCHED.getMessage(request), (Serializable) dashboardService.fetchProviderHistoryGraphs()));
        } catch (NotAuthorizedException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }

    }

    @GetMapping(value = "/elasticity", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RestResponseSPA<Serializable>> fetchElasticityGraphs(HttpServletRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.GENERIC_CHART_FETCHED.getCode(),
                    GenericMessage.GENERIC_CHART_FETCHED.getMessage(request), (Serializable) dashboardService.fetchElasticityGraphs(
                    authService.getAuthenticatedUser())));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));

        }
    }

    @GetMapping(value = "/elasticity/{applicationInstanceID}/{componentNodeID}/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RestResponseSPA<Serializable>> fetchElasticityGraphs(@PathVariable Long applicationInstanceId, @PathVariable Long componentNodeId,
            HttpServletRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new RestResponseSPA<>(GenericMessage.GENERIC_CHART_FETCHED.getCode(), GenericMessage.GENERIC_CHART_FETCHED.getMessage(request),
                            (Serializable) dashboardService.fetchElasticityGraphs(applicationInstanceId, componentNodeId)));
        } catch (GenericBusinessException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
    }

    @GetMapping(value = "/intrusion/detection", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RestResponseSPA<Serializable>> fetchIntrusionDetectionTable(HttpServletRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new RestResponseSPA<>(GenericMessage.GENERIC_CHART_FETCHED.getCode(), GenericMessage.GENERIC_CHART_FETCHED.getMessage(request),
                            (Serializable) dashboardService.fetchIntrusionDetectionTable(authService.getAuthenticatedUser())));
        } catch (NotAuthorizedException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
    }

    @GetMapping(value = "/intrusion/prevention", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RestResponseSPA<Serializable>> fetchIntrusionPreventionTable(HttpServletRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.GENERIC_CHART_FETCHED.getCode(),
                    GenericMessage.GENERIC_CHART_FETCHED.getMessage(request), (Serializable) dashboardService.fetchIntrusionPreventionTable(
                    authService.getAuthenticatedUser())));
        } catch (NotAuthorizedException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
    }

    @GetMapping(value = "/log", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RestResponseSPA<Serializable>> fetchLogs(HttpServletRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new RestResponseSPA<>(GenericMessage.GENERIC_CHART_FETCHED.getCode(), GenericMessage.GENERIC_CHART_FETCHED.getMessage(request),
                            (Serializable) dashboardService.fetchLogs(authService.getAuthenticatedUser())));
        } catch (NotAuthorizedException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
    }

    @GetMapping(value = "/log/alert", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RestResponseSPA<Serializable>> fetchAlertLogs(HttpServletRequest request) {
        try {
            List<StatusTO> statusToList = dashboardService.fetchAlertLogs(authService.getAuthenticatedUser());
            return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.GENERIC_CHART_FETCHED.getCode(),
                    GenericMessage.GENERIC_CHART_FETCHED.getMessage(request), (Serializable) statusToList));
        } catch (NotAuthorizedException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
    }


}
