package eu.orchestrator.backend.service.security;

import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.common.exception.NotAuthorizedException;
import eu.orchestrator.backend.kafka.Sender;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.service.k8s.KubernetesService;
import eu.orchestrator.backend.transfer.SecurityConfigurationResultTO;
import eu.orchestrator.backend.transfer.SecurityConfigurationTO;
import eu.orchestrator.backend.util.Util;
import eu.orchestrator.repository.dao.SecurityConfigurationDAO;
import eu.orchestrator.repository.dao.SecurityConfigurationResultDAO;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.ComponentNode;
import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.repository.domain.ProviderType;
import eu.orchestrator.repository.domain.SecurityConfiguration;
import eu.orchestrator.repository.domain.SecurityConfigurationResult;
import eu.orchestrator.repository.domain.User;
import eu.orchestrator.transfer.entities.backend.SecurityConfiguration.SecurityConfigurationType;
import eu.orchestrator.backend.service.applicationinstance.ApplicationInstanceService;
import eu.orchestrator.backend.service.applicationinstance.ComponentNodeInstanceService;
import eu.orchestrator.backend.service.component.ComponentNodeService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
import jakarta.transaction.Transactional;

@Service
@Transactional(rollbackOn = Exception.class)
public class SecurityConfigurationService {

    private static final Logger logger = Logger.getLogger(SecurityConfigurationService.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ApplicationInstanceService applicationInstanceService;

    @Autowired
    private ComponentNodeService componentNodeService;

    @Autowired
    private ComponentNodeInstanceService componentNodeInstanceService;

    @Autowired
    private KubernetesService kubernetesService;

    @Autowired
    private SecurityConfigurationDAO securityConfigurationDAO;

    @Autowired
    private SecurityConfigurationResultDAO securityConfigurationResultDAO;

    @Autowired
    private Sender kafkaSender;


    public Page fetchSecurityConfigurations(Long id, Pageable pageable, User authenticatedUser) {
        ApplicationInstance existingApplicationInstance = applicationInstanceService.fetchApplicationInstanceById(id);
        if (existingApplicationInstance != null
                && existingApplicationInstance.getStatus().equals(ApplicationInstance.ApplicationInstanceStatus.DEPLOYED.name())
                && (authenticatedUser.isAdmin()
                || existingApplicationInstance.getOrganization().getId().equals(authenticatedUser.getOrganization().getId()))) {

            Page<SecurityConfiguration> page;
            if (pageable.getPageSize() > 100) {
                page = securityConfigurationDAO.findAllByApplicationInstance(existingApplicationInstance,
                        PageRequest.of(pageable.getPageNumber(), 100, pageable.getSort()));
            } else {
                page = securityConfigurationDAO.findAllByApplicationInstance(existingApplicationInstance, pageable);
            }

            List<SecurityConfigurationTO> securityConfigurationTOS = new ArrayList<>();

            if (null != page && null != page.getContent() && !page.getContent().isEmpty()) {
                final User loginUser = authenticatedUser;
                page.getContent().forEach(securityConfiguration -> {
                    SecurityConfigurationTO securityConfigurationTO = new SecurityConfigurationTO();
                    securityConfigurationTO.setId(securityConfiguration.getId());
                    if (securityConfiguration.getSecurityConfigurationType()
                            .compareTo(SecurityConfigurationType.CONFIGURATION_INTEGRITY_VERIFICATION.name()) == 0) {
                        securityConfigurationTO.setAllowDelete(false);
                    } else {
                        securityConfigurationTO.setAllowDelete(securityConfiguration.hasDeleteAllowance(loginUser));
                    }
                    securityConfigurationTO.setAllowEdit(false);
                    securityConfigurationTO.setName(securityConfiguration.getName());
                    securityConfigurationTO.setSecurityConfigurationType(securityConfiguration.getSecurityConfigurationType());
                    securityConfigurationTO.setDateCreated(securityConfiguration.getDateCreated());
                    securityConfigurationTOS.add(securityConfigurationTO);
                });
            }
            return new PageImpl<>(securityConfigurationTOS, pageable, page.getTotalElements());
        }
        throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
    }

    public Page fetchSecurityConfigurationResults(Long id, Long securityConfigurationId, Pageable pageable, User authenticatedUser) {
        Optional<SecurityConfiguration> securityConfigurationOptional = securityConfigurationDAO.findById(securityConfigurationId);
        if (securityConfigurationOptional.isPresent() && (authenticatedUser.isAdmin()
                || securityConfigurationOptional.get().getUser().getOrganization().getId().equals(authenticatedUser.getOrganization().getId()))) {
            SecurityConfiguration securityConfiguration = securityConfigurationOptional.get();
            Page<SecurityConfigurationResult> page;
            if (pageable.getPageSize() > 100) {
                page = securityConfigurationResultDAO.findAllBySecurityConfiguration(securityConfiguration,
                        PageRequest.of(pageable.getPageNumber(), 100));
            } else {
                page = securityConfigurationResultDAO.findAllBySecurityConfiguration(securityConfiguration,
                        PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()));
            }

            List<SecurityConfigurationResultTO> securityConfigurationResultTOList = new ArrayList<>();
            if (null != page && null != page.getContent() && !page.getContent().isEmpty()) {
                page.getContent().forEach(securityConfigurationResult -> {
                    SecurityConfigurationResultTO securityConfigurationResultTO = new SecurityConfigurationResultTO();
                    securityConfigurationResultTO.setDateUpdated(securityConfigurationResult.getLastModified());
                    securityConfigurationResultTO.setStatus(securityConfigurationResult.getStatus());
                    securityConfigurationResultTO.setComponentNodeInstance(
                            securityConfigurationResult.getComponentNodeInstance().getName() + "(" + securityConfigurationResult.getComponentNodeInstance()
                                    .getHexID() + ")");
                    securityConfigurationResultTO.setDescription(securityConfigurationResult.getDescription());
                    securityConfigurationResultTOList.add(securityConfigurationResultTO);
                });
            }
            return new PageImpl<>(securityConfigurationResultTOList, pageable, page.getTotalElements());
        }
        throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
    }

    public void createSecurityConfiguration(Long id, SecurityConfigurationTO securityConfigurationTO, User authenticatedUser) {
        ApplicationInstance existingApplicationInstance = applicationInstanceService.fetchApplicationInstanceById(id);
        if (existingApplicationInstance != null && existingApplicationInstance.getStatus().equals(ApplicationInstance.ApplicationInstanceStatus.DEPLOYED.name())
                && (existingApplicationInstance.hasEditAllowance(authenticatedUser))) {

            boolean requiredFields = null != securityConfigurationTO && null != securityConfigurationTO.getName()
                    && !securityConfigurationTO.getName().isEmpty() && null != securityConfigurationTO.getComponentNodeHexIDs()
                    && !securityConfigurationTO.getComponentNodeHexIDs().isEmpty() && null != securityConfigurationTO.getSecurityConfigurationType()
                    && !securityConfigurationTO.getSecurityConfigurationType().isEmpty();

            if (requiredFields) {
                Optional<SecurityConfiguration> securityPolicyOp = securityConfigurationDAO.findByApplicationInstanceAndName(existingApplicationInstance,
                        securityConfigurationTO.getName());
                if (securityPolicyOp.isPresent()) {
                    throw new GenericBusinessException(GenericMessage.SECURITY_CONFIGURATION_ALREADY_EXISTS.getCode(),
                            GenericMessage.SECURITY_CONFIGURATION_ALREADY_EXISTS);
                }
                if (!StringUtils.isAlphanumeric(securityConfigurationTO.getName())) {
                    throw new GenericBusinessException(GenericMessage.SECURITY_CONFIGURATION_NAME_ALPHANUMERIC.getCode(),
                            GenericMessage.SECURITY_CONFIGURATION_NAME_ALPHANUMERIC);
                }
                SecurityConfiguration securityConfiguration = new SecurityConfiguration();
                try {
                    securityConfiguration.setApplicationInstance(existingApplicationInstance);
                    securityConfiguration.setName(securityConfigurationTO.getName());
                    securityConfiguration.setUser(authenticatedUser);
                    securityConfiguration.setHexID(Util.createRandomHEXString());
                    securityConfiguration.setDateCreated(new Date());
                    securityConfiguration.setLastModified(new Date());
                    securityConfiguration.setSecurityConfigurationType(securityConfigurationTO.getSecurityConfigurationType());
                    securityConfiguration = securityConfigurationDAO.save(securityConfiguration);

                    final boolean statusCompleted
                            = securityConfiguration.getSecurityConfigurationType().equals(SecurityConfigurationType.FORENSIC_ACTIVATION.name());

                    for (String componentNodeHexId : securityConfigurationTO.getComponentNodeHexIDs()) {
                        ComponentNode componentNode = componentNodeService.fetchComponentNodeByHexId(componentNodeHexId);
                        if (componentNode != null) {
                            List<ComponentNodeInstance> componentNodeInstances
                                    = componentNodeInstanceService.fetchAllByApplicationInstanceAndComponentNode(existingApplicationInstance, componentNode);
                            SecurityConfiguration finalSecurityConfiguration = securityConfiguration;

                            componentNodeInstances.forEach(componentNodeInstance -> {
                                SecurityConfigurationResult securityConfigurationResult = new SecurityConfigurationResult();
                                securityConfigurationResult.setComponentNodeInstance(componentNodeInstance);
                                securityConfigurationResult.setDateCreated(new Date());
                                securityConfigurationResult.setLastModified(new Date());
                                securityConfigurationResult.setSecurityConfiguration(finalSecurityConfiguration);
                                securityConfigurationResult.setStatus(SecurityConfigurationResult.SecurityConfigurationResultStatus.process.name());

                                //TODO implement this correctly
                                if (statusCompleted) {
                                    securityConfigurationResult.setStatus(SecurityConfigurationResult.SecurityConfigurationResultStatus.COMPLETE.name());
                                }

                                securityConfigurationResult.setHexID(Util.createRandomHEXString());
                                SecurityConfigurationResult securityConfigurationResultDB = securityConfigurationResultDAO.save(securityConfigurationResult);
                                eu.orchestrator.transfer.entities.backend.SecurityConfiguration securityConfigurationAgent
                                        = new eu.orchestrator.transfer.entities.backend.SecurityConfiguration();
                                securityConfigurationAgent.setGraphHexId(existingApplicationInstance.getApplication().getHexID());
                                securityConfigurationAgent.setGraphInstanceHexId(existingApplicationInstance.getHexID());
                                securityConfigurationAgent.setComponentNodeHexId(componentNodeInstance.getComponentNode().getHexID());
                                securityConfigurationAgent.setComponentNodeInstanceHexId(componentNodeInstance.getHexID());
                                securityConfigurationAgent.setSecurityConfigurationType(
                                        SecurityConfigurationType.valueOf(finalSecurityConfiguration.getSecurityConfigurationType()));
                                securityConfigurationAgent.setResultHexId(securityConfigurationResultDB.getHexID());

                                try {
                                    if (existingApplicationInstance.getProvider().getProviderType().getName()
                                            .equals(ProviderType.ProviderName.KUBERNETES.name())
                                            && (finalSecurityConfiguration.getSecurityConfigurationType()
                                            .equals(SecurityConfigurationType.RUNTIME_FILE_INTEGRITY.name()))
                                            || finalSecurityConfiguration.getSecurityConfigurationType()
                                            .equals(SecurityConfigurationType.CONFIGURATION_INTEGRITY_VERIFICATION.name())
                                    ) {
                                        kubernetesService.attestationCheck(componentNodeInstance, securityConfigurationResultDB);
                                    } else {
                                        kafkaSender.sendSecurityConfigurationToAgent(objectMapper.writeValueAsString(securityConfigurationAgent));
                                    }
                                } catch (JsonProcessingException ex) {
                                    logger.log(Level.SEVERE, ex.getMessage(), ex);
                                }
                            });
                        }
                    }
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, ex.getMessage(), ex);
                }
            }
        }
        throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
    }

    public void deleteSecurityConfiguration(Long securityConfigurationId) {
        Optional<SecurityConfiguration> securityConfigurationOptional = securityConfigurationDAO.findById(securityConfigurationId);
        if (!securityConfigurationOptional.isPresent()) {
            throw new NotAuthorizedException(GenericMessage.NOT_FOUND.getCode(), GenericMessage.NOT_FOUND);
        }
        SecurityConfiguration securityConfiguration = securityConfigurationOptional.get();
        List<SecurityConfigurationResult> securityConfigurationList = securityConfigurationResultDAO.findAllBySecurityConfiguration(securityConfiguration);
        if (securityConfiguration.getSecurityConfigurationType().equals(SecurityConfigurationType.FORENSIC_ACTIVATION.name())) {
            for (SecurityConfigurationResult securityConfigurationResult : securityConfigurationList) {
                eu.orchestrator.transfer.entities.backend.SecurityConfiguration securityConfigurationAgent
                        = new eu.orchestrator.transfer.entities.backend.SecurityConfiguration();
                securityConfigurationAgent.setGraphHexId(securityConfiguration.getApplicationInstance().getApplication().getHexID());
                securityConfigurationAgent.setGraphInstanceHexId(securityConfiguration.getApplicationInstance().getHexID());
                securityConfigurationAgent.setComponentNodeHexId(securityConfigurationResult.getComponentNodeInstance().getComponentNode().getHexID());
                securityConfigurationAgent.setComponentNodeInstanceHexId(securityConfigurationResult.getComponentNodeInstance().getHexID());
                securityConfigurationAgent
                        .setSecurityConfigurationType(SecurityConfigurationType.valueOf(securityConfiguration.getSecurityConfigurationType()));
                securityConfigurationAgent.setResultHexId(securityConfiguration.getHexID());

                try {
                    kafkaSender.sendSecurityConfigurationToAgent(objectMapper.writeValueAsString(securityConfigurationAgent));
                } catch (JsonProcessingException ex) {
                    logger.log(Level.SEVERE, ex.getMessage(), ex);
                }
            }
        }
        securityConfigurationList.forEach(securityConfigurationResult -> {
            securityConfigurationResultDAO.delete(securityConfigurationResult);
        });
        securityConfigurationDAO.delete(securityConfiguration);
    }

    public void saveSecurityConfigurationResult(SecurityConfigurationResult securityConfigurationResult) {
        securityConfigurationResultDAO.save(securityConfigurationResult);
    }
}
