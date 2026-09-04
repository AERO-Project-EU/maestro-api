package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.Component;
import eu.orchestrator.repository.domain.HealthCheck;
import eu.orchestrator.repository.domain.QHealthCheck;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface HealthCheckDAO extends JpaRepository<HealthCheck, Long>,
    QuerydslPredicateExecutor<HealthCheck>, QuerydslBinderCustomizer<QHealthCheck> {

  @Override
  default void customize(QuerydslBindings bindings, QHealthCheck healthCheck) {

    bindings.bind(String.class).first(
        (StringPath path, String value) -> path.containsIgnoreCase(value)
    );

    bindings.excluding(healthCheck.component);
    bindings.excluding(healthCheck.dateCreated);
    bindings.excluding(healthCheck.lastModified);

  }

  Optional<HealthCheck> findByComponent(Component component);

  Optional<HealthCheck> findByNameAndComponent(String name, Component component);

  Page<HealthCheck> findAllByComponentOrderByDateCreatedDesc(Component component,
      Pageable pageable);

}
