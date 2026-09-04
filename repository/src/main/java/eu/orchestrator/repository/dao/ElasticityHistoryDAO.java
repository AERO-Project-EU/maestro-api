package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.ComponentNode;
import eu.orchestrator.repository.domain.ElasticityHistory;
import eu.orchestrator.repository.domain.QElasticityHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 19/4/2019
 */
@Repository
public interface ElasticityHistoryDAO  extends JpaRepository<ElasticityHistory, Long>,
        QuerydslPredicateExecutor<ElasticityHistory>,
        QuerydslBinderCustomizer<QElasticityHistory> {

    @Override
    default void customize(QuerydslBindings bindings, QElasticityHistory elasticityHistory) {

        bindings.bind(String.class).first(
                (StringPath path, String value) -> path.containsIgnoreCase(value)
        );

        bindings.excluding(elasticityHistory.dateCreated);
        bindings.excluding(elasticityHistory.lastModified);
    }

    List<ElasticityHistory> findAllByApplicationInstance(ApplicationInstance applicationInstance);

    List<ElasticityHistory> findAllByApplicationInstanceAndAndComponentNodeOrderByDateCreated(ApplicationInstance applicationInstance, ComponentNode componentNode);

    List<ElasticityHistory> findTop10ByApplicationInstanceAndAndComponentNode(ApplicationInstance applicationInstance, ComponentNode componentNode);
    
    Optional<List<ElasticityHistory>> findByApplicationInstanceAndAndComponentNodeOrderByIdDesc(ApplicationInstance applicationInstance, ComponentNode componentNode);
}
