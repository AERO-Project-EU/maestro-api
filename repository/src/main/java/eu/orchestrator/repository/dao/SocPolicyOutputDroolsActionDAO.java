package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.QSocPolicyInputKafkaStreamFieldModel;
import eu.orchestrator.repository.domain.QSocPolicyOutputDroolsAction;
import eu.orchestrator.repository.domain.SocPolicy;
import eu.orchestrator.repository.domain.SocPolicyOutputDroolsAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SocPolicyOutputDroolsActionDAO extends JpaRepository<SocPolicyOutputDroolsAction, Long>,
        QuerydslPredicateExecutor<SocPolicyOutputDroolsAction>, QuerydslBinderCustomizer<QSocPolicyOutputDroolsAction> {

    @Override
    default void customize(QuerydslBindings bindings, QSocPolicyOutputDroolsAction socPolicyOutputDroolsAction) {

        bindings.bind(String.class).first(
                (StringPath path, String value) -> path.containsIgnoreCase(value)
        );

        bindings.excluding(socPolicyOutputDroolsAction.dateCreated);
        bindings.excluding(socPolicyOutputDroolsAction.lastModified);

    }

    List<SocPolicyOutputDroolsAction> findAllBySocPolicy(SocPolicy socPolicy);

}
