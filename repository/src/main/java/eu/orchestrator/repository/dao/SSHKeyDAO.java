package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.Organization;
import eu.orchestrator.repository.domain.QSSHKey;
import eu.orchestrator.repository.domain.SSHKey;
import eu.orchestrator.repository.domain.User;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SSHKeyDAO extends JpaRepository<SSHKey, Long>, QuerydslPredicateExecutor<SSHKey>,
    QuerydslBinderCustomizer<QSSHKey> {

  @Override
  default void customize(QuerydslBindings bindings, QSSHKey sshKey) {

    bindings.bind(String.class).first(
        (StringPath path, String value) -> path.containsIgnoreCase(value)
    );

    bindings.excluding(sshKey.dateCreated);
    bindings.excluding(sshKey.lastModified);

  }

  Page<SSHKey> findAllByUser(User user, Pageable pageable);

  List<SSHKey> findAllByUserOrderByFriendlyNameAsc(User user);

  List<SSHKey> findAllByOrganizationOrderByFriendlyNameAsc(Organization organization);

  Page<SSHKey> findAllByUserOrderByFriendlyNameAsc(User user, Pageable pageable);

  List<SSHKey> findAllByOrderByFriendlyNameAsc();

  Page<SSHKey> findAllByOrderByFriendlyNameAsc(Pageable pageable);

  Optional<SSHKey> findByFriendlyNameAndUser(String friendlyName, User user);

  Optional<SSHKey> findByUserAndDefaultSSH(User user, boolean defaultKey);

  @Query("select count(s) from SSHKey s")
  Long calculateSSHKeys();

  @PreAuthorize("#username == authentication.principal.username")
  @Query("select s.id from SSHKey s where s.friendlyName=:friendlyName and s.user.username=:username")
  Optional<SSHKey> searchByFriendlyNameAndUsername(@Param("friendlyName") String friendlyName,
      @Param("username") String username);

}
