package eu.orchestrator.backend.rest.resourceprovider;

import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.common.exception.NotAuthorizedException;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.security.AuthService;
import eu.orchestrator.backend.transfer.ApplicationInstanceGraphTO;
import eu.orchestrator.backend.transfer.RestResponseSPA;
import eu.orchestrator.backend.service.applicationinstance.ApplicationInstanceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/applicationinstance")
public class ApplicationInstanceProviderController {

    private static final Logger logger = Logger.getLogger(ApplicationInstanceProviderController.class.getName());

    @Autowired
    private AuthService authService;

    @Autowired
    private ApplicationInstanceService applicationInstanceService;


    @GetMapping(value = "/{id}/provider")
    public ResponseEntity<RestResponseSPA<ApplicationInstanceGraphTO>> fetchProviderById(@PathVariable Long id,
            HttpServletRequest request) {

        boolean requiredFields = null != id && id != 0;

        if (requiredFields) {
            try {
                return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.APPLICATION_INSTANCE_FETCHED.getCode(),
                        GenericMessage.APPLICATION_INSTANCE_FETCHED.getMessage(request), applicationInstanceService.fetchProviderByApplicationInstanceId(id,
                        authService.getAuthenticatedUser())));
            } catch (NotAuthorizedException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
            } catch (GenericBusinessException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
            } catch (Exception ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR.getMessage(request)));
            }
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(),
                    GenericMessage.REQUIRED_FIELDS_MISSING.getMessage(request)));
        }

    }

}
