package eu.orchestrator.repository.dao;

import eu.orchestrator.repository.domain.Notification;
import eu.orchestrator.repository.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;
import java.util.Date;

@Repository
@Transactional
public interface NotificationDAO extends JpaRepository<Notification, Long> {

  Page<Notification> findAllByNotificationType(String notificationType, Pageable pageable);

  Page<Notification> findAllByNotificationTypeAndUserAndWhenIsBetweenOrderByWhenDesc(
      String notificationType, User user, Date from, Date to, Pageable pageable);

  Page<Notification> findAllByNotificationTypeAndDismissAndUserAndWhenIsBetweenOrderByWhenDesc(
      String notificationType, boolean dismiss, User user, Date from, Date to, Pageable pageable);

  Page<Notification> findAllByNotificationTypeAndDismissAndUserAndWhenLessThanOrderByWhenDesc(
      String notificationType, boolean dismiss, User user, Date when, Pageable pageable);

  Page<Notification> findAllByStatus(boolean status, Pageable pageable);

}
