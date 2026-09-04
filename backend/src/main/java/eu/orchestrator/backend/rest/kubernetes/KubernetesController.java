package eu.orchestrator.backend.rest.kubernetes;

import eu.orchestrator.backend.service.k8s.KubernetesService;
import eu.orchestrator.backend.transfer.PolicyEngineKubernetesConfigTO;
import eu.orchestrator.backend.transfer.RestResponseSPA;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.transfer.entities.kubernetes.ClusterLabelsDto;
import eu.orchestrator.transfer.entities.kubernetes.NamespaceDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/kubernetes")
public class KubernetesController {

    private static final Logger logger = Logger.getLogger(KubernetesController.class.getName());

    @Autowired
    private KubernetesService kubernetesService;


    @GetMapping(path = "/namespace/applicationInstanceId/{applicationInstanceId}")
    public ResponseEntity<RestResponseSPA<NamespaceDto>> fetchNamespaceByApplicationInstance(@PathVariable Long applicationInstanceId,
            HttpServletRequest request) {
        boolean requiredFields = null != applicationInstanceId && applicationInstanceId != 0;
        if (requiredFields) {
            try {
                return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.NAMESPACE_FETCHED.getCode(),
                        GenericMessage.NAMESPACE_FETCHED.getMessage(request), kubernetesService.fetchNamespaceByApplicationInstance(applicationInstanceId)));
            } catch (GenericBusinessException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new RestResponseSPA<>(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(),
                    GenericMessage.REQUIRED_FIELDS_MISSING.getMessage(request)));
        }
    }

    @GetMapping(path = "/provider/applicationInstanceHexId/{applicationInstanceHexId}")
    public ResponseEntity<RestResponseSPA<PolicyEngineKubernetesConfigTO>> fetchPolicyEngineK8sConfigByApplicationInstanceHexId(@PathVariable String applicationInstanceHexId,
            HttpServletRequest request) {
        boolean requiredFields = null != applicationInstanceHexId && !applicationInstanceHexId.isEmpty();
        if (requiredFields) {
            try {
                return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.PROVIDER_FETCHED.getCode(),
                        GenericMessage.PROVIDER_FETCHED.getMessage(request), kubernetesService.fetchPolicyEngineK8sConfigByApplicationInstanceHexId(applicationInstanceHexId)));
            } catch (GenericBusinessException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new RestResponseSPA<>(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(),
                    GenericMessage.REQUIRED_FIELDS_MISSING.getMessage(request)));
        }
    }

    @GetMapping(path = "/cluster/labels/provider/{providerId}")
    public ResponseEntity<RestResponseSPA<ClusterLabelsDto>> fetchK8sLabelsByProvider(@PathVariable Long providerId, HttpServletRequest request){

        boolean requiredFields = null != providerId && providerId != 0;
        if (requiredFields) {
            try {
                return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.CLUSTER_LABELS_FETCHED.getCode(),
                        GenericMessage.CLUSTER_LABELS_FETCHED.getMessage(request), kubernetesService.fetchClusterLabels(providerId)));
            } catch (GenericBusinessException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new RestResponseSPA<>(ex.getMessage(), ex.getGenericMessage().getMessage(request)));
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new RestResponseSPA<>(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(),
                    GenericMessage.REQUIRED_FIELDS_MISSING.getMessage(request)));
        }
    }

}
