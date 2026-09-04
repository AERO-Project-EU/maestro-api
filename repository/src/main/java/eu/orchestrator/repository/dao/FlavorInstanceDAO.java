package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.repository.domain.FlavorInstance;
import eu.orchestrator.repository.domain.QFlavorInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface FlavorInstanceDAO extends JpaRepository<FlavorInstance, Long>,
    QuerydslPredicateExecutor<FlavorInstance>, QuerydslBinderCustomizer<QFlavorInstance> {

  @Override
  default void customize(QuerydslBindings bindings, QFlavorInstance flavorInstance) {

    bindings.bind(String.class).first(
        (StringPath path, String value) -> path.containsIgnoreCase(value)
    );

    bindings.excluding(flavorInstance.componentNodeInstance);
    bindings.excluding(flavorInstance.dateCreated);
    bindings.excluding(flavorInstance.lastModified);

  }

  Optional<FlavorInstance> findByComponentNodeInstance(ComponentNodeInstance componentNodeInstance);

}
