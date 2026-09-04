package eu.orchestrator.repository.dao;
import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.Component;
import eu.orchestrator.repository.domain.QServerlessProperties;
import eu.orchestrator.repository.domain.ServerlessProperties;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;
import java.util.Optional;

@Repository
@Transactional
public interface ServerlessPropertiesDAO extends JpaRepository<ServerlessProperties, Long>,
        QuerydslPredicateExecutor<ServerlessProperties>, QuerydslBinderCustomizer<QServerlessProperties> {

    @Override
    default void customize(QuerydslBindings bindings, QServerlessProperties serverlessProperties) {

        bindings.bind(String.class).first(
                (StringPath path, String value) -> path.containsIgnoreCase(value)
        );

        bindings.excluding(serverlessProperties.component);
        bindings.excluding(serverlessProperties.dateCreated);
        bindings.excluding(serverlessProperties.lastModified);


    }

    Optional<ServerlessProperties> findByComponent(Component component);

}