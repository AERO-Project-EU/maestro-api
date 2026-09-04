package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.QSlicePlacementAttachmentPoint;
import eu.orchestrator.repository.domain.SlicePlacement;
import eu.orchestrator.repository.domain.SlicePlacementAttachmentPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SlicePlacementAttachmentPointDAO extends
    JpaRepository<SlicePlacementAttachmentPoint, Long>,
    QuerydslPredicateExecutor<SlicePlacementAttachmentPoint>,
    QuerydslBinderCustomizer<QSlicePlacementAttachmentPoint> {

  @Override
  default void customize(QuerydslBindings bindings,
      QSlicePlacementAttachmentPoint slicePlacementAttachmentPoint) {

    bindings.bind(String.class).first(
        (StringPath path, String value) -> path.containsIgnoreCase(value)
    );

    bindings.excluding(slicePlacementAttachmentPoint.slicePlacement);
    bindings.excluding(slicePlacementAttachmentPoint.dateCreated);
    bindings.excluding(slicePlacementAttachmentPoint.lastModified);

  }

  List<SlicePlacementAttachmentPoint> findAllBySlicePlacement(SlicePlacement slicePlacement);

}
