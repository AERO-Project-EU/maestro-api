package eu.orchestrator.backend.service.resourceprovider;

import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.repository.domain.Provider;
import eu.orchestrator.repository.domain.ProviderType;
import eu.orchestrator.repository.domain.User;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorProviderAuthenticationDetails;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.logging.Logger;
import jakarta.transaction.Transactional;

@Service
@Transactional(rollbackOn = Exception.class)
public class TopologyService {

    private static final Logger logger = Logger.getLogger(TopologyService.class.getName());
    private static final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private ProviderService providerService;

    @Value("${vim.server.url}")
    private String vimURL;


    public String fetchIoTTopology(Long providerID, User authenticatedUser) {
        Provider provider = providerService.findById(providerID);

        if (provider != null && provider.getProviderType().getName().equals(ProviderType.ProviderName.IOT_GATEWAY.name())
                && (provider.getUser().getId().equals(authenticatedUser.getId())
                || provider.getOrganization().getId().equals(authenticatedUser.getOrganization().getId()))) {

            OrchestratorProviderAuthenticationDetails authenticationDetails = new OrchestratorProviderAuthenticationDetails();
            authenticationDetails.setId("" + providerID);
            authenticationDetails.setName(null != provider.getName() && !provider.getName().isEmpty() ? provider.getName() : null);
            authenticationDetails.setAdapterImplementation(null != provider.getProviderType() ? provider.getProviderType().getAdapterImplementation() : null);
            authenticationDetails.setAdapterType(null != provider.getProviderType() ? provider.getProviderType().getName() : null);
            authenticationDetails.setEndpoint(null != provider.getEndpoint() && !provider.getEndpoint().isEmpty() ? provider.getEndpoint() : null);
            authenticationDetails.setUsername(null != provider.getUsername() && !provider.getUsername().isEmpty() ? provider.getUsername() : null);
            authenticationDetails.setPassword(null != provider.getPassword() && !provider.getPassword().isEmpty() ? provider.getPassword() : null);
            authenticationDetails.setMeshIdentifier(null != provider.getMeshIdentifier() && !provider.getMeshIdentifier().isEmpty()
                    ? provider.getMeshIdentifier() : null);
            authenticationDetails.setImageID(null != provider.getImageID() && !provider.getImageID().isEmpty() ? provider.getImageID() : null);
            authenticationDetails.setNetworkID(null != provider.getNetworkID() && !provider.getNetworkID().isEmpty() ? provider.getNetworkID() : null);

            HttpEntity entity = new HttpEntity(authenticationDetails, null);
            ResponseEntity<String> responseEntity = restTemplate.exchange(vimURL + "/api/v1/provider/topology", HttpMethod.POST, entity, String.class);

            if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {
                logger.info("Response: " + responseEntity.getBody());
                JSONObject callbackJSON = new JSONObject(responseEntity.getBody());
                if (callbackJSON.getString("code").equals("SUCCESS")) {
                    if (callbackJSON.has("returnobject")) {
                        JSONObject peers = callbackJSON.getJSONObject("returnobject");
                        return peers.toString();
                    }
                }
            }
        }
        throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
    }
}
