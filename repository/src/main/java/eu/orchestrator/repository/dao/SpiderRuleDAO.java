package eu.orchestrator.repository.dao;

import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.QSpiderRule;
import eu.orchestrator.repository.domain.SpiderRule;

import com.querydsl.core.types.dsl.StringPath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpiderRuleDAO extends JpaRepository<SpiderRule, Long>,
        QuerydslPredicateExecutor<SpiderRule>, QuerydslBinderCustomizer<QSpiderRule> {

    @Override
    default void customize(QuerydslBindings bindings, QSpiderRule spiderRule) {
        bindings.bind(String.class).first(
                (StringPath path, String value) -> path.containsIgnoreCase(value)
        );
    }

    List<SpiderRule> findAllByApplicationInstance(ApplicationInstance applicationInstance);
    
}
