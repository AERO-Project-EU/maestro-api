package eu.orchestrator.backend.service.oss.intent.converters;

public class StubbedFakeAccessGraphLinkIdProvider implements FakeAccessGraphLinkIdProvider {

    @Override
    public String generateIdentifier(long identifier) {
        return "ACCESS_" + identifier;
    }

    @Override
    public boolean isFakeAccessGraphLink(String identifier) {
        return false;
    }

    @Override
    public long extractInterfaceInstanceIdentifier(String identifier) {
        return 0;
    }
}
