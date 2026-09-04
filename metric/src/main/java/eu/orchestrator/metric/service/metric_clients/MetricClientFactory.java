package eu.orchestrator.metric.service.metric_clients;

import eu.orchestrator.common.enums.ProjectEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MetricClientFactory {

    // Bearer token accepted by the Rainbow metrics API, and the port it listens on.
    @Value("${metric.rainbow.token:}")
    private String rainbowToken;

    @Value("${metric.rainbow.port:5000}")
    private String rainbowPort;

    public MetricClient getClient(String applyType) {
        if (applyType.equalsIgnoreCase(ProjectEnum.RAINBOW.getFriendlyName())) {
            return new RainbowClient(rainbowToken, rainbowPort);
        } else {
            return new DefaultClient();
        }
    }

}
