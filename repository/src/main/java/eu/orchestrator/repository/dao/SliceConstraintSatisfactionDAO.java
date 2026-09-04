package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.QSliceConstraintSatisfaction;
import eu.orchestrator.repository.domain.Slice;
import eu.orchestrator.repository.domain.SliceConstraintSatisfaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SliceConstraintSatisfactionDAO extends
    JpaRepository<SliceConstraintSatisfaction, Long>,
    QuerydslPredicateExecutor<SliceConstraintSatisfaction>,
    QuerydslBinderCustomizer<QSliceConstraintSatisfaction> {

  @Override
  default void customize(QuerydslBindings bindings,
      QSliceConstraintSatisfaction sliceConstraintSatisfaction) {

    bindings.bind(String.class).first(
        (StringPath path, String value) -> path.containsIgnoreCase(value)
    );

    bindings.excluding(sliceConstraintSatisfaction.slice);
    bindings.excluding(sliceConstraintSatisfaction.dateCreated);
    bindings.excluding(sliceConstraintSatisfaction.lastModified);

  }

  List<SliceConstraintSatisfaction> findAllBySlice(Slice slice);

}
