package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.*;
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
import java.util.Optional;

@Repository
public interface ApplicationInstanceDAO extends JpaRepository<ApplicationInstance, Long>,
    QuerydslPredicateExecutor<ApplicationInstance>, QuerydslBinderCustomizer<QApplicationInstance> {

  @Override
  default void customize(QuerydslBindings bindings, QApplicationInstance applicationInstance) {

    bindings.bind(String.class).first(
        (StringPath path, String value) -> path.containsIgnoreCase(value)
    );

    bindings.excluding(applicationInstance.componentNodeInstances);
    bindings.excluding(applicationInstance.constraints);
    bindings.excluding(applicationInstance.lastModified);
    bindings.excluding(applicationInstance.dateCreated);

  }

  Optional<ApplicationInstance> findByHexID(String hexID);

  Page<ApplicationInstance> findAllByUser(User user, Pageable pageable);

  List<ApplicationInstance> findAllByUser(User user);

  List<ApplicationInstance> findAllByOrganization(Organization organization);

  Page<ApplicationInstance> findAllByApplication(Application Application, Pageable pageable);

  List<ApplicationInstance> findAllByApplication(Application Application);

  Page<ApplicationInstance> findAllBySlice(Slice slice, Pageable pageable);

  List<ApplicationInstance> findAllByStatusAndUser(String status, User user);

  List<ApplicationInstance> findAllByStatus(String status);

  Optional<ApplicationInstance> findByName(@Param("name") String name);

  Optional<ApplicationInstance> findByNameAndUser(String name, User user);

  Optional<ApplicationInstance> findByNameAndOrganization(String name, Organization organization);

  @Query("select count(a) from ApplicationInstance a where a.organization.id = :organizationID")
  Long calculateApplicationInstances(@Param("organizationID") Long organizationID);

}
