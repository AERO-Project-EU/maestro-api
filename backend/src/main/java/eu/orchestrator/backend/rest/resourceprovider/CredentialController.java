package eu.orchestrator.backend.rest.resourceprovider;

import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.transfer.RestResponseSPA;
import eu.orchestrator.backend.util.Util;
import eu.orchestrator.common.util.NullCheckUtil;
import eu.orchestrator.repository.domain.ProviderType;
import eu.orchestrator.repository.domain.ProviderType.ProviderName;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorProviderAuthenticationDetails;
import eu.orchestrator.backend.service.k8s.KubernetesService;
import eu.orchestrator.backend.service.resourceprovider.ProviderTypeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/provider")
public class CredentialController {

    private static final Logger logger = Logger.getLogger(CredentialController.class.getName());

    @Autowired
    private ProviderTypeService providerTypeService;

    @Autowired
    private KubernetesService kubernetesService;

    @Value("${token.signer.secret}")
    private String tokenSecret;

    @Value("${vim.server.url}")
    private String vimUrl;


    @PostMapping(value = "/credential/validate")
    public ResponseEntity<RestResponseSPA<Serializable>> validateCredentials(
            @RequestBody OrchestratorProviderAuthenticationDetails orchestratorProviderAuthenticationDetails, HttpServletRequest request) {

        // check if adapterType
        if (null == orchestratorProviderAuthenticationDetails.getAdapterType() || orchestratorProviderAuthenticationDetails.getAdapterType().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new RestResponseSPA<>(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(),
                    GenericMessage.REQUIRED_FIELDS_MISSING.getMessage(request)));
        }

        // check if adapterType is OPENSTACK or IOT_GATEWAY or FIFTH_GENERATION_TELCO_PROVIDER
        // if REQUIRED_FIELDS_MISSING
        if ((orchestratorProviderAuthenticationDetails.getAdapterType().equals(ProviderName.OPENSTACK.getFriendlyName())
                || orchestratorProviderAuthenticationDetails.getAdapterType().equals(ProviderName.IOT_GATEWAY.getFriendlyName())
                || (orchestratorProviderAuthenticationDetails.getAdapterType().equals(ProviderName.FIFTH_GENERATION_TELCO_PROVIDER.getFriendlyName())))
                && (null == orchestratorProviderAuthenticationDetails.getEndpoint() || orchestratorProviderAuthenticationDetails.getEndpoint().isEmpty()
                || null == orchestratorProviderAuthenticationDetails.getUsername() || orchestratorProviderAuthenticationDetails.getUsername().isEmpty()
                || null == orchestratorProviderAuthenticationDetails.getPassword() || orchestratorProviderAuthenticationDetails.getPassword().isEmpty()
                || null == orchestratorProviderAuthenticationDetails.getDomain() || orchestratorProviderAuthenticationDetails.getDomain().isEmpty()
                || null == orchestratorProviderAuthenticationDetails.getProject()
                || orchestratorProviderAuthenticationDetails.getProject().isEmpty())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new RestResponseSPA<>(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(),
                    GenericMessage.REQUIRED_FIELDS_MISSING.getMessage(request)));
        } else {
            orchestratorProviderAuthenticationDetails.setPassword(Util.encrypt(orchestratorProviderAuthenticationDetails.getPassword(), tokenSecret));
        }

        // check if adapterType is AWS
        // if REQUIRED_FIELDS_MISSING
        if ((orchestratorProviderAuthenticationDetails.getAdapterType().equals(ProviderName.AWS.getFriendlyName()))
                && (null == orchestratorProviderAuthenticationDetails.getPrivateKey() || orchestratorProviderAuthenticationDetails.getPrivateKey().isEmpty()
                || null == orchestratorProviderAuthenticationDetails.getPublicKey()
                || orchestratorProviderAuthenticationDetails.getPublicKey().isEmpty())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new RestResponseSPA<>(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(),
                    GenericMessage.REQUIRED_FIELDS_MISSING.getMessage(request)));
        }

        // check if adapterType is GCC
        // if REQUIRED_FIELDS_MISSING
        if ((orchestratorProviderAuthenticationDetails.getAdapterType().equals(ProviderName.GCC.getFriendlyName()))
                && (null == orchestratorProviderAuthenticationDetails.getPrivateKey() || orchestratorProviderAuthenticationDetails.getPrivateKey().isEmpty()
                || null == orchestratorProviderAuthenticationDetails.getUsername()
                || orchestratorProviderAuthenticationDetails.getUsername().isEmpty())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new RestResponseSPA<>(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(),
                    GenericMessage.REQUIRED_FIELDS_MISSING.getMessage(request)));
        }

        ProviderType providerType = providerTypeService.fetchByFriendlyName(orchestratorProviderAuthenticationDetails.getAdapterType());
        logger.info("Provider NAME TYPE:" + orchestratorProviderAuthenticationDetails.getAdapterType());
        //check if providerType Exist
        if (providerType == null) {
            providerType = providerTypeService.fetchByName(orchestratorProviderAuthenticationDetails.getAdapterType());
            if (providerType == null) {
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
                        .body(new RestResponseSPA<>(GenericMessage.PROVIDER_TYPE_NOT_EXIST.getCode(),
                                GenericMessage.PROVIDER_TYPE_NOT_EXIST.getMessage(request)));
            }
        }

        //TODO REVIEW FOR K8S
        //Check if ProviderName is Astrid Kubernetes
        if (orchestratorProviderAuthenticationDetails.getAdapterType().equals(ProviderName.RAINBOW_KUBERNETES.getFriendlyName())
                || orchestratorProviderAuthenticationDetails.getAdapterType().equalsIgnoreCase(ProviderName.KUBERNETES.getFriendlyName())
                || orchestratorProviderAuthenticationDetails.getAdapterType().equalsIgnoreCase(ProviderName.RAINBOW_KUBERNETES.name())
                || orchestratorProviderAuthenticationDetails.getAdapterType().equalsIgnoreCase(ProviderName.KUBERNETES.name())
                || orchestratorProviderAuthenticationDetails.getAdapterType().equalsIgnoreCase(ProviderName.KUBERNETES_KNATIVE.getFriendlyName())
                || orchestratorProviderAuthenticationDetails.getAdapterType().equalsIgnoreCase(ProviderName.KUBERNETES_KNATIVE.name())) {
            // if REQUIRED_FIELDS_MISSING
            if (NullCheckUtil.isEmpty(orchestratorProviderAuthenticationDetails.getEndpoint())
                    || NullCheckUtil.isEmpty(orchestratorProviderAuthenticationDetails.getUsername())
                    || NullCheckUtil.isEmpty(orchestratorProviderAuthenticationDetails.getPublicKey())
                    || NullCheckUtil.isEmpty(orchestratorProviderAuthenticationDetails.getPrivateKey())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new RestResponseSPA<>(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(),
                        GenericMessage.REQUIRED_FIELDS_MISSING.getMessage(request)));
            } else {
                if (kubernetesService.checkLogin(orchestratorProviderAuthenticationDetails)) {
                    return ResponseEntity.status(HttpStatus.OK).body(new RestResponseSPA<>(GenericMessage.PROVIDER_CREDENTIALS_SUCCESS.getCode(),
                            GenericMessage.PROVIDER_CREDENTIALS_SUCCESS.getMessage(request)));
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new RestResponseSPA<>(GenericMessage.PROVIDER_CREDENTIALS_FAIL.getCode(),
                            GenericMessage.PROVIDER_CREDENTIALS_FAIL.getMessage(request)));
                }
            }
        }

        //Check if ProviderName is 5G OSS
        if (orchestratorProviderAuthenticationDetails.getAdapterType().equals(ProviderName.FIFTH_GENERATION_OSS_KUBERNETES_PROVIDER.getFriendlyName())) {
            // if REQUIRED_FIELDS_MISSING
            if (NullCheckUtil.isEmpty(orchestratorProviderAuthenticationDetails.getUsername())
                    || NullCheckUtil.isEmpty(orchestratorProviderAuthenticationDetails.getPrivateKey())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new RestResponseSPA<>(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(),
                        GenericMessage.REQUIRED_FIELDS_MISSING.getMessage(request)));
            } else {
                // TODO: 17/1/22 Check login to test connection 
            }
        }

        orchestratorProviderAuthenticationDetails.setAdapterImplementation(providerType.getAdapterImplementation());

        RestTemplate restTemplate = new RestTemplate();
        String vimUri = vimUrl + "/api/v1/credential/validate";
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(vimUri, orchestratorProviderAuthenticationDetails, String.class);

        if (responseEntity.getBody().contains("SUCCESS")) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new RestResponseSPA<>(GenericMessage.PROVIDER_CREDENTIALS_SUCCESS.getCode(),
                            GenericMessage.PROVIDER_CREDENTIALS_SUCCESS.getMessage(request)));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new RestResponseSPA<>(GenericMessage.PROVIDER_CREDENTIALS_FAIL.getCode(),
                            GenericMessage.PROVIDER_CREDENTIALS_FAIL.getMessage(request)));
        }
    }


}
