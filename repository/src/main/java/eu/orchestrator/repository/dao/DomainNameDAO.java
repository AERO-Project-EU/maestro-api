package eu.orchestrator.repository.dao;

import eu.orchestrator.repository.domain.ComponentNodeInstanceIP;
import eu.orchestrator.repository.domain.DomainName;
import eu.orchestrator.repository.domain.Organization;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface DomainNameDAO extends JpaRepository<DomainName, Long> {

  Page<DomainName> findAllByOrganization(Organization organization, Pageable pageable);

  List<DomainName> findAllByComponentNodeInstanceIP(ComponentNodeInstanceIP componentNodeInstanceIP);
}

