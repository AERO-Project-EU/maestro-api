package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.QSocPolicyInputKafkaStreamFieldModel;
import eu.orchestrator.repository.domain.QSocPolicyKafkaExpression;
import eu.orchestrator.repository.domain.SocPolicy;
import eu.orchestrator.repository.domain.SocPolicyKafkaExpression;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SocPolicyKafkaExpressionDAO extends JpaRepository<SocPolicyKafkaExpression, Long>,
        QuerydslPredicateExecutor<SocPolicyKafkaExpression>, QuerydslBinderCustomizer<QSocPolicyKafkaExpression> {

    @Override
    default void customize(QuerydslBindings bindings, QSocPolicyKafkaExpression socPolicyKafkaExpression) {

        bindings.bind(String.class).first(
                (StringPath path, String value) -> path.containsIgnoreCase(value)
        );

        bindings.excluding(socPolicyKafkaExpression.dateCreated);
        bindings.excluding(socPolicyKafkaExpression.lastModified);

    }

    List<SocPolicyKafkaExpression> findAllBySocPolicy(SocPolicy socPolicy);
}
