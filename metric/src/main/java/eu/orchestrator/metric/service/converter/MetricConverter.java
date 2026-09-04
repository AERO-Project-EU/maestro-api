package eu.orchestrator.metric.service.converter;


import eu.orchestrator.metric.dto.MetricForConversion;

public interface MetricConverter {

    String convert(MetricForConversion metricForConversion);

}
