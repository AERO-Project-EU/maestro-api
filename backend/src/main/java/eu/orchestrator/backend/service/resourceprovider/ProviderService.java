package eu.orchestrator.backend.service.resourceprovider;

import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.common.exception.NotAuthorizedException;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.transfer.ProviderQuotaTO;
import eu.orchestrator.backend.transfer.ProviderTO;
import eu.orchestrator.backend.transfer.ProviderTypeTO;
import eu.orchestrator.backend.transfer.RegionTO;
import eu.orchestrator.backend.util.Util;
import eu.orchestrator.repository.dao.ProviderDAO;
import eu.orchestrator.repository.dao.ProviderHistoryDAO;
import eu.orchestrator.repository.dao.ProviderQuotaDAO;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.repository.domain.Provider;
import eu.orchestrator.repository.domain.ProviderHistory;
import eu.orchestrator.repository.domain.ProviderQuota;
import eu.orchestrator.repository.domain.ProviderType;
import eu.orchestrator.repository.domain.ProviderType.ProviderName;
import eu.orchestrator.repository.domain.Region;
import eu.orchestrator.repository.domain.User;
import eu.orchestrator.backend.service.applicationinstance.ApplicationInstanceService;
import eu.orchestrator.backend.service.applicationinstance.ComponentNodeInstanceService;

import com.google.gson.Gson;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;

import static eu.orchestrator.repository.domain.QProvider.provider;

@Service
@Transactional(rollbackOn = Exception.class)
public class ProviderService {

    private static final Logger logger = Logger.getLogger(ProviderService.class.getName());

    @Autowired
    private ProviderTypeService providerTypeService;

    @Autowired
    private ApplicationInstanceService applicationInstanceService;

    @Autowired
    private ComponentNodeInstanceService componentNodeInstanceService;

    @Autowired
    private RegionService regionService;

    @Autowired
    private ProviderDAO providerDAO;

    @Autowired
    private ProviderQuotaDAO providerQuotaDAO;

    @Autowired
    private ProviderHistoryDAO providerHistoryDAO;

    @Autowired
    private EntityManager entityManager;

    @Value("${token.signer.secret}")
    private String tokenSecret;


    public void saveProvider(Provider provider) {
        providerDAO.save(provider);
    }

    public Provider findById(Long id) {
        Optional<Provider> optionalProvider = providerDAO.findById(id);
        return optionalProvider.orElse(null);
    }

    public Provider findByName(String name) {
        Optional<Provider> optionalProvider = providerDAO.findByName(name);
        return optionalProvider.orElse(null);
    }

    public Provider fetchByOne(BooleanExpression predicate) {
        Optional<Provider> providerOP = providerDAO.findOne(predicate);
        return providerOP.orElse(null);
    }

    public Provider fetchByNameAndProjectAndUsername(String name, String project, String username) {
        Optional<Provider> providerOP = providerDAO.findByNameAndProjectAndUsername(name, project, username);
        return providerOP.orElse(null);
    }

    public Page fetchProvidersForDeployment(Pageable pageable, String filters, Provider fProvider, User authenticatedUser) {
        BooleanExpression predicate = provider.eq(provider).and(provider.providerID.notIn(-1L, -2L)).and(provider.internalProvider.eq(false));
        boolean checkRequestParam = null != filters && !filters.isEmpty();
        boolean proceedWithRequestBody = true;
        if (checkRequestParam) {
            try {
                Provider filterProvider = new Gson().fromJson(new String(Base64.decodeBase64(filters)), Provider.class);
                if (null != filterProvider) {
                    proceedWithRequestBody = false;
                    if (null != filterProvider.getName() && !filterProvider.getName().isEmpty()) {
                        predicate = predicate.and(provider.name.containsIgnoreCase(filterProvider.getName()));
                    }
                    if (null != filterProvider.getDefaultProvider()) {
                        predicate = predicate.and(provider.defaultProvider.eq(filterProvider.getDefaultProvider()));
                    }
                    if (null != filterProvider.getInternalProvider()) {
                        predicate = predicate.and(provider.internalProvider.eq(filterProvider.getInternalProvider()));
                    }
                    if (null != filterProvider.getProviderType()) {
                        predicate = predicate.and(provider.providerType.eq(filterProvider.getProviderType()));
                    }
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
        if (proceedWithRequestBody) {
            if (null != fProvider) {
                if (null != fProvider.getName() && !fProvider.getName().isEmpty()) {
                    predicate = predicate.and(provider.name.containsIgnoreCase(fProvider.getName()));
                }
                if (null != fProvider.getDefaultProvider()) {
                    predicate = predicate.and(provider.defaultProvider.eq(fProvider.getDefaultProvider()));
                }
                if (null != fProvider.getInternalProvider()) {
                    predicate = predicate.and(provider.internalProvider.eq(fProvider.getInternalProvider()));
                }
                if (null != fProvider.getProviderType()) {
                    predicate = predicate.and(provider.providerType.eq(fProvider.getProviderType()));
                }
            }
        }

        if (!authenticatedUser.isAdmin()) {
            predicate = predicate.and(provider.organization.eq(authenticatedUser.getOrganization()));
        }

        Page<Provider> page;
        if (pageable.getPageSize() > 100) {
            page = providerDAO.findAll(predicate, PageRequest.of(pageable.getPageNumber(), 100, pageable.getSort()));
        } else {
            page = providerDAO.findAll(predicate, pageable);
        }

        List<ProviderTO> providerTOs = new ArrayList<>();
        if (null != page && null != page.getContent() && !page.getContent().isEmpty()) {
            final User loginUser = authenticatedUser;
            page.getContent().forEach(prov -> {
                ProviderTO providerTO = new ProviderTO();
                providerTO.setProviderID(prov.getProviderID());
                providerTO.setDateCreated(prov.getDateCreated());
                providerTO.setName(prov.getName());
                providerTO.setRegionsCounter(prov.getRegionIDs().size());
                providerTO.setDefaultProvider(prov.getDefaultProvider());
                providerTO.setLastModified(prov.getLastModified());
                providerTO.setOrganization(prov.getOrganization().getName());
                providerTO.setAllowDelete(prov.hasDeleteAllowance(loginUser));
                providerTO.setAllowEdit(prov.hasEditAllowance(loginUser));
                providerTO.setProviderType(new ProviderTypeTO(prov.getProviderType().getId(), prov.getProviderType().getName(),
                        prov.getProviderType().getFriendlyName()));

                Optional<ProviderQuota> providerQuotaOP = providerQuotaDAO.findByProvider(prov);
                if (providerQuotaOP.isPresent()) {
                    ProviderQuota providerQuota = providerQuotaOP.get();
                    ProviderQuotaTO providerQuotaTO = new ProviderQuotaTO();
                    providerQuotaTO.setId(providerQuota.getId());
                    providerQuotaTO.setRunningInstances(providerQuota.getRunningInstances());
                    providerQuotaTO.setMaxInstances(providerQuota.getMaxInstances());
                    providerQuotaTO.setInstancesUtilization(providerQuota.getInstancesUtilization());
                    providerQuotaTO.setUsedVirtualCPUs(providerQuota.getUsedVirtualCPUs());
                    providerQuotaTO.setMaxVirtualCPUs(providerQuota.getMaxVirtualCPUs());
                    providerQuotaTO.setVirtualCPUsUtilization(providerQuota.getVirtualCPUsUtilization());
                    providerQuotaTO.setUsedMemory(providerQuota.getUsedMemory());
                    providerQuotaTO.setMaxMemory(providerQuota.getMaxMemory());
                    providerQuotaTO.setMemoryUtilization(providerQuota.getMemoryUtilization());
                    providerQuotaTO.setUsedFloatingIPs(providerQuota.getUsedFloatingIPs());
                    providerQuotaTO.setClaimedFloatingIPs(providerQuota.getClaimedFloatingIPs());
                    providerQuotaTO.setFloatingIPsConsumption(providerQuota.getFloatingIPsConsumption());
                    providerQuotaTO.setDateCreated(providerQuota.getDateCreated());
                    providerQuotaTO.setLastModified(providerQuota.getLastModified());
                    providerTO.setProviderQuota(providerQuotaTO);
                }

                List<Region> regions = regionService.fetchAllByProviderOrderByNameAsc(prov);
                if (null != regions && !regions.isEmpty()) {
                    List<RegionTO> regionTOs = new ArrayList<>();
                    regions.forEach(region -> {
                        RegionTO regionTO = new RegionTO();
                        regionTO.setRegionID(region.getRegionID());
                        regionTO.setName(region.getName());
                        regionTOs.add(regionTO);
                    });
                    providerTO.setRegions(regionTOs);
                }
                providerTOs.add(providerTO);
            });
        }
        return new PageImpl<>(providerTOs, pageable, page.getTotalElements());
    }

    public Page fetchProviders(Pageable pageable, String filters, Provider fProvider, User authenticatedUser) {
        BooleanExpression predicate = provider.eq(provider).and(provider.providerID.notIn(-1L, -2L)).and(provider.internalProvider.eq(false));

        boolean checkRequestParam = null != filters && !filters.isEmpty();
        boolean proceedWithRequestBody = true;

        if (checkRequestParam) {
            try {
                Provider filterProvider = new Gson().fromJson(new String(Base64.decodeBase64(filters)), Provider.class);
                if (null != filterProvider) {
                    proceedWithRequestBody = false;
                    if (null != filterProvider.getName() && !filterProvider.getName().isEmpty()) {
                        predicate = predicate.and(provider.name.containsIgnoreCase(filterProvider.getName()));
                    }
                    if (null != filterProvider.getDefaultProvider()) {
                        predicate = predicate.and(provider.defaultProvider.eq(filterProvider.getDefaultProvider()));
                    }
                    if (null != filterProvider.getInternalProvider()) {
                        predicate = predicate.and(provider.internalProvider.eq(filterProvider.getInternalProvider()));
                    }
                    if (null != filterProvider.getProviderType()) {
                        predicate = predicate.and(provider.providerType.eq(filterProvider.getProviderType()));
                    }
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }

        if (proceedWithRequestBody) {
            if (null != fProvider) {
                if (null != fProvider.getName() && !fProvider.getName().isEmpty()) {
                    predicate = predicate.and(provider.name.containsIgnoreCase(fProvider.getName()));
                }
                if (null != fProvider.getDefaultProvider()) {
                    predicate = predicate.and(provider.defaultProvider.eq(fProvider.getDefaultProvider()));
                }
                if (null != fProvider.getInternalProvider()) {
                    predicate = predicate.and(provider.internalProvider.eq(fProvider.getInternalProvider()));
                }
                if (null != fProvider.getProviderType()) {
                    predicate = predicate.and(provider.providerType.eq(fProvider.getProviderType()));
                }
            }
        }
        if (!authenticatedUser.isAdmin()) {
            predicate = predicate.and(provider.organization.eq(authenticatedUser.getOrganization()));
        }

        Page<Provider> page;
        if (pageable.getPageSize() > 100) {
            page = providerDAO.findAll(predicate, PageRequest.of(pageable.getPageNumber(), 100, pageable.getSort()));
        } else {
            page = providerDAO.findAll(predicate, pageable);
        }

        List<ProviderTO> providerTOs = new ArrayList<>();
        if (null != page && null != page.getContent() && !page.getContent().isEmpty()) {
            final User loginUser = authenticatedUser;
            page.getContent().forEach(prov -> {
                ProviderTO providerTO = new ProviderTO();
                providerTO.setProviderID(prov.getProviderID());
                providerTO.setDateCreated(prov.getDateCreated());
                providerTO.setName(prov.getName());
                providerTO.setRegionsCounter(prov.getRegionIDs().size());
                providerTO.setDefaultProvider(prov.getDefaultProvider());
                providerTO.setLastModified(prov.getLastModified());
                providerTO.setOrganization(prov.getOrganization().getName());
                providerTO.setAllowDelete(prov.hasDeleteAllowance(loginUser));
                providerTO.setAllowEdit(prov.hasEditAllowance(loginUser));
                providerTO.setProviderType(new ProviderTypeTO(prov.getProviderType().getId(), prov.getProviderType().getName(),
                        prov.getProviderType().getFriendlyName()));

                Optional<ProviderQuota> providerQuotaOP = providerQuotaDAO.findByProvider(prov);
                if (providerQuotaOP.isPresent()) {
                    ProviderQuota providerQuota = providerQuotaOP.get();
                    ProviderQuotaTO providerQuotaTO = new ProviderQuotaTO();
                    providerQuotaTO.setId(providerQuota.getId());
                    providerQuotaTO.setRunningInstances(providerQuota.getRunningInstances());
                    providerQuotaTO.setMaxInstances(providerQuota.getMaxInstances());
                    providerQuotaTO.setInstancesUtilization(providerQuota.getInstancesUtilization());
                    providerQuotaTO.setUsedVirtualCPUs(providerQuota.getUsedVirtualCPUs());
                    providerQuotaTO.setMaxVirtualCPUs(providerQuota.getMaxVirtualCPUs());
                    providerQuotaTO.setVirtualCPUsUtilization(providerQuota.getVirtualCPUsUtilization());
                    providerQuotaTO.setUsedMemory(providerQuota.getUsedMemory());
                    providerQuotaTO.setMaxMemory(providerQuota.getMaxMemory());
                    providerQuotaTO.setMemoryUtilization(providerQuota.getMemoryUtilization());
                    providerQuotaTO.setUsedFloatingIPs(providerQuota.getUsedFloatingIPs());
                    providerQuotaTO.setClaimedFloatingIPs(providerQuota.getClaimedFloatingIPs());
                    providerQuotaTO.setFloatingIPsConsumption(providerQuota.getFloatingIPsConsumption());
                    providerQuotaTO.setDateCreated(providerQuota.getDateCreated());
                    providerQuotaTO.setLastModified(providerQuota.getLastModified());
                    providerTO.setProviderQuota(providerQuotaTO);
                }

                List<Region> regions = regionService.fetchAllByProviderOrderByNameAsc(prov);
                if (null != regions && !regions.isEmpty()) {
                    List<RegionTO> regionTOs = new ArrayList<>();
                    regions.forEach(region -> {
                        RegionTO regionTO = new RegionTO();
                        regionTO.setRegionID(region.getRegionID());
                        regionTO.setName(region.getName());
                        regionTOs.add(regionTO);
                    });
                    providerTO.setRegions(regionTOs);
                }
                providerTOs.add(providerTO);
            });
        }
        return new PageImpl<>(providerTOs, pageable, page.getTotalElements());
    }

    public Provider fetchByIdAndUser(Long id, User authenticatedUser) {
        Provider provider = findById(id);
        if (provider != null && provider.hasEditAllowance(authenticatedUser)) {
            Optional<ProviderQuota> providerQuotaOP = providerQuotaDAO.findByProvider(provider);
            providerQuotaOP.ifPresent(provider::setProviderQuota);
            return provider;
        } else {
            throw new NotAuthorizedException(GenericMessage.NOT_AUTHORIZED.getCode(), GenericMessage.NOT_AUTHORIZED);
        }
    }

    public void create(Provider provider, User authenticatedUser) {
        // Check if user has permission to add provider
        if (!authenticatedUser.isOrganizationAdmin() && !authenticatedUser.isAdmin()) {
            throw new NotAuthorizedException(GenericMessage.PROVIDER_NOT_AUTHORIZED.getCode(), GenericMessage.PROVIDER_NOT_AUTHORIZED);
        }
        // Check if Provider already exists
        if (providerDAO.findByNameAndOrganization(provider.getName(), authenticatedUser.getOrganization()).isPresent()) {
            throw new GenericBusinessException(GenericMessage.PROVIDER_ALREADY_EXISTS.getCode(), GenericMessage.PROVIDER_ALREADY_EXISTS);
        }

        provider.setProviderType(providerTypeService.fetchById(provider.getProviderType().getId()));
        if (provider.getProviderType().getName().equals(ProviderType.ProviderName.OPENSTACK.name())) {
            if ((null == provider.getEndpoint() || provider.getEndpoint().isEmpty()) && (
                    null == provider.getUsername() || provider.getUsername().isEmpty())
                    && (null == provider.getPassword() || provider.getPassword().isEmpty()) && (
                    null == provider.getDomain() || provider.getDomain().isEmpty()) && (
                    null == provider.getProject() || provider.getProject().isEmpty()) && (
                    null == provider.getPublicNetwork() || provider.getPublicNetwork().isEmpty())) {
                throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(), GenericMessage.REQUIRED_FIELDS_MISSING);
            }

        } else if (provider.getProviderType().getName().equals(ProviderName.AWS.name())) {
            if ((null == provider.getPublicKey() || provider.getPublicKey().isEmpty()) && (
                    null == provider.getPrivateKey() || provider.getPrivateKey().isEmpty())) {
                throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(), GenericMessage.REQUIRED_FIELDS_MISSING);
            }
        } else if (provider.getProviderType().getName().equals(ProviderName.GCC.name())) {
            if ((null == provider.getUsername() || provider.getUsername().isEmpty())
                    && (null == provider.getPrivateKey() || provider.getPrivateKey().isEmpty())) {
                throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(), GenericMessage.REQUIRED_FIELDS_MISSING);
            }

        } else if (provider.getProviderType().getName().equals(ProviderName.IOT_GATEWAY.name())) {
            if ((null == provider.getEndpoint() || provider.getEndpoint().isEmpty()) && (
                    null == provider.getUsername() || provider.getUsername().isEmpty())
                    && (null == provider.getPassword() || provider.getPassword().isEmpty()) && (
                    null == provider.getMeshIdentifier() || provider.getMeshIdentifier().isEmpty())) {
                throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(), GenericMessage.REQUIRED_FIELDS_MISSING);
            }

        } else if (provider.getProviderType().getName().equals(ProviderName.FIFTH_GENERATION_TELCO_PROVIDER.name())) {
            if ((null == provider.getEndpoint() || provider.getEndpoint().isEmpty()) && (
                    null == provider.getUsername() || provider.getUsername().isEmpty())
                    && (null == provider.getPassword() || provider.getPassword().isEmpty())) {
                throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(), GenericMessage.REQUIRED_FIELDS_MISSING);
            }
        }

        provider.setUser(authenticatedUser);
        provider.setInternalProvider(false);
        provider.setDateCreated(new Date());
        provider.setLastModified(new Date());
        provider.setOrganization(authenticatedUser.getOrganization());

        List<Region> newRegions = null;
        if (null != provider.getRegions() && !provider.getRegions().isEmpty()) {
            newRegions = new ArrayList<>(provider.getRegions());
            provider.setRegions(null);
        }

        boolean noRegionDefine = newRegions.stream().allMatch(region -> (null == region.getName() || region.getName().isEmpty()));

        if (noRegionDefine) {
            throw new GenericBusinessException(GenericMessage.PROVIDER_NOT_REGION_SET.getCode(), GenericMessage.PROVIDER_NOT_REGION_SET);
        }

        provider.setPassword(Util.encrypt(provider.getPassword(), tokenSecret));
        providerDAO.save(provider);

        if (null != newRegions && !newRegions.isEmpty()) {
            for (Region region : newRegions) {
                // New Region
                boolean rFields = null != region.getName() && !region.getName().isEmpty();
                if (rFields) {
                    region.setDateCreated(new Date());
                    region.setLastModified(new Date());
                    region.setProvider(provider);
                    regionService.saveRegion(region);
                }
            }
        }
    }


    public void update(Provider provider, User authenticatedUser) {
        // Check if Provider already exists or not
        Provider existingProvider = findById(provider.getProviderID());
        if (existingProvider != null && existingProvider.hasEditAllowance(authenticatedUser)) {
            if (!existingProvider.getName().equals(provider.getName())) {
                // Check if Provider already exists
                if (providerDAO.findByNameAndOrganization(provider.getName(), authenticatedUser.getOrganization()).isPresent()) {
                    throw new GenericBusinessException(GenericMessage.PROVIDER_ALREADY_EXISTS.getCode(), GenericMessage.PROVIDER_ALREADY_EXISTS);
                }
            }
            existingProvider.setProviderType(providerTypeService.fetchById(provider.getProviderType().getId()));
            if (provider.getProviderCredentialsChange()) {
                if (provider.getProviderType().getName().equals(ProviderType.ProviderName.OPENSTACK.name())) {
                    if (null == provider.getEndpoint() || provider.getEndpoint().isEmpty()
                            || null == provider.getUsername() || provider.getUsername().isEmpty()
                            || null == provider.getPassword() || provider.getPassword().isEmpty()
                            || null == provider.getDomain() || provider.getDomain().isEmpty()
                            || null == provider.getProject() || provider.getProject().isEmpty()
                            || null == provider.getPublicNetwork() || provider.getPublicNetwork().isEmpty()) {
                        throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(), GenericMessage.REQUIRED_FIELDS_MISSING);
                    }
                    existingProvider.setEndpoint(provider.getEndpoint());
                    existingProvider.setUsername(provider.getUsername());
                    existingProvider.setDomain(provider.getDomain());
                    existingProvider.setProject(provider.getProject());
                    existingProvider.setPassword(Util.encrypt(provider.getPassword(), tokenSecret));
                    if (null != provider.getProxy() && !provider.getProxy().isEmpty()) {
                        existingProvider.setProxy(provider.getProxy());
                    }
                } else if (provider.getProviderType().getName().equals(ProviderName.AWS.name())) {
                    if (null == provider.getPublicKey() || provider.getPublicKey().isEmpty() || null == provider.getPrivateKey()
                            || provider.getPrivateKey().isEmpty()) {
                        throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(), GenericMessage.REQUIRED_FIELDS_MISSING);
                    }
                    existingProvider.setPrivateKey(provider.getPrivateKey());
                    existingProvider.setPublicKey(provider.getPublicKey());
                } else if (provider.getProviderType().getName().equals(ProviderName.GCC.name())) {
                    if (null == provider.getUsername() || provider.getUsername().isEmpty() || null == provider.getPrivateKey()
                            || provider.getPrivateKey().isEmpty()) {
                        throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(), GenericMessage.REQUIRED_FIELDS_MISSING);
                    }
                    existingProvider.setUsername(provider.getUsername());
                    existingProvider.setPrivateKey(provider.getPrivateKey());
                } else if (provider.getProviderType().getName().equals(ProviderName.IOT_GATEWAY.name())) {
                    if (null == provider.getEndpoint() || provider.getEndpoint().isEmpty()
                            || null == provider.getUsername() || provider.getUsername().isEmpty()
                            || null == provider.getPassword() || provider.getPassword().isEmpty()
                            || null == provider.getMeshIdentifier() || provider.getMeshIdentifier().isEmpty()) {
                        throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(), GenericMessage.REQUIRED_FIELDS_MISSING);
                    }
                    existingProvider.setEndpoint(provider.getEndpoint());
                    existingProvider.setUsername(provider.getUsername());
                    existingProvider.setMeshIdentifier(provider.getMeshIdentifier());
                    existingProvider.setPassword(Util.encrypt(provider.getPassword(), tokenSecret));

                } else if (provider.getProviderType().getName().equals(ProviderName.FIFTH_GENERATION_TELCO_PROVIDER.name())) {

                    if (null == provider.getEndpoint() || provider.getEndpoint().isEmpty()
                            || null == provider.getUsername() || provider.getUsername().isEmpty()
                            || null == provider.getPassword() || provider.getPassword().isEmpty()) {
                        throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(), GenericMessage.REQUIRED_FIELDS_MISSING);
                    }
                    existingProvider.setEndpoint(provider.getEndpoint());
                    existingProvider.setUsername(provider.getUsername());
                    existingProvider.setPassword(Util.encrypt(provider.getPassword(), tokenSecret));
                } else if (provider.getProviderType().getName().equals(ProviderName.RAINBOW_KUBERNETES.name())
                        || provider.getProviderType().getName().equals(ProviderName.KUBERNETES.name())
                        || provider.getProviderType().getName().equals(ProviderName.FIVE_G_INDUCE_SLICE.name())) {
                    existingProvider.setEndpoint(provider.getEndpoint());
                    existingProvider.setUsername(provider.getUsername());
                    existingProvider.setPublicKey(provider.getPublicKey());
                    existingProvider.setPrivateKey(provider.getPrivateKey());
                }
            }

            if (provider.getProviderType().getName().equals(ProviderName.RAINBOW_KUBERNETES.name())
                    || provider.getProviderType().getName().equals(ProviderName.KUBERNETES.name())
                    || provider.getProviderType().getName().equals(ProviderName.FIVE_G_INDUCE_SLICE.name())) {
                existingProvider.setNetworkModeHost(provider.getNetworkModeHost());
            }

            existingProvider.setName(provider.getName());

            if (null != provider.getImageID() && !provider.getImageID().isEmpty()) {
                existingProvider.setImageID(provider.getImageID());
            } else {
                existingProvider.setImageID(null);
            }
            if (null != provider.getNetworkID() && !provider.getNetworkID().isEmpty()) {
                existingProvider.setNetworkID(provider.getNetworkID());
            } else {
                existingProvider.setNetworkID(null);
            }
            existingProvider.setDefaultProvider(provider.getDefaultProvider());
            existingProvider.setLastModified(new Date());
            providerDAO.save(existingProvider);

            List<Long> existingRegionIDs = existingProvider.getRegionIDs();
            List<Region> newRegions = provider.getRegions();

            if (null != newRegions && !newRegions.isEmpty()) {
                newRegions.forEach(newRegion -> {
                    if (null != newRegion.getRegionID() && newRegion.getRegionID() != 0) {
                        // Existing, needs to be updated
                        Region exRegion = regionService.fetchById(newRegion.getRegionID());
                        if (exRegion != null) {
                            exRegion.setDateCreated(new Date());
                            exRegion.setProvider(existingProvider);
                            exRegion.setName(newRegion.getName());
                            exRegion.setLastModified(new Date());
                            regionService.saveRegion(exRegion);
                            existingRegionIDs.remove(newRegion.getRegionID());
                        }
                    } else {
                        // New, needs to be added
                        newRegion.setLastModified(new Date());
                        newRegion.setDateCreated(new Date());
                        newRegion.setProvider(existingProvider);
                        regionService.saveRegion(newRegion);
                    }
                });
            }
            if (!existingRegionIDs.isEmpty()) {
                existingRegionIDs.forEach(existingRegionID -> {
                    //regionDAO.deleteById(existingRegionID);
                    Query q = entityManager.createNativeQuery("DELETE FROM region WHERE id = ?");
                    q.setParameter(1, existingRegionID);
                    entityManager.joinTransaction();
                    q.executeUpdate();
                });
            }
        } else {
            throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
        }
    }

    public void changeDefaultStatus(Long id, User authenticatedUser) {
        // Check if Provider already exists or not
        Provider existingProvider = findById(id);
        if (existingProvider != null && existingProvider.hasEditAllowance(authenticatedUser)) {
            if (!Boolean.TRUE.equals(existingProvider.getDefaultProvider())) {
                // Check if it is another one default
                Optional<Provider> existingDefaultProviderOP
                        = providerDAO.findByOrganizationAndDefaultProvider(existingProvider.getOrganization(), true);
                if (existingDefaultProviderOP.isPresent()) {
                    Provider existingDefaultProvider = existingDefaultProviderOP.get();
                    existingDefaultProvider.setLastModified(new Date());
                    existingDefaultProvider.setDefaultProvider(false);
                    providerDAO.save(existingDefaultProvider);
                }
                existingProvider.setDefaultProvider(true);
            } else {
                existingProvider.setDefaultProvider(false);
            }
            existingProvider.setLastModified(new Date());
            providerDAO.save(existingProvider);
        } else {
            throw new NotAuthorizedException(GenericMessage.PROVIDER_NOT_AUTHORIZED.getCode(), GenericMessage.PROVIDER_NOT_AUTHORIZED);
        }
    }

    public void delete(Long id, User authenticatedUser) {
        // Check if Provider exists or not
        Provider existingProvider = findById(id);
        if (existingProvider != null && existingProvider.hasDeleteAllowance(authenticatedUser)) {
            // Check if provider is the default one
            if (Boolean.TRUE.equals(existingProvider.getDefaultProvider())) {
                throw new GenericBusinessException(GenericMessage.CHANGE_DEFAULT_PROVIDER.getCode(), GenericMessage.CHANGE_DEFAULT_PROVIDER);
            }
            // Check if Provider is used
            List<ApplicationInstance> applicationInstances = applicationInstanceService.fetchAllByUser(authenticatedUser);
            if (null != applicationInstances && !applicationInstances.isEmpty()) {
                for (ApplicationInstance applicationInstance : applicationInstances) {
                    if (applicationInstance.getProvider().getProviderID().equals(existingProvider.getProviderID())) {
                        throw new GenericBusinessException(GenericMessage.PROVIDER_USED_IN_APPLICATION_INSTANCES.getCode(),
                                GenericMessage.PROVIDER_USED_IN_APPLICATION_INSTANCES);
                    } else if (applicationInstance.getProvider().getProviderID() == -1L || applicationInstance.getProvider().getProviderID() == -2L) {
                        List<ComponentNodeInstance> componentNodeInstances
                                = componentNodeInstanceService.fetchAllComponentNodeInstancesByApplicationInstance(applicationInstance);
                        if (null != componentNodeInstances && !componentNodeInstances.isEmpty()) {
                            for (ComponentNodeInstance componentNodeInstance : componentNodeInstances) {
                                if (componentNodeInstance.getProvider().getProviderID().equals(existingProvider.getProviderID())) {
                                    throw new GenericBusinessException(GenericMessage.PROVIDER_USED_IN_APPLICATION_INSTANCES.getCode(),
                                            GenericMessage.PROVIDER_USED_IN_APPLICATION_INSTANCES);
                                }
                            }
                        }
                    }
                }
            }
            if (null != existingProvider.getRegions() && !existingProvider.getRegions().isEmpty()) {
                existingProvider.getRegions().forEach(region -> {
                    regionService.deleteRegion(region);
                });
            }
            providerDAO.delete(existingProvider);
        } else {
            throw new NotAuthorizedException(GenericMessage.PROVIDER_NOT_AUTHORIZED.getCode(), GenericMessage.PROVIDER_NOT_AUTHORIZED);
        }
    }

    public ProviderHistory fetchProviderHistoryTopByOrderByDateCreatedDesc() {
        Optional<ProviderHistory> providerHistoryOp = providerHistoryDAO.findTopByOrderByDateCreatedDesc();
        return providerHistoryOp.orElse(null);
    }

    public void saveProviderHistory(ProviderHistory providerHistory) {
        providerHistoryDAO.save(providerHistory);
    }

}
