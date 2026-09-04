package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.Organization;
import eu.orchestrator.repository.domain.QOrganization;
import java.util.Optional;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public interface OrganizationDAO extends JpaRepository<Organization, Long>,
    QuerydslPredicateExecutor<Organization>, QuerydslBinderCustomizer<QOrganization> {

  @Override
  default void customize(QuerydslBindings bindings, QOrganization organization) {

    bindings.bind(String.class).first(
        (StringPath path, String value) -> path.containsIgnoreCase(value)
    );

    bindings.excluding(organization.users);

  }

  Optional<Organization> findByName(String name);

}
