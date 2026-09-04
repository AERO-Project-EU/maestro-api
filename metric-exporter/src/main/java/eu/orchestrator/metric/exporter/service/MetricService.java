package eu.orchestrator.metric.exporter.service;

import eu.orchestrator.metric.exporter.model.InstanceDetails;
import eu.orchestrator.metric.exporter.service.interfaces.MonitoringService;
import eu.orchestrator.transfer.entities.metricExporter.MetricModel;
import eu.orchestrator.transfer.entities.metricExporter.requests.MetricsRequestTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class MetricService {

    private final MetricsFactory metricsFactory;

    @Autowired
    public MetricService(MetricsFactory metricsFactory) {
        this.metricsFactory = metricsFactory;
    }

    public MetricModel getMetrics(MetricsRequestTO metricsRequest) {
        InstanceDetails instanceDetails = metricsRequestToInstanceDetails(metricsRequest);
        MonitoringService monitoringService = metricsFactory.getPrometheusInstance(instanceDetails.getType());

        MetricModel metricModel = getMetricModelInstance(metricsRequest);
        metricModel.setPercentageUsedCPU(monitoringService.cpuLoadPercentage(instanceDetails));
        metricModel.setDiskAvailable(monitoringService.availableDiskSpace(instanceDetails));
        metricModel.setPercentageUsedRAM(monitoringService.usedRamPercentage(instanceDetails));
        metricModel.setNetworkTrafficPacketsReceived(monitoringService.packetsReceived(instanceDetails));
        metricModel.setNetworkTrafficPacketsSend(monitoringService.packetsSent(instanceDetails));
        metricModel.setDiskThroughputRead(monitoringService.diskThroughputRead(instanceDetails));
        metricModel.setDiskThroughputWrite(monitoringService.diskThroughputWrite(instanceDetails));
        metricModel.setProxyMetricModels(monitoringService.proxyModels(instanceDetails));
        metricModel.setTimestamp(monitoringService.timestamp(instanceDetails));

        return metricModel;
    }

    private MetricModel getMetricModelInstance(MetricsRequestTO metricsRequest) {
        MetricModel metricModel = new MetricModel();
        metricModel.setGraphHexId(metricsRequest.getInstance().getApplicationHexId());
        metricModel.setGraphInstanceHexId(metricsRequest.getInstance().getApplicationInstanceHexId());
        metricModel.setComponentNodeHexId(metricsRequest.getInstance().getComponentNodeHexId());
        metricModel.setComponentNodeInstanceHexId(metricsRequest.getInstance().getComponentNodeInstanceHexId());
        return metricModel;
    }

    private InstanceDetails metricsRequestToInstanceDetails(MetricsRequestTO metricsRequestTO) {
        InstanceDetails instanceDetails = new InstanceDetails();
        instanceDetails.setUrl(metricsRequestTO.getSource().getUrl());
        instanceDetails.setType(metricsRequestTO.getSource().getType());
        instanceDetails.setComponentName(metricsRequestTO.getInstance().getName());
        instanceDetails.setApplicationHexId(metricsRequestTO.getInstance().getApplicationHexId());
        instanceDetails.setApplicationInstanceHexId(metricsRequestTO.getInstance().getApplicationInstanceHexId());
        instanceDetails.setComponentNodeHexId(metricsRequestTO.getInstance().getComponentNodeHexId());
        instanceDetails.setComponentNodeInstanceHexId(metricsRequestTO.getInstance().getComponentNodeInstanceHexId());
        return instanceDetails;
    }

}
