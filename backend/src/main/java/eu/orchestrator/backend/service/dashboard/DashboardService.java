package eu.orchestrator.backend.service.dashboard;

import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.service.component.ComponentNodeService;
import eu.orchestrator.backend.service.component.ElasticityBackendService;
import eu.orchestrator.backend.util.Util;
import eu.orchestrator.common.util.NullCheckUtil;
import eu.orchestrator.repository.dao.ProviderDAO;
import eu.orchestrator.repository.dao.ProviderHistoryDAO;
import eu.orchestrator.repository.dao.ProviderQuotaDAO;
import eu.orchestrator.repository.dao.SecurityPolicyDAO;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.ComponentNode;
import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.repository.domain.ComponentNodeInstanceAlert;
import eu.orchestrator.repository.domain.ComponentNodeInstanceStatus;
import eu.orchestrator.repository.domain.ElasticityHistory;
import eu.orchestrator.repository.domain.Provider;
import eu.orchestrator.repository.domain.ProviderHistory;
import eu.orchestrator.repository.domain.ProviderQuota;
import eu.orchestrator.repository.domain.SecurityPolicy;
import eu.orchestrator.repository.domain.User;
import eu.orchestrator.backend.service.application.ApplicationService;
import eu.orchestrator.backend.service.applicationinstance.AlertService;
import eu.orchestrator.backend.service.applicationinstance.ApplicationInstanceService;
import eu.orchestrator.backend.service.applicationinstance.ComponentNodeInstanceService;
import eu.orchestrator.backend.service.applicationinstance.StatusService;
import eu.orchestrator.backend.transfer.DashboardElasticityComponentTO;
import eu.orchestrator.backend.transfer.DashboardElasticityTO;
import eu.orchestrator.backend.transfer.DashboardOverviewTO;
import eu.orchestrator.backend.transfer.DashboardProviderHistory;
import eu.orchestrator.backend.transfer.DashboardProviderTO;
import eu.orchestrator.backend.transfer.DashboardSecurityPolicyComponentTO;
import eu.orchestrator.backend.transfer.DashboardSecurityPolicyTO;
import eu.orchestrator.backend.transfer.StatusTO;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import jakarta.transaction.Transactional;

@Service
@Transactional(rollbackOn = Exception.class)
public class DashboardService {

    @Autowired
    ProviderDAO providerDAO;

    @Autowired
    ProviderQuotaDAO providerQuotaDAO;

    @Autowired
    ProviderHistoryDAO providerHistoryDAO;

    @Autowired
    SecurityPolicyDAO securityPolicyDAO;

    @Autowired
    ComponentNodeService componentNodeService;

    @Autowired
    ComponentNodeInstanceService componentNodeInstanceService;

    @Autowired
    ApplicationService applicationService;

    @Autowired
    ApplicationInstanceService applicationInstanceService;

    @Autowired
    ElasticityBackendService elasticityBackendService;

    @Autowired
    AlertService alertService;

    @Autowired
    StatusService statusService;


    public JSONArray fetchDashboardGraphs(String type, User authenticatedUser) {
        JSONArray graphArray = new JSONArray();

        if (null != authenticatedUser && authenticatedUser.getRole().equals(User.RoleName.ADMIN.name())) {
            // Fetch all active providers
            List<Provider> providers = providerDAO.findAllByEnabled(Boolean.TRUE);
            if (null != providers && !providers.isEmpty()) {
                Integer totalCPUs = 0;
                Integer usedCPUs = 0;
                Integer totalMemory = 0;
                Integer usedMemory = 0;
                Integer totalInstances = 0;
                Integer usedInstances = 0;
                for (Provider provider : providers) {
                    if (provider.getProviderID() != -1L && provider.getProviderID() != -2L) {
                        if (providerQuotaDAO.findByProvider(provider).isPresent()) {
                            ProviderQuota providerQuota = providerQuotaDAO.findByProvider(provider).get();
                            totalCPUs += providerQuota.getMaxVirtualCPUs();
                            usedCPUs += providerQuota.getUsedVirtualCPUs();
                            totalMemory += providerQuota.getMaxMemory();
                            usedMemory += providerQuota.getUsedMemory();
                            totalInstances += providerQuota.getMaxInstances();
                            usedInstances += providerQuota.getRunningInstances();
                        }
                    }
                }

                switch (type) {
                    case "cpu":
                        JSONObject usedCPUsObj = new JSONObject();
                        usedCPUsObj.put("name", "Used vCPUs");
                        usedCPUsObj.put("y", usedCPUs);

                        JSONObject remainingCPUsObj = new JSONObject();
                        remainingCPUsObj.put("name", "Remaining vCPUs");
                        remainingCPUsObj.put("y", totalCPUs - usedCPUs);

                        graphArray.put(usedCPUsObj);
                        graphArray.put(remainingCPUsObj);

                        break;
                    case "memory":
                        JSONObject usedMemoryObj = new JSONObject();
                        usedMemoryObj.put("name", "Used Memory");
                        usedMemoryObj.put("y", usedMemory);

                        JSONObject remainingMemoryObj = new JSONObject();
                        remainingMemoryObj.put("name", "Remaining Memory");
                        remainingMemoryObj.put("y", totalMemory - usedMemory);

                        graphArray.put(usedMemoryObj);
                        graphArray.put(remainingMemoryObj);

                        break;
                    case "instance":
                        JSONObject usedInstancesObj = new JSONObject();
                        usedInstancesObj.put("name", "Used Instances");
                        usedInstancesObj.put("y", usedInstances);

                        JSONObject remainingInstancesObj = new JSONObject();
                        remainingInstancesObj.put("name", "Remaining Instances");
                        remainingInstancesObj.put("y", totalInstances - usedInstances);

                        graphArray.put(usedInstancesObj);
                        graphArray.put(remainingInstancesObj);

                        break;
                }
            }
        }
        return graphArray;
    }

    public DashboardOverviewTO fetchOverview(User authenticatedUser) {
        DashboardOverviewTO dashboardOverviewTO = new DashboardOverviewTO();

        Long applicationCount = applicationService.count(authenticatedUser);
        dashboardOverviewTO.setApplications(applicationCount);

        Long componentCount = componentNodeService.count(authenticatedUser);
        dashboardOverviewTO.setComponents(componentCount);

        List<ApplicationInstance> applicationInstanceList = applicationInstanceService.fetchAllByOrganization(authenticatedUser);
        long totalApplicationInstances = 0L;
        long usedApplicationInstances = 0L;
        if (null != applicationInstanceList && !applicationInstanceList.isEmpty()) {
            totalApplicationInstances = applicationInstanceList.size();

            for (ApplicationInstance applicationInstance : applicationInstanceList) {
                if (applicationInstance.getStatus().equals(ApplicationInstance.ApplicationInstanceStatus.DEPLOYED.name())) {
                    usedApplicationInstances++;
                }
            }

        }
        dashboardOverviewTO.setTotalInstances(totalApplicationInstances);
        dashboardOverviewTO.setUsedInstances(usedApplicationInstances);

        // Fetch all active providers
        List<Provider> providers = providerDAO.findAllByEnabled(Boolean.TRUE);
        long totalCPUs = 0L;
        long totalRam = 0L;
        //TODO find what to do with public provider that they don't have resources limitations
        if (null != providers && !providers.isEmpty()) {
            for (Provider provider : providers) {
                if (provider.getProviderID() != -1L && provider.getProviderID() != -2L) {
                    if (providerQuotaDAO.findByProvider(provider).isPresent()) {
                        ProviderQuota providerQuota = providerQuotaDAO.findByProvider(provider).get();
                        totalCPUs += providerQuota.getMaxVirtualCPUs();
                        totalRam += providerQuota.getMaxMemory();
                    }
                }
            }
        }
        dashboardOverviewTO.setTotalCPUs(totalCPUs);
        //Ram is converted to GB for better visualization
        dashboardOverviewTO.setTotalRam(totalRam / 1024);

        return dashboardOverviewTO;
    }

    public List<DashboardProviderTO> fetchProviderGraphs(User authenticatedUser) {
        List<Provider> providers = providerDAO.findAllByOrganization(authenticatedUser.getOrganization());
        List<DashboardProviderTO> dashboardProviderTOList = new ArrayList<>();
        if (null != providers && !providers.isEmpty()) {
            providers.forEach(provider -> {
                DashboardProviderTO dashboardProviderTO = new DashboardProviderTO();

                dashboardProviderTO.setName(provider.getName());
                dashboardProviderTO.setEnabled(provider.getEnabled());
                dashboardProviderTO.setType(provider.getProviderType().getFriendlyName());

                Optional<ProviderQuota> providerQuotaOP = providerQuotaDAO.findByProvider(provider);
                if (providerQuotaOP.isPresent()) {
                    ProviderQuota providerQuota = providerQuotaOP.get();

                    dashboardProviderTO.setUsedCPUs(providerQuota.getUsedVirtualCPUs());
                    dashboardProviderTO.setTotalCPUs(providerQuota.getMaxVirtualCPUs());
                    dashboardProviderTO.setUsedRam(providerQuota.getUsedMemory());
                    dashboardProviderTO.setTotalRam(providerQuota.getMaxMemory());
                    dashboardProviderTO.setUsedInstances(providerQuota.getRunningInstances());
                    dashboardProviderTO.setTotalInstances(providerQuota.getMaxInstances());
                    dashboardProviderTOList.add(dashboardProviderTO);
                }
            });
        }
        return dashboardProviderTOList;
    }

    public List<DashboardProviderHistory> fetchProviderHistoryGraphs() {

        List<DashboardProviderHistory> dashboardProviderHistoryList = new ArrayList<>();

        List<ProviderHistory> providerHistoryList = providerHistoryDAO.findTop200ByOrderByDateCreated();
        if (null != providerHistoryList && !providerHistoryList.isEmpty()) {
            for (ProviderHistory providerHistory : providerHistoryList) {
                DashboardProviderHistory dashboardProviderHistory = new DashboardProviderHistory();
                dashboardProviderHistory.setRam(providerHistory.getMemory());
                dashboardProviderHistory.setvCPUs(providerHistory.getvCPUs());
                dashboardProviderHistory.setTimestamp(providerHistory.getDateCreated().getTime());
                dashboardProviderHistoryList.add(dashboardProviderHistory);
            }
        }
        return dashboardProviderHistoryList;
    }

    public List<DashboardElasticityTO> fetchElasticityGraphs(User authenticatedUser) {

        List<ApplicationInstance> applicationInstanceList = applicationInstanceService.fetchAllByOrganization(authenticatedUser);
        List<DashboardElasticityTO> dashboardElasticityTOList = new ArrayList<>();

        if (null != applicationInstanceList && !applicationInstanceList.isEmpty()) {
            applicationInstanceList.forEach(applicationInstance -> {
                List<DashboardElasticityComponentTO> dashboardElasticityComponentTOList = new ArrayList<>();

                applicationInstance.getApplication().getComponentNodes().forEach(componentNode -> {
                    if (componentNode.getComponent().getElasticityController().equals("HORIZONTAL") ||
                            componentNode.getComponent().getElasticityController().equals("LAMBDA_FUNCTION")) {

                        List<ElasticityHistory> elasticityHistoryList = elasticityBackendService.fetchHistoryByApplicationInstanceAndComponentNodeOrderByIdDesc(
                                applicationInstance, componentNode);
                        if (NullCheckUtil.isNotEmpty(elasticityHistoryList)) {
                            ElasticityHistory elasticityHistory = elasticityHistoryList.get(0);
                            DashboardElasticityComponentTO dashboardElasticityComponentTO = new DashboardElasticityComponentTO();
                            dashboardElasticityComponentTO.setComponentNodeID(componentNode.getComponentNodeID());
                            dashboardElasticityComponentTO.setComponentNodeName(componentNode.getName());
                            dashboardElasticityComponentTO.setActiveWorkers(elasticityHistory.getActiveWorkers());
                            dashboardElasticityComponentTO.setWorkersCount(elasticityHistory.getWorkersCount());
                            dashboardElasticityComponentTO.setStatus(elasticityHistory.getStatus());
                            dashboardElasticityComponentTO.setDateCreated(elasticityHistory.getDateCreated());
                            dashboardElasticityComponentTO.setTimestamp(elasticityHistory.getDateCreated().getTime());
                            dashboardElasticityComponentTOList.add(dashboardElasticityComponentTO);
                        }
                    }
                });
                if (!dashboardElasticityComponentTOList.isEmpty()) {
                    DashboardElasticityTO dashboardElasticityTO = new DashboardElasticityTO();

                    dashboardElasticityTO.setApplicationInstanceID(applicationInstance.getApplicationInstanceID());
                    dashboardElasticityTO.setApplicationInstanceName(applicationInstance.getName());
                    dashboardElasticityTO.setDashboardElasticityComponentTOList(dashboardElasticityComponentTOList);

                    dashboardElasticityTOList.add(dashboardElasticityTO);
                }
            });
        }
        return dashboardElasticityTOList;
    }

    public List<DashboardElasticityComponentTO> fetchElasticityGraphs(Long applicationInstanceID, Long componentNodeID) {

        ApplicationInstance applicationInstance = applicationInstanceService.fetchApplicationInstanceById(applicationInstanceID);

        if (NullCheckUtil.isNotEmpty(applicationInstance)) {
            ComponentNode componentNode = componentNodeService.fetchComponentNodeById(componentNodeID);
            if (NullCheckUtil.isNotEmpty(componentNode)) {
                List<DashboardElasticityComponentTO> dashboardElasticityComponentTOList = new ArrayList<>();

                List<ElasticityHistory> elasticityHistoryList = elasticityBackendService
                        .fetchHistoryByApplicationInstanceAndAndComponentNodeOrderByDateCreated(applicationInstance, componentNode);

                elasticityHistoryList.forEach(elasticityHistory -> {
                    DashboardElasticityComponentTO dashboardElasticityComponentTO = new DashboardElasticityComponentTO();
                    dashboardElasticityComponentTO.setComponentNodeID(componentNode.getComponentNodeID());
                    dashboardElasticityComponentTO.setComponentNodeName(componentNode.getName());
                    dashboardElasticityComponentTO.setActiveWorkers(elasticityHistory.getActiveWorkers());
                    dashboardElasticityComponentTO.setWorkersCount(elasticityHistory.getWorkersCount());
                    dashboardElasticityComponentTO.setStatus(elasticityHistory.getStatus());
                    dashboardElasticityComponentTO.setDateCreated(elasticityHistory.getDateCreated());
                    dashboardElasticityComponentTO.setTimestamp(elasticityHistory.getDateCreated().getTime());
                    dashboardElasticityComponentTOList.add(dashboardElasticityComponentTO);
                });
                return dashboardElasticityComponentTOList;
            } else {
                throw new GenericBusinessException(GenericMessage.COMPONENT_NODE_IS_NOT_EXIST.getCode(), GenericMessage.COMPONENT_NODE_IS_NOT_EXIST);
            }
        } else {
            throw new GenericBusinessException(GenericMessage.APPLICATION_INSTANCE_NOT_EXIST.getCode(), GenericMessage.APPLICATION_INSTANCE_NOT_EXIST);
        }
    }

    public List<DashboardSecurityPolicyTO> fetchIntrusionDetectionTable(User authenticatedUser) {
        List<ApplicationInstance> applicationInstanceList = applicationInstanceService.fetchAllByOrganization(authenticatedUser);

        List<DashboardSecurityPolicyTO> dashboardSecurityPolicyTOList = new ArrayList<>();

        if (null != applicationInstanceList && !applicationInstanceList.isEmpty()) {

            applicationInstanceList.forEach(applicationInstance -> {
                List<ComponentNodeInstance> componentNodeInstanceList = componentNodeInstanceService
                        .fetchAllComponentNodeInstancesByApplicationInstance(applicationInstance);

                List<DashboardSecurityPolicyComponentTO> componentInstanceList = new ArrayList<>();
                HashMap<String, DashboardSecurityPolicyComponentTO> detectionHashMap = new HashMap<>();

                componentNodeInstanceList.forEach(componentNodeInstance -> {
                    if (Boolean.TRUE.equals(componentNodeInstance.getStatusIDS())) {
                        if (detectionHashMap.containsKey(componentNodeInstance.getComponentNode().getName())) {
                            DashboardSecurityPolicyComponentTO dashboardSecurityPolicyComponentTO = detectionHashMap
                                    .get(componentNodeInstance.getComponentNode().getName());
                            Long count = dashboardSecurityPolicyComponentTO.getCount();
                            count = count + alertService.countComponentNodeInstanceAlerts(applicationInstance.getApplicationInstanceID(),
                                    componentNodeInstance.getComponentNodeInstanceID());
                            dashboardSecurityPolicyComponentTO.setCount(count);

                            detectionHashMap.replace(componentNodeInstance.getComponentNode().getName(), dashboardSecurityPolicyComponentTO);

                        } else {
                            DashboardSecurityPolicyComponentTO dashboardSecurityPolicyComponentTO = new DashboardSecurityPolicyComponentTO();
                            dashboardSecurityPolicyComponentTO.setComponentNodeInstanceID(componentNodeInstance.getComponentNode().getComponentNodeID());
                            dashboardSecurityPolicyComponentTO.setComponentNodeInstanceName(componentNodeInstance.getComponentNode().getName());
                            dashboardSecurityPolicyComponentTO.setCount(alertService
                                    .countComponentNodeInstanceAlerts(applicationInstance.getApplicationInstanceID(),
                                            componentNodeInstance.getComponentNodeInstanceID()));

                            detectionHashMap.put(componentNodeInstance.getComponentNode().getName(), dashboardSecurityPolicyComponentTO);
                        }
                    }
                });
                for (DashboardSecurityPolicyComponentTO dashboardSecurityPolicyComponentTO : detectionHashMap.values()) {
                    componentInstanceList.add(dashboardSecurityPolicyComponentTO);
                }

                if (!componentInstanceList.isEmpty()) {
                    DashboardSecurityPolicyTO dashboardSecurityPolicyTO = new DashboardSecurityPolicyTO();
                    dashboardSecurityPolicyTO.setApplicationInstanceID(applicationInstance.getApplicationInstanceID());
                    dashboardSecurityPolicyTO.setApplicationInstanceName(applicationInstance.getName());
                    dashboardSecurityPolicyTO.setComponentInstanceList(componentInstanceList);

                    dashboardSecurityPolicyTOList.add(dashboardSecurityPolicyTO);
                }
            });
        }
        return dashboardSecurityPolicyTOList;
    }

    public List<DashboardSecurityPolicyTO> fetchIntrusionPreventionTable(User authenticatedUser) {
        List<ApplicationInstance> applicationInstanceList = applicationInstanceService.fetchAllByOrganization(authenticatedUser);

        List<DashboardSecurityPolicyTO> dashboardSecurityPolicyTOList = new ArrayList<>();

        if (null != applicationInstanceList && !applicationInstanceList.isEmpty()) {

            applicationInstanceList.forEach(applicationInstance -> {
                List<ComponentNodeInstance> componentNodeInstanceList = componentNodeInstanceService
                        .fetchAllComponentNodeInstancesByApplicationInstance(applicationInstance);

                List<DashboardSecurityPolicyComponentTO> componentInstanceList = new ArrayList<>();
                HashMap<String, DashboardSecurityPolicyComponentTO> preventionHashMap = new HashMap<>();

                componentNodeInstanceList.forEach(componentNodeInstance -> {
                    if (Boolean.TRUE.equals(componentNodeInstance.getStatusIPS())) {
                        if (preventionHashMap.containsKey(componentNodeInstance.getComponentNode().getName())) {
                            List<SecurityPolicy> securityPolicyList = securityPolicyDAO.
                                    findAllByApplicationInstanceAndComponentNodeInstancesContains(applicationInstance, componentNodeInstance);
                            if (null != securityPolicyList) {
                                DashboardSecurityPolicyComponentTO dashboardSecurityPolicyComponentTO = preventionHashMap
                                        .get(componentNodeInstance.getComponentNode().getName());
                                Long count = dashboardSecurityPolicyComponentTO.getCount();
                                count = count + (long) securityPolicyList.size();
                                dashboardSecurityPolicyComponentTO.setCount(count);
                                preventionHashMap.replace(componentNodeInstance.getComponentNode().getName(), dashboardSecurityPolicyComponentTO);
                            }

                        } else {
                            DashboardSecurityPolicyComponentTO dashboardSecurityPolicyComponentTO = new DashboardSecurityPolicyComponentTO();
                            dashboardSecurityPolicyComponentTO.setComponentNodeInstanceID(componentNodeInstance.getComponentNode().getComponentNodeID());
                            dashboardSecurityPolicyComponentTO.setComponentNodeInstanceName(componentNodeInstance.getComponentNode().getName());
                            dashboardSecurityPolicyComponentTO.setCount(
                                    alertService.countComponentNodeInstanceAlerts(applicationInstance.getApplicationInstanceID(),
                                            componentNodeInstance.getComponentNodeInstanceID()));

                            List<SecurityPolicy> securityPolicyList = securityPolicyDAO.
                                    findAllByApplicationInstanceAndComponentNodeInstancesContains(applicationInstance, componentNodeInstance);
                            if (null != securityPolicyList) {
                                dashboardSecurityPolicyComponentTO.setCount((long) securityPolicyList.size());
                            }
                            preventionHashMap.put(componentNodeInstance.getComponentNode().getName(), dashboardSecurityPolicyComponentTO);
                        }

                    }
                });

                for (DashboardSecurityPolicyComponentTO dashboardSecurityPolicyComponentTO : preventionHashMap.values()) {
                    componentInstanceList.add(dashboardSecurityPolicyComponentTO);
                }
                if (!componentInstanceList.isEmpty()) {
                    DashboardSecurityPolicyTO dashboardSecurityPolicyTO = new DashboardSecurityPolicyTO();
                    dashboardSecurityPolicyTO.setApplicationInstanceID(applicationInstance.getApplicationInstanceID());
                    dashboardSecurityPolicyTO.setApplicationInstanceName(applicationInstance.getName());
                    dashboardSecurityPolicyTO.setComponentInstanceList(componentInstanceList);
                    dashboardSecurityPolicyTOList.add(dashboardSecurityPolicyTO);
                }
            });
        }
        return dashboardSecurityPolicyTOList;
    }

    public List<StatusTO> fetchLogs(User authenticatedUser) {

        List<ComponentNodeInstanceStatus> componentNodeInstanceStatusList =
                statusService.fetchAllComponentNodeInstanceStatusesByOrganizationOrderByDateCreatedAsc(authenticatedUser);

        List<StatusTO> statusTOList = new ArrayList<>();
        if (null != componentNodeInstanceStatusList && !componentNodeInstanceStatusList.isEmpty()) {
            int counter = 200;
            for (ComponentNodeInstanceStatus status : componentNodeInstanceStatusList) {
                if (counter > 0) {
                    //TODO find how to filter out the list with predicate with a query that also orders them
                    if (status.getStatus().equals("SUCCESS") || status.getStatus().equals("ERROR")) {
                        try {
                            StatusTO statusTO = new StatusTO();
                            statusTO.setComponentNodeInstanceStatusID(status.getComponentNodeInstanceStatusID());
                            statusTO.setApplicationInstanceName(status.getApplicationInstance().getName());
                            statusTO.setApplicationInstanceID(status.getComponentNodeInstance().getApplicationInstance().getApplicationInstanceID());
                            statusTO.setComponentNodeInstanceID(status.getComponentNodeInstance().getComponentNodeInstanceID());
                            statusTO.setComponentNodeInstanceName(status.getComponentNodeInstance().getName());
                            statusTO.setDateCreated(status.getDateCreated());
                            statusTO.setLastModified(status.getLastModified());
                            statusTO.setMessage(status.getMessage());
                            statusTO.setReportedChange(status.getReportedChange());
                            statusTO.setStatus(status.getStatus());
                            statusTOList.add(statusTO);
                            counter--;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return statusTOList;
    }

    public List<StatusTO> fetchAlertLogs(User authenticatedUser) {

        List<ComponentNodeInstanceAlert> componentNodeInstanceAlertList
                = alertService.fetchAllByApplicationInstanceOrganizationOrderByDateCreatedAsc(authenticatedUser);

        List<StatusTO> statusTOList = new ArrayList<>();
        if (null != componentNodeInstanceAlertList && !componentNodeInstanceAlertList.isEmpty()) {
            int counter = 200;
            for (ComponentNodeInstanceAlert status : componentNodeInstanceAlertList) {
                if (counter > 0) {
                    //TODO find how to filter out the list with predicate with a query that also orders them
                    if (status.getStatus().equals("ALERT")) {
                        StatusTO statusTO = new StatusTO();
                        statusTO.setComponentNodeInstanceStatusID(status.getComponentNodeInstanceAlertID());
                        statusTO.setApplicationInstanceName(status.getApplicationInstance().getName());
                        statusTO.setApplicationInstanceID(status.getComponentNodeInstance().getApplicationInstance().getApplicationInstanceID());
                        statusTO.setComponentNodeInstanceID(status.getComponentNodeInstance().getComponentNodeInstanceID());
                        statusTO.setComponentNodeInstanceName(status.getComponentNodeInstance().getName());
                        statusTO.setDateCreated(status.getDateCreated());
                        statusTO.setLastModified(status.getLastModified());
                        statusTO.setMessage(status.getMessage());
//              statusTO.setReportedChange(status.getReportedChange());
                        statusTO.setStatus(status.getStatus());
                        statusTOList.add(statusTO);
                        counter--;
                    }
                }
            }
        }
        return statusTOList;
    }


}
