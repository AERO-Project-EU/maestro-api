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
public interface PluginInstanceDAO extends JpaRepository<PluginInstance, Long>,
    QuerydslPredicateExecutor<PluginInstance>, QuerydslBinderCustomizer<QPluginInstance> {

  @Override
  default void customize(QuerydslBindings bindings, QPluginInstance pluginInstance) {

    bindings.bind(String.class).first(
        (StringPath path, String value) -> path.containsIgnoreCase(value)
    );

    bindings.excluding(pluginInstance.plugin);
    bindings.excluding(pluginInstance.componentNodeInstance);
    bindings.excluding(pluginInstance.dateCreated);
    bindings.excluding(pluginInstance.lastModified);

  }

  Optional<PluginInstance> findByNameAndComponentNodeInstance(String name,
      ComponentNodeInstance componentNodeInstance);

  Page<PluginInstance> findAllByComponentNodeInstanceOrderByDateCreatedDesc(
      ComponentNodeInstance componentNodeInstance, Pageable pageable);

  List<PluginInstance> findAllByComponentNodeInstanceOrderByDateCreatedDesc(
      ComponentNodeInstance componentNodeInstance);

}
