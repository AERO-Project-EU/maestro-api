package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.Profile;
import eu.orchestrator.repository.domain.QProfile;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileDAO extends JpaRepository<Profile, Long>,
    QuerydslPredicateExecutor<Profile>, QuerydslBinderCustomizer<QProfile> {


  @Override
  default void customize(QuerydslBindings bindings, QProfile profile) {

    bindings.bind(String.class).first(
        (StringPath path, String value) -> path.containsIgnoreCase(value)
    );

  }

  Optional<Profile> findByApplicationInstanceAndName(ApplicationInstance applicationInstance,
      String name);

  Page<Profile> findAllByApplicationInstance(ApplicationInstance applicationInstance,
      Pageable pageable);

  Optional<Profile> findByHexID(String hexID);

}
