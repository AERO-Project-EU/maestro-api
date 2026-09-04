package eu.orchestrator.backend.service.oss.slice;

import eu.orchestrator.transfer.entities.oss.ComponentPlacement;
import eu.orchestrator.transfer.entities.oss.Slice;
import eu.orchestrator.transfer.entities.oss.VIM;

import org.junit.Before;
import org.junit.Test;

import jakarta.validation.ValidationException;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;

public class SliceValidatorTest {

    private SliceValidator sliceValidator;

    @Before
    public void setUp() {
        sliceValidator = new SliceValidator();
    }

    @Test
    public void shouldThrowIfVIMDescriptorsAreNull() {
        final Slice sliceWithBlankVim = new Slice();
        assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> sliceValidator.validate(sliceWithBlankVim));
    }

    @Test
    public void shouldThrowIfVIMDescriptorsAreEmpty() {
        final Slice sliceWithBlankVim = new Slice();
        sliceWithBlankVim.setVimDescriptors(emptyList());
        assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> sliceValidator.validate(sliceWithBlankVim));
    }

    @Test
    public void shouldThrowIfComponentPlacementsAreNull() {
        final VIM vim = new VIM();
        vim.setVimID("dummyVimId");
        vim.setProject("project");
        final Slice sliceWithBlankComponentPlacements = new Slice();
        sliceWithBlankComponentPlacements.setVimDescriptors(singletonList(vim));
        assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> sliceValidator.validate(sliceWithBlankComponentPlacements));
    }

    @Test
    public void shouldThrowIfComponentPlacementsAreEmpty() {
        final VIM vim = new VIM();
        vim.setVimID("dummyVimId");
        vim.setProject("project");
        final Slice sliceWithBlankComponentPlacements = new Slice();
        sliceWithBlankComponentPlacements.setVimDescriptors(singletonList(vim));
        sliceWithBlankComponentPlacements.setComponentPlacements(emptyList());
        assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> sliceValidator.validate(sliceWithBlankComponentPlacements));
    }

    @Test
    public void shouldThrowIfVimIdIsMissingFromComponentPlacements() {
        final VIM vim = new VIM();
        vim.setVimID("dummyVimId");
        vim.setProject("project");
        final ComponentPlacement componentPlacementWithMissingVimId = new ComponentPlacement();
        componentPlacementWithMissingVimId.setComponentNodeInstanceID("1");
        componentPlacementWithMissingVimId.setFlavorID("flavorId");
        final Slice slice = new Slice();
        slice.setVimDescriptors(singletonList(vim));
        slice.setComponentPlacements(singletonList(componentPlacementWithMissingVimId));

        assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> sliceValidator.validate(slice));
    }

    @Test
    public void shouldThrowIfComponentIdIsMissingFromComponentPlacements() {
        final VIM vim = new VIM();
        vim.setVimID("dummyVimId");
        vim.setProject("project");
        final ComponentPlacement componentPlacementWithMissingVimId = new ComponentPlacement();
        componentPlacementWithMissingVimId.setVimID("dummyVimId");
        componentPlacementWithMissingVimId.setFlavorID("flavorId");
        final Slice slice = new Slice();
        slice.setVimDescriptors(singletonList(vim));
        slice.setComponentPlacements(singletonList(componentPlacementWithMissingVimId));

        assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> sliceValidator.validate(slice));
    }

    @Test
    public void shouldThrowValidationUponPlacementVimIdNotInVimDescriptors() {
        final VIM vim = new VIM();
        vim.setVimID("dummyVimId");
        vim.setProject("project");
        final ComponentPlacement componentPlacement = new ComponentPlacement();
        componentPlacement.setComponentNodeInstanceID("1");
        componentPlacement.setVimID("nonExistentVimInVimDescriptors");
        final Slice slice = new Slice();
        slice.setVimDescriptors(singletonList(vim));
        slice.setComponentPlacements(singletonList(componentPlacement));

        assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> sliceValidator.validate(slice));
    }

    @Test
    public void shouldNotThrowValidationUponCorrectSlice() {
        final VIM vim = new VIM();
        vim.setVimID("dummyVimId");
        vim.setProject("project");
        final ComponentPlacement componentPlacement = new ComponentPlacement();
        componentPlacement.setComponentNodeInstanceID("1");
        componentPlacement.setVimID("dummyVimId");
        final Slice slice = new Slice();
        slice.setVimDescriptors(singletonList(vim));
        slice.setComponentPlacements(singletonList(componentPlacement));

        assertThatNoException().isThrownBy(() -> sliceValidator.validate(slice));
    }
}
