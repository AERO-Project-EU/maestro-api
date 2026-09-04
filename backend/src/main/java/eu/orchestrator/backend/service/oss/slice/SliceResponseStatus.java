package eu.orchestrator.backend.service.oss.slice;

public enum SliceResponseStatus {
    SUCCESS("success"),
    ERROR("error");

    private final String text;

    SliceResponseStatus(String text) {
        this.text = text;
    }

    public static SliceResponseStatus fromString(String value) {
        for (SliceResponseStatus status: SliceResponseStatus.values()) {
            if (status.text.equalsIgnoreCase(value)) {
                return status;
            }
        }

        throw new IllegalArgumentException("No constant with text " + value + " found");
    }
}
