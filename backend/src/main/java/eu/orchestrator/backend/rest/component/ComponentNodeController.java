package eu.orchestrator.backend.rest.component;

import eu.orchestrator.backend.transfer.DockerTO;
import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.common.exception.NotAuthorizedException;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.security.AuthService;
import eu.orchestrator.backend.transfer.RestResponseSPA;
import eu.orchestrator.repository.domain.Component;
import eu.orchestrator.backend.service.component.ComponentNodeService;

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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/component")
public class ComponentNodeController {

    private static final Logger logger = Logger.getLogger(ComponentNodeController.class.getName());

    @Autowired
    private AuthService authService;

    @Autowired
    private ComponentNodeService componentNodeService;


    @PostMapping(value = "/list/all")
    public ResponseEntity<RestResponseSPA<Serializable>> fetchAllComponents(@RequestParam(required = false, value = "filters") String filters,
            @RequestBody(required = false) Component component, HttpServletRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.ENTITIES_FETCHED.getCode(),
                    GenericMessage.ENTITIES_FETCHED.getMessage(request), (Serializable) componentNodeService.fetchAllComponents(filters, component,
                    authService.getAuthenticatedUser())));
        } catch (NotAuthorizedException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
    }

    @PostMapping(value = "/list")
    public ResponseEntity<RestResponseSPA<Serializable>> fetchComponents(Pageable pageable, @RequestParam(required = false, value = "filters") String filters,
            @RequestBody(required = false) Component component, HttpServletRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.ENTITIES_FETCHED.getCode(),
                    GenericMessage.ENTITIES_FETCHED.getMessage(request), (Serializable) componentNodeService.fetchComponents(pageable, filters, component,
                    authService.getAuthenticatedUser())));
        } catch (NotAuthorizedException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
    }

    @PostMapping(value = "/filtered")
    public ResponseEntity<RestResponseSPA<Serializable>> fetchFilteredComponents(Pageable pageable, @RequestBody(required = false) Component component,
            HttpServletRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.ENTITIES_FETCHED.getCode(),
                    GenericMessage.ENTITIES_FETCHED.getMessage(request), (Serializable) componentNodeService.fetchFilteredComponents(pageable, component,
                    authService.getAuthenticatedUser())));
        } catch (NotAuthorizedException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
    }

    @GetMapping(value = "{id}/candidates/{interfaceID}")
    public ResponseEntity<RestResponseSPA<Serializable>> fetchCandidatesByInterfaceId(@PathVariable Long id,
            @PathVariable(value = "interfaceID") Long interfaceId, @RequestParam(value = "name", required = false) String name,
            HttpServletRequest request) {
        boolean requiredFields = null != interfaceId && interfaceId != 0 && null != id && id != 0;
        if (requiredFields) {
            try {
                List<Component> candidates = componentNodeService.fetchCandidatesByInterfaceId(id, interfaceId, name, authService.getAuthenticatedUser());
                return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.COMPONENT_CANDIDATES_FETCHED.getCode(),
                        GenericMessage.COMPONENT_CANDIDATES_FETCHED.getMessage(request), (Serializable) candidates));
            } catch (NotAuthorizedException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
            } catch (Exception ex) {
                logger.log(Level.SEVERE, ex.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                        GenericMessage.GENERIC_ERROR.getMessage(request)));
            }
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(),
                    GenericMessage.REQUIRED_FIELDS_MISSING.getMessage(request)));
        }

    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<RestResponseSPA<Component>> fetchById(@PathVariable Long id, HttpServletRequest request) {
        boolean requiredFields = null != id && id != 0;
        if (requiredFields) {
            try {
                return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.COMPONENT_FETCHED.getCode(),
                        GenericMessage.COMPONENT_FETCHED.getMessage(request), componentNodeService.fetchById(id, authService.getAuthenticatedUser())));
            } catch (NotAuthorizedException ex) {
                logger.log(Level.SEVERE, ex.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(GenericMessage.COMPONENT_FETCH_NOT_ALLOWED.getCode(),
                        GenericMessage.COMPONENT_FETCH_NOT_ALLOWED.getMessage(request)));
            } catch (Exception ex) {
                logger.log(Level.SEVERE, ex.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                        GenericMessage.GENERIC_ERROR.getMessage(request)));
            }
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(),
                    GenericMessage.REQUIRED_FIELDS_MISSING.getMessage(request)));
        }
    }

    @GetMapping(value = "/docker/credentials/{id}")
    public ResponseEntity<RestResponseSPA<DockerTO>> fetchDockerCredentials(@PathVariable Long id, HttpServletRequest request) {
        boolean requiredFields = null != id && id != 0;
        if (requiredFields) {
            try {
                return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.COMPONENT_FETCHED.getCode(),
                    GenericMessage.COMPONENT_FETCHED.getMessage(request), componentNodeService.fetchDockerCredentials(id, authService.getAuthenticatedUser())));
            } catch (NotAuthorizedException ex) {
                logger.log(Level.SEVERE, ex.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(GenericMessage.COMPONENT_FETCH_NOT_ALLOWED.getCode(),
                    GenericMessage.COMPONENT_FETCH_NOT_ALLOWED.getMessage(request)));
            } catch (Exception ex) {
                logger.log(Level.SEVERE, ex.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
            }
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(),
                GenericMessage.REQUIRED_FIELDS_MISSING.getMessage(request)));
        }
    }

    @PostMapping
    public ResponseEntity<RestResponseSPA<Serializable>> create(@RequestBody Component component, HttpServletRequest request) {
        // Required Fields
        boolean requiredFields = null != component.getName() && !component.getName().isEmpty()
                && null != component.getDockerImage() && !component.getDockerImage().isEmpty()
                && null != component.getElasticityController() && !component.getElasticityController().isEmpty()
                && null != component.getDockerCredentialsUsing() && null != component.getDockerCustomRegistry()
                && null != component.getArchitecture() && !component.getArchitecture().isEmpty()
                && null != component.getPublicComponent() && null != component.getRequirement()
                && null != component.getRequirement().getRam() && null != component.getRequirement().getvCPUs()
                && null != component.getRequirement().getStorage() && null != component.getRequirement().getHypervisorType()
                && null != component.getHealthCheck() && null != component.getHealthCheck().getInterval()
                && (component.getHealthCheck().getInterval() > 0) && ((null != component.getHealthCheck().getHttpURL()
                && !component.getHealthCheck().getHttpURL().isEmpty()) || (null != component.getHealthCheck().getArgs()
                && !component.getHealthCheck().getArgs().isEmpty()));
        if (requiredFields) {
            try {
                componentNodeService.create(component, authService.getAuthenticatedUser());
                return ResponseEntity.status(HttpStatus.CREATED).build();
            } catch (NotAuthorizedException ex) {
                logger.log(Level.SEVERE, ex.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
            } catch (GenericBusinessException ex) {
                logger.log(Level.SEVERE, ex.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
            } catch (NumberFormatException ex) {
                logger.log(Level.SEVERE, ex.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new RestResponseSPA<>(GenericMessage.REQUIRED_FIELDS_WITH_PROPER_VALUES.getCode(),
                        GenericMessage.REQUIRED_FIELDS_WITH_PROPER_VALUES.getMessage(request)));
            } catch (Exception ex) {
                logger.log(Level.SEVERE, ex.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                        GenericMessage.GENERIC_ERROR.getMessage(request)));
            }
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(),
                    GenericMessage.REQUIRED_FIELDS_MISSING.getMessage(request)));
        }

    }

    @PutMapping
    public ResponseEntity<RestResponseSPA<Serializable>> update(@RequestBody Component component, HttpServletRequest request) {
        // Required Fields
        boolean requiredFields = null != component.getName() && !component.getName().isEmpty() && null != component.getDockerImage()
                && !component.getDockerImage().isEmpty() && null != component.getElasticityController() && !component.getElasticityController().isEmpty()
                && null != component.getDockerCredentialsUsing() && null != component.getDockerCustomRegistry() && null != component.getArchitecture()
                && !component.getArchitecture().isEmpty() && null != component.getPublicComponent() && null != component.getRequirement()
                && null != component.getRequirement().getRam() && null != component.getRequirement().getvCPUs()
                && null != component.getRequirement().getStorage() && null != component.getRequirement().getHypervisorType()
                && null != component.getHealthCheck() && null != component.getHealthCheck().getInterval() && (component.getHealthCheck().getInterval() > 0)
                && ((null != component.getHealthCheck().getHttpURL() && !component.getHealthCheck().getHttpURL().isEmpty())
                || (null != component.getHealthCheck().getArgs() && !component.getHealthCheck().getArgs().isEmpty()));
        if (requiredFields) {
            try {
                if (componentNodeService.updateWithInterfaces(component, authService.getAuthenticatedUser())) {
                    return ResponseEntity.status(HttpStatus.ACCEPTED).build();
                } else {
                    return ResponseEntity.status(HttpStatus.ACCEPTED).body(new RestResponseSPA<>(GenericMessage.COMPONENT_UPDATE_EXCEPT_INTERFACES.getCode(),
                            GenericMessage.COMPONENT_UPDATE_EXCEPT_INTERFACES.getMessage(request)));
                }
            } catch (NotAuthorizedException ex) {
                logger.log(Level.SEVERE, ex.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
            } catch (GenericBusinessException ex) {
                logger.log(Level.SEVERE, ex.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(ex.getMessage(),
                        ex.getGenericMessage().getMessage(request)));
            } catch (NumberFormatException ex) {
                logger.log(Level.SEVERE, ex.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new RestResponseSPA<>(GenericMessage.REQUIRED_FIELDS_WITH_PROPER_VALUES.getCode(),
                        GenericMessage.REQUIRED_FIELDS_WITH_PROPER_VALUES.getMessage(request)));
            } catch (Exception ex) {
                logger.log(Level.SEVERE, ex.getMessage());
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
                componentNodeService.delete(id, authService.getAuthenticatedUser());
                return ResponseEntity.status(HttpStatus.OK).build();
            } catch (NotAuthorizedException ex) {
                logger.log(Level.SEVERE, ex.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
            } catch (GenericBusinessException ex) {
                logger.log(Level.SEVERE, ex.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(ex.getMessage(),
                        ex.getGenericMessage().getMessage(request)));
            } catch (Exception ex) {
                logger.log(Level.SEVERE, ex.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                        GenericMessage.GENERIC_ERROR.getMessage(request)));
            }
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(),
                    GenericMessage.REQUIRED_FIELDS_MISSING.getMessage(request)));
        }
    }

    @GetMapping(value = "/count")
    public ResponseEntity<RestResponseSPA<Long>> count(HttpServletRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.GENERIC_SUCCESS.getCode(),
                    GenericMessage.GENERIC_SUCCESS.getMessage(request), componentNodeService.count(authService.getAuthenticatedUser())));
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
