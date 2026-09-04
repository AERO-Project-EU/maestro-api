package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ApplicationInstanceQuotaDAO extends JpaRepository<ApplicationInstanceQuota, Long>, QuerydslPredicateExecutor<ApplicationInstanceQuota>, QuerydslBinderCustomizer<QApplicationInstanceQuota> {

    @Override
    default void customize(QuerydslBindings bindings, QApplicationInstanceQuota applicationInstanceQuota) {

        bindings.bind(String.class).first(
                (StringPath path, String value) -> path.containsIgnoreCase(value)
        );

        bindings.excluding(applicationInstanceQuota.applicationInstance);
        bindings.excluding(applicationInstanceQuota.lastModified);
        bindings.excluding(applicationInstanceQuota.dateCreated);

    }

    Optional<ApplicationInstanceQuota> findByApplicationInstance(ApplicationInstance applicationInstance);

}
