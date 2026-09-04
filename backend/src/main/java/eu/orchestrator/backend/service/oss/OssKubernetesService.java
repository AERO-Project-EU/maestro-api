package eu.orchestrator.backend.service.oss;

import eu.orchestrator.backend.service.k8s.KubernetesService;
import eu.orchestrator.backend.service.k8s.resources.NamespaceService;
import eu.orchestrator.backend.util.Util;
import eu.orchestrator.common.util.NullCheckUtil;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.Provider;
import eu.orchestrator.repository.domain.ProviderType;
import eu.orchestrator.repository.domain.ProviderType.ProviderName;
import eu.orchestrator.repository.domain.Slice;
import eu.orchestrator.repository.domain.SliceProvider;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import org.springframework.stereotype.Service;

import jakarta.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static eu.orchestrator.repository.domain.ProviderType.ProviderName.FIFTH_GENERATION_OSS_KUBERNETES_PROVIDER;
import static eu.orchestrator.repository.domain.ProviderType.ProviderName.NEXTWORKS_OSS;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;

@Service
public class OssKubernetesService extends KubernetesService {

    private static final Logger LOGGER = Logger.getLogger(OssKubernetesService.class.getName());
    public static final String OSS_KUBERNETES_NET = "OssKubernetesNet";

    private final NamespaceService namespaceService;

    @Inject
    public OssKubernetesService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    @Override
    protected Config getKubernetesConfig(ApplicationInstance applicationInstance) {
        final Optional<Provider> ossSuppliedProvider = getOssSuppliedProvider(applicationInstance);
        if (!ossSuppliedProvider.isPresent()) {
            LOGGER.log(SEVERE, "Could not retrieve a valid OSS based K8s provider for application instance [id={0}]",
                    applicationInstance.getApplicationInstanceID());
            return null;
        }

        return ossSuppliedProvider.map(this::extractKubernetesConfig).orElse(null);
    }

    private Config extractKubernetesConfig(Provider provider) {
        final String masterUrl = provider.getEndpoint();
        final String caCertData = provider.getUsername();
        final String token = provider.getPublicKey();

        LOGGER.log(INFO, "Creating a K8s config for cluster listening at \"{0}\"", masterUrl);

        return new ConfigBuilder().withMasterUrl(masterUrl).withCaCertData(caCertData).withOauthToken(token).build();
    }

    /**
     * Retrieves an OSS supplied K8s provider.
     * <p>
     * First, we retrieve said provider and then, check whether the <code>privateKey</code> field is <b>not</b> set. The K8s provider is retrieved via this
     * path: application instance => slice => slice provider => provider.
     *
     * @return An OSS supplied K8s provider, otherwise <code>Optional.empty</code>
     */
    private static Optional<Provider> getOssSuppliedProvider(ApplicationInstance applicationInstance) {
        final Slice slice = applicationInstance.getSlice();
        if (NullCheckUtil.isEmpty(slice)) {
            return Optional.empty();
        }

        final List<SliceProvider> providers = slice.getProviders();
        if (NullCheckUtil.isEmpty(providers)) {
            return Optional.empty();
        }

        // Retrieve the first provider, assuming single cluster deployments.
        final SliceProvider sliceProvider = providers.get(0);
        final Provider ossSuppliedProvider = sliceProvider.getProvider();
        if (NullCheckUtil.isEmpty(ossSuppliedProvider)) {
            return Optional.empty();
        }

        final boolean isOssDeployment = NullCheckUtil.isEmpty(ossSuppliedProvider.getPrivateKey());
        return isOssDeployment ? Optional.of(ossSuppliedProvider) : Optional.empty();
    }

    @Override
    protected Optional<String> provideNamespace(ApplicationInstance applicationInstance) {
        if (applicationInstance == null) {
            LOGGER.log(SEVERE, "Application instance cannot be null");
            return Optional.empty();
        }

        final Optional<Provider> ossSuppliedProvider = getOssSuppliedProvider(applicationInstance);
        if (!ossSuppliedProvider.isPresent()) {
            LOGGER.log(SEVERE, "Application instance [id={0}] provider cannot be null", applicationInstance.getApplicationInstanceID());
            return Optional.empty();
        }

        final Optional<String> namespace = ossSuppliedProvider.map(Provider::getProject);
        if (!namespace.isPresent()) {
            return Optional.empty();
        }
        LOGGER.log(Level.INFO, "K8s namespace for application instance [id={0}] is: \"{1}\"",
                new Object[]{applicationInstance.getApplicationInstanceID(), namespace.get()});

        return namespace;
    }

    /**
     * Solely checks whether the supplied configuration has access to the given namespace.
     * <p>
     * In this deployment scenario, the namespace is already created by OSS.
     */
    @Override
    protected boolean namespaceDeployment(String namespace, Config config) {
        return namespaceService.doesNamespaceExist(namespace, config);
    }

    @Override
    protected List<ProviderName> getSupportedProviders() {
        return Arrays.asList(FIFTH_GENERATION_OSS_KUBERNETES_PROVIDER, NEXTWORKS_OSS);
    }

    public boolean isSupportedOssProvider(Provider provider) {
        final ProviderType providerType = provider.getProviderType();
        if (NullCheckUtil.isEmpty(providerType)) {
            return false;
        }

        return getSupportedProviders().contains(providerType.getProviderName());
    }

    @Override
    protected String getComponentsNetworkName(String providerName) {
        return OSS_KUBERNETES_NET;
    }
}
