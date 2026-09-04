package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.Component;
import eu.orchestrator.repository.domain.QRequirement;
import eu.orchestrator.repository.domain.Requirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;
import java.util.Optional;

@Repository
@Transactional
public interface RequirementDAO extends JpaRepository<Requirement, Long>,
    QuerydslPredicateExecutor<Requirement>, QuerydslBinderCustomizer<QRequirement> {

  @Override
  default void customize(QuerydslBindings bindings, QRequirement requirement) {

    bindings.bind(String.class).first(
        (StringPath path, String value) -> path.containsIgnoreCase(value)
    );

    bindings.excluding(requirement.component);

  }

  Optional<Requirement> findByComponent(Component component);

}
