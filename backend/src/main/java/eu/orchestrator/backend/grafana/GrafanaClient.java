package eu.orchestrator.backend.grafana;

import java.util.List;

/**
 * Minimal in-house Grafana HTTP API client. Replaces the former private
 * {@code eu.ubitech.devopsstack:grafanaclient} artifact (only the two methods actually used by
 * the application are kept) and its abandoned {@code com.mashape.unirest} 1.x transitive dependency.
 */
public interface GrafanaClient {

    /**
     * Builds a Prometheus dashboard for the given graph instance from the bundled dashboard/panel
     * templates and creates it in Grafana.
     *
     * @return the created dashboard uid, or {@code null} if the call did not succeed.
     */
    String createPrometheusDashboardForGraphInstance(String graphHexId, String graphInstanceHexId,
            List<String> componentNodeHexIds);

    /** Deletes the Grafana dashboard identified by its uid. */
    void deleteDashboard(String dashboardUid);
}
