package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ProviderQuotaDAO extends JpaRepository<ProviderQuota, Long>,
    QuerydslPredicateExecutor<ProviderQuota>, QuerydslBinderCustomizer<QProviderQuota> {

  @Override
  default void customize(QuerydslBindings bindings, QProviderQuota providerQuota) {

    bindings.bind(String.class).first(
        (StringPath path, String value) -> path.containsIgnoreCase(value)
    );

    bindings.excluding(providerQuota.provider);
    bindings.excluding(providerQuota.lastModified);
    bindings.excluding(providerQuota.dateCreated);

  }

  Optional<ProviderQuota> findByProvider(Provider provider);

}
