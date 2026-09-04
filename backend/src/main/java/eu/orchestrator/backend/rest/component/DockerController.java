package eu.orchestrator.backend.rest.component;

import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.transfer.DockerTO;
import eu.orchestrator.backend.transfer.RestResponseSPA;
import eu.orchestrator.backend.service.component.DockerService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/docker")
public class DockerController {

    @Autowired
    private DockerService dockerService;


    @PostMapping(value = "/credentials")
    public ResponseEntity<RestResponseSPA<Serializable>> credentialsValidator(@RequestBody DockerTO dockerTo, HttpServletRequest request) {
        // check if required field missing
        if (dockerTo.getDockerRegistry() == null || dockerTo.getDockerPassword().isEmpty() || dockerTo.getDockerPassword() == null
                || dockerTo.getDockerUsername().isEmpty() || dockerTo.getDockerUsername() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new RestResponseSPA<>(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(),
                    GenericMessage.REQUIRED_FIELDS_MISSING.getMessage(request)));
        }
        if (dockerService.credentialsValidator(dockerTo)) {
            return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.DOCKER_CREDENTIALS_VALID.getCode(),
                    GenericMessage.DOCKER_CREDENTIALS_VALID.getMessage(request)));
        }
        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new RestResponseSPA<>(GenericMessage.DOCKER_CREDENTIALS_INVALID.getCode(),
                GenericMessage.DOCKER_CREDENTIALS_INVALID.getMessage(request)));
    }

}
