package eu.orchestrator.backend.rest.component;

import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.transfer.RestResponseSPA;
import eu.orchestrator.backend.service.component.ElasticityBackendService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

@RestController("ElasticityController")
@RequestMapping("/api/v1/component")
@Transactional(rollbackOn = Exception.class)
public class ElasticityController {

    @Autowired
    private ElasticityBackendService elasticityBackendService;


    @GetMapping(value = "/retrieve/elasticity/controllers")
    public ResponseEntity<RestResponseSPA<Serializable>> retrieveElasticityControllers(HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.GENERIC_SUCCESS.getCode(),
                GenericMessage.GENERIC_SUCCESS.getMessage(request), (Serializable) elasticityBackendService.retrieveElasticityControllers()));
    }

    @GetMapping(value = "/retrieve/elasticity/{elasticityController}/modes")
    public ResponseEntity<RestResponseSPA<Serializable>> retrieveElasticityControllerModes(@PathVariable("elasticityController") String elasticityController,
            HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.GENERIC_SUCCESS.getCode(),
                GenericMessage.GENERIC_SUCCESS.getMessage(request),
                (Serializable) elasticityBackendService.retrieveElasticityControllerModes(elasticityController)));
    }

}
