package eu.orchestrator.backend.service.oss.intent.converters;

import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.repository.domain.Interface;
import eu.orchestrator.repository.domain.Interface.InterfaceType;
import eu.orchestrator.repository.domain.InterfaceInstance;
import eu.orchestrator.transfer.entities.oss.GraphLinkNodeInstanceTO;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class FakeAccessGraphLinkNodeInstanceGeneratorTest {

    private FakeAccessGraphLinkNodeInstanceGenerator generator;

    @Before
    public void setUp() {
        generator = new FakeAccessGraphLinkNodeInstanceGenerator(new StubbedFakeAccessGraphLinkIdProvider());
    }

    @Test
    public void shouldGenerateNothingIfComponentIsNotCandidateForFakeAccessLinkGeneration() {
        final ComponentNodeInstance nodeInstance = new ComponentNodeInstance();
        final ComponentNodeInstance loadBalancedByComponent = new ComponentNodeInstance();
        nodeInstance.setLoadBalancedBy(loadBalancedByComponent);
        nodeInstance.setLoadBalancer(false);

        final List<GraphLinkNodeInstanceTO> fakeLinks = generator.generate(Collections.singletonList(nodeInstance), Collections.emptyList());

        assertThat(fakeLinks).isEmpty();
    }

    @Test
    public void shouldGenerateNothingIfComponentIsCandidateForFakeAccessLinkGenerationButHasNoAccessInterface() {
        final ComponentNodeInstance nodeInstance = new ComponentNodeInstance();
        nodeInstance.setComponentNodeInstanceID(1L);
        nodeInstance.setLoadBalancer(true);
        final Interface iface = new Interface();
        iface.setInterfaceType(InterfaceType.CORE.name());
        final InterfaceInstance interfaceInstance = new InterfaceInstance();
        interfaceInstance.setInterfaceObj(iface);
        interfaceInstance.setInterfaceInstanceID(2L);
        nodeInstance.setInterfaceInstances(Collections.singletonList(interfaceInstance));
        final List<GraphLinkNodeInstanceTO> fakeLinks = generator.generate(Collections.singletonList(nodeInstance), Collections.emptyList());

        assertThat(fakeLinks).isEmpty();
    }

    @Test
    public void shouldGenerateNothingIfComponentAlreadyHasIncomingAccessLink() {
        final ComponentNodeInstance nodeInstance = new ComponentNodeInstance();
        nodeInstance.setComponentNodeInstanceID(1L);
        nodeInstance.setLoadBalancer(true);
        final GraphLinkNodeInstanceTO accessLinkTowardsComponent = new GraphLinkNodeInstanceTO();
        accessLinkTowardsComponent.setType(InterfaceType.ACCESS.name());
        accessLinkTowardsComponent.setToComponentNodeInstanceID(String.valueOf(nodeInstance.getComponentNodeInstanceID()));

        final List<GraphLinkNodeInstanceTO> fakeLinks = generator.generate(Collections.singletonList(nodeInstance),
                Collections.singletonList(accessLinkTowardsComponent));

        assertThat(fakeLinks).isEmpty();
    }

    @Test
    public void shouldGenerateFakeGraphLinkUsingExistingAccessLinkId() {
        final GraphLinkNodeInstanceTO expected = new GraphLinkNodeInstanceTO();
        expected.setGraphLinkNodeInstanceID("ACCESS_2");
        expected.setToComponentNodeInstanceID("1");
        expected.setType(InterfaceType.ACCESS.name());

        final ComponentNodeInstance nodeInstance = new ComponentNodeInstance();
        nodeInstance.setComponentNodeInstanceID(1L);
        nodeInstance.setLoadBalancer(true);
        final Interface iface = new Interface();
        iface.setInterfaceType(InterfaceType.ACCESS.name());
        final InterfaceInstance interfaceInstance = new InterfaceInstance();
        interfaceInstance.setInterfaceObj(iface);
        interfaceInstance.setInterfaceInstanceID(2L);
        nodeInstance.setInterfaceInstances(Collections.singletonList(interfaceInstance));
        final List<GraphLinkNodeInstanceTO> fakeLinks = generator.generate(Collections.singletonList(nodeInstance), Collections.emptyList());

        assertThat(fakeLinks).hasSize(1);
        final GraphLinkNodeInstanceTO fakeLink = fakeLinks.get(0);
        assertThat(fakeLink).usingRecursiveComparison().isEqualTo(expected);
    }
}
