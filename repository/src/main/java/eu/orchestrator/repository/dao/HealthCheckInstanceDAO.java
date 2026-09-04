package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.repository.domain.HealthCheckInstance;
import eu.orchestrator.repository.domain.QHealthCheckInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface HealthCheckInstanceDAO extends JpaRepository<HealthCheckInstance, Long>,
    QuerydslPredicateExecutor<HealthCheckInstance>, QuerydslBinderCustomizer<QHealthCheckInstance> {

  @Override
  default void customize(QuerydslBindings bindings, QHealthCheckInstance healthCheckInstance) {

    bindings.bind(String.class).first(
        (StringPath path, String value) -> path.containsIgnoreCase(value)
    );

    bindings.excluding(healthCheckInstance.healthCheck);
    bindings.excluding(healthCheckInstance.componentNodeInstance);
    bindings.excluding(healthCheckInstance.dateCreated);
    bindings.excluding(healthCheckInstance.lastModified);

  }

  Optional<HealthCheckInstance> findByNameAndHttpURLAndComponentNodeInstance(String name,
      String httpURL, ComponentNodeInstance componentNodeInstance);

  Optional<HealthCheckInstance> findByComponentNodeInstance(
      ComponentNodeInstance componentNodeInstance);

}
