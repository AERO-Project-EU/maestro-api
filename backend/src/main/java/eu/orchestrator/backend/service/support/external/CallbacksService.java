package eu.orchestrator.backend.service.support.external;

import eu.orchestrator.common.exception.BadRequestBusinessException;
import eu.orchestrator.common.exception.NotFoundException;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.service.applicationinstance.ApplicationInstanceService;
import eu.orchestrator.backend.service.applicationinstance.ComponentNodeInstanceService;
import eu.orchestrator.backend.service.applicationinstance.ConstraintService;
import eu.orchestrator.backend.service.component.ComponentNodeService;
import eu.orchestrator.backend.service.domain.DomainService;
import eu.orchestrator.backend.service.elasticity.ProfileService;
import eu.orchestrator.backend.service.oss.OssService;
import eu.orchestrator.backend.service.oss.slice.SliceResponseStatus;
import eu.orchestrator.backend.service.oss.slice.SliceService;
import eu.orchestrator.backend.service.resourceprovider.ProviderService;
import eu.orchestrator.backend.service.security.IdRuleSetService;
import eu.orchestrator.backend.service.security.SocPolicyService;
import eu.orchestrator.backend.transfer.DashboardTO;
import eu.orchestrator.repository.dao.DeviceInstanceDAO;
import eu.orchestrator.repository.dao.EnvironmentalVariableInstanceDAO;
import eu.orchestrator.repository.dao.FlavorInstanceDAO;
import eu.orchestrator.repository.dao.HealthCheckInstanceDAO;
import eu.orchestrator.repository.dao.InterfaceInstanceDAO;
import eu.orchestrator.repository.dao.LocationInstanceDAO;
import eu.orchestrator.repository.dao.PluginInstanceDAO;
import eu.orchestrator.repository.dao.RuntimePolicyDAO;
import eu.orchestrator.repository.dao.SliceConstraintSatisfactionDAO;
import eu.orchestrator.repository.dao.SlicePlacementAttachmentPointDAO;
import eu.orchestrator.repository.dao.SlicePlacementDAO;
import eu.orchestrator.repository.dao.SliceProviderDAO;
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
import eu.orchestrator.repository.domain.Profile;
import eu.orchestrator.repository.domain.Profile.ProfileStatus;
import eu.orchestrator.repository.domain.ProviderHistory;
import eu.orchestrator.repository.domain.RuntimePolicy;
import eu.orchestrator.repository.domain.SliceConstraintSatisfaction;
import eu.orchestrator.repository.domain.SlicePlacement;
import eu.orchestrator.repository.domain.SlicePlacementAttachmentPoint;
import eu.orchestrator.repository.domain.SliceProvider;
import eu.orchestrator.repository.domain.SocPolicy;
import eu.orchestrator.repository.domain.VolumeInstance;
import eu.orchestrator.transfer.entities.oss.Slice;
import eu.orchestrator.transfer.entities.physiognomica.PhysiognomicaResultModel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.transaction.Transactional;


import static eu.orchestrator.backend.util.Util.toJson;
import static eu.orchestrator.common.enums.GenericMessage.APPLICATION_INSTANCE_NOT_EXIST;

@Service
@Transactional(rollbackOn = Exception.class)
public class CallbacksService {

    private static final Logger logger = Logger.getLogger(CallbacksService.class.getName());

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final RestTemplate restTemplate = new RestTemplate();
    private static final String DASHBOARD_TOPIC = "/dashboard";

    @Autowired
    private ApplicationInstanceService applicationInstanceService;

    @Autowired
    private SliceService sliceService;

    @Autowired
    private ConstraintService constraintService;

    @Autowired
    private ProviderService providerService;

    @Autowired
    private ComponentNodeInstanceService componentNodeInstanceService;

    @Autowired
    private IdRuleSetService idRuleSetService;

    @Autowired
    private DomainService domainService;

    @Autowired
    private ComponentNodeService componentNodeService;

    @Autowired
    private SocPolicyService socPolicyService;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private InterfaceInstanceDAO interfaceInstanceDAO;

    @Autowired
    private SliceConstraintSatisfactionDAO sliceConstraintSatisfactionDAO;

    @Autowired
    private SliceProviderDAO sliceProviderDAO;

    @Autowired
    private SlicePlacementDAO slicePlacementDAO;

    @Autowired
    private SlicePlacementAttachmentPointDAO slicePlacementAttachmentPointDAO;

    @Autowired
    private VolumeInstanceDAO volumeInstanceDAO;

    @Autowired
    private EnvironmentalVariableInstanceDAO environmentalVariableInstanceDAO;

    @Autowired
    private DeviceInstanceDAO deviceInstanceDAO;

    @Autowired
    private PluginInstanceDAO pluginInstanceDAO;

    @Autowired
    private LocationInstanceDAO locationInstanceDAO;

    @Autowired
    private FlavorInstanceDAO flavorInstanceDAO;

    @Autowired
    private HealthCheckInstanceDAO healthCheckInstanceDAO;

    @Autowired
    private RuntimePolicyDAO runtimePolicyDAO;

    @Autowired
    private OssService ossService;

    @Autowired
    private SimpMessagingTemplate wsTemplate;

    @Value("${oss.server.url}")
    private String ossURL;

    @Value("${oss.server.slice-endpoint}")
    String ossSliceEndpoint;

    public void sliceFromOss(Long applicationInstanceId, Slice slice, SliceResponseStatus sliceResponseStatus) {
        logger.log(Level.INFO, "Received slice for application [id={0}] with status [{1}]: {2}",
                new Object[]{applicationInstanceId, sliceResponseStatus, toJson(slice)});

        final ApplicationInstance applicationInstance = applicationInstanceService.fetchApplicationInstanceById(applicationInstanceId);
        if (applicationInstance == null) {
            throw new BadRequestBusinessException(APPLICATION_INSTANCE_NOT_EXIST.getMessageEN(), APPLICATION_INSTANCE_NOT_EXIST);
        }

        ossService.ingestSlice(applicationInstance, slice, sliceResponseStatus);
    }

    public void undeploymentStatusFromOrchestrator(Long applicationInstanceID, String status) {
        if (null != applicationInstanceID && null != status && !status.isEmpty()) {
            ApplicationInstance applicationInstance = applicationInstanceService.fetchApplicationInstanceById(applicationInstanceID);
            if (null != applicationInstance && null != applicationInstance.getStatus()
                    && applicationInstance.getStatus().equals(ApplicationInstance.ApplicationInstanceStatus.UNDEPLOYING.name())) {
                if (status.equals("SUCCESS")) {
                    // TODO
                    // Delete data from OSS and slice tables
                    eu.orchestrator.repository.domain.Slice slice = sliceService.fetchByApplicationInstance(applicationInstance);
                    if (slice != null) {
                        boolean isSuccess = false;
                        // Call OSS first
                        try {
                            // POST Slice Intent transfer object to OSS
                            String ossSliceEndpointUrl = ossURL + "/" + ossSliceEndpoint + "/" + applicationInstance.getApplicationInstanceID().toString();
                            String normalizedOssSliceEndpointUrl = URI.create(ossSliceEndpointUrl).normalize().toString();

                            ResponseEntity<String> responseEntity = restTemplate.exchange(
                                    normalizedOssSliceEndpointUrl, HttpMethod.DELETE, null, String.class);
                            if (null != responseEntity && responseEntity.getStatusCode() == HttpStatus.OK) {
                                logger.info("Response: " + responseEntity.getBody());
                                JSONObject callbackJSON = new JSONObject(responseEntity.getBody());
                                isSuccess = callbackJSON.getString("code").equals("SUCCESS");
                            } else {
                                isSuccess = false;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            isSuccess = false;
                            logger.log(Level.SEVERE, e.getMessage(), e);
                        }
                        if (isSuccess) {
                            List<SliceConstraintSatisfaction> sliceConstraintSatisfactions = sliceConstraintSatisfactionDAO.findAllBySlice(slice);
                            if (null != sliceConstraintSatisfactions && !sliceConstraintSatisfactions.isEmpty()) {
                                sliceConstraintSatisfactionDAO.deleteAll(sliceConstraintSatisfactions);
                            }
                            List<SlicePlacement> slicePlacements = slicePlacementDAO.findAllBySlice(slice);
                            if (null != slicePlacements && !slicePlacements.isEmpty()) {
                                slicePlacements.forEach(slicePlacement -> {
                                    List<SlicePlacementAttachmentPoint> slicePlacementAttachmentPoints = slicePlacementAttachmentPointDAO
                                            .findAllBySlicePlacement(slicePlacement);
                                    if (null != slicePlacementAttachmentPoints && !slicePlacementAttachmentPoints.isEmpty()) {
                                        slicePlacementAttachmentPointDAO.deleteAll(slicePlacementAttachmentPoints);
                                    }
                                });
                            }
                            List<SliceProvider> sliceProviders = sliceProviderDAO.findAllBySlice(slice);
                            if (null != sliceProviders && !sliceProviders.isEmpty()) {
                                sliceProviderDAO.deleteAll(sliceProviders);
                            }
                            sliceService.deleteSlice(slice);
                        } else {
                            applicationInstance.setStatus(ApplicationInstance.ApplicationInstanceStatus.ERROR_OCCURRED.name());
                            applicationInstanceService.saveApplicationInstance(applicationInstance);
                            // TODO Send notification to UI
                        }
                    }
                    if (null != applicationInstance.getComponentNodeInstances() && !applicationInstance.getComponentNodeInstances().isEmpty()) {
                        applicationInstance.getComponentNodeInstances().forEach(componentNodeInstance -> {
                            List<VolumeInstance> volumeInstances = volumeInstanceDAO
                                    .findAllByComponentNodeInstanceOrderByDateCreatedDesc(componentNodeInstance, null).getContent();
                            if (null != volumeInstances && !volumeInstances.isEmpty()) {
                                volumeInstanceDAO.deleteAll(volumeInstances);
                            }
                            List<InterfaceInstance> interfaceInstances = interfaceInstanceDAO
                                    .findAllByComponentNodeInstanceOrderByDateCreatedDesc(componentNodeInstance, null).getContent();
                            if (null != interfaceInstances && !interfaceInstances.isEmpty()) {
                                interfaceInstanceDAO.deleteAll(interfaceInstances);
                            }
                            List<EnvironmentalVariableInstance> environmentalVariableInstances = environmentalVariableInstanceDAO
                                    .findAllByComponentNodeInstanceOrderByDateCreatedDesc(componentNodeInstance, null).getContent();
                            if (null != environmentalVariableInstances && !environmentalVariableInstances.isEmpty()) {
                                environmentalVariableInstanceDAO.deleteAll(environmentalVariableInstances);
                            }
                            List<PluginInstance> pluginInstances = pluginInstanceDAO
                                    .findAllByComponentNodeInstanceOrderByDateCreatedDesc(componentNodeInstance, null).getContent();
                            if (null != pluginInstances && !pluginInstances.isEmpty()) {
                                pluginInstanceDAO.deleteAll(pluginInstances);
                            }
                            List<IDRuleSetInstance> idRuleSetInstances
                                    = idRuleSetService.fetchAllIDRuleSetInstanceByComponentNodeInstanceOrderByDateCreatedDesc(componentNodeInstance);

                            if (null != idRuleSetInstances && !idRuleSetInstances.isEmpty()) {
                                idRuleSetService.deleteAllIDRuleSetInstances(idRuleSetInstances);
                            }
                            List<DeviceInstance> deviceInstances = deviceInstanceDAO
                                    .findAllByComponentNodeInstanceOrderByDateCreatedDesc(componentNodeInstance, null).getContent();
                            if (null != deviceInstances && !deviceInstances.isEmpty()) {
                                deviceInstanceDAO.deleteAll(deviceInstances);
                            }
                            List<LocationInstance> locationInstances = locationInstanceDAO
                                    .findAllByComponentNodeInstance(componentNodeInstance, null).getContent();
                            if (null != locationInstances && !locationInstances.isEmpty()) {
                                locationInstanceDAO.deleteAll(locationInstances);
                            }
                            if (flavorInstanceDAO.findByComponentNodeInstance(componentNodeInstance).isPresent()) {
                                flavorInstanceDAO.delete(flavorInstanceDAO.findByComponentNodeInstance(componentNodeInstance).get());
                            }
                            if (healthCheckInstanceDAO.findByComponentNodeInstance(componentNodeInstance).isPresent()) {
                                healthCheckInstanceDAO.delete(healthCheckInstanceDAO.findByComponentNodeInstance(componentNodeInstance).get());
                            }
                            List<ComponentNodeInstanceStatus> componentNodeInstanceStatuses
                                    = componentNodeInstanceService.fetchCNIStatusesByCNIOrderByDateCreatedDesc(componentNodeInstance);
                            if (null != componentNodeInstanceStatuses && !componentNodeInstanceStatuses.isEmpty()) {
                                componentNodeInstanceService.deleteAllCNIInstanceStatuses(componentNodeInstanceStatuses);
                            }
                            List<ComponentNodeInstanceAlert> componentNodeInstanceAlerts = componentNodeInstanceService
                                    .fetchAllCNIAlertsByComponentNodeInstanceOrderByDateCreatedDesc(componentNodeInstance);
                            if (null != componentNodeInstanceAlerts && !componentNodeInstanceAlerts.isEmpty()) {
                                componentNodeInstanceService.deleteAllCNIInstanceAlerts(componentNodeInstanceAlerts);
                            }
                            List<ComponentNodeInstanceIP> componentNodeInstanceIPs = componentNodeInstanceService
                                    .fetchAllCNIIpsByComponentNodeInstanceOrderByDateCreatedDesc(componentNodeInstance);
                            if (null != componentNodeInstanceIPs && !componentNodeInstanceIPs.isEmpty()) {
                                componentNodeInstanceIPs.forEach(componentNodeInstanceIP -> {
                                    List<DomainName> allDomainName = domainService
                                            .fetchllDomainNamesByComponentNodeInstanceIP(componentNodeInstanceIP);
                                    if (null != allDomainName && !allDomainName.isEmpty()) {
                                        allDomainName.forEach(domainName -> {
                                            domainName.setComponentNodeInstanceIP(null);
                                            domainService.save(domainName);
                                        });
                                    }
                                });
                                componentNodeInstanceService.deleteAllCNIIps(componentNodeInstanceIPs);
                            }
                            if (componentNodeInstance.getLoadBalancer().booleanValue()) {
                                // Delete also component node
                                ComponentNode componentNode
                                        = componentNodeService.fetchComponentNodeById(componentNodeInstance.getComponentNode().getComponentNodeID());
                                componentNodeService.deleteComponentNode(componentNode);
                            }
                            componentNodeInstanceService.deleteComponentNodeInstance(componentNodeInstance);
                        });
                    }
                    if (null != applicationInstance.getConstraints() && !applicationInstance.getConstraints().isEmpty()) {
                        constraintService.deleteAll(applicationInstance.getConstraints());
                    }
                    ApplicationInstanceQuota quota = applicationInstance.getApplicationInstanceQuota();
                    ProviderHistory providerHistory = providerService.fetchProviderHistoryTopByOrderByDateCreatedDesc();
                    if (null != providerHistory) {
                        Long vCPUs = providerHistory.getvCPUs();
                        Long memory = providerHistory.getMemory();

                        vCPUs = vCPUs - Long.valueOf(quota.getVirtualCPUs());
                        memory = memory - Long.valueOf(quota.getMemory());

                        ProviderHistory newProviderHistory = new ProviderHistory();
                        newProviderHistory.setvCPUs(vCPUs);
                        newProviderHistory.setMemory(memory);
                        newProviderHistory.setDateCreated(new Date());
                        providerService.saveProviderHistory(providerHistory);
                    }
                    applicationInstanceService.deleteApplicationInstance(applicationInstance);
                } else {
                    applicationInstance.setStatus(ApplicationInstance.ApplicationInstanceStatus.ERROR_OCCURRED.name());
                    applicationInstanceService.saveApplicationInstance(applicationInstance);
                }
                // TODO Send notifications to UI
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
                } catch (JsonProcessingException ex) {
                    logger.log(Level.SEVERE, ex.getMessage(), ex);
                }
            }
        }
    }

    public void policyStatusFromPolicyEngine(Long policyID, String status) {
        if (null != policyID && null != status && !status.isEmpty()) {
            RuntimePolicy runtimePolicy = runtimePolicyDAO.findById(policyID).get();
            if (null != runtimePolicy && null != runtimePolicy.getStatus() && runtimePolicy.getStatus()
                    .equals(RuntimePolicy.RuntimePolicyStatus.PENDING.name())) {
                if (status.equals("SUCCESS")) {
                    runtimePolicy.setStatus(RuntimePolicy.RuntimePolicyStatus.APPLIED.name());
                    runtimePolicy.setLastModified(new Date());
                    runtimePolicyDAO.save(runtimePolicy);
                } else {
                    runtimePolicy.setStatus(RuntimePolicy.RuntimePolicyStatus.ERROR_OCCURRED.name());
                    runtimePolicy.setLastModified(new Date());
                    runtimePolicyDAO.save(runtimePolicy);
                }
                // TODO Send notifications to UI
            }
        }
    }

    public void socPolicyStatusFromManager(String policyHexID, String status) {
        if (null != policyHexID && null != status && !status.isEmpty()) {
            SocPolicy socPolicy = socPolicyService.fetchByHexID(policyHexID);
            if (socPolicy != null) {
                if (status.equals("SUCCESS")) {
                    socPolicy.setStatus(SocPolicy.RuntimePolicyStatus.APPLIED_STREAM.name());
                    socPolicy.setLastModified(new Date());
                    socPolicyService.save(socPolicy);
                } else {
                    socPolicy.setStatus(SocPolicy.RuntimePolicyStatus.ERROR_OCCURRED_STREAM.name());
                    socPolicy.setLastModified(new Date());
                    socPolicyService.save(socPolicy);
                }
            }
        }
    }

    public void socPolicyStatusFromDrools(String policyHexID, String status) {
        if (null != policyHexID && null != status && !status.isEmpty()) {
            SocPolicy socPolicy = socPolicyService.fetchByHexID(policyHexID);
            if (socPolicy != null) {
                if (status.equals("SUCCESS")) {
                    socPolicy.setStatus(SocPolicy.RuntimePolicyStatus.APPLIED_DROOLS.name());
                    socPolicy.setLastModified(new Date());
                    socPolicyService.save(socPolicy);
                } else {
                    socPolicy.setStatus(SocPolicy.RuntimePolicyStatus.ERROR_OCCURRED_DROOLS.name());
                    socPolicy.setLastModified(new Date());
                    socPolicyService.save(socPolicy);
                }
            }
        }
    }

    public String profilerStatusFromPhysiognomica(String profileHexID, PhysiognomicaResultModel physiognomicaResultModel) {
        String existingProfileHexId = null;
        if (null != profileHexID) {
            Profile existingProfile = profileService.fetchByHexID(profileHexID);
            if (existingProfile == null) {
                throw new NotFoundException(GenericMessage.NOT_FOUND.getCode(), GenericMessage.NOT_FOUND);
            }
            if (physiognomicaResultModel.getStatus().equals("SUCCESS")) {
                existingProfile.setStatus(ProfileStatus.COMPLETE.getFriendlyName());
                existingProfile.setLastModified(new Date());
                profileService.save(existingProfile);
            } else {
                existingProfile.setStatus(ProfileStatus.ERROR_OCCURRED.getFriendlyName());
                existingProfile.setLastModified(new Date());
                profileService.save(existingProfile);
            }
            existingProfileHexId = existingProfile.getHexID();
        }
        return existingProfileHexId;
    }

}
