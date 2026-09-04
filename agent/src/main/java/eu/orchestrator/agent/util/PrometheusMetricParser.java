package eu.orchestrator.agent.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class PrometheusMetricParser {

    public static double jsonParser(String response) {
        JsonParser parser = new JsonParser();
        JsonObject object = parser.parse(response).getAsJsonObject();
        JsonObject data = object.get("data").getAsJsonObject();
        JsonArray metricList = data.get("result").getAsJsonArray();
        if (metricList.size() > 0) {
            JsonObject temp = metricList.get(0).getAsJsonObject();
            JsonArray value = temp.get("value").getAsJsonArray();
            return value.get(1).getAsDouble();
        }

        return -1;
    }
}
