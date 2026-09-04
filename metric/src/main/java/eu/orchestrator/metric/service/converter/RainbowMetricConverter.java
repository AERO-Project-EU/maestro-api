package eu.orchestrator.metric.service.converter;

import eu.orchestrator.common.util.NullCheckUtil;
import eu.orchestrator.metric.dto.MetricForConversion;
import eu.orchestrator.metric.enums.MetricEnum;
import eu.orchestrator.repository.domain.Analytic;
import eu.orchestrator.repository.domain.AnalyticExpression;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.ComponentNodeInstance;

import java.util.Locale;

public class RainbowMetricConverter implements MetricConverter {


    @Override
    public String convert(MetricForConversion metricForConversion) {
        Analytic analytic = metricForConversion.getAnalytic();
        ApplicationInstance applicationInstance = analytic.getApplicationInstance();

        StringBuilder query = new StringBuilder();
        query.append(MetricEnum.QUERY_START.getValue());

        String streamName = analytic.getName() + MetricEnum.UNDERSCORE.getValue() + applicationInstance.getHexID();

        query.append(MetricEnum.DOUBLE_QUOTE.getValue()).append(streamName).append(MetricEnum.COLON.getValue())
                .append(MetricEnum.STREAM_FROM.getValue()).append(MetricEnum.LEFT_PARENTHESIS.getValue())
                .append(MetricEnum.PERIODICITY.getValue()).append(MetricEnum.EQUALS.getValue()).append(MetricEnum.PERIODICITY_VALUE.getValue());

        //TODO we will have multiple expression so we have to solve the metricID,
        // for now we use only the first component
        AnalyticExpression analyticExpression = analytic.getAnalyticExpressions().get(0);
        ComponentNodeInstance componentNodeInstance = analyticExpression.getComponentNodeInstance();

        query.append(MetricEnum.COMMA.getValue()).append(MetricEnum.METRIC_ID.getValue()).append(MetricEnum.EQUALS.getValue())
                .append(MetricEnum.ESCAPE_DOUBLE_QUOTE.getValue()).append(analyticExpression.getMetric()).append(MetricEnum.ESCAPE_DOUBLE_QUOTE.getValue());
        query.append(MetricEnum.COMMA.getValue()).append(MetricEnum.ENTITY_TYPE.getValue()).append(MetricEnum.EQUALS.getValue())
                .append(MetricEnum.ESCAPE_DOUBLE_QUOTE.getValue()).append(MetricEnum.POD.getValue())
                .append(MetricEnum.ESCAPE_DOUBLE_QUOTE.getValue());
        query.append(MetricEnum.COMMA.getValue()).append(MetricEnum.NAMESPACE.getValue()).append(MetricEnum.EQUALS.getValue())
                .append(MetricEnum.ESCAPE_DOUBLE_QUOTE.getValue())
                .append(metricForConversion.getNamespace()).append(MetricEnum.ESCAPE_DOUBLE_QUOTE.getValue());
        query.append(MetricEnum.COMMA.getValue()).append(MetricEnum.NAME.getValue()).append(MetricEnum.EQUALS.getValue())
                .append(MetricEnum.ESCAPE_DOUBLE_QUOTE.getValue()).append(firstPartOfPodNameCreation(componentNodeInstance))
                .append(MetricEnum.PERCENT_SYMBOL.getValue()).append(MetricEnum.ESCAPE_DOUBLE_QUOTE.getValue()).append(MetricEnum.RIGHT_PARENTHESIS.getValue())
                .append(MetricEnum.QUESTION_MARK.getValue()).append(MetricEnum.DOUBLE_QUOTE.getValue()).append(MetricEnum.COMMA.getValue());

        //stream output name WE NEED TO REVIEW THIS ALSO, for now function_AnalyticName
        query.append(MetricEnum.DOUBLE_QUOTE.getValue()).append(analytic.getHexID()).append(MetricEnum.EQUALS.getValue());
        query.append(MetricEnum.COMPUTE.getValue()).append(analytic.getFunction().toLowerCase(Locale.ROOT)).append(MetricEnum.LEFT_PARENTHESIS.getValue());
        query.append(MetricEnum.ESCAPE_DOUBLE_QUOTE.getValue()).append(analyticExpression.getMetric()).append(MetricEnum.ESCAPE_DOUBLE_QUOTE.getValue()).
                append(MetricEnum.FROM.getValue()).append(MetricEnum.LEFT_PARENTHESIS.getValue())
                .append(streamName).append(MetricEnum.RIGHT_PARENTHESIS.getValue());

        if (analytic.getPeriodicity() != null && analytic.getPeriodicity() > 0) {
            query.append(MetricEnum.COMMA.getValue()).append(analytic.getPeriodicity()).append(MetricEnum.SECONDS_SYMBOL.getValue());
        }
        query.append(MetricEnum.RIGHT_PARENTHESIS.getValue());

        if (analytic.getWindow() != null && analytic.getWindow() > 0) {
            Integer window = analytic.getPeriodicity() * analytic.getWindow();
            query.append(MetricEnum.EVERY.getValue()).append(window).append(MetricEnum.SECONDS_SYMBOL.getValue());
        }

        query.append(MetricEnum.QUESTION_MARK.getValue()).append(MetricEnum.DOUBLE_QUOTE.getValue());

        query.append(MetricEnum.QUERY_END.getValue());

        return query.toString();
    }

    //TODO common with componentInstanceNameProvider function in KubernetesUtil service, move it to commonUtil
    private String firstPartOfPodNameCreation(ComponentNodeInstance componentNodeInstance) {
        String name = null;
        if (NullCheckUtil.isNotEmpty(componentNodeInstance.getApplicationInstance())
                && NullCheckUtil.isNotEmpty(componentNodeInstance.getApplicationInstance().getName())
                && NullCheckUtil.isNotEmpty(componentNodeInstance.getName())
                && NullCheckUtil.isNotEmpty(componentNodeInstance.getHexID())) {
            name = componentNodeInstance.getApplicationInstance().getName().toLowerCase(Locale.ROOT)
                    + "-" + componentNodeInstance.getName().toLowerCase(Locale.ROOT)
                    + "-" + componentNodeInstance.getHexID().toLowerCase(Locale.ROOT);
        }

        return name;
    }
}
