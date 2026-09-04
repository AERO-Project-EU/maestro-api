package eu.orchestrator.backend.rest.elasticity;

import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.common.exception.NotAuthorizedException;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.security.AuthService;
import eu.orchestrator.backend.transfer.ProfileTO;
import eu.orchestrator.backend.transfer.ProfilingResultTO;
import eu.orchestrator.backend.transfer.RestResponseSPA;
import eu.orchestrator.backend.service.elasticity.ProfileService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
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
public class ProfileController {

    private static final Logger logger = Logger.getLogger(ProfileController.class.getName());

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)

    @Autowired
    private AuthService authService;

    @Autowired
    private ProfileService profileService;


    @ExceptionHandler({HttpMessageNotReadableException.class})
    public ResponseEntity<RestResponseSPA<Serializable>> handleException(HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new RestResponseSPA<>(GenericMessage.REQUIRED_FIELDS_WITH_PROPER_VALUES.getCode(),
                GenericMessage.REQUIRED_FIELDS_WITH_PROPER_VALUES.getMessage(request)));

    }

    // Profile
    @GetMapping(value = "/algorithm/list")
    public ResponseEntity<RestResponseSPA<Serializable>> fetchProfileAlgorithm(HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.GENERIC_SUCCESS.getCode(),
                GenericMessage.GENERIC_SUCCESS.getMessage(request), (Serializable) profileService.fetchProfileAlgorithm()));
    }

    @GetMapping(value = "/{id}/profile/list")
    public ResponseEntity<RestResponseSPA<Serializable>> fetchProfileById(@PathVariable Long id, Pageable pageable, HttpServletRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.ENTITIES_FETCHED.getCode(),
                    GenericMessage.ENTITIES_FETCHED.getMessage(request), (Serializable) profileService.fetchProfileById(id, pageable,
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

    @PostMapping(value = "/{id}/profile")
    public ResponseEntity<RestResponseSPA<Serializable>> createProfileById(@PathVariable Long id, @RequestBody ProfileTO profileTo,
            HttpServletRequest request) {
        try {
            profileService.createProfileById(id, profileTo, authService.getAuthenticatedUser());
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

    @DeleteMapping(value = "/{id}/profile/{profileID}")
    public ResponseEntity<RestResponseSPA<Serializable>> deleteProfileById(@PathVariable Long id, @PathVariable(value = "profileID") Long profileId,
            HttpServletRequest request) {
        try {
            profileService.deleteProfileById(id, profileId, authService.getAuthenticatedUser());
            return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.PROFILE_DELETE.getCode(),
                    GenericMessage.PROFILE_DELETE.getMessage(request)));
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

    @GetMapping(value = "/{id}/profile/{profileID}/result")
    public ResponseEntity<RestResponseSPA<ProfilingResultTO>> fetchResult(@PathVariable("id") Long id, @PathVariable("profileID") Long profileId,
            HttpServletRequest request) {
        try {
            ProfilingResultTO profilingResultTo = profileService.fetchResult(id, profileId, authService.getAuthenticatedUser());
            if (profilingResultTo != null) {
                return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.GENERIC_SUCCESS.getCode(),
                        GenericMessage.GENERIC_SUCCESS.getMessage(request), profilingResultTo));
            }
            return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.PROFILE_DELETE.getCode(),
                    GenericMessage.PROFILE_DELETE.getMessage(request)));
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

    //TODO need to deleted !
    @GetMapping(value = "/{id}/profiling")
    public ResponseEntity<RestResponseSPA<Serializable>> fetchProfilingUrlsById(@PathVariable Long id, HttpServletRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.ENTITIES_FETCHED.getCode(),
                    GenericMessage.ENTITIES_FETCHED.getMessage(request),
                    (Serializable) profileService.fetchProfilingURLsById(id, authService.getAuthenticatedUser())));
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
}
