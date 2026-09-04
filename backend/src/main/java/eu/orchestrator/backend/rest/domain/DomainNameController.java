package eu.orchestrator.backend.rest.domain;

import eu.orchestrator.common.exception.BadRequestBusinessException;
import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.common.exception.NotAuthorizedException;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.security.AuthService;
import eu.orchestrator.backend.transfer.DomainNameTO;
import eu.orchestrator.backend.transfer.RestResponseSPA;
import eu.orchestrator.repository.domain.DomainName;
import eu.orchestrator.backend.service.domain.DomainService;

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
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/domain")
@SuppressWarnings("Duplicates")
public class DomainNameController {

    private static final Logger logger = Logger.getLogger(DomainNameController.class.getName());

    @Autowired
    private AuthService authService;

    @Autowired
    private DomainService domainService;


    @PostMapping
    public ResponseEntity<RestResponseSPA<Serializable>> create(@RequestBody DomainNameTO domainNameTo, HttpServletRequest request) {
        try {
            domainService.create(domainNameTo, authService.getAuthenticatedUser());
            return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.DOMAIN_CREATED.getCode(),
                    GenericMessage.DOMAIN_CREATED.getMessage(request)));
        } catch (NotAuthorizedException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(),
                    GenericMessage.REQUIRED_FIELDS_MISSING.getMessage(request)));
        }
    }

    @PutMapping
    public ResponseEntity<RestResponseSPA<Serializable>> update(@RequestBody DomainNameTO domainNameTo, HttpServletRequest request) {
        try {
            domainService.update(domainNameTo, authService.getAuthenticatedUser());
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new RestResponseSPA<>(GenericMessage.DOMAIN_UPDATED.getCode(), GenericMessage.DOMAIN_UPDATED.getMessage(request)));
        } catch (NotAuthorizedException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
        } catch (BadRequestBusinessException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
        } catch (GenericBusinessException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
        }
    }

    @PostMapping(path = "/list")
    public ResponseEntity<RestResponseSPA<Serializable>> fetchDomains(Pageable pageable, @RequestBody(required = false) DomainName domainName,
            HttpServletRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.ENTITIES_FETCHED.getCode(),
                    GenericMessage.ENTITIES_FETCHED.getMessage(request), (Serializable) domainService.fetchDomains(pageable, domainName,
                    authService.getAuthenticatedUser())));
        } catch (NotAuthorizedException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(),
                    GenericMessage.REQUIRED_FIELDS_MISSING.getMessage(request)));
        }
    }

    @GetMapping(path = "/available/suffix")
    public ResponseEntity<RestResponseSPA<Serializable>> getAvailableSuffix(HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.SUFFIX_DOMAIN_FETCHED.getCode(),
                GenericMessage.SUFFIX_DOMAIN_FETCHED.getMessage(request), domainService.getAvailableSuffix()));
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<RestResponseSPA<Serializable>> deleteDomainName(@PathVariable("id") Long id, HttpServletRequest request) {
        boolean requiredFields = null != id && id != 0;
        if (requiredFields) {
            try {
                domainService.deleteDomainName(id);
                return ResponseEntity.status(HttpStatus.OK).body(
                        new RestResponseSPA<>(GenericMessage.DOMAIN_DELETED.getCode(), GenericMessage.DOMAIN_DELETED.getMessage(request)));
            } catch (BadRequestBusinessException ex) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
            } catch (GenericBusinessException ex) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new RestResponseSPA<>(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(),
                    GenericMessage.REQUIRED_FIELDS_MISSING.getMessage(request)));
        }
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<RestResponseSPA<Serializable>> fetchById(@PathVariable Long id, HttpServletRequest request) {
        boolean requiredFields = null != id && id != 0;
        if (requiredFields) {
            try {
                return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.ENTITIES_FETCHED.getCode(),
                        GenericMessage.ENTITIES_FETCHED.getMessage(request), domainService.fetchDomainTOById(id)));
            } catch (BadRequestBusinessException ex) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new RestResponseSPA<>(GenericMessage.DOMAIN_DOES_NOT_EXIST.getCode(), GenericMessage.DOMAIN_DOES_NOT_EXIST.getMessage(request)));
            }
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(),
                    GenericMessage.REQUIRED_FIELDS_MISSING.getMessage(request)));
        }
    }

}
