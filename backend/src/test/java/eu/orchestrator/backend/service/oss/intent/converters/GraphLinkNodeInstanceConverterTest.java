package eu.orchestrator.backend.service.oss.intent.converters;

import eu.orchestrator.backend.service.oss.intent.converters.GraphLinkNodeInstanceConverter;
import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.repository.domain.GraphLink;
import eu.orchestrator.repository.domain.GraphLinkNode;
import eu.orchestrator.repository.domain.GraphLinkNodeInstance;
import eu.orchestrator.repository.domain.Interface;
import eu.orchestrator.repository.domain.Interface.InterfaceType;
import eu.orchestrator.transfer.entities.oss.GraphLinkNodeInstanceTO;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GraphLinkNodeInstanceConverterTest {
    private GraphLinkNodeInstanceConverter converter;

    @Before
    public void setUp() {
        converter = new GraphLinkNodeInstanceConverter();
    }

    @Test
    public void shouldConvertGraphLinkNodeInstanceWithAccessInterfaceType() {
        GraphLinkNodeInstanceTO expected = new GraphLinkNodeInstanceTO();
        expected.setGraphLinkNodeInstanceID("1");
        expected.setType(InterfaceType.ACCESS.name());
        expected.setFromComponentNodeInstanceID("2");
        expected.setToComponentNodeInstanceID("3");

        final GraphLinkNodeInstance source = new GraphLinkNodeInstance();
        source.setGraphLinkNodeInstanceID(1L);
        final ComponentNodeInstance from = new ComponentNodeInstance();
        from.setComponentNodeInstanceID(2L);
        source.setComponentNodeInstanceFrom(from);
        final ComponentNodeInstance to = new ComponentNodeInstance();
        to.setComponentNodeInstanceID(3L);
        source.setComponentNodeInstanceTo(to);
        final InterfaceType ifaceType = InterfaceType.ACCESS;
        final Interface iface = new Interface();
        iface.setInterfaceType(ifaceType.name());
        final GraphLink graphLink = new GraphLink();
        graphLink.setInterfaceObj(iface);
        final GraphLinkNode graphLinkNode = new GraphLinkNode();
        graphLinkNode.setGraphLink(graphLink);
        source.setGraphLinkNode(graphLinkNode);
        final GraphLinkNodeInstanceTO converted = converter.convert(source);

        assertThat(converted).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    public void shouldConvertGraphLinkNodeInstanceWithDefaultInterfaceType() {
        GraphLinkNodeInstanceTO expected = new GraphLinkNodeInstanceTO();
        expected.setGraphLinkNodeInstanceID("1");
        expected.setType(InterfaceType.CORE.name());
        expected.setFromComponentNodeInstanceID("2");
        expected.setToComponentNodeInstanceID("3");

        final GraphLinkNodeInstance source = new GraphLinkNodeInstance();
        source.setGraphLinkNodeInstanceID(1L);
        final ComponentNodeInstance from = new ComponentNodeInstance();
        from.setComponentNodeInstanceID(2L);
        source.setComponentNodeInstanceFrom(from);
        final ComponentNodeInstance to = new ComponentNodeInstance();
        to.setComponentNodeInstanceID(3L);
        source.setComponentNodeInstanceTo(to);
        final GraphLinkNodeInstanceTO converted = converter.convert(source);

        assertThat(converted).usingRecursiveComparison().isEqualTo(expected);
    }
}
