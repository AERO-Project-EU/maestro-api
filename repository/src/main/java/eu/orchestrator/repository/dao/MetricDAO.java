package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;

import eu.orchestrator.repository.domain.Metric;
import eu.orchestrator.repository.domain.Plugin;
import eu.orchestrator.repository.domain.QMetric;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MetricDAO extends JpaRepository<Metric, Long>, QuerydslPredicateExecutor<Metric>,
        QuerydslBinderCustomizer<QMetric> {

    @Override
    default void customize(QuerydslBindings bindings, QMetric metric) {

        bindings.bind(String.class).first(
                (StringPath path, String value) -> path.containsIgnoreCase(value)
        );

        bindings.excluding(metric.dateCreated);
        bindings.excluding(metric.lastModified);
        bindings.excluding(metric.plugin);

    }

    Optional<Metric> findByNameAndPlugin(String name, Plugin plugin);

    Page<Metric> findAllByPluginOrderByDateCreated(Plugin plugin, Pageable pageable);

    List<Metric> findAllByPluginOrderByDateCreatedDesc(Plugin plugin);

    @Query("select count(m) from eu.orchestrator.repository.domain.Metric m where m.plugin.pluginID = :pluginID")
    Long calculateMetricsByPlugin(@Param("pluginID") Long pluginID);


    List<Metric> findAllByPlugin(Plugin plugin);

}
