package eu.orchestrator.metric.exporter.service.impl;

import eu.orchestrator.metric.exporter.constant.RainbowK8sMetricIds;
import eu.orchestrator.metric.exporter.exception.EmptyResponseException;
import eu.orchestrator.metric.exporter.exception.MissingMetricValuesException;
import eu.orchestrator.metric.exporter.exception.MissingMetricsException;
import eu.orchestrator.metric.exporter.model.InstanceDetails;
import eu.orchestrator.metric.exporter.service.RainbowK8sService;
import eu.orchestrator.metric.exporter.service.interfaces.MonitoringService;
import eu.orchestrator.transfer.entities.metricExporter.ProxyMetricModel;
import eu.orchestrator.transfer.entities.rainbowk8s.dto.RainbowMonitoringDataDto;
import eu.orchestrator.transfer.entities.rainbowk8s.dto.RainbowMonitoringResponseDto;
import eu.orchestrator.transfer.entities.rainbowk8s.dto.RainbowMonitoringValueDto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import org.springframework.stereotype.Component;

@Component
public class RainbowKubernetes implements MonitoringService {

    private static final Logger logger = Logger.getLogger(RainbowKubernetes.class.getName());

    private final RainbowK8sService rainbowK8sService;
    private long lastUpdateAt;

    private static Map<String, RainbowMonitoringResponseDto> metrics = new HashMap<>();
    private static final int EXPIRATION_TIME = 10;

    public RainbowKubernetes(RainbowK8sService rainbowK8sService) {
        this.rainbowK8sService = rainbowK8sService;
    }

    @Override
    public double cpuLoadPercentage(InstanceDetails instanceDetails) {
        RainbowMonitoringValueDto value = getValue(instanceDetails, RainbowK8sMetricIds.CPU);
        logger.info("RainbowKubernetes - Rainbow CPU: " + value.getVal());
        return Double.parseDouble(value.getVal());
    }

    @Override
    public double availableDiskSpace(InstanceDetails instanceDetails) {
        return 0;
    }

    @Override
    public double diskThroughputRead(InstanceDetails instanceDetails) {
        return 0;
    }

    @Override
    public double diskThroughputWrite(InstanceDetails instanceDetails) {
        return 0;
    }

    @Override
    public double usedRamPercentage(InstanceDetails instanceDetails) {
        RainbowMonitoringValueDto value = getValue(instanceDetails, RainbowK8sMetricIds.MEMORY);
        return Double.parseDouble(value.getVal());
    }

    @Override
    public double packetsSent(InstanceDetails instanceDetails) {
        return 0;
    }

    @Override
    public double packetsReceived(InstanceDetails instanceDetails) {
        return 0;
    }

    @Override
    public List<ProxyMetricModel> proxyModels(InstanceDetails instanceDetails) {
        return new ArrayList<>();
    }

    @Override
    public long timestamp(InstanceDetails instanceDetails) {
        RainbowMonitoringValueDto value = getValue(instanceDetails, RainbowK8sMetricIds.CPU);
        return Long.parseLong(value.getTimestamp());
    }

    /**
     * This function is responsible for fetching the requested value either from memory or by calling the external service, and is also aware of the latest
     * timestamp of the data in order to keep them up to date.
     *
     * @param instanceDetails The details of the component
     * @param metricId        The requested metric id i.e. cpu_ptc, memory_ptc
     * @return Returns the object containing the value and the timestamp
     */
    private RainbowMonitoringValueDto getValue(InstanceDetails instanceDetails, RainbowK8sMetricIds metricId) {
        // In case the static array is empty or contains the response of a different component,
        // then a call is made to the external service to fetch the new data
        if (metrics.isEmpty() || !metrics.containsKey(instanceDetails.getComponentName())) {
            return fetchAndCache(instanceDetails, metricId);
        } else {
            // If the data for the component already exist in memory, then we check if the timestamp
            // has changed from the last time we updated them. The response contains the value
            // and the timestamp regardless of how many requests we make to the service.
            RainbowMonitoringResponseDto response = metrics.get(instanceDetails.getComponentName());
            RainbowMonitoringValueDto valueDto = getMonitoringValue(response, metricId);

            // When a new timestamp arrives, we store the new response.
            if (Instant.now().getEpochSecond() - lastUpdateAt >= EXPIRATION_TIME) {
                valueDto = fetchAndCache(instanceDetails, metricId);
            }
            return valueDto;
        }
    }

    /**
     * This function is responsible to fetch data from the external service and also store the information in memory for future reference
     *
     * @param instanceDetails The details of the component
     * @param metricId        The requested metric id i.e. cpu_ptc, memory_ptc
     * @return Returns the requested value
     */
    private RainbowMonitoringValueDto fetchAndCache(InstanceDetails instanceDetails, RainbowK8sMetricIds metricId) {
        RainbowMonitoringResponseDto response = fetchDataFromExternalService(instanceDetails);
        storeResponse(instanceDetails, response);

        RainbowMonitoringValueDto valueDto = getMonitoringValue(response, metricId);
        lastUpdateAt = Long.parseLong(valueDto.getTimestamp());

        return valueDto;
    }

    /**
     * This function is responsible to fetch the metric data from the external service.
     *
     * @param instanceDetails The details of the component
     * @return The response of the service
     */
    private RainbowMonitoringResponseDto fetchDataFromExternalService(InstanceDetails instanceDetails) {
        return rainbowK8sService.getMonitoringValues(
                instanceDetails.getUrl(),
                instanceDetails.getComponentName()
        );
    }

    /**
     * This function is responsible to update the metrics static map in memory. This could probably be replaced by a in memory database like Redis.
     *
     * @param instanceDetails The details of the component
     * @param response        The new response of the external service
     */
    private void storeResponse(InstanceDetails instanceDetails, RainbowMonitoringResponseDto response) {
        metrics.clear();
        metrics.put(instanceDetails.getComponentName(), response);
    }

    /**
     * This function is responsible to find in the response the requested value
     *
     * @param response The response of the external service
     * @param metricId The requested metric id i.e. cpu_ptc, memory_ptc
     * @return Returns the object containing the value and the timestamp
     */
    private RainbowMonitoringValueDto getMonitoringValue(RainbowMonitoringResponseDto response, RainbowK8sMetricIds metricId) {
        if (response.getMonitoring().isEmpty()) {
            throw new EmptyResponseException("response is empty");
        }

        Optional<RainbowMonitoringDataDto> optionalMonitoringData =
                response.getMonitoring().get(0).getData().stream()
                        .filter(e -> metricId.getValue().equalsIgnoreCase(e.getMetricID()))
                        .findFirst();

        if (!optionalMonitoringData.isPresent()) {
            throw new MissingMetricsException("metrics are missing");
        }

        RainbowMonitoringDataDto monitoringData = optionalMonitoringData.get();
        List<RainbowMonitoringValueDto> monitoringDataValues = monitoringData.getValues();

        if (monitoringDataValues.isEmpty()) {
            throw new MissingMetricValuesException("metrics values are empty");
        }

        return monitoringDataValues.get(0);
    }

}
