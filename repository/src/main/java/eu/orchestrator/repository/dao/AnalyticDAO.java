package eu.orchestrator.repository.dao;

import eu.orchestrator.repository.domain.Analytic;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.QAnalytic;

import com.querydsl.core.types.dsl.StringPath;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;


@Repository
//TODO rename to metric when we break database to modules
public interface AnalyticDAO extends JpaRepository<Analytic, Long>,
        QuerydslPredicateExecutor<Analytic>, QuerydslBinderCustomizer<QAnalytic> {

    @Override
    default void customize(QuerydslBindings bindings, QAnalytic analytic) {
        bindings.bind(String.class).first(
                (StringPath path, String value) -> path.containsIgnoreCase(value)
        );
        bindings.excluding(analytic.lastModified);
        bindings.excluding(analytic.dateCreated);
    }

    Page<Analytic> findAllByApplicationInstance(ApplicationInstance applicationInstance, Pageable pageable);

    Page<Analytic> findAllByApplicationInstanceAndName(ApplicationInstance applicationInstance, String name, Pageable pageable);

}
