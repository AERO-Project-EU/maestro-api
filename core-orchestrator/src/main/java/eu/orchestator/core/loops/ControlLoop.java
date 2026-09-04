package eu.orchestator.core.loops;

import eu.orchestator.core.configuration.AmazonConfig;
import eu.orchestator.core.configuration.BackendConfig;
import eu.orchestator.core.configuration.ConsulConfig;
import eu.orchestator.core.configuration.GeneralConfig;
import eu.orchestator.core.configuration.IntrusionDetectionConfig;
import eu.orchestator.core.configuration.KafkaConfig;
import eu.orchestator.core.configuration.RelayConfig;
import eu.orchestator.core.configuration.UbiDellConfig;
import eu.orchestator.core.configuration.VirtualizationManagerConfig;
import eu.orchestator.core.loops.metric.model.Lifecycle;
import eu.orchestator.core.model.agent.AgentParameters;
import eu.orchestator.core.model.agent.Arguments;
import eu.orchestator.core.model.agent.ComponentImage;
import eu.orchestator.core.model.agent.Dependencies;
import eu.orchestator.core.model.agent.HostsMapping;
import eu.orchestator.core.model.agent.HostsMappingStatus;
import eu.orchestator.core.model.agent.IntrusionDetectionParameters;
import eu.orchestator.core.model.agent.KafkaConfiguration;
import eu.orchestator.core.model.orchestrator.HostnameMapping;
import eu.orchestator.core.model.orchestrator.IPType;
import eu.orchestator.core.model.orchestrator.netdata.NetdataConfiguration;
import eu.orchestator.core.util.ConverterTo;
import eu.orchestator.core.util.MetricsConfigurator;
import eu.orchestator.core.util.OperationOnFiles;
import eu.orchestrator.collector.ChartType;
import eu.orchestrator.collector.Collector;
import eu.orchestrator.elasticity.spi.adapter.ElasticityFrameworkOrchestrator;
import eu.orchestrator.elasticity.spi.model.metricModel.Elasticity;
import eu.orchestrator.elasticity.spi.model.orchestrator.ControllerStatus;
import eu.orchestrator.elasticity.spi.model.orchestrator.OrchestratorScalingTO;
import eu.orchestrator.elasticity.spi.model.orchestrator.ScaleInTO;
import eu.orchestrator.elasticity.spi.model.orchestrator.ScaleOutTO;
import eu.orchestrator.elasticity.spi.model.orchestrator.UpdateControllerTO;
import eu.orchestrator.transfer.entities.backend.SecurityConfiguration;
import eu.orchestrator.transfer.entities.backend.SecurityConfigurationResult;
import eu.orchestrator.transfer.entities.backend.SecurityConfigurationResult.HashType;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorApplicationInstance;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorApplicationInstanceUsedResources;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorChangedStatusNotification;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorComponentNodeInstance;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorDependency;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorFlavor;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorIP;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorInstance;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorKeyValue;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorPort;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorProviderAuthenticationDetails;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorScalingRequest;
import eu.orchestrator.transfer.entities.orchestrator.internal.ComposeStatusObject;
import eu.orchestrator.transfer.entities.orchestrator.internal.ServiceStatus;
import eu.orchestrator.transfer.entities.policyEngine.ActionType;
import eu.orchestrator.transfer.entities.policyEngine.TriggerActionModel;
import eu.orchestrator.transfer.entities.virtualizationManager.VirtulizationManagerRequest;
import eu.orchestrator.transfer.util.Encryption;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.catalog.model.CatalogNode;
import com.ecwid.consul.v1.health.model.Check;
import com.ecwid.consul.v1.kv.model.GetValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;


/**
 * @author Konstantinos Theodosiou.
 * @email konstheodosiou@gmail.com
 * @date 22/3/2019
 */
public class ControlLoop implements Runnable {

    private static final Logger logger = LogManager.getLogger(ControlLoop.class);
    //Config for the other microservices and tools
    private VirtualizationManagerConfig virtualizationManagerConfig;
    private IntrusionDetectionConfig idsConfig;
    private BackendConfig backendConfig;
    private ConsulConfig consulConfig;
    private KafkaConfig kafkaConfig;
    private Boolean backendBoot;
    private AmazonConfig amazonConfig;
    private UbiDellConfig ubidellConfig;
    private RelayConfig relayConfig;
    private List elasticityFrameworkAdapters;
    private GeneralConfig generalConfig;

    //Metrics
    private Collector metricCollector;
    private String metricPrefix;
    private String lifecycleMetric;
    private String elasticityMetric;
    private String workersMetric;
    private String lifecycleMetricId;
    private String lifecycleDimensionId;
    private String elasticityMetricId;
    private String workersMetricId;
    private HashMap<String, String> elasticityDimensionId;      //key: componentNodeHexID value: dimensionID
    private HashMap<String, String> activeWorkersDimensionId;   //key: componentNodeHexID value: dimensionID
    private HashMap<String, Integer> activeWorkersCount;         //key: componentNodeHexID value: active workers count
    private HashMap<String, String> totalWorkersDimensionId;    //key: componentNodeHexID value: dimensionID
    private HashMap<String, Integer> totalWorkersCount;          //key: componentNodeHexID value: total workers count
    private HashMap<String, HostnameMapping> hostnameMappingHashMap;
    private Integer hostnameMappingCounter = 0;

    //Graph state objects of the ControlLoop
    private ComposeStatusObject composeStatusObject;
    private OrchestratorApplicationInstance composeObject;

    private ConsulClient consulClient;
    private ObjectMapper objectMapper;

    //The key is = getComponentNodeHexID() + "_" + getComponentNodeInstanceHexID()
    private HashMap<String, ServiceStatus> hashMapServiceStatus;
    private HashMap<String, OrchestratorComponentNodeInstance> hashMapService;
    //The key is the serverName aka consul node name
    private HashMap<String, OrchestratorComponentNodeInstance> hashMapServerName;

    //Kafka Producers and Consumers
    private Consumer<String, String> backendRequestConsumer;
    private Producer<String, String> reportStatusProducer;
    private Producer<String, String> securityResultsProducer;
    private Consumer<String, String> policyEngineConsumer;
    //Kafka used topics
    private String reportStatusTopic = "";
    private String policyEngineTopic = "";
    private String backendRequest = "";
    private Boolean ipv6Enabled;

    // Consul node healthcheck string
    private static final String NODE_CHECK_ID = "serfHealth";

    //Status
    private static final int SPAWNED = 1;
    private static final int STATUS_INITIALIZED = 2;
    private static final int STATUS_IMAGEDOWNLOADED = 3;
    private static final int STATUS_WAITING_FOR_DEPENDENCIES = 4;
    private static final int STATUS_TRIGGERED_CONTAINER_START = 5;
    private static final int STATUS_STARTED = 6;
    private static final int STATUS_LISTEN_FOR_COMMAND_UP = 7;
    //Status orchestrator
    private static final int SPAWNING = 0;
    private static final int STATUS_COMPONENT_INSTANCE_UP = 8;

    //ERROR status
    private static final int STATUS_TERMINATED_DUE_TO_TIMEOUT = -1;
    private static final int STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_INITIALIZATION = -2;
    private static final int STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_INITIALIZATION_RETRIEVE_IPV4 = -22;
    private static final int STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_INITIALIZATION_RETRIEVE_IPV6 = -23;
    private static final int STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_INITIALIZATION_OF_CONSUL = -24;
    private static final int STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_INITIALIZATION_OF_LOGIN_DOCKER = -25;
    private static final int STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_INITIALIZATION_OF_NETDATA_REGISTRATION = -26;

    private static final int STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_DOWNLOAD = -3;
    private static final int STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_DOWNLOAD_IMAGE = -32;
    private static final int STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_IMAGE_NOT_FOUND = -33;
    private static final int STATUS_TERMINATED_WAITING_FOR_DEPENDENCIES = -41;
    private static final int STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_CONTAINER_TRIGGERING = -5;
    private static final int STATUS_TERMINATED_DUE_TO_BAD_LAUNCH = -6;
    private static final int STATUS_TERMINATED_CONTAINER_SERVICE_IS_NOT_RUNNING = -61;
    private static final int STATUS_TERMINATED_DUE_TO_BAD_KV_PARSING = -7;
    //ERROR status orchestrator
    private static final int ERROR_WRONG_KV_CONFIGURATION = -8;
    private static final int ERROR_NO_NODE_ENTRY = -9;
    private static final int ERROR_COMPONENT_SERVICE_DOWN = -10;
    private static final int ERROR_COMPONENT_VM_DOWN = -11;
    private static final int ERROR_COULD_NOT_ATTACH_PUBLIC_IP = -12;

    //GRAPH Status
    private static final int STATUS_DEPLOYING = 0;
    private static final int STATUS_DEPLOYED = 1;
    private static final int STATUS_UPDATING = 2;

    //Timings
    private static final int WAIT_PERIOD_FOR_NEXT_REQUEST = 1000 * 5; // 5 seconds
    private static final int MAXIMUM_TIMES_OF_REQUEST = 15; // 15 request and then exit
    private static final int READ_TOPIC_TIMEOUT = 1000 * 2; // 2 seconds
    private static final int HEALTH_CHECK_RETRIES = 20; // times to multiply the healtcheck in order to know how to wait until state the component as error
    private static final int TIME_TO_TERMINATE = 1000 * 10; // 10 seconds

    /*
     * Initialize the context for the Control Loop
     */
    public ControlLoop(OrchestratorApplicationInstance composeObject, ConsulConfig consulConfig, IntrusionDetectionConfig idsConfig,
            VirtualizationManagerConfig virtualizationManagerConfig, KafkaConfig kafkaConfig, BackendConfig backendConfig, AmazonConfig amazonConfig,
            UbiDellConfig ubidellConfig, RelayConfig relayConfig, List elasticityFrameworkAdapters, GeneralConfig generalConfig,
            Boolean backendBoot, Collector metricCollector) {

        this.virtualizationManagerConfig = virtualizationManagerConfig;
        this.idsConfig = idsConfig;
        this.backendConfig = backendConfig;
        this.consulConfig = consulConfig;
        this.kafkaConfig = kafkaConfig;
        this.backendBoot = backendBoot;
        this.amazonConfig = amazonConfig;
        this.ubidellConfig = ubidellConfig;
        this.elasticityFrameworkAdapters = elasticityFrameworkAdapters;

        this.generalConfig = generalConfig;

        this.relayConfig = relayConfig;

        this.composeStatusObject = null;
        this.composeObject = composeObject;

        this.consulClient = new ConsulClient(consulConfig.getUrl());
        this.objectMapper = new ObjectMapper();

        this.reportStatusTopic = kafkaConfig.getReportStatusTopic();
        this.backendRequest = kafkaConfig.getBackendRequest();
        this.policyEngineTopic = kafkaConfig.getPolicyEngineTopic();

        String transactionalId = composeObject.getGraphHexID() + "_" + composeObject.getGraphInstanceHexID() + "_";
        this.reportStatusProducer = constructProducer(transactionalId + "status" + kafkaConfig.getGroup());
        this.securityResultsProducer = constructProducer(transactionalId + "status" + kafkaConfig.getGroup() + "_1");

        String consumerGroup = composeObject.getGraphHexID() + "_" + composeObject.getGraphInstanceHexID() + "_";
        this.backendRequestConsumer = constructConsumer(backendRequest, consumerGroup + "backend" + kafkaConfig.getGroup());
        this.policyEngineConsumer = constructConsumer(policyEngineTopic, consumerGroup + "policy" + kafkaConfig.getGroup());

        this.ipv6Enabled = composeObject.getIpv6Enabled();

        this.metricCollector = metricCollector;
        this.metricPrefix = "_" + composeObject.getGraphHexID() + "_" + composeObject.getGraphInstanceHexID() + "_";
        this.lifecycleMetric = "orchestrator" + metricPrefix + "deployment";
        this.elasticityMetric = "orchestrator" + metricPrefix + "elasticity";
        this.workersMetric = "orchestrator" + metricPrefix + "workers";
        this.elasticityDimensionId = new HashMap<>();
        this.activeWorkersDimensionId = new HashMap<>();
        this.totalWorkersDimensionId = new HashMap<>();
        this.activeWorkersCount = new HashMap<>();
        this.totalWorkersCount = new HashMap<>();
        this.hostnameMappingHashMap = new HashMap<>();
    }

    @Override
    public void run() {
        lifecycleMetricId = metricCollector
                .registerMetric(this.lifecycleMetric, "Orchestrator lifecycle metrics", "ms", "orchestrator", this.lifecycleMetric, ChartType.line);
        lifecycleDimensionId = metricCollector.registerDimensionToMetric(lifecycleMetricId, "lifecycle");
        metricCollector.logMetric(lifecycleDimensionId, Lifecycle.RequestDeploy.getStatus());

        workersMetricId = metricCollector
                .registerMetric(this.workersMetric, "Orchestrator workers metrics", "ms", "orchestrator", this.workersMetric, ChartType.line);
        elasticityMetricId = metricCollector
                .registerMetric(this.elasticityMetric, "Orchestrator elasticity metrics", "ms", "orchestrator", this.elasticityMetric, ChartType.line);

        for (OrchestratorComponentNodeInstance componentNodeInstance : composeObject.getServices()) {
            if (null != componentNodeInstance.getMonitoringElasticity() && null != componentNodeInstance.getMonitoringElasticity().getProfile()
                    && !componentNodeInstance.getMonitoringElasticity().getProfile().equals("NONE")) {
                String componentNodeHexId = componentNodeInstance.getComponentNodeHexID();
                String dimensionId;
                logger.info(
                        "GID-GIID-CNID: " + composeObject.getGraphHexID() + "-" + composeObject.getGraphInstanceHexID() + "-" + componentNodeHexId + " Name: "
                                + componentNodeInstance.getComponentNodeName() + " profile: " + componentNodeInstance.getMonitoringElasticity().getProfile());
                if (!elasticityDimensionId.containsKey(componentNodeHexId)) {
                    dimensionId = metricCollector.registerDimensionToMetric(elasticityMetricId, "component_" + componentNodeHexId);
                    elasticityDimensionId.put(componentNodeHexId, dimensionId);

                    dimensionId = metricCollector.registerDimensionToMetric(workersMetricId, "active_component_" + componentNodeHexId);
                    activeWorkersDimensionId.put(componentNodeHexId, dimensionId);
                    activeWorkersCount.put(componentNodeHexId, 0);

                    dimensionId = metricCollector.registerDimensionToMetric(workersMetricId, "total_component_" + componentNodeHexId);
                    totalWorkersDimensionId.put(componentNodeHexId, dimensionId);
                    totalWorkersCount.put(componentNodeHexId, 1);
                } else {
                    totalWorkersCount.put(componentNodeHexId, totalWorkersCount.get(componentNodeHexId) + 1);
                }
            }
        }

        for (String componentNodeHexId : totalWorkersCount.keySet()) {
            metricCollector.logMetric(activeWorkersDimensionId.get(componentNodeHexId), activeWorkersCount.get(componentNodeHexId));
            metricCollector.logMetric(totalWorkersDimensionId.get(componentNodeHexId), totalWorkersCount.get(componentNodeHexId));
        }

        if (backendBoot) {
            hotControlLoop();
        } else {
            coldControlLoop();
        }
    }

    /*
     * Control Loop that started after the VM got down by framework elasticity Microservice
     */
    public void coldControlLoop() {
        String graphHexId = composeObject.getGraphHexID();
        String graphInstanceHexId = composeObject.getGraphInstanceHexID();

        fetchState(graphHexId, graphInstanceHexId);
        //TODO we need to check how many of the components VMs have been spawned
        //and spawn the rest of them
        logger.info("Metadata for the Graph: " + graphHexId + " graphInstance: " + graphInstanceHexId + " fetched.");
        updateHashMaps();

        mainLoop();
    }

    /*
     * Control Loop that started after a request came at the queue
     */
    public void hotControlLoop() {

        String graphHexId = composeObject.getGraphHexID();
        String graphInstanceHexId = composeObject.getGraphInstanceHexID();
        composeStatusObject = getComposeStatusObject(composeObject);

        saveState(graphHexId, graphInstanceHexId);
        updateHashMaps();

        Boolean check = deployGraph(composeObject, true);
        if (!check) {
            logger.error("The deployment for the graph: " + graphHexId + " graphInstance: " + graphInstanceHexId + " FAILED");
            return;
        }
        calculateGraphResources();
        mainLoop();
    }

    /*
     * Loop that monitors the whole instance of a graph
     * and calls other methods in order to have a stable graph
     * also communicates with other Microservices of the framework
     */
    private void mainLoop() {
        String graphHexId = composeObject.getGraphHexID();
        String graphInstanceHexId = composeObject.getGraphInstanceHexID();

        while (true) {

            updateGraphDeploymentStatus();

            for (ServiceStatus serviceStatus : composeStatusObject.getServiceStatusList()) {
                String componentNodeHexId = serviceStatus.getComponentNodeHexID();
                String componentNodeInstanceHexId = serviceStatus.getComponentNodeInstanceHexID();

                //Get current status
                int currentStatus = checkComponentState(graphHexId, graphInstanceHexId, serviceStatus);
                if (!composeObject.getTelco5GEnabled()) {
                    if (serviceStatus.getAccessInterface() && (serviceStatus.getFloatingIP() == null || serviceStatus.getFloatingIP().isEmpty())
                            && currentStatus >= SPAWNED) {
                        //Get the service
                        OrchestratorComponentNodeInstance service = hashMapService
                                .get(serviceStatus.getComponentNodeHexID() + "_" + serviceStatus.getComponentNodeInstanceHexID());
                        Boolean attachCheck = attachFloatingIpCall(service, serviceStatus);
                        if (!attachCheck) {
                            logger.warn("Could not attach Public IP to the service: " + graphHexId + ":" + graphInstanceHexId + ":" + componentNodeHexId + ":"
                                    + componentNodeInstanceHexId);
                            currentStatus = ERROR_COULD_NOT_ATTACH_PUBLIC_IP;
                        } else {
                            logger.info("The Public IP: " + serviceStatus.getFloatingIP() + " attached to the service: " + graphHexId + ":" + graphInstanceHexId
                                    + ":" + componentNodeHexId + ":" + componentNodeInstanceHexId);
                        }
                    }
                }

                updateReportingStatusTopic(serviceStatus, currentStatus);
                //Get the service of the load balancer
                OrchestratorComponentNodeInstance component = hashMapService
                        .get(serviceStatus.getComponentNodeHexID() + "_" + serviceStatus.getComponentNodeInstanceHexID());
                if (null != component.getController() && component.getController()) {
                    ElasticityFrameworkOrchestrator elasticityAdapter = fetchFrameworkAdapterImpl(component);

                    OrchestratorScalingTO orchestratorScalingTo = new OrchestratorScalingTO(composeObject, composeStatusObject, null, backendConfig.getUrl(),
                            backendConfig.getPort(), ConverterTo.consulConfigToConverter(consulConfig), serviceStatus, null, null);

                    Object controllerNewMetadata = elasticityAdapter.getHealthyWorkers(orchestratorScalingTo, component, hashMapServerName);

                    UpdateControllerTO updateControllerTo = elasticityAdapter
                            .updateElasticityController(orchestratorScalingTo, component, controllerNewMetadata);
                    ControllerStatus controllerStatus = updateControllerTo.getControllerStatus();

                    //If update or init done change the load balancer status
                    if (controllerStatus.getStatus() != ControllerStatus.EMPTY_CONTROLLER_CONFIG.getStatus()
                            && controllerStatus.getStatus() != ControllerStatus.NO_CONTROLLER_UPDATE_NEEDED.getStatus()) {

                        component.setControllerMetadata(updateControllerTo.getControllerMetadata());
                    }
                }
            }

            //Read backend Request and apply them
            Boolean check = readBackendRequests();
            //if check = true this means that we receive undeploy request so we must exit
            if (check) {
                break;
            }

            readTriggeredActions();
            saveState(graphHexId, graphInstanceHexId);

            if (this.hostnameMappingCounter == 0) {
                List<String> hostEntries = new ArrayList<>();

                for (String key : this.hostnameMappingHashMap.keySet()) {
                    HostnameMapping hostnameMapping = this.hostnameMappingHashMap.get(key);
                    hostEntries.add(hostnameMapping.getHostname() + ":" + hostnameMapping.getIp());
                }
                HostsMapping hostsMapping = new HostsMapping();
                hostsMapping.setEntries(hostEntries);

                String hostsMappingJson = "";
                try {
                    hostsMappingJson = objectMapper.writeValueAsString(hostsMapping);
                } catch (JsonProcessingException ex) {
                    logger.error("Couldn't cast hostsMapping to json " + ex.getMessage());
                }

                consulClient
                        .setKVValue(this.composeObject.getGraphHexID() + "/" + this.composeObject.getGraphInstanceHexID() + "/host/mappings", hostsMappingJson);
                consulClient.setKVValue(this.composeObject.getGraphHexID() + "/" + this.composeObject.getGraphInstanceHexID() + "/host/status",
                        HostsMappingStatus.FETCH.getStatus() + "");
                this.hostnameMappingCounter = -1;
            }

        }
        metricCollector.logMetric(lifecycleDimensionId, Lifecycle.Undeployed.getStatus());

        //Wait some time in order to allow the Prometheus collect the metric
        try {
            Thread.sleep((long) TIME_TO_TERMINATE);
        } catch (InterruptedException ex) {
            logger.info("Couldn't wait in the sleep before terminating the Control Loop for the graph: " + composeObject.getGraphHexID() + "-" + composeObject
                    .getGraphInstanceHexID() + " with error: " + ex.getMessage());
            Thread.currentThread().interrupt();
        }

        garbageCollection();
    }


    /**
     * Main Loop Methods.
     */

    /*
     * Checks and updates the Reporting Status Topic if needed
     */
    private void updateReportingStatusTopic(ServiceStatus serviceStatus, int currentStatus) {
        //Check if it's the First Info that will be send
        // if not, check if the status has change from the previous time
        if (!serviceStatus.getFirstInfo()) {
            if (serviceStatus.getStatus() == currentStatus) {
                return;
            }
        } else {
            //if yes make it false
            serviceStatus.setFirstInfo(false);
        }

        String graphHexId = composeObject.getGraphHexID();
        String graphInstanceHexId = composeObject.getGraphInstanceHexID();
        String componentNodeHexId = serviceStatus.getComponentNodeHexID();
        String componentNodeInstanceHexId = serviceStatus.getComponentNodeInstanceHexID();

        String msg = graphHexId + " - " + graphInstanceHexId + " - " + componentNodeHexId + " - " + componentNodeInstanceHexId;
        //Construct the appropriate message and update the Reporting Status Topic
        logger.info("The component: " + msg + " is in status: " + currentStatus);

        if (currentStatus < STATUS_COMPONENT_INSTANCE_UP && serviceStatus.getStatus() >= STATUS_COMPONENT_INSTANCE_UP && activeWorkersDimensionId
                .containsKey(componentNodeHexId)) {
            decreaseActiveWorkersMetric(componentNodeHexId, 1);
        } else if (currentStatus >= STATUS_COMPONENT_INSTANCE_UP && serviceStatus.getStatus() < STATUS_COMPONENT_INSTANCE_UP && activeWorkersDimensionId
                .containsKey(componentNodeHexId)) {
            increaseActiveWorkersMetric(componentNodeHexId, 1);
        }

        serviceStatus.setStatus(currentStatus);
        serviceStatus.setChangeType(ServiceStatus.ChangeType.AgentStatusChange.name());

        if (currentStatus >= SPAWNED && (null == serviceStatus.getIpList() || serviceStatus.getIpList().isEmpty())) {

            List<OrchestratorIP> ipList = null;
            for (int i = 0; i < 10; i++) {
                ipList = getIpList(serviceStatus);
                if (ipList != null && !ipList.isEmpty()) {
                    break;
                }
            }

            if (this.ipv6Enabled == false && this.hostnameMappingCounter > 0 && this.hostnameMappingHashMap.containsKey(serviceStatus.getComponentNodeHexID())
                    && ipList != null) {

                HostnameMapping hostnameMapping = this.hostnameMappingHashMap.get(serviceStatus.getComponentNodeHexID());
                if (hostnameMapping.getIp() == null || hostnameMapping.getIp().isEmpty()) {
                    //hostnameMapping.setIp(ipList.get(0).getIp()); Original
                    hostnameMapping.setIp(getServiceIp(serviceStatus, IPType.IPv4));
                    this.hostnameMappingHashMap.put(serviceStatus.getComponentNodeHexID(), hostnameMapping);
                    this.hostnameMappingCounter += -1;
                }
            } else if (this.ipv6Enabled == true && this.hostnameMappingCounter > 0 && this.hostnameMappingHashMap
                    .containsKey(serviceStatus.getComponentNodeHexID())) {

                HostnameMapping hostnameMapping = this.hostnameMappingHashMap.get(serviceStatus.getComponentNodeHexID());
                if (hostnameMapping.getIp() == null || hostnameMapping.getIp().isEmpty()) {
                    String serviceIPv6 = getServiceIp(serviceStatus, IPType.IPv6);
                    if (serviceIPv6 != null && !serviceIPv6.isEmpty()) {
                        hostnameMapping.setIp(serviceIPv6);
                        this.hostnameMappingHashMap.put(serviceStatus.getComponentNodeHexID(), hostnameMapping);
                        this.hostnameMappingCounter += -1;
                    }
                }
            }

            if (serviceStatus.getAccessInterface() && null != serviceStatus.getFloatingIP() && !serviceStatus.getFloatingIP().isEmpty()) {
                OrchestratorIP publicIp = new OrchestratorIP();
                publicIp.setIp(serviceStatus.getFloatingIP());
                publicIp.setInterfaceType("ACCESS");
                publicIp.setNetwork("PublicIP");
                ipList.add(publicIp);
            }
            serviceStatus.setIpList(ipList);
        }

        OrchestratorKeyValue keyValue = getReportStatusString(serviceStatus.getStatus());
        serviceStatus.setReportedChange(keyValue.getValue());

        OrchestratorChangedStatusNotification reportingStatusObject = constructReportingStatusObject(serviceStatus,
                keyValue);
        sendReportingStatus(reportingStatusObject, "0");
    }

    /*
     * Checks and updates the Reporting Status Topic for the whole graph if needed
     */
    private void updateGraphDeploymentStatus() {

        //TODO check in order to report also states < current
        OrchestratorChangedStatusNotification graphStatus = new OrchestratorChangedStatusNotification();
        Boolean change = false;

        //Check if it is the first time that we are reporting something about the whole graph instance
        if (composeStatusObject.getFirstInfo()) {
            //Check if all component instances are up and running
            for (ServiceStatus serviceStatus : composeStatusObject.getServiceStatusList()) {
                if (serviceStatus.getStatus() != STATUS_COMPONENT_INSTANCE_UP) {
                    return;
                }
            }
            //If so, check the graph status, if not deployed and last ReportedChange different that DEPLOYED
            if (composeStatusObject.getStatus() != STATUS_DEPLOYED && !composeStatusObject.getReportedChange().equals("DEPLOYED")) {
                graphStatus.setReportedChange("DEPLOYED");
                composeStatusObject.setReportedChange("DEPLOYED");
                composeStatusObject.setStatus(STATUS_DEPLOYED);
                composeStatusObject.setFirstInfo(false);
                change = true;
                metricCollector.logMetric(lifecycleDimensionId, Lifecycle.Deployed.getStatus());
            }

            for (OrchestratorComponentNodeInstance service : composeObject.getServices()) {
                if (null != service.getProduceHashes() && service.getProduceHashes()) {
                    sendHashesToBackend(service, "0");
                }
            }

        } else {
            Boolean deployed = true;
            //Check if all component instances are up and running
            for (ServiceStatus serviceStatus : composeStatusObject.getServiceStatusList()) {
                if (serviceStatus.getStatus() != STATUS_COMPONENT_INSTANCE_UP) {
                    deployed = false;
                    break;
                }
            }
            //If graph instance status is deployed
            if (composeStatusObject.getStatus() == STATUS_DEPLOYED && composeStatusObject.getReportedChange().equals("DEPLOYED")) {
                //If not deployed then a scaling is currently been done on the graph instance
                if (!deployed) {
                    graphStatus.setReportedChange("UPDATING");
                    composeStatusObject.setReportedChange("UPDATING");
                    composeStatusObject.setStatus(STATUS_UPDATING);
                }
            } else if (composeStatusObject.getStatus() == STATUS_UPDATING && composeStatusObject.getReportedChange().equals("UPDATING")) {
                //If deployed then a scaling has been completed
                if (deployed) {
                    graphStatus.setReportedChange("DEPLOYED");
                    composeStatusObject.setReportedChange("DEPLOYED");
                    composeStatusObject.setStatus(STATUS_DEPLOYED);
                    metricCollector.logMetric(lifecycleDimensionId, Lifecycle.Deployed.getStatus());
                }
            }
        }

        if (change) {
            graphStatus.setGraphID(composeObject.getGraphID());
            graphStatus.setGraphHexID(composeObject.getGraphHexID());
            graphStatus.setGraphName(composeObject.getGraphName());
            graphStatus.setGraphInstanceID(composeObject.getGraphInstanceID());
            graphStatus.setGraphInstanceHexID(composeObject.getGraphInstanceHexID());
            graphStatus.setGraphInstanceName(composeObject.getGraphInstanceName());
            graphStatus.setChangeType(ServiceStatus.ChangeType.GraphStatusChange.name());
            logger.info("The graph: " + graphStatus.getGraphName() + "_" + graphStatus.getGraphInstanceName() + " is in status: " + graphStatus
                    .getReportedChange());
            sendReportingStatus(graphStatus, "0");
        }
    }

    /*
     * Check all the consul modules in order to see the state of a component worker
     */
    private int checkComponentState(String graphId, String graphInstanceId, ServiceStatus serviceStatus) {
        //TODO check in order to report also states < current
        String componentNodeId = serviceStatus.getComponentNodeHexID();
        String componentNodeInstanceId = serviceStatus.getComponentNodeInstanceHexID();

        String msg = graphId + " - " + graphInstanceId + " - " + componentNodeId + " - " + componentNodeInstanceId;
        String kvStatusPath = graphId + "/" + graphInstanceId + "/" + componentNodeId + "/" + componentNodeInstanceId + "/status";

        //check the status from the KV Consul module
        Response<GetValue> kvResponse = consulClient.getKVValue(kvStatusPath);
        //Check if the status path exists
        if (kvResponse == null || kvResponse.getValue() == null) {
            logger.debug("The VM of the " + msg + " has no status path on KV module");
            return ERROR_WRONG_KV_CONFIGURATION;
        }

        //check the value of the status
        String value = kvResponse.getValue().getDecodedValue();
        if (value == null || value.isEmpty()) {
            //Someone must writing in this path
            //TODO must check it after N seconds again?
            logger.debug("Someone is writing on the status of the " + msg);
            return SPAWNED;
        }

        Integer status = Integer.parseInt(value);

        //The status is bigger tha zero, so we must check the other subcases
        if (status > 0) {
            //The status have not been changed after the deployment
            if (status == 1) {
                //Check if the VM has been spawned in the Nodes Consul module
                String nodeName = graphId + "-" + graphInstanceId + "-" + componentNodeId + "-" + componentNodeInstanceId;
                Boolean spawned = false;
                Response<List<Check>> response = consulClient.getHealthChecksForNode(nodeName, QueryParams.DEFAULT);
                if (response == null || response.getValue() == null || response.getValue().isEmpty()) {
                    //The VM has not been spawned yet, as shown by the Nodes Consul module
                    logger.debug("The VM of the " + msg + " must be in spawning phase, considering the Nodes module");
                } else {
                    List<Check> checkList = response.getValue();

                    //iterate through the checks of the node in order to find serfHealth
                    //which tells us if a node is in critical status or not
                    for (Check check : checkList) {
                        if (check.getCheckId().equals(NODE_CHECK_ID)) {
                            if (check.getStatus().equals(Check.CheckStatus.PASSING)) {
                                logger.debug("The VM of the " + msg + " has been spawned");
                                spawned = true;
                            } else {
                                logger.debug("The VM of the " + msg + " is in critical status considering the Nodes module" + ", so it must have been spawned");
                                spawned = false;
                            }
                            break;
                        }
                    }
                }
                if (!spawned) {
                    //TODO shall we check the provider in order to see if the spawning process has began or not?
                    //this call must be synchronous and not by using the queue
                    return SPAWNING;
                } else {
                    return SPAWNED;
                }
            } else { //The status has been changed to a positive value
                //The status says that the service has been started, so lets check the Services Consul module
                if (status >= STATUS_STARTED) {
                    String nodeName = graphId + "-" + graphInstanceId + "-" + componentNodeId + "-" + componentNodeInstanceId;
                    Response<List<Check>> response = consulClient.getHealthChecksForNode(nodeName, QueryParams.DEFAULT);
                    if (response == null || response.getValue() == null || response.getValue().isEmpty()) {
                        //The VM has not been spawned yet, as shown by the Nodes Consul module
                        logger.warn("The status of the component " + msg + " is: " + status + " but it hasn't been register in the Nodes Consul module");
                        return ERROR_NO_NODE_ENTRY;
                    } else {
                        List<Check> checkList = response.getValue();
                        String serviceName = graphId + ":" + graphInstanceId + ":" + componentNodeId;
                        //iterate through the checks of the node in order to find
                        //We only checking if the service of the component is up and running
                        // and the serfHealth, nothing else
                        //TODO check the netdata, security and agent, service in the future
                        for (Check check : checkList) {
                            if (check.getCheckId().equals(NODE_CHECK_ID)) {
                                if (!check.getStatus().equals(Check.CheckStatus.PASSING)) {
                                    logger.error("The VM of the " + msg + " is down");
                                    return ERROR_COMPONENT_VM_DOWN;
                                }
                            } else if (check.getServiceName().equals(serviceName)) {
                                if (!check.getStatus().equals(Check.CheckStatus.PASSING)) {
                                    logger.warn("The Component: " + msg + " seems to be down");
                                    if (serviceStatus.getHealthCheckFirstDate() <= 0) {
                                        logger.warn("Lets wait some more time for the Component: " + msg + " to start properly");
                                        Long currentTime = new Date().getTime() / 1000;
                                        serviceStatus.setHealthCheckFirstDate(currentTime);
                                        return serviceStatus.getStatus();
                                    } else {
                                        Long currentTime = new Date().getTime() / 1000;
                                        Long diff = currentTime - serviceStatus.getHealthCheckFirstDate();
                                        if (diff < serviceStatus.getHealthCheckTimeLimit()) {
                                            logger.warn("Lets wait some more time for the Component: " + msg + " to start properly");
                                            return serviceStatus.getStatus();
                                        }
                                    }
                                    logger.error("The Component: " + msg + " is down");
                                    return ERROR_COMPONENT_SERVICE_DOWN;
                                }
                            }
                        }
                        logger.debug("The VM of the " + msg + " is in Status: " + STATUS_COMPONENT_INSTANCE_UP);
                        return STATUS_COMPONENT_INSTANCE_UP;
                    }
                } else {
                    logger.debug("The VM of the " + msg + " is in Status: " + status);
                    return status;
                }
            }

        } else if (status < 0) { //The status is a negative number, so an error must occurred in the agent
            logger.debug("The VM of the " + msg + " is in ERROR Status: " + status);
            return status;
        } else {
            logger.warn("Someone changed the status value to 0, UNSUPPORTED VALUE, component: " + msg);
            return status;
        }
    }

    /*
     * Construct reporting status object
     */
    private OrchestratorChangedStatusNotification constructReportingStatusObject(ServiceStatus serviceStatus, OrchestratorKeyValue keyValue) {
        OrchestratorChangedStatusNotification reportedStatus = new OrchestratorChangedStatusNotification();

        reportedStatus.setGraphID(composeObject.getGraphID());
        reportedStatus.setGraphHexID(composeObject.getGraphHexID());
        reportedStatus.setGraphName(composeObject.getGraphName());
        reportedStatus.setGraphInstanceID(composeObject.getGraphInstanceID());
        reportedStatus.setGraphInstanceHexID(composeObject.getGraphInstanceHexID());
        reportedStatus.setGraphInstanceName(composeObject.getGraphInstanceName());

        reportedStatus.setComponentNodeID(serviceStatus.getComponentNodeID());
        reportedStatus.setComponentNodeHexID(serviceStatus.getComponentNodeHexID());
        reportedStatus.setComponentNodeName(serviceStatus.getComponentNodeName());
        reportedStatus.setComponentNodeInstanceID(serviceStatus.getComponentNodeInstanceID());
        reportedStatus.setComponentNodeInstanceHexID(serviceStatus.getComponentNodeInstanceHexID());
        reportedStatus.setComponentNodeInstanceName(serviceStatus.getComponentNodeInstanceName());

        reportedStatus.setChangeType(serviceStatus.getChangeType());
        if (serviceStatus.getIpList() != null && !serviceStatus.getIpList().isEmpty()) {
            reportedStatus.setOrchestratorIPs(serviceStatus.getIpList());
        }

        if (serviceStatus.getAccessInterface()) {
            reportedStatus.setPublicIP(serviceStatus.getFloatingIP());
        }

        reportedStatus.setReportedChange(keyValue.getKey());
        reportedStatus.setMessage(keyValue.getValue());

        return reportedStatus;
    }

    /*
     * Converts all the status Integers to messages for the backend
     */
    private OrchestratorKeyValue getReportStatusString(Integer status) {
        OrchestratorKeyValue reportedChange = null;

        switch (status) {
            case (SPAWNING):
                reportedChange = new OrchestratorKeyValue("SPAWNING", "The VM is spawning");
                break;
            case (SPAWNED):
                reportedChange = new OrchestratorKeyValue("SPAWNED", "The VM spawned");
                break;
            case (STATUS_INITIALIZED):
                reportedChange = new OrchestratorKeyValue("STATUS_INITIALIZED", "Agent dependencies fulfilled");
                break;
            case (STATUS_IMAGEDOWNLOADED):
                reportedChange = new OrchestratorKeyValue("STATUS_IMAGEDOWNLOADED", "The component image downloaded");
                break;
            case (STATUS_WAITING_FOR_DEPENDENCIES):
                reportedChange = new OrchestratorKeyValue("STATUS_WAITING_FOR_DEPENDENCIES", "The component is waiting for it's dependencies");
                break;
            case (STATUS_TRIGGERED_CONTAINER_START):
                reportedChange = new OrchestratorKeyValue("STATUS_TRIGGERED_CONTAINER_START", "Initialization process has started");
                break;
            case (STATUS_STARTED):
                reportedChange = new OrchestratorKeyValue("STATUS_STARTED", "Component has been started successfully");
                break;
            case (STATUS_COMPONENT_INSTANCE_UP):
                reportedChange = new OrchestratorKeyValue("STATUS_COMPONENT_INSTANCE_UP", "Component is healthy, up and running");
                break;

            case (STATUS_LISTEN_FOR_COMMAND_UP):
                reportedChange = new OrchestratorKeyValue("STATUS_LISTEN_FOR_COMMAND_UP", "Agent is waiting for IPS commands");
                break;

            case (STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_INITIALIZATION_RETRIEVE_IPV4):
                reportedChange = new OrchestratorKeyValue("STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_INITIALIZATION_RETRIEVE_IPV4",
                        "Agent couldn't get the IPv4 of the VM");
                break;

            case (STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_INITIALIZATION_RETRIEVE_IPV6):
                reportedChange = new OrchestratorKeyValue("STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_INITIALIZATION_RETRIEVE_IPV6",
                        "Agent couldn't get the IPv6 of the VM");
                break;
            case (STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_INITIALIZATION_OF_CONSUL):
                reportedChange = new OrchestratorKeyValue("STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_INITIALIZATION_OF_CONSUL",
                        "Agent faced with an error that has to do with the consul");
                break;
            case (STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_INITIALIZATION_OF_LOGIN_DOCKER):
                reportedChange = new OrchestratorKeyValue("STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_INITIALIZATION_OF_LOGIN_DOCKER",
                        "Agent couldn't login to docker");
                break;

            case (STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_INITIALIZATION_OF_NETDATA_REGISTRATION):
                reportedChange = new OrchestratorKeyValue("STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_INITIALIZATION_OF_NETDATA_REGISTRATION",
                        "Agent couldn't register the monitoring service to the consul");
                break;
            case (STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_IMAGE_NOT_FOUND):
                reportedChange = new OrchestratorKeyValue("STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_IMAGE_NOT_FOUND ",
                        "The docker image doesn't exist in the registry");
                break;
            case (STATUS_TERMINATED_CONTAINER_SERVICE_IS_NOT_RUNNING):
                reportedChange = new OrchestratorKeyValue("STATUS_TERMINATED_CONTAINER_SERVICE_IS_NOT_RUNNING",
                        "Service could't be started due to wrong configuration");
                break;

            case (STATUS_TERMINATED_DUE_TO_TIMEOUT):
                reportedChange = new OrchestratorKeyValue("STATUS_TERMINATED_DUE_TO_TIMEOUT", "xxxx");
                break;
            case (STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_INITIALIZATION):
                reportedChange = new OrchestratorKeyValue("STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_INITIALIZATION", "Agent dependencies couldn't fulfilled");
                break;
            case (STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_DOWNLOAD):
                reportedChange = new OrchestratorKeyValue("STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_DOWNLOAD", "Agent couldn't download the image");
                break;
            case (STATUS_TERMINATED_WAITING_FOR_DEPENDENCIES):
                reportedChange = new OrchestratorKeyValue("STATUS_TERMINATED_WAITING_FOR_DEPENDENCIES",
                        "The component couldn't wait more for it's dependencies");
                break;
            case (STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_CONTAINER_TRIGGERING):
                reportedChange = new OrchestratorKeyValue("STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_CONTAINER_TRIGGERING",
                        "Agent couldn't trigger start for the component");
                break;
            case (STATUS_TERMINATED_DUE_TO_BAD_LAUNCH):
                reportedChange = new OrchestratorKeyValue("STATUS_TERMINATED_DUE_TO_BAD_LAUNCH", "xxxx");
                break;
            case (STATUS_TERMINATED_DUE_TO_BAD_KV_PARSING):
                reportedChange = new OrchestratorKeyValue("STATUS_TERMINATED_DUE_TO_BAD_KV_PARSING",
                        "Agent couldn't parse correctly it's arguments by the Consul KV module");
                break;
            case (ERROR_WRONG_KV_CONFIGURATION):
                reportedChange = new OrchestratorKeyValue("ERROR_WRONG_KV_CONFIGURATION", "Wrong KV configuration in Consul Registry by orchestrator");
                break;
            case (ERROR_NO_NODE_ENTRY):
                reportedChange = new OrchestratorKeyValue("ERROR_NO_NODE_ENTRY", "This VM cannot be found in Consul Registry");
                break;
            case (ERROR_COMPONENT_SERVICE_DOWN):
                reportedChange = new OrchestratorKeyValue("ERROR_COMPONENT_SERVICE_DOWN", "This component is not working");
                break;
            case (ERROR_COMPONENT_VM_DOWN):
                reportedChange = new OrchestratorKeyValue("ERROR_COMPONENT_VM_DOWN", "The VM of this component is not working");
                break;
            case (ERROR_COULD_NOT_ATTACH_PUBLIC_IP):
                reportedChange = new OrchestratorKeyValue("ERROR_COULD_NOT_ATTACH_PUBLIC_IP", "Could not attach public IP for the access interface");
                break;
            default:
                reportedChange = new OrchestratorKeyValue("UNDEFINED", "Undefined status: " + status);
        }

        return reportedChange;
    }

    /*
     * Calculate graph used resources
     */
    private void calculateGraphResources() {
        Integer storage = 0;
        Integer vcpus = 0;
        Integer memory = 0;

        for (OrchestratorComponentNodeInstance service : composeObject.getServices()) {
            storage += service.getFlavor().getStorage();
            vcpus += service.getFlavor().getvCPUs();
            memory += service.getFlavor().getRam();
        }

        OrchestratorApplicationInstanceUsedResources usedResources = new OrchestratorApplicationInstanceUsedResources();

        usedResources.setStorage(storage);
        usedResources.setvCPUs(vcpus);
        usedResources.setRam(memory);

        OrchestratorChangedStatusNotification graphStatus = new OrchestratorChangedStatusNotification();

        graphStatus.setGraphID(composeObject.getGraphID());
        graphStatus.setGraphHexID(composeObject.getGraphHexID());
        graphStatus.setGraphName(composeObject.getGraphName());
        graphStatus.setGraphInstanceID(composeObject.getGraphInstanceID());
        graphStatus.setGraphInstanceHexID(composeObject.getGraphInstanceHexID());
        graphStatus.setGraphInstanceName(composeObject.getGraphInstanceName());
        graphStatus.setChangeType(OrchestratorChangedStatusNotification.ChangeType.QuotasChange.name());

        graphStatus.setvCPUs(vcpus.toString());
        graphStatus.setMemory(memory.toString());
        graphStatus.setStorage(storage.toString());

        logger.info("The graph: " + graphStatus.getGraphName() + "_" + graphStatus.getGraphInstanceName() + " is in status: " + graphStatus.getChangeType());

        logger.info("Resources for graph: " + graphStatus.getGraphName() + "_" + graphStatus.getGraphInstanceName() + " vCpus: " + vcpus.toString() + " Ram: "
                + memory.toString() + " Storage: " + storage.toString());

        sendReportingStatus(graphStatus, "0");
    }

    /**
     * Un/Deployment Graph Methods.
     */

    /*
     * Construct the deploy requests for the  Virtualization manager
     * and send them
     */
    public Boolean deployGraph(OrchestratorApplicationInstance composeObject, Boolean initial) {
        ExecutorService executor = Executors.newWorkStealingPool();
        List<Callable<Boolean>> connectioncallables = new ArrayList<>();

        /*
         *   Create a hashMap in order to have in a variable
         *   the match between provider details list and provider ID
         */
        Map<String, OrchestratorProviderAuthenticationDetails> metadata = new HashMap<>();
        composeObject.getProviderAuthenticationDetails().stream().forEach(authDetails -> {
            if (!metadata.containsKey(authDetails.getId())) {
                metadata.put(authDetails.getId(), authDetails);
            }
        });

        String graphHexId = composeObject.getGraphHexID();
        String graphInstanceHexId = composeObject.getGraphInstanceHexID();

        /*
         *   Iterate through service and deploy make the appropriate requests
         *   to the virtualization manager
         */
        for (OrchestratorComponentNodeInstance service : composeObject.getServices()) {
            String componentNodeHexId = service.getComponentNodeHexID();
            String componentNodeInstanceHexId = service.getComponentNodeInstanceHexID();
            String userData = "";

            /*
             *   Construct the initialization script that needed
             *   for the instantiation of the VM and its services
             */
            String fileInput = "";
            if (!metadata.get(service.getProviderID()).getAdapterType().startsWith("IOT_GATEWAY")) {

                try {
                    String currentDir = System.getProperty("user.dir");
                    File file;
                    if (this.ipv6Enabled) {
                        if (currentDir.contains("orchestrator")) {
                            file = new ClassPathResource("initUbuntuWorker_IPv6.sh").getFile();
                        } else {
                            file = new File(currentDir + "/initUbuntuWorker_IPv6.sh");
                        }
                        fileInput = OperationOnFiles.readFile(file.getAbsoluteFile().toString());
                        fileInput = fileInput.replaceAll("@MASTERIP", consulConfig.getIpv6());
                    } else {
                        if (currentDir.contains("orchestrator")) {
                            file = new ClassPathResource("initUbuntuWorker_IPv4.sh").getFile();
                        } else {
                            file = new File(currentDir + "/initUbuntuWorker_IPv4.sh");
                        }
                        fileInput = OperationOnFiles.readFile(file.getAbsoluteFile().toString());
                        fileInput = fileInput.replaceAll("@MASTERIP", consulConfig.getIpv6());
                    }

                    fileInput = fileInput.replaceAll("@GRAPHID", graphHexId);
                    fileInput = fileInput.replaceAll("@GRAPHINSTANCEID", graphInstanceHexId);
                    fileInput = fileInput.replaceAll("@COMPONENTNODEID", componentNodeHexId);
                    fileInput = fileInput.replaceAll("@COMPONENTNODEINSTANCEID", componentNodeInstanceHexId);

                    fileInput = fileInput.replaceAll("@CLUSTER_NAME", relayConfig.getCluster());

                    String netdataPrefix = graphHexId + ":" + graphInstanceHexId + ":" + componentNodeHexId;
                    NetdataConfiguration netdataConfiguration = MetricsConfigurator.createConfiguration(service.getPlugins(), netdataPrefix, service);
                    fileInput = fileInput.replaceAll("@NETDATA_CONFIG", netdataConfiguration.getNetdataConfig());

                    String pluginConfig = MetricsConfigurator.getCustomPluginConfiguration(netdataConfiguration);
                    fileInput = fileInput.replaceAll("@PLUGIN_CONFIG", pluginConfig);
                    fileInput = fileInput.replaceAll("@IDS_CONFIG", configureIds(service));

                    String agentFetchUrl = "sudo wget --header \"PRIVATE-TOKEN: " + generalConfig.getAgentToken() + "\" "
                            + generalConfig.getAgentUrl() + " -O /opt/agent.jar";

                    //String agentFetchUrl = "sudo wget " + agentConfig.getUrl() + " -O /opt/agent.jar";
                    fileInput = fileInput.replaceAll("@AGENT_FETCH_URL", agentFetchUrl);
                    fileInput = fileInput.replaceAll("@SSH_KEYS", configureSshKeys(service));
                    fileInput = fileInput.replaceAll("@WORKER_PASSWORD", configureWorkerPassword());
                    fileInput = fileInput.replaceAll("@CJDNS_SERVICE_INSTALL", configureCjdnsService());

                    if (this.ipv6Enabled) {
                        logger.info(
                                "Instantiation IPv6 script done for the component: " + graphHexId + "_" + graphInstanceHexId + "_" + componentNodeHexId + "_"
                                        + componentNodeInstanceHexId);
                    } else {
                        logger.info(
                                "Instantiation IPv4 script done for the component: " + graphHexId + "_" + graphInstanceHexId + "_" + componentNodeHexId + "_"
                                        + componentNodeInstanceHexId);
                    }
                } catch (IOException ex) {
                    //TODO make the appropriate callback to notify the backend
                    logger.error("Couldn't create the instantiation file for the component: " + graphHexId + "_" + graphInstanceHexId + "_" + componentNodeHexId
                            + "_" + componentNodeInstanceHexId);
                    //return new RestResponse(BasicResponseCode.EXCEPTION, "Couldn't create the instantiation file for the component: " + graphId + "_"
                    //      + graphInstanceId + "_" + componentNodeId + "_" + componentNodeInstanceId);
                    return false;
                }
            }
            /*
             *   Construct the appropriate request for the virtualization manager
             */
            OrchestratorInstance instance = new OrchestratorInstance();
            OrchestratorProviderAuthenticationDetails provider = metadata.get(service.getProviderID());

            instance.setName(graphHexId + "_" + graphInstanceHexId + "_" + componentNodeHexId + "_" + componentNodeInstanceHexId);
            List<String> networkList = new ArrayList<>();

            if (service.getDependsOn() != null && !service.getDependsOn().isEmpty()) {
                for (OrchestratorDependency dependency : service.getDependsOn()) {
                    if (dependency.getNetworkAttachmentPoint() != null && !networkList.contains(dependency.getNetworkAttachmentPoint())) {
                        networkList.add(dependency.getNetworkAttachmentPoint());
                    }
                }
            }

            if (service.getPorts() != null && !service.getPorts().isEmpty()) {
                for (OrchestratorPort port : service.getPorts()) {
                    if (port.getNetworkAttachmentPoint() != null && !networkList.contains(port.getNetworkAttachmentPoint())) {
                        networkList.add(port.getNetworkAttachmentPoint());
                    }
                }
            }

            if (networkList.size() < 2) {
                if (networkList.isEmpty()) {
                    networkList.add(provider.getNetworkID());
                }
                // if it has only one network, doesn't need to configure its networks
                fileInput = fileInput.replaceFirst("@NETWORK_CONFIGURATION", "");
            } else {
                // if it has more than one networks, we must put the extra sub-script
                String subString = getNetworkConfigurationScript();
                fileInput = fileInput.replaceFirst("@NETWORK_CONFIGURATION", subString);
            }

            instance.setNetworkIDList(networkList);

            OrchestratorFlavor flavor = new OrchestratorFlavor();
            flavor.setStorage(service.getFlavor().getStorage());
            flavor.setvCPUs(service.getFlavor().getvCPUs());
            flavor.setRam(service.getFlavor().getRam());
            if (service.getFlavor().getId() != null && !service.getFlavor().getId().isEmpty()) {
                instance.setImageID(provider.getImageID());

                // TODO setKeyPair need to be acquired by the service object
                flavor.setId(service.getFlavor().getId());

                fileInput = fileInput.replaceAll("@RELAYIP", relayConfig.getIpv4());
                fileInput = fileInput.replaceAll("@RELAYPORT", relayConfig.getPort());
                fileInput = fileInput.replaceAll("@RELAYLOGIN", relayConfig.getLogin());
                fileInput = fileInput.replaceAll("@RELAYPASSWORD", relayConfig.getPassword());
                fileInput = fileInput.replaceAll("@RELAYPUBLICKEY", relayConfig.getPublicKey());
                fileInput = fileInput.replaceAll("@RELAYPEERNAME", relayConfig.getPeerName());

                byte[] encodedBytes = Base64.getEncoder().encode(fileInput.getBytes());
                userData = new String(encodedBytes);

            } else {

                if (provider.getAdapterType().startsWith("OPENSTACK")) {
                    instance.setImageID(provider.getImageID());

                    //UBIDELL
                    //if (provider.getProject().equals("maestro")) {
                    // // TODO setKeyPair need to be acquired by the service object
                    //instance.setKeyPair(ubidellConfig.getKeyID());
                    //} else {
                    //logger.error("Couldn't find proper IDs for the provider: " + provider.getName());
                    //}

                    fileInput = fileInput.replaceAll("@RELAYIP", relayConfig.getIpv4());
                    fileInput = fileInput.replaceAll("@RELAYPORT", relayConfig.getPort());
                    fileInput = fileInput.replaceAll("@RELAYLOGIN", relayConfig.getLogin());
                    fileInput = fileInput.replaceAll("@RELAYPASSWORD", relayConfig.getPassword());
                    fileInput = fileInput.replaceAll("@RELAYPUBLICKEY", relayConfig.getPublicKey());
                    fileInput = fileInput.replaceAll("@RELAYPEERNAME", relayConfig.getPeerName());

                    byte[] encodedBytes = Base64.getEncoder().encode(fileInput.getBytes());
                    userData = new String(encodedBytes);

                } else if (provider.getAdapterType().startsWith("AWS")) {
                    //TODO this is a hack, doesn't suppose to happen like this
                    provider.setRegion(provider.getDomain());
                    instance.setImageID(provider.getImageID());
                    instance.setInstanceType(amazonConfig.getFlavorId());

                    byte[] encodedBytes = Base64.getEncoder().encode(fileInput.getBytes());
                    userData = new String(encodedBytes);

                } else if (provider.getAdapterType().startsWith("IOT_GATEWAY")) {

                    if (service.getDependsOn() != null && !service.getDependsOn().isEmpty()) {
                        userData = "true";
                    } else {
                        userData = "false";
                    }

                    if ((service.getLoadBalancer() != null && service.getLoadBalancer()) || (service.getLambdaProxy() != null && service.getLambdaProxy())) {
                        userData = userData + " " + "true";
                    } else {
                        userData = userData + " " + "false";
                    }

                    userData = userData + " " + "true";
                    userData = userData + " " + consulConfig.getIpv6();

                    //user data = flag lb ipv6 masterIPv6 masterIPv4 port login password publicKey peerName nexusIPv6
                    userData = userData + " " + relayConfig.getIpv4();
                    userData = userData + " " + relayConfig.getPort();
                    userData = userData + " " + relayConfig.getLogin();
                    userData = userData + " " + relayConfig.getPassword();
                    userData = userData + " " + relayConfig.getPublicKey();
                    userData = userData + " " + relayConfig.getPeerName();
                    //userData = userData + " " + provider.getNexusIP();
                }
            }

            instance.setUserData(userData);

            VirtulizationManagerRequest vimRequest = new VirtulizationManagerRequest();
            vimRequest.setAuthDetails(metadata.get(service.getProviderID()));
            vimRequest.setInstance(instance);
            vimRequest.setFlavor(flavor);

            logger.info("ComponentName: " + service.getComponentNodeName() + " Network list: " + vimRequest.getInstance().getNetworkIDList());
            /*
             *   Push the appropriate values to the key-value store of the Consul
             */
            agentParameterization(service);

            connectioncallables.add(() -> {
                //TODO the flavorID must be assigned from another service and get it in the composeObject
                if (vimRequest.getFlavor().getId() == null || vimRequest.getFlavor().getId().isEmpty()) {
                    String flavorId = this.getFlavorId(vimRequest.getAuthDetails(), vimRequest.getFlavor());
                    if (flavorId == null || flavorId.isEmpty()) {
                        return null;
                    }
                    vimRequest.getFlavor().setId(flavorId);
                }

                return this.spawnInstanceCall(vimRequest, service);
            });
        }

        if (initial) {
            if (this.hostnameMappingHashMap.isEmpty()) {
                consulClient.setKVValue(this.composeObject.getGraphHexID() + "/" + this.composeObject.getGraphInstanceHexID() + "/host/status",
                        HostsMappingStatus.PROCEED.getStatus() + "");
                this.hostnameMappingCounter = -1;
            } else {
                consulClient.setKVValue(this.composeObject.getGraphHexID() + "/" + this.composeObject.getGraphInstanceHexID() + "/host/status",
                        HostsMappingStatus.WAIT.getStatus() + "");
                this.hostnameMappingCounter = this.hostnameMappingHashMap.size();
            }
        }

        try {
            executor.invokeAll(connectioncallables).stream()
                    .map(future -> {
                        try {
                            return future.get();
                        } catch (Exception ex) {
                            throw new IllegalStateException(ex);
                        }
                    })
                    .forEach(System.out::println);
        } catch (InterruptedException ex) {
            //TODO make the appropriate callback to the backend
            logger.error("Couldn't invoke the Virtualization Manager spawnInstanceCall", ex);
            return false;
        }

        logger.debug("Deployment for graph: " + graphHexId + "_" + graphInstanceHexId + " started!");
        return true;
    }

    /*
     * Construct the undeploy requests for the  Virtualization manager
     * and send them
     * TODO check if the undeployment succeed or not
     */
    private Boolean undeployGraph(OrchestratorApplicationInstance composeObject) {
        ExecutorService executor = Executors.newWorkStealingPool();
        List<Callable<Boolean>> connectioncallables = new ArrayList<>();

        /*
         *   Create a hashMap in order to have in a variable
         *   the match between provider details list and provider ID
         */
        Map<String, OrchestratorProviderAuthenticationDetails> metadata = new HashMap<>();
        composeObject.getProviderAuthenticationDetails().stream().forEach(authDetails -> {
            if (!metadata.containsKey(authDetails.getId())) {
                metadata.put(authDetails.getId(), authDetails);
            }
        });

        String graphHexId = composeObject.getGraphHexID();
        String graphInstanceHexId = composeObject.getGraphInstanceHexID();

        /*
         *   Iterate through service and deploy make the appropriate requests
         *   to the virtualization manager
         */
        for (OrchestratorComponentNodeInstance service : composeObject.getServices()) {
            String componentNodeHexId = service.getComponentNodeHexID();
            String componentNodeInstanceHexId = service.getComponentNodeInstanceHexID();

            /*
             *   Construct the appropriate request for the virtualization manager
             */
            OrchestratorInstance instance = new OrchestratorInstance();
            instance.setName(graphHexId + "_" + graphInstanceHexId + "_" + componentNodeHexId + "_" + componentNodeInstanceHexId);

            ServiceStatus serviceStatus = hashMapServiceStatus.get(service.getComponentNodeHexID() + "_" + service.getComponentNodeInstanceHexID());

            if (!composeObject.getTelco5GEnabled()) {
                if (serviceStatus.getAccessInterface() && serviceStatus.getFloatingIP() != null && !serviceStatus.getFloatingIP().isEmpty()) {
                    Boolean detachCheck = detachFloatingIpCall(service, serviceStatus);
                    if (detachCheck) {
                        logger.info("The public IP of the service: " + graphHexId + ":" + graphInstanceHexId + ":" + componentNodeHexId + ":"
                                + componentNodeInstanceHexId + " detached successfully");
                    } else {
                        logger.warn("Could not detach the public IP of the service: " + graphHexId + ":" + graphInstanceHexId + ":" + componentNodeHexId + ":"
                                + componentNodeInstanceHexId);
                    }
                }
            }

            instance.setId(serviceStatus.getVmID());

            VirtulizationManagerRequest vimRequest = new VirtulizationManagerRequest();
            vimRequest.setAuthDetails(metadata.get(service.getProviderID()));
            vimRequest.setInstance(instance);

            connectioncallables.add(() ->
                    this.deleteInstanceCall(vimRequest)
            );
        }

        try {
            executor.invokeAll(connectioncallables).stream()
                    .map(future -> {
                        try {
                            return future.get();
                        } catch (Exception ex) {
                            throw new IllegalStateException(ex);
                        }
                    })
                    .forEach(System.out::println);
        } catch (InterruptedException ex) {
            //TODO make the appropriate callback to the backend
            logger.error("Couldn't invoke the Virtualization Manager deleteInstanceCall", ex);
            return false;
        }

        consulClient.deleteKVValues(graphHexId + "/" + graphInstanceHexId);
        return true;
    }

    /*
     * Make all the appropriate procedures in order to Scale Out a
     * component of the Graph
     */
    private Boolean scaleOut(TriggerActionModel triggerActionModel) {
        OrchestratorScalingTO scalingObject = new OrchestratorScalingTO(composeObject, composeStatusObject, triggerActionModel, backendConfig.getUrl(),
                backendConfig.getPort(), ConverterTo.consulConfigToConverter(consulConfig), null, metricCollector,
                elasticityDimensionId.get(triggerActionModel.getComponentNodeHexID()));

        //Find the controller and then the appropriate implementation
        OrchestratorComponentNodeInstance controller = findController(triggerActionModel.getComponentNodeHexID());
        if (controller == null) {
            logger.error("There is not a controller for the component with following identification: "
                    + triggerActionModel.getGraphHexID() + ":" + triggerActionModel.getComponentNodeHexID() + ":" + triggerActionModel.getComponentNodeHexID()
                    + " in order to Scale OUT!");
            return false;
        }
        ElasticityFrameworkOrchestrator elasticityAdapter = fetchFrameworkAdapterImpl(controller);

        ScaleOutTO scaleOutTo = elasticityAdapter.scaleOut(scalingObject);

        if (!scaleOutTo.getProceedWithScaling()) {
            logger.warn("Scaling deployment of the graph component: " + triggerActionModel.getGraphHexID() + "_" + triggerActionModel.getGraphInstanceHexID()
                    + triggerActionModel.getComponentNodeHexID());
            return false;
        }

        // //TODO add log metric to the UTIL
        //  metricCollector.logMetric(elasticityDimensionID.get(triggerActionModel.getComponentNodeHexID()), Elasticity.ScaleOutElasticity.getStatus());
        // //TODO add log metric to the util request from backend
        // metricCollector.logMetric(elasticityDimensionID.get(triggerActionModel.getComponentNodeHexID()), Elasticity.ScaleOutBackendReply.getStatus());

        increaseTotalWorkersMetric(scaleOutTo.getServiceToScale().getComponentNodeHexID(), scaleOutTo.getWorkersToRequest());

        //Construct Scale Out Compose Object from OrchestratorApplicationInstance
        //and update the compose object
        OrchestratorApplicationInstance scalingComposeObject = scaleOutTo.getScalingComposeObject();
        composeObject.getServices().addAll(scalingComposeObject.getServices());

        //Get serviceStatusList from the scalingComposeObject and update the composeStatusObject
        List<ServiceStatus> serviceStatusList = getServiceStatusList(scalingComposeObject.getServices());
        composeStatusObject.getServiceStatusList().addAll(serviceStatusList);

        String graphHexId = composeObject.getGraphHexID();
        String graphInstanceHexId = composeObject.getGraphInstanceHexID();
        saveState(graphHexId, graphInstanceHexId);

        //Update the hashMapServiceStatus
        updateHashMaps();

        Boolean check = deployGraph(scalingComposeObject, false);
        if (!check) {
            logger.warn("Scaling deployment of the graph component: " + triggerActionModel.getGraphHexID() + "_" + triggerActionModel.getGraphInstanceHexID()
                    + triggerActionModel.getComponentNodeHexID());
            return false;
        }
        calculateGraphResources();
        metricCollector.logMetric(elasticityDimensionId.get(triggerActionModel.getComponentNodeHexID()), Elasticity.ScaleOutOrchestrator.getStatus());

        return true;
    }

    /*
     * Make all the appropriate procedures in order to Scale In a
     * component of the Graph
     */
    private Boolean scaleIn(TriggerActionModel triggerActionModel) {
        OrchestratorScalingTO scalingObject = new OrchestratorScalingTO(composeObject, composeStatusObject, triggerActionModel, backendConfig.getUrl(),
                backendConfig.getPort(), ConverterTo.consulConfigToConverter(consulConfig), null, metricCollector,
                elasticityDimensionId.get(triggerActionModel.getComponentNodeHexID()));

        //Find the controller and then the appropriate implementation
        OrchestratorComponentNodeInstance controller = findController(triggerActionModel.getComponentNodeHexID());
        if (controller == null) {
            logger.error("There is not a controller for the component with following identification: " + triggerActionModel.getGraphHexID() + ":"
                    + triggerActionModel.getComponentNodeHexID() + ":" + triggerActionModel.getComponentNodeHexID() + " in order to Scale IN!");
            return false;
        }

        ElasticityFrameworkOrchestrator elasticityAdapter = fetchFrameworkAdapterImpl(controller);
        ScaleInTO scaleInTo = elasticityAdapter.findWorkersForRemoval(scalingObject);
        if (scaleInTo.getQualifiedForRemoval() == null || scaleInTo.getQualifiedForRemoval().isEmpty()) {
            return false;
        }

        decreaseTotalWorkersMetric(triggerActionModel.getComponentNodeHexID(), scaleInTo.getWorkersToRemove());

        scaleInTo = elasticityAdapter.removeWorkersFromController(scaleInTo, scalingObject);

        List<ServiceStatus> qualifiedForRemoval = scaleInTo.getQualifiedForRemoval();
        String graphHexId = scalingObject.getComposeObject().getGraphHexID();
        String graphInstanceHexId = scalingObject.getComposeObject().getGraphInstanceHexID();

        Map<String, OrchestratorProviderAuthenticationDetails> metadata = new HashMap<>();
        composeObject.getProviderAuthenticationDetails().stream().forEach(authDetails -> {
            if (!metadata.containsKey(authDetails.getId())) {
                metadata.put(authDetails.getId(), authDetails);
            }
        });

        //Construct all the delete calls to the virtualization manager
        ExecutorService executor = Executors.newWorkStealingPool();
        List<Callable<Boolean>> connectioncallables = new ArrayList<>();
        List<OrchestratorComponentNodeInstance> qualifiedServericesForRemoval = new ArrayList<>();

        metricCollector.logMetric(elasticityDimensionId.get(triggerActionModel.getComponentNodeHexID()), Elasticity.ScaleInPreElasticity.getStatus());

        for (ServiceStatus serviceStatus : qualifiedForRemoval) {
            String componentNodeId = serviceStatus.getComponentNodeHexID();
            String componentNodeInstanceId = serviceStatus.getComponentNodeInstanceHexID();

            /*
             *   Construct the appropriate request for the virtualization manager
             */
            OrchestratorInstance instance = new OrchestratorInstance();
            instance.setName(graphHexId + "_" + graphInstanceHexId + "_" + componentNodeId + "_" + componentNodeInstanceId);
            instance.setId(serviceStatus.getVmID());

            OrchestratorComponentNodeInstance service = hashMapService
                    .get(serviceStatus.getComponentNodeHexID() + "_" + serviceStatus.getComponentNodeInstanceHexID());
            qualifiedServericesForRemoval.add(service);

            VirtulizationManagerRequest vimRequest = new VirtulizationManagerRequest();
            vimRequest.setAuthDetails(metadata.get(service.getProviderID()));
            vimRequest.setInstance(instance);

            //Construct the Orchestrator Scaling Request for the backend Microservice
            OrchestratorScalingRequest scalingRequest = new OrchestratorScalingRequest();
            scalingRequest.setGraphID(composeObject.getGraphID());
            scalingRequest.setGraphName(composeObject.getGraphName());
            scalingRequest.setGraphHexID(composeObject.getGraphHexID());
            scalingRequest.setGraphInstanceID(composeObject.getGraphInstanceID());
            scalingRequest.setGraphInstanceHexID(composeObject.getGraphInstanceHexID());
            scalingRequest.setGraphInstanceName(composeObject.getGraphInstanceName());

            scalingRequest.setComponentNodeID(serviceStatus.getComponentNodeID());
            scalingRequest.setComponentNodeHexID(serviceStatus.getComponentNodeHexID());
            scalingRequest.setComponentNodeName(serviceStatus.getComponentNodeName());
            scalingRequest.setComponentNodeInstanceID(serviceStatus.getComponentNodeInstanceID());
            scalingRequest.setComponentNodeInstanceHexID(serviceStatus.getComponentNodeInstanceHexID());
            scalingRequest.setComponentNodeInstanceName(serviceStatus.getComponentNodeInstanceName());

            connectioncallables.add(() -> {
                Boolean check = this.deleteInstanceCall(vimRequest);
                if (check == true) {

                    String scaleInUri = "http://" + backendConfig.getUrl() + ":" + backendConfig.getPort() + "/api/v1/scaling/down";
                    try {
                        RestTemplate restTemplate = new RestTemplate();
                        HttpEntity entity = new HttpEntity(scalingRequest);
                        ResponseEntity<String> responseEntity =
                                restTemplate.exchange(scaleInUri, HttpMethod.POST, entity, String.class);

                        decreaseActiveWorkersMetric(triggerActionModel.getComponentNodeHexID(), 1);

                    } catch (RestClientException ex) {
                        logger.warn("Backend call error in scale in action: " + ex.getMessage());
                    }
                    //TODO we must somehow check if the backend did the removal of the component

                    return true;
                } else {
                    return false;
                }
            });
        }

        try {
            executor.invokeAll(connectioncallables).stream()
                    .map(future -> {
                        try {
                            return future.get();
                        } catch (Exception ex) {
                            throw new IllegalStateException(ex);
                        }
                    })
                    .forEach(System.out::println);
        } catch (InterruptedException exe) {
            //TODO make the appropriate callback to the backend
            logger.error("Couldn't invoke the Virtualization Manager deleteInstanceCall", exe.getMessage());
            return false;
        }
        metricCollector.logMetric(elasticityDimensionId.get(triggerActionModel.getComponentNodeHexID()), Elasticity.ScaleInOrchestrator.getStatus());

        //delete node consul status and clean HashMaps
        for (ServiceStatus serviceStatus : qualifiedForRemoval) {
            String commponentNodeHexId = serviceStatus.getComponentNodeHexID();
            String componentNodeInstanceHexId = serviceStatus.getComponentNodeInstanceHexID();
            String path = graphHexId + "/" + graphInstanceHexId + "/" + commponentNodeHexId + "/" + componentNodeInstanceHexId + "/";
            consulClient.deleteKVValues(path);

            OrchestratorComponentNodeInstance componentNodeInstance = hashMapService
                    .get(serviceStatus.getComponentNodeHexID() + "_" + serviceStatus.getComponentNodeInstanceHexID());
            if (componentNodeInstance.getStatusSoc() != null && componentNodeInstance.getStatusSoc()) {
                removeWazuhAgent(findWazuhAgentId(graphHexId, graphInstanceHexId, componentNodeInstance.getComponentNodeHexID(),
                        componentNodeInstance.getComponentNodeInstanceHexID()));
            }

            hashMapServiceStatus.remove(serviceStatus.getComponentNodeHexID() + "_" + serviceStatus.getComponentNodeInstanceHexID());
            hashMapService.remove(serviceStatus.getComponentNodeHexID() + "_" + serviceStatus.getComponentNodeInstanceHexID());
            hashMapServerName.remove(serviceStatus.getNodeName());
        }

        Object controllerMetadata = controller.getControllerMetadata();
        controllerMetadata = elasticityAdapter.postScaleInControllerMetadataClean(scaleInTo, controllerMetadata);
        if (controllerMetadata != null) {
            controller.setControllerMetadata(controllerMetadata);
        }

        metricCollector.logMetric(elasticityDimensionId.get(triggerActionModel.getComponentNodeHexID()), Elasticity.ScaleInPostElasticity.getStatus());

        //Clean up the metadata and save the new state
        composeObject.getServices().removeAll(qualifiedServericesForRemoval);
        composeStatusObject.getServiceStatusList().removeAll(qualifiedForRemoval);
        saveState(graphHexId, graphInstanceHexId);

        calculateGraphResources();
        return true;
    }

    private ElasticityFrameworkOrchestrator fetchFrameworkAdapterImpl(OrchestratorComponentNodeInstance component) {
        ElasticityFrameworkOrchestrator elasticityAdapter = null;

        if (component.getController()) {
            elasticityAdapter = (ElasticityFrameworkOrchestrator) ((List) elasticityFrameworkAdapters.stream()
                    .filter(adapter -> adapter.getClass().getName().equals(component.getElasticityControllerAdapterImplementation()))
                    .collect(Collectors.toList())).get(0);
        }

        return elasticityAdapter;
    }

    private OrchestratorComponentNodeInstance findController(String componentNodeHexId) {
        OrchestratorComponentNodeInstance controller = null;
        //TODO maybe in the future I need to find the controller in a more proper way.
        //Possible solution to just have in the controller metadata all the components that it manages

        for (OrchestratorComponentNodeInstance component : composeObject.getServices()) {
            //Check if this component is controller and if so check if it has dependency on the scalable component
            if (null != component.getController() && component.getController() && null != component.getDependsOn() && !component.getDependsOn().isEmpty()) {
                for (OrchestratorDependency dependency : component.getDependsOn()) {
                    if (dependency.getDependency().equals(componentNodeHexId)) {
                        controller = component;
                        break;
                    }
                }
            }
        }
        return controller;
    }

    /*
     * Constructs the Compose Status Object extracted from the Compose Object
     */
    private ComposeStatusObject getComposeStatusObject(OrchestratorApplicationInstance object) {
        ComposeStatusObject composeStatusObject = new ComposeStatusObject();
        composeStatusObject.setGraphID(object.getGraphID());
        composeStatusObject.setGraphHexID(object.getGraphHexID());
        composeStatusObject.setGraphName(object.getGraphName());
        composeStatusObject.setGraphInstanceID(object.getGraphInstanceID());
        composeStatusObject.setGraphInstanceHexID(object.getGraphInstanceHexID());
        composeStatusObject.setGraphInstanceName(object.getGraphInstanceName());

        composeStatusObject.setFirstInfo(true);
        composeStatusObject.setReportedChange("DEPLOYING");
        composeStatusObject.setStatus(STATUS_DEPLOYING);

        List<ServiceStatus> serviceStatusList = getServiceStatusList(object.getServices());
        composeStatusObject.setServiceStatusList(serviceStatusList);

        return composeStatusObject;
    }

    /*
     * Get a List of ServiceStatus from a List with Services
     */
    private List<ServiceStatus> getServiceStatusList(List<OrchestratorComponentNodeInstance> serviceList) {
        List<ServiceStatus> serviceStatusList = new ArrayList<>();

        for (OrchestratorComponentNodeInstance service : serviceList) {
            ServiceStatus serviceStatus = new ServiceStatus();
            serviceStatus.setComponentNodeID(service.getComponentNodeID());
            serviceStatus.setComponentNodeHexID(service.getComponentNodeHexID());
            serviceStatus.setComponentNodeName(service.getComponentNodeName());
            serviceStatus.setComponentNodeInstanceID(service.getComponentNodeInstanceID());
            serviceStatus.setComponentNodeInstanceHexID(service.getComponentNodeInstanceHexID());
            serviceStatus.setComponentNodeInstanceName(service.getComponentNodeInstanceName());

            serviceStatus.setFirstInfo(true);
            //TODO redefine if the initialization of the status is needed
            serviceStatus.setStatus(SPAWNING);

            String graphHexId = composeObject.getGraphHexID();
            String graphInstanceHexId = composeObject.getGraphInstanceHexID();
            String componentNodeHexId = service.getComponentNodeHexID();
            String componentNodeInstanceHexId = service.getComponentNodeInstanceHexID();
            String nodeName = graphHexId + "-" + graphInstanceHexId + "-" + componentNodeHexId + "-" + componentNodeInstanceHexId;
            serviceStatus.setNodeName(nodeName);
            serviceStatus.setFloatingIP("");
            serviceStatus.setIpList(null);
            serviceStatus.setHealthCheckFirstDate((long) 0);
            Long interval = Long.valueOf(service.getHealthCheck().getInterval());
            serviceStatus.setHealthCheckTimeLimit(interval * HEALTH_CHECK_RETRIES);

            serviceStatus.setAccessInterface(false);
            if (service.getPorts() != null) {
                for (OrchestratorPort port : service.getPorts()) {
                    if (port.getType().equals(OrchestratorPort.InterfaceType.ACCESS.name())) {
                        serviceStatus.setAccessInterface(true);
                        break;
                    }
                }
            }

            if (null != service.getController() && service.getController()) {
                ElasticityFrameworkOrchestrator elasticityAdapter = fetchFrameworkAdapterImpl(service);
                Object controllerMetadata = elasticityAdapter.initControllerMetadata(serviceList);
                service.setControllerMetadata(controllerMetadata);
            }

            //TODO make the same enum to the MonitoringElasticity object too
            serviceStatus.setScalability(ServiceStatus.ScalabilityType.valueOf(service.getMonitoringElasticity().getProfile()));
            serviceStatus.setBalancedByComponentNodeHexID(service.getBalancedByComponentNodeHexID());

            serviceStatusList.add(serviceStatus);
        }

        return serviceStatusList;
    }

    /*
     * Constructs the appropriate object and pushes them in the Consul KV store
     */
    private void agentParameterization(OrchestratorComponentNodeInstance service) {
        String graphHexId = composeObject.getGraphHexID();
        String graphInstanceHexId = composeObject.getGraphInstanceHexID();
        String componentNodeHexId = service.getComponentNodeHexID();
        String componentNodeInstanceHexId = service.getComponentNodeInstanceHexID();
        String euProject = generalConfig.getEuProject();

        // Construct Agent Parameters json
        AgentParameters agentParameters = new AgentParameters();

        if (euProject != null) {
            agentParameters.setEuProject(euProject);
        }

        if (service.getStatusIDS() != null && service.getStatusIDS() == true) {
            agentParameters.setHasEnableIds(service.getStatusIDS());
        } else {
            agentParameters.setHasEnableIds(false);
        }
        if (service.getStatusIPS() != null && service.getStatusIPS() == true) {
            agentParameters.setHasEnableIps(service.getStatusIPS());
        } else {
            agentParameters.setHasEnableIps(false);
        }

        agentParameters.setHasEnableIpv6(ipv6Enabled);

        // Check if it has dependencies
        if (service.getDependsOn() != null && !service.getDependsOn().isEmpty()) {
            agentParameters.setHasDependencies(true);
        } else {
            agentParameters.setHasDependencies(false);
        }

        // Check if it is Load Balancer
        if ((service.getLoadBalancer() != null && service.getLoadBalancer() == true) || (service.getLambdaProxy() != null
                && service.getLambdaProxy() == true)) {
            agentParameters.setLoadBalancer(true);
        } else {
            agentParameters.setLoadBalancer(false);
        }

        // Check if it has Access Interface
        agentParameters.setHasEnablePublicIpV4(false);
        if (!composeObject.getTelco5GEnabled()) {
            if (null != service.getPorts() && !service.getPorts().isEmpty()) {
                for (OrchestratorPort port : service.getPorts()) {
                    if (port.getType().equals(OrchestratorPort.InterfaceType.ACCESS.name())) {
                        agentParameters.setHasEnablePublicIpV4(true);
                        break;
                    }
                }
            }
        }
        // Add nfs server IP and prometheus url
        agentParameters.setNfsServerHost(this.generalConfig.getNfsServerIp());
        agentParameters.setPrometheusHost(this.generalConfig.getPrometheusUrl());

        if (service.getHasEnableSecurity() != null) {
            agentParameters.setHasEnableSecurity(service.getHasEnableSecurity());
        } else {
            agentParameters.setHasEnableSecurity(false);
        }

        if (service.getStatusSoc() != null) {
            agentParameters.setHasEnableSoc(service.getStatusSoc());
            if (service.getStatusSoc()) {
                agentParameters.setSocManagerIp(generalConfig.getSocManagerPrivateIp());
                agentParameters.setSocAgentUrl(generalConfig.getSocAgentUrl());
            }
        } else {
            agentParameters.setHasEnableSoc(false);
        }

        agentParameters.setSocAuditdEnabled(generalConfig.getSocAuditdEnabled());

        String agentParametersJson = "";
        try {
            agentParametersJson = objectMapper.writeValueAsString(agentParameters);
        } catch (JsonProcessingException ex) {
            logger.error("Couldn't cast agentParameters to json " + ex.getMessage());
        }

        // Construct KafkaConfiguration
        KafkaConfiguration kafkaConfiguration = new KafkaConfiguration();
        String kafkaUrl = kafkaConfig.getUrl() + ":" + kafkaConfig.getPort();
        kafkaConfiguration.setKafkaServer(composeObject.getOrchestratorKafkaConfig().getKafkaServer());
        kafkaConfiguration.setTopicNameIps(composeObject.getOrchestratorKafkaConfig().getAgentIpsConfigTopic());
        kafkaConfiguration.setTopicNameIds(composeObject.getOrchestratorKafkaConfig().getAgentIdsConfigTopic());
        kafkaConfiguration.setTopicNameIdsAlert(composeObject.getOrchestratorKafkaConfig().getAgentIdsAlertTopic());
        kafkaConfiguration.setSocConfigTopic(composeObject.getOrchestratorKafkaConfig().getAgentSocConfigTopic());
        kafkaConfiguration.setSecurityConfigTopic(composeObject.getOrchestratorKafkaConfig().getAgentSecurityConfigTopic());
        kafkaConfiguration.setSecurityConfigResultTopic(composeObject.getOrchestratorKafkaConfig().getAgentSecurityConfigResultTopic());

        //TODO Check if needed
        kafkaConfiguration.setKafkaServerSoc(kafkaConfig.getSocUrl());

        String kafkaConfigurationJson = "";
        try {
            kafkaConfigurationJson = objectMapper.writeValueAsString(kafkaConfiguration);
        } catch (JsonProcessingException ex) {
            logger.error("Couldn't cast kafkaConfiguration to json " + ex.getMessage());
        }

        // Construct IntrusionDetectionParameters
        String intrusionDetectionParametersJson = "";
        if (agentParameters.isHasEnableIds()) {
            IntrusionDetectionParameters intrusionDetectionParameters = new IntrusionDetectionParameters();
            intrusionDetectionParameters.setUserName(idsConfig.getUsername());
            intrusionDetectionParameters.setPassword(idsConfig.getPassword());
            intrusionDetectionParameters.setUrl(idsConfig.getRegistry());
            intrusionDetectionParameters.setIdsDockerImage(idsConfig.getImage());

            List<String> capabilities = new ArrayList<>();
            capabilities.add("--privileged");
            capabilities.add("--cap-add=NET_RAW");
            capabilities.add("--network=host");
            intrusionDetectionParameters.setCapabilities(capabilities);

            List<String> volumes = new ArrayList<>();
            volumes.add("/opt/snort/config:/var/lib/snort");
            volumes.add("/opt/snort/logs:/var/log/snort");
            intrusionDetectionParameters.setVolumes(volumes);

            intrusionDetectionParameters.setAlertPath("/opt/snort/logs/alert");
            intrusionDetectionParameters.setRuleSetPath("/opt/snort/config/rules/local.rules");

            try {
                intrusionDetectionParametersJson = objectMapper.writeValueAsString(intrusionDetectionParameters);
            } catch (JsonProcessingException ex) {
                logger.error("Couldn't cast intrusionDetectionParameters to json " + ex.getMessage());
            }
        }

        // Construct Component Image json
        ComponentImage componentImage = new ComponentImage();
        // TODO must get registry username and password from the frontend
        componentImage.setUserName(service.getDockerUsername());
        componentImage.setPassword(service.getDockerPassword());
        componentImage.setUrl(service.getRegistry());
        componentImage.setServiceImage(service.getImage());
        String componentImageJson = "";
        try {
            componentImageJson = objectMapper.writeValueAsString(componentImage);
        } catch (JsonProcessingException ex) {
            logger.error("Couldn't cast componentImage to json " + ex.getMessage());
        }

        // Construct Arguments json
        ArrayList<String> environment = new ArrayList<>();
        if (service.getEnvironmentalVariables() != null && !service.getEnvironmentalVariables().isEmpty()) {
            for (Object key : service.getEnvironmentalVariables().keySet()) {
                if (service.getEnvironmentalVariables().get(key).contains(" ")) {
                    environment.add(key.toString() + "=\'" + service.getEnvironmentalVariables().get(key) + "\'");
                } else {
                    environment.add(key.toString() + "=" + service.getEnvironmentalVariables().get(key));
                }
            }
        }

        ArrayList<String> ports = new ArrayList<>();
        if (service.getPorts() != null && !service.getPorts().isEmpty()) {
            service.getPorts().stream().forEach(x -> {
                if (x.getProtocol() == null || x.getProtocol().equals("BOTH")) {
                    ports.add(x.getPublished() + ":" + x.getTarget() + "/tcp");
                    ports.add(x.getPublished() + ":" + x.getTarget() + "/udp");
                } else if (x.getProtocol().equals("TCP")) {
                    ports.add(x.getPublished() + ":" + x.getTarget() + "/tcp");
                } else if (x.getProtocol().equals("UDP")) {
                    ports.add(x.getPublished() + ":" + x.getTarget() + "/udp");
                }
            });
        }

        ArrayList<String> commands = new ArrayList<>();
        if (service.getCommand() != null && !service.getCommand().isEmpty()) {
            service.getCommand().stream().forEach(x -> commands.add(x));
        }
        ArrayList<String> devices = new ArrayList<>();
        if (service.getDevices() != null && !service.getDevices().isEmpty()) {
            List<String> deviceList = new ArrayList<>(service.getDevices().values());
            if (deviceList != null && deviceList.size() > 0) {
                for (String dev : deviceList) {
                    devices.add(dev);
                }
            }
        }
        Arguments arguments = new Arguments();
        arguments.setEnvs(environment);
        arguments.setPorts(ports);
        arguments.setCommands(commands);
        arguments.setDevice(devices);

        arguments.setVolumes(service.getVolumes());

        arguments.setNetworkModeHost(null != service.getNetworkModeHost() ? service.getNetworkModeHost().booleanValue() : false);
        arguments.setPrivileged(null != service.getPrivilege() ? service.getPrivilege().booleanValue() : false);
        arguments.setHostname(null != service.getHostname() && !service.getHostname().isEmpty() ? service.getHostname() : null);
        arguments.setSharedMemorySize(null != service.getSharedMemorySize() && !service.getSharedMemorySize().isEmpty() ? service.getSharedMemorySize() : null);
        arguments.setCapabilityAdds(
                null != service.getCapabilityAdds() && !service.getCapabilityAdds().isEmpty() ? (List<String>) service.getCapabilityAdds() : new ArrayList<>());
        arguments.setCapabilityDrops(
                null != service.getCapabilityDrops() && !service.getCapabilityDrops().isEmpty() ? (List<String>) service.getCapabilityDrops()
                        : new ArrayList<>());
        arguments.setUser(null != service.getDockerExecutionUser() && !service.getDockerExecutionUser().isEmpty() ? service.getDockerExecutionUser() : null);

        if (service.getHostname() != null && !service.getHostname().isEmpty()) {
            HostnameMapping hostnameMapping = new HostnameMapping(service.getHostname());
            this.hostnameMappingHashMap.put(service.getComponentNodeHexID(), hostnameMapping);
        }

        String ulimitMemlock = "";
        ulimitMemlock = null != service.getUlimitMemlockSoft() && !service.getUlimitMemlockSoft().isEmpty() ? service.getUlimitMemlockSoft() + ":" : "";
        ulimitMemlock =
                null != service.getUlimitMemlockHard() && !service.getUlimitMemlockHard().isEmpty() ? ulimitMemlock + service.getUlimitMemlockHard() : "";
        arguments.setUlimitMemlock(null != ulimitMemlock && !ulimitMemlock.isEmpty() ? ulimitMemlock : null);

        String argumentsJson = "";
        try {
            argumentsJson = objectMapper.writeValueAsString(arguments);
        } catch (JsonProcessingException ex) {
            logger.error("Couldn't cast arguments to json " + ex.getMessage());
        }

        // Construct Dependencies json
        ArrayList<String> componentList = new ArrayList<>();
        if (service.getDependsOn() != null && !service.getDependsOn().isEmpty()) {
            service.getDependsOn().stream().forEach(x -> componentList.add(x.getDependency()));
        }
        Dependencies dependencies = new Dependencies();
        dependencies.setDependsOnComponent(componentList);
        String dependenciesJson = "";
        try {
            dependenciesJson = objectMapper.writeValueAsString(dependencies);
        } catch (JsonProcessingException ex) {
            logger.error("Couldn't cast dependencies to json " + ex.getMessage());
        }

        // Construct HealthCheck json
        eu.orchestator.core.model.agent.HealthCheck healthCheck = new eu.orchestator.core.model.agent.HealthCheck();
        healthCheck.setInterval(service.getHealthCheck().getInterval());
        // check if it has arguments
        if (service.getHealthCheck().getArgs() == null || service.getHealthCheck().getArgs().isEmpty()) {
            // if so, it must have http health check
            healthCheck.setHttpEndpoint(service.getHealthCheck().getHttpURL());
            healthCheck.setArgs("");
        } else {
            // else it has args based health check
            healthCheck.setHttpEndpoint("");
            healthCheck.setArgs(service.getHealthCheck().getArgs());
        }
        String healthCheckJson = "";
        try {
            healthCheckJson = objectMapper.writeValueAsString(healthCheck);
        } catch (JsonProcessingException ex) {
            logger.error("Couldn't cast healthCheck to json " + ex.getMessage());
        }

        int status = 1;

        //Check if it is loadBalancer and add the extra lbStatus
        if ((service.getLoadBalancer() != null && service.getLoadBalancer() == true) || (service.getLambdaProxy() != null
                && service.getLambdaProxy() == true)) {
            consulClient
                    .setKVValue(graphHexId + "/" + graphInstanceHexId + "/" + componentNodeHexId + "/" + componentNodeInstanceHexId + "/" + "lbStatus", "0");
        }
        consulClient.setKVValue(graphHexId + "/" + graphInstanceHexId + "/" + componentNodeHexId + "/" + componentNodeInstanceHexId + "/" + "status",
                "" + status + "");
        consulClient.setKVValue(graphHexId + "/" + graphInstanceHexId + "/" + componentNodeHexId + "/configurations/agentParameters", agentParametersJson);
        consulClient
                .setKVValue(graphHexId + "/" + graphInstanceHexId + "/" + componentNodeHexId + "/configurations/kafkaConfiguration", kafkaConfigurationJson);
        consulClient.setKVValue(graphHexId + "/" + graphInstanceHexId + "/" + componentNodeHexId + "/configurations/componentImage", componentImageJson);
        consulClient.setKVValue(graphHexId + "/" + graphInstanceHexId + "/" + componentNodeHexId + "/configurations/arguments", argumentsJson);
        consulClient.setKVValue(graphHexId + "/" + graphInstanceHexId + "/" + componentNodeHexId + "/configurations/dependencies", dependenciesJson);
        consulClient.setKVValue(graphHexId + "/" + graphInstanceHexId + "/" + componentNodeHexId + "/configurations/healthcheck", healthCheckJson);
        if (agentParameters.isHasEnableIds()) {
            consulClient.setKVValue(graphHexId + "/" + graphInstanceHexId + "/" + componentNodeHexId + "/configurations/intrusionDetectionParameters",
                    intrusionDetectionParametersJson);
        }
    }

    /*
     * Construct a compose object from a OrchestratorApplicationInstance object
     */
    private String configureIds(OrchestratorComponentNodeInstance service) {
        String initIdsScript = "";
        if (service.getStatusIDS() != null && service.getStatusIDS() == true) {
            initIdsScript = "sudo mkdir -p /opt/snort/logs\n";
            initIdsScript += "sudo mkdir -p /opt/snort/config\n";
            initIdsScript +=
                    "sudo wget --user " + idsConfig.getConfigUsername() + " --password \'" + idsConfig.getConfigPassword() + "\' " + idsConfig.getConfigUrl()
                            + " -P /opt/snort/\n";
            initIdsScript += "sudo tar -zxf /opt/snort/config.tar.gz --directory /opt/snort\n";
            initIdsScript += "sudo chmod 777 -R /opt/snort\n";

        }
        return initIdsScript;
    }

    private String configureSshKeys(OrchestratorComponentNodeInstance service) {
        String initSshKeyScript = "";
        if (null == service.getSshKey() || service.getSshKey().isEmpty()) {
            return initSshKeyScript;
        }
        initSshKeyScript += "sudo mkdir -p /home/ubuntu/.ssh\n";
        initSshKeyScript += "sudo touch /home/ubuntu/.ssh/authorized_keys\n";
        initSshKeyScript += "sudo echo \"" + service.getSshKey() + "\"   >> /home/ubuntu/.ssh/authorized_keys";

        return initSshKeyScript;
    }

    /*
     * Update the Hash Maps
     */
    private void updateHashMaps() {
        if (hashMapServiceStatus == null) {
            hashMapServiceStatus = new HashMap<>();
        }

        if (hashMapService == null) {
            hashMapService = new HashMap<>();
        }

        if (hashMapServerName == null) {
            hashMapServerName = new HashMap<>();
        }

        composeStatusObject.getServiceStatusList().stream().forEach(serviceStatus -> {
            if (!this.hashMapServiceStatus.containsKey(serviceStatus.getComponentNodeHexID() + "_" + serviceStatus.getComponentNodeInstanceHexID())) {
                hashMapServiceStatus.put(serviceStatus.getComponentNodeHexID() + "_" + serviceStatus.getComponentNodeInstanceHexID(), serviceStatus);
            }
        });

        composeObject.getServices().stream().forEach(service -> {
            if (!this.hashMapService.containsKey(service.getComponentNodeHexID() + "_" + service.getComponentNodeInstanceHexID())) {
                hashMapService.put(service.getComponentNodeHexID() + "_" + service.getComponentNodeInstanceHexID(), service);
            }
        });

        composeStatusObject.getServiceStatusList().stream().forEach(serviceStatus -> {
            if (!this.hashMapServerName.containsKey(serviceStatus.getNodeName())) {
                OrchestratorComponentNodeInstance service = hashMapService
                        .get(serviceStatus.getComponentNodeHexID() + "_" + serviceStatus.getComponentNodeInstanceHexID());
                hashMapServerName.put(serviceStatus.getNodeName(), service);
            }
        });
    }

    /*
     * Get IP of them VM of a specific service
     */

    private String getServiceIp(ServiceStatus serviceStatus, IPType ipType) {
        String graphHexId = composeObject.getGraphHexID();
        String graphInstanceHexId = composeObject.getGraphInstanceHexID();
        String componentNodeHexId = serviceStatus.getComponentNodeHexID();
        String componentNodeInstanceHexId = serviceStatus.getComponentNodeInstanceHexID();

        String serviceIp = null;

        if (ipType.equals(IPType.IPv6)) {
            if (serviceStatus.getPrivateIPv6() != null && !serviceStatus.getPrivateIPv6().isEmpty()) {
                return serviceStatus.getPrivateIPv6();
            }

            String nodeName = graphHexId + "-" + graphInstanceHexId + "-" + componentNodeHexId + "-" + componentNodeInstanceHexId;
            Response<CatalogNode> response = consulClient.getCatalogNode(nodeName, QueryParams.DEFAULT);
            System.out.println(response);
            //Check if the response has any values, if it's emtpy
            // means that has no running service
            if (response == null || response.getValue() == null || response.getValue().getNode() == null || response.getValue().getNode().getAddress() == null
                    || response.getValue().getNode().getAddress().isEmpty()) {
                logger.error("Couldn't get a proper response from the consul NODES Service");
                return serviceIp;
            }

            serviceIp = response.getValue().getNode().getAddress();
            serviceStatus.setPrivateIPv6(serviceIp);

        } else if (ipType.equals(IPType.PublicIP)) {
            serviceIp = serviceStatus.getFloatingIP();

        } else if (ipType.equals(IPType.IPv4)) {

            if (serviceStatus.getPrivateIP() != null && !serviceStatus.getPrivateIP().isEmpty()) {
                return serviceStatus.getPrivateIP();
            }

            Map<String, OrchestratorProviderAuthenticationDetails> metadata = new HashMap<>();
            composeObject.getProviderAuthenticationDetails().stream().forEach(authDetails -> {
                if (!metadata.containsKey(authDetails.getId())) {
                    metadata.put(authDetails.getId(), authDetails);
                }
            });

            OrchestratorComponentNodeInstance service = hashMapService
                    .get(serviceStatus.getComponentNodeHexID() + "_" + serviceStatus.getComponentNodeInstanceHexID());
            OrchestratorProviderAuthenticationDetails authenticationDetails = metadata.get(service.getProviderID());

            OrchestratorInstance instance = new OrchestratorInstance();
            instance.setId(serviceStatus.getVmID());

            VirtulizationManagerRequest vimRequest = new VirtulizationManagerRequest();
            vimRequest.setAuthDetails(authenticationDetails);
            vimRequest.setInstance(instance);

            String singleInstanceUri =
                    "http://" + virtualizationManagerConfig.getUrl() + ":" + virtualizationManagerConfig.getPort() + "/api/v1/instance/get/instance";

            RestTemplate restTemplate = new RestTemplate();
            HttpEntity entity = new HttpEntity(vimRequest);
            ResponseEntity<String> responseEntity = restTemplate.exchange(singleInstanceUri, HttpMethod.POST, entity, String.class);

            if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {

                logger.debug("Response: " + responseEntity.getBody());
                try {
                    JSONObject callbackJson = new JSONObject(responseEntity.getBody());

                    if (callbackJson.getString("code").equals("SUCCESS")) {
                        JSONObject returnobject = (JSONObject) callbackJson.get("returnobject");
                        serviceIp = (String) returnobject.get("privateIP");
                        serviceStatus.setPrivateIP(serviceIp);
                    }
                } catch (JSONException ex) {
                    logger.error("Couldn't get a proper response from VIM: \n" + ex.getMessage());
                    return serviceIp;
                }
            }
        }
        return serviceIp;
    }

    /*
     * Get the List of private IPs of the VM of a specific service
     */

    private List<OrchestratorIP> getIpList(ServiceStatus serviceStatus) {

        HashMap<String, String> serviceIps = null;
        List<OrchestratorIP> ipList = null;

        String singleInstanceUri = "http://" + virtualizationManagerConfig.getUrl() + ":" + virtualizationManagerConfig.getPort()
                + "/api/v1/instance/get/instance";

        Map<String, OrchestratorProviderAuthenticationDetails> metadata = new HashMap<>();
        composeObject.getProviderAuthenticationDetails().stream().forEach(authDetails -> {
            if (!metadata.containsKey(authDetails.getId())) {
                metadata.put(authDetails.getId(), authDetails);
            }
        });

        OrchestratorComponentNodeInstance service = hashMapService
                .get(serviceStatus.getComponentNodeHexID() + "_" + serviceStatus.getComponentNodeInstanceHexID());
        OrchestratorProviderAuthenticationDetails authenticationDetails = metadata.get(service.getProviderID());

        OrchestratorInstance instance = new OrchestratorInstance();
        instance.setId(serviceStatus.getVmID());

        VirtulizationManagerRequest vimRequest = new VirtulizationManagerRequest();
        vimRequest.setAuthDetails(authenticationDetails);
        vimRequest.setInstance(instance);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity entity = new HttpEntity(vimRequest);

        for (int j = 0; j < MAXIMUM_TIMES_OF_REQUEST; j++) {
            ipList = null;
            ResponseEntity<String> responseEntity = restTemplate.exchange(singleInstanceUri, HttpMethod.POST, entity, String.class);
            if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {

                logger.debug("Response: " + responseEntity.getBody());
                try {
                    JSONObject callbackJson = new JSONObject(responseEntity.getBody());
                    if (callbackJson.getString("code").equals("SUCCESS")) {
                        JSONObject returnobject = (JSONObject) callbackJson.get("returnobject");
                        serviceIps = new ObjectMapper().readValue(returnobject.get("ipList").toString(), HashMap.class);

                        ipList = new ArrayList<>();
                        for (String networkName : serviceIps.keySet()) {
                            //We are skiping the duplication of the Public IP if any
                            if (null != serviceStatus.getFloatingIP() && !serviceStatus.getFloatingIP().isEmpty()
                                    && serviceIps.get(networkName).equals(serviceStatus.getFloatingIP())) {
                                logger.info("SAME IP " + serviceStatus.getFloatingIP());
                                continue;
                            }
                            OrchestratorIP orchestratorIp = new OrchestratorIP();
                            orchestratorIp.setNetwork(networkName);
                            orchestratorIp.setIp(serviceIps.get(networkName));
                            ipList.add(orchestratorIp);
                        }
                        break;
                    }
                } catch (JSONException ex) {
                    logger.error("Couldn't get a proper response from VIM: \n" + ex.getMessage());
                } catch (IOException ex) {
                    logger.error("Couldn't parse properly the ipList HashMap: \n" + ex.getMessage());
                }
            }
            try {
                Thread.sleep((long) WAIT_PERIOD_FOR_NEXT_REQUEST);
            } catch (InterruptedException ex) {
                logger.info("Couldn't wait in the sleep of the deleteState method: " + ex.getMessage());
                Thread.currentThread().interrupt();
            }
        }
        return ipList;
    }

    /*
     * Returns the init file for the VM in a String
     */

    private String getFlavorId(OrchestratorProviderAuthenticationDetails authenticationDetails, OrchestratorFlavor flavor) {
        String getFlavorsUri = "http://" + virtualizationManagerConfig.getUrl() + ":" + virtualizationManagerConfig.getPort() + "/api/v1/flavor/get";

        OrchestratorFlavor currflavor = new OrchestratorFlavor();
        currflavor.setId(null);
        Boolean firstCheck;

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity entity = new HttpEntity(authenticationDetails);

        for (int j = 0; j < MAXIMUM_TIMES_OF_REQUEST; j++) {
            try {
                ResponseEntity<String> responseEntity =
                        restTemplate.exchange(getFlavorsUri, HttpMethod.POST, entity, String.class);
                firstCheck = true;
                if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {

                    logger.debug("Response: " + responseEntity.getBody());

                    JSONObject callbackJson = null;
                    try {
                        callbackJson = new JSONObject(responseEntity.getBody());
                        if (callbackJson.getString("code").equals("SUCCESS")) {

                            JSONArray returnObject = (JSONArray) callbackJson.get("returnobject");
                            long size = returnObject.length();
                            for (int i = 0; i < size; i++) {
                                JSONObject object = returnObject.getJSONObject(i);
                                Integer vcpu = Integer.parseInt(object.get("vCPU").toString());
                                Integer ram = Integer.parseInt(object.get("ram").toString());
                                Integer storage = Integer.parseInt(object.get("storage").toString());

                                if (vcpu >= flavor.getvCPUs() && ram >= flavor.getRam() && storage >= flavor.getStorage()) {
                                    if (!firstCheck) {
                                        if (vcpu <= currflavor.getvCPUs() && ram <= currflavor.getRam() && storage <= currflavor.getStorage()) {
                                            currflavor.setvCPUs(vcpu);
                                            currflavor.setRam(ram);
                                            currflavor.setStorage(storage);
                                            currflavor.setId(object.get("id").toString());
                                            firstCheck = false;
                                        }

                                    } else {
                                        currflavor.setvCPUs(vcpu);
                                        currflavor.setRam(ram);
                                        currflavor.setStorage(storage);
                                        currflavor.setId(object.get("id").toString());
                                        firstCheck = false;
                                    }
                                }
                            }
                            break;
                        }
                    } catch (JSONException ex) {
                        logger.error(
                                "For loop that searches for flavorID throw the following error: " + ex.getMessage());
                        return currflavor.getId();
                    }
                }
            } catch (RestClientException ex) {
                logger.error("Rest client exception at getFlavor method: " + ex.getMessage());
            }
            try {
                Thread.sleep((long) WAIT_PERIOD_FOR_NEXT_REQUEST);
            } catch (InterruptedException ex) {
                logger.info("Couldn't wait in the sleep of the deleteState method: " + ex.getMessage());
                Thread.currentThread().interrupt();
            }
        }
        return currflavor.getId();
    }

    /*
     * Generates the scripts that configures the networks if more than one
     */
    /**
     * Renders the block that installs the cjdns init script as a service. Returns an empty string
     * when cjdns.init.script.url is not set, so nothing is downloaded from a host the operator did
     * not choose.
     */
    private String configureCjdnsService() {
        String url = generalConfig.getCjdnsInitScriptUrl();
        if (null == url || url.trim().isEmpty()) {
            return "";
        }
        return "sudo wget " + url.trim() + " -O cjdns.txt\n"
                + "sudo cp cjdns.txt /etc/init.d/cjdns\n"
                + "sudo chmod +x /etc/init.d/cjdns\n"
                + "sudo update-rc.d cjdns defaults\n"
                + "sudo service cjdns start\n";
    }

    /**
     * Renders the line that sets the password of the default worker user. Returns an empty string
     * when worker.password is not set, which leaves the image's own credentials untouched: the
     * workers are reachable over SSH with the configured keys either way.
     */
    private String configureWorkerPassword() {
        String password = generalConfig.getWorkerPassword();
        if (null == password || password.trim().isEmpty()) {
            return "";
        }
        return "sudo echo -e \"" + password + "\\n" + password + "\" | sudo passwd " + generalConfig.getWorkerUsername();
    }

    /**
     * Renders one "ip route add" line per CIDR configured in network.additional.routes. Returns an
     * empty string when the property is not set, so no site specific subnet is baked into the script.
     */
    private String additionalRoutesScript() {
        String routes = generalConfig.getNetworkAdditionalRoutes();
        if (null == routes || routes.trim().isEmpty()) {
            return "";
        }

        StringBuilder script = new StringBuilder();
        for (String route : routes.split(",")) {
            if (!route.trim().isEmpty()) {
                script.append("\t\tsudo ip route add ").append(route.trim()).append(" via \\$privateIP.1\n");
            }
        }
        return script.toString();
    }

    private String getNetworkConfigurationScript() {
        String subScript = "";

        subScript = "boolean=false\n"
                + "interfaces=()\n"
                + "for i in {3..10}\n"
                + "do\n"
                + "\tethName=\"ens\\$i\"\n"
                + "\tstate=\"\"\n"
                + "\tstate=\\$(ip a s \\$ethName | grep \"state DOWN\")\n"
                + "\tif [ ! -z \"\\$state\" ]\n"
                + "\tthen\n"
                + "\t\tboolean=true\n"
                + "\t\tinterfaces=(\"\\${interfaces[@]}\" \"\\$ethName\")\n"
                + "\t\tmac=\\$(ifconfig \\$ethName | grep -o -E '([[:xdigit:]]{1,2}:){5}[[:xdigit:]]{1,2}')\n"
                + "\n"
                + "sudo echo \"        \\$ethName:\n"
                + "            dhcp4: true\n"
                + "            match:\n"
                + "                macaddress: \\$mac\n"
                + "            set-name: \\$ethName\" | sudo tee -a /etc/netplan/50-cloud-init.yaml\n"
                + "\tfi\n"
                + "done\n"
                + "if \\$boolean\n"
                + "then\n"
                + "\tsudo netplan generate\n"
                + "\tsudo netplan apply\n"
                + "\tfor ethName in \"\\${interfaces[@]}\"\n"
                + "\tdo\n"
                + "\t\tprivateIP=\"\"\n"
                + "\t\twhile true\n"
                + "\t\tdo\n"
                + "\t\t\t# privateIP=\\$(ip a s ens3| grep -A8 -m1 MULTICAST | grep -m1 inet | cut -d' ' -f6 | cut -d'/' -f1 | cut -d'.' --fields=1,2,3)\n"
                + "\t\t\t\n"
                + "\t\t\tcheck=\\$(ip a s \\$ethName| grep -A8 -m1 MULTICAST | grep -m1 inet | cut -d' ' -f6 | cut -d'/' -f1 | cut -d'.' --fields=1)\n"
                + "\t\t\tsize=\\${#check}\n"
                + "\t\t\techo \\$size\n"
                + "\t\t\tif ([ \\$size -gt 3 ] || [ \\$size -eq 0 ])\n"
                + "\t\t\tthen\n"
                + "\t\t\t\tcontinue;\n"
                + "\t\t\tfi\n"
                + "\t\t\tprivateIP=\\$(ip a s \\$ethName| grep -A8 -m1 MULTICAST | grep -m1 inet | cut -d' ' -f6 | cut -d'/' -f1 | cut -d'.' --fields=1,2,3)\n"
                + "\t\t\tif [ ! -z \"\\$privateIP\" ]\n"
                + "\t\t\tthen\n"
                + "\t\t\t\tbreak;\n"
                + "\t\t\tfi\n"
                + "\t\tdone\n"
                //+ "\t\tprivateIP=\"\\$privateIP.1\"\n"
                + "\t\techo \\$privateIP\n"
                + "\t\tsudo ip route del default via \\$privateIP.1\n"
                + "\t\tsudo ip route del default via \\$privateIP.254\n"
                + additionalRoutesScript()
                + "\t\tfileName=\"10-netplan-\\$ethName.network\"\n"
                + "\n"
                + "\t\tsudo echo \"[Match]\n"
                + "Name=\\$ethName\n"
                + "\n"
                + "[Network]\n"
                + "DHCP=ipv4\n"
                + "\n"
                + "[DHCP]\n"
                + "UseMTU=true\n"
                + "RouteMetric=200\" | sudo tee /etc/systemd/network/\\$fileName\n"
                + "\n"
                + "\tdone \n"
                + "fi";

        return subScript;
    }

    /**
     * Interaction with the SOC Manager.
     */

    public void disableSslVerification() {

        TrustManager[] trustAllCerts = new TrustManager[]{new X509ExtendedTrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) {

            }

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {

            }

            @Override
            public void checkClientTrusted(X509Certificate[] certs, String authType) {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] certs, String authType) {

            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

        }};

        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
        } catch (KeyManagementException | NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    }

    private String findWazuhAgentId(String graphHexId, String graphInstanceHexId, String componentNodeHexId, String componentNodeInstanceHexId) {
        String wazuhAgentId = "";
        String name =
                graphHexId.toLowerCase() + "-" + graphInstanceHexId.toLowerCase() + "-" + componentNodeHexId.toLowerCase() + "-" + componentNodeInstanceHexId
                        .toLowerCase();

        //fetch wazuh authentication token
        String getWazuhAgentUrl = "https://" + generalConfig.getSocManagerIp() + ":" + generalConfig.getSocManagerPort() + "/agents?name=" + name;
        String authToken = "Bearer " + getWazuhAuthToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", authToken);

        disableSslVerification();

        HttpEntity entity = new HttpEntity(headers);
        RestTemplate restTemplate = new RestTemplate();

        for (int j = 0; j < MAXIMUM_TIMES_OF_REQUEST; j++) {

            try {
                ResponseEntity<String> responseEntity = restTemplate.exchange(getWazuhAgentUrl, HttpMethod.GET, entity, String.class);

                if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {
                    logger.debug("Method findWazuhAgentID response: " + responseEntity.getBody());

                    JSONObject callbackJson = null;
                    try {
                        callbackJson = new JSONObject(responseEntity.getBody());
                        if (callbackJson.getInt("error") == 0) {
                            JSONObject data = (JSONObject) callbackJson.get("data");
                            JSONArray affectedItems = (JSONArray) data.get("affected_items");
                            JSONObject affectedItemJSONObject = new JSONObject();
                            for (Object ob : affectedItems) {
                                affectedItemJSONObject = (JSONObject) ob;
                                String agentName = affectedItemJSONObject.getString("name");
                                if (name.equalsIgnoreCase(agentName)) {
                                    wazuhAgentId = affectedItemJSONObject.getString("id");
                                    logger.info("Wazuh Agent ID for component: " + name + " ID: " + wazuhAgentId);
                                    break;
                                }
                            }
                            if (!wazuhAgentId.equals("")) {
                                break;
                            }
                        }
                    } catch (JSONException ex) {
                        logger.error("Json parsing at findWazuhAgentID throw the following error: " + ex.getMessage());
                    }
                }
            } catch (HttpClientErrorException eh) {
                logger.error("Http Client Error Exception at findWazuhAgentID method: " + eh.getMessage());
            } catch (RestClientException er) {
                logger.error("Rest client exception at findWazuhAgentID method: " + er.getMessage());
            }

            try {
                Thread.sleep((long) WAIT_PERIOD_FOR_NEXT_REQUEST);
            } catch (InterruptedException ex) {
                logger.info("Couldn't wait in the sleep of the findWazuhAgentID method: " + ex.getMessage());
                Thread.currentThread().interrupt();
            }
        }
        return wazuhAgentId;
    }

    private Boolean removeWazuhAgent(String wazuhAgentId) {
        Boolean status = false;

        String getWazuhAgentUrl =
                "https://" + generalConfig.getSocManagerIp() + ":" + generalConfig.getSocManagerPort() + "/agents?agents_list=" + wazuhAgentId
                        + "&status=all&older_than=10s";

        String authToken = "Bearer " + getWazuhAuthToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", authToken);

        disableSslVerification();

        HttpEntity entity = new HttpEntity(headers);
        RestTemplate restTemplate = new RestTemplate();

        for (int j = 0; j < MAXIMUM_TIMES_OF_REQUEST; j++) {
            try {
                ResponseEntity<String> responseEntity = restTemplate.exchange(getWazuhAgentUrl, HttpMethod.DELETE, entity, String.class);
                if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {
                    logger.debug("Method removeWazuhAgent response: " + responseEntity.getBody());

                    JSONObject callbackJson = null;
                    try {
                        callbackJson = new JSONObject(responseEntity.getBody());
                        if (callbackJson.getInt("error") == 0) {
                            if (callbackJson.getString("message").equals("All selected agents were deleted")) {
                                status = true;
                                break;
                            }
                        }
                    } catch (JSONException ex) {
                        logger.error("Json parsing at removeWazuhAgent throw the following error: " + ex.getMessage());
                    }
                }
            } catch (HttpClientErrorException eh) {
                logger.error("Http Client Error Exception at removeWazuhAgent method: " + eh.getMessage());
            } catch (RestClientException er) {
                logger.error("Rest client exception at removeWazuhAgent method: " + er.getMessage());
            }

            try {
                Thread.sleep((long) WAIT_PERIOD_FOR_NEXT_REQUEST);
            } catch (InterruptedException ex) {
                logger.info("Couldn't wait in the sleep of the removeWazuhAgent method: " + ex.getMessage());
                Thread.currentThread().interrupt();
            }
        }
        logger.info("Wazuh Agent with ID: " + wazuhAgentId + " status: " + status);
        return status;
    }

    private String getWazuhAuthToken() {

        String wazuhAuthUrl = "https://" + generalConfig.getSocManagerIp() + ":" + generalConfig.getSocManagerPort() + "/security/user/authenticate?raw=true";
        String notEncoded = generalConfig.getSocManagerUsername() + ":" + generalConfig.getSocManagerPassword();

        String encodedAuth = "Basic " + Base64.getEncoder().encodeToString(notEncoded.getBytes());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", encodedAuth);

        disableSslVerification();

        HttpEntity entity = new HttpEntity(headers);
        RestTemplate restTemplate = new RestTemplate();

        String authToken;
        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(wazuhAuthUrl, HttpMethod.GET, entity, String.class);
            return responseEntity.getBody();
        } catch (Exception ex) {
            logger.error("Error at retrieve wazuh token: ", ex.getMessage());
            return null;
        }
    }

    /**
     * Interaction with the Backend Microservice.
     */

    /*
     * Sends status reports to the backend Microservice
     */
    private void sendReportingStatus(OrchestratorChangedStatusNotification status, String key) {
        //TODO check that sends correctly the messages
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonInString = "";
        try {
            jsonInString = objectMapper.writeValueAsString(status);
        } catch (JsonProcessingException ex) {
            logger.error("Couldn't construct the json string of the reporting-status", ex.getMessage());
            return;
        }

        try {
            reportStatusProducer.beginTransaction();
            reportStatusProducer.send(new ProducerRecord<String, String>(reportStatusTopic, key, jsonInString)).get();
            reportStatusProducer.commitTransaction();
        } catch (Exception ex) {
            logger.error("Couldn't send the the json string to the " + reportStatusTopic + " topic", ex.getMessage());
            return;
        }
    }

    /*
     * Reads the topic the backend send requests
     */
    private Boolean readBackendRequests() {
        Boolean undeployed = false;

        Duration duration = Duration.ofMillis(READ_TOPIC_TIMEOUT);
        ConsumerRecords<String, String> records = backendRequestConsumer.poll(duration);
        if (records.isEmpty()) {
            logger.debug("The backend-requests topic is empty");
            return false;
        } else {

            for (ConsumerRecord<String, String> record : records) {
                try {
                    OrchestratorApplicationInstance backendRequest = objectMapper.readValue(record.value(), OrchestratorApplicationInstance.class);
                    if (backendRequest.getGraphHexID().equals(composeObject.getGraphHexID()) && backendRequest.getGraphInstanceHexID()
                            .equals(composeObject.getGraphInstanceHexID())) {

                        //TODO currently when we receive a message here we do undeployGraph and only that
                        metricCollector.logMetric(lifecycleDimensionId, Lifecycle.RequestUndeploy.getStatus());
                        Boolean check = undeployGraph(composeObject);

                        RestTemplate restTemplate = new RestTemplate();
                        String uri = backendRequest.getCallbackURL();
                        if (check) {
                            uri = uri + "/SUCCESS";
                            logger.info("The graph: " + composeObject.getGraphName() + "_" + composeObject.getGraphInstanceName() + " UNDEPLOYED");
                            undeployed = true;
                        } else {
                            uri = uri + "/ERROR";
                            logger.info("The graph: " + composeObject.getGraphName() + "_" + composeObject.getGraphInstanceName() + " failed to be undeployed");
                        }

                        //TODO try to make it until success try catch if an error occurs
                        ResponseEntity<String> responseEntity = restTemplate.exchange(uri, HttpMethod.POST, null, String.class);
                        //if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {
                        //
                        //    logger.debug("Response: " + responseEntity.getBody());
                        //
                        //    JSONObject callbackJSON = new JSONObject(responseEntity.getBody());
                        //
                        //    if (callbackJSON.getString("code").equals("SUCCESS")) {
                        //        return true;
                        //    }
                        //}
                    }
                } catch (IOException ex) {
                    logger.error("IOException at readBackendRequests " + ex.getMessage());
                } catch (NullPointerException ex) {
                    logger.error("IOException at NullPointerException " + ex.getMessage() + "\n record value " + record.value());
                }
            }
        }
        backendRequestConsumer.commitSync();
        return undeployed;
    }

    /**
     * Interaction with Policy-Engine Microservice.
     */

    /*
     * Reads the topics that gives the triggering actions from the policy-engine
     */
    private void readTriggeredActions() {
        Duration duration = Duration.ofMillis(READ_TOPIC_TIMEOUT);
        ConsumerRecords<String, String> records = policyEngineConsumer.poll(duration);
        if (records.isEmpty()) {
            logger.debug("The graph-triggered-actions topic is empty");
        } else {
            for (ConsumerRecord<String, String> record : records) {
                try {
                    TriggerActionModel triggerActionModel = objectMapper.readValue(record.value(), TriggerActionModel.class);

                    if (triggerActionModel.getGraphHexID().equals(composeObject.getGraphHexID()) && triggerActionModel.getGraphInstanceHexID()
                            .equals(composeObject.getGraphInstanceHexID())) {

                        //TODO we need to find a way to make it transactional
                        if (triggerActionModel.getAction().name().equals(ActionType.scaleOut.name())) {

                            String msg = triggerActionModel.getGraphHexID() + " - " + triggerActionModel.getGraphInstanceHexID() + " - " + triggerActionModel
                                    .getComponentNodeHexID();
                            logger.info("Scale OUT action received for: " + msg);
                            metricCollector.logMetric(elasticityDimensionId.get(triggerActionModel.getComponentNodeHexID()),
                                    Elasticity.ScaleOutPolicyRequest.getStatus());
                            if (generalConfig.getMonitoringDelayEnabled()) {
                                //TODO Delete in production, artificial time consumption added
                                try {
                                    Thread.sleep(2 * 1000);
                                } catch (InterruptedException ex) {
                                    ex.printStackTrace();
                                }
                            }

                            scaleOut(triggerActionModel);
                            metricCollector
                                    .logMetric(elasticityDimensionId.get(triggerActionModel.getComponentNodeHexID()), Elasticity.ScaleOutTotal.getStatus());

                        } else if (triggerActionModel.getAction().name().equals(ActionType.scaleIn.name())) {

                            String msg = triggerActionModel.getGraphHexID() + " - " + triggerActionModel.getGraphInstanceHexID() + " - " + triggerActionModel
                                    .getComponentNodeHexID();
                            logger.info("Scale IN action received for: " + msg);
                            metricCollector.logMetric(elasticityDimensionId.get(triggerActionModel.getComponentNodeHexID()),
                                    Elasticity.ScaleInPolicyRequest.getStatus());
                            if (generalConfig.getMonitoringDelayEnabled()) {
                                //TODO Delete in production, artificial time consumption added
                                try {
                                    Thread.sleep(2 * 1000);
                                } catch (InterruptedException ex) {
                                    ex.printStackTrace();
                                }
                            }

                            scaleIn(triggerActionModel);
                            metricCollector
                                    .logMetric(elasticityDimensionId.get(triggerActionModel.getComponentNodeHexID()), Elasticity.ScaleInTotal.getStatus());
                        } else {
                            logger.warn("The policy engine send a unknown action: " + triggerActionModel.getAction().name());
                        }
                    }
                } catch (IOException ex) {
                    logger.error("IOException at readTriggeredActions " + ex.getMessage());
                }
            }
            policyEngineConsumer.commitSync();
        }
    }

    /**
     * Interaction with Virtualization-Manager Microservice.
     */

    /*
     * Send the spawn instance request to virtual manager with rest call
     * //TODO what to do if JSONObject throws exception?
     */
    private Boolean spawnInstanceCall(VirtulizationManagerRequest vimRequest, OrchestratorComponentNodeInstance service) {
        String spawnInstanceUri = "http://" + virtualizationManagerConfig.getUrl() + ":" + virtualizationManagerConfig.getPort() + "/api/v1/instance/boot";

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity entity = new HttpEntity(vimRequest);
        for (int j = 0; j < MAXIMUM_TIMES_OF_REQUEST; j++) {
            try {
                ResponseEntity<String> responseEntity = restTemplate.exchange(spawnInstanceUri, HttpMethod.POST, entity, String.class);
                if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {

                    logger.debug("Response: " + responseEntity.getBody());

                    JSONObject callbackJson = new JSONObject(responseEntity.getBody());

                    if (callbackJson.getString("code").equals("SUCCESS")) {

                        JSONObject returnObject = (JSONObject) callbackJson.get("returnobject");

                        ServiceStatus serviceStatus = hashMapServiceStatus.get(service.getComponentNodeHexID() + "_" + service.getComponentNodeInstanceHexID());
                        logger.info("VM ID: " + (String) returnObject.get("id"));
                        serviceStatus.setVmID((String) returnObject.get("id"));
                        return true;
                    }
                }
            } catch (JSONException ex) {
                logger.error("For loop at spawnInstanceCall method throw the following error: " + ex.getMessage());
            } catch (RestClientException ex) {
                logger.error("Rest client exception at spawnInstanceCall method: " + ex.getMessage());
            }

            try {
                Thread.sleep((long) WAIT_PERIOD_FOR_NEXT_REQUEST);
            } catch (InterruptedException ex) {
                logger.info("Couldn't wait in the sleep of the spawnInstanceCall method: " + ex.getMessage());
                Thread.currentThread().interrupt();
            }
        }
        return false;
    }

    /*
     * Send the delete instance request to virtual manager with rest call
     */
    private Boolean deleteInstanceCall(VirtulizationManagerRequest vimRequest) {
        String spawnInstanceUri = "http://" + virtualizationManagerConfig.getUrl() + ":" + virtualizationManagerConfig.getPort() + "/api/v1/instance/remove";
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity entity = new HttpEntity(vimRequest);

        for (int i = 0; i < MAXIMUM_TIMES_OF_REQUEST; i++) {

            try {
                ResponseEntity<String> responseEntity = restTemplate.exchange(spawnInstanceUri, HttpMethod.POST, entity, String.class);
                if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {
                    logger.debug("Response: " + responseEntity.getBody());
                    JSONObject callbackJson = new JSONObject(responseEntity.getBody());
                    if (callbackJson.getString("code").equals("SUCCESS")) {
                        return true;
                    }
                }

            } catch (JSONException ex) {
                logger.error("For loop at deleteInstanceCall method throw the following error: " + ex.getMessage());
            } catch (RestClientException ex) {
                logger.error("Rest client exception at deleteInstanceCall method: " + ex.getMessage());
            }

            try {
                Thread.sleep((long) WAIT_PERIOD_FOR_NEXT_REQUEST);
            } catch (InterruptedException ex) {
                logger.info("Couldn't wait in the sleep of the deleteInstanceCall method: " + ex.getMessage());
                Thread.currentThread().interrupt();
            }
        }
        return false;
    }

    /*
     * Send attach floating ip on the instance request to virtual manager with rest call
     */
    private Boolean attachFloatingIpCall(OrchestratorComponentNodeInstance service, ServiceStatus serviceStatus) {
        String attachFloatingIpUri =
                "http://" + virtualizationManagerConfig.getUrl() + ":" + virtualizationManagerConfig.getPort() + "/api/v1/instance/attach/floating";

        Map<String, OrchestratorProviderAuthenticationDetails> metadata = new HashMap<>();
        composeObject.getProviderAuthenticationDetails().stream().forEach(authDetails -> {
            if (!metadata.containsKey(authDetails.getId())) {
                metadata.put(authDetails.getId(), authDetails);
            }
        });

        OrchestratorInstance instance = new OrchestratorInstance();
        instance.setId(serviceStatus.getVmID());
        instance.setFloatingPool(metadata.get(service.getProviderID()).getPublicNetwork());

        VirtulizationManagerRequest vimRequest = new VirtulizationManagerRequest();
        vimRequest.setAuthDetails(metadata.get(service.getProviderID()));
        vimRequest.setInstance(instance);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity entity = new HttpEntity(vimRequest);
        for (int j = 0; j < MAXIMUM_TIMES_OF_REQUEST; j++) {
            try {
                ResponseEntity<String> responseEntity = restTemplate.exchange(attachFloatingIpUri, HttpMethod.POST, entity, String.class);
                if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {

                    logger.debug("Response: " + responseEntity.getBody());

                    JSONObject callbackJson = new JSONObject(responseEntity.getBody());

                    if (callbackJson.getString("code").equals("SUCCESS")) {

                        JSONObject returnObject = (JSONObject) callbackJson.get("returnobject");

                        logger.info("FloatingIP: " + (String) returnObject.get("floatingIP"));
                        serviceStatus.setFloatingIP((String) returnObject.get("floatingIP"));
                        return true;
                    }
                }
            } catch (JSONException ex) {
                logger.error(
                        "For loop at attachFloatingIPCall method throw the following error: " + ex.getMessage());
            } catch (RestClientException ex) {
                logger.error("Rest client exception at attachFloatingIPCall method: " + ex.getMessage());
            }

            try {
                Thread.sleep((long) WAIT_PERIOD_FOR_NEXT_REQUEST);
            } catch (InterruptedException ex) {
                logger.info("Couldn't wait in the sleep of the attachFloatingIPCall method: " + ex.getMessage());
                Thread.currentThread().interrupt();
            }
        }
        return false;
    }

    /*
     * Send detach floating ip on the instance request to virtual manager with rest call
     */
    private Boolean detachFloatingIpCall(OrchestratorComponentNodeInstance service, ServiceStatus serviceStatus) {
        String detachFloatingIpUri =
                "http://" + virtualizationManagerConfig.getUrl() + ":" + virtualizationManagerConfig.getPort() + "/api/v1/instance/detach/floating";
        Map<String, OrchestratorProviderAuthenticationDetails> metadata = new HashMap<>();
        composeObject.getProviderAuthenticationDetails().stream().forEach(authDetails -> {
            if (!metadata.containsKey(authDetails.getId())) {
                metadata.put(authDetails.getId(), authDetails);
            }
        });

        OrchestratorInstance instance = new OrchestratorInstance();
        instance.setId(serviceStatus.getVmID());
        instance.setFloatingIP(serviceStatus.getFloatingIP());

        VirtulizationManagerRequest vimRequest = new VirtulizationManagerRequest();
        vimRequest.setAuthDetails(metadata.get(service.getProviderID()));
        vimRequest.setInstance(instance);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity entity = new HttpEntity(vimRequest);
        for (int j = 0; j < MAXIMUM_TIMES_OF_REQUEST; j++) {
            try {
                ResponseEntity<String> responseEntity = restTemplate.exchange(detachFloatingIpUri, HttpMethod.POST, entity, String.class);
                if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {

                    logger.debug("Response: " + responseEntity.getBody());

                    JSONObject callbackJson = new JSONObject(responseEntity.getBody());
                    if (callbackJson.getString("code").equals("SUCCESS")) {
                        return true;
                    }
                }
            } catch (JSONException ex) {
                logger.error("For loop at detachFloatingIPCall method throw the following error: " + ex.getMessage());
            } catch (RestClientException ex) {
                logger.error("Rest client exception at detachFloatingIPCall method: " + ex.getMessage());
            }

            try {
                Thread.sleep((long) WAIT_PERIOD_FOR_NEXT_REQUEST);
            } catch (InterruptedException ex) {
                logger.info("Couldn't wait in the sleep of the detachFloatingIPCall method: " + ex.getMessage());
                Thread.currentThread().interrupt();
            }
        }
        return false;
    }

    /**
     * Push Metric Helping Methods.
     */

    /*
     * Calculate env service hashes of the component node instance
     */
    private SecurityConfigurationResult calculateEnvServiceHashes(OrchestratorComponentNodeInstance service) {

        JsonArray envList = new JsonArray();
        Map<String, String> envVariables = service.getEnvironmentalVariables();
        ServiceStatus serviceStatus = hashMapServiceStatus.get(service.getComponentNodeHexID() + "_" + service.getComponentNodeInstanceHexID());
        if (envVariables != null && !envVariables.isEmpty()) {
            for (String envKey : envVariables.keySet()) {

                JsonObject env = new JsonObject();
                String envValue = envVariables.get(envKey);

                if (envValue.startsWith("@") || envValue.startsWith("#")) {
                    String envValueSubString = envValue.substring(1);
                    String envIp = "";

                    if (envValueSubString.equals("IPV4_PRIVATE")) {
                        envIp = getServiceIp(serviceStatus, IPType.IPv4);
                    } else if (envValueSubString.equals("IPV4_PUBLIC")) {
                        envIp = getServiceIp(serviceStatus, IPType.PublicIP);
                    } else if (envValueSubString.equals("IPV6_PRIVATE")) {
                        envIp = getServiceIp(serviceStatus, IPType.IPv6);
                    } else {
                        for (ServiceStatus depServiceStatus : composeStatusObject.getServiceStatusList()) {
                            if (depServiceStatus.getComponentNodeHexID().equals(envValueSubString)) {
                                if (composeObject.getIpv6Enabled()) {
                                    envIp = getServiceIp(depServiceStatus, IPType.IPv6);
                                } else {
                                    envIp = getServiceIp(depServiceStatus, IPType.IPv4);
                                }
                                break;
                            }
                        }
                    }
                    if (envValue.startsWith("#")) {
                        envIp = "[" + envIp + "]";
                    }

                    envValue = envIp;
                }

                env.addProperty(envKey, envValue);
                envList.add(env);
            }
        } else {
            System.out.println("No env for component: " + service.getComponentNodeName());
        }
        String envListString = envList.toString();

        String dockerEnvHashing = Encryption.stringHash(envListString);
        SecurityConfigurationResult securityConfigurationResult = new SecurityConfigurationResult();
        securityConfigurationResult.setHashType(HashType.DOCKER_ENV);
        securityConfigurationResult.setValue(dockerEnvHashing);
        return securityConfigurationResult;
    }

    /*
     * Calculate port service hashes of the component node instance
     */
    private SecurityConfigurationResult calculatePortServiceHashes(OrchestratorComponentNodeInstance service) {

        List<String> portList = new ArrayList<>();
        //        if (service.getNetworkModeHost() != null && service.getNetworkModeHost() == true) {
        //            //TODO fill in when network mode host is enabled
        //
        //        } else {
        System.out.println("CNID: " + service.getComponentNodeHexID() + " ports: " + service.getPorts());
        for (OrchestratorPort orchestratorPort : service.getPorts()) {
            String port = orchestratorPort.getPublished() + ":" + orchestratorPort.getTarget();
            if (orchestratorPort.getProtocol() == null || orchestratorPort.getProtocol().equals("BOTH")) {
                portList.add(port + "/tcp");
                portList.add(port + "/udp");
            } else if (orchestratorPort.getProtocol().equals("TCP")) {
                portList.add(port + "/tcp");
            } else if (orchestratorPort.getProtocol().equals("UDP")) {
                portList.add(port + "/udp");
            } else {

                System.out.println("CNID: " + service.getComponentNodeHexID() + " protocol: " + orchestratorPort.getProtocol());


            }
        }
        //        }
        portList.sort(Comparator.comparing(String::toString));
        try {
            String dockerPortList = "";
            if (service.getNetworkModeHost() != null && service.getNetworkModeHost() == true) {
                //TODO fill in when network mode host is enabled
            } else {
                dockerPortList = objectMapper.writeValueAsString(portList);
            }
            System.out.println("CNID: " + service.getComponentNodeHexID() + " portList: " + dockerPortList);
            String dockerPortHashing = Encryption.stringHash(dockerPortList);
            SecurityConfigurationResult securityConfigurationResult = new SecurityConfigurationResult();
            securityConfigurationResult.setHashType(HashType.DOCKER_PORT);
            securityConfigurationResult.setValue(dockerPortHashing);
            return securityConfigurationResult;

        } catch (JsonProcessingException ex) {
            logger.error("Couldn't construct the json string of the reporting-status", ex.getMessage());
        }

        return null;
    }

    /*
     * Send calculated hashes to backend using kafka
     */
    private void sendHashesToBackend(OrchestratorComponentNodeInstance service, String key) {
        SecurityConfiguration securityConfiguration = new SecurityConfiguration();
        securityConfiguration.setGraphHexId(this.composeObject.getGraphHexID());
        securityConfiguration.setGraphInstanceHexId(this.composeObject.getGraphInstanceHexID());
        securityConfiguration.setComponentNodeHexId(service.getComponentNodeHexID());
        securityConfiguration.setComponentNodeInstanceHexId(service.getComponentNodeInstanceHexID());
        securityConfiguration.setResultHexId(null);
        securityConfiguration.setCertificate(null);
        securityConfiguration.setSecurityConfigurationType(null);

        List<SecurityConfigurationResult> securityConfigurationResultList = new ArrayList<>();
        securityConfigurationResultList.add(calculateEnvServiceHashes(service));
        securityConfigurationResultList.add(calculatePortServiceHashes(service));

        try {
            securityConfiguration.setSecurityConfigurationResultList(securityConfigurationResultList);
            String securityConfigurationAsJson = objectMapper.writeValueAsString(securityConfiguration);
            System.out.println(securityConfigurationAsJson);
            String encryptedSecurityConfiguration = Encryption.encrypt(securityConfigurationAsJson, generalConfig.getEncryptionKeyToken());
            securityResultsProducer.beginTransaction();
            securityResultsProducer.send(new ProducerRecord<String, String>(kafkaConfig.getSecurityResults(), key, encryptedSecurityConfiguration)).get();
            securityResultsProducer.commitTransaction();

        } catch (JsonProcessingException ex) {
            logger.error("Couldn't construct the json string of the reporting-status", ex.getMessage());
            return;
        } catch (Exception ex) {
            logger.error("Couldn't send the the json string to the " + kafkaConfig.getSecurityResults() + " topic", ex.getMessage());
            return;
        }
    }


    /**
     * Push Metric Helping Methods.
     */

    private void increaseActiveWorkersMetric(String componentHexId, int amount) {
        activeWorkersCount.put(componentHexId, activeWorkersCount.get(componentHexId) + amount);
        metricCollector.logMetric(activeWorkersDimensionId.get(componentHexId), activeWorkersCount.get(componentHexId));
    }

    private void decreaseActiveWorkersMetric(String componentHexId, int amount) {
        activeWorkersCount.put(componentHexId, activeWorkersCount.get(componentHexId) - amount);
        metricCollector.logMetric(activeWorkersDimensionId.get(componentHexId), activeWorkersCount.get(componentHexId));
    }

    private void increaseTotalWorkersMetric(String componentNodeHexId, int amount) {
        totalWorkersCount.put(componentNodeHexId, totalWorkersCount.get(componentNodeHexId) + amount);
        metricCollector.logMetric(totalWorkersDimensionId.get(componentNodeHexId), totalWorkersCount.get(componentNodeHexId));
    }

    private void decreaseTotalWorkersMetric(String componentNodeHexId, int amount) {
        totalWorkersCount.put(componentNodeHexId, totalWorkersCount.get(componentNodeHexId) - amount);
        metricCollector.logMetric(totalWorkersDimensionId.get(componentNodeHexId), totalWorkersCount.get(componentNodeHexId));
    }

    /**
     * Control loop state management.
     */

    /*
     * Fetches the state of the graphInstance and initializes the appropriate objects
     */
    private void fetchState(String graphHexId, String graphInstanceHexId) {
        String path = "maestro/orchestrator/state/" + graphHexId + "/" + graphInstanceHexId + "/";
        while (true) {
            try {
                String gsonComposeObject = consulClient.getKVValue(path + "composeObject").getValue().getDecodedValue();
                composeObject = new Gson().fromJson(gsonComposeObject, OrchestratorApplicationInstance.class);

                if (consulClient.getKVValue(path + "composeStatusObject").getValue() != null) {
                    String gsonComposeObjectStatus = consulClient.getKVValue(path + "composeStatusObject").getValue().getDecodedValue();
                    composeStatusObject = new Gson().fromJson(gsonComposeObjectStatus, ComposeStatusObject.class);
                } else {
                    //TODO fetch somehow the floating IP
                    composeStatusObject = getComposeStatusObject(composeObject);
                }
                break;
            } catch (com.ecwid.consul.transport.TransportException ex) {
                logger.error("Couldn't fetch the state of the graph" + ex.getMessage());
                try {
                    Thread.sleep((long) 5 * 1000);
                } catch (InterruptedException ex1) {
                    logger.error("InterruptedException at fetchState " + ex1.getMessage());
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /*
     * Saves the state of the graphInstance for the needed objects
     */
    private void saveState(String graphId, String graphInstanceId) {
        String path = "maestro/orchestrator/state/" + graphId + "/" + graphInstanceId + "/";
        try {
            String gsonComposeObject = new Gson().toJson(composeObject);
            consulClient.setKVValue(path + "composeObject", gsonComposeObject);

            String gsonComposeObjectStatus = new Gson().toJson(composeStatusObject);
            consulClient.setKVValue(path + "composeStatusObject", gsonComposeObjectStatus);
        } catch (com.ecwid.consul.transport.TransportException ex) {
            logger.error("Couldn't save the state of the graph" + ex.getMessage());
        }
    }

    /*
     * Deletes the state of the graphInstance from the persistent storage
     */
    private void deleteState(String graphHexId, String graphInstanceHexId) {
        for (int i = 0; i < MAXIMUM_TIMES_OF_REQUEST; i++) {
            try {
                String path = "maestro/orchestrator/state/" + graphHexId + "/" + graphInstanceHexId + "/";
                consulClient.deleteKVValues(path);
                break;
            } catch (com.ecwid.consul.transport.TransportException ex) {
                logger.error("Couldn't delete the state of the graph" + ex.getMessage());
            }
            try {
                Thread.sleep((long) WAIT_PERIOD_FOR_NEXT_REQUEST);
            } catch (InterruptedException ex) {
                logger.info("Couldn't wait in the sleep of the deleteState method: " + ex.getMessage());
                Thread.currentThread().interrupt();
            }
        }

        for (int i = 0; i < MAXIMUM_TIMES_OF_REQUEST; i++) {
            try {
                String machinesConstants = "maestro/machinesConstants/" + graphHexId + "/" + graphInstanceHexId + "/";
                consulClient.deleteKVValues(machinesConstants);
                break;
            } catch (com.ecwid.consul.transport.TransportException ex) {
                logger.error("Couldn't delete the machinesConstants of the graph" + ex.getMessage());
            }
            try {
                Thread.sleep((long) WAIT_PERIOD_FOR_NEXT_REQUEST);
            } catch (InterruptedException ex) {
                logger.info("Couldn't wait in the sleep of the deleteState method: " + ex.getMessage());
                Thread.currentThread().interrupt();
            }
        }

        for (OrchestratorComponentNodeInstance componentNodeInstance : composeObject.getServices()) {
            try {
                Response<CatalogNode> node = consulClient.getCatalogNode(
                        graphHexId + "-" + graphInstanceHexId + "-" + componentNodeInstance.getComponentNodeHexID() + "-" + componentNodeInstance
                                .getComponentNodeInstanceHexID(), QueryParams.DEFAULT);
                consulClient.agentForceLeave(node.getValue().getNode().getNode());
            } catch (NullPointerException ex) {
                logger.warn("Couldn't enforce the consul agent of node: " + graphHexId + "-" + graphInstanceHexId + "-" + componentNodeInstance
                        .getComponentNodeHexID() + "-" + componentNodeInstance.getComponentNodeInstanceHexID() + " to leave!");
            }

            if (componentNodeInstance.getStatusSoc() != null && componentNodeInstance.getStatusSoc()) {
                removeWazuhAgent(findWazuhAgentId(graphHexId, graphInstanceHexId, componentNodeInstance.getComponentNodeHexID(), componentNodeInstance
                        .getComponentNodeInstanceHexID()));
            }
        }

    }

    /**
     * Other Helping Methods.
     */

    /*
     * Creates a consumer for a specified topic
     * with a specific group and consumer id
     */
    private Consumer constructConsumer(String topic, String group) {
        String bootstrapServer = kafkaConfig.getUrl() + ":" + kafkaConfig.getPort();

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, group);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, "3000");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "50000");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "100000");

        // subscribe consumer into a topic
        Consumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList(topic));

        return consumer;
    }

    /*
     * Creates a producer for a specified topic
     */
    private Producer constructProducer(String transactionId) {
        //TODO try catch runtime exceptions and check that the producer AND the topic created correctly
        String bootstrapServer = kafkaConfig.getUrl() + ":" + kafkaConfig.getPort();
        Properties props = new Properties();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, transactionId);

        Producer<String, String> producer = new KafkaProducer<>(props);
        producer.initTransactions();
        return producer;
    }

    /*
     * Deletes all the unneeded things
     */
    private void garbageCollection() {
        //TODO try catch runtime exceptions
        reportStatusProducer.flush();
        reportStatusProducer.close();

        backendRequestConsumer.unsubscribe();
        backendRequestConsumer.close();

        policyEngineConsumer.unsubscribe();
        policyEngineConsumer.close();

        String graphHexId = composeObject.getGraphHexID();
        String graphInstanceHexId = composeObject.getGraphInstanceHexID();

        deleteState(graphHexId, graphInstanceHexId);

        logger.info("Garbage Collection for graph: " + composeObject.getGraphName() + "_" + composeObject.getGraphInstanceName() + " finished!");
    }

}
