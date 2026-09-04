package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.Organization;
import eu.orchestrator.repository.domain.Provider;
import eu.orchestrator.repository.domain.QProvider;
import eu.orchestrator.repository.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProviderDAO extends JpaRepository<Provider, Long>,
    QuerydslPredicateExecutor<Provider>, QuerydslBinderCustomizer<QProvider> {

  @Override
  default void customize(QuerydslBindings bindings, QProvider provider) {

    bindings.bind(String.class).first(
        (StringPath path, String value) -> path.containsIgnoreCase(value)
    );

  }

  Page<Provider> findAllByUser(User user, Pageable pageable);

  List<Provider> findAllByUser(User user);

  List<Provider> findAllByOrganization(Organization organization);

  List<Provider> findAllByEnabled(Boolean enabled);

  List<Provider> findAllByEnabledAndUser(Boolean enabled, User user);

  Optional<Provider> findByName(String name);

  Optional<Provider> findByNameAndOrganization(String name, Organization organization);

  Optional<Provider> findByNameAndProjectAndUsername(String name, String project, String username);

  Optional<Provider> findByNameAndUser(String name, User user);


  Optional<Provider> findByUserAndDefaultProvider(User user, Boolean defaultProvider);

  Optional<Provider> findByOrganizationAndDefaultProvider(Organization Organization,
      boolean defaultProvider);

  @Query("select count(p) from Provider p")
  Long calculateProviders();

  @PreAuthorize("#username == authentication.principal.username")
  @Query("select p.id from Provider p where p.name=:name and p.user.username=:username")
  Optional<Provider> searchByNameAndUsername(@Param("name") String name,
      @Param("username") String username);

}
