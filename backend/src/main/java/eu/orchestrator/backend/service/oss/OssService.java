package eu.orchestrator.backend.service.oss;

import eu.orchestrator.backend.service.applicationinstance.ApplicationInstanceService;
import eu.orchestrator.backend.service.oss.client.OssClient;
import eu.orchestrator.backend.service.oss.intent.SliceIntentConverter;
import eu.orchestrator.backend.service.oss.slice.SliceIngestionService;
import eu.orchestrator.backend.service.oss.slice.SliceResponseStatus;
import eu.orchestrator.backend.service.support.helper.NotificationService;
import eu.orchestrator.common.exception.BadRequestBusinessException;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.transfer.entities.oss.Slice;
import eu.orchestrator.transfer.entities.oss.SliceIntent;

import org.springframework.stereotype.Service;

import jakarta.inject.Inject;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import static eu.orchestrator.backend.util.Util.toJson;
import static eu.orchestrator.common.enums.GenericMessage.GENERIC_ERROR;
import static eu.orchestrator.repository.domain.ApplicationInstance.ApplicationInstanceStatus.ERROR_OCCURRED;
import static eu.orchestrator.repository.domain.ApplicationInstance.ApplicationInstanceStatus.WAITING_CONFIRMATION;
import static eu.orchestrator.repository.domain.ApplicationInstance.ApplicationInstanceStatus.WAITING_OSS;

@Service
public class OssService {

    private static final Logger LOGGER = Logger.getLogger(OssService.class.getName());

    private final ApplicationInstanceService applicationInstanceService;
    private final NotificationService notificationsService;
    private final OssClient client;
    private final SliceIngestionService sliceIngestionService;
    private final SliceIntentConverter sliceIntentConverter;

    @Inject
    public OssService(ApplicationInstanceService applicationInstanceService,
            NotificationService notificationsService, OssClient client, SliceIngestionService sliceIngestionService,
            SliceIntentConverter sliceIntentConverter) {
        this.applicationInstanceService = applicationInstanceService;
        this.notificationsService = notificationsService;
        this.client = client;
        this.sliceIngestionService = sliceIngestionService;
        this.sliceIntentConverter = sliceIntentConverter;
    }

    public boolean requestSlice(ApplicationInstance instance) {
        LOGGER.log(Level.INFO, "Populating intent for application instance [{0}]", instance.getApplicationInstanceID());

        SliceIntent intent;
        try {
            intent = sliceIntentConverter.convert(instance);
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Failed to convert slice intent from application instance [{0}]: {1}",
                    new Object[]{instance.getApplicationInstanceID(), exception.getMessage()});
            return false;
        }

        boolean submittedSuccessfully;
        try {
            submittedSuccessfully = client.requestSlice(intent);
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Failed to submit intent for application instance [{0}] to OSS: {1}",
                    new Object[]{instance.getApplicationInstanceID(), exception.getMessage()});
            return false;
        }

        return submittedSuccessfully;
    }

    public void ingestSlice(ApplicationInstance applicationInstance, Slice slice, SliceResponseStatus sliceResponseStatus) {
        final long applicationInstanceId = applicationInstance.getApplicationInstanceID();
        LOGGER.log(Level.INFO, "Handling slice for application [id={0}] with status [{1}]: {2}",
                new Object[]{applicationInstanceId, sliceResponseStatus, toJson(slice)});

        if (sliceResponseStatus.equals(SliceResponseStatus.ERROR) || slice == null) {
            LOGGER.log(Level.INFO, "Updating application [id={0}] to {1}", new Object[]{applicationInstanceId, ERROR_OCCURRED});
            applicationInstance.setStatus(ERROR_OCCURRED.name());
            applicationInstanceService.saveApplicationInstance(applicationInstance);

            notificationsService.notifyForApplicationInstance(applicationInstanceId,
                    String.format("Something went wrong with the deployment of %s", applicationInstance.getName()), applicationInstance.getUser());
            return;
        }

        if (!applicationInstance.hasStatus(WAITING_OSS)) {
            throw new BadRequestBusinessException(
                    String.format("Received slice for application [id=%d] whose status is [%s]. Ignoring...", applicationInstanceId,
                            applicationInstance.getStatus()), GENERIC_ERROR);
        }

        final eu.orchestrator.repository.domain.Slice storedSlice = sliceIngestionService.ingest(applicationInstance, slice);

        LOGGER.log(Level.INFO, "Changing application [id={0}] status to {1}...", new Object[]{applicationInstanceId, WAITING_CONFIRMATION});
        applicationInstance.setSlice(storedSlice);
        applicationInstance.setStatus(WAITING_CONFIRMATION.name());
        applicationInstance.setLastModified(new Date());
        applicationInstanceService.saveApplicationInstance(applicationInstance);

        final String deploymentPlanReadinessMessage = applicationInstance.getName() + "'s deployment plan is ready!";
        notificationsService.notifyForApplicationInstance(applicationInstanceId, deploymentPlanReadinessMessage, applicationInstance.getUser());
    }
}
