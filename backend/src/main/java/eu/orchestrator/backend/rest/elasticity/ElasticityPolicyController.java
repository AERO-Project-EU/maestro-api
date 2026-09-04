package eu.orchestrator.backend.rest.elasticity;

import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.common.exception.NotAuthorizedException;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.security.AuthService;
import eu.orchestrator.backend.transfer.RestResponseSPA;
import eu.orchestrator.repository.domain.RuntimePolicy;
import eu.orchestrator.backend.service.elasticity.ElasticityService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/applicationinstance")
public class ElasticityPolicyController {

    private static final Logger logger = Logger.getLogger(ElasticityPolicyController.class.getName());

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)

    @Autowired
    private AuthService authService;

    @Autowired
    private ElasticityService elasticityService;


    @PostMapping(value = "/{id}/runtimepolicies/{type}/list")
    public ResponseEntity<RestResponseSPA<Serializable>> fetchRuntimePoliciesByTypeAndId(@PathVariable Long id, @PathVariable String type,
            Pageable pageable, HttpServletRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new RestResponseSPA<>(GenericMessage.ENTITIES_FETCHED.getCode(), GenericMessage.ENTITIES_FETCHED.getMessage(request),
                            (Serializable) elasticityService.fetchRuntimePoliciesByTypeAndId(id, pageable, authService.getAuthenticatedUser())));
        } catch (NotAuthorizedException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
        } catch (GenericBusinessException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
    }

    @PostMapping(value = "/{id}/runtimepolicies/elasticity")
    public ResponseEntity<RestResponseSPA<Serializable>> createElasticityRuntimePolicyById(@PathVariable Long id, @RequestBody RuntimePolicy runtimePolicy,
            HttpServletRequest request) {
        try {
            elasticityService.createElasticityRuntimePolicyById(id, runtimePolicy, authService.getAuthenticatedUser());
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

    @DeleteMapping(value = "/{id}/runtimepolicies/elasticity/{policyID}")
    public ResponseEntity<RestResponseSPA<Serializable>> deleteElasticityRuntimePolicyById(@PathVariable Long id, @PathVariable Long policyId,
            HttpServletRequest request) {
        try {
            elasticityService.deleteElasticityRuntimePolicyById(id, policyId, authService.getAuthenticatedUser());
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
    }

    // Security Policies
    @PutMapping(value = "/{id}/runtimepolicies/security")
    public ResponseEntity<RestResponseSPA<Serializable>> createSecurityRuntimePolicyById(@PathVariable Long id,
            @RequestBody List<RuntimePolicy> runtimePolicies, HttpServletRequest request) {
        try {
            elasticityService.createSecurityRuntimePolicyById(id, runtimePolicies, authService.getAuthenticatedUser());
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
    }

}
