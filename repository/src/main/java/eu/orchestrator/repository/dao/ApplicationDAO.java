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
public interface ApplicationDAO extends JpaRepository<Application, Long>,
    QuerydslPredicateExecutor<Application>,
    QuerydslBinderCustomizer<QApplication> {

  @Override
  default void customize(QuerydslBindings bindings, QApplication application) {

    bindings.bind(String.class).first(
        (StringPath path, String value) -> path.containsIgnoreCase(value)
    );

    bindings
        .excluding(application.componentNodes, application.graphLinkNodes, application.lastModified,
            application.dateCreated);

  }

  Optional<Application> findByName(@Param("name") String name);

  @Query("select a from Application a where a.name=:name and (a.organization.id=:orgid or a.publicApplication=:publicApplication)")
  Optional<Application> findByNameAndCheckOrganization(@Param("name") String name,
      @Param("orgid") Long organizationID, @Param("publicApplication") boolean publicApplication);

  Optional<Application> findByHexID(String hexID);

  @Query("select a from Application a where a.name=:name and a.user.username=:username")
  Optional<Application> searchByNameAndUsername(@Param("name") String name,
      @Param("username") String username);

  Optional<Application> findByNameAndUser(String name, User user);

  Optional<Application> findByNameAndOrganization(String name, Organization organization);

  Optional<Application> findByNameAndPublicApplication(String name, Boolean publicApplication);

  Page<Application> findAllByUser(User user, Pageable pageable);

  List<Application> findAllByUser(User user);

  List<Application> findAllByOrganization(Organization organization);

  Page<Application> findAllByUserOrPublicApplication(User user, boolean publicApplication,
      Pageable pageable);

  Page<Application> findAllByComponentNodesIsIn(List<ComponentNode> componentNodes,
      Pageable pageable);

  @Query("select count(a) from Application a where a.organization.id = :organizationID or a.publicApplication = :publicApplication")
  Long calculateApplications(@Param("organizationID") Long organizationID, @Param("publicApplication") Boolean publicApplication);

}
