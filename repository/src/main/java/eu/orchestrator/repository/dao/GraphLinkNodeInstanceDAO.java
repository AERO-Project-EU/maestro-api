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

import java.util.List;
import java.util.Optional;

@Repository
public interface GraphLinkNodeInstanceDAO extends JpaRepository<GraphLinkNodeInstance, Long>,
    QuerydslPredicateExecutor<GraphLinkNodeInstance>,
    QuerydslBinderCustomizer<QGraphLinkNodeInstance> {

  @Override
  default void customize(QuerydslBindings bindings, QGraphLinkNodeInstance graphLinkNodeInstance) {

    bindings.bind(String.class).first(
        (StringPath path, String value) -> path.containsIgnoreCase(value)
    );

    bindings.excluding(graphLinkNodeInstance.dateCreated);
    bindings.excluding(graphLinkNodeInstance.lastModified);
    bindings.excluding(graphLinkNodeInstance.componentNodeInstanceTo);
    bindings.excluding(graphLinkNodeInstance.componentNodeInstanceFrom);
    bindings.excluding(graphLinkNodeInstance.graphLinkNode);

  }

  List<GraphLinkNodeInstance> findAllByApplicationInstance(ApplicationInstance applicationInstance);

  Page<GraphLinkNodeInstance> findAllByApplicationInstanceOrderByDateCreatedDesc(
      ApplicationInstance applicationInstance, Pageable pageable);

  List<GraphLinkNodeInstance> findAllByApplicationInstanceAndComponentNodeInstanceTo(
      ApplicationInstance applicationInstance, ComponentNodeInstance componentNodeInstance);

  List<GraphLinkNodeInstance> findAllByApplicationInstanceAndComponentNodeInstanceFrom(
      ApplicationInstance applicationInstance, ComponentNodeInstance componentNodeInstance);

  Optional<GraphLinkNodeInstance> findByComponentNodeInstanceFromAndComponentNodeInstanceToAndGraphLinkNodeAndApplicationInstance(
      ComponentNodeInstance componentNodeInstanceFrom,
      ComponentNodeInstance componentNodeInstanceTo, GraphLinkNode graphLinkNode,
      ApplicationInstance applicationInstance);

}
