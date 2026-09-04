package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.Component;
import eu.orchestrator.repository.domain.GraphLink;
import eu.orchestrator.repository.domain.Interface;
import eu.orchestrator.repository.domain.QGraphLink;
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
public interface GraphLinkDAO extends JpaRepository<GraphLink, Long>,
    QuerydslPredicateExecutor<GraphLink>, QuerydslBinderCustomizer<QGraphLink> {

  @Override
  default void customize(QuerydslBindings bindings, QGraphLink graphLink) {

    bindings.bind(String.class).first(
        (StringPath path, String value) -> path.containsIgnoreCase(value)
    );

    bindings.excluding(graphLink.dateCreated);
    bindings.excluding(graphLink.lastModified);
    bindings.excluding(graphLink.component);
    bindings.excluding(graphLink.interfaceObj);

  }

  Page<GraphLink> findAllByComponentOrderByDateCreatedDesc(Component component, Pageable pageable);

  List<GraphLink> findAllByComponentOrderByDateCreatedAsc(Component component);

  Long countAllByInterfaceObj(Interface interfaceObj);

  Optional<GraphLink> findByInterfaceObjAndFriendlyNameAndComponent(Interface interfaceObj,
      String friendlyName, Component component);

  List<GraphLink> findByInterfaceObj(Interface interfaceObj);

}
