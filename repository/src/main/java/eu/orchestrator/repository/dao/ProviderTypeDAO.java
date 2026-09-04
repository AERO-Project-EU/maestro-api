package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;

import eu.orchestrator.repository.domain.ProviderType;
import eu.orchestrator.repository.domain.QProviderType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProviderTypeDAO extends JpaRepository<ProviderType, Long>,
        QuerydslPredicateExecutor<ProviderType>, QuerydslBinderCustomizer<QProviderType> {

    @Override
    default void customize(QuerydslBindings bindings, QProviderType providerType) {

        bindings.bind(String.class).first(
                (StringPath path, String value) -> path.containsIgnoreCase(value)
        );

    }

    Optional<ProviderType> findByName(String name);

    Optional<ProviderType> findByFriendlyName(String friendlyName);
}
