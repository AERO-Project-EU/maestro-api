package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.QSocPolicyOutputDroolsActionHeader;
import eu.orchestrator.repository.domain.SocPolicyOutputDroolsAction;
import eu.orchestrator.repository.domain.SocPolicyOutputDroolsActionHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SocPolicyOutputDroolsActionHeaderDAO extends JpaRepository<SocPolicyOutputDroolsActionHeader, Long>,
        QuerydslPredicateExecutor<SocPolicyOutputDroolsActionHeader>, QuerydslBinderCustomizer<QSocPolicyOutputDroolsActionHeader> {

    @Override
    default void customize(QuerydslBindings bindings, QSocPolicyOutputDroolsActionHeader socPolicyOutputDroolsActionHeader) {

        bindings.bind(String.class).first(
                (StringPath path, String value) -> path.containsIgnoreCase(value)
        );

        bindings.excluding(socPolicyOutputDroolsActionHeader.dateCreated);
        bindings.excluding(socPolicyOutputDroolsActionHeader.lastModified);

    }

    List<SocPolicyOutputDroolsActionHeader> findAllBySocPolicyOutputDroolsAction(SocPolicyOutputDroolsAction socPolicyOutputDroolsAction);
}
