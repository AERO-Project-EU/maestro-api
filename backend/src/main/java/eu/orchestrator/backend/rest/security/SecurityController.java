package eu.orchestrator.backend.rest.security;

import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.transfer.RestResponseSPA;
import eu.orchestrator.backend.service.k8s.KubernetesService;
import eu.orchestrator.backend.service.model.NetworkPolicyNodeRequest;
import eu.orchestrator.backend.service.model.NetworkPolicyRequest;

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
@RequestMapping("/api/v1/security")
@SuppressWarnings("Duplicates")
public class SecurityController {

    @Autowired
    private KubernetesService kubernetesService;


    @PostMapping(value = "/applyNetworkPolicy")
    public ResponseEntity<RestResponseSPA<Serializable>> applyNetworkPolicy(@RequestBody NetworkPolicyRequest networkPolicyRequest,
            HttpServletRequest request) {
        if (kubernetesService.networkPolicyApply(networkPolicyRequest.getComponentNodeInstanceHexId(), networkPolicyRequest.getIp())) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
    }

    @PostMapping(value = "/applyNetworkPolicyNode")
    public ResponseEntity<RestResponseSPA<Serializable>> applyNetworkPolicyNode(@RequestBody NetworkPolicyNodeRequest networkPolicyNodeRequest,
            HttpServletRequest request) {
        if (kubernetesService.applyNetworkPolicyWithNodeCheck(networkPolicyNodeRequest.getComponentNodeInstanceHexId(), networkPolicyNodeRequest.getIp(),
                networkPolicyNodeRequest.getNodeName())) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new RestResponseSPA<>(GenericMessage.GENERIC_ERROR.getCode(),
                    GenericMessage.GENERIC_ERROR.getMessage(request)));
        }
    }

}
