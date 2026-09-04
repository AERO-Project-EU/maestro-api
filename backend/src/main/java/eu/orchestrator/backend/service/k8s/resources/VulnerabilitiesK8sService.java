package eu.orchestrator.backend.service.k8s.resources;

import eu.orchestrator.backend.service.model.VulnerabilitiesResponse;
import eu.orchestrator.backend.service.model.Vulnerability;
import eu.orchestrator.common.util.NullCheckUtil;

import com.google.gson.Gson;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.utils.Serialization;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@org.springframework.stereotype.Service
public class VulnerabilitiesK8sService {

    private static final Logger logger = LoggerFactory.getLogger(VulnerabilitiesK8sService.class);

    public VulnerabilitiesResponse getVulnerabilities(String namespace, String componentNodeInstanceId, Config config) {
        try (final KubernetesClient client = new DefaultKubernetesClient(config)) {

            CustomResourceDefinitionContext crdContext = new CustomResourceDefinitionContext.Builder()
                    .withName("vulnerabilityreports.aquasecurity.github.io")
                    .withGroup("aquasecurity.github.io")
                    .withVersion("v1alpha1")
                    .withPlural("vulnerabilityreports")
                    .withScope("Namespaced")
                    .build();

            String crdJson = Serialization.asJson(
                    client.genericKubernetesResources(crdContext).inNamespace(namespace).list());
            JSONObject crdJSONObject = new JSONObject(crdJson);

            String vulnerabilitiesReportName = null;
            if (NullCheckUtil.isNotEmpty(crdJSONObject) && NullCheckUtil.isNotEmpty(crdJSONObject.get("items"))) {
                JSONArray items = (JSONArray) crdJSONObject.get("items");
                for (int i = 0; i < items.length(); i++) {
                    JSONObject md1 = items.getJSONObject(i);
                    if (NullCheckUtil.isNotEmpty(md1.get("metadata"))) {
                        JSONObject md2 = (JSONObject) md1.get("metadata");
                        if (NullCheckUtil.isNotEmpty(md2.get("name"))) {
                            String tempVulnerabilitiesReportName = (String) md2.get("name");
                            if (tempVulnerabilitiesReportName.contains(componentNodeInstanceId)) {
                                vulnerabilitiesReportName = tempVulnerabilitiesReportName;
                                break;
                            }
                        }
                    }
                }
            }

            VulnerabilitiesResponse vulnerabilitiesResponse = new VulnerabilitiesResponse();

            if (vulnerabilitiesReportName != null) {
                String vulnCrdJson = Serialization.asJson(client.genericKubernetesResources(crdContext)
                        .inNamespace(namespace).withName(vulnerabilitiesReportName).get());
                JSONObject vulnCrdJSONObject = new JSONObject(vulnCrdJson);
                if (NullCheckUtil.isNotEmpty(vulnCrdJSONObject) && NullCheckUtil.isNotEmpty(vulnCrdJSONObject.get("report"))) {
                    JSONObject vulnReport = (JSONObject) vulnCrdJSONObject.get("report");
                    if (NullCheckUtil.isNotEmpty(vulnReport.get("vulnerabilities"))) {
                        JSONArray vulnerabilitiesJSONArray = (JSONArray) vulnReport.get("vulnerabilities");
                        vulnerabilitiesResponse = new VulnerabilitiesResponse();
                        for (int i = 0; i < vulnerabilitiesJSONArray.length(); i++) {
                            Vulnerability vulnerability = new Gson().fromJson(vulnerabilitiesJSONArray.get(i).toString(), Vulnerability.class);
                            vulnerabilitiesResponse.getVulnerabilities().add(vulnerability);
                        }
                    }
                }
            }

            return vulnerabilitiesResponse;

        } catch (Exception e) {
            logger.error("Vulnerabiliries ERROR: ", e);
            return null;
        }
    }
}
