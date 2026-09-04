package eu.orchestrator.backend.service.oss.intent.converters;

import eu.orchestrator.backend.util.Util;
import eu.orchestrator.common.util.NullCheckUtil;
import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.repository.domain.Interface.InterfaceType;
import eu.orchestrator.repository.domain.InterfaceInstance;
import eu.orchestrator.transfer.entities.oss.GraphLinkNodeInstanceTO;

import org.springframework.stereotype.Component;

import jakarta.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

@Component
public class FakeAccessGraphLinkNodeInstanceGenerator {

    private static final Predicate<InterfaceInstance> IS_ACCESS_INTERFACE = interfaceInstance -> interfaceInstance.hasInterfaceType(InterfaceType.ACCESS);
    private final FakeAccessGraphLinkIdProvider fakeAccessGraphLinkIdProvider;

    @Inject
    public FakeAccessGraphLinkNodeInstanceGenerator(FakeAccessGraphLinkIdProvider fakeAccessGraphLinkIdProvider) {
        this.fakeAccessGraphLinkIdProvider = fakeAccessGraphLinkIdProvider;
    }

    public List<GraphLinkNodeInstanceTO> generate(Collection<ComponentNodeInstance> componentNodeInstances,
            List<GraphLinkNodeInstanceTO> alreadyCreatedGraphLinkNodeInstances) {
        final Predicate<ComponentNodeInstance> componentIsNotAccessible
                = componentNodeInstance -> !isComponentAccessible(componentNodeInstance.getComponentNodeInstanceID(), alreadyCreatedGraphLinkNodeInstances);

        return componentNodeInstances
                .stream()
                .filter(this::isCandidateForFakeGraphLinkAccessInterfaceType)
                .filter(componentIsNotAccessible)
                .filter(this::hasAccessInterface)
                .map(this::generateFakeAccessGraphLinkNodeInstance)
                .collect(toList());
    }

    /**
     * A component is a candidate for fake i. incoming ii. access link generation iff: - is not load balanced by another component - is load balancer
     */
    private boolean isCandidateForFakeGraphLinkAccessInterfaceType(ComponentNodeInstance nodeInstance) {
        final boolean isNotLoadBalancedByOtherComponent = nodeInstance.getLoadBalancedBy() == null;
        final boolean actsAsLoadBalancer = nodeInstance.getLoadBalancer();

        return isNotLoadBalancedByOtherComponent || actsAsLoadBalancer;
    }

    /**
     * Tests whether the current component has an incoming ACCESS graph link.
     */
    private boolean isComponentAccessible(long componentNodeInstanceId, List<GraphLinkNodeInstanceTO> graphLinkNodeInstances) {
        final String instanceId = String.valueOf(componentNodeInstanceId);

        final Predicate<GraphLinkNodeInstanceTO> isAccessGraphLink = instance -> instance.getType().equals(InterfaceType.ACCESS.name());
        final Predicate<GraphLinkNodeInstanceTO> isLinkTowardsComponent = instance -> instance.getToComponentNodeInstanceID().equals(instanceId);
        final Predicate<GraphLinkNodeInstanceTO> isComponentAccessible = isAccessGraphLink.and(isLinkTowardsComponent);

        return graphLinkNodeInstances.stream().anyMatch(isComponentAccessible);
    }

    private boolean hasAccessInterface(ComponentNodeInstance componentNodeInstance) {
        final List<InterfaceInstance> interfaceInstances = componentNodeInstance.getInterfaceInstances();
        if (NullCheckUtil.isEmpty(interfaceInstances)) {
            return false;
        }

        // In the original source, these are retrieved via a DAO in an orderByDateCreatedDesc fashion, i.e., order matters.
        // TODO Does the order really matter here? If so, take it into account and probably fetch via the DAO.
        return interfaceInstances.stream().anyMatch(IS_ACCESS_INTERFACE);
    }

    private GraphLinkNodeInstanceTO generateFakeAccessGraphLinkNodeInstance(ComponentNodeInstance componentNodeInstance) {
        final GraphLinkNodeInstanceTO graphLinkNodeInstance = new GraphLinkNodeInstanceTO();

        final String fakeGraphLinkAccessId = generateFakeGraphLinkAccessId(componentNodeInstance);
        graphLinkNodeInstance.setGraphLinkNodeInstanceID(fakeGraphLinkAccessId);

        graphLinkNodeInstance.setType(InterfaceType.ACCESS.name());

        final Long componentNodeInstanceID = componentNodeInstance.getComponentNodeInstanceID();
        graphLinkNodeInstance.setToComponentNodeInstanceID(String.valueOf(componentNodeInstanceID));

        return graphLinkNodeInstance;
    }

    private String generateFakeGraphLinkAccessId(ComponentNodeInstance componentNodeInstance) {
        final InterfaceInstance firstAccessInterface = getFirstAccessInterface(componentNodeInstance.getInterfaceInstances());

        final long graphLinkAccessId = firstAccessInterface.getInterfaceInstanceID();

        return fakeAccessGraphLinkIdProvider.generateIdentifier(graphLinkAccessId);
    }

    // This will always return a value, because the respective filter ("hasAccessInterface") has been called.
    private InterfaceInstance getFirstAccessInterface(List<InterfaceInstance> interfaceInstances) {
        if (NullCheckUtil.isEmpty(interfaceInstances)) {
            throw new IllegalArgumentException("interfaceInstances cannot be empty");
        }

        final Optional<InterfaceInstance> accessInterface = interfaceInstances.stream().filter(IS_ACCESS_INTERFACE).findFirst();
        if (!accessInterface.isPresent()) {
            throw new IllegalArgumentException("Could not find any ACCESS interface instances");
        }

        return accessInterface.get();
    }
}
