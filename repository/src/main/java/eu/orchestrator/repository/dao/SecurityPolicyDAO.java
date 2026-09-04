package eu.orchestrator.repository.dao;

import com.querydsl.core.types.dsl.StringPath;
import eu.orchestrator.repository.domain.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 15/3/2019
 */
@Repository
public interface SecurityPolicyDAO extends JpaRepository<SecurityPolicy, Long>,
        QuerydslPredicateExecutor<SecurityPolicy>, QuerydslBinderCustomizer<QSecurityPolicy> {

    @Override
    default void customize(QuerydslBindings bindings, QSecurityPolicy securityPolicy) {

        bindings.bind(String.class).first(
                (StringPath path, String value) -> path.containsIgnoreCase(value)
        );

        bindings.excluding(securityPolicy.dateCreated);
        bindings.excluding(securityPolicy.lastModified);

    }

    Optional<SecurityPolicy> findByName(String name);

    Optional<SecurityPolicy> findByHexID(String hexID);

    Optional<SecurityPolicy> findByApplicationInstance(ApplicationInstance applicationInstance);

    Page<SecurityPolicy> findAllByApplicationInstance(ApplicationInstance applicationInstance, Pageable pageable);

    Optional<SecurityPolicy> findByApplicationInstanceAndName(ApplicationInstance applicationInstance,String name);

    List<SecurityPolicy> findAllByApplicationInstanceAndComponentNodeInstancesContains(ApplicationInstance applicationInstance, ComponentNodeInstance componentNodeInstance);

}