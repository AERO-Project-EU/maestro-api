package eu.orchestrator.backend.controller;

import eu.orchestrator.backend.service.oss.OssKubernetesService;
import eu.orchestrator.backend.service.oss.OssService;
import eu.orchestrator.backend.transfer.DashboardTO;
import eu.orchestrator.backend.util.Util;
import eu.orchestrator.common.util.NullCheckUtil;
import eu.orchestrator.repository.dao.ApplicationInstanceDAO;
import eu.orchestrator.repository.dao.ComponentNodeInstanceDAO;
import eu.orchestrator.repository.dao.ComponentNodeInstanceIPDAO;
import eu.orchestrator.repository.dao.ProviderDAO;
import eu.orchestrator.repository.dao.ProviderQuotaDAO;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.repository.domain.ComponentNodeInstanceIP;
import eu.orchestrator.repository.domain.Provider;
import eu.orchestrator.repository.domain.ProviderQuota;
import eu.orchestrator.repository.domain.ProviderType;
import eu.orchestrator.transfer.entities.metricExporter.MetricModel;
import eu.orchestrator.transfer.entities.metricExporter.ProxyMetricModel;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorProviderAuthenticationDetails;
import eu.orchestrator.backend.service.k8s.KubernetesService;
import eu.orchestrator.backend.service.model.RainbowMetricsRequest;
import eu.orchestrator.backend.service.model.RainbowMetricsResponse;
import eu.orchestrator.backend.service.model.RainbowMetricsValues;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
public class WebController {

    final static RestTemplate restTemplate = new RestTemplate();
    static final String GRAPHS_TOPIC = "/graphs";
    private static final Logger logger = Logger.getLogger(WebController.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String DASHBOARD_TOPIC = "/dashboard";

    @Value("${vim.server.url}")
    String vimURL;

    @Value("${exporter.server.url}")
    String graphExporterURL;

    @Autowired
    SimpMessagingTemplate wsTemplate;

    @Autowired
    ProviderDAO providerDAO;

    @Autowired
    ApplicationInstanceDAO applicationInstanceDAO;

    @Autowired
    ProviderQuotaDAO providerQuotaDAO;

    @Autowired
    ComponentNodeInstanceDAO componentNodeInstanceDAO;

    @Autowired
    ComponentNodeInstanceIPDAO componentNodeInstanceIPDAO;

    @Autowired
    KubernetesService kubernetesService;

    @Autowired
    OssKubernetesService ossKubernetesService;

//    @Scheduled(fixedDelay = 120000)
//    public void checkForResourceQuotas() {
//
//        // Fetch all active providers
//        List<Provider> providers = providerDAO.findAllByEnabled(Boolean.TRUE);
//
//        if (null != providers && !providers.isEmpty()) {
//
////            logger.info("Need to check for quotas of: " + providers.size() + " providers");
//
//            AtomicReference<Boolean> newQuota = new AtomicReference<>(false);
//            providers.stream().filter(provider -> (provider.getProviderType().getName().equals(ProviderType.ProviderName.OPENSTACK.name())
//                    || provider.getProviderType().getName().equals(ProviderType.ProviderName.AWS.name())
//                    || provider.getProviderType().getName().equals(ProviderType.ProviderName.GCC.name()))
//                    && !provider.getInternalProvider().booleanValue()).forEach(provider -> {
//
//                try {
//
////                logger.info("Fetching: " + provider.getName() + " quotas");
//
//                    // Fetch Quota for this provider
//                    OrchestratorProviderAuthenticationDetails authenticationDetails = new OrchestratorProviderAuthenticationDetails();
//                    authenticationDetails.setId(provider.getProviderID() + "");
//                    authenticationDetails.setAdapterType(provider.getProviderType().getName());
//                    authenticationDetails.setAdapterImplementation(
//                            null != provider.getProviderType().getAdapterImplementation() ? provider
//                                    .getProviderType().getAdapterImplementation() : null);
//                    authenticationDetails
//                            .setDomain(null != provider.getDomain() ? provider.getDomain() : null);
//                    authenticationDetails.setMeshIdentifier(
//                            null != provider.getMeshIdentifier() ? provider.getMeshIdentifier() : null);
//                    authenticationDetails
//                            .setProject(null != provider.getProject() ? provider.getProject() : null);
//                    authenticationDetails
//                            .setEndpoint(null != provider.getEndpoint() ? provider.getEndpoint() : null);
//                    authenticationDetails
//                            .setUsername(null != provider.getUsername() ? provider.getUsername() : null);
//                    authenticationDetails
//                            .setPassword(null != provider.getPassword() ? provider.getPassword() : null);
//                    authenticationDetails
//                            .setPrivateKey(null != provider.getPrivateKey() ? provider.getPrivateKey() : null);
//                    authenticationDetails
//                            .setPublicKey(null != provider.getPublicKey() ? provider.getPublicKey() : null);
//                    authenticationDetails
//                            .setImageID(null != provider.getImageID() ? provider.getImageID() : null);
//                    authenticationDetails
//                            .setNetworkID(null != provider.getNetworkID() ? provider.getNetworkID() : null);
//                    authenticationDetails
//                            .setPublicNetwork(null != provider.getPublicNetwork() ? provider.getPublicNetwork() : null);
//
//                    HttpEntity entity = new HttpEntity(authenticationDetails, null);
//
//                    ResponseEntity<String> responseEntity = restTemplate
//                            .exchange(vimURL + "/api/v1/provider/resources", HttpMethod.POST, entity,
//                                    String.class);
//
//                    if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {
//
////                    logger.info("Response: "+ responseEntity.getBody());
//
//                        JSONObject responseBody = new JSONObject(responseEntity.getBody());
//
//                        if (null != responseBody && null != responseBody.get("code") && responseBody
//                                .getString("code").equals("SUCCESS")) {
//
////                        logger.info("Quotas for provider: " + provider.getName() + " have been fetched successfully!");
//
//                            JSONObject quotas = responseBody.getJSONObject("returnobject");
//                            if (null != quotas) {
//
//                                ProviderQuota quota;
//
//                                if (providerQuotaDAO.findByProvider(provider).isPresent()) {
//
//                                    //Existing
//                                    quota = providerQuotaDAO.findByProvider(provider).get();
//
//                                } else {
//
//                                    // New Quota
//                                    quota = new ProviderQuota();
//                                    quota.setDateCreated(new Date());
//                                    newQuota.set(true);
//
//                                }
//
//                                if (!newQuota.get()) {
//                                    if ((quota.getUsedVirtualCPUs() != quotas.getInt("usedVCpus"))
//                                            || (quota.getMaxVirtualCPUs() != quotas.getInt("maxVCpus"))
//                                            || (quota.getUsedMemory() != quotas.getInt("usedRam"))
//                                            || (quota.getMaxMemory() != quotas.getInt("maxRam"))
//                                            || (quota.getRunningInstances() != quotas.getInt("runningInstances"))
//                                            || (quota.getMaxInstances() != quotas.getInt("maxInstances"))) {
//
//                                        newQuota.set(true);
//                                    }
//                                }
//
//                                quota.setProvider(provider);
//
//                                // vCPUs
//                                quota.setUsedVirtualCPUs(quotas.getInt("usedVCpus"));
//                                quota.setMaxVirtualCPUs(quotas.getInt("maxVCpus"));
//                                quota.setVirtualCPUsUtilization(quotas.getDouble("vCpuUtilization"));
//
//                                // MEMORY
//                                quota.setUsedMemory(quotas.getInt("usedRam"));
//                                quota.setMaxMemory(quotas.getInt("maxRam"));
//                                quota.setMemoryUtilization(quotas.getDouble("ramUtilization"));
//
//                                // Instances
//                                quota.setRunningInstances(quotas.getInt("runningInstances"));
//                                quota.setMaxInstances(quotas.getInt("maxInstances"));
//                                quota.setInstancesUtilization(quotas.getDouble("instancesUtilization"));
//
//                                // IPs
//                                if (null != quotas.get("usedFloatingIPs") && !quotas.isNull("usedFloatingIPs")) {
//                                    quota.setUsedFloatingIPs(quotas.getInt("usedFloatingIPs"));
//                                } else {
//                                    quota.setUsedFloatingIPs(0);
//                                }
//                                if (null != quotas.get("claimedFloatingIPs") && !quotas
//                                        .isNull("claimedFloatingIPs")) {
//                                    quota.setClaimedFloatingIPs(quotas.getInt("claimedFloatingIPs"));
//                                } else {
//                                    quota.setClaimedFloatingIPs(0);
//                                }
//                                if (null != quotas.get("floatingIPsUtilization") && !quotas
//                                        .isNull("floatingIPsUtilization")) {
//                                    quota.setFloatingIPsConsumption(quotas.getDouble("floatingIPsUtilization"));
//                                } else {
//                                    quota.setFloatingIPsConsumption(0D);
//                                }
//
//                                quota.setLastModified(new Date());
//                                providerQuotaDAO.save(quota);
//
//                            }
//
//                        } else {
//                            logger.info("Quotas for provider: " + provider.getName() + " have not been fetched successfully!");
//                        }
//
//                    } else {
//                        logger.info("Quotas for provider: " + provider.getName() + " have not been fetched successfully!");
//                    }
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            });
//
//            if (newQuota.get()) {
//                try {
//                    DashboardTO dashboardTO = new DashboardTO();
//                    dashboardTO.setOverview(true);
//                    dashboardTO.setResources(true);
//                    String notificationAsString = null;
//                    notificationAsString = objectMapper.writeValueAsString(dashboardTO);
//                    wsTemplate.convertAndSend(DASHBOARD_TOPIC, notificationAsString);
//                } catch (JsonProcessingException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//
//    }

//    @Scheduled(fixedDelay = 10000)
//    public void checkForCNIResources() {
//
//        // Fetch all deployed application instances
//        List<ApplicationInstance> applicationInstances = applicationInstanceDAO
//                .findAllByStatus(ApplicationInstance.ApplicationInstanceStatus.DEPLOYED.name());
//
//        if (null != applicationInstances && !applicationInstances.isEmpty()) {
//
////            logger.info("Need to check for graphs of " + applicationInstances.size() + " application instances");
//
//            applicationInstances.stream().forEach(applicationInstance -> {
//
//                //TODO: REVIEW rainbow k8s
//                if (Util.isNotEmpty(applicationInstance.getProvider())
//                        && Util.isNotEmpty(applicationInstance.getProvider().getProviderType())
//                        && ProviderType.ProviderName.RAINBOW_KUBERNETES.name()
//                        .equalsIgnoreCase(applicationInstance.getProvider().getProviderType().getName())) {
//
//                    List<ComponentNodeInstance> componentNodeInstanceList
//                            = componentNodeInstanceDAO.findAllByApplicationInstance(applicationInstance);
//
//                    if (Util.isNotEmpty(componentNodeInstanceList)) {
//                        for (ComponentNodeInstance cni : componentNodeInstanceList) {
//                            List<ComponentNodeInstanceIP> componentNodeInstanceIPList =
//                                    componentNodeInstanceIPDAO.findAllByComponentNodeInstanceOrderByDateCreatedDesc(cni);
//                            if (Util.isNotEmpty(componentNodeInstanceIPList)) {
//                                for (ComponentNodeInstanceIP ip : componentNodeInstanceIPList) {
//
//                  /*
//                  curl --location --request POST 'http://[fc19:fe84:6855:515b:2f58:17e6:bba8:6a5]:50000/get' --header 'Content-Type: text/plain' --data-raw '{ "latest": true}'
//                  */
//                                    String rainbowUrl = "http://[" + ip.getIp() + "]:50000/get";
//
//                                    RainbowMetricsRequest rainbowMetricsRequest = new RainbowMetricsRequest();
//                                    rainbowMetricsRequest.setLatest(Boolean.TRUE);
//
//                                    HttpEntity entity = new HttpEntity(rainbowMetricsRequest);
//
//                                    ResponseEntity<String> responseEntity = restTemplate
//                                            .exchange(rainbowUrl, HttpMethod.POST, entity, String.class);
//
//                                    if (responseEntity.getStatusCode() == HttpStatus.OK) {
//                                        try {
//                                            RainbowMetricsResponse rainbowMetricsResponse = objectMapper.
//                                                    readValue(responseEntity.getBody(), new TypeReference<RainbowMetricsResponse>() {
//                                                    });
//
//                                            if (Util.isNotEmpty(rainbowMetricsResponse)
//                                                    && Util.isNotEmpty(rainbowMetricsResponse.getMonitoring())) {
//
//                                                JSONObject metricsObj = new JSONObject();
//                                                metricsObj
//                                                        .put("applicationInstanceID", applicationInstance.getApplicationInstanceID());
//
//                                                JSONObject metricModelObj = new JSONObject();
//
//                                                metricModelObj.put("graphId", applicationInstance.getApplication().getId() + "");
//                                                metricModelObj
//                                                        .put("graphInstanceId", applicationInstance.getApplicationInstanceID() + "");
//                                                metricModelObj.put("graphName", applicationInstance.getApplication().getName());
//                                                metricModelObj.put("graphInstanceName", applicationInstance.getName());
//
//                                                metricModelObj.put("componentNodeInstanceId",
//                                                        cni.getComponentNodeInstanceID() + "");
//                                                metricModelObj.put("componentNodeId",
//                                                        cni.getComponentNode().getComponentNodeID() + "");
//                                                metricModelObj.put("componentNodeInstanceName", cni.getName());
//                                                metricModelObj
//                                                        .put("componentNodeName", cni.getComponentNode().getName());
//
//                                                double systemRamTotal = 0.0;
//                                                double systemRamUsed = 0.0;
//                                                double systemCpuUsed = 0.0;
//                                                for (RainbowMetricsValues rm : rainbowMetricsResponse.getMonitoring()) {
//                                                    if (rm.getMetricID().equalsIgnoreCase("_SYSTEM_RAM_VISIBLETOTAL")) {
//                                                        systemRamTotal = rm.getVal();
//                                                        metricModelObj.put("timestamp", rm.getTimestamp());
//                                                    }
//
//                                                    if (rm.getMetricID().equalsIgnoreCase("_SYSTEM_RAM_USED")) {
//                                                        systemRamUsed = rm.getVal();
//                                                    }
//
//                                                    if (rm.getMetricID().equalsIgnoreCase("_SYSTEM_CPU_VISIBLETOTAL")) {
//                                                        systemCpuUsed = rm.getVal();
//                                                    }
//
//
//                                                }
//
//                                                metricModelObj.put("percentageUsedCPU", systemCpuUsed);
//                                                metricModelObj.put("diskAvailable", 15.0);
//                                                metricModelObj.put("percentageUsedRAM", (systemRamUsed / systemRamTotal) * 100);
//
//                                                metricModelObj.put("networkTrafficPacketsSend", 0.0);
//                                                metricModelObj.put("networkTrafficPacketsReceived", 0.0);
//                                                metricModelObj.put("diskThroughputRead", 0.0);
//                                                metricModelObj.put("diskThroughputWrite", 0.0);
//
//                                                metricsObj
//                                                        .put(cni.getComponentNodeInstanceID() + "", metricModelObj);
//
//                                                String wsMessage = metricsObj.toString();
//
//                                                wsTemplate.convertAndSend(GRAPHS_TOPIC, wsMessage);
//
//                                            } else {
//                                                logger.info("Graphs for application instance: " + applicationInstance.getName()
//                                                        + " have not been fetched successfully!");
//                                            }
//
//                                        } catch (Exception e) {
//                                            logger.log(Level.SEVERE, e.getMessage(), e);
//                                            e.printStackTrace();
//                                        }
//                                    } else {
//                                        logger.info("Graphs for application instance: " + applicationInstance.getName()
//                                                + " have not been fetched successfully!");
//                                    }
//
//                                }
//                            }
//                        }
//                    }
//
//                } else if (Util.isNotEmpty(applicationInstance.getProvider()) && Util.isNotEmpty(applicationInstance.getProvider().getProviderType())
//                        && ProviderType.ProviderName.KUBERNETES.name().equalsIgnoreCase(applicationInstance.getProvider().getProviderType().getName())) {
//
//                    //TODO fix, now do nothing to avoid error
//
//                } else {
//
//                    String url =
//                            graphExporterURL + "/api/v1/metrics/retrieve/" + applicationInstance.getApplication()
//                                    .getHexID() + "/" + applicationInstance.getHexID();
//
////                logger.info("Fetching: " + applicationInstance.getName() + " graphs from: " + url);
//
//                    ResponseEntity<String> responseEntity = restTemplate
//                            .exchange(url, HttpMethod.GET, null, String.class);
//
//                    if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {
//
////                    logger.info("Response: " + responseEntity.getBody());
//
//                        try {
//
//                            List<MetricModel> metrics = objectMapper
//                                    .readValue(responseEntity.getBody(), new TypeReference<List<MetricModel>>() {
//                                    });
//
//                            if (null != metrics && !metrics.isEmpty()) {
//
//                                JSONObject metricsObj = new JSONObject();
//                                metricsObj
//                                        .put("applicationInstanceID", applicationInstance.getApplicationInstanceID());
//
//                                for (MetricModel metric : metrics) {
//
//                                    if (componentNodeInstanceDAO
//                                            .searchSpecificComponentWithHexID(metric.getComponentNodeInstanceHexId(),
//                                                    applicationInstance.getApplicationInstanceID()).isPresent()) {
//
//                                        ComponentNodeInstance componentNodeInstance = componentNodeInstanceDAO
//                                                .searchSpecificComponentWithHexID(metric.getComponentNodeInstanceHexId(),
//                                                        applicationInstance.getApplicationInstanceID()).get();
//
//                                        JSONObject metricModelObj = new JSONObject();
//
//                                        metricModelObj.put("graphId", applicationInstance.getApplication().getId() + "");
//                                        metricModelObj
//                                                .put("graphInstanceId", applicationInstance.getApplicationInstanceID() + "");
//                                        metricModelObj.put("graphName", applicationInstance.getApplication().getName());
//                                        metricModelObj.put("graphInstanceName", applicationInstance.getName());
//
//                                        metricModelObj.put("componentNodeInstanceId",
//                                                componentNodeInstance.getComponentNodeInstanceID() + "");
//                                        metricModelObj.put("componentNodeId",
//                                                componentNodeInstance.getComponentNode().getComponentNodeID() + "");
//                                        metricModelObj.put("componentNodeInstanceName", componentNodeInstance.getName());
//                                        metricModelObj
//                                                .put("componentNodeName", componentNodeInstance.getComponentNode().getName());
//
//                                        metricModelObj.put("percentageUsedCPU", metric.getPercentageUsedCPU());
//                                        metricModelObj.put("diskAvailable", metric.getDiskAvailable());
//                                        metricModelObj.put("percentageUsedRAM", metric.getPercentageUsedRAM());
//
//                                        metricModelObj
//                                                .put("networkTrafficPacketsSend", metric.getNetworkTrafficPacketsSend());
//                                        metricModelObj.put("networkTrafficPacketsReceived",
//                                                metric.getNetworkTrafficPacketsReceived());
//
//                                        metricModelObj.put("diskThroughputRead", metric.getDiskThroughputRead());
//                                        metricModelObj.put("diskThroughputWrite", metric.getDiskThroughputWrite());
//
//                                        metricModelObj.put("timestamp", metric.getTimestamp());
//
//                                        if (null != metric.getProxyMetricModels() && !metric.getProxyMetricModels()
//                                                .isEmpty()) {
//
//                                            JSONArray proxyMetrics = new JSONArray();
//
//                                            for (ProxyMetricModel proxyMetricModel : metric.getProxyMetricModels()) {
//
//                                                JSONObject proxyMetricObj = new JSONObject();
//                                                proxyMetricObj.put("functionName", proxyMetricModel.getFunctionName());
//                                                proxyMetricObj.put("invocationsResponseTime",
//                                                        proxyMetricModel.getInvocationsResponseTime());
//                                                proxyMetricObj
//                                                        .put("invocationsPerSec", proxyMetricModel.getInvocationsPerSec());
//
//                                                proxyMetrics.put(proxyMetricObj);
//
//                                            }
//
//                                            metricModelObj.put("proxyMetrics", proxyMetrics);
//
//                                        }
//
//                                        metricsObj
//                                                .put(componentNodeInstance.getComponentNodeInstanceID() + "", metricModelObj);
//
//                                    }
//
//                                }
//
//                                // Send WS message to UI
//                                String wsMessage = metricsObj.toString();
////                            logger.info("WS Msg: " + wsMessage);
//
//                                wsTemplate.convertAndSend(GRAPHS_TOPIC, wsMessage);
//
////                            logger.info("Graphs for application instance: " + applicationInstance.getName() + " have been fetched successfully!");
//
//                            } else {
//                                logger.info("Graphs for application instance: " + applicationInstance.getName()
//                                        + " have not been fetched successfully!");
//                            }
//
//                        } catch (Exception e) {
//                            logger.log(Level.SEVERE, e.getMessage(), e);
//                            e.printStackTrace();
//                        }
//
//
//                    } else {
//                        logger.info("Graphs for application instance: " + applicationInstance.getName()
//                                + " have not been fetched successfully!");
//                    }
//
//                }
//
//
//            });
//
//        }
//
//
//    }


    //TODO check again - K8S rainbow check status
    @Scheduled(fixedDelay = 30000)
    public void checkRainbowServiceGraph() {
        try {
            kubernetesService.updateK8sDeploymentStatus();
            ossKubernetesService.updateK8sDeploymentStatus();
        } catch (Exception exception) {
            logger.log(Level.SEVERE, "Failed to update k8s deployment status: {0}", exception.getMessage());
        }
    }

    /*@RequestMapping(value = "/{pathOne}", method = RequestMethod.GET)
    public String pathDepthOne() {
        return "index";
    }

    @RequestMapping(value = "/{pathOne:?:^((?!api).)*$}/{pathTwo:[^\\.]*}", method = RequestMethod.GET)
    public String pathDepthTwo() { return "index"; }

    @RequestMapping(value = "/{pathOne:?:^((?!api).)*$}/{pathTwo:[^\\.]*}/{pathThree:[^\\.]*}", method = RequestMethod.GET)
    public String pathDepthThree() {
        return "index";
    }

    @RequestMapping(value = "/{pathOne:?:^((?!api).)*$}/{pathTwo:[^\\.]*}/{pathThree:[^\\.]*}/{pathFour:[^\\.]*}", method = RequestMethod.GET)
    public String pathDepthFour() {
        return "index";
    }*/

}
