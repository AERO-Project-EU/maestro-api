package eu.orchestrator.backend.service.oss.intent.converters;

import eu.orchestrator.common.util.NullCheckUtil;
import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.transfer.entities.oss.ComponentNodeInstanceTO;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;


@Component
public class ComponentNodeInstanceConverter implements Converter<ComponentNodeInstance, ComponentNodeInstanceTO> {

    @Override
    public ComponentNodeInstanceTO convert(ComponentNodeInstance source) {
        if (NullCheckUtil.isEmpty(source) || NullCheckUtil.isEmpty(source.getComponentNodeInstanceID()) || NullCheckUtil.isEmpty(source.getName())) {
            throw new IllegalArgumentException("Input is null");
        }

        final ComponentNodeInstanceTO nodeInstance = new ComponentNodeInstanceTO();

        nodeInstance.setComponentNodeInstanceID(String.valueOf(source.getComponentNodeInstanceID()));
        nodeInstance.setComponentNodeInstanceName(source.getName());

        return nodeInstance;
    }
}
