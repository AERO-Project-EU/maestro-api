package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.Application;
import eu.orchestrator.repository.domain.Component;
import eu.orchestrator.repository.domain.ComponentNode;
import eu.orchestrator.repository.domain.QComponentNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ComponentNodeDAO extends JpaRepository<ComponentNode, Long>,
    QuerydslPredicateExecutor<ComponentNode>, QuerydslBinderCustomizer<QComponentNode> {

  @Override
  default void customize(QuerydslBindings bindings, QComponentNode componentNode) {

    bindings.bind(String.class).first(
        (StringPath path, String value) -> path.containsIgnoreCase(value)
    );

    bindings.excluding(componentNode.component);
    bindings.excluding(componentNode.dateCreated);
    bindings.excluding(componentNode.lastModified);

  }

  Optional<ComponentNode> findByName(String name);

  Optional<ComponentNode> findByHexID(String hexID);

  Optional<ComponentNode> findByNameAndApplication(String name, Application application);

  @Query("select cn from ComponentNode cn where lower(cn.name) = :name and cn.application.id = :applicationID")
  Optional<ComponentNode> searchSpecificComponentNode(@Param("name") String name,
      @Param("applicationID") Long applicationID);

  List<ComponentNode> findAllByComponent(Component component);

  List<ComponentNode> findAllByApplication(Application application);


}
