package eu.orchestrator.metric.exporter.service.interfaces;

import eu.orchestrator.metric.exporter.model.InstanceDetails;
import eu.orchestrator.transfer.entities.metricExporter.ProxyMetricModel;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MonitoringService {

    double cpuLoadPercentage(InstanceDetails instanceDetails);

    double availableDiskSpace(InstanceDetails instanceDetails);

    double diskThroughputRead(InstanceDetails instanceDetails);

    double diskThroughputWrite(InstanceDetails instanceDetails);

    double usedRamPercentage(InstanceDetails instanceDetails);

    double packetsSent(InstanceDetails instanceDetails);

    double packetsReceived(InstanceDetails instanceDetails);

    List<ProxyMetricModel> proxyModels(InstanceDetails instanceDetails);

    long timestamp(InstanceDetails instanceDetails);

}
