package eu.orchestrator.metric.exporter.service.impl;

import eu.orchestrator.metric.exporter.configuration.MonitoringPortsConfiguration;
import eu.orchestrator.metric.exporter.service.ConsulService;
import eu.orchestrator.metric.exporter.model.InstanceDetails;
import eu.orchestrator.metric.exporter.service.PrometheusClient;
import eu.orchestrator.metric.exporter.service.interfaces.MonitoringService;
import eu.orchestrator.transfer.entities.metricExporter.ProxyMetricModel;

import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.health.model.HealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VmPrometheus implements MonitoringService {

    private final ConsulService consulService;
    private final PrometheusClient prometheusClient;
    private final MonitoringPortsConfiguration ports;

    // instance='[<address>]:<netdata-port>'} suffix shared by every netdata query
    private final String metricsSuffix;

    @Autowired
    public VmPrometheus(ConsulService consulService, PrometheusClient prometheusClient, MonitoringPortsConfiguration ports) {
        this.consulService = consulService;
        this.prometheusClient = prometheusClient;
        this.ports = ports;
        this.metricsSuffix = "]:" + ports.getNetdata() + "'}";
    }

    @Override
    public double cpuLoadPercentage(InstanceDetails instanceDetails) {
        String query = getMetricPrefix(instanceDetails) + "_cpu_cpu_percentage_average{dimension='idle',instance='[" + getServiceAddress(instanceDetails) + metricsSuffix;
        return 100 - prometheusClient.retrieveDoubleFromPrometheus(instanceDetails.getUrl(), query);
    }

    @Override
    public double availableDiskSpace(InstanceDetails instanceDetails) {
        String query = getMetricPrefix(instanceDetails) + "_disk_space_GB_average{dimension='avail',instance='[" + getServiceAddress(instanceDetails) + metricsSuffix;
        return prometheusClient.retrieveDoubleFromPrometheus(instanceDetails.getUrl(), query);
    }

    @Override
    public double diskThroughputRead(InstanceDetails instanceDetails) {
        String query = getMetricPrefix(instanceDetails) + "_disk_io_kilobytes_persec_average{dimension='reads'}";
        return prometheusClient.retrieveDoubleFromPrometheus(instanceDetails.getUrl(), query);
    }

    @Override
    public double diskThroughputWrite(InstanceDetails instanceDetails) {
        String query = getMetricPrefix(instanceDetails) + "_disk_io_kilobytes_persec_average{dimension='writes'}";
        double diskWrite = prometheusClient.retrieveDoubleFromPrometheus(instanceDetails.getUrl(), query);
        return (diskWrite < 0) ? diskWrite * (-1) : diskWrite;
    }

    @Override
    public double usedRamPercentage(InstanceDetails instanceDetails) {
        double usedMemory = prometheusClient.retrieveDoubleFromPrometheus(instanceDetails.getUrl(),
                getMetricPrefix(instanceDetails) + "_system_ram_MB_average{chart='system.ram',dimension='used', instance='[" + getServiceAddress(instanceDetails)
                        + metricsSuffix);
        double bufferedMemory = prometheusClient.retrieveDoubleFromPrometheus(instanceDetails.getUrl(),
                getMetricPrefix(instanceDetails) + "_system_ram_MB_average{chart='system.ram',dimension='buffers',instance='[" + getServiceAddress(instanceDetails)
                        + metricsSuffix);
        double cachedMemory = prometheusClient.retrieveDoubleFromPrometheus(instanceDetails.getUrl(),
                getMetricPrefix(instanceDetails) + "_system_ram_MB_average{chart='system.ram',dimension='cached',instance='[" + getServiceAddress(instanceDetails)
                        + metricsSuffix);
        double freeMemory = prometheusClient.retrieveDoubleFromPrometheus(instanceDetails.getUrl(),
                getMetricPrefix(instanceDetails) + "_system_ram_MB_average{chart='system.ram',dimension='free',instance='[" + getServiceAddress(instanceDetails)
                        + metricsSuffix);
        return (usedMemory * 100) / (cachedMemory + bufferedMemory + usedMemory + freeMemory);
    }

    @Override
    public double packetsSent(InstanceDetails instanceDetails) {
        double packetsSend = prometheusClient.retrieveDoubleFromPrometheus(instanceDetails.getUrl(), "sum(" + getMetricPrefix(instanceDetails) + "_net_packets_packets_persec_average{dimension='sent'})");
        return (packetsSend < 0) ? packetsSend * (-1) : packetsSend;
    }

    @Override
    public double packetsReceived(InstanceDetails instanceDetails) {
        String query = "sum(" + getMetricPrefix(instanceDetails) + "_net_packets_packets_persec_average{dimension='received'})";
        return prometheusClient.retrieveDoubleFromPrometheus(instanceDetails.getUrl(), query);
    }

    @Override
    public List<ProxyMetricModel> proxyModels(InstanceDetails instanceDetails) {
        List<ProxyMetricModel> proxyMetricModels = new ArrayList<>();
        Response<List<HealthService>> healthyServices2 = consulService.getHealthServices("traefik");
        Optional<HealthService> traefikServices = healthyServices2.getValue().stream()
                .filter(service1 -> service1.getNode().getAddress().compareTo(getServiceAddress(instanceDetails)) == 0).findFirst();

        if (traefikServices.isPresent()) {

            List<String> serviceTags = traefikServices.map(healthService -> healthService.getService().getTags().stream()
                            .filter(tag -> tag.compareTo("metrics") != 0 && tag.compareTo("arm") != 0 && tag.compareTo("amd64") != 0)
                            .collect(Collectors.toList()))
                    .orElseGet(ArrayList::new);
            for (String tag : serviceTags) {
                double totalRequest = prometheusClient.retrieveDoubleFromPrometheus(instanceDetails.getUrl(),
                        "traefik_backend_requests_total{backend='" + tag + "', instance='[" + traefikServices.get().getNode().getAddress() + "]:" + ports.getTraefik() + "'}");
                double totalTime = prometheusClient.retrieveDoubleFromPrometheus(instanceDetails.getUrl(),
                        "traefik_backend_request_duration_seconds_sum{backend='" + tag + "', instance='[" + traefikServices.get().getNode().getAddress()
                                + "]:" + ports.getTraefik() + "'}");
                double invocationsPerSec = prometheusClient.retrieveRequestPerSec(instanceDetails.getUrl(),
                        "traefik_backend_requests_total{backend='" + tag + "', instance='[" + traefikServices.get().getNode().getAddress()
                                + "]:" + ports.getTraefik() + "'}[20s]");

                ProxyMetricModel proxyMetricModel = new ProxyMetricModel();
                proxyMetricModel.setFunctionName(tag);
                proxyMetricModel.setInvocationsResponseTime((totalRequest == -1) ? 0 : (totalTime / totalRequest));
                proxyMetricModel.setInvocationsPerSec(invocationsPerSec);
                proxyMetricModels.add(proxyMetricModel);
            }
        }
        return proxyMetricModels;
    }

    @Override
    public long timestamp(InstanceDetails instanceDetails) {
        String query = getMetricPrefix(instanceDetails) + "_cpu_cpu_percentage_average{dimension='idle',instance='[" + getServiceAddress(instanceDetails) + metricsSuffix;
        return prometheusClient.retrieveTimestampFromPrometheus(instanceDetails.getUrl(), query);
    }

    protected String getMetricPrefix(InstanceDetails instanceDetails) {
        return "netdata:" + instanceDetails.getApplicationHexId() + ":"
                + instanceDetails.getApplicationInstanceHexId() + ":"
                + instanceDetails.getComponentNodeHexId();
    }

    private String getServiceAddress(InstanceDetails instanceDetails) {
        String componentPatternPrefix =
                instanceDetails.getApplicationHexId() + "-"
                        + instanceDetails.getApplicationInstanceHexId() + "-"
                        + instanceDetails.getComponentNodeHexId() + "-"
                        + instanceDetails.getComponentNodeInstanceHexId();
        return consulService.getService(componentPatternPrefix).getNode().getAddress();
    }

}
