package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.QSocPolicyDroolsExpressionHeader;
import eu.orchestrator.repository.domain.SocPolicy;
import eu.orchestrator.repository.domain.SocPolicyDroolsExpression;
import eu.orchestrator.repository.domain.SocPolicyDroolsExpressionHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SocPolicyDroolsExpressionHeaderDAO extends JpaRepository<SocPolicyDroolsExpressionHeader, Long>,
        QuerydslPredicateExecutor<SocPolicyDroolsExpressionHeader>,
        QuerydslBinderCustomizer<QSocPolicyDroolsExpressionHeader> {

    @Override
    default void customize(QuerydslBindings bindings, QSocPolicyDroolsExpressionHeader socPolicyDroolsExpressionHeader) {

        bindings.bind(String.class).first(
                (StringPath path, String value) -> path.containsIgnoreCase(value)
        );

        bindings.excluding(socPolicyDroolsExpressionHeader.dateCreated);
        bindings.excluding(socPolicyDroolsExpressionHeader.lastModified);

    }

    List<SocPolicyDroolsExpressionHeader> findAllBySocPolicyDroolsExpression(SocPolicyDroolsExpression socPolicyDroolsExpression);

}
