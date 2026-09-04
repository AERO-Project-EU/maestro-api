package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.repository.domain.InterfaceInstance;
import eu.orchestrator.repository.domain.QInterfaceInstance;
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
public interface InterfaceInstanceDAO extends JpaRepository<InterfaceInstance, Long>,
    QuerydslPredicateExecutor<InterfaceInstance>, QuerydslBinderCustomizer<QInterfaceInstance> {

  @Override
  default void customize(QuerydslBindings bindings, QInterfaceInstance interfaceInstance) {

    bindings.bind(String.class).first(
        (StringPath path, String value) -> path.containsIgnoreCase(value)
    );

    bindings.excluding(interfaceInstance.componentNodeInstance);
    bindings.excluding(interfaceInstance.interfaceObj);
    bindings.excluding(interfaceInstance.dateCreated);
    bindings.excluding(interfaceInstance.lastModified);

  }

  Optional<InterfaceInstance> findByNameAndPort(String name, String port);

  Optional<InterfaceInstance> findByNameAndComponentNodeInstance(String name,
      ComponentNodeInstance componentNodeInstance);

  List<InterfaceInstance> findFirst10ByOrderByNameAsc();

  List<InterfaceInstance> findFirst10ByNameIsLikeOrderByNameAsc(String name);

  Page<InterfaceInstance> findAllByComponentNodeInstanceOrderByDateCreatedDesc(
      ComponentNodeInstance componentNodeInstance, Pageable pageable);

  List<InterfaceInstance> findAllByComponentNodeInstanceOrderByDateCreatedDesc(
      ComponentNodeInstance componentNodeInstance);

  Optional<InterfaceInstance> findByInterfaceInstanceID(Long interfaceInstanceID);

}
