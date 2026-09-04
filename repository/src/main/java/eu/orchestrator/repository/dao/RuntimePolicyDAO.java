package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.*;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RuntimePolicyDAO extends JpaRepository<RuntimePolicy, Long>,
    QuerydslPredicateExecutor<RuntimePolicy>, QuerydslBinderCustomizer<QRuntimePolicy> {

  @Override
  default void customize(QuerydslBindings bindings, QRuntimePolicy runtimePolicy) {

    bindings.bind(String.class).first(
        (StringPath path, String value) -> path.containsIgnoreCase(value)
    );

    bindings.excluding(runtimePolicy.dateCreated);
    bindings.excluding(runtimePolicy.lastModified);

  }

  Optional<RuntimePolicy> findByName(String name);

  Optional<RuntimePolicy> findByHexID(String hexID);

  Optional<RuntimePolicy> findByApplicationInstanceAndName(ApplicationInstance applicationInstance,
      String name);

  Page<RuntimePolicy> findAllByApplicationInstance(ApplicationInstance applicationInstance,
      Pageable pageable);

  Page<RuntimePolicy> findAllByApplicationInstanceAndType(ApplicationInstance applicationInstance,
      String type, Pageable pageable);

  List<RuntimePolicy> findAllByApplicationInstanceAndType(ApplicationInstance applicationInstance,
      String type);

}
