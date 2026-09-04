package eu.orchestrator.backend.service.domain;

import eu.orchestrator.backend.config.DnsServerConfig;
import eu.orchestrator.common.exception.BadRequestBusinessException;
import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.transfer.DomainNameTO;
import eu.orchestrator.backend.transfer.RestResponseSPA;
import eu.orchestrator.backend.util.DNSUtil;
import eu.orchestrator.repository.dao.ComponentNodeInstanceIPDAO;
import eu.orchestrator.repository.dao.DomainNameDAO;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.repository.domain.ComponentNodeInstanceIP;
import eu.orchestrator.repository.domain.DomainName;
import eu.orchestrator.repository.domain.User;
import eu.orchestrator.backend.service.applicationinstance.ApplicationInstanceService;
import eu.orchestrator.backend.service.applicationinstance.ComponentNodeInstanceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import jakarta.transaction.Transactional;

@Service
@Transactional(rollbackOn = Exception.class)
public class DomainService {

    @Autowired
    private DomainNameDAO domainNameDAO;

    @Autowired
    private ComponentNodeInstanceIPDAO componentNodeInstanceIPDAO;

    @Autowired
    private ApplicationInstanceService applicationInstanceService;

    @Autowired
    private ComponentNodeInstanceService componentNodeInstanceService;

    @Autowired
    private DnsServerConfig dnsServerConfig;


    public DomainName fetchDomainNameById(Long id) {
        Optional<DomainName> domainNameOptional = domainNameDAO.findById(id);
        return domainNameOptional.orElse(null);
    }

    public void create(DomainNameTO domainNameTO, User authenticatedUser) {
        // just reserved (Associate == false)
        if (null != domainNameTO.getSuffix()
                && !domainNameTO.getSuffix().isEmpty()
                && null != domainNameTO.getPrefix()
                && !domainNameTO.getPrefix().isEmpty()
                && !Boolean.TRUE.equals(domainNameTO.getAssociate())) {

            RestResponseSPA restResponseSPA = DNSUtil
                    .addDomain(dnsServerConfig, domainNameTO.getPrefix(), domainNameTO
                            .getSuffix(), "127.0.0.1");

            if (restResponseSPA.getCode().compareTo("1") == 0) {
                DomainName domainName = new DomainName();
                domainName.setDomain(domainNameTO.getPrefix() + "." + domainNameTO.getSuffix());
                domainName.setDateCreated(new Date());
                domainName.setLastModified(new Date());
                domainName.setUser(authenticatedUser);
                domainName.setOrganization(authenticatedUser.getOrganization());

                domainNameDAO.save(domainName);
            } else {
                throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
            }
        }

        // reserved + associate (Associate == true)
        if (null != domainNameTO.getSuffix()
                && !domainNameTO.getSuffix().isEmpty()
                && null != domainNameTO.getPrefix()
                && !domainNameTO.getPrefix().isEmpty()
                && Boolean.TRUE.equals(domainNameTO.getAssociate())
                && null != domainNameTO.getComponentNodeInstanceIPAddress()
                && !domainNameTO.getComponentNodeInstanceIPAddress().isEmpty()) {

            RestResponseSPA restResponseSPA = DNSUtil
                    .addDomain(dnsServerConfig, domainNameTO.getPrefix(), domainNameTO.getSuffix(),
                            domainNameTO.getComponentNodeInstanceIPAddress());

            if (restResponseSPA.getCode().compareTo("1") == 0) {

                Optional<ComponentNodeInstanceIP> componentNodeInstanceIPOptional = componentNodeInstanceIPDAO
                        .findAllByApplicationInstanceAndComponentNodeInstanceAndIp(
                                applicationInstanceService.fetchApplicationInstanceById(domainNameTO.getApplicationInstanceId()),
                                componentNodeInstanceService.fetchComponentNodeInstanceById(domainNameTO.getComponentNodeInstanceId()),
                                domainNameTO.getComponentNodeInstanceIPAddress());

                DomainName domainName = new DomainName();
                domainName.setDomain(domainNameTO.getPrefix() + "." + domainNameTO.getSuffix());
                domainName.setDateCreated(new Date());
                domainName.setLastModified(new Date());
                domainName.setComponentNodeInstanceIP(componentNodeInstanceIPOptional.get());
                domainName.setUser(authenticatedUser);
                domainName.setOrganization(authenticatedUser.getOrganization());

                domainNameDAO.save(domainName);
            } else {
                throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
            }
        }
    }

    public void update(DomainNameTO domainNameTO, User authenticatedUser) {
        DomainName domainName = fetchDomainNameById(domainNameTO.getDomainNameID());

        if (domainName != null) {

            // just reserved (Associate == false)
            if (null != domainNameTO.getSuffix()
                    && !domainNameTO.getSuffix().isEmpty()
                    && null != domainNameTO.getPrefix()
                    && !domainNameTO.getPrefix().isEmpty()
                    && !Boolean.TRUE.equals(domainNameTO.getAssociate())) {

                RestResponseSPA restResponseSPA = DNSUtil
                        .updateDomain(dnsServerConfig, domainNameTO.getPrefix(), domainNameTO
                                .getSuffix(), "127.0.0.1");

                if (restResponseSPA.getCode().compareTo("1") == 0) {
                    domainName.setDomainNameID(domainNameTO.getDomainNameID());
                    domainName.setDomain(domainNameTO.getPrefix() + "." + domainNameTO.getSuffix());
                    domainName.setComponentNodeInstanceIP(null);
                    domainName.setLastModified(new Date());
                    domainName.setUser(authenticatedUser);
                    domainName.setOrganization(authenticatedUser.getOrganization());

                    domainNameDAO.save(domainName);
                } else {
                    throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
                }
            }

            // reserved + associate (Associate == true)
            if (null != domainNameTO.getSuffix()
                    && !domainNameTO.getSuffix().isEmpty()
                    && null != domainNameTO.getPrefix()
                    && !domainNameTO.getPrefix().isEmpty()
                    && Boolean.TRUE.equals(domainNameTO.getAssociate())
                    && null != domainNameTO.getComponentNodeInstanceIPAddress()
                    && !domainNameTO.getComponentNodeInstanceIPAddress().isEmpty()) {

                RestResponseSPA restResponseSPA = DNSUtil
                        .updateDomain(dnsServerConfig, domainNameTO.getPrefix(), domainNameTO.getSuffix(),
                                domainNameTO.getComponentNodeInstanceIPAddress());

                if (restResponseSPA.getCode().compareTo("1") == 0) {

                    ApplicationInstance applicationInstance = applicationInstanceService.fetchApplicationInstanceById(domainNameTO.getApplicationInstanceId());
                    ComponentNodeInstance componentNodeInstance = componentNodeInstanceService.fetchComponentNodeInstanceById(
                            domainNameTO.getComponentNodeInstanceId());

                    Optional<ComponentNodeInstanceIP> componentNodeInstanceIPOptional = componentNodeInstanceIPDAO
                            .findAllByApplicationInstanceAndComponentNodeInstanceAndIp(applicationInstance,
                                    componentNodeInstance, domainNameTO.getComponentNodeInstanceIPAddress());

                    domainName = new DomainName();
                    domainName.setDomainNameID(domainNameTO.getDomainNameID());
                    domainName.setDomain(domainNameTO.getPrefix() + "." + domainNameTO.getSuffix());
                    domainName.setLastModified(new Date());
                    domainName.setComponentNodeInstanceIP(componentNodeInstanceIPOptional.get());
                    domainName.setUser(authenticatedUser);
                    domainName.setOrganization(authenticatedUser.getOrganization());

                    domainNameDAO.save(domainName);
                } else {
                    throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
                }
            }
            throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(), GenericMessage.REQUIRED_FIELDS_MISSING);
        } else {
            throw new BadRequestBusinessException(GenericMessage.DOMAIN_DOES_NOT_EXIST.getCode(), GenericMessage.DOMAIN_DOES_NOT_EXIST);
        }
    }

    public Page fetchDomains(Pageable pageable, DomainName fDomainName, User authenticatedUser) {
        Page<DomainName> page;
        if (authenticatedUser.isAdmin()) {
            page = domainNameDAO.findAll(pageable);
        } else {
            page = domainNameDAO.findAllByOrganization(authenticatedUser.getOrganization(), pageable);
        }

        List<DomainNameTO> listTOs = new ArrayList<>();
        page.getContent().forEach(object -> {
            DomainNameTO domainNameTO = new DomainNameTO();
            domainNameTO.setDomain(object.getDomain());
            domainNameTO.setDomainNameID(object.getDomainNameID());
            domainNameTO.setDateCreated(object.getDateCreated());

            if (null != object.getComponentNodeInstanceIP()) {
                domainNameTO.setApplicationInstanceName(object.getComponentNodeInstanceIP().getApplicationInstance().getName());
                domainNameTO.setApplicationInstanceHexId(object.getComponentNodeInstanceIP().getApplicationInstance().getHexID());
                domainNameTO.setComponentNodeInstanceName(object.getComponentNodeInstanceIP().getComponentNodeInstance().getName());
                domainNameTO.setComponentNodeInstanceHexId(object.getComponentNodeInstanceIP().getComponentNodeInstance().getHexID());
                domainNameTO.setComponentNodeInstanceIPAddress(object.getComponentNodeInstanceIP().getIp());
            }
            listTOs.add(domainNameTO);
        });
        return new PageImpl<>(listTOs, pageable, page.getTotalElements());
    }

    public String[] getAvailableSuffix() {
        return dnsServerConfig.getDnsDomains();
    }

    public void deleteDomainName(Long id) {

        DomainName domainName = fetchDomainNameById(id);

        if (domainName != null) {
            RestResponseSPA restResponseSPA = DNSUtil.deleteDomain(dnsServerConfig, domainName.getDomain());
            if (restResponseSPA.getCode().compareTo("1") == 0 || restResponseSPA.getCode().compareTo("17") == 0) {
                domainNameDAO.deleteById(id);
            } else {
                throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
            }
        } else {
            throw new BadRequestBusinessException(GenericMessage.DOMAIN_DOES_NOT_EXIST.getCode(), GenericMessage.DOMAIN_DOES_NOT_EXIST);
        }
    }

    public DomainNameTO fetchDomainTOById(Long id) {

        DomainName domainName = fetchDomainNameById(id);

        if (domainName != null) {
            DomainNameTO domainNameTO = new DomainNameTO();
            domainNameTO.setDomain(domainName.getDomain());
            domainNameTO.setDomainNameID(domainName.getDomainNameID());
            domainNameTO.setDateCreated(domainName.getDateCreated());

            if (null != domainName.getComponentNodeInstanceIP()) {
                domainNameTO.setApplicationInstanceName(
                        domainName.getComponentNodeInstanceIP().getApplicationInstance().getName());
                domainNameTO.setApplicationInstanceHexId(
                        domainName.getComponentNodeInstanceIP().getApplicationInstance().getHexID());
                domainNameTO.setApplicationInstanceId(
                        domainName.getComponentNodeInstanceIP().getApplicationInstance().getApplicationInstanceID());
                domainNameTO.setComponentNodeInstanceName(
                        domainName.getComponentNodeInstanceIP().getComponentNodeInstance().getName());
                domainNameTO.setComponentNodeInstanceHexId(
                        domainName.getComponentNodeInstanceIP().getComponentNodeInstance().getHexID());
                domainNameTO.setComponentNodeInstanceId(
                        domainName.getComponentNodeInstanceIP().getComponentNodeInstance().getComponentNodeInstanceID());
                domainNameTO.setComponentNodeInstanceIPAddress(domainName.getComponentNodeInstanceIP().getIp());
            }
            return domainNameTO;
        } else {
            throw new BadRequestBusinessException(GenericMessage.DOMAIN_DOES_NOT_EXIST.getCode(), GenericMessage.DOMAIN_DOES_NOT_EXIST);
        }
    }

    public List<DomainName> fetchllDomainNamesByComponentNodeInstanceIP(ComponentNodeInstanceIP componentNodeInstanceIP) {
        return domainNameDAO.findAllByComponentNodeInstanceIP(componentNodeInstanceIP);
    }

    public void save(DomainName domainName) {
        domainNameDAO.save(domainName);
    }
}
