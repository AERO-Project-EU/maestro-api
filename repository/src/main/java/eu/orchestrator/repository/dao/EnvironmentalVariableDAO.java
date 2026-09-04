package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.Component;
import eu.orchestrator.repository.domain.EnvironmentalVariable;
import eu.orchestrator.repository.domain.QEnvironmentalVariable;
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
public interface EnvironmentalVariableDAO extends JpaRepository<EnvironmentalVariable, Long>,
    QuerydslPredicateExecutor<EnvironmentalVariable>,
    QuerydslBinderCustomizer<QEnvironmentalVariable> {

  @Override
  default void customize(QuerydslBindings bindings, QEnvironmentalVariable environmentalVariable) {

    bindings.bind(String.class).first(
        (StringPath path, String value) -> path.containsIgnoreCase(value)
    );

    bindings.excluding(environmentalVariable.component);
    bindings.excluding(environmentalVariable.dateCreated);
    bindings.excluding(environmentalVariable.lastModified);

  }

  Optional<EnvironmentalVariable> findByKeyAndValueAndComponent(String key, String value,
      Component component);

  Optional<EnvironmentalVariable> findByKeyAndComponent(String key, Component component);

  Page<EnvironmentalVariable> findAllByComponentOrderByDateCreatedDesc(Component component,
      Pageable pageable);

  List<EnvironmentalVariable> findAllByComponentOrderByDateCreatedAsc(Component component);

}
