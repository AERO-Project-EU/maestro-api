package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.Label;
import eu.orchestrator.repository.domain.QLabel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface LabelDAO extends JpaRepository<Label, Long>, QuerydslPredicateExecutor<Label>,
    QuerydslBinderCustomizer<QLabel> {

  @Override
  default void customize(QuerydslBindings bindings, QLabel label) {

    bindings.bind(String.class).first(
        (StringPath path, String value) -> path.containsIgnoreCase(value)
    );

    bindings.excluding(label.dateCreated);
    bindings.excluding(label.lastModified);

  }

  Optional<Label> findByName(String name);

}
