package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.Component;
import eu.orchestrator.repository.domain.Interface;
import eu.orchestrator.repository.domain.QInterface;
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
public interface InterfaceDAO extends JpaRepository<Interface, Long>,
    QuerydslPredicateExecutor<Interface>, QuerydslBinderCustomizer<QInterface> {

  @Override
  default void customize(QuerydslBindings bindings, QInterface qInterface) {

    bindings.bind(String.class).first(
        (StringPath path, String value) -> path.containsIgnoreCase(value)
    );

    bindings.excluding(qInterface.dateCreated);
    bindings.excluding(qInterface.lastModified);

  }

  Optional<Interface> findByName(String name);

  List<Interface> findByComponent(Component component);

  Optional<Interface> findByPortAndInterfaceTypeAndTransmissionProtocol(String port,
      String interfaceType, String transmissionProtocol);

  Page<Interface> findAllByInterfaceTypeOrderByDateCreatedDesc(String interfaceType,
      Pageable pageable);

  Page<Interface> findAllByTransmissionProtocolOrderByDateCreatedDesc(String transmissionProtocol,
      Pageable pageable);

  List<Interface> findFirst10ByOrderByNameAsc();

  List<Interface> findFirst10ByNameIsLikeOrderByNameAsc(String name);

}
