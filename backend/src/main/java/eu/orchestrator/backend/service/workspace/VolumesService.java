package eu.orchestrator.backend.service.workspace;

import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.backend.service.applicationinstance.ApplicationInstanceService;
import eu.orchestrator.backend.service.applicationinstance.ComponentNodeInstanceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import jakarta.transaction.Transactional;

@Service
@Transactional(rollbackOn = Exception.class)
public class VolumesService {

    @Autowired
    private ApplicationInstanceService applicationInstanceService;

    @Autowired
    private ComponentNodeInstanceService componentNodeInstanceService;


    public List<String> checkIfVolumesAreSe(Long id) {
        ApplicationInstance existingApplicationInstance = applicationInstanceService.fetchApplicationInstanceById(id);
        if (existingApplicationInstance == null) {
            throw new GenericBusinessException(GenericMessage.APPLICATION_INSTANCE_NOT_EXIST.getCode(), GenericMessage.APPLICATION_INSTANCE_NOT_EXIST);
        }
        List<ComponentNodeInstance> componentNodeInstanceList
                = componentNodeInstanceService.fetchAllComponentNodeInstancesByApplicationInstance(existingApplicationInstance);
        List<String> componentNodeWithOutVolumeMapping = new ArrayList<>();
        for (ComponentNodeInstance componentNodeInstance : componentNodeInstanceList) {
            boolean isVolumeNoSet = componentNodeInstance.getVolumeInstances().stream().anyMatch(volumeInstance -> volumeInstance.getHostPath().isEmpty());
            if (isVolumeNoSet) {
                componentNodeWithOutVolumeMapping.add(componentNodeInstance.getComponentNode().getName());
            }
        }
        return componentNodeWithOutVolumeMapping;
    }
}
