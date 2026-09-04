package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.*;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface IDRuleDAO extends JpaRepository<IDRule, Long>, QuerydslPredicateExecutor<IDRule>,
    QuerydslBinderCustomizer<QIDRule> {

  @Override
  default void customize(QuerydslBindings bindings, QIDRule idRule) {

    bindings.bind(String.class).first(
        (StringPath path, String value) -> path.containsIgnoreCase(value)
    );

    bindings.excluding(idRule.dateCreated);
    bindings.excluding(idRule.lastModified);
    bindings.excluding(idRule.idRuleSet);

  }

  Optional<IDRule> findByNameAndIdRuleSet(String name, IDRuleSet idRuleSet);

  Page<IDRule> findAllByIdRuleSetOrderByDateCreated(IDRuleSet idRuleSet, Pageable pageable);

  List<IDRule> findAllByIdRuleSetOrderByDateCreatedDesc(IDRuleSet idRuleSet);

  @Query("select count(r) from IDRule r where r.idRuleSet.id = :ruleSetID")
  Long calculateIDRulesByIDRuleSet(@Param("ruleSetID") Long ruleSetID);

}
