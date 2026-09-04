package eu.orchestrator.backend.util;

import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.RuntimePolicyExpression;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class PrometheusUtil {

    private static final RestTemplate restTemplate = new RestTemplate();
    private static final Logger logger = Logger.getLogger(PrometheusUtil.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String serializePrometheusExpression(
            List<RuntimePolicyExpression> runtimePolicyExpressions, ApplicationInstance applicationInstance) {

        String prometheusExpression = "";

        for (int i = 0; i < runtimePolicyExpressions.size(); i++) {

            prometheusExpression = prometheusExpression + "( ";

            if (null != runtimePolicyExpressions.get(i).getFunction() && !runtimePolicyExpressions.get(i).getFunction()
                    .isEmpty() && null != RuntimePolicyExpression.FunctionType
                    .valueOf(runtimePolicyExpressions.get(i).getFunction())) {

                // Handle Function
                if (runtimePolicyExpressions.get(i).getFunction()
                        .equals(RuntimePolicyExpression.FunctionType.NONE.name())) {

                    prometheusExpression += "netdata:";

                } else if (runtimePolicyExpressions.get(i).getFunction()
                        .equals(RuntimePolicyExpression.FunctionType.AVG.name())) {

                    prometheusExpression += "avg(netdata:";

                } else if (runtimePolicyExpressions.get(i).getFunction()
                        .equals(RuntimePolicyExpression.FunctionType.SUM.name())) {

                    prometheusExpression += "sum(netdata:";

                }

                // Handle application and application instance along with component node
                prometheusExpression +=
                        applicationInstance.getApplication().getHexID() + ":" + applicationInstance.getHexID()
                                + ":" + runtimePolicyExpressions.get(i).getComponentNodeHexID();

                // Handle metric
                if (runtimePolicyExpressions.get(i).getMetric().startsWith("_")) {
                    prometheusExpression += runtimePolicyExpressions.get(i).getMetric();
                } else {
                    prometheusExpression += "_" + runtimePolicyExpressions.get(i).getMetric();
                }

                // Handle dimensions
                if (null != runtimePolicyExpressions.get(i).getDimension() && !runtimePolicyExpressions.get(i).getDimension()
                        .isEmpty()) {

                    prometheusExpression += "{dimension=\"" + runtimePolicyExpressions.get(i).getDimension() + "\"}";

                } else if (runtimePolicyExpressions.get(i).getDimension().equals("ALL")) {

                    prometheusExpression += "{}";

                }

                if (!runtimePolicyExpressions.get(i).getFunction()
                        .equals(RuntimePolicyExpression.FunctionType.NONE.name())) {

                    prometheusExpression += ") ";
                }

                // Handle Operand
                prometheusExpression +=
                        " " + RuntimePolicyExpression.OperandType.valueOf(runtimePolicyExpressions.get(i).getOperand())
                                .getFriendlyName() + " ";

                // Handle Threshold
                prometheusExpression += runtimePolicyExpressions.get(i).getThreshold();

                prometheusExpression += ") ";

                if (i < runtimePolicyExpressions.size() - 1) {
                    prometheusExpression += " and ";

                }

            }
        }

        logger.info("Prometheus Expression: " + prometheusExpression);

        return prometheusExpression;
    }

    public static List<String> retrieveDimensions(String applicationHexID,
            String applicationInstanceHexID, String componentNodeHexID, String metric,
            String prometheusURL) {

        List<String> dimensions = new ArrayList<>();

        String query =
                "netdata:" + applicationHexID + ":" + applicationInstanceHexID + ":" + componentNodeHexID
                        + metric;
        logger.info("Query for retrieving dimension: " + query);

        Map<String, String> uriParams = new HashMap<>();
        uriParams.put("query", query);

        ResponseEntity<String> response = restTemplate
                .getForEntity(prometheusURL + "/api/v1/query?query={query}", String.class, uriParams);

        if (null != response && null != response.getStatusCode()
                && response.getStatusCode() == HttpStatus.OK) {

            if (null != response.getBody() && !response.getBody().isEmpty()) {

                JsonObject prometheusResponse = new JsonParser().parse(response.getBody())
                        .getAsJsonObject();

                if (null != prometheusResponse && null != prometheusResponse.get("status")
                        && null != prometheusResponse.get("status").getAsString()
                        && !prometheusResponse.get("status").getAsString().isEmpty() && prometheusResponse
                        .get("status").getAsString().equals("success")
                        && null != prometheusResponse.get("data") && null != prometheusResponse.get("data")
                        .getAsJsonObject()) {

                    JsonObject data = prometheusResponse.get("data").getAsJsonObject();

                    if (null != data && data.has("result")) {

                        JsonArray metrics = data.get("result").getAsJsonArray();

                        if (null != metrics && metrics.size() > 0) {

                            for (JsonElement tm : metrics) {

                                if (null != tm.getAsJsonObject() && null != tm.getAsJsonObject().get("metric")
                                        && null != tm.getAsJsonObject().get("metric").getAsJsonObject()) {

                                    JsonObject value = tm.getAsJsonObject().get("metric").getAsJsonObject();

                                    if (null != value.get("dimension") && null != value.get("dimension")
                                            .getAsString()) {

                                        String dimension = value.get("dimension").getAsString();

                                        if (!dimensions.contains(dimension)) {
                                            dimensions.add(dimension);
                                        }

                                    }

                                }

                            }
                        }


                    }

                }

            }

        }

        logger.info("Dimensions: " + new Gson().toJson(dimensions));

        return dimensions;
    }

    public static void main(String[] args) {

        String applicationHexID = "cCvAPPd1Y2";
        String applicationInstanceHexID = "tJdruYpvkZ";
        String componentNodeHexID = "7tYC2F8LuD";
        String metric = "_apps_cpu_cpu_time___average";
        String prometheusServerURL = System.getProperty("prometheus.url", "http://localhost:9090");

        retrieveDimensions(applicationHexID, applicationInstanceHexID, componentNodeHexID, metric,
                prometheusServerURL);


    }

}
