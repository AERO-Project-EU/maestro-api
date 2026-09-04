package eu.orchestrator.backend.rest.elasticity;

import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.transfer.RestResponseSPA;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorApplicationInstance;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorScalingRequest;
import eu.orchestrator.backend.service.elasticity.ScalingBackendService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

@Transactional(rollbackOn = Exception.class)
@RestController
@RequestMapping("/api/v1/scaling")
public class ScalingController {

    private static final Logger logger = Logger.getLogger(ScalingController.class.getName());

    @Autowired
    private ScalingBackendService scalingBackendService;


    @PostMapping(value = "/up")
    public ResponseEntity<RestResponseSPA<OrchestratorApplicationInstance>> scaleUpFunctionality(
            @RequestBody OrchestratorScalingRequest orchestratorScalingRequest, HttpServletRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new RestResponseSPA<>(GenericMessage.GENERIC_SUCCESS.getCode(), GenericMessage.COMPONENT_NODE_INSTANCE_SCALE_UP.getMessage(request),
                            scalingBackendService.scaleUpFunctionality(orchestratorScalingRequest)));
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

    @PostMapping(value = "/down")
    public ResponseEntity<RestResponseSPA<Serializable>> scaleDownFunctionality(@RequestBody OrchestratorScalingRequest orchestratorScalingRequest,
            HttpServletRequest request) {
        try {
            scalingBackendService.scaleDownFunctionality(orchestratorScalingRequest);
            return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.GENERIC_SUCCESS.getCode(),
                    GenericMessage.COMPONENT_NODE_INSTANCE_SCALE_DOWN.getMessage(request)));
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
