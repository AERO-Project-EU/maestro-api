package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.Provider;
import eu.orchestrator.repository.domain.Region;
import eu.orchestrator.repository.domain.QRegion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RegionDAO extends JpaRepository<Region, Long>, QuerydslPredicateExecutor<Region>,
    QuerydslBinderCustomizer<QRegion> {

  @Override
  default void customize(QuerydslBindings bindings, QRegion region) {

    bindings.bind(String.class).first(
        (StringPath path, String value) -> path.containsIgnoreCase(value)
    );

    bindings.excluding(region.dateCreated);
    bindings.excluding(region.lastModified);

  }

  Optional<Region> findByName(String name);

  List<Region> findAllByProviderOrderByNameAsc(Provider provider);
}
