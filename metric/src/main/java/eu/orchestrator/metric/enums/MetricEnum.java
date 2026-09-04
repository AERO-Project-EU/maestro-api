package eu.orchestrator.metric.enums;

public enum MetricEnum {


    DOUBLE_QUOTE("\""),
    ESCAPE_DOUBLE_QUOTE("\\\""),
    UNDERSCORE("_"),
    COLON(": "),
    PERCENT_SYMBOL("%"),
    SECONDS_SYMBOL("s"),
    LEFT_PARENTHESIS("( "),
    RIGHT_PARENTHESIS(") "),
    QUESTION_MARK("; "),
    COMMA(", "),
    EQUALS("= "),

    COOKIE("Cookie"),

    // Rainbow metric enums
    QUERY_START("{\"Queries\":  ["),
    QUERY_END("]}"),
    STREAM_FROM("stream from storageLayer"),
    PERIODICITY("periodicity"),
    PERIODICITY_VALUE("1000"),
    METRIC_ID("metricID"),
    ENTITY_TYPE("entityType"),
    POD("POD"),
    NAMESPACE("namespace"),
    NAME("name"),
    COMPUTE("compute "),
    FROM(" from "),
    EVERY("EVERY "),

    // REST CALLS
    KUBERNETES_NAMESPACE_URI("/kubernetes/namespace/applicationInstanceId/"),

    HTTP("http://"),
    METRICS_API_URI("/api/insights/");


    private final String value;

    MetricEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
