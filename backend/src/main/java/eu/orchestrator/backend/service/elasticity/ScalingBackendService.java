package eu.orchestrator.backend.service.elasticity;

import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.backend.kafka.Sender;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.transfer.DashboardTO;
import eu.orchestrator.backend.util.Util;
import eu.orchestrator.common.util.NullCheckUtil;
import eu.orchestrator.elasticity.spi.adapter.ElasticityFrameworkBackend;
import eu.orchestrator.elasticity.spi.model.backend.ScalingObjects;
import eu.orchestrator.repository.dao.ComponentNodeInstanceStatusDAO;
import eu.orchestrator.repository.dao.ElasticityHistoryDAO;
import eu.orchestrator.repository.dao.SecurityConfigurationDAO;
import eu.orchestrator.repository.dao.SecurityConfigurationResultDAO;
import eu.orchestrator.repository.domain.Application;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.repository.domain.ComponentNodeInstance.SecurityEnablers;
import eu.orchestrator.repository.domain.ComponentNodeInstanceStatus;
import eu.orchestrator.repository.domain.ElasticityHistory;
import eu.orchestrator.repository.domain.SecurityConfiguration;
import eu.orchestrator.repository.domain.SecurityConfigurationResult;
import eu.orchestrator.repository.domain.SecurityConfigurationResult.SecurityConfigurationResultStatus;
import eu.orchestrator.transfer.entities.backend.SecurityConfiguration.SecurityConfigurationType;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorApplicationInstance;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorChangedStatusNotification;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorScalingRequest;
import eu.orchestrator.backend.service.application.ApplicationService;
import eu.orchestrator.backend.service.applicationinstance.ApplicationInstanceService;
import eu.orchestrator.backend.service.applicationinstance.ComponentNodeInstanceService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import jakarta.transaction.Transactional;

@Service
@Transactional(rollbackOn = Exception.class)
public class ScalingBackendService {

    private static final Logger logger = Logger.getLogger(ScalingBackendService.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String APPLICATION_INSTANCE_TOPIC = "/applicationinstance";
    private static final String DASHBOARD_TOPIC = "/dashboard";

    @Autowired
    private ApplicationInstanceService applicationInstanceService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ComponentNodeInstanceService componentNodeInstanceService;

    @Autowired
    private ElasticityService elasticityService;

    @Autowired
    private SecurityConfigurationDAO securityConfigurationDAO;

    @Autowired
    private SecurityConfigurationResultDAO securityConfigurationResultDAO;

    @Autowired
    private ComponentNodeInstanceStatusDAO componentNodeInstanceStatusDAO;

    @Autowired
    private ElasticityHistoryDAO elasticityHistoryDAO;

    @Autowired
    private Sender sender;

    @Autowired
    private SimpMessagingTemplate wsTemplate;


    public OrchestratorApplicationInstance scaleUpFunctionality(OrchestratorScalingRequest orchestratorScalingRequest) {

        try {
            if (null != orchestratorScalingRequest && null != orchestratorScalingRequest
                    .getGraphInstanceID() && !orchestratorScalingRequest.getGraphInstanceID().isEmpty()
                    && null != orchestratorScalingRequest.getComponentNodeID() && !orchestratorScalingRequest
                    .getComponentNodeID().isEmpty() && null != orchestratorScalingRequest.getComponentNodeInstanceID()
                    && !orchestratorScalingRequest.getComponentNodeInstanceID().isEmpty()) {

                String scalingRequestJson = objectMapper.writeValueAsString(orchestratorScalingRequest);
                logger.log(Level.SEVERE, "Scaling up: {}", scalingRequestJson);

                ApplicationInstance applicationInstance =
                        applicationInstanceService.fetchApplicationInstanceById(Long.valueOf(orchestratorScalingRequest.getGraphInstanceID()));

                if (null != applicationInstance && null != applicationInstance.getStatus()
                        && applicationInstance.getStatus()
                        .equals(ApplicationInstance.ApplicationInstanceStatus.DEPLOYED.name())) {

                    // Check if service exists
                    if (!applicationInstance.getComponentNodeInstances().stream().filter(
                                    componentNodeInstance -> componentNodeInstance.getComponentNodeInstanceID()
                                            .equals(Long.valueOf(orchestratorScalingRequest.getComponentNodeInstanceID())))
                            .collect(Collectors.toList()).isEmpty()) {

                        Application application = applicationService.fetchApplicationById(applicationInstance.getApplication().getId());

                        // Create new worker
                        ComponentNodeInstance componentNodeInstance = componentNodeInstanceService
                                .fetchComponentNodeInstanceById(Long.valueOf(orchestratorScalingRequest.getComponentNodeInstanceID()));

                        ScalingObjects scalingRequest = new ScalingObjects();
                        scalingRequest.setApplication(application);
                        scalingRequest.setApplicationInstance(applicationInstance);
                        scalingRequest.setComponentNodeInstanceWorker(componentNodeInstance);
                        scalingRequest.setNumberOfWorkers(orchestratorScalingRequest.getNumberOfWorkers());

                        ScalingObjects scalingResponse;
                        ElasticityFrameworkBackend elasticityFrameworkBackend = elasticityService.fetchElasticityBackendAdapter(componentNodeInstance);
                        if (elasticityFrameworkBackend == null) {
                            throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
                        }

                        scalingResponse = elasticityFrameworkBackend.scaleOut(scalingRequest);

                        if (!scalingResponse.getProceedWithScaling()) {
                            throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
                        }

                        //TODO astrid
                        //ADD new result
                        if (null != componentNodeInstance.getSecurityEnablers()
                                && componentNodeInstance.getSecurityEnablers().contains(SecurityEnablers.CONFIGURATION_INTEGRITY_VERIFICATION)) {

                            List<SecurityConfiguration> securityConfigurationList = securityConfigurationDAO.findAllByApplicationInstance(applicationInstance);
                            SecurityConfiguration securityConfiguration = securityConfigurationList.stream().filter(temp -> {
                                Boolean flag = temp.getSecurityConfigurationType().compareTo(SecurityEnablers.CONFIGURATION_INTEGRITY_VERIFICATION.name()) == 0;
                                return flag;
                            }).findFirst().get();

                            scalingResponse.getComponentNodeInstanceList().stream().forEach(newComponentNodeInstance -> {
                                //Store Security Configuration Result
                                SecurityConfigurationResult securityConfigurationResult = new SecurityConfigurationResult();
                                securityConfigurationResult.setComponentNodeInstance(newComponentNodeInstance);
                                securityConfigurationResult.setDateCreated(new Date());
                                securityConfigurationResult.setLastModified(new Date());
                                securityConfigurationResult.setSecurityConfiguration(securityConfiguration);
                                securityConfigurationResult.setStatus(SecurityConfigurationResultStatus.process.name());
                                securityConfigurationResult.setHexID(Util.createRandomHEXString());
                                securityConfigurationResultDAO.save(securityConfigurationResult);

                                //Notify agent for Security Configuration Result
                                eu.orchestrator.transfer.entities.backend.SecurityConfiguration securityConfigurationAgent =
                                        new eu.orchestrator.transfer.entities.backend.SecurityConfiguration();
                                securityConfigurationAgent.setGraphHexId(securityConfiguration.getApplicationInstance().getApplication().getHexID());
                                securityConfigurationAgent.setGraphInstanceHexId(securityConfiguration.getApplicationInstance().getHexID());
                                securityConfigurationAgent.setComponentNodeHexId(
                                        securityConfigurationResult.getComponentNodeInstance().getComponentNode().getHexID());
                                securityConfigurationAgent.setComponentNodeInstanceHexId(securityConfigurationResult.getComponentNodeInstance().getHexID());
                                securityConfigurationAgent.setSecurityConfigurationType(SecurityConfigurationType.CONFIGURATION_INTEGRITY_VERIFICATION);
                                securityConfigurationAgent.setResultHexId(securityConfigurationResult.getHexID());

                                try {
                                    sender.sendSecurityConfigurationToAgent(objectMapper.writeValueAsString(securityConfigurationAgent));
                                } catch (JsonProcessingException e) {
                                    e.printStackTrace();
                                }
                            });
                        }

                        //TODO chech that the response is correct
                        int existingWorkers = scalingRequest.getExistingWorkers();
                        List<ComponentNodeInstance> componentNodeInstanceList = scalingResponse.getComponentNodeInstanceList();
                        Map<Long, ComponentNodeInstance> workers = new HashMap<>();

                        for (ComponentNodeInstance workerI : componentNodeInstanceList) {
                            OrchestratorChangedStatusNotification cniStatus = new OrchestratorChangedStatusNotification();
                            cniStatus.setChangeType(OrchestratorChangedStatusNotification.ChangeType.GraphStatusChange.name());
                            cniStatus.setMessage("A new worker is going to be created!");
                            cniStatus.setStatus("LOADING");
                            cniStatus.setComponentNodeInstanceID(workerI.getComponentNodeInstanceID() + "");
                            cniStatus.setComponentNodeInstanceHexID(workerI.getHexID() + "");
                            cniStatus.setProvider(workerI.getProvider().getName());
                            cniStatus.setComponentNodeInstanceName(workerI.getName());
                            cniStatus.setComponentNodeID(workerI.getComponentNode().getComponentNodeID() + "");
                            cniStatus.setComponentNodeName(workerI.getComponentNode().getName());
                            cniStatus.setGraphID(workerI.getApplicationInstance().getApplication().getId() + "");
                            cniStatus.setGraphName(workerI.getApplicationInstance().getApplication().getName());
                            cniStatus.setGraphInstanceID(workerI.getApplicationInstance().getApplicationInstanceID() + "");
                            cniStatus.setGraphInstanceName(workerI.getApplicationInstance().getName());
                            cniStatus.setLoadBalancedBy(null != workerI.getLoadBalancedBy()
                                    ? workerI.getLoadBalancedBy().getComponentNodeInstanceID() + "" : null);
                            cniStatus.setWorkerStatus("NEW");
                            cniStatus.setDateCreated(new Date());

                            // Send WS message to UI
                            wsTemplate.convertAndSend(APPLICATION_INSTANCE_TOPIC,
                                    objectMapper.writeValueAsString(cniStatus));

                            ComponentNodeInstanceStatus componentNodeInstanceStatus = new ComponentNodeInstanceStatus();
                            componentNodeInstanceStatus.setReportedChange(cniStatus.getReportedChange());
                            componentNodeInstanceStatus.setApplicationInstance(applicationInstance);
                            componentNodeInstanceStatus.setLastModified(new Date());
                            componentNodeInstanceStatus.setDateCreated(new Date());
                            componentNodeInstanceStatus.setComponentNodeInstance(workerI);
                            componentNodeInstanceStatus.setMessage(cniStatus.getMessage());
                            componentNodeInstanceStatus.setStatus(cniStatus.getStatus());
                            componentNodeInstanceStatusDAO.save(componentNodeInstanceStatus);

                            workers.put(workerI.getComponentNodeInstanceID(), workerI);
                        }

                        if (!workers.isEmpty()) {
                            List<ElasticityHistory> elasticityHistoryListOld = elasticityService
                                    .fetchByApplicationInstanceAndAndComponentNodeOrderByIdDesc(applicationInstance, componentNodeInstance.getComponentNode());

                            if (NullCheckUtil.isNotEmpty(elasticityHistoryListOld)) {
                                //TODO what will happen   when the Receiver write to the last entry we have already read it?
                                ElasticityHistory elasticityHistoryOld = elasticityHistoryListOld.get(0);
                                ElasticityHistory elasticityHistory = new ElasticityHistory();
                                elasticityHistory.setApplicationInstance(applicationInstance);
                                elasticityHistory.setComponentNode(componentNodeInstance.getComponentNode());
                                elasticityHistory.setActiveWorkers(elasticityHistoryOld.getActiveWorkers());
                                elasticityHistory.setWorkersCount((long) (existingWorkers + orchestratorScalingRequest.getNumberOfWorkers()));
                                elasticityHistory.setStatus(ElasticityHistory.ElasticityStatus.SCALING_OUT.name());
                                elasticityHistory.setDateCreated(new Date());
                                elasticityHistory.setLastModified(new Date());

                                elasticityHistoryDAO.save(elasticityHistory);
                            } else {
                                logger.warning("Couldn't find elasticityHistory for the component node: "
                                        + componentNodeInstance.getComponentNode().getName() + " of the application instance: "
                                        + applicationInstance.getName() + " at scaling out!");
                            }
                        }

                        String scalingResponseJson = objectMapper.writeValueAsString(scalingResponse.getOrchestratorApplicationInstance());

                        logger.info("Scaling up finished: " + scalingResponseJson);

                        try {
                            DashboardTO dashboardTO = new DashboardTO();
                            dashboardTO.setElasticity(true);
                            dashboardTO.setIdsSecurity(true);
                            dashboardTO.setIpsSecurity(true);
                            String notificationAsString = null;
                            notificationAsString = objectMapper.writeValueAsString(dashboardTO);
                            wsTemplate.convertAndSend(DASHBOARD_TOPIC, notificationAsString);
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }

                        //TODO send deployment topology into kafka topic
                        try {
                            String topologyAsString = objectMapper.writeValueAsString(scalingResponse.getOrchestratorApplicationInstance());
                            sender.sendOrchestratorDeploymentTopology(topologyAsString);
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                            logger.log(Level.SEVERE, e.getMessage(), e);
                        }
                        return scalingResponse.getOrchestratorApplicationInstance();
                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
            throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
        }
        throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
    }

    public void scaleDownFunctionality(OrchestratorScalingRequest orchestratorScalingRequest) {
        try {
            if (null != orchestratorScalingRequest && null != orchestratorScalingRequest
                    .getGraphInstanceID() && !orchestratorScalingRequest.getGraphInstanceID().isEmpty()
                    && null != orchestratorScalingRequest.getComponentNodeID() && !orchestratorScalingRequest
                    .getComponentNodeID().isEmpty()
                    && null != orchestratorScalingRequest.getComponentNodeInstanceID()
                    && !orchestratorScalingRequest.getComponentNodeInstanceID().isEmpty()) {

                String scalingRequest = objectMapper.writeValueAsString(orchestratorScalingRequest);

                logger.log(Level.INFO, "Scaling down: {}", scalingRequest);

                ApplicationInstance applicationInstance =
                        applicationInstanceService.fetchApplicationInstanceById(Long.valueOf(orchestratorScalingRequest.getGraphInstanceID()));

                if (null != applicationInstance && null != applicationInstance.getStatus()
                        && applicationInstance.getStatus().equals(ApplicationInstance.ApplicationInstanceStatus.DEPLOYED.name())) {

                    // Check if service exists
                    if (!applicationInstance.getComponentNodeInstances().stream().filter(
                                    componentNodeInstance -> componentNodeInstance.getComponentNodeInstanceID()
                                            .equals(Long.valueOf(orchestratorScalingRequest.getComponentNodeInstanceID())))
                            .collect(Collectors.toList()).isEmpty()) {

                        // Find existing worker
                        ComponentNodeInstance componentNodeInstance = componentNodeInstanceService
                                .fetchComponentNodeInstanceById(Long.valueOf(orchestratorScalingRequest.getComponentNodeInstanceID()));

                        ElasticityFrameworkBackend elasticityFrameworkBackend = elasticityService.fetchElasticityBackendAdapter(componentNodeInstance);
                        if (elasticityFrameworkBackend == null) {
                            throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
                        }

                        if (elasticityFrameworkBackend.scaleIn(orchestratorScalingRequest)) {
                            // Send notification before deleting it
                            OrchestratorChangedStatusNotification cniStatus = new OrchestratorChangedStatusNotification();
                            cniStatus.setChangeType(OrchestratorChangedStatusNotification.ChangeType.GraphStatusChange.name());
                            cniStatus.setMessage("This worker is shutted down!");
                            cniStatus.setStatus("INFO");
                            cniStatus.setComponentNodeInstanceID(componentNodeInstance.getComponentNodeInstanceID() + "");
                            cniStatus.setComponentNodeInstanceName(componentNodeInstance.getName());
                            cniStatus.setComponentNodeID(componentNodeInstance.getComponentNode().getComponentNodeID() + "");
                            cniStatus.setComponentNodeName(componentNodeInstance.getComponentNode().getName());
                            cniStatus.setGraphID(componentNodeInstance.getApplicationInstance().getApplication().getId() + "");
                            cniStatus.setGraphName(componentNodeInstance.getApplicationInstance().getApplication().getName());
                            cniStatus.setGraphInstanceID(componentNodeInstance.getApplicationInstance().getApplicationInstanceID() + "");
                            cniStatus.setGraphInstanceName(componentNodeInstance.getApplicationInstance().getName());
                            cniStatus.setLoadBalancedBy(null != componentNodeInstance.getLoadBalancedBy() ?
                                    componentNodeInstance.getLoadBalancedBy().getComponentNodeInstanceID() + "" : null);
                            cniStatus.setWorkerStatus("REMOVE");
                            cniStatus.setDateCreated(new Date());

                            // Send WS message to UI
                            wsTemplate.convertAndSend(APPLICATION_INSTANCE_TOPIC, objectMapper.writeValueAsString(cniStatus));

                            logger.info("Scaling in has been finished successfully!");

                            List<ElasticityHistory> elasticityHistoryListOld = elasticityService
                                    .fetchByApplicationInstanceAndAndComponentNodeOrderByIdDesc(applicationInstance, componentNodeInstance.getComponentNode());

                            if (NullCheckUtil.isNotEmpty(elasticityHistoryListOld)) {
                                ElasticityHistory elasticityHistoryOld = elasticityHistoryListOld.get(0);

                                //TODO what will happen when the activeWorkers<workersCount and the orchestrator has not removed a non active worker?
                                ElasticityHistory elasticityHistory = new ElasticityHistory();
                                elasticityHistory.setApplicationInstance(applicationInstance);
                                elasticityHistory.setComponentNode(componentNodeInstance.getComponentNode());
                                elasticityHistory.setActiveWorkers(elasticityHistoryOld.getActiveWorkers() - 1);
                                elasticityHistory.setWorkersCount(elasticityHistoryOld.getWorkersCount() - 1);
                                elasticityHistory.setStatus(ElasticityHistory.ElasticityStatus.NEUTRAL.name());
                                elasticityHistory.setDateCreated(new Date());
                                elasticityHistory.setLastModified(new Date());

                                elasticityHistoryDAO.save(elasticityHistory);
                            } else {
                                logger.warning("Couldn't find elasticityHistory for the component node: "
                                        + componentNodeInstance.getComponentNode().getName() + " of the application instance: "
                                        + applicationInstance.getName() + " at scaling in!");
                            }

                            try {
                                DashboardTO dashboardTO = new DashboardTO();
                                dashboardTO.setElasticity(true);
                                dashboardTO.setIdsSecurity(true);
                                dashboardTO.setIpsSecurity(true);
                                dashboardTO.setSystemLogs(true);
                                dashboardTO.setAlertLogs(true);
                                String notificationAsString = null;
                                notificationAsString = objectMapper.writeValueAsString(dashboardTO);
                                wsTemplate.convertAndSend(DASHBOARD_TOPIC, notificationAsString);
                            } catch (JsonProcessingException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
        }
        throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
    }
}
