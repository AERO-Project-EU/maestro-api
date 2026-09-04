package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.QRadioServiceType;
import eu.orchestrator.repository.domain.RadioServiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RadioServiceTypeDAO extends JpaRepository<RadioServiceType, Long>,
    QuerydslPredicateExecutor<RadioServiceType>, QuerydslBinderCustomizer<QRadioServiceType> {

  @Override
  default void customize(QuerydslBindings bindings, QRadioServiceType application) {

    bindings.bind(String.class).first(
        (StringPath path, String value) -> path.containsIgnoreCase(value)
    );

  }

  List<RadioServiceType> findAllByOrderBySstValue();

  Optional<RadioServiceType> findBySstValue(Integer sstValue);

}
