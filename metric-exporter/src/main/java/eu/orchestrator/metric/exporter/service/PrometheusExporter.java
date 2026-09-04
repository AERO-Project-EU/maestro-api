package eu.orchestrator.metric.exporter.service;

import eu.orchestrator.metric.exporter.configuration.PrometheusConfiguration;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class PrometheusExporter {

    private static final String URL_PREFIX = "http://";
    private static final String URL_PATH = "/api/v1/query?query={query}";
    private static final String QUERY_KEY_NAME = "query";
    private static final String DATA_KEY_NAME = "data";
    private static final String RESULT_KEY_NAME = "result";

    private final PrometheusConfiguration prometheusConfiguration;

    @Autowired
    public PrometheusExporter(PrometheusConfiguration prometheusConfiguration) {
        this.prometheusConfiguration = prometheusConfiguration;
    }

    public long retrieveTimeStampFromPrometheus(String query) {
        Map<String, String> uriParams = new HashMap<>();
        uriParams.put(QUERY_KEY_NAME, query);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate
                .getForEntity(URL_PREFIX + prometheusConfiguration.getUrl() + ":" + prometheusConfiguration.getPort() + URL_PATH, String.class, uriParams);

        JsonParser parser = new JsonParser();
        JsonObject object = parser.parse(response.getBody()).getAsJsonObject();
        JsonObject data = object.get(DATA_KEY_NAME).getAsJsonObject();

        if (data.has(RESULT_KEY_NAME)) {
            JsonArray metricList = data.get(RESULT_KEY_NAME).getAsJsonArray();
            if (metricList.size() > 0) {
                JsonObject temp = metricList.get(0).getAsJsonObject();
                JsonArray value = temp.get("value").getAsJsonArray();
                return value.get(0).getAsLong();
            }
        }
        return -1;
    }

    public double retrieveValueFromPrometheus(String query) {
        Map<String, String> uriParams = new HashMap<>();
        uriParams.put(QUERY_KEY_NAME, query);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate
                .getForEntity(URL_PREFIX + prometheusConfiguration.getUrl() + ":" + prometheusConfiguration.getPort() + URL_PATH, String.class, uriParams);

        JsonParser parser = new JsonParser();
        JsonObject object = parser.parse(response.getBody()).getAsJsonObject();
        JsonObject data = object.get(DATA_KEY_NAME).getAsJsonObject();

        if (data.has(RESULT_KEY_NAME)) {
            JsonArray metricList = data.get(RESULT_KEY_NAME).getAsJsonArray();
            if (metricList.size() > 0) {
                JsonObject temp = metricList.get(0).getAsJsonObject();
                JsonArray value = temp.get("value").getAsJsonArray();
                return value.get(1).getAsDouble();
            }
        }

        return -1;
    }

    public double retrieveRequestPerSec(String query) {
        Map<String, String> uriParams = new HashMap<>();
        uriParams.put(QUERY_KEY_NAME, query);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate
                .getForEntity(URL_PREFIX + prometheusConfiguration.getUrl() + ":" + prometheusConfiguration.getPort() + URL_PATH, String.class, uriParams);

        JsonParser parser = new JsonParser();
        JsonObject object = parser.parse(response.getBody()).getAsJsonObject();
        JsonObject data = object.get(DATA_KEY_NAME).getAsJsonObject();
        JsonArray metricList = data.get(RESULT_KEY_NAME).getAsJsonArray();

        if (metricList.size() > 0) {
            JsonObject temp = metricList.get(0).getAsJsonObject();
            JsonArray value = temp.get("values").getAsJsonArray();
            JsonArray array = value.get(0).getAsJsonArray();

            if (array.size() > 0) {
                double previousValue = array.get(1).getAsDouble();
                array = value.get(1).getAsJsonArray();
                double currentValue = array.get(1).getAsDouble();
                return (currentValue - previousValue) / 10;
            }
        }

        return 0;
    }
}
