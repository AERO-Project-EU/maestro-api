package eu.orchestrator.metric.exporter.rest.api;

import eu.orchestrator.metric.exporter.configuration.MonitoringPortsConfiguration;
import eu.orchestrator.metric.exporter.service.ConsulService;
import eu.orchestrator.metric.exporter.service.MetricService;
import eu.orchestrator.metric.exporter.service.PrometheusExporter;
import eu.orchestrator.transfer.entities.metricExporter.MetricModel;
import eu.orchestrator.transfer.entities.metricExporter.requests.MetricsRequestTO;
import eu.orchestrator.transfer.entities.metricExporter.ProxyMetricModel;

import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.health.model.HealthService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/metrics")
public class MetricExporterRestController {

    private static final Logger logger = Logger.getLogger(MetricExporterRestController.class.getName());

    private final PrometheusExporter prometheusExporter;

    private final MetricService metricService;

    private final ConsulService consulService;

    private final MonitoringPortsConfiguration ports;

    public MetricExporterRestController(
            PrometheusExporter prometheusExporter,
            MetricService metricService,
            ConsulService consulService,
            MonitoringPortsConfiguration ports
    ) {
        this.prometheusExporter = prometheusExporter;
        this.metricService = metricService;
        this.consulService = consulService;
        this.ports = ports;
    }

    private Long timestamp;

    @RequestMapping(value = "/retrieve/{graphHexID}/{graphInstanceHexID}", method = RequestMethod.GET)
    public List<MetricModel> getMonitoringMetrics(@PathVariable("graphHexID") String graphHexId,
            @PathVariable("graphInstanceHexID") String graphInstanceHexId) {

        logger.info("Metric Exporter - MetricExporterRestController - getMonitoringMetrics");

        ArrayList<MetricModel> allInstanceMetrics = new ArrayList<>();
        String componentPatternPrefix = graphHexId + "-" + graphInstanceHexId;

        Response<List<HealthService>> healthyServices = consulService.getHealthServices("netdata");

        List<HealthService> allServices = healthyServices.getValue().stream().filter(service -> service.getNode().getNode().startsWith(componentPatternPrefix))
                .collect(Collectors.toList());

        for (HealthService service : allServices) {
            String[] identifiers = service.getNode().getNode().split("-");
            String componentNodeHexId = identifiers[2];
            String componentNodeInstanceHexId = identifiers[3];

            MetricModel metricModel = new MetricModel();
            metricModel.setGraphHexId(graphHexId);
            metricModel.setGraphInstanceHexId(graphInstanceHexId);
            metricModel.setComponentNodeHexId(componentNodeHexId);
            metricModel.setComponentNodeInstanceHexId(componentNodeInstanceHexId);

            String metricPrefix = "netdata:" + graphHexId + ":" + graphInstanceHexId + ":" + componentNodeHexId;
            String metricsSuffice = "]:" + ports.getNetdata() + "'}";

            // get CPU percentage
            double cpuLoad = 100 - prometheusExporter.retrieveValueFromPrometheus(
                    metricPrefix + "_cpu_cpu_percentage_average{dimension='idle',instance='[" + service.getNode().getAddress() + metricsSuffice);
            metricModel.setPercentageUsedCPU(cpuLoad);

            // get available Disk
            double availableDisk = prometheusExporter.retrieveValueFromPrometheus(
                    metricPrefix + "_disk_space_GB_average{dimension='avail',instance='[" + service.getNode().getAddress() + metricsSuffice);
            metricModel.setDiskAvailable(availableDisk);

            // get RAM percentage
            double usedMemory = prometheusExporter.retrieveValueFromPrometheus(
                    metricPrefix + "_system_ram_MB_average{chart='system.ram',dimension='used', instance='[" + service.getNode().getAddress() + metricsSuffice);
            double bufferedMemory = prometheusExporter.retrieveValueFromPrometheus(
                    metricPrefix + "_system_ram_MB_average{chart='system.ram',dimension='buffers',instance='[" + service.getNode().getAddress()
                            + metricsSuffice);
            double cachedMemory = prometheusExporter.retrieveValueFromPrometheus(
                    metricPrefix + "_system_ram_MB_average{chart='system.ram',dimension='cached',instance='[" + service.getNode().getAddress()
                            + metricsSuffice);
            double freeMemory = prometheusExporter.retrieveValueFromPrometheus(
                    metricPrefix + "_system_ram_MB_average{chart='system.ram',dimension='free',instance='[" + service.getNode().getAddress() + metricsSuffice);
            double percMemory = (usedMemory * 100) / (cachedMemory + bufferedMemory + usedMemory + freeMemory);
            metricModel.setPercentageUsedRAM(percMemory);

            // get network traffic
            double packetsReceived = prometheusExporter
                    .retrieveValueFromPrometheus("sum(" + metricPrefix + "_net_packets_packets_persec_average{dimension='received'})");
            metricModel.setNetworkTrafficPacketsReceived(packetsReceived);
            double packetsSend = prometheusExporter
                    .retrieveValueFromPrometheus("sum(" + metricPrefix + "_net_packets_packets_persec_average{dimension='sent'})");
            packetsSend = (packetsSend < 0) ? packetsSend * (-1) : packetsSend;
            metricModel.setNetworkTrafficPacketsSend(packetsSend);

            // get Disk IO
            double diskRead = prometheusExporter.retrieveValueFromPrometheus(metricPrefix + "_disk_io_kilobytes_persec_average{dimension='reads'}");
            metricModel.setDiskThroughputRead(diskRead);
            double diskWrite = prometheusExporter.retrieveValueFromPrometheus(metricPrefix + "_disk_io_kilobytes_persec_average{dimension='writes'}");
            diskWrite = (diskWrite < 0) ? diskWrite * (-1) : diskWrite;
            metricModel.setDiskThroughputWrite(diskWrite);

            Response<List<HealthService>> healthyServices2 = consulService.getHealthServices("traefik");
            Optional<HealthService> traefikServices = healthyServices2.getValue().stream()
                    .filter(service1 -> service1.getNode().getAddress().compareTo(service.getNode().getAddress()) == 0).findFirst();

            if (traefikServices.isPresent()) {
                List<ProxyMetricModel> proxyMetricModels = new ArrayList<>();
                List<String> serviceTags = traefikServices.get().getService().getTags().stream()
                        .filter(tag -> tag.compareTo("metrics") != 0 && tag.compareTo("arm") != 0 && tag.compareTo("amd64") != 0).collect(Collectors.toList());

                for (String tag : serviceTags) {
                    double totalRequest = prometheusExporter.retrieveValueFromPrometheus(
                            "traefik_backend_requests_total{backend='" + tag + "', instance='[" + traefikServices.get().getNode().getAddress() + "]:" + ports.getTraefik() + "'}");
                    double totalTime = prometheusExporter.retrieveValueFromPrometheus(
                            "traefik_backend_request_duration_seconds_sum{backend='" + tag + "', instance='[" + traefikServices.get().getNode().getAddress()
                                    + "]:" + ports.getTraefik() + "'}");
                    double invocationsPerSec = prometheusExporter.retrieveRequestPerSec(
                            "traefik_backend_requests_total{backend='" + tag + "', instance='[" + traefikServices.get().getNode().getAddress()
                                    + "]:" + ports.getTraefik() + "'}[20s]");

                    ProxyMetricModel proxyMetricModel = new ProxyMetricModel();
                    proxyMetricModel.setFunctionName(tag);
                    proxyMetricModel.setInvocationsResponseTime((totalRequest == -1) ? 0 : (totalTime / totalRequest));
                    proxyMetricModel.setInvocationsPerSec(invocationsPerSec);
                    proxyMetricModels.add(proxyMetricModel);
                }
                metricModel.setProxyMetricModels(proxyMetricModels);
            }

            // retrieve timestamp
            timestamp = prometheusExporter.retrieveTimeStampFromPrometheus(
                    metricPrefix + "_cpu_cpu_percentage_average{dimension='idle',instance='[" + service.getNode().getAddress() + metricsSuffice);
            metricModel.setTimestamp(timestamp);

            allInstanceMetrics.add(metricModel);
        }
        return allInstanceMetrics;
    }

    @PostMapping
    public MetricModel getMonitoringMetricsByProvider(@RequestBody MetricsRequestTO metricsRequest) {
        logger.info("Metric Exporter - MetricExporterRestController - getMonitoringMetrics");
        return metricService.getMetrics(metricsRequest);

    }

    // TODO: 22/2/22 add cache to prevent multiple calls to fetch data like instance or application address

}
