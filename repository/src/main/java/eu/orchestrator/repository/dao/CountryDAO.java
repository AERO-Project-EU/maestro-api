package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.Country;
import eu.orchestrator.repository.domain.QCountry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CountryDAO extends JpaRepository<Country, Long>,
    QuerydslPredicateExecutor<Country>, QuerydslBinderCustomizer<QCountry> {

  @Override
  default void customize(QuerydslBindings bindings, QCountry country) {

    bindings.bind(String.class).first(
        (StringPath path, String value) -> path.containsIgnoreCase(value)
    );

    bindings.excluding(country.dateCreated);

  }

  List<Country> findAllByOrderByName();

  Optional<Country> findByName(String name);

  Optional<Country> findByAlpha2(String alpha2);

}
