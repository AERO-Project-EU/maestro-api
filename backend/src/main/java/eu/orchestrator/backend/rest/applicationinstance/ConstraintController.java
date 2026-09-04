package eu.orchestrator.backend.rest.applicationinstance;

import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.common.exception.NotAuthorizedException;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.security.AuthService;
import eu.orchestrator.backend.transfer.ConstraintTO;
import eu.orchestrator.backend.transfer.RestResponseSPA;
import eu.orchestrator.backend.util.Util;
import eu.orchestrator.backend.service.applicationinstance.ConstraintService;
import eu.orchestrator.common.util.NullCheckUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
public class ConstraintController {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)

    private static final Logger logger = Logger.getLogger(ConstraintController.class.getName());

    @Autowired
    private AuthService authService;

    @Autowired
    private ConstraintService constraintService;


    @GetMapping(value = "/{id}/constraints")
    public ResponseEntity<RestResponseSPA<Serializable>> fetchConstraintsById(@PathVariable Long id, HttpServletRequest request) {
        try {
            List<ConstraintTO> constraintTOs = constraintService.fetchConstraintsByApplicationInstanceId(id, authService.getAuthenticatedUser());
            if (NullCheckUtil.isNotEmpty(constraintTOs)) {
                return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.ENTITIES_FETCHED.getCode(),
                        GenericMessage.ENTITIES_FETCHED.getMessage(request), (Serializable) constraintTOs));
            } else {
                return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.ENTITIES_FETCHED.getCode(),
                        GenericMessage.ENTITIES_FETCHED.getMessage(request), new ArrayList<>()));
            }
        } catch (NotAuthorizedException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
    }

    @GetMapping(value = "/{id}/constraints/{constraintCategory}")
    public ResponseEntity<RestResponseSPA<Serializable>> fetchConstraintsByCategoryAndId(@PathVariable Long id, @PathVariable String constraintCategory,
            HttpServletRequest request) {
        try {
            List<ConstraintTO> constraintTOs = constraintService.fetchConstraintsByCategoryAndApplicationInstanceId(id, constraintCategory,
                    authService.getAuthenticatedUser());
            if (NullCheckUtil.isNotEmpty(constraintTOs)) {
                return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.ENTITIES_FETCHED.getCode(),
                        GenericMessage.ENTITIES_FETCHED.getMessage(request), (Serializable) constraintTOs));
            } else {
                return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.ENTITIES_FETCHED.getCode(),
                        GenericMessage.ENTITIES_FETCHED.getMessage(request), new ArrayList<>()));
            }
        } catch (NotAuthorizedException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
    }

    @PutMapping(value = "/{id}/constraints/{constraintCategory}")
    public ResponseEntity<RestResponseSPA<Serializable>> updateConstraintsByCategoryAndId(@PathVariable Long id,
            @PathVariable String constraintCategory, @RequestBody List<ConstraintTO> constraints, HttpServletRequest request) {
        try {
            constraintService.updateConstraintsByCategoryAndApplicationInstanceId(id, constraintCategory, constraints, authService.getAuthenticatedUser());
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } catch (GenericBusinessException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(ex.getMessage(),
                    ex.getGenericMessage().getMessage(request)));
        } catch (NotAuthorizedException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
    }

}
