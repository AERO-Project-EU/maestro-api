package eu.orchestrator.agent.configuration;

public enum HostnameMappingsStatus {

    STATUS_WAIT(0),
    STATUS_PROCEED(1),
    STATUS_QUERY(2);

    private int value;

    HostnameMappingsStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
