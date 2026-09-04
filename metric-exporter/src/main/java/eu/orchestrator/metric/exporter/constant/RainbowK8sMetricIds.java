package eu.orchestrator.metric.exporter.constant;

public enum RainbowK8sMetricIds {

    CPU("cpu_ptc"),
    MEMORY("memory_ptc");

    private final String value;

    RainbowK8sMetricIds(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
