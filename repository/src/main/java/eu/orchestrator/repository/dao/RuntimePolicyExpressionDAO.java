package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.QRuntimePolicyExpression;
import eu.orchestrator.repository.domain.RuntimePolicy;
import eu.orchestrator.repository.domain.RuntimePolicyExpression;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RuntimePolicyExpressionDAO extends JpaRepository<RuntimePolicyExpression, Long>,
    QuerydslPredicateExecutor<RuntimePolicyExpression>,
    QuerydslBinderCustomizer<QRuntimePolicyExpression> {

  @Override
  default void customize(QuerydslBindings bindings,
      QRuntimePolicyExpression runtimePolicyExpression) {

    bindings.bind(String.class).first(
        (StringPath path, String value) -> path.containsIgnoreCase(value)
    );

    bindings.excluding(runtimePolicyExpression.runtimePolicy);
    bindings.excluding(runtimePolicyExpression.lastModified);
    bindings.excluding(runtimePolicyExpression.dateCreated);

  }

  List<RuntimePolicyExpression> findAllByRuntimePolicy(RuntimePolicy runtimePolicy);

}
