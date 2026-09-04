package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.repository.domain.QServerlessPropertiesInstance;
import eu.orchestrator.repository.domain.ServerlessPropertiesInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

import java.util.Optional;

public interface ServerlessPropertiesInstanceDAO extends JpaRepository<ServerlessPropertiesInstance, Long>,
        QuerydslPredicateExecutor<ServerlessPropertiesInstance>, QuerydslBinderCustomizer<QServerlessPropertiesInstance> {

    @Override
    default void customize(QuerydslBindings bindings, QServerlessPropertiesInstance serverlessPropertiesInstance) {

        bindings.bind(String.class).first(
                (StringPath path, String value) -> path.containsIgnoreCase(value)
        );

        bindings.excluding(serverlessPropertiesInstance.serverlessProperties);
        bindings.excluding(serverlessPropertiesInstance.componentNodeInstance);
        bindings.excluding(serverlessPropertiesInstance.dateCreated);
        bindings.excluding(serverlessPropertiesInstance.lastModified);

    }

//    Optional<> findByNameAndComponentNodeInstance(String name, ComponentNodeInstance componentNodeInstance);

    Optional<ServerlessPropertiesInstance> findByComponentNodeInstance(
            ComponentNodeInstance componentNodeInstance);

}
