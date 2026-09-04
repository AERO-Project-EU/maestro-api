package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RuntimePolicyActionDAO extends JpaRepository<RuntimePolicyAction, Long>,
    QuerydslPredicateExecutor<RuntimePolicyAction>, QuerydslBinderCustomizer<QRuntimePolicyAction> {

  @Override
  default void customize(QuerydslBindings bindings, QRuntimePolicyAction runtimePolicyAction) {

    bindings.bind(String.class).first(
        (StringPath path, String value) -> path.containsIgnoreCase(value)
    );

    bindings.excluding(runtimePolicyAction.runtimePolicy);
    bindings.excluding(runtimePolicyAction.lastModified);
    bindings.excluding(runtimePolicyAction.dateCreated);

  }

  List<RuntimePolicyAction> findAllByRuntimePolicy(RuntimePolicy runtimePolicy);

}
