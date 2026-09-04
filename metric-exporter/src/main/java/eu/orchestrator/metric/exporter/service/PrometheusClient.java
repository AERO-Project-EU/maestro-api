package eu.orchestrator.metric.exporter.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class PrometheusClient {

    private static final String URL_PATH = "/api/v1/query?query={query}";
    private static final String QUERY_KEY_NAME = "query";
    private static final String DATA_KEY_NAME = "data";
    private static final String RESULT_KEY_NAME = "result";

    public JsonElement retrieveValueFromPrometheus(String url, String query) {

        JsonObject temp = getData(url, query);

        if (temp == null) {
            return null;
        }

        JsonArray value = temp.get("value").getAsJsonArray();
        return value.get(1);

    }

    public double retrieveDoubleFromPrometheus(String url, String query) {

        JsonElement value = retrieveValueFromPrometheus(url, query);
        if (value == null) {
            return -1;
        }

        return value.getAsDouble();
    }

    public long retrieveTimestampFromPrometheus(String url, String query) {

        JsonObject temp = getData(url, query);

        if (temp == null) {
            return -1;
        }

        JsonArray value = temp.get("value").getAsJsonArray();
        if (value == null) {
            return -1;
        }

        return value.get(0).getAsLong();
    }

    public double retrieveRequestPerSec(String url, String query) {

        JsonObject temp = getData(url, query);

        if (temp == null) {
            return -1;
        }

        JsonArray value = temp.get("values").getAsJsonArray();
        JsonArray array = value.get(0).getAsJsonArray();

        if (array.size() <= 0) {
            return 0;
        }

        double previousValue = array.get(1).getAsDouble();
        array = value.get(1).getAsJsonArray();
        double currentValue = array.get(1).getAsDouble();
        return (currentValue - previousValue) / 10;

    }

    public String retrieveInstanceName(String url, String query) {
        JsonObject result = getData(url, query);

        if (result == null) {
            return "";
        }

        JsonObject metric = result.get("metric").getAsJsonObject();

        if (metric == null) {
            return "";
        }

        return metric.get("instance").getAsString();

    }

    private JsonObject getData(String url, String query) {
        Map<String, String> uriParams = new HashMap<>();
        uriParams.put(QUERY_KEY_NAME, query);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate
                .getForEntity(url + URL_PATH, String.class, uriParams);

        JsonObject object = new Gson().fromJson(response.getBody(), JsonObject.class);
        JsonObject data = object.get(DATA_KEY_NAME).getAsJsonObject();

        if (!data.has(RESULT_KEY_NAME)) {
            return null;
        }

        JsonArray metricList = data.get(RESULT_KEY_NAME).getAsJsonArray();

        if (metricList.size() <= 0) {
            return null;
        }

        return metricList.get(0).getAsJsonObject();
    }

}
