package eu.orchestrator.repository.dao;

import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.repository.domain.ComponentNodeInstanceHash;
import eu.orchestrator.repository.domain.QComponentNodeInstanceHash;

import com.querydsl.core.types.dsl.StringPath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ComponentNodeInstanceHashDAO extends JpaRepository<ComponentNodeInstanceHash, Long>,
    QuerydslPredicateExecutor<ComponentNodeInstanceHash>, QuerydslBinderCustomizer<QComponentNodeInstanceHash> {

  @Override
  default void customize(QuerydslBindings bindings, QComponentNodeInstanceHash componentNodeInstanceHash) {

    bindings.bind(String.class).first(
        (StringPath path, String value) -> path.containsIgnoreCase(value)
    );

    bindings.excluding(componentNodeInstanceHash.dateCreated);
    bindings.excluding(componentNodeInstanceHash.lastModified);

  }

  Optional<ComponentNodeInstanceHash> findByComponentNodeInstanceAndType(ComponentNodeInstance componentNodeInstance, String type);

  Optional<ComponentNodeInstanceHash> findByComponentNodeInstance(ComponentNodeInstance componentNodeInstance);

  Long countByComponentNodeInstance(ComponentNodeInstance componentNodeInstance);

  void deleteAllByComponentNodeInstance(ComponentNodeInstance componentNodeInstance);
}
