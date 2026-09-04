package eu.orchestrator.backend.service.support.external;

import eu.orchestrator.common.exception.GenericBusinessException;
import eu.orchestrator.common.exception.NotFoundException;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.service.applicationinstance.ApplicationInstanceService;
import eu.orchestrator.backend.service.applicationinstance.ComponentNodeInstanceService;
import eu.orchestrator.backend.service.component.ComponentNodeService;
import eu.orchestrator.backend.util.Util;
import eu.orchestrator.repository.dao.MetricDAO;
import eu.orchestrator.repository.dao.NotificationDAO;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.ComponentNode;
import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.repository.domain.ComponentNodeInstanceIP;
import eu.orchestrator.repository.domain.Metric;
import eu.orchestrator.repository.domain.Notification;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.stream.Collectors;
import jakarta.transaction.Transactional;

@Service
@Transactional(rollbackOn = Exception.class)
public class ExternalService {

    @Autowired
    private ApplicationInstanceService applicationInstanceService;

    @Autowired
    private ComponentNodeInstanceService componentNodeInstanceService;

    @Autowired
    private ComponentNodeService componentNodeService;

    @Autowired
    private MetricDAO metricDAO;

    @Autowired
    private NotificationDAO notificationDAO;

    @Autowired
    private SimpMessagingTemplate wsTemplate;


    public String fetchApplicationInstances() {
        List<ApplicationInstance> applicationInstances = applicationInstanceService.fetchAllApplicationInstances();
        if (null != applicationInstances && !applicationInstances.isEmpty()) {
            JSONArray array = new JSONArray();
            applicationInstances.stream().forEach(applicationInstance -> {
                JSONObject obj = new JSONObject();
                obj.put("applicationInstanceID", applicationInstance.getApplicationInstanceID());
                obj.put("applicationInstanceHexID", applicationInstance.getHexID());
                obj.put("applicationInstanceName", applicationInstance.getName());
                obj.put("applicationInstanceStatus", applicationInstance.getStatus());
                obj.put("applicationID", applicationInstance.getApplication().getId());
                obj.put("applicationName", applicationInstance.getApplication().getName());
                obj.put("applicationHexID", applicationInstance.getApplication().getHexID());
                array.put(obj);
            });
            return array.toString();
        }
        throw new NotFoundException(GenericMessage.NOT_FOUND.getCode(), GenericMessage.NOT_FOUND);
    }

    public String fetchApplicationInstanceMetrics(String applicationInstanceHexID) {
        if (null != applicationInstanceHexID && !applicationInstanceHexID.isEmpty()) {
            ApplicationInstance applicationInstance = applicationInstanceService.fetchApplicationInstanceByHexId(applicationInstanceHexID);
            if (applicationInstance != null) {
                if (applicationInstance.getStatus().equals(ApplicationInstance.ApplicationInstanceStatus.DEPLOYED.name())) {
                    List<ComponentNodeInstance> componentNodeInstances
                            = componentNodeInstanceService.fetchAllComponentNodeInstancesByApplicationInstance(applicationInstance);
                    Map<String, List<Metric>> mapOfMetrics = new HashMap<>();
                    if (null != componentNodeInstances && !componentNodeInstances.isEmpty()) {
                        componentNodeInstances.forEach(componentNodeInstance -> {
                            if (!mapOfMetrics.containsKey(String.valueOf(componentNodeInstance.getComponentNode().getComponentNodeID()))) {
                                List<Metric> metricsSTR = new ArrayList<>();
                                if (null != componentNodeInstance.getPluginInstances() && !componentNodeInstance.getPluginInstances().isEmpty()) {
                                    componentNodeInstance.getPluginInstances().forEach(pluginInstance -> {
                                        if (!pluginInstance.getDeletedPlugin().booleanValue()) {
                                            List<Metric> metrics
                                                    = metricDAO.findAllByPluginOrderByDateCreated(pluginInstance.getPlugin(), null).getContent();
                                            if (null != metrics && !metrics.isEmpty()) {
                                                metrics.forEach(metric -> {
                                                    if (metricsSTR.stream().filter(mtrc
                                                            -> mtrc.getName().equals(metric.getName())).collect(Collectors.toList()).isEmpty()) {
                                                        metricsSTR.add(metric);
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                                mapOfMetrics.put(String.valueOf(componentNodeInstance.getComponentNode().getComponentNodeID()), metricsSTR);
                            } else {
                                List<Metric> metricsSTR = new ArrayList<>();
                                List<Metric> oldMetricsSTR = mapOfMetrics.get(String.valueOf(componentNodeInstance.getComponentNode().getComponentNodeID()));
                                if (null != componentNodeInstance.getPluginInstances() && !componentNodeInstance.getPluginInstances().isEmpty()) {
                                    componentNodeInstance.getPluginInstances().forEach(pluginInstance -> {
                                        if (!pluginInstance.getDeletedPlugin().booleanValue()) {
                                            List<Metric> metrics
                                                    = metricDAO.findAllByPluginOrderByDateCreated(pluginInstance.getPlugin(), null).getContent();
                                            if (null != metrics && !metrics.isEmpty()) {
                                                metrics.forEach(metric -> {
                                                    if (oldMetricsSTR.stream().filter(mtrc
                                                            -> mtrc.getName().equals(metric.getName())).collect(Collectors.toList()).isEmpty()) {
                                                        metricsSTR.add(metric);
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                                metricsSTR.addAll(oldMetricsSTR);
                                mapOfMetrics.remove(String.valueOf(componentNodeInstance.getComponentNode().getComponentNodeID()));
                                mapOfMetrics.put(String.valueOf(componentNodeInstance.getComponentNode().getComponentNodeID()), metricsSTR);
                            }
                        });
                        JSONArray array = new JSONArray();
                        if (!mapOfMetrics.isEmpty()) {
                            mapOfMetrics.entrySet().forEach(entry -> {
                                ComponentNode componentNode = componentNodeService.fetchComponentNodeById(Long.valueOf(entry.getKey()));
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("id", componentNode.getComponentNodeID());
                                jsonObject.put("name", componentNode.getName());
                                jsonObject.put("hexID", componentNode.getHexID());
                                List<Metric> metrics = entry.getValue();
                                JSONArray metricsArray = new JSONArray();
                                if (null != metrics && !metrics.isEmpty()) {
                                    metrics.forEach(metric -> {
                                        JSONObject metricObj = new JSONObject();
                                        metricObj.put("name", metric.getName());
                                        metricObj.put("friendlyName", metric.getFriendlyName());
                                        metricObj.put("unit", metric.getUnit());
                                        metricsArray.put(metricObj);
                                    });
                                }
                                jsonObject.put("metrics", metricsArray);
                                array.put(jsonObject);
                            });
                        }
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("applicationInstanceID", applicationInstance.getApplicationInstanceID());
                        jsonObject.put("applicationInstanceHexID", applicationInstance.getHexID());
                        jsonObject.put("applicationInstanceName", applicationInstance.getName());
                        jsonObject.put("applicationInstanceStatus", applicationInstance.getStatus());
                        jsonObject.put("applicationID", applicationInstance.getApplication().getId());
                        jsonObject.put("applicationName", applicationInstance.getApplication().getName());
                        jsonObject.put("applicationHexID", applicationInstance.getApplication().getHexID());
                        jsonObject.put("components", array);
                        return jsonObject.toString();
                    }
                }
            }
        }
        throw new NotFoundException(GenericMessage.NOT_FOUND.getCode(), GenericMessage.NOT_FOUND);
    }

    public void sendFakeNotification() {
        Util.sendPushNotification("1", "Fake notification!!!", "", null, null,
                Notification.ComponentType.APPLICATION_INSTANCE.name(), notificationDAO, wsTemplate);
    }

    //TODO evaluator
    public String getComponentNodeInstancePublicIp(String componentNodeInstanceID) {
        ComponentNodeInstance componentNodeInstance = componentNodeInstanceService.fetchComponentNodeInstanceById(Long.parseLong(componentNodeInstanceID));
        if (componentNodeInstance != null) {
            SortedSet<ComponentNodeInstanceIP> componentNodeInstanceIPList = componentNodeInstance.getComponentNodeInstanceIPs();
            Optional<ComponentNodeInstanceIP> componentNodeInstanceIPOptional = componentNodeInstanceIPList.stream()
                    .filter(ip -> null != ip.getType())
                    .filter(ip -> ip.getType().compareTo("ACCESS") == 0)
                    .findFirst();
            if (componentNodeInstanceIPOptional.isPresent()) {
                ComponentNodeInstanceIP componentNodeInstanceIP = componentNodeInstanceIPOptional.get();
                return componentNodeInstanceIP.getIp();
            } else {
                throw new NotFoundException(GenericMessage.NOT_FOUND.getCode(), GenericMessage.NOT_FOUND);
            }
        }
        throw new GenericBusinessException(GenericMessage.GENERIC_ERROR.getCode(), GenericMessage.GENERIC_ERROR);
    }
}
