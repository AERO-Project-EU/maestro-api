package eu.orchestrator.backend.service.oss.intent;

import eu.orchestrator.backend.service.oss.OssConfiguration;
import eu.orchestrator.backend.service.oss.ProviderNameReducer;
import eu.orchestrator.backend.service.oss.intent.converters.ComponentNodeInstanceConverter;
import eu.orchestrator.backend.service.oss.intent.converters.ConstraintConverter;
import eu.orchestrator.backend.service.oss.intent.converters.FakeAccessGraphLinkNodeInstanceGenerator;
import eu.orchestrator.backend.service.oss.intent.converters.GraphLinkNodeInstanceConverter;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.Provider;
import eu.orchestrator.repository.domain.ProviderType;
import eu.orchestrator.repository.domain.ProviderType.ProviderName;
import eu.orchestrator.transfer.entities.oss.ComponentNodeInstanceTO;
import eu.orchestrator.transfer.entities.oss.ConstraintTO;
import eu.orchestrator.transfer.entities.oss.GraphLinkNodeInstanceTO;
import eu.orchestrator.transfer.entities.oss.InfrastructureType;
import eu.orchestrator.transfer.entities.oss.SliceIntent;
import eu.orchestrator.transfer.entities.oss.SliceOrchestrator;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import jakarta.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import static eu.orchestrator.backend.util.Util.toJson;
import static eu.orchestrator.transfer.entities.oss.InfrastructureType.KUBERNETES;
import static eu.orchestrator.transfer.entities.oss.InfrastructureType.OPENSTACK;
import static java.util.stream.Collectors.toList;

@Component
public class SliceIntentConverter implements Converter<ApplicationInstance, SliceIntent> {

    private static final Logger LOGGER = Logger.getLogger(SliceIntentConverter.class.getName());

    private final OssConfiguration ossConfiguration;
    private final ComponentNodeInstanceConverter componentNodeInstanceConverter;
    private final ConstraintConverter constraintConverter;
    private final GraphLinkNodeInstanceConverter graphLinkNodeInstanceConverter;
    private final FakeAccessGraphLinkNodeInstanceGenerator fakeAccessGraphLinkNodeInstanceGenerator;

    @Inject
    public SliceIntentConverter(OssConfiguration ossConfiguration,
            ComponentNodeInstanceConverter componentNodeInstanceConverter,
            ConstraintConverter constraintConverter,
            GraphLinkNodeInstanceConverter graphLinkNodeInstanceConverter,
            FakeAccessGraphLinkNodeInstanceGenerator fakeAccessGraphLinkNodeInstanceGenerator) {
        this.ossConfiguration = ossConfiguration;
        this.componentNodeInstanceConverter = componentNodeInstanceConverter;
        this.constraintConverter = constraintConverter;
        this.graphLinkNodeInstanceConverter = graphLinkNodeInstanceConverter;
        this.fakeAccessGraphLinkNodeInstanceGenerator = fakeAccessGraphLinkNodeInstanceGenerator;
    }

    @Override
    public SliceIntent convert(ApplicationInstance source) {
        final SliceIntent intent = new SliceIntent();
        intent.setName(source.getName());
        intent.setApplicationInstanceID(String.valueOf(source.getApplicationInstanceID()));
        intent.setCallbackURL(ossConfiguration.getCallBackUrl(source.getApplicationInstanceID()));
        intent.setDescription(source.getDescription());

        final InfrastructureType infrastructureType = fromProviderName(source.getProvider());
        if (infrastructureType.equals(OPENSTACK)) {
            throw new UnsupportedOperationException(
                    String.format("Infrastructure type [%s] is not supported yet", OPENSTACK));
        }
        intent.setInfrastructureType(infrastructureType);

        intent.setSliceOrchestrator(getOrchestratorFromProviderName(source.getProvider()));

        final List<ComponentNodeInstanceTO> nodeInstances = mapTo(source.getComponentNodeInstances(), componentNodeInstanceConverter::convert);
        intent.setComponentNodeInstances(nodeInstances);

        // TODO: Request regions from OSS and do a id-for-name substitution for values of all component hosting constraints with metric REGION
        // TODO: This ^, is to be done outside the converter.
        final List<ConstraintTO> constraints = mapTo(source.getConstraints(), constraintConverter::convert);
        intent.setConstraints(constraints);

        final List<GraphLinkNodeInstanceTO> graphLinkNodeInstances = mapTo(source.getGraphLinkNodeInstances(), graphLinkNodeInstanceConverter::convert);
        final List<GraphLinkNodeInstanceTO> accessGraphLinkNodeInstances = fakeAccessGraphLinkNodeInstanceGenerator.generate(
                source.getComponentNodeInstances(), graphLinkNodeInstances);
        graphLinkNodeInstances.addAll(accessGraphLinkNodeInstances);
        intent.setGraphLinkNodeInstances(graphLinkNodeInstances);

        LOGGER.log(Level.INFO, "Translated slice intent for application instance [{0}]: is {1}",
                new Object[]{source.getApplicationInstanceID(), toJson(intent)});

        return intent;
    }

    private InfrastructureType fromProviderName(Provider provider) {
        final ProviderName providerName = extractProviderName(provider, "Cannot infer infrastructure type from null provider[.type]");

        final ProviderNameReducer providerNameReducer = new ProviderNameReducer();

        if (providerNameReducer.isKubernetesBasedProvider(providerName)) {
            return KUBERNETES;
        } else if (providerNameReducer.isOpenStackBasedProvider(providerName)) {
            return OPENSTACK;
        }

        throw new IllegalArgumentException(
                String.format("Cannot infer infrastructure type for provider of type [%s]", providerName));
    }

    private ProviderName extractProviderName(Provider provider, String errorMessage) {
        if (provider == null) {
            throw new IllegalArgumentException(errorMessage);
        }

        final ProviderType providerType = provider.getProviderType();
        if (providerType == null) {
            throw new IllegalArgumentException(errorMessage);
        }

        return providerType.getProviderName();
    }

    private SliceOrchestrator getOrchestratorFromProviderName(Provider provider) {
        final ProviderName providerName = extractProviderName(provider, "Cannot infer slice orchestrator from null provider[.type]");

        switch (providerName) {
            case FIFTH_GENERATION_OSS_KUBERNETES_PROVIDER:
                return SliceOrchestrator.OSS;
            case NEXTWORKS_OSS:
                return SliceOrchestrator.NEXTWORKS;
            default:
                throw new IllegalArgumentException(String.format("Unsupported slice orchestrator \"%s\". Aborting...", providerName));
        }
    }


    private static <T, R> List<R> mapTo(Collection<T> collection, Function<T, R> mapper) {
        return collection.stream().map(mapper).collect(toList());
    }
}
