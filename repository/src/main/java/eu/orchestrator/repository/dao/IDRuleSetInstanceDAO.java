package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.*;
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
public interface IDRuleSetInstanceDAO extends JpaRepository<IDRuleSetInstance, Long>,
    QuerydslPredicateExecutor<IDRuleSetInstance>, QuerydslBinderCustomizer<QIDRuleSetInstance> {

  @Override
  default void customize(QuerydslBindings bindings, QIDRuleSetInstance idRuleSetInstance) {

    bindings.bind(String.class).first(
        (StringPath path, String value) -> path.containsIgnoreCase(value)
    );

    bindings.excluding(idRuleSetInstance.idRuleSet);
    bindings.excluding(idRuleSetInstance.componentNodeInstance);
    bindings.excluding(idRuleSetInstance.dateCreated);
    bindings.excluding(idRuleSetInstance.lastModified);

  }

  Optional<IDRuleSetInstance> findByNameAndComponentNodeInstance(String name,
      ComponentNodeInstance componentNodeInstance);

  Page<IDRuleSetInstance> findAllByComponentNodeInstanceOrderByDateCreatedDesc(
      ComponentNodeInstance componentNodeInstance, Pageable pageable);

  List<IDRuleSetInstance> findAllByComponentNodeInstanceOrderByDateCreatedDesc(
      ComponentNodeInstance componentNodeInstance);

}
