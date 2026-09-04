package eu.orchestrator.backend.service.component;

import eu.orchestrator.backend.transfer.DockerTO;
import eu.orchestrator.backend.util.DockerUtil;

import org.springframework.stereotype.Service;

@Service
public class DockerService {

    public boolean credentialsValidator(DockerTO dockerTO) {
        return DockerUtil.dockerCredentialsValidator(dockerTO.getDockerUsername(), dockerTO.getDockerPassword(), dockerTO.getDockerRegistry());
    }

}
