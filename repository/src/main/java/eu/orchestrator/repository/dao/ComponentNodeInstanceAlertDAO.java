package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.repository.domain.ComponentNodeInstanceAlert;
import eu.orchestrator.repository.domain.Organization;
import eu.orchestrator.repository.domain.QComponentNodeInstanceAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ComponentNodeInstanceAlertDAO extends
    JpaRepository<ComponentNodeInstanceAlert, Long>,
    QuerydslPredicateExecutor<ComponentNodeInstanceAlert>,
    QuerydslBinderCustomizer<QComponentNodeInstanceAlert> {

  @Override
  default void customize(QuerydslBindings bindings,
      QComponentNodeInstanceAlert componentNodeInstanceAlert) {

    bindings.bind(String.class).first(
        (StringPath path, String value) -> path.containsIgnoreCase(value)
    );

    bindings.excluding(componentNodeInstanceAlert.componentNodeInstance);
    bindings.excluding(componentNodeInstanceAlert.dateCreated);
    bindings.excluding(componentNodeInstanceAlert.lastModified);

  }

  Page<ComponentNodeInstanceAlert> findAllByComponentNodeInstanceOrderByDateCreatedDesc(
      ComponentNodeInstance componentNodeInstance, Pageable pageable);

  List<ComponentNodeInstanceAlert> findAllByComponentNodeInstanceOrderByDateCreatedDesc(
      ComponentNodeInstance componentNodeInstance);

  Page<ComponentNodeInstanceAlert> findAllByComponentNodeInstanceInOrderByDateCreatedDesc(
      List<ComponentNodeInstance> componentNodeInstances, Pageable pageable);

  List<ComponentNodeInstanceAlert> findAllByComponentNodeInstanceInOrderByDateCreatedDesc(
      List<ComponentNodeInstance> componentNodeInstances);

  List<ComponentNodeInstanceAlert> findTop30ByComponentNodeInstanceInOrderByDateCreatedDesc(
      List<ComponentNodeInstance> componentNodeInstances);

  List<ComponentNodeInstanceAlert> findAllByApplicationInstance_OrganizationOrderByDateCreatedAsc(Organization organization);

  @Query("select count(a) from ComponentNodeInstanceAlert a where a.applicationInstance.applicationInstanceID = :applicationInstanceID " +
          "and a.componentNodeInstance.componentNodeInstanceID = :componentNodeInstanceID")
  Long calculateComponentNodeInstanceAlerts(@Param("applicationInstanceID") Long applicationInstanceID, @Param("componentNodeInstanceID") Long componentNodeInstanceID);
}
