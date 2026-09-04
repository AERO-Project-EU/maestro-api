package eu.orchestrator.repository.dao.rainbow;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.rainbow.QSlo;
import eu.orchestrator.repository.domain.rainbow.Slo;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;

@Repository
public interface SloDAO extends JpaRepository<Slo, Long>,
        QuerydslPredicateExecutor<Slo>,
        QuerydslBinderCustomizer<QSlo> {

    @Override
    default void customize(QuerydslBindings bindings, QSlo qSlo) {

        bindings.bind(String.class).first(
                (StringPath path, String value) -> path.containsIgnoreCase(value)
        );

        bindings.excluding(qSlo.createdAt);

    }

    Page<Slo> findAllByApplicationInstance(ApplicationInstance applicationInstance, Pageable pageable);

    Optional<Slo> findByIdAndApplicationInstance(Long id, ApplicationInstance applicationInstance);
}
