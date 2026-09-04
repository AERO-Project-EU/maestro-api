package eu.orchestrator.backend.service.oss.intent.converters;

import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.transfer.entities.oss.ComponentNodeInstanceTO;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ComponentNodeInstanceConverterTest {

    private ComponentNodeInstanceConverter converter;

    @Before
    public void setUp() {
        converter = new ComponentNodeInstanceConverter();
    }

    @Test
    public void shouldConvertComponentNodeInstance() {
        final ComponentNodeInstanceTO expected = new ComponentNodeInstanceTO();
        expected.setComponentNodeInstanceID("1");
        expected.setComponentNodeInstanceName("component");

        final ComponentNodeInstance instance = new ComponentNodeInstance();
        instance.setComponentNodeInstanceID(1L);
        instance.setName("component");
        final ComponentNodeInstanceTO converted = converter.convert(instance);

        assertThat(converted).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    public void shouldThrowUponNullInput() {
        assertThatThrownBy(() -> converter.convert(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldThrowUponNullId() {
        final ComponentNodeInstance instance = new ComponentNodeInstance();
        instance.setComponentNodeInstanceID(null);
        instance.setName("component");

        assertThatThrownBy(() -> converter.convert(instance)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldThrowUponNullName() {
        final ComponentNodeInstance instance = new ComponentNodeInstance();
        instance.setComponentNodeInstanceID(1L);
        instance.setName(null);

        assertThatThrownBy(() -> converter.convert(instance)).isInstanceOf(IllegalArgumentException.class);
    }
}
