package eu.orchestrator.repository.dao;

import eu.orchestrator.repository.domain.NotificationTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface NotificationTemplateDAO extends JpaRepository<NotificationTemplate, Long> {

  Page<NotificationTemplate> findByStatus(boolean status, Pageable pageable);

  Optional<NotificationTemplate> findByName(String name);

  Page<NotificationTemplate> findByNotificationType(String notificationType, Pageable pageable);

}
