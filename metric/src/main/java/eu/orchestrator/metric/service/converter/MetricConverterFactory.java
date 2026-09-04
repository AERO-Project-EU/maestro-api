package eu.orchestrator.metric.service.converter;

import eu.orchestrator.common.enums.ProjectEnum;
import org.springframework.stereotype.Service;

@Service
public class MetricConverterFactory {

    public MetricConverter getConverter(String converterType) {
        if (converterType.equalsIgnoreCase(ProjectEnum.RAINBOW.getFriendlyName())) {
            return new RainbowMetricConverter();
        } else {
            return new DefaultMetricConverter();
        }
    }

}
