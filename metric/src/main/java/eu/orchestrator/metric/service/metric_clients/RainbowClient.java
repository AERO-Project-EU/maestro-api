package eu.orchestrator.metric.service.metric_clients;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import eu.orchestrator.metric.enums.MetricEnum;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

public class RainbowClient implements MetricClient {

    private final String bearerToken;
    private final String metricsPort;

    public RainbowClient(String bearerToken, String metricsPort) {
        this.bearerToken = bearerToken;
        this.metricsPort = metricsPort;
    }

    @Override
    public void apply(String metricJson, String hexId, String endpointWithPort) {

        final String uri = buildUri(hexId, endpointWithPort);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + bearerToken);

        HttpEntity<String> entity = new HttpEntity<>(metricJson, headers);
        RestTemplate restTemplate = new RestTemplate();

        try {
            restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);
        } catch (HttpClientErrorException httpClientErrorException) {
            throw new HttpClientErrorException(
                    httpClientErrorException.getStatusCode(),
                    getErrorMessage(httpClientErrorException.getResponseBodyAsString())
            );
        }
    }

    @Override
    public void delete(String hexId, String endpointWithPort) {

        final String uri = buildUri(hexId, endpointWithPort);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + bearerToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();

        try {
            restTemplate.exchange(uri, HttpMethod.DELETE, entity, String.class);
        } catch (HttpClientErrorException httpClientErrorException) {
            throw new HttpClientErrorException(
                    httpClientErrorException.getStatusCode(),
                    getErrorMessage(httpClientErrorException.getResponseBodyAsString())
            );
        }
    }

    private String buildUri(String hexId, String endpointWithPort) {
        String endpoint = endpointWithPort.substring(endpointWithPort.lastIndexOf("/") + 1);
        endpoint = endpoint.substring(0, endpoint.lastIndexOf("]") + 1);

        return MetricEnum.HTTP.getValue() + endpoint + ":" + metricsPort
                + MetricEnum.METRICS_API_URI.getValue() + hexId;
    }

    private String getErrorMessage(String jsonResponse) {
        Gson gson = new Gson();
        JsonObject object = gson.fromJson(jsonResponse, JsonObject.class);
        try {
            return object.get("message").getAsString();
        } catch (Exception exception) {
            return "Unknown error occurred";
        }
    }

}
