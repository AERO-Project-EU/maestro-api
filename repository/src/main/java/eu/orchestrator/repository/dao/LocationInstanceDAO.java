package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.repository.domain.LocationInstance;
import eu.orchestrator.repository.domain.QLocationInstance;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationInstanceDAO extends JpaRepository<LocationInstance, Long>,
    QuerydslPredicateExecutor<LocationInstance>, QuerydslBinderCustomizer<QLocationInstance> {

  @Override
  default void customize(QuerydslBindings bindings, QLocationInstance locationInstance) {

    bindings.bind(String.class).first(
        (StringPath path, String value) -> path.containsIgnoreCase(value)
    );

    bindings.excluding(locationInstance.componentNodeInstance);
    bindings.excluding(locationInstance.dateCreated);
    bindings.excluding(locationInstance.lastModified);

  }

  Page<LocationInstance> findAllByComponentNodeInstance(ComponentNodeInstance componentNodeInstance,
      Pageable pageable);

  List<LocationInstance> findAllByComponentNodeInstance(
      ComponentNodeInstance componentNodeInstance);

}
