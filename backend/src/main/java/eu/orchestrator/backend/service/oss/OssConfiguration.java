package eu.orchestrator.backend.service.oss;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public class OssConfiguration {

    private static final String SLICE_CALLBACK_ENDPOINT = "api/v1/callback/slice";

    @Value("${oss.server.url}")
    String serverUrl;

    @Value("${oss.server.slice-endpoint}")
    String sliceEndpoint;

    @Value("${oss.tac.url}")
    String trackingAreaCodeEndpoint;

    @Value("${ui.server.url}")
    String callbackHost;

    public String getSliceUrl() {
        final URI normalizedUrl = URI.create(serverUrl + "/" + sliceEndpoint).normalize();
        return normalizedUrl.toString();
    }

    public String getCallBackUrl(long applicationInstanceId) {
        final URI normalizedCallbackUrl = URI.create(callbackHost + "/" + SLICE_CALLBACK_ENDPOINT + "/" + applicationInstanceId).normalize();
        return normalizedCallbackUrl.toString();
    }
}
