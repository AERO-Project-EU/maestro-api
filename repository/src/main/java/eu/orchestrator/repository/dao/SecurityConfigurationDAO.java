package eu.orchestrator.repository.dao;

import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.QSecurityConfiguration;
import eu.orchestrator.repository.domain.SecurityConfiguration;

import com.querydsl.core.types.dsl.StringPath;
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
public interface SecurityConfigurationDAO extends JpaRepository<SecurityConfiguration, Long>,
    QuerydslPredicateExecutor<SecurityConfiguration>, QuerydslBinderCustomizer<QSecurityConfiguration> {

  @Override
  default void customize(QuerydslBindings bindings, QSecurityConfiguration SecurityConfiguration) {

    bindings.bind(String.class).first(
        (StringPath path, String value) -> path.containsIgnoreCase(value)
    );

    bindings.excluding(SecurityConfiguration.dateCreated);
    bindings.excluding(SecurityConfiguration.lastModified);

  }

  Optional<SecurityConfiguration> findByName(String name);

  Optional<SecurityConfiguration> findByHexID(String hexID);

  Page<SecurityConfiguration> findAllByApplicationInstance(ApplicationInstance applicationInstance, Pageable pageable);

  Optional<SecurityConfiguration> findByApplicationInstanceAndName(ApplicationInstance applicationInstance, String name);

  List<SecurityConfiguration> findAllByApplicationInstance(ApplicationInstance applicationInstance);
}
