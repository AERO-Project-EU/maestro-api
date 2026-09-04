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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PluginDAO extends JpaRepository<Plugin, Long>, QuerydslPredicateExecutor<Plugin>,
    QuerydslBinderCustomizer<QPlugin> {

  @Override
  default void customize(QuerydslBindings bindings, QPlugin plugin) {

    bindings.bind(String.class).first(
        (StringPath path, String value) -> path.containsIgnoreCase(value)
    );

    bindings.excluding(plugin.metrics);

  }

  Page<Plugin> findAllByUser(@Param("user") User user, Pageable pageable);

  List<Plugin> findAllByDefaultPlugin(boolean defaultPlugin);

  List<Plugin> findAllByUserAndDefaultPlugin(User user, boolean defaultPlugin);

  Page<Plugin> findAllByUserOrPublicPluginOrderByNameAsc(User user, boolean publicPlugin,
      Pageable pageable);

  @Query("select p from Plugin p where p.user.username = :username")
  Page<Plugin> customSearchByUser(@Param("username") String username, Pageable pageable);

  Optional<Plugin> findByName(@Param("name") String name);

  Optional<Plugin> findByNameAndPublicPlugin(String name, Boolean publicPlugin);

  Optional<Plugin> findByNameAndUser(String name, User user);

  Optional<Plugin> findByNameAndOrganization(String name, Organization organization);

  Optional<Plugin> findByNameAndModuleNameAndUser(String name, String moduleName, User user);

  Optional<Plugin> findByNameAndModuleNameAndOrganization(String name, String moduleName,
      Organization organization);

  Optional<Plugin> findByNameAndModuleName(String name, String moduleName);

  Optional<Plugin> findByNameAndModuleNameAndPublicPlugin(String name, String moduleName, Boolean publicPlugin);


  @Query("select count(p) from Plugin p")
  Long calculatePlugins();

  @PreAuthorize("#username == authentication.principal.username")
  @Query("select p.id from Plugin p where p.name=:name and p.moduleName=:moduleName and p.user.username=:username")
  Optional<Plugin> searchByNameAndModuleNameAndUsername(@Param("name") String name,
      @Param("moduleName") String moduleName, @Param("username") String username);

}
