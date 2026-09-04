package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.repository.domain.DeviceInstance;
import eu.orchestrator.repository.domain.QDeviceInstance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DeviceInstanceDAO extends JpaRepository<DeviceInstance, Long>,
    QuerydslPredicateExecutor<DeviceInstance>, QuerydslBinderCustomizer<QDeviceInstance> {

  @Override
  default void customize(QuerydslBindings bindings, QDeviceInstance deviceInstance) {

    bindings.bind(String.class).first(
        (StringPath path, String value) -> path.containsIgnoreCase(value)
    );

    bindings.excluding(deviceInstance.device);
    bindings.excluding(deviceInstance.componentNodeInstance);
    bindings.excluding(deviceInstance.dateCreated);
    bindings.excluding(deviceInstance.lastModified);

  }

  Optional<DeviceInstance> findByKeyAndValueAndComponentNodeInstance(String key, String value,
      ComponentNodeInstance componentNodeInstance);

  Page<DeviceInstance> findAllByComponentNodeInstanceOrderByDateCreatedDesc(
      ComponentNodeInstance componentNodeInstance, Pageable pageable);

}
