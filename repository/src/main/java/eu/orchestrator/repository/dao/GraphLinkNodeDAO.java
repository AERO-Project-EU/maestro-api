package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface GraphLinkNodeDAO extends JpaRepository<GraphLinkNode, Long>,
    QuerydslPredicateExecutor<GraphLinkNode>, QuerydslBinderCustomizer<QGraphLinkNode> {

  @Override
  default void customize(QuerydslBindings bindings, QGraphLinkNode graphLinkNode) {

    bindings.bind(String.class).first(
        (StringPath path, String value) -> path.containsIgnoreCase(value)
    );

    bindings.excluding(graphLinkNode.dateCreated);
    bindings.excluding(graphLinkNode.lastModified);
    bindings.excluding(graphLinkNode.componentNodeTo);
    bindings.excluding(graphLinkNode.componentNodeFrom);
    bindings.excluding(graphLinkNode.graphLink);

  }

  Page<GraphLinkNode> findAllByApplicationOrderByDateCreatedDesc(Application application,
      Pageable pageable);

  Optional<GraphLinkNode> findByComponentNodeFromAndComponentNodeToAndGraphLinkAndApplication(
      ComponentNode componentFrom, ComponentNode componentTo, GraphLink graphLink,
      Application application);

}
