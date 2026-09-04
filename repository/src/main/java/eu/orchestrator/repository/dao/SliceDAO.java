package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.QSlice;
import eu.orchestrator.repository.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SliceDAO extends JpaRepository<Slice, Long>, QuerydslPredicateExecutor<Slice>,
    QuerydslBinderCustomizer<QSlice> {

  @Override
  default void customize(QuerydslBindings bindings, QSlice slice) {

    bindings.bind(String.class).first(
        (StringPath path, String value) -> path.containsIgnoreCase(value)
    );

    bindings.excluding(slice.applicationInstance);
    bindings.excluding(slice.dateCreated);
    bindings.excluding(slice.lastModified);

  }

  Optional<Slice> findByApplicationInstance(ApplicationInstance applicationInstance);

}
