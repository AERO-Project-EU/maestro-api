package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.QSocPolicyInputKafkaStream;
import eu.orchestrator.repository.domain.SocPolicy;
import eu.orchestrator.repository.domain.SocPolicyInputKafkaStream;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SocPolicyInputKafkaStreamDAO extends JpaRepository<SocPolicyInputKafkaStream, Long>,
    QuerydslPredicateExecutor<SocPolicyInputKafkaStream>, QuerydslBinderCustomizer<QSocPolicyInputKafkaStream> {

    @Override
    default void customize(QuerydslBindings bindings, QSocPolicyInputKafkaStream socPolicyInputKafkaStream) {

        bindings.bind(String.class).first(
                (StringPath path, String value) -> path.containsIgnoreCase(value)
        );

        bindings.excluding(socPolicyInputKafkaStream.dateCreated);
        bindings.excluding(socPolicyInputKafkaStream.lastModified);

    }

    List<SocPolicyInputKafkaStream> findAllBySocPolicy(SocPolicy socPolicy);
}
