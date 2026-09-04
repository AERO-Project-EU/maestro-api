package eu.orchestrator.backend.util;

import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.backend.grafana.GrafanaClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class GrafanaUtil {

    @Autowired
    private GrafanaClient grafanaClient;

    public String createDashboard(ApplicationInstance applicationInstance) {

        String applicationID = applicationInstance.getApplication().getHexID();
        String applicationInstanceID = applicationInstance.getHexID();

        Collection<String> componentsID = new ArrayList();

        for (ComponentNodeInstance componentNodeInstance : applicationInstance
                .getComponentNodeInstances()) {
            componentsID.add(componentNodeInstance.getComponentNode().getHexID());

        }

        List<String> componentsHexID = ((ArrayList<String>) componentsID)
                .subList(0, componentsID.size());

        String dashboardUid = grafanaClient
                .createPrometheusDashboardForGraphInstance(applicationID, applicationInstanceID,
                        componentsHexID);

        return dashboardUid != null ? dashboardUid : "";
    }

    public void deleteDashboard(String applicationUUID) {
        grafanaClient.deleteDashboard(applicationUUID);
    }
}
