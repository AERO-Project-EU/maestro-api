package eu.orchestrator.backend.rest.support.helper;

import eu.orchestrator.common.exception.NotAuthorizedException;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.security.AuthService;
import eu.orchestrator.repository.domain.Notification;
import eu.orchestrator.repository.domain.User;
import eu.orchestrator.transfer.response.BasicResponseCode;
import eu.orchestrator.transfer.response.RestResponse;
import eu.orchestrator.backend.service.support.helper.NotificationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/notification")
public class NotificationController {

    private static final Logger logger = Logger.getLogger(NotificationController.class.getName());

    @Autowired
    private AuthService authService;

    @Autowired
    private NotificationService notificationService;


    @GetMapping(value = "/previous/{id}")
    public RestResponse fetchPreviousNotifications(@PathVariable("id") Long notificationId, HttpServletRequest request) {
        try {
            Page<Notification> notificationPage = notificationService.fetchPreviousNotifications(notificationId, authService.getAuthenticatedUser());
            if (notificationPage != null && !notificationPage.getContent().isEmpty()) {
                return new RestResponse(BasicResponseCode.SUCCESS, null, notificationPage.getContent());
            }
            return new RestResponse(BasicResponseCode.SUCCESS, null, "eonf");
        } catch (NotAuthorizedException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return new RestResponse(BasicResponseCode.EXCEPTION, GenericMessage.NOT_AUTHORIZED.getMessage(request));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return new RestResponse(BasicResponseCode.EXCEPTION, Message.NOTIFICATION_ERROR);
        }
    }

    @PutMapping(value = "/seen")
    public RestResponse seen(@RequestBody List<Long> notificationIDs, HttpServletRequest request) {
        try {
            notificationService.seen(notificationIDs, authService.getAuthenticatedUser());
            return new RestResponse(BasicResponseCode.SUCCESS, null);
        } catch (NotAuthorizedException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return new RestResponse(BasicResponseCode.EXCEPTION, GenericMessage.NOT_AUTHORIZED.getMessage(request));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return new RestResponse(BasicResponseCode.EXCEPTION, Message.NOTIFICATION_ERROR);
        }
    }

    @PutMapping(value = "/batchread")
    public RestResponse batchRead(@RequestBody List<Long> notificationIDs, HttpServletRequest request) {
        try {
            notificationService.batchRead(notificationIDs, authService.getAuthenticatedUser());
            return new RestResponse(BasicResponseCode.SUCCESS, null);
        } catch (NotAuthorizedException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return new RestResponse(BasicResponseCode.EXCEPTION, GenericMessage.NOT_AUTHORIZED.getMessage(request));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return new RestResponse(BasicResponseCode.EXCEPTION, Message.NOTIFICATION_ERROR);
        }
    }


    @PutMapping(value = "/dismiss/{id}")
    public RestResponse dismiss(@PathVariable("id") Long id, HttpServletRequest request) {
        try {
            notificationService.dismiss(id, authService.getAuthenticatedUser());
            return new RestResponse(BasicResponseCode.SUCCESS, null);
        } catch (NotAuthorizedException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return new RestResponse(BasicResponseCode.EXCEPTION, GenericMessage.NOT_AUTHORIZED.getMessage(request));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return new RestResponse(BasicResponseCode.EXCEPTION, Message.NOTIFICATION_ERROR);
        }
    }

    @PutMapping(value = "/read/{flag}/{id})")
    public RestResponse read(@PathVariable("id") Long id, @PathVariable("flag") String flag, HttpServletRequest request) {
        try {
            notificationService.read(id, flag, authService.getAuthenticatedUser());
            return new RestResponse(BasicResponseCode.SUCCESS, null);
        } catch (NotAuthorizedException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return new RestResponse(BasicResponseCode.EXCEPTION, GenericMessage.NOT_AUTHORIZED.getMessage(request));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return new RestResponse(BasicResponseCode.EXCEPTION, Message.NOTIFICATION_ERROR);
        }
    }

    @PutMapping(value = "/settings")
    public RestResponse settings(@RequestBody User user, HttpServletRequest request) {
        logger.info("isNotificationWebEnabled: " + user.getNotificationWebEnabled());
        logger.info("isNotificationEmailEnabled: " + user.getNotificationEmailEnabled());
        try {
            notificationService.settings(user, authService.getAuthenticatedUser());
            return new RestResponse(BasicResponseCode.SUCCESS, Message.NOTIFICATION_SETTINGS_SUCCESS);
        } catch (NotAuthorizedException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return new RestResponse(BasicResponseCode.EXCEPTION, GenericMessage.NOT_AUTHORIZED.getMessage(request));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return new RestResponse(BasicResponseCode.EXCEPTION, Message.NOTIFICATION_ERROR);
        }
    }

    private static final class Message {

        static final String NOTIFICATION_SETTINGS_SUCCESS = "Notification settings has been updated successfully";
        static final String NOTIFICATION_ERROR = "Error occurred! Please try again!";
    }

}
