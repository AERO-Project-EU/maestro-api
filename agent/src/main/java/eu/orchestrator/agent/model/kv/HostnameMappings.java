package eu.orchestrator.agent.model.kv;

import java.util.ArrayList;
import java.util.List;

public class HostnameMappings {

    private List<String> entries = new ArrayList<>();

    public List<String> getEntries() {
        return entries;
    }

    public void setEntries(List<String> entries) {
        this.entries = entries;
    }
}
