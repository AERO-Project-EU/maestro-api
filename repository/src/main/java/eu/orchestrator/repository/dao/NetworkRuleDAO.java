package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.NetworkRule;
import eu.orchestrator.repository.domain.QNetworkRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface NetworkRuleDAO extends JpaRepository<NetworkRule, Long>,
        QuerydslPredicateExecutor<NetworkRule>,
        QuerydslBinderCustomizer<QNetworkRule> {


  @Override
  default void customize(QuerydslBindings bindings, QNetworkRule networkRule) {

    bindings.bind(String.class).first(
        (StringPath path, String value) -> path.containsIgnoreCase(value)
    );

  }

  Optional<NetworkRule> findByComponentNodeInstanceHexIdAndIp(String componentNodeInstanceHexId, String ip);

  List<NetworkRule> findAllByComponentNodeInstanceHexId(String componentNodeInstanceHexId);

  void deleteByComponentNodeInstanceHexIdAndIp(String componentNodeInstanceHexId, String ip);

}
