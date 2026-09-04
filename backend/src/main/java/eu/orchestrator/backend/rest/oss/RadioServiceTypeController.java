package eu.orchestrator.backend.rest.oss;

import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.common.exception.NotAuthorizedException;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.security.AuthService;
import eu.orchestrator.backend.transfer.RestResponseSPA;
import eu.orchestrator.repository.domain.RadioServiceType;
import eu.orchestrator.backend.service.oss.RadioServiceTypeService;

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
@RequestMapping("/api/v1/radioservicetype")
@SuppressWarnings("Duplicates")
public class RadioServiceTypeController {

    private static final Logger logger = Logger.getLogger(RadioServiceTypeController.class.getName());

    @Autowired
    private AuthService authService;

    @Autowired
    private RadioServiceTypeService radioServiceTypeService;


    @PostMapping(value = "/list")
    public ResponseEntity<RestResponseSPA<Serializable>> fetchRadioServiceTypes(Pageable pageable,
            @RequestParam(required = false, value = "filters") String filters, @RequestBody(required = false) RadioServiceType radioServiceType,
            HttpServletRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.ENTITIES_FETCHED.getCode(),
                    GenericMessage.ENTITIES_FETCHED.getMessage(request),
                    (Serializable) radioServiceTypeService.fetchRadioServiceTypes(pageable, filters, radioServiceType, authService.getAuthenticatedUser())));
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
    public ResponseEntity<RestResponseSPA<RadioServiceType>> fetchById(@PathVariable Long id, HttpServletRequest request) {
        try {
            if (!radioServiceTypeService.checkRadioServiceTypeAuthentication(authService.getAuthenticatedUser())) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.RADIO_SERVICE_NOT_AUTHORIZED.getCode(),
                        GenericMessage.RADIO_SERVICE_NOT_AUTHORIZED.getMessage(request)));
            }
        } catch (NotAuthorizedException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
        boolean requiredFields = null != id && id != 0;
        if (requiredFields) {
            try {
                //check if radio service type exist
                RadioServiceType radioServiceType = radioServiceTypeService.fetchById(id);
                if (radioServiceType != null) {
                    return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.RADIO_SERVICE_TYPE_FETCHED.getCode(),
                            GenericMessage.RADIO_SERVICE_TYPE_FETCHED.getMessage(request), radioServiceType));
                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.RADIO_SERVICE_NOT_EXIST.getCode(),
                            GenericMessage.RADIO_SERVICE_NOT_EXIST.getMessage(request)));
                }
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
    public ResponseEntity<RestResponseSPA<Serializable>> create(@RequestBody RadioServiceType radioServiceType, HttpServletRequest request) {
        try {
            if (!radioServiceTypeService.checkRadioServiceTypeAuthentication(authService.getAuthenticatedUser())) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.RADIO_SERVICE_NOT_AUTHORIZED.getCode(),
                        GenericMessage.RADIO_SERVICE_NOT_AUTHORIZED.getMessage(request)));
            }
        } catch (NotAuthorizedException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
        boolean requiredFields = null != radioServiceType && null != radioServiceType.getSstValue() && radioServiceType.getSstValue() != 0
                && null != radioServiceType.getServiceType() && !radioServiceType.getServiceType().isEmpty() && null != radioServiceType.getCharacteristics()
                && !radioServiceType.getCharacteristics().isEmpty();
        if (requiredFields) {
            try {
                radioServiceTypeService.create(radioServiceType);
                return ResponseEntity.status(HttpStatus.CREATED).build();
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
    public ResponseEntity<RestResponseSPA<Serializable>> update(@RequestBody RadioServiceType radioServiceType, HttpServletRequest request) {
        try {
            if (!radioServiceTypeService.checkRadioServiceTypeAuthentication(authService.getAuthenticatedUser())) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.RADIO_SERVICE_NOT_AUTHORIZED.getCode(),
                        GenericMessage.RADIO_SERVICE_NOT_AUTHORIZED.getMessage(request)));
            }
        } catch (NotAuthorizedException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
        // Required Fields
        boolean requiredFields = null != radioServiceType && null != radioServiceType.getId() && radioServiceType.getId() != 0
                && null != radioServiceType.getSstValue() && radioServiceType.getSstValue() != 0 && null != radioServiceType.getServiceType()
                && !radioServiceType.getServiceType().isEmpty() && null != radioServiceType.getCharacteristics()
                && !radioServiceType.getCharacteristics().isEmpty();
        if (requiredFields) {
            try {
                radioServiceTypeService.update(radioServiceType);
                return ResponseEntity.status(HttpStatus.ACCEPTED).build();
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RestResponseSPA<>(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(),
                            GenericMessage.REQUIRED_FIELDS_MISSING.getMessage(request)));
        }
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<RestResponseSPA<Serializable>> delete(@PathVariable Long id, HttpServletRequest request) {
        try {
            if (!radioServiceTypeService.checkRadioServiceTypeAuthentication(authService.getAuthenticatedUser())) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.RADIO_SERVICE_NOT_AUTHORIZED.getCode(),
                        GenericMessage.RADIO_SERVICE_NOT_AUTHORIZED.getMessage(request)));
            }
        } catch (NotAuthorizedException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
        boolean requiredFields = null != id && id != 0;
        if (requiredFields) {
            try {
                radioServiceTypeService.delete(id);
                return ResponseEntity.status(HttpStatus.OK).build();
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

}
