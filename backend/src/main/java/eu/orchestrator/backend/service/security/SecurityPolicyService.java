package eu.orchestrator.backend.service.security;

import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.common.exception.NotFoundException;
import eu.orchestrator.backend.kafka.Sender;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.transfer.DashboardTO;
import eu.orchestrator.backend.transfer.SecurityPolicyTO;
import eu.orchestrator.backend.util.Util;
import eu.orchestrator.repository.dao.SecurityPolicyDAO;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.ComponentNode;
import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.repository.domain.SecurityPolicy;
import eu.orchestrator.repository.domain.User;
import eu.orchestrator.transfer.entities.agent.ActionsCommands;
import eu.orchestrator.backend.service.applicationinstance.ApplicationInstanceService;
import eu.orchestrator.backend.service.applicationinstance.ComponentNodeInstanceService;
import eu.orchestrator.backend.service.component.ComponentNodeService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
public class SecurityPolicyService {

    private static final Logger logger = Logger.getLogger(SecurityPolicyService.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String DASHBOARD_TOPIC = "/dashboard";

    @Autowired
    private ApplicationInstanceService applicationInstanceService;

    @Autowired
    private ComponentNodeService componentNodeService;

    @Autowired
    private ComponentNodeInstanceService componentNodeInstanceService;

    @Autowired
    private SecurityPolicyDAO securityPolicyDAO;

    @Autowired
    private Sender kafkaSender;

    @Autowired
    private SimpMessagingTemplate wsTemplate;


    public Page fetchSecurityPoliciesById(Long id, Pageable pageable, User authenticatedUser) {
        ApplicationInstance existingApplicationInstance = applicationInstanceService.fetchApplicationInstanceById(id);
        if (existingApplicationInstance != null && existingApplicationInstance.getStatus().
                equals(ApplicationInstance.ApplicationInstanceStatus.DEPLOYED.name())
                && (authenticatedUser.isAdmin() || existingApplicationInstance.getOrganization().getId()
                .equals(authenticatedUser.getOrganization().getId()))) {
            Page<SecurityPolicy> page;
            if (pageable.getPageSize() > 100) {
                page = securityPolicyDAO.findAllByApplicationInstance(existingApplicationInstance,
                        PageRequest.of(pageable.getPageNumber(), 100, pageable.getSort()));
            } else {
                page = securityPolicyDAO.findAllByApplicationInstance(existingApplicationInstance, pageable);
            }
            List<SecurityPolicyTO> securityPolicyTOS = new ArrayList<>();
            if (null != page && null != page.getContent() && !page.getContent().isEmpty()) {
                final User loginUser = authenticatedUser;
                page.getContent().forEach(securityPolicy -> {
                    SecurityPolicyTO securityPolicyTO = new SecurityPolicyTO();
                    BeanUtils.copyProperties(securityPolicy, securityPolicyTO);
                    securityPolicyTO.setApplicationID(existingApplicationInstance.getApplication().getId());
                    securityPolicyTO.setApplicationInstanceID(existingApplicationInstance.getApplicationInstanceID());
                    List<String> componentNodeInstaceHexIDs = new ArrayList<>();
                    securityPolicy.getComponentNodeInstances().forEach(componentNodeInstance -> {
                        componentNodeInstaceHexIDs.add(componentNodeInstance.getHexID());
                    });
                    securityPolicyTO.setComponentNodeHexIDs(componentNodeInstaceHexIDs);
                    securityPolicyTO.setAllowDelete(securityPolicy.hasDeleteAllowance(loginUser));
                    securityPolicyTO.setAllowEdit(false);
                    securityPolicyTO.setStatus(securityPolicy.getStatus());
                    securityPolicyTO.setDateCreated(securityPolicy.getDateCreated());
                    securityPolicyTOS.add(securityPolicyTO);
                });
            }
            return new PageImpl<>(securityPolicyTOS, pageable, page.getTotalElements());
        }
        throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
    }

    public void createSecurityPolicyById(Long id, SecurityPolicyTO securityPolicyTO, User authenticatedUser) {
        ApplicationInstance existingApplicationInstance = applicationInstanceService.fetchApplicationInstanceById(id);

        if (existingApplicationInstance != null
                && existingApplicationInstance.getStatus().equals(ApplicationInstance.ApplicationInstanceStatus.DEPLOYED.name())
                && Boolean.TRUE.equals(existingApplicationInstance.hasEditAllowance(authenticatedUser))) {

            boolean requiredFields = null != securityPolicyTO && null != securityPolicyTO.getName() && !securityPolicyTO.getName().isEmpty()
                    && null != securityPolicyTO.getComponentNodeHexIDs() && !securityPolicyTO.getComponentNodeHexIDs().isEmpty()
                    && null != securityPolicyTO.getIpAddress() && !securityPolicyTO.getIpAddress().isEmpty()
                    && null != securityPolicyTO.getRule() && !securityPolicyTO.getRule().isEmpty();
            SecurityPolicy securityPolicy = new SecurityPolicy();
            if (requiredFields) {
                Optional<SecurityPolicy> securityPolicyOp
                        = securityPolicyDAO.findByApplicationInstanceAndName(existingApplicationInstance, securityPolicyTO.getName());
                if (securityPolicyOp.isPresent()) {
                    throw new GenericBusinessException(GenericMessage.SECURITY_POLICY_ALREADY_EXISTS.getCode(), GenericMessage.SECURITY_POLICY_ALREADY_EXISTS);
                }
                if (!StringUtils.isAlphanumeric(securityPolicyTO.getName())) {
                    throw new GenericBusinessException(GenericMessage.SECURITY_POLICY_NAME_ALPHANUMERIC.getCode(),
                            GenericMessage.SECURITY_POLICY_NAME_ALPHANUMERIC);
                }
                try {
                    securityPolicy.setName(securityPolicyTO.getName());
                    securityPolicy.setHexID(Util.createRandomHEXString());
                    securityPolicy.setApplicationInstance(existingApplicationInstance);
                    securityPolicy.setIpAddress(securityPolicyTO.getIpAddress());
                    securityPolicy.setRule(securityPolicyTO.getRule());
                    //TODO check if we need to make the status pending at first
                    securityPolicy.setStatus(SecurityPolicy.SecurityPolicyStatus.PENDING.name());
                    securityPolicy.setDateCreated(new Date());
                    securityPolicy.setLastModified(new Date());
                    securityPolicy.setUser(authenticatedUser);

                    List<ComponentNodeInstance> securityComponentNodeInstances = new ArrayList<>();

                    for (String componentNodeHexID : securityPolicyTO.getComponentNodeHexIDs()) {
                        ComponentNode componentNode = componentNodeService.fetchComponentNodeByHexId(componentNodeHexID);
                        if (null != componentNode) {
                            List<ComponentNodeInstance> componentNodeInstances
                                    = componentNodeInstanceService.fetchAllByApplicationInstanceAndComponentNode(existingApplicationInstance, componentNode);
                            if (null != componentNodeInstances && !componentNodeInstances.isEmpty()) {
                                componentNodeInstances.forEach(componentNodeInstance -> {
                                    securityComponentNodeInstances.add(componentNodeInstance);
                                });
                            } else {
                                throw new NotFoundException(GenericMessage.SECURITY_POLICY_COMPONENT_NODE_DOESNT_EXISTS.getCode(),
                                        GenericMessage.SECURITY_POLICY_COMPONENT_NODE_DOESNT_EXISTS);
                            }
                        } else {
                            throw new NotFoundException(GenericMessage.SECURITY_POLICY_COMPONENT_NODE_INSTANCE_DOESNT_EXISTS.getCode(),
                                    GenericMessage.SECURITY_POLICY_COMPONENT_NODE_INSTANCE_DOESNT_EXISTS);
                        }
                    }
                    securityPolicy.setComponentNodeInstances(securityComponentNodeInstances);
                    securityPolicyDAO.save(securityPolicy);
                    securityPolicy.getComponentNodeInstances().forEach(componentNodeInstance -> {
                        componentNodeInstance.getSecurityPolicies().add(securityPolicy);
                        componentNodeInstance.setLastModified(new Date());
                        componentNodeInstanceService.saveComponentNodeInstance(componentNodeInstance);
                    });
                    existingApplicationInstance.getSecurityPolicies().add(securityPolicy);
                    existingApplicationInstance.setLastModified(new Date());
                    applicationInstanceService.saveApplicationInstance(existingApplicationInstance);
                    String command = "";
                    command = SecurityPolicy.RuleType.valueOf(securityPolicy.getRule()).getFriendlyName();
                    command += " --add --ip " + securityPolicy.getIpAddress();
                    List<String> componentNodeHexIDs = new ArrayList<>();
                    securityPolicy.getComponentNodeInstances().forEach(componentNodeInstance -> {
                        if (!componentNodeHexIDs.contains(componentNodeInstance.getComponentNode().getHexID())) {
                            componentNodeHexIDs.add(componentNodeInstance.getComponentNode().getHexID());
                        }
                    });
//                    for (String componentNodeHexID : componentNodeHexIDs) {
//                        ActionsCommands actionsCommands = new ActionsCommands();
//                        actionsCommands.setCommand(command);
//                        String actionID = existingApplicationInstance.getApplication().getHexID() + ":" + existingApplicationInstance.getHexID() + ":"
//                                + componentNodeHexID + ":*";
//                        actionsCommands.setId(actionID);
//                        String actionsCommandsAsString = objectMapper.writeValueAsString(actionsCommands);
//                        kafkaSender.sendSecurityPolicy(actionsCommandsAsString);
//                    }
                    //TODO what happens if the objectMapper fails???
                    try {
                        DashboardTO dashboardTO = new DashboardTO();
                        dashboardTO.setIpsSecurity(true);
                        String notificationAsString = null;
                        notificationAsString = objectMapper.writeValueAsString(dashboardTO);
                        wsTemplate.convertAndSend(DASHBOARD_TOPIC, notificationAsString);
                    } catch (JsonProcessingException ex) {
                        logger.log(Level.SEVERE, ex.getMessage(), ex);
                    }
                } catch (Exception e) {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                    if (securityPolicyDAO.existsById(securityPolicy.getId())) {
                        securityPolicy.getComponentNodeInstances().forEach(componentNodeInstance -> {
                            if (componentNodeInstance.getSecurityPolicies().contains(securityPolicy)) {
                                componentNodeInstance.getSecurityPolicies().remove(securityPolicy);
                                componentNodeInstance.setLastModified(new Date());
                                componentNodeInstanceService.saveComponentNodeInstance(componentNodeInstance);
                            }
                        });
                        if (existingApplicationInstance.getSecurityPolicies().contains(securityPolicy)) {
                            existingApplicationInstance.getSecurityPolicies().remove(securityPolicy);
                            existingApplicationInstance.setLastModified(new Date());
                            applicationInstanceService.saveApplicationInstance(existingApplicationInstance);
                        }
                        securityPolicyDAO.delete(securityPolicy);
                    }
                    throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
                }
            } else {
                throw new GenericBusinessException(GenericMessage.REQUIRED_FIELDS_MISSING.getCode(), GenericMessage.REQUIRED_FIELDS_MISSING);
            }
        }
        //throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
    }

    public void deleteSecurityPolicyById(Long id, Long policyID, User authenticatedUser) {
        ApplicationInstance existingApplicationInstance = applicationInstanceService.fetchApplicationInstanceById(id);
        Optional<SecurityPolicy> existingSecurityPolicyOP = securityPolicyDAO.findById(policyID);
        if (existingApplicationInstance != null && existingSecurityPolicyOP.isPresent()
                && existingApplicationInstance.getStatus().equals(ApplicationInstance.ApplicationInstanceStatus.DEPLOYED.name())
                && existingApplicationInstance.getApplicationInstanceID()
                .equals(existingSecurityPolicyOP.get().getApplicationInstance().getApplicationInstanceID())
                && (existingApplicationInstance.hasEditAllowance(authenticatedUser))) {
            try {
                SecurityPolicy existingSecurityPolicy = existingSecurityPolicyOP.get();
                String command = "";
                command = SecurityPolicy.RuleType.valueOf(existingSecurityPolicy.getRule()).getFriendlyName();
                command += " --del --ip " + existingSecurityPolicy.getIpAddress();
                List<String> componentNodeHexIDs = new ArrayList<>();
                existingSecurityPolicy.getComponentNodeInstances().stream().forEach(componentNodeInstance -> {
                    if (!componentNodeHexIDs.contains(componentNodeInstance.getComponentNode().getHexID())) {
                        componentNodeHexIDs.add(componentNodeInstance.getComponentNode().getHexID());
                    }
                });
//                for (String componentNodeHexID : componentNodeHexIDs) {
//                    ActionsCommands actionsCommands = new ActionsCommands();
//                    actionsCommands.setCommand(command);
//                    String actionID = existingApplicationInstance.getApplication().getHexID() + ":" + existingApplicationInstance.getHexID()
//                            + ":" + componentNodeHexID + ":*";
//                    actionsCommands.setId(actionID);
//                    String actionsCommandsAsString = objectMapper.writeValueAsString(actionsCommands);
//                    kafkaSender.sendSecurityPolicy(actionsCommandsAsString);
//                }
                //TODO what happens if the objectMapper fails???
                existingSecurityPolicy.getComponentNodeInstances().forEach(componentNodeInstance -> {
                    if (componentNodeInstance.getSecurityPolicies().contains(existingSecurityPolicy)) {
                        componentNodeInstance.getSecurityPolicies().remove(existingSecurityPolicy);
                        componentNodeInstance.setLastModified(new Date());
                        componentNodeInstanceService.saveComponentNodeInstance(componentNodeInstance);
                    }
                });
                if (existingApplicationInstance.getSecurityPolicies().contains(existingSecurityPolicy)) {
                    existingApplicationInstance.getSecurityPolicies().remove(existingSecurityPolicy);
                    existingApplicationInstance.setLastModified(new Date());
                    applicationInstanceService.saveApplicationInstance(existingApplicationInstance);
                }
                existingSecurityPolicy.setComponentNodeInstances(new ArrayList<>());
                securityPolicyDAO.save(existingSecurityPolicy);
                securityPolicyDAO.delete(existingSecurityPolicy);
                try {
                    DashboardTO dashboardTO = new DashboardTO();
                    dashboardTO.setIpsSecurity(true);
                    String notificationAsString = null;
                    notificationAsString = objectMapper.writeValueAsString(dashboardTO);
                    wsTemplate.convertAndSend(DASHBOARD_TOPIC, notificationAsString);
                } catch (JsonProcessingException ex) {
                    logger.log(Level.SEVERE, ex.getMessage(), ex);
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
                throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
            }
        }
        //throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
    }
}
