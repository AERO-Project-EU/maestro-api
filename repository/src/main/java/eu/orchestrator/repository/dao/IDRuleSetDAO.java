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
import java.util.Optional;

@Repository
public interface IDRuleSetDAO extends JpaRepository<IDRuleSet, Long>,
    QuerydslPredicateExecutor<IDRuleSet>, QuerydslBinderCustomizer<QIDRuleSet> {

  @Override
  default void customize(QuerydslBindings bindings, QIDRuleSet ruleSet) {

    bindings.bind(String.class).first(
        (StringPath path, String value) -> path.containsIgnoreCase(value)
    );

    bindings.excluding(ruleSet.iDRules);

  }

  Page<IDRuleSet> findAllByUser(@Param("user") User user, Pageable pageable);

  Page<IDRuleSet> findAllByUserOrPredefinedIDRuleSet(@Param("user") User user,
      @Param("predefinedIDRuleSet") boolean predefinedIDRuleSet, Pageable pageable);

  @Query("select p from IDRuleSet p where p.user.username = :username")
  Page<IDRuleSet> customSearchByUser(@Param("username") String username, Pageable pageable);

  Optional<IDRuleSet> findByName(@Param("name") String name);

  Optional<IDRuleSet> findByNameAndPublicIDRuleSet(String name, Boolean publicIDRuleSet);

  Optional<IDRuleSet> findByNameAndUser(String name, User user);

  Optional<IDRuleSet> findByNameAndOrganization(String name, Organization organization);

  @Query("select count(p) from IDRuleSet p")
  Long calculatePlugins();

  @PreAuthorize("#username == authentication.principal.username")
  @Query("select p.id from IDRuleSet p where p.name=:name and p.user.username=:username")
  Optional<IDRuleSet> searchByNameAndUsername(@Param("name") String name,
      @Param("username") String username);

}
