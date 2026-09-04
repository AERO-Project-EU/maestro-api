package eu.orchestrator.backend.rest.applicationinstance;

import eu.orchestrator.common.exception.NotAuthorizedException;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.security.AuthService;
import eu.orchestrator.backend.transfer.RestResponseSPA;
import eu.orchestrator.backend.transfer.StatusTO;
import eu.orchestrator.backend.util.Util;
import eu.orchestrator.backend.service.applicationinstance.StatusService;
import eu.orchestrator.common.util.NullCheckUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
public class StatusController {

    private static final Logger logger = Logger.getLogger(StatusController.class.getName());

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)

    @Autowired
    private AuthService authService;

    @Autowired
    private StatusService statusService;


    @GetMapping(value = "/{id}/statuses")
    public ResponseEntity<RestResponseSPA<Serializable>> fetchStatusesById(@PathVariable Long id, Pageable pageable,
            HttpServletRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.ENTITIES_FETCHED.getCode(),
                    GenericMessage.ENTITIES_FETCHED.getMessage(request), new PageImpl<>(new ArrayList<>(), pageable, 0)));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
    }

    @GetMapping(value = "/{id}/statuses/limited")
    public ResponseEntity<RestResponseSPA<Serializable>> fetchStatusesLimitedById(@PathVariable Long id, HttpServletRequest request) {
        try {
            List<StatusTO> statusesTOs = statusService.fetchStatusesLimitedById(id, authService.getAuthenticatedUser());
            if (NullCheckUtil.isNotEmpty(statusesTOs)) {
                return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.ENTITIES_FETCHED.getCode(),
                        GenericMessage.ENTITIES_FETCHED.getMessage(request), (Serializable) statusesTOs));
            } else {
                return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.ENTITIES_FETCHED.getCode(),
                        GenericMessage.ENTITIES_FETCHED.getMessage(request), new ArrayList<>()));
            }
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
