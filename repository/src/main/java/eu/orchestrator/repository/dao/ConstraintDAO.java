package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConstraintDAO extends JpaRepository<Constraint, Long>,
    QuerydslPredicateExecutor<Constraint>, QuerydslBinderCustomizer<QConstraint> {

  @Override
  default void customize(QuerydslBindings bindings, QConstraint constraint) {

    bindings.bind(String.class).first(
        (StringPath path, String value) -> path.containsIgnoreCase(value)
    );

    bindings.excluding(constraint.componentNodeInstance);
    bindings.excluding(constraint.lastModified);
    bindings.excluding(constraint.dateCreated);

  }

  Optional<Constraint> findAllByApplicationInstanceAndConstraintMetricAndComponentNodeInstance(
      ApplicationInstance applicationInstance, String constraintMetric,
      ComponentNodeInstance componentNodeInstance);

  Optional<Constraint> findAllByApplicationInstanceAndConstraintMetricAndGraphLinkNodeInstance(
      ApplicationInstance applicationInstance, String constraintMetric,
      GraphLinkNodeInstance graphLinkNodeInstance);

  Optional<Constraint> findAllByApplicationInstanceAndInterfaceInstance(
      ApplicationInstance applicationInstance, InterfaceInstance interfaceInstance);

  Page<Constraint> findAllByApplicationInstance(ApplicationInstance applicationInstance,
      Pageable pageable);

  List<Constraint> findAllByApplicationInstance(ApplicationInstance applicationInstance);

  List<Constraint> findAllByApplicationInstanceAndComponentNodeInstance(
      ApplicationInstance applicationInstance, ComponentNodeInstance componentNodeInstance);

  Page<Constraint> findAllByApplicationInstanceAndConstraintCategory(
      ApplicationInstance applicationInstance, String constraintCategory, Pageable pageable);

  List<Constraint> findAllByApplicationInstanceAndConstraintCategory(
      ApplicationInstance applicationInstance, String constraintCategory);

  Page<Constraint> findAllByRadioServiceTypeAndConstraintCategory(RadioServiceType radioServiceType,
      String constraintCategory, Pageable pageable);

  Page<Constraint> findAllByQiAndConstraintCategory(QI qi, String constraintCategory,
      Pageable pageable);

  List<Constraint> findAllByQiAndConstraintCategory(QI qi, String constraintCategory);

  List<Constraint> findAllByRadioServiceTypeAndConstraintCategory(RadioServiceType radioServiceType,
      String constraintCategory);

}
