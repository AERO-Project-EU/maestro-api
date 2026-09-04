package eu.orchestrator.backend.rest.applicationinstance;

import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.common.exception.NotAuthorizedException;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.security.AuthService;
import eu.orchestrator.backend.transfer.ApplicationInstanceGraphTO;
import eu.orchestrator.backend.transfer.RestResponseSPA;
import eu.orchestrator.backend.util.Util;
import eu.orchestrator.common.util.NullCheckUtil;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.backend.service.applicationinstance.ApplicationInstanceService;
import eu.orchestrator.backend.service.resourceprovider.ProviderService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/api/v1/applicationinstance")
@SuppressWarnings("Duplicates")
public class ApplicationInstanceController {

    private static final Logger logger = Logger.getLogger(ApplicationInstanceController.class.getName());

    @DateTimeFormat(iso = ISO.DATE_TIME)

    @Autowired
    private AuthService authService;

    @Autowired
    private ApplicationInstanceService applicationInstanceService;

    @Autowired
    private ProviderService providerService;


    @PostMapping(value = "/list")
    public ResponseEntity<RestResponseSPA<Serializable>> fetchApplicationInstances(Pageable pageable,
            @RequestBody(required = false) ApplicationInstance applicationInstance, HttpServletRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.ENTITIES_FETCHED.getCode(),
                    GenericMessage.ENTITIES_FETCHED.getMessage(request),
                    (Serializable) applicationInstanceService.fetchApplicationInstanceList(pageable, applicationInstance, authService.getAuthenticatedUser())));
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

    @GetMapping(value = "/search/{name}")
    public ResponseEntity<RestResponseSPA<Serializable>> checkIfApplicationInstanceNameExists(@PathVariable() String name, HttpServletRequest request) {
        try {
            if (null != name && !name.isEmpty()) {
                if (!applicationInstanceService.checkIfApplicationInstanceNameExists(name, authService.getAuthenticatedUser())) {
                    return ResponseEntity.status(HttpStatus.OK).build();
                }
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new RestResponseSPA<>(GenericMessage.APPLICATION_INSTANCE_ALREADY_EXISTS.getCode(),
                                GenericMessage.APPLICATION_INSTANCE_ALREADY_EXISTS.getMessage(request)));
            }
        } catch (NotAuthorizedException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(),
                    ex.getGenericMessage().getMessage(request)));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                GenericMessage.GENERIC_ERROR.getMessage(request)));
    }

    @GetMapping(value = "/count")
    public ResponseEntity<RestResponseSPA<Long>> count(HttpServletRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.GENERIC_SUCCESS.getCode(),
                    GenericMessage.GENERIC_SUCCESS.getMessage(request), applicationInstanceService.count(authService.getAuthenticatedUser())));
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

    @GetMapping(value = "/{id}")
    public ResponseEntity<RestResponseSPA<ApplicationInstanceGraphTO>> fetchById(@PathVariable Long id, HttpServletRequest request) {
        boolean requiredFields = null != id && id != 0;
        if (requiredFields) {
            try {
                ApplicationInstance applicationInstance = applicationInstanceService.fetchApplicationInstanceById(id);
                if (NullCheckUtil.isNotEmpty(applicationInstance)) {
                    ApplicationInstanceGraphTO applicationInstanceGraphTo = applicationInstanceService.fetchApplicationInstanceGraphTO(applicationInstance,
                            authService.getAuthenticatedUser());
                    if (NullCheckUtil.isNotEmpty(applicationInstanceGraphTo)) {
                        return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.APPLICATION_INSTANCE_FETCHED.getCode(),
                                GenericMessage.APPLICATION_INSTANCE_FETCHED.getMessage(request), applicationInstanceGraphTo));
                    }
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(new RestResponseSPA<>(GenericMessage.APPLICATION_INSTANCE_NOT_AUTHORIZED.getCode(),
                                    GenericMessage.APPLICATION_INSTANCE_NOT_AUTHORIZED.getMessage(request)));
                }
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new RestResponseSPA<>(GenericMessage.APPLICATION_INSTANCE_NOT_EXIST.getCode(),
                                GenericMessage.APPLICATION_INSTANCE_NOT_EXIST.getMessage(request)));
            } catch (NotAuthorizedException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(),
                        ex.getGenericMessage().getMessage(request)));
            } catch (Exception ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                        GenericMessage.GENERIC_ERROR.getMessage(request)));
            }
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(),
                    GenericMessage.REQUIRED_FIELDS_MISSING.getMessage(request)));
        }
    }

    @PostMapping
    public ResponseEntity<RestResponseSPA<ApplicationInstanceGraphTO>> create(@RequestBody ApplicationInstance applicationInstance,
            HttpServletRequest request) {
        boolean requiredFields = null != applicationInstance && null != applicationInstance.getApplication()
                && null != applicationInstance.getApplication().getId() && applicationInstance.getApplication().getId() > 0
                && null != applicationInstance.getOverlay() && null != applicationInstance.getProvider()
                && null != applicationInstance.getProvider().getProviderID() && applicationInstance.getProvider().getProviderID() != 0
                && null != providerService.findById(applicationInstance.getProvider().getProviderID())
                && null != applicationInstance.getName() && !applicationInstance.getName().isEmpty();
        if (requiredFields) {
            try {
                return ResponseEntity.status(HttpStatus.CREATED).body(new RestResponseSPA<>(GenericMessage.APPLICATION_INSTANCE_FETCHED.getCode(),
                        GenericMessage.APPLICATION_INSTANCE_FETCHED.getMessage(request), applicationInstanceService.create(applicationInstance,
                        authService.getAuthenticatedUser())));
            } catch (NotAuthorizedException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(),
                        ex.getGenericMessage().getMessage(request)));
            } catch (GenericBusinessException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(ex.getMessage(),
                        ex.getGenericMessage().getMessage(request)));
            } catch (Exception ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                        GenericMessage.GENERIC_ERROR.getMessage(request)));
            }
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(),
                    GenericMessage.REQUIRED_FIELDS_MISSING.getMessage(request)));
        }
    }

    @PutMapping
    public ResponseEntity<RestResponseSPA<ApplicationInstanceGraphTO>> update(@RequestBody ApplicationInstance applicationInstance,
            HttpServletRequest request) {
        // Required Fields
        boolean requiredFields = null != applicationInstance && null != applicationInstance.getApplicationInstanceID()
                && applicationInstance.getApplicationInstanceID() > 0;
        if (requiredFields) {
            try {
                return ResponseEntity.status(HttpStatus.ACCEPTED).body(new RestResponseSPA<>(GenericMessage.APPLICATION_INSTANCE_FETCHED.getCode(),
                        GenericMessage.APPLICATION_INSTANCE_FETCHED.getMessage(request), applicationInstanceService.update(applicationInstance,
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
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(),
                    GenericMessage.REQUIRED_FIELDS_MISSING.getMessage(request)));
        }
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<RestResponseSPA<Serializable>> delete(@PathVariable Long id, HttpServletRequest request) {
        boolean requiredFields = null != id && id > 0;
        if (requiredFields) {
            try {
                applicationInstanceService.delete(id, authService.getAuthenticatedUser());
                return ResponseEntity.status(HttpStatus.OK).build();
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
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(),
                GenericMessage.REQUIRED_FIELDS_MISSING.getMessage(request)));
    }

}
