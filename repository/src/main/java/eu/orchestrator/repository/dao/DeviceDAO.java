package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.Component;
import eu.orchestrator.repository.domain.Device;
import eu.orchestrator.repository.domain.QDevice;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DeviceDAO extends JpaRepository<Device, Long>, QuerydslPredicateExecutor<Device>,
    QuerydslBinderCustomizer<QDevice> {

  @Override
  default void customize(QuerydslBindings bindings, QDevice device) {

    bindings.bind(String.class).first(
        (StringPath path, String value) -> path.containsIgnoreCase(value)
    );

    bindings.excluding(device.component);
    bindings.excluding(device.dateCreated);
    bindings.excluding(device.lastModified);

  }

  Optional<Device> findByKeyAndValueAndComponent(String key, String value, Component component);

  Optional<Device> findByKeyAndComponent(String key, Component component);

  Page<Device> findAllByComponentOrderByDateCreatedDesc(Component component, Pageable pageable);

  List<Device> findAllByComponentOrderByDateCreatedAsc(Component component);

}
