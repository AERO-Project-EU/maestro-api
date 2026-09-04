package eu.orchestrator.backend.rest.component;

import eu.orchestrator.common.exception.NotAuthorizedException;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.security.AuthService;
import eu.orchestrator.backend.transfer.RestResponseSPA;
import eu.orchestrator.repository.domain.Interface;
import eu.orchestrator.backend.service.component.InterfaceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/interface")
public class InterfaceController {

    private static final Logger logger = Logger.getLogger(InterfaceController.class.getName());

    @Autowired
    private AuthService authService;

    @Autowired
    private InterfaceService interfaceService;


    @PostMapping(value = "/list/{componentID}")
    public ResponseEntity<RestResponseSPA<Serializable>> fetchInterfaces(Pageable pageable, @RequestParam(required = false, value = "filters") String filters,
            @RequestBody(required = false) Interface interfaceParam, @PathVariable(required = false, value = "componentID") String componentId,
            HttpServletRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.ENTITIES_FETCHED.getCode(),
                    GenericMessage.ENTITIES_FETCHED.getMessage(request),
                    interfaceService.fetchInterfaces(pageable, filters, interfaceParam, authService.getAuthenticatedUser())));
        } catch (NotAuthorizedException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
    }

}
