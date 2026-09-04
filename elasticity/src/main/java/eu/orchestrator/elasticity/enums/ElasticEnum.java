package eu.orchestrator.elasticity.enums;

public enum ElasticEnum {

    // REST CALLS
    COOKIE("Cookie"),
    KUBERNETES_RAINBOW_SERVICE_GRAPH("/rainbow/servicegraph/applicationInstanceId/");


    private final String value;

    ElasticEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
