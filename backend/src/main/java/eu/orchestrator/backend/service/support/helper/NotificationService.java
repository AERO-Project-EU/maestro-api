package eu.orchestrator.backend.service.support.helper;

import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.service.support.auth.UserBackendService;
import eu.orchestrator.backend.util.Util;
import eu.orchestrator.repository.dao.NotificationDAO;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.Notification;
import eu.orchestrator.repository.domain.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@Service
@Transactional(rollbackOn = Exception.class)
public class NotificationService {

    private final NotificationDAO notificationDAO;
    private final SimpMessagingTemplate wsTemplate;
    private final UserBackendService userBackendService;

    @Inject
    public NotificationService(NotificationDAO notificationDAO, SimpMessagingTemplate wsTemplate,
            UserBackendService userBackendService) {
        this.notificationDAO = notificationDAO;
        this.wsTemplate = wsTemplate;
        this.userBackendService = userBackendService;
    }


    public Notification fetchById(Long notificationID) {
        Optional<Notification> notificationOptional = notificationDAO.findById(notificationID);
        return notificationOptional.orElse(null);
    }

    public Page<Notification> fetchPreviousNotifications(Long notificationID, User authenticatedUser) {
        if (null != authenticatedUser && null != notificationID && notificationID != 0L) {
            Notification existingNotification = fetchById(notificationID);
            existingNotification.setUser(authenticatedUser);
            notificationDAO.save(existingNotification);
            if (null != existingNotification.getUser() && existingNotification.getUser().getId().equals(authenticatedUser.getId())) {
                Date whenDate = existingNotification.getWhen();
                Calendar todayMinus15 = Calendar.getInstance();
                todayMinus15.add(Calendar.DAY_OF_YEAR, -15);
                if (whenDate.compareTo(todayMinus15.getTime()) > 0) {
                    Page<Notification> page = notificationDAO.findAllByNotificationTypeAndDismissAndUserAndWhenIsBetweenOrderByWhenDesc(
                            Notification.NotificationType.PUSH.getFriendlyName(), false, authenticatedUser,
                            todayMinus15.getTime(), whenDate, PageRequest.of(0, 10));
                    if (null != page && !page.getContent().isEmpty()) {
                        return page;
                    }
                }
                throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
            }
            throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
        }
        throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
    }

    public void seen(List<Long> notificationIDs, User authenticatedUser) {
        if (null != authenticatedUser && null != notificationIDs && !notificationIDs.isEmpty()) {
            notificationIDs.forEach(notificationID -> {
                Notification existingNotification = fetchById(notificationID);
                if (null != existingNotification && null != existingNotification.getUser()
                        && existingNotification.getUser().getId().equals(authenticatedUser.getId())) {
                    existingNotification.setSeen(true);
                    notificationDAO.save(existingNotification);
                }
            });
        }
        throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
    }

    public void batchRead(List<Long> notificationIDs, User authenticatedUser) {
        if (null != authenticatedUser && null != notificationIDs && !notificationIDs.isEmpty()) {
            notificationIDs.forEach(notificationID -> {
                Notification existingNotification = fetchById(notificationID);
                if (null != existingNotification && null != existingNotification.getUser()
                        && existingNotification.getUser().getId().equals(authenticatedUser.getId())) {
                    existingNotification.setRead(true);
                    notificationDAO.save(existingNotification);
                }
            });
        }
        throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
    }

    public void dismiss(Long id, User authenticatedUser) {
        if (null != authenticatedUser && null != id && id != 0L) {
            Notification existingNotification = fetchById(id);
            if (null != existingNotification && null != existingNotification.getUser()
                    && existingNotification.getUser().getId().equals(authenticatedUser.getId())) {
                existingNotification.setDismiss(true);
                notificationDAO.save(existingNotification);
            }
            throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
        }
        throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
    }

    public void read(Long id, String flag, User authenticatedUser) {
        if (null != authenticatedUser && null != id && id != 0L && null != flag && !flag.isEmpty()) {
            Notification existingNotification = fetchById(id);
            if (null != existingNotification && null != existingNotification.getUser()
                    && existingNotification.getUser().getId().equals(authenticatedUser.getId())) {
                existingNotification.setRead(Boolean.parseBoolean(flag));
                notificationDAO.save(existingNotification);
            }
            throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
        }
        throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
    }

    public void settings(User user, User authenticatedUser) {
        authenticatedUser.setNotificationWebEnabled(user.getNotificationWebEnabled());
        authenticatedUser.setNotificationEmailEnabled(user.getNotificationEmailEnabled());
        userBackendService.save(authenticatedUser);
    }

    public boolean notifyForApplicationInstance(long applicationInstanceId, String message, User user) {
        final String redirectUrl = "/instance/" + applicationInstanceId;

        return Util.sendPushNotification(String.valueOf(applicationInstanceId), message, "", redirectUrl,
                user, Notification.ComponentType.APPLICATION_INSTANCE.name(), notificationDAO, wsTemplate);
    }
}
