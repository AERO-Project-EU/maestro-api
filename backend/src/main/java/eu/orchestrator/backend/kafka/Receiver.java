package eu.orchestrator.backend.kafka;

import eu.orchestrator.backend.transfer.ApplicationInstanceComponentsTO;
import eu.orchestrator.backend.transfer.ComponentIPTO;
import eu.orchestrator.backend.util.KubernetesUtil;
import eu.orchestrator.backend.util.Util;
import eu.orchestrator.repository.dao.ApplicationDAO;
import eu.orchestrator.repository.dao.ApplicationInstanceDAO;
import eu.orchestrator.repository.dao.ApplicationInstanceQuotaDAO;
import eu.orchestrator.repository.dao.ComponentNodeDAO;
import eu.orchestrator.repository.dao.ComponentNodeInstanceAlertDAO;
import eu.orchestrator.repository.dao.ComponentNodeInstanceDAO;
import eu.orchestrator.repository.dao.ComponentNodeInstanceHashDAO;
import eu.orchestrator.repository.dao.ComponentNodeInstanceIPDAO;
import eu.orchestrator.repository.dao.ComponentNodeInstanceStatusDAO;
import eu.orchestrator.repository.dao.ElasticityHistoryDAO;
import eu.orchestrator.repository.dao.IDRuleDAO;
import eu.orchestrator.repository.dao.IDRuleSetDAO;
import eu.orchestrator.repository.dao.IDRuleSetInstanceDAO;
import eu.orchestrator.repository.dao.NotificationDAO;
import eu.orchestrator.repository.dao.ProviderHistoryDAO;
import eu.orchestrator.repository.dao.SecurityConfigurationDAO;
import eu.orchestrator.repository.dao.SecurityConfigurationResultDAO;
import eu.orchestrator.repository.domain.Application;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.ApplicationInstance.ApplicationInstanceStatus;
import eu.orchestrator.repository.domain.ApplicationInstanceQuota;
import eu.orchestrator.repository.domain.ComponentNode;
import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.repository.domain.ComponentNodeInstance.SecurityEnablers;
import eu.orchestrator.repository.domain.ComponentNodeInstanceAlert;
import eu.orchestrator.repository.domain.ComponentNodeInstanceHash;
import eu.orchestrator.repository.domain.ComponentNodeInstanceIP;
import eu.orchestrator.repository.domain.ComponentNodeInstanceStatus;
import eu.orchestrator.repository.domain.ElasticityHistory;
import eu.orchestrator.repository.domain.IDRule;
import eu.orchestrator.repository.domain.IDRuleSet;
import eu.orchestrator.repository.domain.IDRuleSetInstance;
import eu.orchestrator.repository.domain.ProviderHistory;
import eu.orchestrator.repository.domain.SecurityConfiguration;
import eu.orchestrator.repository.domain.SecurityConfigurationResult.SecurityConfigurationResultStatus;
import eu.orchestrator.transfer.entities.agent.IDSAlert;
import eu.orchestrator.transfer.entities.agent.IDSConfiguration;
import eu.orchestrator.transfer.entities.backend.SecurityConfiguration.SecurityConfigurationType;
import eu.orchestrator.transfer.entities.backend.SecurityConfigurationResult;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorChangedStatusNotification;
import eu.orchestrator.transfer.entities.policyEngine.PolicyEngineActionType;
import eu.orchestrator.transfer.entities.policyEngine.PolicyEngineTrigger;
import eu.orchestrator.transfer.entities.policyEngine.TriggerActionModel;
import eu.orchestrator.backend.transfer.DashboardTO;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.client.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@Transactional
@ConditionalOnProperty(prefix = "kafka-flag", name = "enabled", havingValue = "on")
public class Receiver {

    static final String APPLICATION_INSTANCE_TOPIC = "/applicationinstance";
    private static final Logger logger = Logger.getLogger(Receiver.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String DASHBOARD_TOPIC = "/dashboard";
    private static final Long IDS_UPDATE_INTERVAL = Long.valueOf(5000); //in milliseconds
    @Autowired
    SimpMessagingTemplate wsTemplate;
    @Autowired
    NotificationDAO notificationDAO;
    @Autowired
    ApplicationInstanceDAO applicationInstanceDAO;
    @Autowired
    ApplicationDAO applicationDAO;
    @Autowired
    ComponentNodeDAO componentNodeDAO;
    @Autowired
    ApplicationInstanceQuotaDAO applicationInstanceQuotaDAO;
    @Autowired
    ComponentNodeInstanceDAO compponentNodeInstanceDAO;
    @Autowired
    ComponentNodeInstanceStatusDAO componentNodeInstanceStatusDAO;
    @Autowired
    ComponentNodeInstanceAlertDAO componentNodeInstanceAlertDAO;
    @Autowired
    ComponentNodeInstanceIPDAO componentNodeInstanceIPDAO;
    @Autowired
    SecurityConfigurationResultDAO securityConfigurationResultDAO;
    @Autowired
    IDRuleSetDAO idRuleSetDAO;
    @Autowired
    IDRuleDAO idRuleDAO;
    @Autowired
    IDRuleSetInstanceDAO idRuleSetInstanceDAO;
    @Autowired
    ElasticityHistoryDAO elasticityHistoryDAO;
    @Autowired
    ProviderHistoryDAO providerHistoryDAO;
    @Autowired
    ComponentNodeInstanceHashDAO componentNodeInstanceHashDAO;
    @Autowired
    ComponentNodeInstanceDAO componentNodeInstanceDAO;
    @Autowired
    Sender sender;
    private final AtomicReference<Long> previousTime = new AtomicReference<>(new Date().getTime());
    @Autowired
    private SecurityConfigurationDAO securityConfigurationDAO;
    @Value("${token.signer.secret}")
    private String tokenSecret;

//    @KafkaListener(topics = "${kafka.topic.reporting}")
//    public void listenForReports(@Payload String message) {
//        logger.info("Received message: '" + message + "'");
//
//        boolean notifyAgent = false;
//        AtomicReference<Boolean> updateElasticity = new AtomicReference<>(false);
//        Boolean updateSystemLogs = false;
//
//        try {
//
//            // Cast message to transfer object
//            OrchestratorChangedStatusNotification changedStatusNotification = objectMapper
//                    .readValue(message, OrchestratorChangedStatusNotification.class);
//
//            if (null != changedStatusNotification
//                    && null != OrchestratorChangedStatusNotification.ChangeType
//                    .valueOf(changedStatusNotification.getChangeType())) {
//
//                // Check change type
//                if (OrchestratorChangedStatusNotification.ChangeType
//                        .valueOf(changedStatusNotification.getChangeType()).name()
//                        .equals(OrchestratorChangedStatusNotification.ChangeType.AgentStatusChange.name())) {
//
//                    ApplicationInstance applicationInstance = applicationInstanceDAO.findById(Long.valueOf(changedStatusNotification.getGraphInstanceID()))
//                            .get();
//                    Application application = applicationDAO.findById(applicationInstance.getApplication().getId()).get();
//                    ComponentNodeInstance componentNodeInstance = compponentNodeInstanceDAO.findById(
//                            Long.valueOf(changedStatusNotification.getComponentNodeInstanceID())).get();
//                    ComponentNode componentNode = componentNodeDAO.findById(componentNodeInstance.getComponentNode().getComponentNodeID()).get();
//
//                    ComponentNodeInstanceStatus cniStatus = new ComponentNodeInstanceStatus();
//                    cniStatus.setComponentNodeInstance(componentNodeInstance);
//                    cniStatus.setDateCreated(new Date());
//                    cniStatus.setLastModified(new Date());
//                    cniStatus.setMessage(changedStatusNotification.getMessage());
//                    cniStatus.setApplicationInstance(applicationInstance);
//                    cniStatus.setReportedChange(changedStatusNotification.getReportedChange());
//
//                    String status = null;
//
//                    if (changedStatusNotification.getReportedChange().contains("ERROR")
//                            || changedStatusNotification.getReportedChange().contains("TERMINATED")) {
//                        status = "ERROR";
//                        updateSystemLogs = true;
//                    } else {
//
//                        // Handle IPs
//                        if (null != changedStatusNotification.getOrchestratorIPs()
//                                && !changedStatusNotification.getOrchestratorIPs().isEmpty()) {
//
//                            changedStatusNotification.getOrchestratorIPs().stream().forEach(orchestratorIP -> {
//                                if (null != orchestratorIP.getIp()) {
//                                    Optional<List<ComponentNodeInstanceIP>> existingIP =
//                                            componentNodeInstanceIPDAO.findAllByComponentNodeInstanceAndIp(componentNodeInstance, orchestratorIP.getIp());
//
//                                    //Check if the IP already exists
//                                    if (!existingIP.isPresent()) {
//                                        ComponentNodeInstanceIP cniIP = new ComponentNodeInstanceIP();
//                                        cniIP.setComponentNodeInstance(componentNodeInstance);
//                                        cniIP.setApplicationInstance(applicationInstance);
//                                        cniIP.setDateCreated(new Date());
//                                        cniIP.setLastModified(new Date());
//                                        cniIP.setGraphInstanceID(applicationInstance.getApplicationInstanceID() + "");
//                                        cniIP.setIp(null != orchestratorIP.getIp() ? orchestratorIP.getIp() : null);
//                                        cniIP.setNetwork(
//                                                null != orchestratorIP.getNetwork() ? orchestratorIP.getNetwork() : null);
//                                        cniIP.setType(
//                                                null != orchestratorIP.getInterfaceType() ? orchestratorIP.getInterfaceType()
//                                                        : null);
//                                        componentNodeInstanceIPDAO.save(cniIP);
//                                    }
//                                }
//                            });
//                        }
//
//                        if (changedStatusNotification.getReportedChange().contains("STARTED")
//                                || changedStatusNotification.getReportedChange().contains("UP")) {
//
//                            if (changedStatusNotification.getReportedChange().contains("UP")) {
//                                notifyAgent = true;
//
//                                //Check if the component is scalable in order to add elasticity history
//                                if (!componentNodeInstance.getComponentNode().getComponent().getElasticityController().equals("NONE")) {
//                                    Optional<List<ElasticityHistory>> elasticityHistoryOp = elasticityHistoryDAO.
//                                            findByApplicationInstanceAndAndComponentNodeOrderByIdDesc(applicationInstance,
//                                                    componentNodeInstance.getComponentNode());
//
//                                    if (null != elasticityHistoryOp && elasticityHistoryOp.isPresent()) {
//
//                                        ElasticityHistory elasticityHistory = elasticityHistoryOp.get().get(0);
//                                        elasticityHistory.setActiveWorkers(elasticityHistory.getActiveWorkers() + 1);
//
//                                        if (elasticityHistory.getActiveWorkers() == elasticityHistory.getWorkersCount()) {
//                                            elasticityHistory.setStatus(ElasticityHistory.ElasticityStatus.NEUTRAL.name());
//                                        }
//
//                                        elasticityHistoryDAO.save(elasticityHistory);
//                                        updateElasticity.set(true);
//                                    }
//                                }
//                            }
//
//                            updateSystemLogs = true;
//                            status = "SUCCESS";
//                        } else {
//                            status = "INFO";
//                        }
//                    }
//                    changedStatusNotification.setStatus(status);
//                    changedStatusNotification.setDateCreated(new Date());
//                    changedStatusNotification.setLoadBalancedBy(
//                            null != cniStatus.getComponentNodeInstance().getLoadBalancedBy() ?
//                                    cniStatus.getComponentNodeInstance().getLoadBalancedBy()
//                                            .getComponentNodeInstanceID() + "" : null);
//                    cniStatus.setStatus(status);
//
//                    componentNodeInstanceStatusDAO.save(cniStatus);
//
//                    // Send WS message to UI
//                    wsTemplate.convertAndSend(APPLICATION_INSTANCE_TOPIC,
//                            objectMapper.writeValueAsString(changedStatusNotification));
//
//                    List<IDRuleSetInstance> idRuleSetInstances = idRuleSetInstanceDAO
//                            .findAllByComponentNodeInstanceOrderByDateCreatedDesc(componentNodeInstance, null)
//                            .getContent();
//
//                    if (notifyAgent && null != idRuleSetInstances && !idRuleSetInstances.isEmpty()
//                            && !componentNodeInstance.getLoadBalancer().booleanValue()) {
//
//                        IDSConfiguration idsConfigurationMSG = new IDSConfiguration();
//                        idsConfigurationMSG.setGraphInstanceHexId(applicationInstance.getHexID());
//                        idsConfigurationMSG.setGraphHexId(application.getHexID());
//                        idsConfigurationMSG.setComponentNodeInstanceHexId(componentNodeInstance.getHexID());
//                        idsConfigurationMSG.setComponentNodeHexId(componentNode.getHexID());
//
//                        List<String> rules = new ArrayList<>();
//
//                        idRuleSetInstances.stream().forEach(idRuleSetInstance -> {
//
//                            IDRuleSet idRuleSet = idRuleSetDAO
//                                    .findById(idRuleSetInstance.getIdRuleSet().getId()).get();
//
//                            List<IDRule> idRules = idRuleDAO.findAllByIdRuleSetOrderByDateCreated(idRuleSet, null)
//                                    .getContent();
//
//                            if (null != idRules && !idRules.isEmpty()) {
//
//                                idRules.stream().forEach(idRule -> {
//
//                                    String rule = idRule.getName();
//
//                                    if (!rules.contains(rule)) {
//                                        rules.add(rule);
//                                    }
//
//                                });
//
//                            }
//
//                        });
//
//                        idsConfigurationMSG.setRules(rules);
//
//                        sender.sendIDRulesToAgent(objectMapper.writeValueAsString(idsConfigurationMSG));
//                    }
//
//                    if (updateSystemLogs || updateElasticity.get()) {
//                        try {
//                            DashboardTO dashboardTO = new DashboardTO();
//                            dashboardTO.setSystemLogs(updateSystemLogs);
//                            dashboardTO.setElasticity(updateElasticity.get());
//                            String notificationAsString = null;
//                            notificationAsString = objectMapper.writeValueAsString(dashboardTO);
//                            wsTemplate.convertAndSend(DASHBOARD_TOPIC, notificationAsString);
//                        } catch (JsonProcessingException e) {
//                            e.printStackTrace();
//                        }
//                    }
//
//                } else if (OrchestratorChangedStatusNotification.ChangeType
//                        .valueOf(changedStatusNotification.getChangeType()).name()
//                        .equals(OrchestratorChangedStatusNotification.ChangeType.GraphStatusChange.name())) {
//                    if (null != changedStatusNotification.getGraphInstanceID() && !changedStatusNotification
//                            .getGraphInstanceID().isEmpty()) {
//
//                        if (applicationInstanceDAO
//                                .findById(Long.valueOf(changedStatusNotification.getGraphInstanceID()))
//                                .isPresent()) {
//                            ApplicationInstance existingAppInstance = applicationInstanceDAO
//                                    .findById(Long.valueOf(changedStatusNotification.getGraphInstanceID())).get();
//
//                            existingAppInstance.setStatus(changedStatusNotification.getReportedChange());
//                            Boolean deployed = false;
//                            if (existingAppInstance.getStatus().compareTo(ApplicationInstanceStatus.DEPLOYED.name()) == 0) {
//                                existingAppInstance.setDateDeployed(new Date());
//                                deployed = true;
//
//                                //TODO astrid send on agent message for a configuration_integrity_validation
//                                List<SecurityConfiguration> securityConfigurationList = securityConfigurationDAO.findAllByApplicationInstance(
//                                        existingAppInstance);
//
//                                securityConfigurationList.stream().filter(securityConfiguration -> {
//                                    boolean flag = securityConfiguration.getSecurityConfigurationType()
//                                            .compareTo(SecurityEnablers.CONFIGURATION_INTEGRITY_VERIFICATION.name()) == 0;
//                                    return flag;
//                                }).forEach(securityConfiguration -> {
//                                    List<eu.orchestrator.repository.domain.SecurityConfigurationResult> securityConfigurationResultList = securityConfigurationResultDAO
//                                            .findAllBySecurityConfiguration(securityConfiguration);
//
//                                    securityConfigurationResultList.stream().forEach(securityConfigurationResult -> {
//
//                                        eu.orchestrator.transfer.entities.backend.SecurityConfiguration securityConfigurationAgent = new eu.orchestrator.transfer.entities.backend.SecurityConfiguration();
//                                        securityConfigurationAgent.setGraphHexId(securityConfiguration.getApplicationInstance().getApplication().getHexID());
//                                        securityConfigurationAgent.setGraphInstanceHexId(securityConfiguration.getApplicationInstance().getHexID());
//                                        securityConfigurationAgent.setComponentNodeHexId(
//                                                securityConfigurationResult.getComponentNodeInstance().getComponentNode().getHexID());
//                                        securityConfigurationAgent.setComponentNodeInstanceHexId(
//                                                securityConfigurationResult.getComponentNodeInstance().getHexID());
//                                        securityConfigurationAgent.setSecurityConfigurationType(SecurityConfigurationType.CONFIGURATION_INTEGRITY_VERIFICATION);
//                                        securityConfigurationAgent.setResultHexId(securityConfigurationResult.getHexID());
//
//                                        try {
//                                            sender.sendSecurityConfigurationToAgent(objectMapper.writeValueAsString(securityConfigurationAgent));
//                                        } catch (JsonProcessingException e) {
//                                            e.printStackTrace();
//                                        }
//
//                                    });
//                                });
//
//                                //TODO Spider
//                                ApplicationInstanceComponentsTO applicationInstanceComponentsTO = new ApplicationInstanceComponentsTO();
//                                applicationInstanceComponentsTO.setId(existingAppInstance.getApplicationInstanceID());
//                                applicationInstanceComponentsTO.setName(existingAppInstance.getName());
//                                List<ComponentIPTO> componentList = new ArrayList();
//                                for (ComponentNodeInstance componentNodeInstance : existingAppInstance.getComponentNodeInstances()) {
//                                    SortedSet<ComponentNodeInstanceIP> cniIPs = componentNodeInstance.getComponentNodeInstanceIPs();
//                                    for (ComponentNodeInstanceIP cniIP : cniIPs) {
//                                        if (cniIP.getType() != null && cniIP.getType().equals("ACCESS")) {
//                                            ComponentIPTO component = new ComponentIPTO();
//                                            component.setIp(cniIP.getIp());
//                                            component.setName(cniIP.getComponentNodeInstance().getName());
//                                            componentList.add(component);
//                                        }
//                                    }
//                                }
//                                applicationInstanceComponentsTO.setComponents(componentList);
//                                sender.sendApplicationInstanceIPs(objectMapper.writeValueAsString(applicationInstanceComponentsTO));
//                            }
//
//                            existingAppInstance.setDeploymentTimestamp(Instant.now().toEpochMilli());
//                            existingAppInstance.setLastModified(new Date());
//                            applicationInstanceDAO.save(existingAppInstance);
//
//                            if (deployed) {
//                                try {
//                                    DashboardTO dashboardTO = new DashboardTO();
//                                    dashboardTO.setOverview(true);
//                                    String notificationAsString = null;
//                                    notificationAsString = objectMapper.writeValueAsString(dashboardTO);
//                                    wsTemplate.convertAndSend(DASHBOARD_TOPIC, notificationAsString);
//                                } catch (JsonProcessingException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//
//                            logger.info("Status of application instance : " + existingAppInstance.getName()
//                                    + " has been updated!");
//
//                        }
//
//                    }
//
//                } else if (OrchestratorChangedStatusNotification.ChangeType
//                        .valueOf(changedStatusNotification.getChangeType()).name()
//                        .equals(OrchestratorChangedStatusNotification.ChangeType.QuotasChange.name())) {
//
//                    if (null != changedStatusNotification.getGraphInstanceID() && !changedStatusNotification
//                            .getGraphInstanceID().isEmpty()) {
//
//                        logger.info(
//                                "Fetching app instance with ID: " + changedStatusNotification.getGraphInstanceID()
//                                        + "!");
//
//                        if (applicationInstanceDAO
//                                .findById(Long.valueOf(changedStatusNotification.getGraphInstanceID()))
//                                .isPresent()) {
//
//                            ApplicationInstance existingAppInstance = applicationInstanceDAO
//                                    .findById(Long.valueOf(changedStatusNotification.getGraphInstanceID())).get();
//
//                            if (null != existingAppInstance) {
//
//                                // Fetch Quotas if exist
//                                ApplicationInstanceQuota quota;
//                                ProviderHistory providerHistory;
//                                Boolean newQuota = false;
//
//                                if (applicationInstanceQuotaDAO.findByApplicationInstance(existingAppInstance)
//                                        .isPresent()) {
//
//                                    // Existing
//                                    quota = applicationInstanceQuotaDAO.findByApplicationInstance(existingAppInstance)
//                                            .get();
//
//                                } else {
//
//                                    // New Quota
//                                    quota = new ApplicationInstanceQuota();
//                                    quota.setDateCreated(new Date());
//                                    newQuota = true;
//                                }
//
//                                Optional<ProviderHistory> providerHistoryOp = providerHistoryDAO
//                                        .findTopByOrderByDateCreatedDesc();
//                                if (null != providerHistoryOp && providerHistoryOp.isPresent()) {
//                                    providerHistory = providerHistoryOp.get();
//                                } else {
//                                    providerHistory = new ProviderHistory();
//                                    providerHistory.setMemory((long) 0);
//                                    providerHistory.setvCPUs((long) 0);
//                                }
//
//                                Long vCPUs = providerHistory.getvCPUs();
//                                Long memory = providerHistory.getMemory();
//                                if (newQuota) {
//                                    vCPUs = vCPUs + Long.valueOf(changedStatusNotification.getvCPUs());
//                                    memory = memory + Long.valueOf(changedStatusNotification.getMemory());
//                                } else {
//                                    vCPUs = vCPUs
//                                            + Long.valueOf(changedStatusNotification.getvCPUs())
//                                            - Long.valueOf(quota.getVirtualCPUs());
//                                    memory = memory
//                                            + Long.valueOf(changedStatusNotification.getMemory())
//                                            - Long.valueOf(quota.getMemory());
//                                }
//
//                                quota.setApplicationInstance(existingAppInstance);
//
//                                quota.setMemory(changedStatusNotification.getMemory());
//                                quota.setStorage(changedStatusNotification.getStorage());
//                                quota.setVirtualCPUs(changedStatusNotification.getvCPUs());
//
//                                quota.setLastModified(new Date());
//                                applicationInstanceQuotaDAO.save(quota);
//
//                                ProviderHistory newProviderHistory = new ProviderHistory();
//                                newProviderHistory.setvCPUs(vCPUs);
//                                newProviderHistory.setMemory(memory);
//                                newProviderHistory.setDateCreated(new Date());
//                                providerHistoryDAO.save(newProviderHistory);
//
//                                try {
//                                    DashboardTO dashboardTO = new DashboardTO();
//                                    dashboardTO.setProviderHistory(true);
//                                    String notificationAsString = null;
//                                    notificationAsString = objectMapper.writeValueAsString(dashboardTO);
//                                    wsTemplate.convertAndSend(DASHBOARD_TOPIC, notificationAsString);
//                                } catch (JsonProcessingException e) {
//                                    e.printStackTrace();
//                                }
//
//                                logger.info("Quotas for application instance : " + existingAppInstance.getName()
//                                        + " have been updated!");
//
//                            } else {
//                                logger.info("App instance with ID " + changedStatusNotification.getGraphInstanceID()
//                                        + " cannot be found!");
//                            }
//
//                        }
//
//                    }
//
//                }
//
//            }
//
//        } catch (Exception e) {
//            logger.log(Level.SEVERE, e.getMessage(), e);
//            e.printStackTrace();
//        }
//    }
//
//    @KafkaListener(topics = "${kafka.topic.agent-ids-alerting}")
//    public void listenForAlerts(@Payload String message) {
//        logger.info("Received message: '" + message + "'");
//
//        try {
//
//            // Cast message to transfer object
//            IDSAlert idsAlertNotification = objectMapper.readValue(message, IDSAlert.class);
//
//            if (null != idsAlertNotification) {
//
//                if (applicationInstanceDAO.findByHexID(idsAlertNotification.getGraphInstanceHexId())
//                        .isPresent()) {
//
//                    ApplicationInstance existingApplicationInstance = applicationInstanceDAO
//                            .findByHexID(idsAlertNotification.getGraphInstanceHexId()).get();
//
//                    if (existingApplicationInstance.getStatus()
//                            .equals(ApplicationInstance.ApplicationInstanceStatus.DEPLOYED.name())) {
//
//                        if (compponentNodeInstanceDAO
//                                .findByHexID(idsAlertNotification.getComponentNodeInstanceHexId()).isPresent()) {
//
//                            ComponentNodeInstance componentNodeInstance = compponentNodeInstanceDAO
//                                    .findByHexID(idsAlertNotification.getComponentNodeInstanceHexId()).get();
//
//                            if (componentNodeInstance.getApplicationInstance().getApplicationInstanceID()
//                                    .equals(existingApplicationInstance.getApplicationInstanceID())) {
//
//                                // Store alert and send it to UI with Push Notification
//                                ComponentNodeInstanceAlert componentNodeInstanceAlert = new ComponentNodeInstanceAlert();
//                                componentNodeInstanceAlert.setApplicationInstance(existingApplicationInstance);
//                                componentNodeInstanceAlert.setComponentNodeInstance(componentNodeInstance);
//                                componentNodeInstanceAlert
//                                        .setComponentNodeInstanceName(componentNodeInstance.getName());
//                                componentNodeInstanceAlert.setMessage(idsAlertNotification.getAlert());
//                                componentNodeInstanceAlert.setDateCreated(new Date());
//                                componentNodeInstanceAlert.setLastModified(new Date());
//                                componentNodeInstanceAlert.setStatus("ALERT");
//                                componentNodeInstanceAlertDAO.save(componentNodeInstanceAlert);
//
//                                componentNodeInstanceAlert.setComponentNodeInstance(null);
//                                componentNodeInstanceAlert.setApplicationInstance(null);
//                                componentNodeInstanceAlert.setGraphInstanceID(
//                                        existingApplicationInstance.getApplicationInstanceID() + "");
//                                componentNodeInstanceAlert.setComponentNodeInstanceAlertID(null);
//
//                                // Send WS message to UI
//                                wsTemplate.convertAndSend(APPLICATION_INSTANCE_TOPIC,
//                                        objectMapper.writeValueAsString(componentNodeInstanceAlert));
//
//                                Long currentTime = new Date().getTime();
//                                if ((currentTime - previousTime.get()) > IDS_UPDATE_INTERVAL) {
//                                    previousTime.set(currentTime);
//                                    try {
//                                        DashboardTO dashboardTO = new DashboardTO();
//                                        dashboardTO.setIdsSecurity(true);
//                                        dashboardTO.setAlertLogs(true);
//                                        String notificationAsString = null;
//                                        notificationAsString = objectMapper.writeValueAsString(dashboardTO);
//                                        wsTemplate.convertAndSend(DASHBOARD_TOPIC, notificationAsString);
//                                    } catch (JsonProcessingException e) {
//                                        e.printStackTrace();
//                                    }
//                                }
//
//                            }
//
//                        }
//
//
//                    }
//
//
//                }
//
//
//            }
//
//        } catch (Exception e) {
//            logger.log(Level.SEVERE, e.getMessage(), e);
//            e.printStackTrace();
//        }
//
//
//    }
//
//    @KafkaListener(topics = "${kafka.topic.policy-engine-info-msgs}")
//    public void listenForMsgsFromPolicyEngine(@Payload String message) {
//        logger.info("Received message: '" + message + "'");
//
//        try {
//
//            // Cast message to transfer object
//            TriggerActionModel policyEngineMsgNotification = objectMapper
//                    .readValue(message, TriggerActionModel.class);
//
//            if (null != policyEngineMsgNotification) {
//
//                if (applicationInstanceDAO.findByHexID(policyEngineMsgNotification.getGraphInstanceHexID())
//                        .isPresent()) {
//
//                    ApplicationInstance existingApplicationInstance = applicationInstanceDAO
//                            .findByHexID(policyEngineMsgNotification.getGraphInstanceHexID()).get();
//
//                    if (existingApplicationInstance.getStatus()
//                            .equals(ApplicationInstance.ApplicationInstanceStatus.DEPLOYED.name())) {
//
//                        List<ComponentNodeInstance> componentNodeInstances = compponentNodeInstanceDAO
//                                .findAllByApplicationInstance(existingApplicationInstance, null).getContent();
//
//                        if (null != componentNodeInstances && !componentNodeInstances.isEmpty()) {
//
//                            ComponentNodeInstance componentNodeInstance = null != componentNodeInstances.stream()
//                                    .filter(cni -> cni.getComponentNode().getHexID()
//                                            .equals(policyEngineMsgNotification.getComponentNodeHexID())).collect(
//                                            Collectors.toList()) && !componentNodeInstances.stream().filter(
//                                    cni -> cni.getComponentNode().getHexID()
//                                            .equals(policyEngineMsgNotification.getComponentNodeHexID())).collect(
//                                    Collectors.toList()).isEmpty() ? componentNodeInstances.stream().filter(
//                                    cni -> cni.getComponentNode().getHexID()
//                                            .equals(policyEngineMsgNotification.getComponentNodeHexID())).collect(
//                                    Collectors.toList()).get(0) : null;
//
//                            if (null != componentNodeInstance) {
//
//                                if (componentNodeInstance.getApplicationInstance().getApplicationInstanceID()
//                                        .equals(existingApplicationInstance.getApplicationInstanceID())) {
//
//                                    // Store alert and send it to UI with Push Notification
//                                    ComponentNodeInstanceAlert componentNodeInstanceAlert = new ComponentNodeInstanceAlert();
//                                    componentNodeInstanceAlert.setApplicationInstance(existingApplicationInstance);
//                                    componentNodeInstanceAlert.setComponentNodeInstance(componentNodeInstance);
//                                    componentNodeInstanceAlert
//                                            .setComponentNodeInstanceName(componentNodeInstance.getName());
//                                    componentNodeInstanceAlert.setMessage(policyEngineMsgNotification.getContext());
//                                    componentNodeInstanceAlert.setDateCreated(new Date());
//                                    componentNodeInstanceAlert.setLastModified(new Date());
//                                    componentNodeInstanceAlert.setStatus("MSG");
//                                    componentNodeInstanceAlertDAO.save(componentNodeInstanceAlert);
//
//                                    componentNodeInstanceAlert.setComponentNodeInstance(null);
//                                    componentNodeInstanceAlert.setApplicationInstance(null);
//                                    componentNodeInstanceAlert.setGraphInstanceID(
//                                            existingApplicationInstance.getApplicationInstanceID() + "");
//                                    componentNodeInstanceAlert.setComponentNodeInstanceAlertID(null);
//
//                                    // Send WS message to UI
//                                    wsTemplate.convertAndSend(APPLICATION_INSTANCE_TOPIC,
//                                            objectMapper.writeValueAsString(componentNodeInstanceAlert));
//
//                                }
//
//                            }
//
//                        }
//
//                    }
//
//                }
//
//            }
//
//        } catch (Exception e) {
//            logger.log(Level.SEVERE, e.getMessage(), e);
//            e.printStackTrace();
//        }
//
//    }
//
//    @KafkaListener(topics = "${kafka.topic.agent-security-configuration-result}")
//    public void listenForAgentSecurityResults(@Payload String message) {
//        logger.info("Received message: '" + message + "'");
//
//        // Cast message to transfer object
//        try {
//
//            String decryptMessage = Util.decrypt(message, tokenSecret);
//
//            eu.orchestrator.transfer.entities.backend.SecurityConfiguration securityConfigurationFromAgent =
//                    objectMapper.readValue(decryptMessage, eu.orchestrator.transfer.entities.backend.SecurityConfiguration.class);
//
//            //TODO
//            //check the certificate
//            if (securityConfigurationFromAgent.getCertificate().compareTo("1111") == 0) {
//
//            }
//
//            // check SecurityConfigurationType
//            if (securityConfigurationFromAgent.getSecurityConfigurationType().name()
//                    .compareTo(SecurityConfigurationType.CONFIGURATION_INTEGRITY_VERIFICATION.name()) == 0
//                    || securityConfigurationFromAgent.getSecurityConfigurationType().name().compareTo(SecurityConfigurationType.RUNTIME_FILE_INTEGRITY.name())
//                    == 0) {
//
//                Optional<eu.orchestrator.repository.domain.SecurityConfigurationResult> securityConfigurationResultOptional = securityConfigurationResultDAO
//                        .findByHexID(securityConfigurationFromAgent.getResultHexId());
//
//                // check if SecurityConfigurationResult exist
//                if (securityConfigurationResultOptional.isPresent()) {
//                    eu.orchestrator.repository.domain.SecurityConfigurationResult securityConfigurationResultDomain = securityConfigurationResultOptional.get();
//
//                    String description = null == securityConfigurationResultDomain.getDescription() ? "" : securityConfigurationResultDomain.getDescription();
//
//                    for (SecurityConfigurationResult securityConfigurationResult : securityConfigurationFromAgent.getSecurityConfigurationResultList()) {
//                        // check if hash
//                        if (null != securityConfigurationResult.getHashType()) {
//                            Optional<ComponentNodeInstanceHash> componentNodeInstanceHashOptional = componentNodeInstanceHashDAO
//                                    .findByComponentNodeInstanceAndType(securityConfigurationResultDomain.getComponentNodeInstance(),
//                                            securityConfigurationResult.getHashType().name());
//
//                            if (componentNodeInstanceHashOptional.isPresent()) {
//                                ComponentNodeInstanceHash componentNodeInstanceHash = componentNodeInstanceHashOptional.get();
//                                String agentValue = securityConfigurationResult.getValue();
//                                String originalValue = Util.decrypt(componentNodeInstanceHash.getValue(), tokenSecret);
//
//                                if (agentValue.compareTo(originalValue) == 0) {
//                                    description = description + "\n" + securityConfigurationResult.getHashType().name() + " (OK)";
//                                } else {
//                                    description = description + "\n" + securityConfigurationResult.getHashType().name() + " (FAILED)";
//                                }
//                            } else {
//                                description = description + "\n" + securityConfigurationResult.getHashType().name() + " (NOT FOUND)";
//                            }
//                        }
//                        // Error Caused by: java.lang.NullPointerException: null
//                        // 	at eu.orchestrator.repository.domain.ComponentNodeInstance.compareTo(ComponentNodeInstance.java:258)
//                        //	at eu.orchestrator.repository.domain.ComponentNodeInstance.compareTo(ComponentNodeInstance.java:30)
//                        // Try also custom update as follows but it didn't work
//                        //  @Modifying
//                        //  @Query("update SecurityConfigurationResult securityConfigurationResult set securityConfigurationResult.status = :status where securityConfigurationResult.id = :id")
//                        //  int updateStatus(@Param("status") String status, @Param("id") Long id);
//
//                        // Resolved with delete securityConfigurationResult before save
//                    }
//                    securityConfigurationResultDomain.setDescription(description);
//                    securityConfigurationResultDomain.setLastModified(new Date());
//                    securityConfigurationResultDomain.setStatus(SecurityConfigurationResultStatus.COMPLETE.name());
//                    securityConfigurationResultDAO.deleteById(securityConfigurationResultDomain.getId());
//                    securityConfigurationResultDAO.save(securityConfigurationResultDomain);
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    @KafkaListener(topics = "${kafka.topic.orchestrator-security-configuration-results}")
//    public void listenForOrchestratorSecurityResults(@Payload String message) {
//        logger.info("Received message: '" + message + "'");
//
//        // Cast message to transfer object
//        try {
//            String decryptMessage = Util.decrypt(message, tokenSecret);
//
//            eu.orchestrator.transfer.entities.backend.SecurityConfiguration securityConfigurationFromOrchestrator =
//                    objectMapper.readValue(decryptMessage, eu.orchestrator.transfer.entities.backend.SecurityConfiguration.class);
//
//            Optional<ComponentNodeInstance> componentNodeInstanceOptional = componentNodeInstanceDAO
//                    .findByHexID(securityConfigurationFromOrchestrator.getComponentNodeInstanceHexId());
//
//            if (componentNodeInstanceOptional.isPresent()) {
//                ComponentNodeInstance componentNodeInstance = componentNodeInstanceOptional.get();
//
//                Long totalHashesForComponentNodeInstance = componentNodeInstanceHashDAO.countByComponentNodeInstance(componentNodeInstance);
//
//                // Check if componentNodeInstance has 4 hashes (image, credentials, ports, envs)
//                if (totalHashesForComponentNodeInstance < 4) {
//                    for (SecurityConfigurationResult securityConfigurationResult : securityConfigurationFromOrchestrator.getSecurityConfigurationResultList()) {
//                        ComponentNodeInstanceHash componentNodeInstanceHash = new ComponentNodeInstanceHash();
//                        componentNodeInstanceHash.setType(securityConfigurationResult.getHashType().name());
//                        componentNodeInstanceHash.setValue(Util.encrypt(securityConfigurationResult.getValue(), tokenSecret));
//                        componentNodeInstanceHash.setComponentNodeInstance(componentNodeInstance);
//                        componentNodeInstanceHash.setDateCreated(new Date());
//                        componentNodeInstanceHash.setLastModified(new Date());
//                        componentNodeInstanceHashDAO.save(componentNodeInstanceHash);
//                    }
//                }
//
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    @KafkaListener(topics = "${kafka.topic.policy-engine-actions}")
//    public void listenForPolicyEngineActions(@Payload String message) {
//        logger.log(Level.INFO,"{0} -> Policy Engine Actions Kafka Listener: Received Policy Engine Action Message: {1}",
//                new Object[]{new Date(), message});
//        try {
//            ApplicationInstance applicationInstance;
//            ComponentNodeInstance componentNodeInstance;
//
//            // Cast message to PolicyEngineTrigger transfer object
//            PolicyEngineTrigger policyEngineTrigger = objectMapper
//                    .readValue(message, PolicyEngineTrigger.class);
//
//            // Fetch ApplicationInstance by applicationInstanceHexID
//            Optional<ApplicationInstance> applicationInstanceOptional = applicationInstanceDAO.
//                    findByHexID(policyEngineTrigger.getApplicationInstanceHexId());
//
//            if (applicationInstanceOptional.isPresent()) {
//                applicationInstance = applicationInstanceOptional.get();
//
//                if(!applicationInstance.getStatus().equals(ApplicationInstance.ApplicationInstanceStatus.DEPLOYED.name())){
//                    logger.log(Level.WARNING,"{0} -> Policy Engine Actions Kafka Listener: ApplicationInstance " +
//                                    "with ID: {1} is not DEPLOYED. Scaling failed.",
//                            new Object[]{new Date(), policyEngineTrigger.getApplicationInstanceHexId()});
//                    return;
//                }
//            } else {
//                logger.log(Level.WARNING,"{0} -> Policy Engine Actions Kafka Listener: Could not find ApplicationInstance " +
//                                "with ID: {1}",
//                        new Object[]{new Date(), policyEngineTrigger.getApplicationInstanceHexId()});
//                return;
//            }
//
//            // Fetch ComponentNodeInstance by componentNodeInstanceHexID
//            Optional<ComponentNodeInstance> componentNodeInstanceOptional =
//                    compponentNodeInstanceDAO.findByHexID(policyEngineTrigger.getAction().getComponentNodeInstanceHexID());
//
//            if (componentNodeInstanceOptional.isPresent()) {
//                componentNodeInstance = componentNodeInstanceOptional.get();
//            } else {
//                logger.log(Level.WARNING,"{0} -> Policy Engine Actions Kafka Listener: Could not find ComponentNodeInstance " +
//                                "with ID: {1}",
//                        new Object[]{new Date(), policyEngineTrigger.getAction().getComponentNodeInstanceHexID()});
//                return;
//            }
//
//            logger.log(Level.INFO,"{0} -> Policy Engine Actions Kafka Listener: Found ComponentNodeInstance: {1}",
//                    new Object[]{new Date(), componentNodeInstance});
//
//            // Parameters
//            Config config = KubernetesUtil.configProvider(applicationInstance);
//            int minimumWorkers = componentNodeInstance.getMinimumWorkers();
//            int maximumWorkers = componentNodeInstance.getMaximumWorkers();
//            String deploymentName = KubernetesUtil.componentInstanceNameProvider(componentNodeInstance) + "-deployment";
//            String namespace = KubernetesUtil.namespaceProvider(applicationInstance);
//            int currentReplicasState = KubernetesUtil.fetchReplicasFromK8sDeployment(config, namespace, deploymentName);
//            int desiredReplicasState = 0;
//
//            if (policyEngineTrigger.getAction().getType().equals(PolicyEngineActionType.SCALE_OUT)) {
//                int tmpScaleOutDesiredReplicasState = policyEngineTrigger.getAction().getWorkersNumber() + currentReplicasState;
//                desiredReplicasState = Math.min(tmpScaleOutDesiredReplicasState, maximumWorkers);
//            } else if (policyEngineTrigger.getAction().getType().equals(PolicyEngineActionType.SCALE_IN)) {
//                    int tmpScaleInDesiredReplicasState = Math.abs(policyEngineTrigger.getAction().getWorkersNumber() - currentReplicasState);
//                    desiredReplicasState = Math.max(tmpScaleInDesiredReplicasState, minimumWorkers);
//            }
//
//            logger.log(Level.INFO,"{0} -> Policy Engine Actions Kafka Listener: ComponentNodeInstanceHexID: {1} " +
//                            "- Minimum Workers: {2} " +
//                            "- Maximum Workers: {3} " +
//                            "- Scale Type: {4} " +
//                            "- Current Replicas: {5} " +
//                            "- Replicas Requested: {6} " +
//                            "- Desired Replicas: {7}",
//                    new Object[]{new Date(), componentNodeInstance.getHexID(), minimumWorkers, maximumWorkers,
//                    policyEngineTrigger.getAction().getType().getAction(), currentReplicasState,
//                            policyEngineTrigger.getAction().getWorkersNumber(), desiredReplicasState});
//
//            // Scale K8s Deployment
//            KubernetesUtil.scaleK8sDeployment(config, namespace, deploymentName, desiredReplicasState);
//
//            logger.log(Level.INFO,"{0} -> Policy Engine Actions Kafka Listener: ComponentNodeInstance with ID {1} and " +
//                            " K8s deploymentName {2} in namespace {3} scaled successfully.",
//                    new Object[]{new Date(), componentNodeInstance.getHexID(), deploymentName, namespace});
//        } catch (Exception e) {
//            logger.log(Level.SEVERE, e.getMessage(), e);
//            e.printStackTrace();
//        }
//    }

}
