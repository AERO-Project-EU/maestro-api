package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.Component;
import eu.orchestrator.repository.domain.Interface;
import eu.orchestrator.repository.domain.Organization;
import eu.orchestrator.repository.domain.QComponent;
import eu.orchestrator.repository.domain.User;
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
public interface ComponentDAO extends JpaRepository<Component, Long>,
    QuerydslPredicateExecutor<Component>, QuerydslBinderCustomizer<QComponent> {

  @Override
  default void customize(QuerydslBindings bindings, QComponent component) {

    bindings.bind(String.class).first(
        (StringPath path, String value) -> path.containsIgnoreCase(value)
    );

    bindings.excluding(component.requirement);
    bindings.excluding(component.environmentalVariables);
//        bindings.excluding(component.tags);
    bindings.excluding(component.labels);
    bindings.excluding(component.dateCreated);
    bindings.excluding(component.lastModified);
    bindings.excluding(component.requiredInterfaces);
//        bindings.excluding(component.exposedInterfaces);
    bindings.excluding(component.iconBase64);
    bindings.excluding(component.iconContent);
    bindings.excluding(component.iconPath);
    bindings.excluding(component.iconFilename);
    bindings.excluding(component.iconContentType);

  }

  Page<Component> findAllByUser(@Param("user") User user, Pageable pageable);

  List<Component> findAllByUser(@Param("user") User user);

  List<Component> findAllByOrganizationOrPublicComponent(Organization organization,
      boolean publicComponent);

  Optional<Component> findByHexID(String hexID);

  Page<Component> findAllByUserOrPublicComponentOrderByNameAsc(User user, boolean publicComponent,
      Pageable pageable);

  List<Component> findAllByUserOrPublicComponentOrderByNameAsc(User user, boolean publicComponent);

  @Query("select c from Component c where c.user.username = :username")
  Page<Component> customSearchByUser(@Param("username") String username, Pageable pageable);

  Optional<Component> findByName(@Param("name") String name);

  Optional<Component> findByNameAndUser(String name, User user);

  Optional<Component> findByNameAndOrganization(String name, Organization organization);

  Optional<Component> findByNameAndPublicComponent(String name, Boolean publicComponent);

  @Query("select c from Component c where c.name=:name and c.user.username=:username")
  Optional<Component> searchByNameAndUsername(@Param("name") String name,
      @Param("username") String username);

  List<Component> findAllByNameContainingIgnoreCaseAndExposedInterfacesContains(
      @Param("name") String name, @Param("interfaceObj") Interface interfaceObj);

  @Query("select count(c) from Component c where c.organization.id = :organizationID")
  Long calculateComponents(@Param("organizationID") Long organizationID);

  @Query("select count(c) from Component c where c.organization.id = :organizationID or c.publicComponent = :publicComponent")
  Long calculateComponentsAndPublicComponent(@Param("organizationID") Long organizationID,
      @Param("publicComponent") Boolean publicComponent);

}
