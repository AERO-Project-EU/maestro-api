package eu.orchestrator.backend.service.oss;

import eu.orchestrator.repository.domain.ProviderType.ProviderName;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

import static eu.orchestrator.repository.domain.ProviderType.ProviderName.FIFTH_GENERATION_OSS_KUBERNETES_PROVIDER;
import static eu.orchestrator.repository.domain.ProviderType.ProviderName.FIFTH_GENERATION_TELCO_PROVIDER;
import static eu.orchestrator.repository.domain.ProviderType.ProviderName.FIVE_G_INDUCE_SLICE;
import static eu.orchestrator.repository.domain.ProviderType.ProviderName.KUBERNETES;
import static eu.orchestrator.repository.domain.ProviderType.ProviderName.NEXTWORKS_OSS;
import static eu.orchestrator.repository.domain.ProviderType.ProviderName.OPENSTACK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@RunWith(JUnitParamsRunner.class)
public class ProviderNameReducerTest {

    private ProviderNameReducer providerNameReducer;

    @Before
    public void setUp() {
        providerNameReducer = new ProviderNameReducer();
    }

    public static List<ProviderName[]> providerNameTestData() {
        return Arrays.asList(new ProviderName[][] {
                {FIFTH_GENERATION_TELCO_PROVIDER, OPENSTACK},
                {FIFTH_GENERATION_OSS_KUBERNETES_PROVIDER, KUBERNETES},
                {FIVE_G_INDUCE_SLICE, KUBERNETES},
                {KUBERNETES, KUBERNETES},
                {NEXTWORKS_OSS, KUBERNETES},
        });
    }

    @Test
    @Parameters(method = "providerNameTestData")
    public void testReducer(ProviderName given, ProviderName expected) {
        assertThat(providerNameReducer.reduce(given)).isEqualTo(expected);
    }

    @Test
    public void shouldThrowUponUnsupportedProviderName() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> providerNameReducer.reduce(ProviderName.USER_DEFINED));
    }

    public static List<Object[]> isK8sBasedProviderTestData() {
        return Arrays.asList(new Object[][] {
                {FIFTH_GENERATION_TELCO_PROVIDER, false},
                {FIFTH_GENERATION_OSS_KUBERNETES_PROVIDER, true},
                {FIVE_G_INDUCE_SLICE, true},
                {KUBERNETES, true},
                {NEXTWORKS_OSS, true},
        });
    }

    @Test
    @Parameters(method = "isK8sBasedProviderTestData")
    public void shouldProvideCheckForK8sBasedProvider(ProviderName providerName, boolean expectedPredicateAnswer) {
        final boolean isKubernetesBasedProvider = providerNameReducer.isKubernetesBasedProvider(providerName);

        assertThat(isKubernetesBasedProvider).isEqualTo(expectedPredicateAnswer);
    }

    public static List<Object[]> isOpenStackBasedProviderTestData() {
        return Arrays.asList(new Object[][] {
                {FIFTH_GENERATION_TELCO_PROVIDER, true},
                {FIFTH_GENERATION_OSS_KUBERNETES_PROVIDER, false},
                {FIVE_G_INDUCE_SLICE, false},
                {KUBERNETES, false},
                {NEXTWORKS_OSS, false},
        });
    }

    @Test
    @Parameters(method = "isOpenStackBasedProviderTestData")
    public void shouldProvideCheckForOpenStackBasedProvider(ProviderName providerName, boolean expectedPredicateAnswer) {
        final boolean isOpenStackBasedProvider = providerNameReducer.isOpenStackBasedProvider(providerName);

        assertThat(isOpenStackBasedProvider).isEqualTo(expectedPredicateAnswer);
    }
}
