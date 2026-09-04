package eu.orchestrator.backend.service.oss.intent.converters;

public interface FakeAccessGraphLinkIdProvider {

    String generateIdentifier(long identifier);

    boolean isFakeAccessGraphLink(String identifier);

    long extractInterfaceInstanceIdentifier(String identifier);
}
