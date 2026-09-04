package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.QSocPolicyInputKafkaStream;
import eu.orchestrator.repository.domain.QSocPolicyInputKafkaStreamFieldModel;
import eu.orchestrator.repository.domain.SocPolicyInputKafkaStream;
import eu.orchestrator.repository.domain.SocPolicyInputKafkaStreamFieldModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SocPolicyInputKafkaStreamFieldModelDAO extends JpaRepository<SocPolicyInputKafkaStreamFieldModel, Long>,
        QuerydslPredicateExecutor<SocPolicyInputKafkaStreamFieldModel>, QuerydslBinderCustomizer<QSocPolicyInputKafkaStreamFieldModel> {


    @Override
    default void customize(QuerydslBindings bindings, QSocPolicyInputKafkaStreamFieldModel socPolicyInputKafkaStreamFieldModel) {

        bindings.bind(String.class).first(
                (StringPath path, String value) -> path.containsIgnoreCase(value)
        );

        bindings.excluding(socPolicyInputKafkaStreamFieldModel.dateCreated);
        bindings.excluding(socPolicyInputKafkaStreamFieldModel.lastModified);

    }

    List<SocPolicyInputKafkaStreamFieldModel> findAllBySocPolicyInputKafkaStream(SocPolicyInputKafkaStream socPolicyInputKafkaStream);
}
