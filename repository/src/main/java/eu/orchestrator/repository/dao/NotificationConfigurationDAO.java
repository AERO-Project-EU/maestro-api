package eu.orchestrator.repository.dao;

import eu.orchestrator.repository.domain.NotificationConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface NotificationConfigurationDAO extends
    JpaRepository<NotificationConfiguration, Long> {

  Optional<NotificationConfiguration> findByNotificationType(String notificationType);

}
