package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.Profile;
import eu.orchestrator.repository.domain.ProfileMetric;
import eu.orchestrator.repository.domain.QProfileMetric;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileMetricDAO extends JpaRepository<ProfileMetric, Long>,
    QuerydslPredicateExecutor<ProfileMetric>, QuerydslBinderCustomizer<QProfileMetric> {


  @Override
  default void customize(QuerydslBindings bindings, QProfileMetric profileMetric) {

    bindings.bind(String.class).first(
        (StringPath path, String value) -> path.containsIgnoreCase(value)
    );

  }


  List<ProfileMetric> findAllByProfile(Profile profile);
}
