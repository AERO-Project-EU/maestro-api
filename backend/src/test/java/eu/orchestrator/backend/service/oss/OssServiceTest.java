package eu.orchestrator.backend.service.oss;

import eu.orchestrator.backend.service.applicationinstance.ApplicationInstanceService;
import eu.orchestrator.backend.service.oss.client.OssClient;
import eu.orchestrator.backend.service.oss.intent.SliceIntentConverter;
import eu.orchestrator.backend.service.oss.slice.SliceIngestionService;
import eu.orchestrator.backend.service.oss.slice.SliceResponseStatus;
import eu.orchestrator.backend.service.support.helper.NotificationService;
import eu.orchestrator.common.exception.BadRequestBusinessException;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.ApplicationInstance.ApplicationInstanceStatus;
import eu.orchestrator.transfer.entities.oss.Slice;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import static eu.orchestrator.repository.domain.ApplicationInstance.ApplicationInstanceStatus.WAITING_CONFIRMATION;
import static eu.orchestrator.repository.domain.ApplicationInstance.ApplicationInstanceStatus.WAITING_OSS;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(JUnitParamsRunner.class)
public class OssServiceTest {

    private OssService ossService;
    @Mock
    private ApplicationInstanceService applicationInstanceService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private OssClient ossClient;
    @Mock
    private SliceIngestionService sliceIngestionService;
    @Mock
    private SliceIntentConverter sliceIntentConverter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ossService = new OssService(applicationInstanceService, notificationService, ossClient, sliceIngestionService, sliceIntentConverter);
    }

    @Test
    public void uponSliceResponseErrorShouldMarkAsApplicationInstanceErrorAndInform() {
        final long applicationInstanceID = 1L;
        final ApplicationInstance dummyApplicationInstance = new ApplicationInstance();
        dummyApplicationInstance.setApplicationInstanceID(applicationInstanceID);
        final Slice dummySlice = new Slice();
        dummySlice.setSliceID("sliceId");
        dummySlice.setSliceIntentID("intentId");
        ossService.ingestSlice(dummyApplicationInstance, dummySlice, SliceResponseStatus.ERROR);

        assertThat(dummyApplicationInstance.getStatus()).isEqualTo(ApplicationInstanceStatus.ERROR_OCCURRED.name());
        verify(notificationService, times(1)).notifyForApplicationInstance(eq(applicationInstanceID), anyString(), any());
    }

    public static List<ApplicationInstanceStatus> getAllNonWaitingForOssStatuses() {
        final Predicate<ApplicationInstanceStatus> notWaitingForOss = status -> !status.equals(WAITING_OSS);
        return Arrays.stream(ApplicationInstanceStatus.values()).filter(notWaitingForOss).collect(toList());
    }

    @Test
    @Parameters(method = "getAllNonWaitingForOssStatuses")
    public void shouldThrowWhenApplicationStatusIsNotWaitingForOss(ApplicationInstanceStatus invalidStatus) {
        final ApplicationInstance dummyApplicationInstance = new ApplicationInstance();
        dummyApplicationInstance.setApplicationInstanceID(1L);
        dummyApplicationInstance.setStatus(invalidStatus.name());
        final Slice dummySlice = new Slice();

        assertThatExceptionOfType(BadRequestBusinessException.class).isThrownBy(
                () -> ossService.ingestSlice(dummyApplicationInstance, dummySlice, SliceResponseStatus.SUCCESS));
    }

    @Test
    public void shouldDelegateToSliceIngestionService() {

        final long applicationInstanceID = 1L;
        final ApplicationInstance dummyApplicationInstance = new ApplicationInstance();
        dummyApplicationInstance.setApplicationInstanceID(applicationInstanceID);
        dummyApplicationInstance.setStatus(WAITING_OSS.name());
        final Slice dummySlice = new Slice();

        final eu.orchestrator.repository.domain.Slice storedSlice = new eu.orchestrator.repository.domain.Slice();
        when(sliceIngestionService.ingest(dummyApplicationInstance, dummySlice)).thenReturn(storedSlice);

        ossService.ingestSlice(dummyApplicationInstance, dummySlice, SliceResponseStatus.SUCCESS);
        assertThat(dummyApplicationInstance.getStatus()).isEqualTo(WAITING_CONFIRMATION.name());
        assertThat(dummyApplicationInstance.getSlice()).isNotNull();

        verify(notificationService, times(1)).notifyForApplicationInstance(eq(applicationInstanceID), anyString(), any());
    }

}
