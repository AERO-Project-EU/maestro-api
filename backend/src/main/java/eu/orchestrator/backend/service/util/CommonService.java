package eu.orchestrator.backend.service.util;

import eu.orchestrator.backend.transfer.ApplicationInstanceGraphTO;
import eu.orchestrator.backend.transfer.ComponentNodeInstanceTO;
import eu.orchestrator.backend.transfer.SSHKeyTO;
import eu.orchestrator.backend.util.ConverterUtil;
import eu.orchestrator.backend.util.Util;
import eu.orchestrator.common.util.NullCheckUtil;
import eu.orchestrator.repository.dao.ComponentNodeInstanceAlertDAO;
import eu.orchestrator.repository.dao.ComponentNodeInstanceDAO;
import eu.orchestrator.repository.dao.ComponentNodeInstanceIPDAO;
import eu.orchestrator.repository.dao.ComponentNodeInstanceStatusDAO;
import eu.orchestrator.repository.dao.ConstraintDAO;
import eu.orchestrator.repository.dao.GraphLinkNodeInstanceDAO;
import eu.orchestrator.repository.dao.InterfaceInstanceDAO;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.Component;
import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.repository.domain.ComponentNodeInstanceAlert;
import eu.orchestrator.repository.domain.ComponentNodeInstanceIP;
import eu.orchestrator.repository.domain.ComponentNodeInstanceStatus;
import eu.orchestrator.repository.domain.Constraint;
import eu.orchestrator.repository.domain.GraphLinkNodeInstance;
import eu.orchestrator.repository.domain.InterfaceInstance;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import jakarta.transaction.Transactional;

@Service
@Transactional(rollbackOn = Exception.class)
public class CommonService {

    @Autowired
    ComponentNodeInstanceDAO componentNodeInstanceDAO;
    @Autowired
    ConstraintDAO constraintDAO;
    @Autowired
    GraphLinkNodeInstanceDAO graphLinkNodeInstanceDAO;
    @Autowired
    InterfaceInstanceDAO interfaceInstanceDAO;
    @Autowired
    ComponentNodeInstanceIPDAO componentNodeInstanceIPDAO;
    @Autowired
    ComponentNodeInstanceAlertDAO componentNodeInstanceAlertDAO;
    @Autowired
    ComponentNodeInstanceStatusDAO componentNodeInstanceStatusDAO;
    @Value("${nfs.server.path}")
    private String rootPathOnServer;

    @Value("${links.showGrafana}")
    private boolean showGrafanaLink;

    @Value("${links.showPrometheus}")
    private boolean showPrometheusLink;

    @Value("${links.showVulnerabilities}")
    private boolean showVulnerabilitiesLink;

    @Value("${links.showKibana}")
    private boolean showKibanaLink;

    public ApplicationInstanceGraphTO convertApplicationInstanceToTO(ApplicationInstance applicationInstance) {

        ApplicationInstanceGraphTO applicationInstanceGraphTO = ConverterUtil.convertApplicationInstanceTo(applicationInstance);

        applicationInstanceGraphTO.setApplication(ConverterUtil.applicationGraphTOFromApplicationInstance(applicationInstance));

        applicationInstanceGraphTO.setProvider(ConverterUtil.providerTOFromApplicationInstance(applicationInstance));

        applicationInstanceGraphTO.setStatus(ApplicationInstance.ApplicationInstanceStatus
                .valueOf(applicationInstance.getStatus()).getFriendlyName());

        List<Constraint> existingConstraints = constraintDAO
                .findAllByApplicationInstance(applicationInstance);

        if (NullCheckUtil.isNotEmpty(existingConstraints)) {
            applicationInstanceGraphTO.setConstraints(ConverterUtil.convertConstraintsToConstraintTO(existingConstraints));
        } else {
            applicationInstanceGraphTO.setConstraints(null);
        }

        List<ComponentNodeInstance> exComponentNodeInstances = componentNodeInstanceDAO
                .findAllByApplicationInstance(applicationInstance);

        if (NullCheckUtil.isNotEmpty(exComponentNodeInstances)) {

            List<ComponentNodeInstanceTO> componentNodeInstanceTOs = new ArrayList<>();
            exComponentNodeInstances.forEach(exComponentNodeInstance ->
                    componentNodeInstanceTOs.add(convertComponentNodeInstanceToTO(exComponentNodeInstance))
            );

            applicationInstanceGraphTO.setComponentNodeInstances(componentNodeInstanceTOs);
        } else {
            applicationInstanceGraphTO.setComponentNodeInstances(null);
        }

        List<GraphLinkNodeInstance> exGraphLinkNodeInstances = graphLinkNodeInstanceDAO
                .findAllByApplicationInstance(applicationInstance);

        if (NullCheckUtil.isNotEmpty(exGraphLinkNodeInstances)) {
            applicationInstanceGraphTO.setGraphLinkNodeInstances(
                    ConverterUtil.convertGraphLinkNodeInstancesToGraphLinkNodeInstanceTO(exGraphLinkNodeInstances));
        } else {
            applicationInstanceGraphTO.setGraphLinkNodeInstances(new ArrayList<>());
        }

        applicationInstanceGraphTO.setShowGrafanaLink(showGrafanaLink);
        applicationInstanceGraphTO.setShowPrometheusLink(showPrometheusLink);
        applicationInstanceGraphTO.setShowVulnerabilitiesLink(showVulnerabilitiesLink);
        applicationInstanceGraphTO.setShowKibanaLink(showKibanaLink);

        return applicationInstanceGraphTO;
    }

    public ComponentNodeInstanceTO convertComponentNodeInstanceToTO(ComponentNodeInstance componentNodeInstance) {

        ComponentNodeInstanceTO componentNodeInstanceTO = ConverterUtil.convertComponentNodeInstanceTo(componentNodeInstance);

        if (NullCheckUtil.isNotEmpty(componentNodeInstance.getLoadBalancedBy())) {
            componentNodeInstanceTO.setLoadBalancedBy(convertComponentNodeInstanceToTO(componentNodeInstance.getLoadBalancedBy()));
        }

        componentNodeInstanceTO.setLoadBalancer(null != componentNodeInstance.getLoadBalancer() && componentNodeInstance.getLoadBalancer());

        componentNodeInstanceTO.setComponentNode(ConverterUtil.componentNodeTOFromComponentInstance(componentNodeInstance));

        if (null != componentNodeInstance.getSshKey()) {
            SSHKeyTO sshKeyTO = new SSHKeyTO();
            BeanUtils.copyProperties(componentNodeInstance.getSshKey(), sshKeyTO);
            componentNodeInstanceTO.setSshKey(sshKeyTO);
        } else {
            componentNodeInstanceTO.setSshKey(null);
        }

        if (null != componentNodeInstance.getKubernetesRuntimeClassName()) {
            componentNodeInstanceTO.setKubernetesRuntimeClassName(componentNodeInstance.getKubernetesRuntimeClassName());
        } else {
            componentNodeInstanceTO.setKubernetesRuntimeClassName(null);
        }

        if (null != componentNodeInstance.getProvider()) {
            componentNodeInstanceTO.setProvider(ConverterUtil.providerTOFromComponentNodeInstance(componentNodeInstance));
        } else {
            componentNodeInstanceTO.setProvider(null);
        }

        componentNodeInstanceTO.setStatusIPS(componentNodeInstance.getStatusIPS());

        Collection<String> collection = new ArrayList<>();
        if (null != componentNodeInstance.getSecurityEnablers() && !componentNodeInstance.getSecurityEnablers().isEmpty()) {
            componentNodeInstance.getSecurityEnablers().stream().forEach(securityEnablers ->
                    collection.add(securityEnablers.getFriendlyName())
            );
        }
        componentNodeInstanceTO.setSecurityEnablers(collection);

        componentNodeInstanceTO.setStatusIDS(componentNodeInstance.getStatusIDS());

        componentNodeInstanceTO.setStatusSOC(componentNodeInstance.getStatusSOC());

        componentNodeInstanceTO.setNetworkModeHost(componentNodeInstance.getNetworkModeHost());

        componentNodeInstanceTO.setPrivilege(componentNodeInstance.getPrivilege());

        componentNodeInstanceTO.setHostname(componentNodeInstance.getHostname());

        componentNodeInstanceTO.setDnsEntry(componentNodeInstance.getDnsEntry());

        componentNodeInstanceTO.setSharedMemorySize(componentNodeInstance.getSharedMemorySize());

        if (null != componentNodeInstance.getCapabilityAdds() && !componentNodeInstance.getCapabilityAdds().isEmpty()) {
            Collection<String> newCapabilities = new ArrayList<>();
            Collection<Component.CapabilityAdd> exCapabilities = componentNodeInstance.getCapabilityAdds();
            exCapabilities.stream().forEach(capability ->
                    newCapabilities.add(capability.getFriendlyName())
            );

            componentNodeInstanceTO.setCapabilityAdds(newCapabilities);
        } else {
            componentNodeInstanceTO.setCapabilityAdds(null);
        }

        // CapabilityDrops
        if (null != componentNodeInstance.getCapabilityDrops() && !componentNodeInstance.getCapabilityDrops().isEmpty()) {
            Collection<String> newCapabilities = new ArrayList<>();
            Collection<Component.CapabilityDrop> exCapabilities = componentNodeInstance.getCapabilityDrops();
            exCapabilities.stream().forEach(capability ->
                    newCapabilities.add(capability.getFriendlyName())
            );

            componentNodeInstanceTO.setCapabilityDrops(newCapabilities);
        } else {
            componentNodeInstanceTO.setCapabilityDrops(null);
        }

        componentNodeInstanceTO.setMinimumWorkers(componentNodeInstance.getMinimumWorkers());
        componentNodeInstanceTO.setMaximumWorkers(componentNodeInstance.getMaximumWorkers());

        componentNodeInstanceTO.setCommand(componentNodeInstance.getCommand());

        List<InterfaceInstance> exInterfaceInstances = interfaceInstanceDAO
                .findAllByComponentNodeInstanceOrderByDateCreatedDesc(componentNodeInstance);

        if (NullCheckUtil.isNotEmpty(exInterfaceInstances)) {
            componentNodeInstanceTO.setInterfaceInstances(ConverterUtil.convertInterfaceInstancesToInterfaceInstancesTO(exInterfaceInstances));
        } else {
            componentNodeInstanceTO.setInterfaceInstances(null);
        }

        if (null != componentNodeInstance.getiDRuleSetInstances() && !componentNodeInstance
                .getInterfaceInstances().isEmpty()) {
            componentNodeInstanceTO.setiDRuleSetInstances(ConverterUtil.idRuleSetInstancesTOFromComponentNodeInstance(componentNodeInstance));
        } else {
            componentNodeInstanceTO.setiDRuleSetInstances(null);
        }

        if (null != componentNodeInstance.getLocationInstances() && !componentNodeInstance
                .getLocationInstances().isEmpty()) {
            componentNodeInstanceTO.setLocationInstances(ConverterUtil.locationInstanceTOFromComponentNodeInstance(componentNodeInstance));
        } else {
            componentNodeInstanceTO.setLocationInstances(null);
        }

        if (null != componentNodeInstance.getFlavorInstance()) {
            componentNodeInstanceTO.setFlavorInstance(ConverterUtil.flavorInstanceTOFromComponentNodeInstance(componentNodeInstance));
        } else {
            componentNodeInstanceTO.setFlavorInstance(null);
        }

        if (null != componentNodeInstance.getHealthCheckInstance()) {
            componentNodeInstanceTO.setHealthCheckInstance(ConverterUtil.healthCheckInstanceTOFromComponentNodeInstance(componentNodeInstance));
        } else {
            componentNodeInstanceTO.setHealthCheckInstance(null);
        }

        if (null != componentNodeInstance.getServerlessPropertiesInstance()) {
            componentNodeInstanceTO.setServerlessPropertiesInstance(ConverterUtil.serverlessPropertiesInstanceInstanceTOFromComponentNodeInstance(componentNodeInstance));
        } else {
            componentNodeInstanceTO.setServerlessPropertiesInstance(null);
        }


        if (null != componentNodeInstance.getPluginInstances() && !componentNodeInstance
                .getPluginInstances().isEmpty()) {
            componentNodeInstanceTO.setPluginInstances(ConverterUtil.pluginInstancesTOFromComponentNodeInstance(componentNodeInstance));
        } else {
            componentNodeInstanceTO.setPluginInstances(null);
        }

        if (null != componentNodeInstance.getEnvironmentalVariableInstances()
                && !componentNodeInstance.getEnvironmentalVariableInstances().isEmpty()) {
            componentNodeInstanceTO.setEnvironmentalVariableInstances(
                    ConverterUtil.environmentalVariableInstancesTOFromComponentNodeInstance(componentNodeInstance));
        } else {
            componentNodeInstanceTO.setEnvironmentalVariableInstances(null);
        }

        if (null != componentNodeInstance.getDeviceInstances() && !componentNodeInstance
                .getDeviceInstances().isEmpty()) {
            componentNodeInstanceTO.setDeviceInstances(ConverterUtil.deviceInstanceTOFromComponentNodeInstance(componentNodeInstance));
        } else {
            componentNodeInstanceTO.setDeviceInstances(null);
        }

        if (null != componentNodeInstance.getVolumeInstances() && !componentNodeInstance
                .getVolumeInstances().isEmpty()) {
            componentNodeInstanceTO.setVolumeInstances(ConverterUtil.volumeInstanceTOFromComponentNodeInstance(componentNodeInstance, rootPathOnServer));
        } else {
            componentNodeInstanceTO.setVolumeInstances(null);
        }

        List<ComponentNodeInstanceIP> componentNodeInstanceIPs = componentNodeInstanceIPDAO
                .findAllByComponentNodeInstanceOrderByDateCreatedDesc(componentNodeInstance);

        if (NullCheckUtil.isNotEmpty(componentNodeInstanceIPs)) {
            componentNodeInstanceTO.setComponentNodeInstanceIPs(ConverterUtil.componentNodeInstanceIPsToIpsTO(componentNodeInstanceIPs, componentNodeInstance));
        } else {
            componentNodeInstanceTO.setComponentNodeInstanceIPs(null);
        }

        List<ComponentNodeInstanceAlert> componentNodeInstanceAlerts = componentNodeInstanceAlertDAO
                .findAllByComponentNodeInstanceOrderByDateCreatedDesc(componentNodeInstance);

        if (NullCheckUtil.isNotEmpty(componentNodeInstanceAlerts)) {
            componentNodeInstanceTO.setComponentNodeInstanceAlerts(
                    ConverterUtil.componentNodeInstanceAlertsToAlertsTO(componentNodeInstanceAlerts, componentNodeInstance));
        } else {
            componentNodeInstanceTO.setComponentNodeInstanceAlerts(null);
        }

        List<ComponentNodeInstanceStatus> componentNodeInstanceStatuses = componentNodeInstanceStatusDAO
                .findAllByComponentNodeInstanceOrderByDateCreatedDesc(componentNodeInstance);

        if (NullCheckUtil.isNotEmpty(componentNodeInstanceStatuses)) {
            componentNodeInstanceTO.setComponentNodeInstanceStatuses(
                    ConverterUtil.componentNodeInstanceStatusesToStatusesTO(componentNodeInstanceStatuses, componentNodeInstance));
        } else {
            componentNodeInstanceTO.setComponentNodeInstanceStatuses(null);
        }

        return componentNodeInstanceTO;
    }

}
