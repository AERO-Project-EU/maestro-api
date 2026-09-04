package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.repository.domain.QVolumeInstance;
import eu.orchestrator.repository.domain.Volume;
import eu.orchestrator.repository.domain.VolumeInstance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface VolumeInstanceDAO extends JpaRepository<VolumeInstance, Long>,
    QuerydslPredicateExecutor<VolumeInstance>, QuerydslBinderCustomizer<QVolumeInstance> {

  @Override
  default void customize(QuerydslBindings bindings, QVolumeInstance volumeInstance) {

    bindings.bind(String.class).first(
        (StringPath path, String value) -> path.containsIgnoreCase(value)
    );

    bindings.excluding(volumeInstance.volume);
    bindings.excluding(volumeInstance.componentNodeInstance);
    bindings.excluding(volumeInstance.lastModified);
    bindings.excluding(volumeInstance.dateCreated);

  }

  Optional<VolumeInstance> findByVolumeAndComponentNodeInstance(Volume volume,
      ComponentNodeInstance componentNodeInstance);

  Page<VolumeInstance> findAllByComponentNodeInstanceOrderByDateCreatedDesc(
      ComponentNodeInstance componentNodeInstance, Pageable pageable);

  Optional<VolumeInstance> findByVolumeInstanceID(Long volumeInstanceID);

}
