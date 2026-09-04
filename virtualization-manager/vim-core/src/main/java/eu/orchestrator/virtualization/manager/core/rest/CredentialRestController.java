package eu.orchestrator.virtualization.manager.core.rest;

import eu.orchestrator.spi.adapter.ProviderAdapter;
import eu.orchestrator.spi.model.CredentialsModel;
import eu.orchestrator.spi.response.SPIResponse;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorProviderAuthenticationDetails;
import eu.orchestrator.transfer.response.BasicResponseCode;
import eu.orchestrator.transfer.response.RestResponse;
import eu.orchestrator.virtualization.manager.core.util.TranslateModel;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Konstantinos Theodosiou
 */
@RestController
@RequestMapping("/api/v1/credential")
public class CredentialRestController {
    private static final Logger logger = Logger.getLogger(CredentialRestController.class.getName());

    @Resource(name = "providerAdapters")
    List providerAdapters;

    @RequestMapping(value = "/validate", method = RequestMethod.POST)
    public RestResponse validateCredential(@RequestBody OrchestratorProviderAuthenticationDetails orchestratorProviderAuthenticationDetails) {
        ProviderAdapter providerAdapter = (ProviderAdapter) ((List) providerAdapters.stream().filter(adapter -> adapter.getClass().getName().equals(orchestratorProviderAuthenticationDetails.getAdapterImplementation())).collect(Collectors.toList())).get(0);
        if(providerAdapter==null){
            return new RestResponse(BasicResponseCode.EXCEPTION, CredentialRestController.Message.PROVIDER_DOES_NOT_EXIST_ERROR);
        }

        CredentialsModel credentialsModel = TranslateModel.createCredentialModel(orchestratorProviderAuthenticationDetails);

        SPIResponse response = providerAdapter.validateCredentials(credentialsModel);
        return new RestResponse(response.getCode(),response.getMessage(), response.getReturnobject());
    }

    private final static class Message {

        final static String PROVIDER_DOES_NOT_EXIST_ERROR = "Provider adapter doesn't exist";
    }
}