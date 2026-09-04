package eu.orchestrator.backend.rest.security;

import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.common.exception.NotAuthorizedException;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.security.AuthService;
import eu.orchestrator.backend.transfer.RestResponseSPA;
import eu.orchestrator.backend.transfer.SecurityConfigurationTO;
import eu.orchestrator.backend.service.security.SecurityConfigurationService;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/api/v1/applicationinstance")
public class SecurityConfigurationController {

    private static final Logger logger = Logger.getLogger(SecurityConfigurationController.class.getName());

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)

    @Autowired
    private AuthService authService;

    @Autowired
    private SecurityConfigurationService securityConfigurationService;


    @ExceptionHandler({HttpMessageNotReadableException.class})
    public ResponseEntity<RestResponseSPA> handleException(HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new RestResponseSPA(GenericMessage.REQUIRED_FIELDS_WITH_PROPER_VALUES.getCode(),
                GenericMessage.REQUIRED_FIELDS_WITH_PROPER_VALUES.getMessage(request)));
    }

    //TODO astrid
    @PostMapping(value = "/{id}/configuration/security/list")
    public ResponseEntity<RestResponseSPA<Serializable>> fetchSecurityConfigurations(@PathVariable Long id, Pageable pageable, HttpServletRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.ENTITIES_FETCHED.getCode(),
                    GenericMessage.ENTITIES_FETCHED.getMessage(request),
                    (Serializable) securityConfigurationService.fetchSecurityConfigurations(id, pageable, authService.getAuthenticatedUser())));
        } catch (NotAuthorizedException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
        } catch (GenericBusinessException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(ex.getMessage(),
                    ex.getGenericMessage().getMessage(request)));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
    }

    @PostMapping(value = "/{id}/configuration/{securityConfigurationId}/result/security/list")
    public ResponseEntity<RestResponseSPA<Serializable>> fetchSecurityConfigurationResults(@PathVariable("id") Long id,
            @PathVariable("securityConfigurationId") Long securityConfigurationId, Pageable pageable, HttpServletRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.ENTITIES_FETCHED.getCode(),
                    GenericMessage.ENTITIES_FETCHED.getMessage(request),
                    (Serializable) securityConfigurationService.fetchSecurityConfigurationResults(id, securityConfigurationId, pageable,
                            authService.getAuthenticatedUser())));
        } catch (NotAuthorizedException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
        } catch (GenericBusinessException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(ex.getMessage(),
                    ex.getGenericMessage().getMessage(request)));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
    }

    @PostMapping(value = "/{id}/configuration/security")
    public ResponseEntity<RestResponseSPA<Serializable>> createSecurityConfiguration(@PathVariable Long id,
            @RequestBody SecurityConfigurationTO securityConfigurationTo, HttpServletRequest request) {
        try {
            securityConfigurationService.createSecurityConfiguration(id, securityConfigurationTo, authService.getAuthenticatedUser());
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (NotAuthorizedException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
        } catch (GenericBusinessException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(ex.getMessage(),
                    ex.getGenericMessage().getMessage(request)));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
    }

    @DeleteMapping(value = "/{id}/configuration/security/{securityConfigurationId}")
    public ResponseEntity<RestResponseSPA<Serializable>> deleteSecurityConfiguration(@PathVariable Long id, @PathVariable Long securityConfigurationId,
            HttpServletRequest request) {
        try {
            securityConfigurationService.deleteSecurityConfiguration(securityConfigurationId);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new RestResponseSPA<>(GenericMessage.GENERIC_SUCCESS.getCode(), GenericMessage.GENERIC_SUCCESS.getMessage(request)));
        } catch (NotAuthorizedException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
    }
}
