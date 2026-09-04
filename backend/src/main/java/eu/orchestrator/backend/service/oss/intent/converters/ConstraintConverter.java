package eu.orchestrator.backend.service.oss.intent.converters;

import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.repository.domain.Constraint;
import eu.orchestrator.repository.domain.Constraint.ConstraintCategory;
import eu.orchestrator.repository.domain.Country;
import eu.orchestrator.repository.domain.InterfaceInstance;
import eu.orchestrator.transfer.entities.oss.ConstraintTO;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ConstraintConverter implements Converter<Constraint, ConstraintTO> {

    @Override
    public ConstraintTO convert(Constraint source) {
        final ConstraintTO constraint = new ConstraintTO();
        constraint.setConstraintID(String.valueOf(source.getConstraintID()));
        constraint.setType(source.getConstraintType());

        final ConstraintCategory category = ConstraintCategory.valueOf(source.getConstraintCategory());
        constraint.setCategory(category.name());
        switch (category) {
            case COMPONENT_HOSTING:
                populateComponentHostingConstraint(source, constraint);
                break;
            case ACCESS:
                populateAccessConstraint(source, constraint);
                break;
            case GRAPH_LINK:
                populateGraphLinkConstraint(source, constraint);
                break;
            default:
                throw new IllegalArgumentException(
                        String.format("Constraint category [%s] not supported yet", category.name()));
        }

        return constraint;
    }

    private void populateComponentHostingConstraint(Constraint source, ConstraintTO constraint) {
        final ComponentNodeInstance componentNodeInstance = source.getComponentNodeInstance();
        constraint.setComponentNodeInstanceID(String.valueOf(componentNodeInstance.getComponentNodeInstanceID()));

        constraint.setConstraintMetric(source.getConstraintMetric());
        constraint.setConstraintValue(source.getConstraintValue());
        constraint.setConstraintUnit(source.getConstraintUnit());
    }

    private void populateAccessConstraint(Constraint source, ConstraintTO constraint) {
        final InterfaceInstance interfaceInstance = source.getInterfaceInstance();

        final Long interfaceInstanceID = interfaceInstance.getInterfaceInstanceID();
        constraint.setInterfaceInstanceID(String.valueOf(interfaceInstanceID));

        final ComponentNodeInstance componentNodeInstance = interfaceInstance.getComponentNodeInstance();
        final Long componentNodeInstanceID = componentNodeInstance.getComponentNodeInstanceID();
        constraint.setComponentNodeInstanceID(String.valueOf(componentNodeInstanceID));

        final Integer sstValue = source.getRadioServiceType().getSstValue();
        constraint.setRadioServiceType(String.valueOf(sstValue));

        final Country country = source.getCountry();
        if (country != null) {
            constraint.setLocation(country.getName());
        }

        constraint.setQi(source.getQi().getQiValue());
        constraint.setResourceType(source.getResourceType());
        constraint.setAllocationRetentionPriorityProfile(source.getAllocationRetentionPriorityProfile());
        constraint.setMinimumGuaranteedBandwidth(source.getMinimumGuaranteedBandwidth());
        constraint.setMaximumRequiredBandwidth(source.getMaximumRequiredBandwidth());
    }

    private void populateGraphLinkConstraint(Constraint source, ConstraintTO constraint) {
        final Long graphLinkNodeInstanceID = source.getGraphLinkNodeInstance().getGraphLinkNodeInstanceID();
        constraint.setGraphLinkNodeInstanceID(String.valueOf(graphLinkNodeInstanceID));

        constraint.setConstraintMetric(source.getConstraintMetric());
        constraint.setConstraintValue(source.getConstraintValue());
        constraint.setConstraintUnit(source.getConstraintUnit());
    }
}
