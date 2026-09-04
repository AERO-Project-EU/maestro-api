package eu.orchestrator.backend.service.oss.slice;

import eu.orchestrator.common.util.NullCheckUtil;
import eu.orchestrator.transfer.entities.oss.ComponentPlacement;
import eu.orchestrator.transfer.entities.oss.Slice;
import eu.orchestrator.transfer.entities.oss.VIM;

import jakarta.validation.ValidationException;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.join;

public class SliceValidator {

    public void validate(Slice slice) {
        final List<VIM> vimDescriptors = slice.getVimDescriptors();
        if (vimDescriptors.isEmpty()) {
            throw new ValidationException("vimDescriptors cannot be empty");
        }

        final List<ComponentPlacement> componentPlacements = slice.getComponentPlacements();
        if (componentPlacements.isEmpty()) {
            throw new ValidationException("componentPlacements cannot be empty");
        }

        final Predicate<ComponentPlacement> hasEmptyVimId = componentPlacement -> NullCheckUtil.isEmpty(componentPlacement.getVimID());
        final Predicate<ComponentPlacement> hasEmptyComponentNodeInstanceId = componentPlacement -> NullCheckUtil.isEmpty(
                componentPlacement.getComponentNodeInstanceID());
        final Predicate<ComponentPlacement> isNotValidComponentPlacement = hasEmptyVimId.or(hasEmptyComponentNodeInstanceId);
        final boolean blankVimIdOrComponentNodeInstanceId = componentPlacements.stream().anyMatch(isNotValidComponentPlacement);
        if (blankVimIdOrComponentNodeInstanceId) {
            throw new ValidationException("componentPlacement (vimId and componentNodeInstanceId) cannot be empty");
        }

        final Set<String> vimIdsInVimDescriptors = vimDescriptors.stream().map(VIM::getVimID).collect(toSet());
        final Set<String> vimIdsInComponentPlacements = componentPlacements.stream().map(ComponentPlacement::getVimID).collect(toSet());
        if (!vimIdsInVimDescriptors.equals(vimIdsInComponentPlacements)) {
            throw new ValidationException(
                    String.format("VIM identifiers in descriptors '%s' don't match the ones in component node placements '%s'",
                            join(vimIdsInVimDescriptors, ','), join(vimIdsInComponentPlacements, ',')));
        }
    }
}
