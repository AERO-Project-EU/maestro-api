package eu.orchestrator.common.enums;

public enum ProjectEnum {

    MAESTRO("Maestro"),
    RAINBOW("Rainbow");

    private final String friendlyName;

    ProjectEnum(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

}
