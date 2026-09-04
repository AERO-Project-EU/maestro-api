package eu.orchestrator.backend.service.oss.intent.converters;

import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.repository.domain.GraphLinkNode;
import eu.orchestrator.repository.domain.GraphLinkNodeInstance;
import eu.orchestrator.repository.domain.Interface.InterfaceType;
import eu.orchestrator.transfer.entities.oss.GraphLinkNodeInstanceTO;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class GraphLinkNodeInstanceConverter implements Converter<GraphLinkNodeInstance, GraphLinkNodeInstanceTO> {

    @Override
    public GraphLinkNodeInstanceTO convert(GraphLinkNodeInstance source) {
        final GraphLinkNodeInstanceTO destination = new GraphLinkNodeInstanceTO();

        destination.setGraphLinkNodeInstanceID(String.valueOf(source.getGraphLinkNodeInstanceID()));

        destination.setType(getInterfaceType(source.getGraphLinkNode()));

        final ComponentNodeInstance componentNodeInstanceFrom = source.getComponentNodeInstanceFrom();
        destination.setFromComponentNodeInstanceID(String.valueOf(componentNodeInstanceFrom.getComponentNodeInstanceID()));

        final ComponentNodeInstance componentNodeInstanceTo = source.getComponentNodeInstanceTo();
        destination.setToComponentNodeInstanceID(String.valueOf(componentNodeInstanceTo.getComponentNodeInstanceID()));

        return destination;
    }

    private String getInterfaceType(GraphLinkNode graphLinkNode) {
        if (graphLinkNode != null) {
            return graphLinkNode.getGraphLink().getInterfaceObj().getInterfaceType();
        }

        return getDefaultInterfaceType().name();
    }

    private InterfaceType getDefaultInterfaceType() {
        return InterfaceType.CORE;
    }
}
