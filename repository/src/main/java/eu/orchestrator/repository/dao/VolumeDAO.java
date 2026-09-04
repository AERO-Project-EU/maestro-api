package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.*;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface VolumeDAO extends JpaRepository<Volume, Long>, QuerydslPredicateExecutor<Volume>,
    QuerydslBinderCustomizer<QVolume> {

  @Override
  default void customize(QuerydslBindings bindings, QVolume volume) {

    bindings.bind(String.class).first(
        (StringPath path, String value) -> path.containsIgnoreCase(value)
    );

    bindings.excluding(volume.component);
    bindings.excluding(volume.dateCreated);
    bindings.excluding(volume.lastModified);

  }

  Optional<Volume> findByDockerPathAndComponent(String dockerpath, Component component);

  Page<Volume> findAllByComponentOrderByDateCreatedDesc(Component component, Pageable pageable);

  List<Volume> findAllByComponentOrderByDateCreatedAsc(Component component);

}
