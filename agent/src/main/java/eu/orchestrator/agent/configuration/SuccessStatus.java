package eu.orchestrator.agent.configuration;

public enum SuccessStatus {

    STATUS_INITIALIZED(2),
    STATUS_IMAGE_DOWNLOADED(3),
    STATUS_WAITING_FOR_DEPENDENCIES(4),
    STATUS_TRIGGERED_CONTAINER_START(5),
    STATUS_STARTED(6),
    STATUS_LISTEN_FOR_COMMAND(7);

    private int value;

    SuccessStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
