package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.ProviderHistory;
import eu.orchestrator.repository.domain.QProviderHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 24/4/2019
 */
@Repository
public interface ProviderHistoryDAO extends JpaRepository<ProviderHistory, Long>,
        QuerydslPredicateExecutor<ProviderHistory>,
        QuerydslBinderCustomizer<QProviderHistory> {

    @Override
    default void customize(QuerydslBindings bindings, QProviderHistory providerHistory) {

        bindings.bind(String.class).first(
                (StringPath path, String value) -> path.containsIgnoreCase(value)
        );

        bindings.excluding(providerHistory.dateCreated);
    }

    List<ProviderHistory> findTop200ByOrderByDateCreated();

    Optional<ProviderHistory> findTopByOrderByDateCreatedDesc();

}
