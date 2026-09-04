package eu.orchestrator.transfer.entities.policyEngine;

public enum PolicyEngineActionType {

    SCALE_OUT("SCALE_OUT", "Scale Out"),
    SCALE_IN("SCALE_IN","Scale In");
    private final String action;
    private final String friendlyName;

    PolicyEngineActionType(String action, String friendlyName) {
        this.action = action;
        this.friendlyName = friendlyName;
    }

    public String getAction() {
        return action;
    }

    public static PolicyEngineActionType getFriendlyName(String action) {
        for(PolicyEngineActionType ot: PolicyEngineActionType.values()) {
            if (ot.action.equals(action)) return ot;
        }

        return null;
    }
}