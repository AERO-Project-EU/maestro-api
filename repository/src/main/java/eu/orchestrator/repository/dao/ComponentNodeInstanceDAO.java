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
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface ComponentNodeInstanceDAO extends JpaRepository<ComponentNodeInstance, Long>,
    QuerydslPredicateExecutor<ComponentNodeInstance>,
    QuerydslBinderCustomizer<QComponentNodeInstance> {

  @Override
  default void customize(QuerydslBindings bindings, QComponentNodeInstance componentNodeInstance) {

    bindings.bind(String.class).first(
        (StringPath path, String value) -> path.containsIgnoreCase(value)
    );

    bindings.excluding(componentNodeInstance.componentNode);
    bindings.excluding(componentNodeInstance.dateCreated);
    bindings.excluding(componentNodeInstance.lastModified);

  }

  Optional<ComponentNodeInstance> findByName(String name);

  Optional<ComponentNodeInstance> findByHexID(String hexID);

  Optional<ComponentNodeInstance> findByNameAndApplicationInstance(String name,
      ApplicationInstance applicationInstance);

  Optional<ComponentNodeInstance> findByComponentNode(ComponentNode componentNode);

  Optional<ComponentNodeInstance> findByComponentNodeAndApplicationInstance(
      ComponentNode componentNode, ApplicationInstance applicationInstance);

  Page<ComponentNodeInstance> findAllByApplicationInstance(ApplicationInstance applicationInstance,
      Pageable pageable);

  List<ComponentNodeInstance> findAllByApplicationInstance(ApplicationInstance applicationInstance);

  List<ComponentNodeInstance> findAllByApplicationInstanceAndComponentNode(ApplicationInstance applicationInstance, ComponentNode componentNode);

  Optional<ComponentNodeInstance> findByApplicationInstanceAndName(
      ApplicationInstance applicationInstance, String name);

  Page<ComponentNodeInstance> findAllBySshKey(SSHKey sshKey, Pageable pageable);

  List<ComponentNodeInstance> findAllBySshKey(SSHKey sshKey);

  @Query("select count(cni) from ComponentNodeInstance cni where cni.loadBalancedBy = :loadBalancerCNIID and cni.loadBalancedBy is not null")
  Long calculateWorkers(@Param("loadBalancerCNIID") Long loadBalancerCNIID);

  @Query("select cni from ComponentNodeInstance cni where lower(cni.name) = :name and cni.applicationInstance.id = :applicationInstanceID")
  Optional<ComponentNodeInstance> searchSpecificComponent(@Param("name") String name,
      @Param("applicationInstanceID") Long applicationInstanceID);

  @Query("select cni from ComponentNodeInstance cni where cni.hexID = :hexID and cni.applicationInstance.id = :applicationInstanceID")
  Optional<ComponentNodeInstance> searchSpecificComponentWithHexID(@Param("hexID") String hexID,
      @Param("applicationInstanceID") Long applicationInstanceID);

  List<ComponentNodeInstance> findAllByLoadBalancedByAndLoadBalancedByIsNotNull(
      ComponentNodeInstance componentNodeInstance);

}
