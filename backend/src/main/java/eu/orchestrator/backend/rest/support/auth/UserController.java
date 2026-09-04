package eu.orchestrator.backend.rest.support.auth;

import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.common.exception.NotAuthorizedException;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.security.AuthService;
import eu.orchestrator.backend.transfer.RestResponseSPA;
import eu.orchestrator.repository.domain.User;
import eu.orchestrator.backend.service.support.auth.UserBackendService;

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
@RequestMapping("/api/v1/user")
@SuppressWarnings("Duplicates")
public class UserController {

    private static final Logger logger = Logger.getLogger(UserController.class.getName());

    @Autowired
    private AuthService authService;

    @Autowired
    private UserBackendService userBackendService;


    @PostMapping(value = "/list")
    public ResponseEntity<RestResponseSPA<Serializable>> fetchUsers(Pageable pageable, @RequestParam(required = false, value = "filters") String filters,
            @RequestBody(required = false) User user, HttpServletRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new RestResponseSPA<>(GenericMessage.ENTITIES_FETCHED.getCode(), GenericMessage.ENTITIES_FETCHED.getMessage(request),
                            (Serializable) userBackendService.fetchUsers(pageable, filters, user, authService.getAuthenticatedUser())));
        } catch (NotAuthorizedException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<RestResponseSPA<User>> fetchById(@PathVariable Long id, HttpServletRequest request) {
        boolean requiredFields = null != id && id != 0;
        if (requiredFields) {
            try {
                return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.USER_FETCHED.getCode(),
                        GenericMessage.USER_FETCHED.getMessage(request), userBackendService.fetchById(id, authService.getAuthenticatedUser())));
            } catch (NotAuthorizedException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
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
    public ResponseEntity<RestResponseSPA<Serializable>> create(@RequestBody User user, HttpServletRequest request) {
        boolean requiredFields = null != user.getUsername() && !user.getUsername().isEmpty() && null != user.getPassword() && !user.getPassword().isEmpty()
                && null != user.getVerifyPassword() && !user.getVerifyPassword().isEmpty() && null != user.getFirstName() && !user.getFirstName().isEmpty()
                && null != user.getLastName() && !user.getLastName().isEmpty() && null != user.getEmail() && !user.getEmail().isEmpty()
                && null != user.getPhone() && !user.getPhone().isEmpty() && null != user.getOrganization() && null != user.getRole()
                && !user.getRole().isEmpty();
        if (requiredFields) {
            try {
                userBackendService.create(user, authService.getAuthenticatedUser());
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
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(),
                    GenericMessage.REQUIRED_FIELDS_MISSING.getMessage(request)));
        }
    }

    @PutMapping
    public ResponseEntity<RestResponseSPA<Serializable>> update(@RequestBody User user, HttpServletRequest request) {
        boolean requiredFields = null != user && null != user.getId() && user.getId() != 0 && null != user.getFirstName() && !user.getFirstName().isEmpty()
                && null != user.getLastName() && !user.getLastName().isEmpty() && null != user.getPhone() && !user.getPhone().isEmpty()
                && null != user.getOrganization() && null != user.getRole() && !user.getRole().isEmpty();
        if (null != user && (null != user.getNewPassword() && !user.getNewPassword().isEmpty() && null == user.getVerifyPassword()
                || user.getVerifyPassword().isEmpty())) {
            requiredFields = false;
        }
        if (requiredFields) {
            try {
                userBackendService.update(user, authService.getAuthenticatedUser());
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

    @PutMapping(value = "/{id}/status")
    public ResponseEntity<RestResponseSPA<Serializable>> changeStatus(@PathVariable Long id, HttpServletRequest request) {
        boolean requiredFields = null != id && id != 0;
        if (requiredFields) {
            try {
                userBackendService.changeStatus(id, authService.getAuthenticatedUser());
                return ResponseEntity.status(HttpStatus.ACCEPTED).build();
            } catch (NotAuthorizedException ex) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
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
                userBackendService.delete(id, authService.getAuthenticatedUser());
                return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.USER_DEACTIVATED.getCode(),
                        GenericMessage.USER_DEACTIVATED.getMessage(request)));
            } catch (NotAuthorizedException ex) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
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

}
