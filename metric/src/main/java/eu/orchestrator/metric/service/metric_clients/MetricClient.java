package eu.orchestrator.metric.service.metric_clients;

public interface MetricClient {

    void apply(String metricJson, String hexId, String endpointWithPort);

    void delete(String hexId, String endpointWithPort);

}
