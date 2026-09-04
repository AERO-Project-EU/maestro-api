package eu.orchestrator.backend.rest.oss;

import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.common.exception.NotAuthorizedException;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.security.AuthService;
import eu.orchestrator.backend.transfer.RestResponseSPA;
import eu.orchestrator.backend.service.oss.slice.SliceService;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/slice/{sliceID}")
public class SliceController {

    private static final Logger logger = Logger.getLogger(SliceController.class.getName());

    @Autowired
    private AuthService authService;

    @Autowired
    private SliceService sliceService;


    @GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<RestResponseSPA<String>> fetchSliceById(@PathVariable(value = "sliceID") Long sliceId, HttpServletRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.ENTITIES_FETCHED.getCode(),
                    GenericMessage.ENTITIES_FETCHED.getMessage(request), sliceService.fetchSliceById(sliceId, authService.getAuthenticatedUser())));
        } catch (JsonProcessingException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(ex.getMessage(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
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
