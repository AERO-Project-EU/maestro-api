package eu.orchestrator.backend.service.oss;

import eu.orchestrator.repository.domain.ProviderType.ProviderName;

import static eu.orchestrator.repository.domain.ProviderType.ProviderName.KUBERNETES;
import static eu.orchestrator.repository.domain.ProviderType.ProviderName.OPENSTACK;

public class ProviderNameReducer {

    public ProviderName reduce(ProviderName given) {
        switch (given) {
            case FIFTH_GENERATION_TELCO_PROVIDER:
                return OPENSTACK;
            case FIFTH_GENERATION_OSS_KUBERNETES_PROVIDER:
            case FIVE_G_INDUCE_SLICE:
            case KUBERNETES:
            case NEXTWORKS_OSS:
                return KUBERNETES;
            default:
                throw new IllegalArgumentException(String.format("ProviderName '%s' is not yet supported for reduction", given));
        }
    }

    public boolean isKubernetesBasedProvider(ProviderName providerName) {
        final ProviderName reducedProvider = reduce(providerName);
        return reducedProvider.equals(KUBERNETES);
    }

    public boolean isOpenStackBasedProvider(ProviderName providerName) {
        final ProviderName reducedProvider = reduce(providerName);
        return reducedProvider.equals(OPENSTACK);
    }
}
