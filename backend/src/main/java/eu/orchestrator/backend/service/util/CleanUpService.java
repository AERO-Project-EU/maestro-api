package eu.orchestrator.backend.service.util;

import eu.orchestrator.backend.transfer.DashboardTO;
import eu.orchestrator.repository.dao.ApplicationInstanceDAO;
import eu.orchestrator.repository.dao.ComponentNodeDAO;
import eu.orchestrator.repository.dao.ComponentNodeInstanceAlertDAO;
import eu.orchestrator.repository.dao.ComponentNodeInstanceDAO;
import eu.orchestrator.repository.dao.ComponentNodeInstanceIPDAO;
import eu.orchestrator.repository.dao.ComponentNodeInstanceStatusDAO;
import eu.orchestrator.repository.dao.ConstraintDAO;
import eu.orchestrator.repository.dao.DeviceInstanceDAO;
import eu.orchestrator.repository.dao.DomainNameDAO;
import eu.orchestrator.repository.dao.EnvironmentalVariableInstanceDAO;
import eu.orchestrator.repository.dao.FlavorInstanceDAO;
import eu.orchestrator.repository.dao.HealthCheckInstanceDAO;
import eu.orchestrator.repository.dao.IDRuleSetInstanceDAO;
import eu.orchestrator.repository.dao.InterfaceInstanceDAO;
import eu.orchestrator.repository.dao.LocationInstanceDAO;
import eu.orchestrator.repository.dao.PluginInstanceDAO;
import eu.orchestrator.repository.dao.ProviderHistoryDAO;
import eu.orchestrator.repository.dao.VolumeInstanceDAO;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.ApplicationInstanceQuota;
import eu.orchestrator.repository.domain.ComponentNode;
import eu.orchestrator.repository.domain.ComponentNodeInstanceAlert;
import eu.orchestrator.repository.domain.ComponentNodeInstanceIP;
import eu.orchestrator.repository.domain.ComponentNodeInstanceStatus;
import eu.orchestrator.repository.domain.DeviceInstance;
import eu.orchestrator.repository.domain.DomainName;
import eu.orchestrator.repository.domain.EnvironmentalVariableInstance;
import eu.orchestrator.repository.domain.IDRuleSetInstance;
import eu.orchestrator.repository.domain.InterfaceInstance;
import eu.orchestrator.repository.domain.LocationInstance;
import eu.orchestrator.repository.domain.PluginInstance;
import eu.orchestrator.repository.domain.ProviderHistory;
import eu.orchestrator.repository.domain.VolumeInstance;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import jakarta.transaction.Transactional;


@org.springframework.stereotype.Service
@Transactional(rollbackOn = Exception.class)
public class CleanUpService {

    private static final Logger logger = LoggerFactory.getLogger(CleanUpService.class);

    private static final String DASHBOARD_TOPIC = "/dashboard";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private SimpMessagingTemplate wsTemplate;

    @Autowired
    private ApplicationInstanceDAO applicationInstanceDAO;

    @Autowired
    private ConstraintDAO constraintDAO;

    @Autowired
    private VolumeInstanceDAO volumeInstanceDAO;

    @Autowired
    private InterfaceInstanceDAO interfaceInstanceDAO;

    @Autowired
    private EnvironmentalVariableInstanceDAO environmentalVariableInstanceDAO;

    @Autowired
    private PluginInstanceDAO pluginInstanceDAO;

    @Autowired
    private IDRuleSetInstanceDAO idRuleSetInstanceDAO;

    @Autowired
    private DeviceInstanceDAO deviceInstanceDAO;

    @Autowired
    private LocationInstanceDAO locationInstanceDAO;

    @Autowired
    private FlavorInstanceDAO flavorInstanceDAO;

    @Autowired
    private HealthCheckInstanceDAO healthCheckInstanceDAO;

    @Autowired
    private ComponentNodeInstanceStatusDAO componentNodeInstanceStatusDAO;

    @Autowired
    private ComponentNodeInstanceAlertDAO componentNodeInstanceAlertDAO;

    @Autowired
    private ComponentNodeInstanceIPDAO componentNodeInstanceIPDAO;

    @Autowired
    private DomainNameDAO domainNameDAO;

    @Autowired
    private ComponentNodeDAO componentNodeDAO;

    @Autowired
    private ComponentNodeInstanceDAO componentNodeInstanceDAO;

    @Autowired
    private ProviderHistoryDAO providerHistoryDAO;


    public void applicationInstanceRemoveStatuses(Long applicationInstanceID, boolean status) {

        try {

            Optional<ApplicationInstance> applicationInstanceOptional = applicationInstanceDAO.findById(applicationInstanceID);
            ApplicationInstance applicationInstance;

            if (applicationInstanceOptional.isPresent()) {
                applicationInstance = applicationInstanceOptional.get();
                if (null != applicationInstanceOptional.get().getStatus()
                        && applicationInstance.getStatus().equals(ApplicationInstance.ApplicationInstanceStatus.UNDEPLOYING.name())) {

                    if (status) {
                        if (null != applicationInstance.getComponentNodeInstances() && !applicationInstance
                                .getComponentNodeInstances().isEmpty()) {

                            applicationInstance.getComponentNodeInstances().stream()
                                    .forEach(componentNodeInstance -> {

                                        List<VolumeInstance> volumeInstances = volumeInstanceDAO
                                                .findAllByComponentNodeInstanceOrderByDateCreatedDesc(componentNodeInstance,
                                                        null).getContent();

                                        if (null != volumeInstances && !volumeInstances.isEmpty()) {

                                            volumeInstanceDAO.deleteAll(volumeInstances);

                                        }

                                        List<InterfaceInstance> interfaceInstances = interfaceInstanceDAO
                                                .findAllByComponentNodeInstanceOrderByDateCreatedDesc(componentNodeInstance,
                                                        null).getContent();

                                        if (null != interfaceInstances && !interfaceInstances.isEmpty()) {

                                            interfaceInstanceDAO.deleteAll(interfaceInstances);

                                        }

                                        List<EnvironmentalVariableInstance> environmentalVariableInstances = environmentalVariableInstanceDAO
                                                .findAllByComponentNodeInstanceOrderByDateCreatedDesc(componentNodeInstance,
                                                        null).getContent();

                                        if (null != environmentalVariableInstances && !environmentalVariableInstances
                                                .isEmpty()) {

                                            environmentalVariableInstanceDAO.deleteAll(environmentalVariableInstances);

                                        }

                                        List<PluginInstance> pluginInstances = pluginInstanceDAO
                                                .findAllByComponentNodeInstanceOrderByDateCreatedDesc(componentNodeInstance,
                                                        null).getContent();

                                        if (null != pluginInstances && !pluginInstances.isEmpty()) {

                                            pluginInstanceDAO.deleteAll(pluginInstances);

                                        }

                                        List<IDRuleSetInstance> idRuleSetInstances = idRuleSetInstanceDAO
                                                .findAllByComponentNodeInstanceOrderByDateCreatedDesc(componentNodeInstance,
                                                        null).getContent();

                                        if (null != idRuleSetInstances && !idRuleSetInstances.isEmpty()) {

                                            idRuleSetInstanceDAO.deleteAll(idRuleSetInstances);
                                        }

                                        List<DeviceInstance> deviceInstances = deviceInstanceDAO
                                                .findAllByComponentNodeInstanceOrderByDateCreatedDesc(componentNodeInstance,
                                                        null).getContent();

                                        if (null != deviceInstances && !deviceInstances.isEmpty()) {

                                            deviceInstanceDAO.deleteAll(deviceInstances);
                                        }

                                        List<LocationInstance> locationInstances = locationInstanceDAO
                                                .findAllByComponentNodeInstance(componentNodeInstance, null).getContent();

                                        if (null != locationInstances && !locationInstances.isEmpty()) {

                                            locationInstanceDAO.deleteAll(locationInstances);
                                        }

                                        if (flavorInstanceDAO.findByComponentNodeInstance(componentNodeInstance)
                                                .isPresent()) {

                                            flavorInstanceDAO.delete(
                                                    flavorInstanceDAO.findByComponentNodeInstance(componentNodeInstance)
                                                            .get());
                                        }

                                        if (healthCheckInstanceDAO.findByComponentNodeInstance(componentNodeInstance)
                                                .isPresent()) {

                                            healthCheckInstanceDAO.delete(
                                                    healthCheckInstanceDAO.findByComponentNodeInstance(componentNodeInstance)
                                                            .get());
                                        }

                                        List<ComponentNodeInstanceStatus> componentNodeInstanceStatuses = componentNodeInstanceStatusDAO
                                                .findAllByComponentNodeInstanceOrderByDateCreatedDesc(componentNodeInstance,
                                                        null).getContent();

                                        if (null != componentNodeInstanceStatuses && !componentNodeInstanceStatuses
                                                .isEmpty()) {

                                            componentNodeInstanceStatusDAO.deleteAll(componentNodeInstanceStatuses);
                                        }

                                        List<ComponentNodeInstanceAlert> componentNodeInstanceAlerts = componentNodeInstanceAlertDAO
                                                .findAllByComponentNodeInstanceOrderByDateCreatedDesc(componentNodeInstance,
                                                        null).getContent();

                                        if (null != componentNodeInstanceAlerts && !componentNodeInstanceAlerts
                                                .isEmpty()) {

                                            componentNodeInstanceAlertDAO.deleteAll(componentNodeInstanceAlerts);
                                        }

                                        List<ComponentNodeInstanceIP> componentNodeInstanceIPs = componentNodeInstanceIPDAO
                                                .findAllByComponentNodeInstanceOrderByDateCreatedDesc(componentNodeInstance,
                                                        null).getContent();

                                        if (null != componentNodeInstanceIPs && !componentNodeInstanceIPs.isEmpty()) {

                                            componentNodeInstanceIPs.forEach(componentNodeInstanceIP -> {

                                                List<DomainName> allDomainName = domainNameDAO
                                                        .findAllByComponentNodeInstanceIP(componentNodeInstanceIP);

                                                if (null != allDomainName && !allDomainName.isEmpty()) {
                                                    allDomainName.forEach(domainName -> {
                                                        domainName.setComponentNodeInstanceIP(null);
                                                        domainNameDAO.save(domainName);
                                                    });

                                                }
                                            });

                                            componentNodeInstanceIPDAO.deleteAll(componentNodeInstanceIPs);
                                        }

                                        if (componentNodeInstance.getLoadBalancer().booleanValue()) {

                                            // Delete also component node
                                            ComponentNode componentNode = componentNodeDAO
                                                    .findById(componentNodeInstance.getComponentNode().getComponentNodeID())
                                                    .get();
                                            componentNodeDAO.delete(componentNode);

                                        }

                                        componentNodeInstanceDAO.delete(componentNodeInstance);

                                    });

                        }

                        if (null != applicationInstance.getConstraints() && !applicationInstance
                                .getConstraints().isEmpty()) {

                            constraintDAO.deleteAll(applicationInstance.getConstraints());

                        }

                        ApplicationInstanceQuota quota = applicationInstance.getApplicationInstanceQuota();
                        Optional<ProviderHistory> providerHistoryOp = providerHistoryDAO
                                .findTopByOrderByDateCreatedDesc();
                        if (null != providerHistoryOp && providerHistoryOp.isPresent()) {
                            ProviderHistory providerHistory = providerHistoryOp.get();
                            Long vCPUs = providerHistory.getvCPUs();
                            Long memory = providerHistory.getMemory();

                            if (null != quota) {
                                vCPUs = vCPUs - Long.valueOf(quota.getVirtualCPUs());
                                memory = memory - Long.valueOf(quota.getMemory());
                            }

                            ProviderHistory newProviderHistory = new ProviderHistory();
                            newProviderHistory.setvCPUs(vCPUs);
                            newProviderHistory.setMemory(memory);
                            newProviderHistory.setDateCreated(new Date());
                            providerHistoryDAO.save(newProviderHistory);
                        }

                        applicationInstanceDAO.delete(applicationInstance);
                    } else {
                        applicationInstance
                                .setStatus(ApplicationInstance.ApplicationInstanceStatus.ERROR_OCCURRED.name());
                        applicationInstanceDAO.save(applicationInstance);
                    }

                    notifyUI();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }

    }

    private void notifyUI() {
        try {
            DashboardTO dashboardTO = new DashboardTO();
            dashboardTO.setOverview(true);
            dashboardTO.setSystemLogs(true);
            dashboardTO.setAlertLogs(true);
            dashboardTO.setElasticity(true);
            dashboardTO.setIdsSecurity(true);
            dashboardTO.setIpsSecurity(true);
            dashboardTO.setProviderHistory(true);
            String notificationAsString = null;
            notificationAsString = objectMapper.writeValueAsString(dashboardTO);
            wsTemplate.convertAndSend(DASHBOARD_TOPIC, notificationAsString);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
    }

}
