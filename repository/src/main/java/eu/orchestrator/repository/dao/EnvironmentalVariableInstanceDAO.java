package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.repository.domain.EnvironmentalVariableInstance;
import eu.orchestrator.repository.domain.QEnvironmentalVariableInstance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EnvironmentalVariableInstanceDAO extends
    JpaRepository<EnvironmentalVariableInstance, Long>,
    QuerydslPredicateExecutor<EnvironmentalVariableInstance>,
    QuerydslBinderCustomizer<QEnvironmentalVariableInstance> {

  @Override
  default void customize(QuerydslBindings bindings,
      QEnvironmentalVariableInstance environmentalVariableInstance) {

    bindings.bind(String.class).first(
        (StringPath path, String value) -> path.containsIgnoreCase(value)
    );

    bindings.excluding(environmentalVariableInstance.environmentalVariable);
    bindings.excluding(environmentalVariableInstance.componentNodeInstance);
    bindings.excluding(environmentalVariableInstance.dateCreated);
    bindings.excluding(environmentalVariableInstance.lastModified);

  }

  Optional<EnvironmentalVariableInstance> findByKeyAndValueAndComponentNodeInstance(String key,
      String value, ComponentNodeInstance componentNodeInstance);

  Page<EnvironmentalVariableInstance> findAllByComponentNodeInstanceOrderByDateCreatedDesc(
      ComponentNodeInstance componentNodeInstance, Pageable pageable);

  Optional<EnvironmentalVariableInstance> findByEnvironmentalVariableInstanceID(
      Long environmentalVariableInstanceID);

}
