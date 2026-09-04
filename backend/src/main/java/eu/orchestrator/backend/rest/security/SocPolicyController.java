package eu.orchestrator.backend.rest.security;

import eu.orchestrator.common.exception.NotAuthorizedException;
import eu.orchestrator.common.exception.NotFoundException;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.security.AuthService;
import eu.orchestrator.backend.transfer.RestResponseSPA;
import eu.orchestrator.repository.domain.SocPolicy;
import eu.orchestrator.backend.service.security.SocPolicyService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.http.HttpServletRequest;

@RestController
// TODO change base path
@RequestMapping("/api/v1/applicationinstance")
public class SocPolicyController {

    private static final Logger logger = Logger.getLogger(SocPolicyController.class.getName());

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)

    @Autowired
    private AuthService authService;

    @Autowired
    private SocPolicyService socPolicyService;


    @ExceptionHandler({HttpMessageNotReadableException.class})
    public ResponseEntity<RestResponseSPA> handleException(HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new RestResponseSPA(GenericMessage.REQUIRED_FIELDS_WITH_PROPER_VALUES.getCode(),
                GenericMessage.REQUIRED_FIELDS_WITH_PROPER_VALUES.getMessage(request)));

    }

    @PostMapping(value = "/{id}/policies/soc/list")
    public ResponseEntity<RestResponseSPA<Serializable>> fetchSocPolicyByApplicationInstanceId(@PathVariable Long id, Pageable pageable,
            HttpServletRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.GENERIC_SUCCESS.getCode(),
                    GenericMessage.GENERIC_SUCCESS.getMessage(request), (Serializable) socPolicyService.fetchSocPolicyByApplicationInstanceId(id, pageable,
                    authService.getAuthenticatedUser())));
        } catch (NotAuthorizedException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
    }

    @PostMapping(value = "/{id}/policies/soc")
    public ResponseEntity<RestResponseSPA<Serializable>> createSocPolicyByApplicationInstanceId(@PathVariable Long id, @RequestBody SocPolicy socPolicy,
            HttpServletRequest request) {
        try {
            socPolicyService.createSocPolicyByApplicationInstanceId(id, socPolicy, authService.getAuthenticatedUser());
            return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.GENERIC_SUCCESS.getCode(),
                    GenericMessage.GENERIC_SUCCESS.getMessage(request)));
        } catch (NotAuthorizedException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
    }

    @DeleteMapping(value = "/{id}/policies/soc/{socPolicyID}")
    public ResponseEntity<RestResponseSPA<Serializable>> deleteSocPolicyByApplicationInstanceId(@PathVariable Long id,
            @PathVariable(value = "socPolicyID") Long socPolicyId, HttpServletRequest request) {
        try {
            socPolicyService.deleteSocPolicyByApplicationInstanceId(id, socPolicyId);
            return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.GENERIC_SUCCESS.getCode(),
                    GenericMessage.GENERIC_SUCCESS.getMessage(request)));
        } catch (NotFoundException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
    }

    @PutMapping(value = "/update/{id}/soc/{flag}")
    public ResponseEntity<RestResponseSPA<Serializable>> updateSocForAllComponentNodeInstances(@PathVariable Long id, @PathVariable Boolean flag,
            HttpServletRequest request) {
        try {
            socPolicyService.updateSocForAllComponentNodeInstances(id, flag);
            return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.GENERIC_SUCCESS.getCode(),
                    GenericMessage.GENERIC_SUCCESS.getMessage(request)));
        } catch (NotFoundException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
    }

}
