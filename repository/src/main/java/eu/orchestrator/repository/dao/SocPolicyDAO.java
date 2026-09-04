package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.*;
import eu.orchestrator.repository.domain.SocPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SocPolicyDAO extends JpaRepository<SocPolicy, Long>,
    QuerydslPredicateExecutor<SocPolicy>, QuerydslBinderCustomizer<QSocPolicy> {

  @Override
  default void customize(QuerydslBindings bindings, QSocPolicy SocPolicy) {

    bindings.bind(String.class).first(
        (StringPath path, String value) -> path.containsIgnoreCase(value)
    );

    bindings.excluding(SocPolicy.dateCreated);
    bindings.excluding(SocPolicy.lastModified);

  }

  Optional<SocPolicy> findByName(String name);

  Optional<SocPolicy> findByHexID(String hexID);

  Page<SocPolicy> findAllByApplicationInstance(ApplicationInstance applicationInstance, Pageable pageable);

  Optional<SocPolicy> findByApplicationInstanceAndName(ApplicationInstance applicationInstance,String name);

  List<SocPolicy> findAllByApplicationInstance(ApplicationInstance applicationInstance);
}
