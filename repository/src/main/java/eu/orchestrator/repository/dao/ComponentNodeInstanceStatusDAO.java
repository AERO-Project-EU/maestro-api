package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.repository.domain.ComponentNodeInstanceStatus;
import eu.orchestrator.repository.domain.Organization;
import eu.orchestrator.repository.domain.QComponentNodeInstanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ComponentNodeInstanceStatusDAO extends
    JpaRepository<ComponentNodeInstanceStatus, Long>,
    QuerydslPredicateExecutor<ComponentNodeInstanceStatus>,
    QuerydslBinderCustomizer<QComponentNodeInstanceStatus> {

  @Override
  default void customize(QuerydslBindings bindings,
      QComponentNodeInstanceStatus componentNodeInstanceStatus) {

    bindings.bind(String.class).first(
        (StringPath path, String value) -> path.containsIgnoreCase(value)
    );

    bindings.excluding(componentNodeInstanceStatus.componentNodeInstance);
    bindings.excluding(componentNodeInstanceStatus.dateCreated);
    bindings.excluding(componentNodeInstanceStatus.lastModified);

  }

  Page<ComponentNodeInstanceStatus> findAllByComponentNodeInstanceOrderByDateCreatedDesc(
      ComponentNodeInstance componentNodeInstance, Pageable pageable);

  List<ComponentNodeInstanceStatus> findAllByComponentNodeInstanceOrderByDateCreatedDesc(
      ComponentNodeInstance componentNodeInstance);

  Page<ComponentNodeInstanceStatus> findAllByComponentNodeInstanceInOrderByDateCreatedDesc(
      List<ComponentNodeInstance> componentNodeInstances, Pageable pageable);

  List<ComponentNodeInstanceStatus> findTop30ByComponentNodeInstanceInOrderByDateCreatedDesc(
      List<ComponentNodeInstance> componentNodeInstances);
  
  List<ComponentNodeInstanceStatus> findAllByApplicationInstance_OrganizationOrderByDateCreatedAsc(Organization organization);
}
