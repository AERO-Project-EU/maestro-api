package eu.orchestrator.agent;

import eu.orchestrator.agent.configuration.AgentConfiguration;
import eu.orchestrator.agent.configuration.ErrorStatus;
import eu.orchestrator.agent.configuration.HostnameMappingsStatus;
import eu.orchestrator.agent.configuration.SuccessStatus;
import eu.orchestrator.agent.model.DockerConfigurationForHashing;
import eu.orchestrator.agent.model.MachineConstants;
import eu.orchestrator.agent.model.kv.AgentParameters;
import eu.orchestrator.agent.model.kv.Arguments;
import eu.orchestrator.agent.model.kv.ComponentImage;
import eu.orchestrator.agent.model.kv.Dependencies;
import eu.orchestrator.agent.model.kv.HealthCheck;
import eu.orchestrator.agent.model.kv.HostnameMappings;
import eu.orchestrator.agent.model.kv.KafkaConfiguration;
import eu.orchestrator.agent.service.ForensicService;
import eu.orchestrator.agent.util.CommandLineExecutor;
import eu.orchestrator.agent.util.Encryption;
import eu.orchestrator.agent.util.PrometheusMetricParser;
import eu.orchestrator.collector.ChartType;
import eu.orchestrator.collector.Collector;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.NewCheck;
import com.ecwid.consul.v1.agent.model.NewService;
import com.ecwid.consul.v1.health.model.HealthService;
import com.ecwid.consul.v1.kv.model.GetValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;


/**
 * @author Panagiotis Gouvas.
 * @author Panagiotis Parthenis.
 */
public class Agent {

    //LOGGER
    private static final Logger LOGGER = Logger.getLogger(Agent.class.getName());

    //Arguments
    private String graphHexId;
    private String graphInstanceHexId;
    private String componentNodeHexId;
    private String componentNodeInstanceHexId;

    //Inner Arguments
    private String privateIpAddressIPv6;

    private String privateIpAddressIPv4;
    private String publicIpAddressIPv4;

    private int status = 1;

    //initial clients
    private ConsulClient consulClient;
    private MachineConstants machineConstants;
    private Collector collector;

    //Times
    private static long startAt;

    //Profile Metric
    private String metricAgentId;
    private String benchmarkId;

    //Inner Objects from consul KV
    private AgentParameters agentParameters;
    private Arguments arguments;
    private ComponentImage componentImage;
    private Dependencies dependencies;
    private HealthCheck healthCheck;
    private HostnameMappings hostnameMappings;
    private KafkaConfiguration kafkaConfiguration;

    // Inner Objects for security
    // TODO ASTRID
    private List<String> dockerEnvironmentVariablesKeysList = new ArrayList<>();
    private String dockerImage;


    public static void main(String[] args) {
        Agent agent = new Agent();
        agent.bootAgent(args);
    }

    public void bootAgent(String[] args) {

        phase1(args);

        phase2();

        phase3();

        phase4();

        phase5();

        phase6();

        phase7();

    }

    //--------------------------------------------------------------------------------All phases
    private void phase1(String[] args) {

        startMrClock();

        graphHexId = args[0];
        graphInstanceHexId = args[1];
        componentNodeHexId = args[2];
        componentNodeInstanceHexId = args[3];

        LOGGER.log(Level.INFO,
                "Starting Agent for descriptor: {0}  AND descriptor instance: {1} AND component: {2} AND instance: {3}",
                new Object[]{graphHexId, graphInstanceHexId, componentNodeHexId,
                        componentNodeInstanceHexId});

        collector = new Collector(AgentConfiguration.AGENT_METRIC_EXPORTER_RUNNING_ON_SOCKET);
        metricAgentId = collector
                .registerMetric("agentExecution", "Agent Execution time", "milliseconds", "agent",
                        "agent_profile_execution", ChartType.line);

        benchmarkId = collector
                .registerMetric("benchmarkExecution", "Benchmark Execution metrics", "milliseconds", "agent",
                        "benchmark_profile_execution", ChartType.line);

        pushMetric("phase1");
    }

    private void phase2() {

        //get ipv6
        startMrClock();
        for (int i = 0; i < AgentConfiguration.MAX_ITERATIONS; i++) {
            if (getCjdnsIpV6()) {
                pushMetric("phase2_getIPv6");
                LOGGER.log(Level.INFO, "Retrieved successfully CJDNS  IPv6: {0}", privateIpAddressIPv6);
                break;

            } else {
                relaxTime();

            }
        }

        //init consul
        startMrClock();
        if (status > 0) {
            for (int i = 0; i < AgentConfiguration.MAX_ITERATIONS; i++) {
                if (initializationConsul()) {
                    status = 1;
                    setStatus(status);
                    pushMetric("phase2_initialize_consul");
                    LOGGER.log(Level.INFO, "Consul agent initialized successfully");
                    break;

                } else {
                    relaxTime();

                }
            }
        }

        //retrieve objects from consul KV
        startMrClock();
        if (status > 0) {
            for (int i = 0; i < AgentConfiguration.MAX_ITERATIONS; i++) {
                if (retrieveAllObject()) {
                    status = 1;
                    setStatus(status);
                    pushMetric("phase2_retrieve_all_objects");
                    LOGGER.log(Level.INFO, "Retrieve all objects from consul KV");
                    break;

                } else {
                    relaxTime();

                }
            }
        }

        //get ipv4 (private)
        startMrClock();
        if (status > 0) {
            for (int i = 0; i < AgentConfiguration.MAX_ITERATIONS; i++) {
                if (getPrivateIpv4()) {
                    status = 1;
                    setStatus(status);
                    pushMetric("phase2_getIPv4_private");
                    LOGGER.log(Level.INFO, "Retrieved successfully private IPv4: {0}", privateIpAddressIPv4);
                    break;

                } else {
                    relaxTime();

                }
            }
        }

        //get ipv4 (public)
        startMrClock();
        if (status > 0 && agentParameters.isHasEnablePublicIpV4()) {
            for (int i = 0; i < AgentConfiguration.MAX_ITERATIONS; i++) {
                if (getPublicIpv4()) {
                    status = 1;
                    setStatus(status);
                    pushMetric("phase2_getIPv4_public");
                    LOGGER.log(Level.INFO, "Retrieved successfully public IPv4: {0}", publicIpAddressIPv4);
                    break;

                } else {
                    relaxTime();

                }
            }
        }

        //WAZUH service
        startMrClock();
        if (status > 0 && agentParameters.isHasEnableSoc()) {
            LOGGER.info("start WAZUH");
            for (int i = 0; i < AgentConfiguration.MAX_ITERATIONS; i++) {
                if (initializeWazuhService()) {
                    status = 1;
                    setStatus(status);
                    pushMetric("phase2_WAZUH_service_started");
                    LOGGER.log(Level.INFO, "WAZUH Service started successfully");
                    break;

                } else {
                    relaxTime();

                }
            }
        }

        //AUDITD installation
        startMrClock();
        if (status > 0 && agentParameters.isHasEnableSoc() && agentParameters.getSocAuditdEnabled()
                && !("Spider").equalsIgnoreCase(agentParameters.getEuProject())) {
            LOGGER.info("start Auditd");
            for (int i = 0; i < AgentConfiguration.MAX_ITERATIONS; i++) {
                if (initializeAuditdService()) {
                    status = 1;
                    setStatus(status);
                    pushMetric("phase2_AUDITD_service_started");
                    LOGGER.log(Level.INFO, "AUDITD Service started successfully");
                    break;

                } else {
                    relaxTime();

                }
            }
        }

        //AUDITBEAT installation
        startMrClock();
        if (status > 0 && agentParameters.isHasEnableSoc() && agentParameters.getSocAuditdEnabled()
                && ("Spider").equalsIgnoreCase(agentParameters.getEuProject())) {
            LOGGER.info("start Auditbeat");
            for (int i = 0; i < AgentConfiguration.MAX_ITERATIONS; i++) {
                if (initializeAuditbeatService()) {
                    status = 1;
                    setStatus(status);
                    pushMetric("phase2_AUDITBEAT_service_started");
                    LOGGER.log(Level.INFO, "AUDITBEAT Service started successfully");
                    break;

                } else {
                    relaxTime();
                }
            }
        }

        //PACKETBEAT installation
        startMrClock();
        if (status > 0 && agentParameters.isHasEnableSoc() && agentParameters.getSocAuditdEnabled()
                && ("Spider").equalsIgnoreCase(agentParameters.getEuProject())) {
            LOGGER.info("start Packetbeat");
            for (int i = 0; i < AgentConfiguration.MAX_ITERATIONS; i++) {
                if (initializePacketbeatService()) {
                    status = 1;
                    setStatus(status);
                    pushMetric("phase2_PACKETBEAT_service_started");
                    LOGGER.log(Level.INFO, "PACKETBEAT Service started successfully");
                    break;

                } else {
                    relaxTime();
                }
            }
        }

        //push into consul KV machine constants
        startMrClock();
        if (status > 0) {
            for (int i = 0; i < AgentConfiguration.MAX_ITERATIONS; i++) {
                if (setMachineConstantsIntoConsul()) {
                    status = 1;
                    setStatus(status);
                    pushMetric("phase2_push_machine_constants");
                    LOGGER.log(Level.INFO, "Machine Constants pushed successfully on consul KV");
                    break;
                } else {
                    relaxTime();

                }
            }
        }

        //docker login
        startMrClock();
        if (status > 0) {
            for (int i = 0; i < AgentConfiguration.MAX_ITERATIONS; i++) {
                if (loginDocker()) {
                    status = 1;
                    setStatus(status);
                    pushMetric("phase2_docker_login");
                    LOGGER.log(Level.INFO, "Docker login successfully");
                    break;

                } else {
                    relaxTime();

                }
            }
        }

        //netdata registration
        startMrClock();
        if (status > 0) {
            for (int i = 0; i < AgentConfiguration.MAX_ITERATIONS; i++) {
                if (netdataRegistration()) {
                    status = 1;
                    setStatus(status);
                    pushMetric("phase2_register_netdata_service");
                    LOGGER.log(Level.INFO, "Register Netdata Service successfully");
                    break;

                } else {
                    relaxTime();

                }
            }
        }

        if (status > 0) {
            LOGGER.log(Level.INFO, "Status: {0}",
                    SuccessStatus.STATUS_INITIALIZED);
            status = SuccessStatus.STATUS_INITIALIZED.getValue();
            setStatus(status);
        }
    }

    private void phase3() {

        startMrClock();
        if (status > 0) {
            for (int i = 0; i < AgentConfiguration.MAX_ITERATIONS; i++) {
                if (downloadDockerImage()) {
                    status = SuccessStatus.STATUS_INITIALIZED.getValue();
                    setStatus(status);
                    pushMetric("phase3_download_docker_images");
                    LOGGER.log(Level.INFO, "Image downloaded successfully");
                    break;

                } else {
                    relaxTime();

                }
            }
        }

        if (status > 0) {
            LOGGER.log(Level.INFO, "Status: {0}",
                    SuccessStatus.STATUS_IMAGE_DOWNLOADED);
            status = SuccessStatus.STATUS_IMAGE_DOWNLOADED.getValue();
            setStatus(status);

        }
    }

    private void phase4() {
        startMrClock();
        if (status > 0 && agentParameters.isHasDependencies()) {
            LOGGER.log(Level.INFO, "Status: {0}",
                    SuccessStatus.STATUS_WAITING_FOR_DEPENDENCIES);
            status = SuccessStatus.STATUS_WAITING_FOR_DEPENDENCIES.getValue();
            setStatus(status);

            checkIfDependenciesAreReady();
            LOGGER.log(Level.INFO, "Dependencies are resolved");

        }
        pushMetric("phase4_check_for_dependencies");

        if (status > 0 && agentParameters.isLoadBalancer()) {
            startMrClock();
            checkIfLoadbalancerConfigIsReady();
            pushMetric("phase4_loadbalancer_dependencies");
        }

        if (status > 0) {
            startMrClock();
            boolean hasHostnameMappings = checkIfGraphInstancehasHostnameMappings();
            if (hasHostnameMappings) {
                pushMetric("phase4_hostname_mappings");
            }
        }
    }

    private void phase5() {

        startMrClock();
        if (status > 0) {
            for (int i = 0; i < AgentConfiguration.MAX_ITERATIONS; i++) {
                if (startContainerService()) {
                    status = SuccessStatus.STATUS_IMAGE_DOWNLOADED.getValue();
                    setStatus(status);
                    pushMetric("phase5_start_docker_service");
                    LOGGER.log(Level.INFO, "Container start successfully");
                    break;

                } else {
                    relaxTime();

                }
            }
        }

        if (status > 0) {
            LOGGER.log(Level.INFO, "Status: {0}",
                    SuccessStatus.STATUS_TRIGGERED_CONTAINER_START);
            status = SuccessStatus.STATUS_TRIGGERED_CONTAINER_START.getValue();
            setStatus(status);
        }
    }

    private void phase6() {

        startMrClock();
        if (status > 0) {
            for (int i = 0; i < AgentConfiguration.MAX_ITERATIONS; i++) {
                if (checkIfContainerServiceRunning()) {
                    status = SuccessStatus.STATUS_TRIGGERED_CONTAINER_START.getValue();
                    setStatus(status);
                    pushMetric("phase6_check_if_service_ready");
                    LOGGER.log(Level.INFO, "Container-Service running successfully");
                    break;

                } else {
                    relaxTime();

                }
            }
        }

        startMrClock();
        if (status > 0 && agentParameters.isLoadBalancer()) {
            registerTraefik();
            pushMetric("phase6_register_traefik_as_service");
            LOGGER.log(Level.INFO, "Traefik-Service register as individual service");

        }

        if (status > 0) {
            LOGGER.log(Level.INFO, "Status: {0}",
                    SuccessStatus.STATUS_STARTED);
            status = SuccessStatus.STATUS_STARTED.getValue();
            setStatus(status);
        }
    }

    private void phase7() {

        if (status > 0) {
            LOGGER.log(Level.INFO, "Status: {0}",
                    SuccessStatus.STATUS_LISTEN_FOR_COMMAND);
            status = SuccessStatus.STATUS_LISTEN_FOR_COMMAND.getValue();
            setStatus(status);

            ForensicService forensicService = new ForensicService(graphHexId, graphInstanceHexId, componentNodeHexId, componentNodeInstanceHexId, consulClient);
            forensicService.disable();
            listenIntoTopics();
        }
    }

    //--------------------------------------------------------------------------------general

    // update status
    public void setStatus(int status) {
        consulClient.setKVValue(graphHexId + "/" + graphInstanceHexId + "/" + componentNodeHexId + "/"
                + componentNodeInstanceHexId + "/status", "" + status + "");
        LOGGER.log(Level.INFO, "Status --------------------------> {0}", status);
    }

    //wait random time before next iteration
    public static void relaxTime() {
        int waitTime = ThreadLocalRandom.current()
                .nextInt(AgentConfiguration.MIN_WAITING_TIME, AgentConfiguration.MAX_WAITING_TIME);

        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException exception) {
            LOGGER.log(Level.SEVERE, "Error: Sleep failed some how thread interrupted", exception);
            Thread.currentThread().interrupt();
        }
    }

    //push profile metrics
    public void pushMetric(String dimensionName) {
        long endAt = System.currentTimeMillis();
        long executionTime = endAt - startAt;

        if (null != privateIpAddressIPv6
                && !privateIpAddressIPv6.isEmpty()
                && null != agentParameters
                && null != agentParameters.getPrometheusHost()
                && !agentParameters.getPrometheusHost().isEmpty()) {
            String query = "netdata:" + graphHexId + ":" + graphInstanceHexId + ":" + componentNodeHexId
                    + "_agent_profile_execution_milliseconds_average{dimension=\"" + dimensionName + "\", instance=\"" + privateIpAddressIPv6 + ":" + AgentConfiguration.NETDATA_PORT + "\"}";

            String prometheusRestEndpoint
                    = "http://" + agentParameters.getPrometheusHost() + "/api/v1/query?query={query}";

            HttpResponse<String> response = Unirest.get(prometheusRestEndpoint).routeParam("query", query).asString();

            double currentValue = PrometheusMetricParser.jsonParser(response.getBody());

            if (currentValue < 0) {
                String dimensionId = collector.registerDimensionToMetric(metricAgentId, dimensionName);
                collector.logMetric(dimensionId, (int) executionTime);
            } else {
                String dimensionId = collector.registerDimensionToMetric(metricAgentId, dimensionName);
                collector.logMetric(dimensionId, (int) currentValue);
            }
        } else {
            String dimensionId = collector.registerDimensionToMetric(metricAgentId, dimensionName);
            collector.logMetric(dimensionId, (int) executionTime);
        }
    }

    //start timer (MrClock)
    public static void startMrClock() {
        startAt = System.currentTimeMillis();
    }

    //--------------------------------------------------------------------------------phase 2

    // get VM's private ipv4 address
    private boolean getPrivateIpv4() {

        String ipv4Pattern = "(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])";
        Pattern validPattern = Pattern.compile(ipv4Pattern, Pattern.CASE_INSENSITIVE);
        Enumeration<NetworkInterface> networkInterfaces = null;

        try {
            networkInterfaces = NetworkInterface.getNetworkInterfaces();

        } catch (SocketException exception) {
            LOGGER.log(Level.SEVERE, "Error Status: {0}",
                    ErrorStatus.STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_INITIALIZATION_RETRIEVE_IPV4_PRIVATE);
            status = ErrorStatus.STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_INITIALIZATION_RETRIEVE_IPV4_PRIVATE
                    .getValue();
            return false;

        }

        if (networkInterfaces == null) {
            LOGGER.log(Level.SEVERE, "Error Status: {0}",
                    ErrorStatus.STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_INITIALIZATION_RETRIEVE_IPV4_PRIVATE);
            status = ErrorStatus.STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_INITIALIZATION_RETRIEVE_IPV4_PRIVATE
                    .getValue();
            return false;

        }

        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();

            while (addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement();

                if (validPattern.matcher(address.getHostAddress()).matches() && (
                        address.getHostAddress().compareTo(AgentConfiguration.LOCALHOST_IP) != 0) && (
                        address.getHostAddress().compareTo(AgentConfiguration.LOCALHOST_DOCKER_IP) != 0)) {
                    privateIpAddressIPv4 = address.getHostAddress();
                }
            }
        }
        return true;
    }

    // get VM's CJDNS ipv6 address
    private boolean getCjdnsIpV6() {

        Enumeration<NetworkInterface> networkInterfaces = null;

        try {
            networkInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException exception) {
            LOGGER.log(Level.SEVERE, "Error Status: {0}",
                    ErrorStatus.STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_INITIALIZATION_RETRIEVE_IPV6);
            status = ErrorStatus.STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_INITIALIZATION_RETRIEVE_IPV6.getValue();
            return false;
        }

        if (networkInterfaces == null) {
            LOGGER.log(Level.SEVERE, "Error Status: {0}",
                    ErrorStatus.STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_INITIALIZATION_RETRIEVE_IPV6);
            status = ErrorStatus.STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_INITIALIZATION_RETRIEVE_IPV6.getValue();
            return false;
        }

        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();

            while (addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement();

                if (address instanceof Inet6Address && !address.isSiteLocalAddress() && !address
                        .isAnyLocalAddress() && !address.isLinkLocalAddress() && !address.isLoopbackAddress()
                        && !address.isMulticastAddress() && address.getHostAddress().startsWith("fc")) {
                    int endIndex = address.getHostAddress().indexOf('%');
                    privateIpAddressIPv6 = "[" + address.getHostAddress().substring(0, endIndex) + "]";

                }
            }
        }
        return true;
    }

    // initialized consul-java-client
    private boolean initializationConsul() {

        URL url = null;

        try {
            url = new URL("http://" + privateIpAddressIPv6 + ":" + AgentConfiguration.CONSUL_PORT);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            if (con.getResponseCode() == 200) {
                consulClient = new ConsulClient(AgentConfiguration.CONSUL_HOST);

            } else {
                LOGGER.log(Level.SEVERE, "Error Status: {0}",
                        ErrorStatus.STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_INITIALIZATION_OF_CONSUL);
                status = ErrorStatus.STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_INITIALIZATION_OF_CONSUL.getValue();
                return false;

            }

        } catch (IOException exception) {
            LOGGER.log(Level.SEVERE, "Error Status: {0}",
                    ErrorStatus.STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_INITIALIZATION_OF_CONSUL);
            status = ErrorStatus.STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_INITIALIZATION_OF_CONSUL.getValue();
            return false;
        }

        return true;
    }

    //retrieve all object from consul KV
    private boolean retrieveAllObject() {
        Response<GetValue> componentImageJson = null;
        Response<GetValue> agentParametersJson = null;
        Response<GetValue> argumentsJson = null;
        Response<GetValue> dependenciesJson = null;
        Response<GetValue> healthCheckJson = null;
        Response<GetValue> kafkaConfigurationJson = null;

        try {
            componentImageJson = consulClient.getKVValue(
                    graphHexId + "/" + graphInstanceHexId + "/" + componentNodeHexId
                            + "/configurations/componentImage");
            agentParametersJson = consulClient.getKVValue(
                    graphHexId + "/" + graphInstanceHexId + "/" + componentNodeHexId
                            + "/configurations/agentParameters");
            argumentsJson = consulClient.getKVValue(
                    graphHexId + "/" + graphInstanceHexId + "/" + componentNodeHexId
                            + "/configurations/arguments");
            dependenciesJson = consulClient.getKVValue(
                    graphHexId + "/" + graphInstanceHexId + "/" + componentNodeHexId
                            + "/configurations/dependencies");
            healthCheckJson = consulClient.getKVValue(
                    graphHexId + "/" + graphInstanceHexId + "/" + componentNodeHexId
                            + "/configurations/healthcheck");
            kafkaConfigurationJson = consulClient.getKVValue(
                    graphHexId + "/" + graphInstanceHexId + "/" + componentNodeHexId
                            + "/configurations/kafkaConfiguration");

        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Error Status: {0}",
                    ErrorStatus.STATUS_TERMINATED_DUE_TO_BAD_KV_PARSING);
            status = ErrorStatus.STATUS_TERMINATED_DUE_TO_BAD_KV_PARSING.getValue();
            setStatus(status);

            return false;

        }

        if ((componentImageJson.getValue() == null)
                || (agentParametersJson.getValue() == null)
                || (argumentsJson.getValue() == null)
                || (dependenciesJson.getValue() == null)
                || (healthCheckJson.getValue() == null)
                || (kafkaConfigurationJson.getValue() == null)) {

            LOGGER.log(Level.SEVERE, "Error Status: {0}",
                    ErrorStatus.STATUS_TERMINATED_DUE_TO_BAD_KV_PARSING);
            status = ErrorStatus.STATUS_TERMINATED_DUE_TO_BAD_KV_PARSING.getValue();
            setStatus(status);

            return false;
        }

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            componentImage = objectMapper
                    .readValue(componentImageJson.getValue().getDecodedValue(), ComponentImage.class);

            if (componentImage.getPassword() != null && !componentImage.getPassword().isEmpty()) {
                componentImage.setPassword(Encryption.decryption(componentImage.getPassword()));
            }

            agentParameters = objectMapper
                    .readValue(agentParametersJson.getValue().getDecodedValue(), AgentParameters.class);
            arguments = objectMapper
                    .readValue(argumentsJson.getValue().getDecodedValue(), Arguments.class);
            dependencies = objectMapper
                    .readValue(dependenciesJson.getValue().getDecodedValue(), Dependencies.class);
            healthCheck = objectMapper
                    .readValue(healthCheckJson.getValue().getDecodedValue(), HealthCheck.class);
            kafkaConfiguration = objectMapper
                    .readValue(kafkaConfigurationJson.getValue().getDecodedValue(), KafkaConfiguration.class);

        } catch (IOException exception) {
            LOGGER.log(Level.SEVERE, "Error Status: {0}",
                    ErrorStatus.STATUS_TERMINATED_DUE_TO_BAD_KV_PARSING);
            status = ErrorStatus.STATUS_TERMINATED_DUE_TO_BAD_KV_PARSING.getValue();
            setStatus(status);

            return false;
        }

        return true;
    }

    // get VM's public ipv4 address
    private boolean getPublicIpv4() {

        // TODO this url works only for openstack!
        while (agentParameters.isHasEnablePublicIpV4()) {
            try {
                URL url = new URL(AgentConfiguration.OPENSTACK_URL);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer content = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                con.disconnect();

                publicIpAddressIPv4 = content.toString();

                if (null == publicIpAddressIPv4 || publicIpAddressIPv4.isEmpty()) {
                    relaxTime();
                } else {
                    return true;
                }

            } catch (Exception exception) {
                LOGGER.log(Level.SEVERE, "Error Status: {0}",
                        ErrorStatus.STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_INITIALIZATION_RETRIEVE_IPV4_PUBLIC);
                status = ErrorStatus.STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_INITIALIZATION_RETRIEVE_IPV4_PUBLIC.getValue();
                return false;
            }
        }

        return true;
    }

    // set and start Wazuh
    private boolean initializeWazuhService() {

        // run WAZUH agent-auth
        String[] cmd = {
                "/bin/sh",
                "-c",
                "curl -so wazuh-agent.deb "
                        + agentParameters.getSocAgentUrl() + " && "
                        + "sudo WAZUH_MANAGER_IP='" + agentParameters.getSocManagerIp() + "'  dpkg -i ./wazuh-agent.deb && "
                        + "sudo systemctl daemon-reload && "
                        + "sudo systemctl enable wazuh-agent && "
                        + "sudo systemctl start wazuh-agent "
                //"sudo /var/ossec/bin/agent-auth -I " + publicIpAddressIPv4 + " -m " + agentParameters.getSocManagerIp()
                //        + " -v /var/ossec/etc/rootCA.pem"
        };
        LOGGER.log(Level.INFO, "Wazuh Agent command: {0}", cmd[2]);
        String output = CommandLineExecutor.multiLine(cmd);

        if (output.contains("error")) {
            System.out.println("--->" + output);
            return false;
        }

        //         Edit the Wazuh agenEdit the Wazuh agent configuration t configuration
        //        String content = null;
        //        try {
        //            content = new String(Files.readAllBytes(Paths.get("/var/ossec/etc/ossec.conf")));
        //
        //            content = content.replace("      <address></address>", "      <address>" + agentParameters.getSocManagerIp() + "</address>");
        //
        //            BufferedWriter writer = new BufferedWriter(new FileWriter("/var/ossec/etc/ossec.conf"));
        //            writer.write(content);
        //            writer.flush();
        //            writer.close();
        //
        //        } catch (IOException exception) {
        //            exception.printStackTrace();
        //            return false;
        //        }

        //         start service
        //        cmd[2] = "sudo service wazuh-agent start";
        //        output = CommandLineExecutor.multiLine(cmd);

        return true;
    }

    //    // set and start Auditd
    private boolean initializeAuditdService() {

        List<String> command = new ArrayList<>();
        String output;
        // Install auditd and configure it
        command.add("/bin/sh");
        command.add("-c");
        command.add("sudo apt-get update && sudo apt-get install -y auditd && echo 'wazuh_command.remote_commands=1' | "
                + "sudo tee  /var/ossec/etc/local_internal_options.conf && "
                + "sleep 5 && sudo service auditd start && "
                + "sleep 5 && sudo auditctl -w /home -p w -k audit-wazuh-w  && "
                + "sudo auditctl -w /home -p a -k audit-wazuh-a && "
                + "sudo auditctl -w /home -p r -k audit-wazuh-r && "
                + "sudo auditctl -w /home -p x -k audit-wazuh-x && "
                + "sudo service wazuh-agent restart");

        try {
            output = CommandLineExecutor.multiLine(command.toArray(new String[command.size()]));
            if (output.contains("error") || output.contains("Can't open")) {
                LOGGER.log(Level.SEVERE, output);
                return false;
            } else {
                LOGGER.log(Level.INFO, output);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            return false;
        }

        // Configure audit rules in order to be able to get audit all executed commands
        command = new ArrayList<>();
        command.add("/bin/sh");
        command.add("-c");
        command.add("sudo echo '"
                //                "-a exit,always -F arch=b64 -F euid=0 -S execve -k rootact\n" +
                //                "-a exit,always -F arch=b32 -F euid=0 -S execve -k rootact\n" +
                //                "-a exit,always -F arch=b64 -F euid>=1000 -S execve -k useract\n" +
                //                "-a exit,always -F arch=b32 -F euid>=1000 -S execve -k useract\n\n" +
                //                "-a exit,always -F arch=b64 -S execve\n" +
                //                "-a exit,always -F arch=b32 -S execve\n" +
                //                "-a exit,always -F arch=b64 -S read\n" +
                //                "-a exit,always -F arch=b64 -S open' | " +
                + "-a exit,always  -F dir=" + AgentConfiguration.HOST_WORKING_DIRECTORY + " -F perm=warx' | "
                + "sudo tee -a /etc/audit/rules.d/audit.rules");

        try {
            output = CommandLineExecutor.multiLine(command.toArray(new String[command.size()]));
            if (output.contains("error") || output.contains("Can't open")) {
                LOGGER.log(Level.SEVERE, output);
                return false;
            } else {
                LOGGER.log(Level.INFO, output);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            return false;
        }

        //confiqure auditd
        command = new ArrayList<>();
        command.add("/bin/sh");
        command.add("-c");
        command.add("sudo sed -i '0,/max_log_file =/{s/max_log_file = .*/max_log_file = 100/}' /etc/audit/auditd.conf && "
                + "sudo sed -i '0,/num_logs =/{s/num_logs = .*/num_logs = 0/}' /etc/audit/auditd.conf && "
                + "sudo sed -i '0,/space_left =/{s/space_left = .*/space_left = 1000/}' /etc/audit/auditd.conf && "
                + "sudo sed -i '0,/space_left_action =/{s/space_left_action = .*/space_left_action = ROTATE/}' /etc/audit/auditd.conf ");

        try {
            output = CommandLineExecutor.multiLine(command.toArray(new String[command.size()]));
            if (output.contains("error") || output.contains("Can't open")) {
                LOGGER.log(Level.SEVERE, output);
                return false;
            } else {
                LOGGER.log(Level.INFO, output);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            return false;
        }

        String configureScript = "sudo sed -i -z 's/<\\\\/ossec_config>\\\\n\\\\n<ossec_config>"
                + "/  <localfile>\\\\n    <log_format>audit<\\\\/log_format>\\\\n    <location>\\\\/var\\\\/log\\\\/audit\\\\/audit.*<\\\\/location>\\\\n"
                + "  <\\\\/localfile>\\\\n\\\\n"
                + "<\\\\/ossec_config>\\\\n\\\\n<ossec_config>\\\\n  "
                + "<localfile>\\\\n    <log_format>audit<\\\\/log_format>\\\\n    <location>\\\\/var\\\\/log\\\\/audit\\\\/audit.*<\\\\/location>\\\\n "
                + " <\\\\/localfile>\\\\n\\\\n"
                + "  <localfile>\\\\n    <log_format>syslog<\\\\/log_format>\\\\n "
                + "   <location>\\\\/var\\\\/lib\\\\/docker\\\\/containers\\\\/*\\\\/*.log<\\\\/location>\\\\n  <\\\\/localfile>\\\\n/' "
                + "/var/ossec/etc/ossec.conf\n\n";

        // Configure wazuh-agent
        command = new ArrayList<>();
        command.add("/bin/sh");
        command.add("-c");
        command.add("sudo sed -i -z "
                + "'s/<\\/ossec_config>\\n\\n<ossec_config>/"
                + "  <localfile>\\n"
                + "    <log_format>audit<\\/log_format>\\n"
                + "    <location>\\/var\\/log\\/audit\\/audit.*<\\/location>\\n"
                + "  <\\/localfile>\\n\\n"
                + "<\\/ossec_config>\\n\\n"
                + "<ossec_config>\\n"
                + "  <localfile>\\n"
                + "    <log_format>audit<\\/log_format>\\n"
                + "    <location>\\/var\\/log\\/audit\\/audit.*<\\/location>\\n"
                + "  <\\/localfile>\\n\\n"
                + "  <localfile>\\n"
                + "    <log_format>syslog<\\/log_format>\\n"
                + "    <location>\\/var\\/lib\\/docker\\/containers\\/*\\/*.log<\\/location>\\n"
                + "  <\\/localfile>\\n/' "
                + "/var/ossec/etc/ossec.conf\n\n");

        try {
            output = CommandLineExecutor.multiLine(command.toArray(new String[command.size()]));
            if (output.contains("error") || output.contains("Can't open")) {
                LOGGER.log(Level.SEVERE, output);
                return false;
            } else {
                LOGGER.log(Level.INFO, output);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            return false;
        }

        // Restart auditd and wazuh-agent to get the new configurations
        command = new ArrayList<>();
        command.add("/bin/sh");
        command.add("-c");
        command.add("sudo service auditd restart && "
                + "sudo service wazuh-agent restart");

        try {
            output = CommandLineExecutor.multiLine(command.toArray(new String[command.size()]));
            if (output.contains("error") || output.contains("Can't open")) {
                LOGGER.log(Level.SEVERE, output);
                return false;
            } else {
                LOGGER.log(Level.INFO, output);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            return false;
        }

        // Install auditbeat
        command = new ArrayList<>();
        command.add("/bin/sh");
        command.add("-c");
        command.add("curl -L " + AgentConfiguration.BEATS_DOWNLOAD_BASE_URL + "/auditbeat/auditbeat-"
                + AgentConfiguration.AUDITBEAT_LEGACY_VERSION + "-amd64.deb -o /tmp/audibeat.deb && "
                + "sudo dpkg -i /tmp/audibeat.deb");

        try {
            output = CommandLineExecutor.multiLine(command.toArray(new String[command.size()]));
            if (output.contains("error") || output.contains("Can't open")) {
                LOGGER.log(Level.SEVERE, output);
                return false;
            } else {
                LOGGER.log(Level.INFO, output);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            return false;
        }

        // Configure AuditBeat auditbeat
        command = new ArrayList<>();
        command.add("/bin/sh");
        command.add("-c");
        command.add("sed -i -z 's/output.elasticsearch:/#output.elasticsearch:/' /etc/auditbeat/auditbeat.yml && "
                + "sed -i -z 's/hosts: \\[\"localhost:9200\"]/#hosts: \\[\"localhost:9200\"]/' /etc/auditbeat/auditbeat.yml && "
                + "echo 'output.kafka:\\n  hosts: [\"" + AgentConfiguration.AUDIT_LOGS_KAFKA_BOOTSTRAP + "\"]\\n  topic: \""
                + AgentConfiguration.AUDIT_LOGS_KAFKA_TOPIC + "\"' >> /etc/auditbeat/auditbeat.yml && "
                + "sudo service auditbeat restart");

        try {
            output = CommandLineExecutor.multiLine(command.toArray(new String[command.size()]));
            if (output.contains("error") || output.contains("Can't open")) {
                LOGGER.log(Level.SEVERE, output);
                return false;
            } else {
                LOGGER.log(Level.INFO, output);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            return false;
        }

        //create file for spider tests
        command = new ArrayList<>();
        command.add("/bin/sh");
        command.add("-c");
        command.add("echo true > " + AgentConfiguration.HOST_WORKING_DIRECTORY + "/flag.txt && "
                + "sudo chown root " + AgentConfiguration.HOST_WORKING_DIRECTORY + "/flag.txt && "
                + "sudo chmod 600 " + AgentConfiguration.HOST_WORKING_DIRECTORY + "/flag.txt && "
                + "sudo cat " + AgentConfiguration.HOST_WORKING_DIRECTORY + "/flag.txt ");

        try {
            output = CommandLineExecutor.multiLine(command.toArray(new String[command.size()]));
            if (output.contains("error") || output.contains("Can't open")) {
                LOGGER.log(Level.SEVERE, output);
                return false;
            } else {
                LOGGER.log(Level.INFO, output);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            return false;
        }

        return true;
    }

    private boolean initializeAuditbeatService() {

        List<String> command = new ArrayList<>();
        String output;
        command.add("/bin/sh");
        command.add("-c");
        command.add("curl -L " + AgentConfiguration.BEATS_DOWNLOAD_BASE_URL + "/auditbeat/auditbeat-"
                + AgentConfiguration.AUDITBEAT_VERSION + "-amd64.deb -o /tmp/auditbeat.deb && "
                + "sudo dpkg -i /tmp/auditbeat.deb && "
                + "sudo rm -f /tmp/auditbeat.deb ");

        try {
            output = CommandLineExecutor.multiLine(command.toArray(new String[command.size()]));
            if (output.contains("error") || output.contains("Can't open")) {
                LOGGER.log(Level.SEVERE, output);
                return Boolean.FALSE;
            } else {
                LOGGER.log(Level.INFO, output);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            return Boolean.FALSE;
        }

        try {
            Files.copy(getClass().getResourceAsStream("/configfiles/auditbeat.yml"), Paths.get("/etc/auditbeat/auditbeat.yml"),
                    StandardCopyOption.REPLACE_EXISTING);
            Files.copy(getClass().getResourceAsStream("/configfiles/auditbeat-rules.conf"), Paths.get("/etc/auditbeat/audit.rules.d/auditbeat-rules.conf"),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            return Boolean.FALSE;
        }

        String kafkaSocUrl = "sudo sed -i 's/hosts: \\[ kafka_server ]/hosts: [ \""
                + kafkaConfiguration.getKafkaServerSoc()
                + "\" ]/' /etc/auditbeat/auditbeat.yml && ";
        command = new ArrayList<>();
        command.add("/bin/sh");
        command.add("-c");
        command.add(kafkaSocUrl
                + "sudo chmod 644 /etc/auditbeat/auditbeat.yml && "
                + "sudo chown root:root /etc/auditbeat/auditbeat.yml && "
                + "sudo chmod 644 /etc/auditbeat/audit.rules.d/auditbeat-rules.conf && "
                + "sudo chown root:root /etc/auditbeat/audit.rules.d/auditbeat-rules.conf && "
                + "sudo systemctl enable auditbeat && "
                + "sudo systemctl start auditbeat ");

        try {
            output = CommandLineExecutor.multiLine(command.toArray(new String[command.size()]));
            if (output.contains("error") || output.contains("Can't open")) {
                LOGGER.log(Level.SEVERE, output);
                return Boolean.FALSE;
            } else {
                LOGGER.log(Level.INFO, output);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            return Boolean.FALSE;
        }

        return Boolean.TRUE;

    }

    private boolean initializePacketbeatService() {

        List<String> command = new ArrayList<>();
        String output;
        command.add("/bin/sh");
        command.add("-c");
        command.add("curl -L " + AgentConfiguration.BEATS_DOWNLOAD_BASE_URL + "/packetbeat/packetbeat-"
                + AgentConfiguration.PACKETBEAT_VERSION + "-amd64.deb -o /tmp/packetbeat.deb && "
                + "sudo dpkg -i /tmp/packetbeat.deb && "
                + "sudo rm -f /tmp/packetbeat.deb ");

        try {
            output = CommandLineExecutor.multiLine(command.toArray(new String[command.size()]));
            if (output.contains("error") || output.contains("Can't open")) {
                LOGGER.log(Level.SEVERE, output);
                return Boolean.FALSE;
            } else {
                LOGGER.log(Level.INFO, output);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            return Boolean.FALSE;
        }

        try {
            Files.copy(getClass().getResourceAsStream("/configfiles/packetbeat.yml"), Paths.get("/etc/packetbeat/packetbeat.yml"),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            return Boolean.FALSE;
        }

        String kafkaSocUrl = "sudo sed -i 's/hosts: \\[ kafka_server ]/hosts: [ \""
                + kafkaConfiguration.getKafkaServerSoc()
                + "\" ]/' /etc/packetbeat/packetbeat.yml && ";
        command = new ArrayList<>();
        command.add("/bin/sh");
        command.add("-c");
        command.add(kafkaSocUrl
                + "sudo chmod 644 /etc/packetbeat/packetbeat.yml && "
                + "sudo chown root:root /etc/packetbeat/packetbeat.yml && "
                + "sudo systemctl enable packetbeat && "
                + "sudo systemctl start packetbeat ");

        try {
            output = CommandLineExecutor.multiLine(command.toArray(new String[command.size()]));
            if (output.contains("error") || output.contains("Can't open")) {
                LOGGER.log(Level.SEVERE, output);
                return Boolean.FALSE;
            } else {
                LOGGER.log(Level.INFO, output);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            return Boolean.FALSE;
        }

        return Boolean.TRUE;

    }


    // set machine constants into consul KV
    public boolean setMachineConstantsIntoConsul() {

        machineConstants = new MachineConstants();

        ObjectMapper objectMapper = new ObjectMapper();
        String text = null;

        try {
            text = objectMapper.writeValueAsString(machineConstants);
            consulClient.setKVValue(
                    "maestro/machinesConstants/" + graphHexId + "/" + graphInstanceHexId + "/"
                            + componentNodeHexId + "/" + componentNodeInstanceHexId, text);

        } catch (JsonProcessingException exception) {
            LOGGER.log(Level.SEVERE, "Error Status: {0}",
                    ErrorStatus.STATUS_TERMINATED_DUE_TO_BAD_KV_PARSING);
            status = ErrorStatus.STATUS_TERMINATED_DUE_TO_BAD_KV_PARSING.getValue();
            setStatus(status);
            return false;

        }

        for (String key : machineConstants.getLscpu().keySet()) {

            if (key.compareTo("Thread(s) per core") == 0) {

                String dimensionId = collector.registerDimensionToMetric(benchmarkId, key);
                collector.logMetric(dimensionId, Integer.parseInt(machineConstants.getLscpu().get(key)));

            } else if (key.compareTo("BogoMIPS") == 0) {

                Double temp = Double.parseDouble(machineConstants.getLscpu().get(key));
                String dimensionId = collector.registerDimensionToMetric(benchmarkId, key);
                collector.logMetric(dimensionId, temp.intValue());

            } else if (key.compareTo("CPU(s)") == 0) {

                String dimensionId = collector.registerDimensionToMetric(benchmarkId, key);
                collector.logMetric(dimensionId, Integer.parseInt(machineConstants.getLscpu().get(key)));

            } else if (key.compareTo("Core(s) per socket") == 0) {

                String dimensionId = collector.registerDimensionToMetric(benchmarkId, key);
                collector.logMetric(dimensionId, Integer.parseInt(machineConstants.getLscpu().get(key)));

            } else if (key.compareTo("L3 cache") == 0) {
                String temp = "";
                if (machineConstants.getLscpu().get(key).contains("MiB") || machineConstants.getLscpu().get(key).contains("KiB")) {
                    temp = machineConstants.getLscpu().get(key).substring(0, machineConstants.getLscpu().get(key).length() - 4);
                } else if (machineConstants.getLscpu().get(key).contains("M") || machineConstants.getLscpu().get(key).contains("K")) {
                    temp = machineConstants.getLscpu().get(key).substring(0, machineConstants.getLscpu().get(key).length() - 1);
                } else {
                    temp = machineConstants.getLscpu().get(key);
                }

                String dimensionId = collector.registerDimensionToMetric(benchmarkId, key);
                collector.logMetric(dimensionId, Integer.parseInt(temp));
            }
        }

        String dimensionRamId = collector.registerDimensionToMetric(benchmarkId, "ram");
        Double temp = Double.parseDouble(machineConstants.getRam());
        temp = Math.ceil(temp);
        collector.logMetric(dimensionRamId, temp.intValue());

        String[] cmd = {
                "/bin/sh",
                "-c",
                ""
        };

        cmd[2] = "dd if=/dev/zero of=" + AgentConfiguration.HOST_WORKING_DIRECTORY
                + "/test bs=64k count=16k conv=fdatasync";
        String output = CommandLineExecutor.multiLine(cmd);

        int position = output.indexOf("s,");
        String value = output.substring(position + 3, output.length() - 6);
        Double diskInputOutputValue = Double.parseDouble(value);

        String dimensionDiskId = collector.registerDimensionToMetric(benchmarkId, "Disk I/O");
        collector.logMetric(dimensionDiskId, diskInputOutputValue.intValue());

        cmd[2] = "sudo rm -rf test";
        CommandLineExecutor.multiLine(cmd);

        return true;
    }

    // login docker
    private boolean loginDocker() {

        if (componentImage.getUrl() == null
                || componentImage.getPassword() == null
                || componentImage.getPassword().isEmpty()
                || componentImage.getUserName() == null
                || componentImage.getUserName().isEmpty()) {

            return true;
        }

        String[] cmd = {
                "/bin/sh",
                "-c",
                "docker login " + componentImage.getUrl() + " -u " + componentImage.getUserName() + " -p"
                        + componentImage.getPassword()
        };

        String output = CommandLineExecutor.multiLine(cmd);

        if (output.contains("Login Succeeded")) {
            return true;

        } else {
            LOGGER.log(Level.SEVERE, "Error Status: {0}",
                    ErrorStatus.STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_INITIALIZATION_OF_LOGIN_DOCKER);
            status = ErrorStatus.STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_INITIALIZATION_OF_LOGIN_DOCKER.getValue();
            setStatus(status);
            return false;

        }

    }

    //register netdata service if service is running
    private boolean netdataRegistration() {

        URL url = null;

        try {

            url = new URL("http://" + privateIpAddressIPv6 + ":" + AgentConfiguration.NETDATA_PORT + "/api/v1/allmetrics");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            if (con.getResponseCode() == 200) {
                NewService newService = new NewService();
                newService.setId("netdataID");
                List<String> tagList = new ArrayList();
                tagList.add("metrics");
                tagList.add(machineConstants.getOperatingSystemArchitecture());
                newService.setTags(tagList);
                newService.setName("netdata");
                newService.setPort(AgentConfiguration.NETDATA_PORT);
                newService.setAddress(privateIpAddressIPv6);

                NewService.Check serviceCheck = new NewService.Check();
                serviceCheck.setHttp("http://" + privateIpAddressIPv6 + ":" + AgentConfiguration.NETDATA_PORT + "/api/v1/allmetrics");
                serviceCheck.setInterval("5s");
                serviceCheck.setDeregisterCriticalServiceAfter("5m");
                newService.setCheck(serviceCheck);

                consulClient.agentServiceRegister(newService);

            } else {
                LOGGER.log(Level.SEVERE, "Error Status: {0}",
                        ErrorStatus.STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_INITIALIZATION_OF_NETDATA_REGISTRATION);
                status = ErrorStatus.STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_INITIALIZATION_OF_NETDATA_REGISTRATION.getValue();
                setStatus(status);
                return false;

            }
        } catch (IOException exception) {
            LOGGER.log(Level.SEVERE, "Error Status: {0}",
                    ErrorStatus.STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_INITIALIZATION_OF_NETDATA_REGISTRATION);
            status = ErrorStatus.STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_INITIALIZATION_OF_NETDATA_REGISTRATION.getValue();
            setStatus(status);
            return false;

        }

        return true;
    }

    //--------------------------------------------------------------------------------phase 3

    //download image
    private boolean downloadDockerImage() {
        LOGGER.log(Level.INFO, "Download image: " + componentImage.getServiceImage());

        //docker pull for private registry
        if (componentImage.getUrl() != null && !componentImage.getUrl().isEmpty()) {
            LOGGER.log(Level.INFO,
                    "Download image: " + componentImage.getUrl() + "/" + componentImage.getServiceImage());
            dockerImage = componentImage.getUrl() + "/" + componentImage.getServiceImage();

            CommandLineExecutor.singleLine(
                    "docker pull " + componentImage.getUrl() + "/" + componentImage.getServiceImage());

            String[] cmd = {
                    "/bin/sh",
                    "-c",
                    "docker images"
            };
            String output = CommandLineExecutor.multiLine(cmd);

            String imageName = componentImage.getServiceImage().split(":")[0];
            if (!output.contains(componentImage.getUrl() + "/" + imageName)) {
                LOGGER.log(Level.SEVERE, "Error Status: {0}",
                        ErrorStatus.STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_IMAGE_NOT_FOUND);
                status = ErrorStatus.STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_IMAGE_NOT_FOUND.getValue();
                setStatus(status);
                return false;
            } else {
                return true;
            }

        } else {
            LOGGER.log(Level.INFO, "Download image: " + componentImage.getServiceImage());
            dockerImage = componentImage.getServiceImage();

            //docker pull from official Docker Hub
            CommandLineExecutor.singleLine(
                    "docker pull " + componentImage.getServiceImage());

            String[] cmd = {
                    "/bin/sh",
                    "-c",
                    "docker images"
            };
            String output = CommandLineExecutor.multiLine(cmd);

            String imageName = componentImage.getServiceImage().split(":")[0];
            if (!output.contains(imageName)) {
                LOGGER.log(Level.SEVERE, "Error Status: {0}",
                        ErrorStatus.STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_IMAGE_NOT_FOUND);
                status = ErrorStatus.STATUS_TERMINATED_DUE_TO_UNSUCCESSFUL_IMAGE_NOT_FOUND.getValue();
                setStatus(status);
                return false;
            } else {
                return true;
            }

        }

    }

    //--------------------------------------------------------------------------------phase 4

    //wait until all services are up and running
    private void checkIfDependenciesAreReady() {

        LOGGER.log(Level.INFO,
                "This service has component dependencies: " + dependencies.getDependsOnComponent());

        for (String serviceName : dependencies.getDependsOnComponent()) {
            boolean resolvedDependencies = false;

            while (!resolvedDependencies) {
                Response<List<HealthService>> healthyServices = consulClient
                        .getHealthServices(graphHexId + ":" + graphInstanceHexId + ":" + serviceName, true,
                                QueryParams.DEFAULT);

                if (!healthyServices.getValue().isEmpty()) {
                    LOGGER.log(Level.INFO, "Resolved dependency {0}", serviceName);
                    resolvedDependencies = true;
                }
            }
        }

    }

    //wait until load balancer workers are ready
    private boolean checkIfLoadbalancerConfigIsReady() {
        Response<GetValue> loadbalancer = consulClient.getKVValue(
                graphHexId + "/" + graphInstanceHexId + "/" + componentNodeHexId + "/"
                        + componentNodeInstanceHexId + "/lbStatus");

        if (loadbalancer.getValue() == null) {
            LOGGER.log(Level.SEVERE, "Error Status: {0}",
                    ErrorStatus.STATUS_TERMINATED_DUE_TO_BAD_KV_PARSING);
            status = ErrorStatus.STATUS_TERMINATED_DUE_TO_BAD_KV_PARSING.getValue();
            setStatus(status);

            return false;
        }

        while (loadbalancer.getValue().getDecodedValue().compareTo("0") == 0) {
            loadbalancer = consulClient.getKVValue(
                    graphHexId + "/" + graphInstanceHexId + "/" + componentNodeHexId + "/"
                            + componentNodeInstanceHexId + "/lbStatus");
        }
        return true;
    }

    //wait until load balancer workers are ready
    private boolean checkIfGraphInstancehasHostnameMappings() {
        Response<GetValue> hostnameMappingsStatus = consulClient.getKVValue(
                graphHexId + "/" + graphInstanceHexId + "/host/status");

        if (hostnameMappingsStatus.getValue().getDecodedValue().compareTo(HostnameMappingsStatus.STATUS_PROCEED
                .getValue() + "") == 0) {
            return false;
        }

        while (hostnameMappingsStatus.getValue().getDecodedValue().compareTo(HostnameMappingsStatus.STATUS_WAIT.getValue() + "") == 0) {
            hostnameMappingsStatus = consulClient.getKVValue(
                    graphHexId + "/" + graphInstanceHexId + "/host/status");
        }

        if (hostnameMappingsStatus.getValue().getDecodedValue().compareTo(HostnameMappingsStatus.STATUS_QUERY.getValue() + "") == 0) {
            Response<GetValue> hostnameMappingsJson = null;

            try {
                hostnameMappingsJson = consulClient.getKVValue(
                        graphHexId + "/" + graphInstanceHexId + "/host/mappings");

                if (null != hostnameMappingsJson) {

                    ObjectMapper objectMapper = new ObjectMapper();

                    hostnameMappings = objectMapper.readValue(hostnameMappingsJson.getValue().getDecodedValue(), HostnameMappings.class);

                    return true;

                } else {
                    throw new Exception();
                }

            } catch (Exception exception) {
                LOGGER.log(Level.SEVERE, "Error Status: {0}",
                        ErrorStatus.STATUS_TERMINATED_DUE_TO_BAD_KV_PARSING);
                status = ErrorStatus.STATUS_TERMINATED_DUE_TO_BAD_KV_PARSING.getValue();
                setStatus(status);

                return false;
            }
        }

        LOGGER.log(Level.SEVERE, "Error Status: {0}",
                ErrorStatus.STATUS_TERMINATED_DUE_TO_BAD_KV_PARSING);
        status = ErrorStatus.STATUS_TERMINATED_DUE_TO_BAD_KV_PARSING.getValue();
        setStatus(status);

        return false;
    }

    //--------------------------------------------------------------------------------phase 5


    // trigger container to start
    private boolean startContainerService() {

        ArrayList<String> enviroments = new ArrayList<>();

        for (String env : arguments.getEnvs()) {
            String[] stringTable = env.split("=");
            if (stringTable[1].startsWith("@")) {
                int beginString = env.indexOf('@');
                String componentName = env.substring(beginString + 1);

                boolean resolvedDependencies = false;

                if (componentName.equals("IPV4_PRIVATE")) {
                    String newEnv = env.replace("@" + componentName, privateIpAddressIPv4);
                    enviroments.add(newEnv);

                    resolvedDependencies = true;
                }

                if (componentName.equals("IPV4_PUBLIC")) {
                    String newEnv = env.replace("@" + componentName, publicIpAddressIPv4);
                    enviroments.add(newEnv);

                    resolvedDependencies = true;
                }

                if (componentName.equals("IPV6_PRIVATE")) {
                    String newEnv = env.replace("@" + componentName, privateIpAddressIPv6);
                    enviroments.add(newEnv);

                    resolvedDependencies = true;
                }

                while (!resolvedDependencies) {
                    Response<List<HealthService>> healthyServices = consulClient
                            .getHealthServices(graphHexId + ":" + graphInstanceHexId + ":" + componentName, true,
                                    QueryParams.DEFAULT);

                    if (!healthyServices.getValue().isEmpty()) {
                        String address = healthyServices.getValue().get(0).getService().getAddress();

                        if (address.contains("[") && address.contains("]")) {
                            address = address.substring(1, address.length() - 1);

                        }

                        String newEnv = env.replace("@" + componentName, address);
                        enviroments.add(newEnv);

                        resolvedDependencies = true;
                    }
                }

            } else if (stringTable[1].startsWith("#")) {
                int beginString = env.indexOf('#');
                String componentName = env.substring(beginString + 1);

                boolean resolvedDependencies = false;

                while (!resolvedDependencies) {
                    Response<List<HealthService>> healthyServices = consulClient
                            .getHealthServices(graphHexId + ":" + graphInstanceHexId + ":" + componentName, true,
                                    QueryParams.DEFAULT);

                    if (!healthyServices.getValue().isEmpty()) {
                        String address = healthyServices.getValue().get(0).getService().getAddress();
                        String newEnv = env.replace("#" + componentName, address);
                        enviroments.add(newEnv);

                        resolvedDependencies = true;
                    }
                }

            } else {
                enviroments.add(env);
            }
        }

        CommandLineExecutor.singleLine("docker rm " + componentNodeHexId + " --force");

        StringBuilder dockerCommand = new StringBuilder();

        dockerCommand = dockerCommand.append("docker run -d");

        if (null != arguments.getUser()
                && !arguments.getUser().isEmpty()) {
            dockerCommand = dockerCommand.append(" --user=\"" + arguments.getUser() + "\"");
        }

        if (null != arguments.getUlimitMemlock()
                && !arguments.getUlimitMemlock().isEmpty()) {
            dockerCommand = dockerCommand
                    .append(" --ulimit memlock=" + arguments.getUlimitMemlock());
        }

        //add volumes
        if (null != arguments.getVolumes()
                && !arguments.getVolumes().isEmpty()) {
            for (String key : arguments.getVolumes().keySet()) {

                String[] test = key.split("/");

                StringBuilder hostVolume = new StringBuilder();
                hostVolume = hostVolume.append(AgentConfiguration.ROOT_HOST_VOLUME_PATH);

                for (int i = 3; i < test.length; i++) {
                    hostVolume = hostVolume.append(test[i] + "/");
                }

                Path hostVolumePath = Paths.get(hostVolume.toString());

                if (!Files.exists(hostVolumePath)) {
                    try {
                        Files.createDirectories(hostVolumePath);
                    } catch (IOException exception) {
                        LOGGER.log(Level.SEVERE, "Error Status: {0}", ErrorStatus.STATUS_TERMINATED_CONTAINER_SERVICE_IS_NOT_RUNNING);
                        status = ErrorStatus.STATUS_TERMINATED_CONTAINER_SERVICE_IS_NOT_RUNNING.getValue();
                        return false;
                    }
                }

                System.out.println("------> " + "sudo mount " + agentParameters.getNfsServerHost() + ":" + key + " " + hostVolume.toString());
                CommandLineExecutor.singleLine("sudo mount " + agentParameters.getNfsServerHost() + ":" + key + " " + hostVolume.toString());

                String dockerVolume = arguments.getVolumes().get(key);
                dockerCommand = dockerCommand.append(" -v " + hostVolume.toString() + ":" + dockerVolume);
            }
        }

        //add network mode host
        if (agentParameters.isHasEnableIpv6()
                || arguments.getNetworkModeHost()) {
            dockerCommand = dockerCommand.append(" --network host ");

        }

        //add hostname
        if (arguments.getHostname() != null
                && !arguments.getHostname().isEmpty()) {
            dockerCommand = dockerCommand.append(" --hostname " + arguments.getHostname());
        }

        //add other hostname of graph instance
        if (null != hostnameMappings
                && !hostnameMappings.getEntries().isEmpty()) {
            for (String hostnameMapping : hostnameMappings.getEntries()) {
                dockerCommand = dockerCommand.append(" --add-host=" + hostnameMapping);
            }
        }

        //add privileged
        if (arguments.getPrivileged()) {
            dockerCommand = dockerCommand.append(" --privileged");
        }

        //add sharedMemorySize
        if (arguments.getSharedMemorySize() != null
                && !arguments.getSharedMemorySize().isEmpty()) {
            dockerCommand = dockerCommand.append(" --shm_size " + arguments.getSharedMemorySize());
        }

        //add container name
        dockerCommand = dockerCommand.append(" --name " + componentNodeHexId);

        //add ports
        for (String port : arguments.getPorts()) {
            dockerCommand = dockerCommand.append(" -p " + port);
        }

        //add envs
        for (String env : enviroments) {
            dockerCommand = dockerCommand.append(" -e " + env);
            dockerEnvironmentVariablesKeysList.add(env.split("=")[0]);
        }

        //add capabilities devices
        for (String device : arguments.getDevice()) {
            dockerCommand = dockerCommand.append(" --device  " + device);

        }

        //add capabilities add
        for (String capabilityAdd : arguments.getCapabilityAdds()) {
            dockerCommand = dockerCommand.append(" --cap-add=" + capabilityAdd);

        }

        //add capabilities drop
        for (String capabilityDrop : arguments.getCapabilityDrops()) {
            dockerCommand = dockerCommand.append(" --cap-drop=" + capabilityDrop);

        }

        //add images
        if (componentImage.getUrl() != null
                && !componentImage.getUrl().isEmpty()) {
            dockerCommand = dockerCommand
                    .append(" " + componentImage.getUrl() + "/" + componentImage.getServiceImage());

        } else {
            dockerCommand = dockerCommand
                    .append(" " + componentImage.getServiceImage());

        }

        //add command
        for (String command : arguments.getCommands()) {
            dockerCommand = dockerCommand.append(" " + command);

        }

        String[] cmd = {
                "/bin/sh",
                "-c",
                dockerCommand.toString()
                //will be filled by cmdappend
        };

        CommandLineExecutor.multiLine(cmd);
        LOGGER.log(Level.INFO, "{0}", dockerCommand);

        return true;
    }

    //--------------------------------------------------------------------------------phase 6

    // check if service is running
    private boolean checkIfContainerServiceRunning() {

        String[] cmd = {
                "/bin/sh",
                "-c",
                "docker ps -a | grep " + componentNodeHexId + " | grep Up | awk '{print $1}'"
                //will be filled by cmdappend
        };
        String containerId = CommandLineExecutor.multiLine(cmd);

        if (componentImage.getServiceImage().contains("jppf-node:5.2.9")) {
            cmd[2] = "docker restart " + componentNodeHexId + "";
            CommandLineExecutor.multiLine(cmd);
            System.out.println("docker image is jppf-node , so container restarted");

        }

        if (!containerId.isEmpty()) {

            //service register
            NewService newService = new NewService();
            newService.setId(componentNodeHexId + "ID");
            newService.setTags(Collections.singletonList("dockerService"));
            newService.setName(graphHexId + ":" + graphInstanceHexId + ":" + componentNodeHexId);

            if (agentParameters.isHasEnableIpv6()) {
                newService.setAddress(privateIpAddressIPv6);
            } else {
                newService.setAddress(privateIpAddressIPv4);
            }

            consulClient.agentServiceRegister(newService);

            NewCheck serviceCheck = new NewCheck();
            serviceCheck.setName("docker service check");
            serviceCheck.setServiceId(componentNodeHexId + "ID");

            if (healthCheck.getArgs().isEmpty()) {
                serviceCheck.setHttp(healthCheck.getHttpEndpoint());
                serviceCheck.setInterval(healthCheck.getInterval() + "s");
                LOGGER.log(Level.INFO, "apply health checks  on HTTP ----> {0} ", healthCheck);

            } else {
                ArrayList<String> args = new ArrayList<>();
                args.add("docker");
                args.add("exec");
                args.add(componentNodeHexId);

                String[] tempTable = healthCheck.getArgs().split(" ");
                for (String arg : tempTable) {
                    args.add(arg);
                }

                serviceCheck.setArgs(args);
                serviceCheck.setInterval(healthCheck.getInterval() + "s");
                LOGGER.log(Level.INFO, "apply health checks  on command ----> {0} ", healthCheck);

            }

            consulClient.agentCheckRegister(serviceCheck);

        } else {
            LOGGER.log(Level.SEVERE, "Error Status: {0}",
                    ErrorStatus.STATUS_TERMINATED_CONTAINER_SERVICE_IS_NOT_RUNNING);
            status = ErrorStatus.STATUS_TERMINATED_CONTAINER_SERVICE_IS_NOT_RUNNING.getValue();
            setStatus(status);
            return false;
        }

        return true;
    }

    //TODO remove method
    //check Traefik is register into consul
    private boolean registerTraefik() {

        NewService newService = new NewService();
        newService.setId("traefikID");
        List<String> tagList = new ArrayList();
        tagList.add("metrics");
        tagList.add(machineConstants.getOperatingSystemArchitecture());

        for (String name : dependencies.getDependsOnComponent()) {
            tagList.add(name);
        }

        newService.setTags(tagList);
        newService.setName("traefik");
        newService.setPort(AgentConfiguration.METRIC_EXPORTER_PORT);
        newService.setAddress(privateIpAddressIPv6);

        NewService.Check serviceCheck = new NewService.Check();
        serviceCheck.setHttp("http://" + privateIpAddressIPv6 + ":" + AgentConfiguration.METRIC_EXPORTER_PORT + "/metrics");
        serviceCheck.setInterval("5s");
        serviceCheck.setDeregisterCriticalServiceAfter("5m");
        newService.setCheck(serviceCheck);

        consulClient.agentServiceRegister(newService);

        return true;
    }

    //--------------------------------------------------------------------------------phase 7

    private void listenIntoTopics() {

        //TODO for Astrid
        if (agentParameters.isHasEnableSecurity()) {

            DockerConfigurationForHashing dockerConfigurationForHashing = new DockerConfigurationForHashing();
            dockerConfigurationForHashing.setImage(dockerImage);
            dockerConfigurationForHashing.setEnvironmentVariableKey(dockerEnvironmentVariablesKeysList);
            dockerConfigurationForHashing.setRegistry(componentImage.getUrl());
            dockerConfigurationForHashing.setPassword(componentImage.getPassword());
            dockerConfigurationForHashing.setUsername(componentImage.getUserName());

            SecurityConfigurationExecution securityConfigurationExecution = new SecurityConfigurationExecution(
                    graphHexId, graphInstanceHexId, componentNodeHexId, componentNodeInstanceHexId,
                    kafkaConfiguration.getKafkaServer(), kafkaConfiguration.getSecurityConfigTopic(),
                    kafkaConfiguration.getSecurityConfigResultTopic(), dockerConfigurationForHashing, consulClient);
            securityConfigurationExecution.start();
        }

        //TODO for SOC
        if (agentParameters.isHasEnableSoc()) {

            SocExecution socExecutionThread = new SocExecution(
                    graphHexId, graphInstanceHexId, componentNodeHexId, componentNodeInstanceHexId,
                    kafkaConfiguration.getKafkaServerSoc(), kafkaConfiguration.getSocConfigTopic(), consulClient);
            socExecutionThread.start();
        }

        if (agentParameters.isHasEnableIps()) {
            IntrusionPreventionExecution intrusionPreventionExecutionThread = new IntrusionPreventionExecution(
                    graphHexId, graphInstanceHexId, componentNodeHexId, componentNodeInstanceHexId,
                    kafkaConfiguration.getKafkaServer(), kafkaConfiguration.getTopicNameIps(), consulClient);
            intrusionPreventionExecutionThread.start();
        }

        if (agentParameters.isHasEnableIds()) {
            IntrusionDetectionExecution intrusionDetectionExecutionThread = new IntrusionDetectionExecution(
                    graphHexId, graphInstanceHexId, componentNodeHexId, componentNodeInstanceHexId,
                    kafkaConfiguration.getKafkaServer(), kafkaConfiguration.getTopicNameIds(),
                    kafkaConfiguration.getTopicNameIdsAlert(), consulClient);
            intrusionDetectionExecutionThread.start();
        }

    }
}
