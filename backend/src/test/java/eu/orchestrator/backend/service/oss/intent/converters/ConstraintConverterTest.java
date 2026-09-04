package eu.orchestrator.backend.service.oss.intent.converters;

import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.repository.domain.Constraint;
import eu.orchestrator.repository.domain.Constraint.ConstraintCategory;
import eu.orchestrator.repository.domain.Country;
import eu.orchestrator.repository.domain.GraphLinkNodeInstance;
import eu.orchestrator.repository.domain.InterfaceInstance;
import eu.orchestrator.repository.domain.QI;
import eu.orchestrator.repository.domain.RadioServiceType;
import eu.orchestrator.transfer.entities.oss.ConstraintTO;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ConstraintConverterTest {

    public static final String CONSTRAINT_TYPE_HARD = "HARD";
    public static final String CONSTRAINT_METRIC_RAM = "RAM";
    public static final String CONSTRAINT_UNIT_GB = "GB";
    public static final String GREECE = "Greece";
    private ConstraintConverter converter;

    @Before
    public void setUp() {
        converter = new ConstraintConverter();
    }

    @Test
    public void shouldConvertComponentHostingConstraint() {
        final ConstraintTO expected = new ConstraintTO();
        expected.setConstraintID("1");
        expected.setType(CONSTRAINT_TYPE_HARD);
        expected.setCategory(ConstraintCategory.COMPONENT_HOSTING.name());
        expected.setComponentNodeInstanceID("10");
        expected.setConstraintMetric(CONSTRAINT_METRIC_RAM);
        expected.setConstraintValue("2");
        expected.setConstraintUnit(CONSTRAINT_UNIT_GB);

        final Constraint source = new Constraint();
        source.setConstraintID(1L);
        source.setConstraintType(CONSTRAINT_TYPE_HARD);
        source.setConstraintCategory(ConstraintCategory.COMPONENT_HOSTING.name());
        final ComponentNodeInstance nodeInstance = new ComponentNodeInstance();
        nodeInstance.setComponentNodeInstanceID(10L);
        source.setComponentNodeInstance(nodeInstance);
        source.setConstraintMetric(CONSTRAINT_METRIC_RAM);
        source.setConstraintValue("2");
        source.setConstraintUnit(CONSTRAINT_UNIT_GB);
        final ConstraintTO converted = converter.convert(source);

        assertThat(converted).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    public void shouldConvertAccessConstraint() {
        final ConstraintTO expected = new ConstraintTO();
        expected.setConstraintID("1");
        expected.setType(CONSTRAINT_TYPE_HARD);
        expected.setCategory(ConstraintCategory.ACCESS.name());
        expected.setInterfaceInstanceID("2");
        expected.setComponentNodeInstanceID("2");
        expected.setRadioServiceType("10");
        expected.setRadioServiceType("10");
        expected.setLocation(GREECE);
        expected.setQi("qiValue");
        expected.setResourceType("resourceType");
        expected.setAllocationRetentionPriorityProfile(3);
        expected.setMinimumGuaranteedBandwidth(4d);
        expected.setMaximumRequiredBandwidth(5d);

        final Constraint source = new Constraint();
        source.setConstraintID(1L);
        source.setConstraintType(CONSTRAINT_TYPE_HARD);
        source.setConstraintCategory(ConstraintCategory.ACCESS.name());
        final ComponentNodeInstance componentNodeInstance = new ComponentNodeInstance();
        componentNodeInstance.setComponentNodeInstanceID(2L);
        final InterfaceInstance interfaceInstance = new InterfaceInstance();
        interfaceInstance.setInterfaceInstanceID(2L);
        interfaceInstance.setComponentNodeInstance(componentNodeInstance);
        source.setInterfaceInstance(interfaceInstance);
        final RadioServiceType radioServiceType = new RadioServiceType();
        radioServiceType.setSstValue(10);
        source.setRadioServiceType(radioServiceType);
        final Country country = new Country();
        country.setName(GREECE);
        source.setCountry(country);
        final QI qi = new QI();
        qi.setQiValue("qiValue");
        source.setQi(qi);
        source.setResourceType("resourceType");
        source.setAllocationRetentionPriorityProfile(3);
        source.setMinimumGuaranteedBandwidth(4d);
        source.setMaximumRequiredBandwidth(5d);
        final ConstraintTO converted = converter.convert(source);

        assertThat(converted).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    public void shouldConvertGraphLinkConstraint() {
        final ConstraintTO expected = new ConstraintTO();
        expected.setConstraintID("1");
        expected.setType(CONSTRAINT_TYPE_HARD);
        expected.setCategory(ConstraintCategory.GRAPH_LINK.name());
        expected.setGraphLinkNodeInstanceID("2");
        expected.setConstraintMetric("metric");
        expected.setConstraintUnit("unit");
        expected.setConstraintValue("value");

        final Constraint source = new Constraint();
        source.setConstraintID(1L);
        source.setConstraintType(CONSTRAINT_TYPE_HARD);
        source.setConstraintCategory(ConstraintCategory.GRAPH_LINK.name());
        final GraphLinkNodeInstance graphLinkNodeInstance = new GraphLinkNodeInstance();
        graphLinkNodeInstance.setGraphLinkNodeInstanceID(2L);
        source.setGraphLinkNodeInstance(graphLinkNodeInstance);
        source.setConstraintMetric("metric");
        source.setConstraintValue("value");
        source.setConstraintUnit("unit");
        final ConstraintTO converted = converter.convert(source);

        assertThat(converted).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    public void shouldFailUponUnknownConstraintCategory() {
        final Constraint source = new Constraint();
        source.setConstraintID(1L);
        source.setConstraintType(CONSTRAINT_TYPE_HARD);
        source.setConstraintCategory("UNKNOWN_CATEGORY_CONSTRAINT");

        assertThatThrownBy(() -> converter.convert(source)).isInstanceOf(IllegalArgumentException.class);
    }
}
