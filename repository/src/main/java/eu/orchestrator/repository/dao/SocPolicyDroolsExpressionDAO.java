package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.QSocPolicyDroolsExpression;
import eu.orchestrator.repository.domain.SocPolicy;
import eu.orchestrator.repository.domain.SocPolicyDroolsExpression;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SocPolicyDroolsExpressionDAO extends JpaRepository<SocPolicyDroolsExpression, Long>,
        QuerydslPredicateExecutor<SocPolicyDroolsExpression>,
        QuerydslBinderCustomizer<QSocPolicyDroolsExpression> {

    @Override
    default void customize(QuerydslBindings bindings, QSocPolicyDroolsExpression socPolicyDroolsExpression) {

        bindings.bind(String.class).first(
                (StringPath path, String value) -> path.containsIgnoreCase(value)
        );

        bindings.excluding(socPolicyDroolsExpression.dateCreated);
        bindings.excluding(socPolicyDroolsExpression.lastModified);

    }

    List<SocPolicyDroolsExpression> findAllBySocPolicy(SocPolicy socPolicy);
}
