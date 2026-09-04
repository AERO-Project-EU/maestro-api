package eu.orchestrator.repository.dao;

import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.repository.domain.QSecurityConfigurationResult;
import eu.orchestrator.repository.domain.SecurityConfiguration;
import eu.orchestrator.repository.domain.SecurityConfigurationResult;

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
public interface SecurityConfigurationResultDAO extends JpaRepository<SecurityConfigurationResult, Long>,
    QuerydslPredicateExecutor<SecurityConfigurationResult>, QuerydslBinderCustomizer<QSecurityConfigurationResult> {

  @Override
  default void customize(QuerydslBindings bindings, QSecurityConfigurationResult SecurityConfigurationResult) {

    bindings.bind(String.class).first(
        (StringPath path, String value) -> path.containsIgnoreCase(value)
    );

    bindings.excluding(SecurityConfigurationResult.dateCreated);
    bindings.excluding(SecurityConfigurationResult.lastModified);

  }

  Optional<SecurityConfigurationResult> findByHexID(String hexID);

  Page<SecurityConfigurationResult> findAllBySecurityConfiguration(SecurityConfiguration securityConfiguration, Pageable pageable);

  List<SecurityConfigurationResult> findAllBySecurityConfiguration(SecurityConfiguration securityConfiguration);

  void deleteAllByComponentNodeInstance(ComponentNodeInstance componentNodeInstance);

}
