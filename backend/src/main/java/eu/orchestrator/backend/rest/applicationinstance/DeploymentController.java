package eu.orchestrator.backend.rest.applicationinstance;

import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.common.exception.NotAuthorizedException;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.security.AuthService;
import eu.orchestrator.backend.transfer.ApplicationInstanceGraphTO;
import eu.orchestrator.backend.transfer.RestResponseSPA;
import eu.orchestrator.backend.util.Util;
import eu.orchestrator.common.util.NullCheckUtil;
import eu.orchestrator.repository.domain.SlicePlacement;
import eu.orchestrator.backend.service.applicationinstance.DeploymentService;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/applicationinstance/{applicationInstanceID}/request")
@SuppressWarnings("Duplicates")
public class DeploymentController {

    private static final Logger logger = Logger.getLogger(DeploymentController.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private AuthService authService;

    @Autowired
    private DeploymentService deploymentService;


    @PostMapping(value = "/placement")
    public ResponseEntity<RestResponseSPA<Serializable>> requestPlacement(@PathVariable(value = "applicationInstanceID") Long applicationInstanceId,
            HttpServletRequest request) {
        try {
            List<SlicePlacement> slicePlacements = deploymentService.requestPlacement(applicationInstanceId, authService.getAuthenticatedUser());
            if (NullCheckUtil.isNotEmpty(slicePlacements)) {
                return ResponseEntity.status(HttpStatus.CREATED).body(new RestResponseSPA<>(GenericMessage.APPLICATION_INSTANCE_PLACEMENT_CREATED.getCode(),
                        GenericMessage.APPLICATION_INSTANCE_PLACEMENT_CREATED.getMessage(request), objectMapper.writeValueAsString(slicePlacements)));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                        GenericMessage.GENERIC_ERROR.getMessage(request)));
            }
        } catch (NotAuthorizedException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
    }

    @PostMapping(value = "/slice")
    public ResponseEntity<RestResponseSPA<ApplicationInstanceGraphTO>> requestSliceIntent(
            @PathVariable(value = "applicationInstanceID") Long applicationInstanceId, HttpServletRequest request) {
        try {
            ApplicationInstanceGraphTO applicationInstanceGraphTo = deploymentService.requestSliceIntent(applicationInstanceId,
                    authService.getAuthenticatedUser());
            if (NullCheckUtil.isNotEmpty(applicationInstanceGraphTo)) {
                return ResponseEntity.status(HttpStatus.ACCEPTED).body(new RestResponseSPA<>(GenericMessage.APPLICATION_INSTANCE_SLICE_CREATED.getCode(),
                        GenericMessage.APPLICATION_INSTANCE_SLICE_CREATED.getMessage(request), applicationInstanceGraphTo));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                        GenericMessage.GENERIC_ERROR.getMessage(request)));
            }
        } catch (NotAuthorizedException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
        } catch (GenericBusinessException ex) {
            logger.log(Level.SEVERE, ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
    }

    @PostMapping(value = "/deployment")
    public ResponseEntity<RestResponseSPA<ApplicationInstanceGraphTO>> requestDeployment(
            @PathVariable(value = "applicationInstanceID") Long applicationInstanceId, HttpServletRequest request) {
        try {
            ApplicationInstanceGraphTO applicationInstanceGraphTo = deploymentService.requestDeployment(applicationInstanceId,
                    authService.getAuthenticatedUser());
            if (NullCheckUtil.isNotEmpty(applicationInstanceGraphTo)) {
                return ResponseEntity.status(HttpStatus.ACCEPTED).body(new RestResponseSPA<>(GenericMessage.APPLICATION_INSTANCE_SLICE_CREATED.getCode(),
                        GenericMessage.APPLICATION_INSTANCE_SLICE_CREATED.getMessage(request), applicationInstanceGraphTo));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                        GenericMessage.GENERIC_ERROR.getMessage(request)));
            }
        } catch (NotAuthorizedException exception) {
            logger.log(Level.SEVERE, exception.getMessage(), exception);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(exception.getMessage(), exception.getGenericMessage().getMessage(request)));
        } catch (Exception exception) {
            logger.log(Level.SEVERE, exception.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
    }

    @PostMapping(value = "/undeployment")
    public ResponseEntity<RestResponseSPA<Serializable>> requestUndeployment(
            @PathVariable(value = "applicationInstanceID") Long applicationInstanceId, HttpServletRequest request) {
        try {
            deploymentService.requestUndeployment(applicationInstanceId, authService.getAuthenticatedUser());
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } catch (NotAuthorizedException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
    }

    @PostMapping(value = "/cancellation")
    public ResponseEntity<RestResponseSPA<Serializable>> requestCancellation(
            @PathVariable(value = "applicationInstanceID") Long applicationInstanceId, HttpServletRequest request) {
        try {
            deploymentService.requestCancellation(applicationInstanceId, authService.getAuthenticatedUser());
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
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
