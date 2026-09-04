package eu.orchestrator.backend.service.oss.intent.converters;

import org.springframework.stereotype.Component;

@Component
public class FakeAccessGraphLinkIdProviderImpl implements FakeAccessGraphLinkIdProvider {

    private static final String FAKE_GRAPH_LINK_ACCESS_ID_PREFIX = "ACCESS_";

    @Override
    public String generateIdentifier(long identifier) {
        // TODO clarify whether the pattern "ACCESS_" + interfaceInstance.getInterfaceInstanceID() is of importance
        return String.format("%s%d", FAKE_GRAPH_LINK_ACCESS_ID_PREFIX, identifier);
    }

    @Override
    public boolean isFakeAccessGraphLink(String identifier) {
        return identifier.startsWith(FAKE_GRAPH_LINK_ACCESS_ID_PREFIX);
    }

    @Override
    public long extractInterfaceInstanceIdentifier(String identifier) {
        final String interfaceInstanceIdentifier = identifier.substring(FAKE_GRAPH_LINK_ACCESS_ID_PREFIX.length());
        return Long.parseLong(interfaceInstanceIdentifier);
    }
}
