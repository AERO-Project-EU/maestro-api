package eu.orchestrator.backend.grafana;

import org.json.JSONObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link GrafanaClient} implementation backed by Spring's {@link RestTemplate} and the dashboard
 * templates bundled under {@code classpath:/grafana/}. Behaviour mirrors the former
 * {@code eu.ubitech.grafanaclient.GrafanaConnector}:
 * <ul>
 *   <li>{@code POST {grafanaDomain}/api/dashboards/db} to create a dashboard (returns its uid);</li>
 *   <li>{@code DELETE {grafanaDomain}/api/dashboards/uid/{uid}} to delete one;</li>
 *   <li>{@code Authorization: Bearer <token>} on every request.</li>
 * </ul>
 */
public class RestTemplateGrafanaClient implements GrafanaClient {

    private static final Logger logger = Logger.getLogger(RestTemplateGrafanaClient.class.getName());

    private final String grafanaDomain;
    private final String authorizationHeader;
    private final RestTemplate restTemplate;

    // Dashboard + panel templates (loaded once from the classpath).
    private final String dashboardTemplate = load("prometheus-dashboard.json");
    private final String cpuTemplate = load("cpu.json");
    private final String memoryTemplate = load("memory.json");
    private final String networkInTemplate = load("network-in.json");
    private final String networkOutTemplate = load("network-out.json");
    private final String diskReadTemplate = load("disk-read.json");
    private final String diskWriteTemplate = load("disk-write.json");

    public RestTemplateGrafanaClient(String grafanaDomain, String apiToken) {
        this.grafanaDomain = grafanaDomain;
        this.authorizationHeader = "Bearer " + apiToken;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String createPrometheusDashboardForGraphInstance(String graphHexId, String graphInstanceHexId,
            List<String> componentNodeHexIds) {

        String cpuTargets = "";
        String memoryTargets = "";
        String networkInTargets = "";
        String networkOutTargets = "";
        String diskReadTargets = "";
        String diskWriteTargets = "";

        char refId = 'A';
        for (int i = 0; i < componentNodeHexIds.size(); i++) {
            String component = componentNodeHexIds.get(i);

            cpuTargets = buildText(cpuTargets + cpuTemplate, graphHexId, graphInstanceHexId, component, refId);
            memoryTargets = buildText(memoryTargets + memoryTemplate, graphHexId, graphInstanceHexId, component, refId);
            networkInTargets = buildText(networkInTargets + networkInTemplate, graphHexId, graphInstanceHexId, component, refId);
            networkOutTargets = buildText(networkOutTargets + networkOutTemplate, graphHexId, graphInstanceHexId, component, refId);
            diskReadTargets = buildText(diskReadTargets + diskReadTemplate, graphHexId, graphInstanceHexId, component, refId);
            diskWriteTargets = buildText(diskWriteTargets + diskWriteTemplate, graphHexId, graphInstanceHexId, component, refId);

            refId++;

            if (i < componentNodeHexIds.size() - 1) {
                cpuTargets += ",";
                memoryTargets += ",";
                networkInTargets += ",";
                networkOutTargets += ",";
                diskReadTargets += ",";
                diskWriteTargets += ",";
            }
        }

        String dashboardJson = dashboardTemplate
                .replace("@CPU_TEMPLATE", cpuTargets)
                .replace("@MEMORY_TEMPLATE", memoryTargets)
                .replace("@NETWORK_IN_TEMPLATE", networkInTargets)
                .replace("@NETWORK_OUT_TEMPLATE", networkOutTargets)
                .replace("@DISK_READS_TEMPLATE", diskReadTargets)
                .replace("@DISK_WRITES_TEMPLATE", diskWriteTargets)
                .replace("@GRAPHID", graphHexId)
                .replace("@GRAPHINSTANCEID", graphInstanceHexId);

        return createDashboard(dashboardJson);
    }

    @Override
    public void deleteDashboard(String dashboardUid) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader);
            restTemplate.exchange(grafanaDomain + "/api/dashboards/uid/" + dashboardUid,
                    HttpMethod.DELETE, new HttpEntity<>(headers), String.class);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to delete Grafana dashboard " + dashboardUid + ": " + e.getMessage());
        }
    }

    private String createDashboard(String dashboardJson) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader);
            headers.setContentType(MediaType.APPLICATION_JSON);

            ResponseEntity<String> response = restTemplate.exchange(grafanaDomain + "/api/dashboards/db",
                    HttpMethod.POST, new HttpEntity<>(dashboardJson, headers), String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return new JSONObject(response.getBody()).optString("uid", null);
            }
            logger.warning("Grafana dashboard creation returned status " + response.getStatusCode());
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to create Grafana dashboard: " + e.getMessage());
        }
        return null;
    }

    // Mirrors the former eu.ubitech.grafanaclient.util.TextProcess#buildText.
    private static String buildText(String input, String graphHexId, String graphInstanceHexId,
            String componentHexId, char refId) {
        return input
                .replace("@GRAPHID", graphHexId)
                .replace("@GRAPHINSTANCEID", graphInstanceHexId)
                .replace("@COMPONENT", componentHexId)
                .replace("@ALFABETIC", String.valueOf(refId));
    }

    private static String load(String fileName) {
        try (InputStream in = new ClassPathResource("grafana/" + fileName).getInputStream()) {
            return StreamUtils.copyToString(in, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to load Grafana template grafana/" + fileName, e);
        }
    }
}
