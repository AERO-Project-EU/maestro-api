package eu.orchestrator.backend.service.oss.client;

import eu.orchestrator.backend.service.oss.OssConfiguration;
import eu.orchestrator.transfer.entities.oss.SliceIntent;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class OssRestClient implements OssClient {

    private static final Logger LOGGER = Logger.getLogger(OssRestClient.class.getName());
    private final OssConfiguration configuration;
    private final RestTemplate restTemplate;

    public OssRestClient(OssConfiguration configuration) {
        this.configuration = configuration;
        restTemplate = new RestTemplate();
    }

    @Override
    public boolean requestSlice(SliceIntent sliceIntent) {
        final HttpEntity<SliceIntent> sliceEntity = new HttpEntity<>(sliceIntent);

        LOGGER.log(Level.INFO, "Will submit slice intent for application [{0}] to [{1}]",
                new Object[] {sliceIntent.getApplicationInstanceID(), configuration.getSliceUrl()});

        final ResponseEntity<String> response = restTemplate.exchange(configuration.getSliceUrl(), HttpMethod.POST, sliceEntity, String.class);

        return response.getStatusCode() == HttpStatus.ACCEPTED;
    }
}
