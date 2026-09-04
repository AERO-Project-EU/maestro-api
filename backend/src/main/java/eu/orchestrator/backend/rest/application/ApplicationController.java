package eu.orchestrator.backend.rest.application;

import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.common.exception.NotAuthorizedException;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.security.AuthService;
import eu.orchestrator.backend.transfer.ApplicationGraphTO;
import eu.orchestrator.backend.transfer.RestResponseSPA;
import eu.orchestrator.repository.domain.Application;
import eu.orchestrator.backend.service.application.ApplicationService;

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
@RequestMapping("/api/v1/application")
@SuppressWarnings("Duplicates")
public class ApplicationController {

    private static final Logger logger = Logger.getLogger(ApplicationController.class.getName());

    @Autowired
    private AuthService authService;

    @Autowired
    private ApplicationService applicationService;


    @PostMapping(value = "/list")
    public ResponseEntity<RestResponseSPA<Serializable>> fetchApplications(Pageable pageable, @RequestParam(required = false, value = "filters") String filters,
            @RequestBody(required = false) Application application, HttpServletRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.ENTITIES_FETCHED.getCode(),
                    GenericMessage.ENTITIES_FETCHED.getMessage(request),
                    (Serializable) applicationService.applicationList(filters, application, pageable, authService.getAuthenticatedUser())));
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

    @GetMapping(value = "/search/{name}")
    public ResponseEntity<RestResponseSPA<Serializable>> checkIfApplicationNameExists(@PathVariable() String name, HttpServletRequest request) {
        try {
            if (null != name && !name.isEmpty()) {
                if (!applicationService.checkIfApplicationNameExists(name, authService.getAuthenticatedUser())) {
                    return ResponseEntity.status(HttpStatus.OK).build();
                }
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.APPLICATION_ALREADY_EXISTS.getCode(),
                        GenericMessage.APPLICATION_ALREADY_EXISTS.getMessage(request)));
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

    // Use this controller for Application filtering field
    @GetMapping(value = "/fetchByInstance")
    public ResponseEntity<RestResponseSPA<Serializable>> fetchApplicationsByInstance(HttpServletRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.ENTITIES_FETCHED.getCode(),
                    GenericMessage.ENTITIES_FETCHED.getMessage(request),
                    (Serializable) applicationService.fetchApplicationsByInstance(authService.getAuthenticatedUser())));
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

    @GetMapping(value = "/{id}")
    public ResponseEntity<RestResponseSPA<ApplicationGraphTO>> fetchById(@PathVariable Long id, HttpServletRequest request) {
        boolean requiredFields = null != id && id != 0;
        if (requiredFields) {
            try {
                ApplicationGraphTO applicationGraphTo = applicationService.fetchById(id, authService.getAuthenticatedUser());
                if (null != applicationGraphTo) {
                    return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.APPLICATION_FETCHED.getCode(),
                            GenericMessage.APPLICATION_FETCHED.getMessage(request), applicationGraphTo));
                }
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.APPLICATION_NOT_AUTHORIZED.getCode(),
                        GenericMessage.APPLICATION_NOT_AUTHORIZED.getMessage(request)));
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
    public ResponseEntity<RestResponseSPA<Serializable>> create(@RequestBody Application application, HttpServletRequest request) {

        boolean requiredFields = null != application && null != application.getName() && !application.getName().isEmpty()
                && null != application.getPublicApplication() && null != application.getComponentNodes() && !application.getComponentNodes().isEmpty();
        if (requiredFields) {
            try {
                applicationService.create(application, authService.getAuthenticatedUser());
                return ResponseEntity.status(HttpStatus.CREATED).build();
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
    public ResponseEntity<RestResponseSPA<Serializable>> update(@RequestBody Application application, HttpServletRequest request) {
        // Required Fields
        boolean requiredFields = null != application && null != application.getId() && application.getId() != 0
                && null != application.getName() && !application.getName().isEmpty();
        if (requiredFields) {
            try {
                applicationService.update(application, authService.getAuthenticatedUser());
                return ResponseEntity.status(HttpStatus.ACCEPTED).build();
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
        boolean requiredFields = null != id && id != 0;
        if (requiredFields) {
            try {
                applicationService.delete(id, authService.getAuthenticatedUser());
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
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(),
                    GenericMessage.REQUIRED_FIELDS_MISSING.getMessage(request)));
        }

    }

    // Used to show count of Applications on Dashboard
    @GetMapping(value = "/count")
    public ResponseEntity<RestResponseSPA<Long>> count(HttpServletRequest request) {
        Long count = applicationService.count(authService.getAuthenticatedUser());
        return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.GENERIC_SUCCESS.getCode(),
                GenericMessage.GENERIC_SUCCESS.getMessage(request), count));

    }

}
