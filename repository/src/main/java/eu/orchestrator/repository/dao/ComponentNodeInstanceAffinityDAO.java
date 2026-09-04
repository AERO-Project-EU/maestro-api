package eu.orchestrator.repository.dao;

import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.repository.domain.ComponentNodeInstanceAffinity;
import eu.orchestrator.repository.domain.QComponentNodeInstanceAffinity;

import com.querydsl.core.types.dsl.StringPath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

import java.util.List;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 7/9/23
 */
public interface ComponentNodeInstanceAffinityDAO extends JpaRepository<ComponentNodeInstanceAffinity, Long>,
        QuerydslPredicateExecutor<ComponentNodeInstanceAffinity>, QuerydslBinderCustomizer<QComponentNodeInstanceAffinity> {

    @Override
    default void customize(QuerydslBindings bindings,
            QComponentNodeInstanceAffinity componentNodeInstanceAffinity) {

        bindings.bind(String.class).first(
                (StringPath path, String value) -> path.containsIgnoreCase(value)
        );

        bindings.excluding(componentNodeInstanceAffinity.componentNodeInstance);
        bindings.excluding(componentNodeInstanceAffinity.dateCreated);
        bindings.excluding(componentNodeInstanceAffinity.lastModified);

    }

    List<ComponentNodeInstanceAffinity> findAllByApplicationInstance(ApplicationInstance applicationInstance);

    List<ComponentNodeInstanceAffinity> findAllByComponentNodeInstance(ComponentNodeInstance componentNodeInstance);

}