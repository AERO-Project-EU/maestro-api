package eu.orchestrator.backend.rest.applicationinstance;

import eu.orchestrator.backend.transfer.ApplicationInstanceGraphTO;
import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.common.exception.NotAuthorizedException;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.security.AuthService;
import eu.orchestrator.backend.transfer.ComponentNodeInstanceTO;
import eu.orchestrator.backend.transfer.IDRuleSetInstanceTO;
import eu.orchestrator.backend.transfer.RestResponseSPA;
import eu.orchestrator.backend.util.Util;
import eu.orchestrator.common.util.NullCheckUtil;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.repository.domain.IDRuleSetInstance;
import eu.orchestrator.backend.service.applicationinstance.ComponentNodeInstanceService;
import eu.orchestrator.transfer.entities.backend.repository.component.ComponentNodeInstanceAffinityDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
public class ComponentNodeInstanceController {

    private static final Logger logger = Logger.getLogger(ComponentNodeInstanceController.class.getName());

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)

    @Autowired
    private AuthService authService;

    @Autowired
    private ComponentNodeInstanceService componentNodeInstanceService;


    // TODO move to security
    @GetMapping(value = "/{id}/componentnode/{componentNodeID}/idrulesetinstances")
    public ResponseEntity<RestResponseSPA<Serializable>> fetchIdRuleSetInstancesByComponentNodeAndId(@PathVariable Long id,
            @PathVariable(value = "componentNodeID") Long componentNodeId, HttpServletRequest request) {
        try {
            List<IDRuleSetInstanceTO> idRuleSetInstanceTOs = componentNodeInstanceService.fetchIDRuleSetInstancesByComponentNodeAndId(id, componentNodeId,
                    authService.getAuthenticatedUser());
            if (NullCheckUtil.isNotEmpty(idRuleSetInstanceTOs)) {
                return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.ENTITIES_FETCHED.getCode(),
                        GenericMessage.ENTITIES_FETCHED.getMessage(request), (Serializable) idRuleSetInstanceTOs));
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

    // TODO move to security
    @PutMapping(value = "/{id}/componentnode/{componentNodeID}/idrulesetinstances")
    public ResponseEntity<RestResponseSPA<Serializable>> updateIdRuleSetInstancesByComponentNodeAndId(@PathVariable Long id,
            @PathVariable(value = "componentNodeID") Long componentNodeId, @RequestBody List<IDRuleSetInstance> idRuleSetInstances,
            HttpServletRequest request) {
        try {
            componentNodeInstanceService.updateIDRuleSetInstancesByComponentNodeAndId(id, componentNodeId, idRuleSetInstances,
                    authService.getAuthenticatedUser());
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } catch (NotAuthorizedException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
    }

    // TODO move to elasticity
    @GetMapping(value = "/{id}/componentnode/{componentNodeHexID}/metrics")
    public ResponseEntity<RestResponseSPA<Serializable>> fetchMetricsByComponentNodeHexId(@PathVariable Long id,
            @PathVariable(value = "componentNodeHexID") String componentNodeHexId, HttpServletRequest request) {
        try {
            List<String> metrics = componentNodeInstanceService.fetchMetricsByComponentNodeHexID(id, componentNodeHexId, authService.getAuthenticatedUser());
            if (NullCheckUtil.isNotEmpty(metrics)) {
                return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.ENTITIES_FETCHED.getCode(),
                        GenericMessage.ENTITIES_FETCHED.getMessage(request), (Serializable) metrics));
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

    // TODO move to elasticity
    @GetMapping(value = "/{id}/componentnode/{componentNodeHexID}/metrics/{metricName}/dimensions")
    public ResponseEntity<RestResponseSPA<Serializable>> fetchDimensionsByComponentNodeHexIdAndMetricNameAndId(@PathVariable Long id,
            @PathVariable(value = "componentNodeHexID") String componentNodeHexId, @PathVariable String metricName, HttpServletRequest request) {
        try {
            List<String> dimensions = componentNodeInstanceService.fetchDimensionsByComponentNodeHexIDAndMetricNameAndId(id, componentNodeHexId, metricName,
                    authService.getAuthenticatedUser());
            if (NullCheckUtil.isNotEmpty(dimensions)) {
                return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.ENTITIES_FETCHED.getCode(),
                        GenericMessage.ENTITIES_FETCHED.getMessage(request), (Serializable) dimensions));
            } else {
                return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.ENTITIES_FETCHED.getCode(),
                        GenericMessage.ENTITIES_FETCHED.getMessage(request), new ArrayList<>()));
            }
        } catch (NotAuthorizedException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
    }

    @GetMapping(value = "/{id}/componentnodeinstance/{cniID}")
    public ResponseEntity<RestResponseSPA<ComponentNodeInstanceTO>> fetchComponentNodeInstanceInfoById(@PathVariable Long id,
            @PathVariable(value = "cniID") Long cniId, HttpServletRequest request) {
        boolean requiredFields = null != id && id > 0 && null != cniId && cniId > 0;
        if (requiredFields) {
            try {
                componentNodeInstanceService.fetchComponentNodeInstanceInfoById(id, cniId, authService.getAuthenticatedUser());
                return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.COMPONENT_NODE_INSTANCE_FETCHED.getCode(),
                        GenericMessage.COMPONENT_NODE_INSTANCE_FETCHED.getMessage(request),
                        componentNodeInstanceService.fetchComponentNodeInstanceInfoById(id, cniId, authService.getAuthenticatedUser())));
            } catch (NotAuthorizedException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
            } catch (GenericBusinessException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(ex.getMessage(),
                        ex.getGenericMessage().getMessage(request)));
            } catch (Exception ex) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                        GenericMessage.GENERIC_ERROR.getMessage(request)));
            }
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(),
                    GenericMessage.REQUIRED_FIELDS_MISSING.getMessage(request)));
        }
    }

    //Fetch all componentNode instances
    @GetMapping(value = "/{id}/componentnodeinstance")
    public ResponseEntity<RestResponseSPA<Serializable>> retrieveAllComponentNodeInstances(@PathVariable Long id, HttpServletRequest request) {
        List<ComponentNodeInstance> componentNodeInstanceList = componentNodeInstanceService.retrieveAllComponentNodeInstances(id);
        if (NullCheckUtil.isNotEmpty(componentNodeInstanceList)) {
            return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.COMPONENT_NODE_INSTANCE_FETCHED.getCode(),
                    GenericMessage.COMPONENT_NODE_INSTANCE_FETCHED.getMessage(request), (Serializable) componentNodeInstanceList));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                GenericMessage.GENERIC_ERROR.getMessage(request)));
    }


    @PostMapping(value = "/{id}/componentnodeinstance/list")
    public ResponseEntity<RestResponseSPA<Serializable>> fetchComponentNodeInstances(Pageable pageable,
        @RequestBody(required = false) ApplicationInstance applicationInstance, @PathVariable Long id,  HttpServletRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.ENTITIES_FETCHED.getCode(),
                    GenericMessage.ENTITIES_FETCHED.getMessage(request),
                    (Serializable) componentNodeInstanceService.fetchComponentNodeInstanceList(pageable, applicationInstance, id, authService.getAuthenticatedUser())));
        } catch (NotAuthorizedException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(),
                    ex.getGenericMessage().getMessage(request)));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
    }
    @PutMapping(value = "/{id}/componentnodeinstance/{cniID}")
    public ResponseEntity<RestResponseSPA<Serializable>> updateComponentNodeInstanceInfoById(@PathVariable Long id, @PathVariable(value = "cniID") Long cniId,
            @RequestBody ComponentNodeInstanceTO componentNodeInstanceTo, HttpServletRequest request) {
        boolean requiredFields = null != id && id > 0 && null != cniId && cniId > 0 && null != componentNodeInstanceTo
                && null != componentNodeInstanceTo.getComponentNodeInstanceID() && componentNodeInstanceTo.getComponentNodeInstanceID().equals(cniId);
        if (requiredFields) {
            try {
                componentNodeInstanceService.updateComponentNodeInstanceInfoById(id, cniId, componentNodeInstanceTo, authService.getAuthenticatedUser());
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
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(),
                    GenericMessage.REQUIRED_FIELDS_MISSING.getMessage(request)));
        }
    }


    @PostMapping(value = "/{id}/componentnodeinstance/{cniID}/affinity")
    public ResponseEntity<RestResponseSPA<ApplicationInstanceGraphTO>> componentNodeInstanceAffinity(@PathVariable Long id, @PathVariable(value = "cniID") Long cniId,
            @RequestBody ComponentNodeInstanceAffinityDto cniAffinityDto, HttpServletRequest request) {

        boolean requiredFields = null != cniAffinityDto
                && null != cniAffinityDto.getApplicationInstanceId() && cniAffinityDto.getApplicationInstanceId().equals(id)
                && null != cniAffinityDto.getComponentNodeInstanceId() && cniAffinityDto.getComponentNodeInstanceId().equals(cniId)
                && null != cniAffinityDto.getAffinityLabels() && ! cniAffinityDto.getAffinityLabels().isEmpty();

        if (requiredFields) {
            try {
                componentNodeInstanceService.createCNIAffinity(cniAffinityDto, authService.getAuthenticatedUser());


                return ResponseEntity.status(HttpStatus.CREATED).body(new RestResponseSPA<>(GenericMessage.COMPONENT_NODE_INSTANCE_AFFINITY_CREATED.getCode(),
                        GenericMessage.COMPONENT_NODE_INSTANCE_AFFINITY_CREATED.getMessage(request),null));
            } catch (NotAuthorizedException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new RestResponseSPA<>(ex.getMessage(),
                        ex.getGenericMessage().getMessage(request)));
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
