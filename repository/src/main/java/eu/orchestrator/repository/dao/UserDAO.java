package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.Organization;
import eu.orchestrator.repository.domain.QUser;
import eu.orchestrator.repository.domain.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.util.Optional;

@Repository
@Transactional
public interface UserDAO extends JpaRepository<User, Long>, QuerydslPredicateExecutor<User>,
    QuerydslBinderCustomizer<QUser> {

  @Override
  default void customize(QuerydslBindings bindings, QUser user) {

    bindings.bind(String.class).first(
        (StringPath path, String value) -> path.containsIgnoreCase(value)
    );

    bindings.excluding(user.password);

  }

  Optional<User> findByUsername(String username);

  Optional<User> findByEmail(String email);

  List<User> findAllByOrganization(Organization organization);

}
