package eu.orchestrator.metric.exporter.service.impl;

import eu.orchestrator.metric.exporter.model.InstanceDetails;
import eu.orchestrator.metric.exporter.service.PrometheusClient;
import eu.orchestrator.metric.exporter.service.interfaces.MonitoringService;
import eu.orchestrator.transfer.entities.metricExporter.ProxyMetricModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class KubernetesPrometheus implements MonitoringService {

    private final PrometheusClient prometheusClient;

    @Autowired
    public KubernetesPrometheus(PrometheusClient prometheusClient) {
        this.prometheusClient = prometheusClient;
    }

    @Override
    public double cpuLoadPercentage(InstanceDetails instanceDetails) {
        String instanceName = getInstance(instanceDetails);
        String query = "avg(netdata_cgroup_cpu_percentage_average{chart=~\".*" + instanceDetails.getApplicationInstanceHexId() + ".*"
                + instanceDetails.getComponentNodeInstanceHexId() + ".*\", instance=\"" + instanceName + "\"})";
        return prometheusClient.retrieveDoubleFromPrometheus(instanceDetails.getUrl(), query);
    }

    @Override
    public double availableDiskSpace(InstanceDetails instanceDetails) {
        String instanceName = getInstance(instanceDetails);
        String query = "(sum(netdata_disk_space_GiB_average{dimension=\"avail\", instance=\"" + instanceName + "\"}) / sum(netdata_disk_space_GiB_average{instance=\"" + instanceName + "\"})) * 100";
        return prometheusClient.retrieveDoubleFromPrometheus(instanceDetails.getUrl(), query);
    }

    @Override
    public double diskThroughputRead(InstanceDetails instanceDetails) {
        String instanceName = getInstance(instanceDetails);
        String query = "avg(netdata_disk_ops_operations_persec_average{dimension=\"reads\", instance=\"" + instanceName + "\"})";
        double diskThroughput = prometheusClient.retrieveDoubleFromPrometheus(instanceDetails.getUrl(), query);
        return (diskThroughput < 0) ? diskThroughput * (-1) : diskThroughput;
    }

    @Override
    public double diskThroughputWrite(InstanceDetails instanceDetails) {
        String instanceName = getInstance(instanceDetails);
        String query = "avg(netdata_disk_ops_operations_persec_average{dimension=\"writes\", instance=\"" + instanceName + "\"})";
        double diskThroughput = prometheusClient.retrieveDoubleFromPrometheus(instanceDetails.getUrl(), query);
        return (diskThroughput < 0) ? diskThroughput * (-1) : diskThroughput;
    }

    @Override
    public double usedRamPercentage(InstanceDetails instanceDetails) {
        String instanceName = getInstance(instanceDetails);
        String query = "avg(netdata_cgroup_mem_utilization_percentage_average{chart=~\".*" + instanceDetails.getApplicationInstanceHexId() + ".*"
                + instanceDetails.getComponentNodeInstanceHexId() + ".*\", instance=\"" + instanceName + "\"})";
        return prometheusClient.retrieveDoubleFromPrometheus(instanceDetails.getUrl(), query);
    }

    @Override
    public double packetsSent(InstanceDetails instanceDetails) {
        String instanceName = getInstance(instanceDetails);
        String query =
                "avg(netdata_net_packets_packets_persec_average{chart=~\".*" + instanceDetails.getApplicationInstanceHexId() + ".*\", dimension=\"sent\", instance=\"" + instanceName + "\"})";
        double packetsSent = prometheusClient.retrieveDoubleFromPrometheus(instanceDetails.getUrl(), query);
        return (packetsSent < 0) ? packetsSent * (-1) : packetsSent;
    }

    @Override
    public double packetsReceived(InstanceDetails instanceDetails) {
        String instanceName = getInstance(instanceDetails);
        String query =
                "avg(netdata_net_packets_packets_persec_average{chart=~\".*" + instanceDetails.getApplicationInstanceHexId() + ".*\", dimension=\"received\", instance=\"" + instanceName + "\"})";
        double packetsSent = prometheusClient.retrieveDoubleFromPrometheus(instanceDetails.getUrl(), query);
        return (packetsSent < 0) ? packetsSent * (-1) : packetsSent;
    }

    @Override
    public List<ProxyMetricModel> proxyModels(InstanceDetails instanceDetails) {
        return new ArrayList<>();
    }

    @Override
    public long timestamp(InstanceDetails instanceDetails) {
        String instanceName = getInstance(instanceDetails);
        String query = "avg(netdata_cgroup_cpu_percentage_average{chart=~\".*" + instanceDetails.getApplicationInstanceHexId() + ".*"
                + instanceDetails.getComponentNodeInstanceHexId() + ".*\", instance=\"" + instanceName + "\"})";
        return prometheusClient.retrieveTimestampFromPrometheus(instanceDetails.getUrl(), query);
    }

    private String getInstance(InstanceDetails instanceDetails) {
        String query = "netdata_cgroup_cpu_percentage_average{chart=~\".*" + instanceDetails.getApplicationInstanceHexId() + ".*"
                + instanceDetails.getComponentNodeInstanceHexId() + ".*\", dimension=\"system\"}";
        return prometheusClient.retrieveInstanceName(instanceDetails.getUrl(), query);
    }

}
