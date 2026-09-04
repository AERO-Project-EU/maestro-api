package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SliceProviderDAO extends JpaRepository<SliceProvider, Long>,
    QuerydslPredicateExecutor<SliceProvider>, QuerydslBinderCustomizer<QSliceProvider> {

  @Override
  default void customize(QuerydslBindings bindings, QSliceProvider sliceProvider) {

    bindings.bind(String.class).first(
        (StringPath path, String value) -> path.containsIgnoreCase(value)
    );

    bindings.excluding(sliceProvider.slice);
    bindings.excluding(sliceProvider.dateCreated);
    bindings.excluding(sliceProvider.lastModified);

  }

  List<SliceProvider> findAllBySlice(Slice slice);

}
