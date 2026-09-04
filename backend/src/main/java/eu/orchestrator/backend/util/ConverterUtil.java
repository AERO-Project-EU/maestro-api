package eu.orchestrator.backend.util;

import eu.orchestrator.backend.transfer.*;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.ComponentNode;
import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.repository.domain.ComponentNodeInstanceAlert;
import eu.orchestrator.repository.domain.ComponentNodeInstanceIP;
import eu.orchestrator.repository.domain.ComponentNodeInstanceStatus;
import eu.orchestrator.repository.domain.Constraint;
import eu.orchestrator.repository.domain.GraphLinkNode;
import eu.orchestrator.repository.domain.GraphLinkNodeInstance;
import eu.orchestrator.repository.domain.Interface;
import eu.orchestrator.repository.domain.InterfaceInstance;
import eu.orchestrator.repository.domain.QI.ResourceType;

import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

public class ConverterUtil {

    private ConverterUtil() {
    }

    public static ApplicationInstanceGraphTO convertApplicationInstanceTo(ApplicationInstance applicationInstance) {
        ApplicationInstanceGraphTO applicationInstanceGraphTO = new ApplicationInstanceGraphTO();
        applicationInstanceGraphTO.setName(applicationInstance.getName());
        applicationInstanceGraphTO
                .setApplicationInstanceID(applicationInstance.getApplicationInstanceID());
        applicationInstanceGraphTO.setHexID(applicationInstance.getHexID());
        applicationInstanceGraphTO.setDescription(
                null != applicationInstance.getDescription() && !applicationInstance
                        .getDescription().isEmpty() ? applicationInstance.getDescription() : null);
        applicationInstanceGraphTO.setOverlay(applicationInstance.getOverlay());
        applicationInstanceGraphTO
                .setDeploymentTimestamp(applicationInstance.getDeploymentTimestamp());

        applicationInstanceGraphTO.setGrafanaUUID(applicationInstance.getGrafanaDashboardUUID());

        applicationInstanceGraphTO.setDateDeployed(applicationInstance.getDateDeployed());

        return applicationInstanceGraphTO;
    }

    public static ApplicationGraphTO applicationGraphTOFromApplicationInstance(ApplicationInstance applicationInstance) {

        // Create ApplicationGraphTO
        ApplicationGraphTO applicationGraphTO = new ApplicationGraphTO();
        applicationGraphTO.setId(applicationInstance.getApplication().getId());
        applicationGraphTO.setHexID(applicationInstance.getApplication().getHexID());
        applicationGraphTO.setName(applicationInstance.getApplication().getName());
        applicationGraphTO
                .setPublicApplication(applicationInstance.getApplication().getPublicApplication());

        List<ComponentNodeTO> componentNodeTOs = new ArrayList<>();
        for (ComponentNode componentNode : applicationInstance.getApplication()
                .getComponentNodes()) {

            ComponentNodeTO componentNodeTO = new ComponentNodeTO();
            componentNodeTO.setComponentNodeID(componentNode.getComponentNodeID());
            componentNodeTO.setHexID(componentNode.getHexID());
            componentNodeTO.setName(componentNode.getName());

            ComponentTO componentTO = new ComponentTO();
            componentTO.setId(componentNode.getComponent().getId());
            componentTO.setName(componentNode.getComponent().getName());
            componentTO.setHexID(componentNode.getComponent().getHexID());
            componentTO.setPublicComponent(componentNode.getComponent().getPublicComponent());

            List<InterfaceTO> exposedInterfacesTO;
            if (null != componentNode.getComponent().getExposedInterfaces() && !componentNode
                    .getComponent().getExposedInterfaces().isEmpty()) {
                exposedInterfacesTO = new ArrayList<>();

                for (Interface expInterface : componentNode.getComponent().getExposedInterfaces()) {

                    InterfaceTO interfaceTO = new InterfaceTO();
                    interfaceTO.setInterfaceID(expInterface.getInterfaceID());
                    interfaceTO.setName(expInterface.getName());
                    interfaceTO.setPort(expInterface.getPort());
                    interfaceTO.setInterfaceType(expInterface.getInterfaceType());
                    interfaceTO.setTransmissionProtocol(expInterface.getTransmissionProtocol());
                    exposedInterfacesTO.add(interfaceTO);

                }

            } else {
                exposedInterfacesTO = null;
            }

            componentTO.setExposedInterfaces(exposedInterfacesTO);

            componentNodeTO.setComponent(componentTO);
            componentNodeTOs.add(componentNodeTO);
        }
        applicationGraphTO.setComponentNodes(componentNodeTOs);

        if (null != applicationInstance.getApplication().getGraphLinkNodes()
                && !applicationInstance.getApplication()
                .getGraphLinkNodes()
                .isEmpty()) {
            List<GraphLinkNodeTO> graphLinkNodeTOs = new ArrayList<>();
            for (GraphLinkNode graphLinkNode : applicationInstance.getApplication()
                    .getGraphLinkNodes()) {

                GraphLinkNodeTO graphLinkNodeTO = new GraphLinkNodeTO();
                graphLinkNodeTO.setGraphLinkNodeID(graphLinkNode.getGraphLinkNodeID());

                GraphLinkTO graphLinkTO = new GraphLinkTO();
                graphLinkTO.setGraphLinkID(graphLinkNode.getGraphLink().getGraphLinkID());
                graphLinkTO.setFriendlyName(graphLinkNode.getGraphLink().getFriendlyName());

                InterfaceTO interfaceTO = new InterfaceTO();
                interfaceTO
                        .setInterfaceID(
                                graphLinkNode.getGraphLink().getInterfaceObj().getInterfaceID());
                interfaceTO.setTransmissionProtocol(
                        graphLinkNode.getGraphLink().getInterfaceObj().getTransmissionProtocol());
                interfaceTO.setPort(graphLinkNode.getGraphLink().getInterfaceObj().getPort());
                interfaceTO.setInterfaceType(
                        graphLinkNode.getGraphLink().getInterfaceObj().getInterfaceType());
                interfaceTO.setName(graphLinkNode.getGraphLink().getInterfaceObj().getName());
                graphLinkTO.setInterfaceObj(interfaceTO);

                graphLinkNodeTO.setGraphLink(graphLinkTO);

                ComponentNodeTO componentNodeFromTO = new ComponentNodeTO();
                componentNodeFromTO
                        .setComponentNodeID(graphLinkNode.getComponentNodeFrom().getComponentNodeID());
                componentNodeFromTO.setHexID(graphLinkNode.getComponentNodeFrom().getHexID());
                componentNodeFromTO.setName(graphLinkNode.getComponentNodeFrom().getName());

                ComponentTO componentFromTO = new ComponentTO();
                componentFromTO.setId(graphLinkNode.getComponentNodeFrom().getComponent().getId());
                componentFromTO
                        .setName(graphLinkNode.getComponentNodeFrom().getComponent().getName());
                componentFromTO
                        .setHexID(graphLinkNode.getComponentNodeFrom().getComponent().getHexID());
                componentFromTO.setPublicComponent(
                        graphLinkNode.getComponentNodeFrom().getComponent().getPublicComponent());
                componentNodeFromTO.setComponent(componentFromTO);

                graphLinkNodeTO.setComponentNodeFrom(componentNodeFromTO);

                ComponentNodeTO componentNodeToTO = new ComponentNodeTO();
                componentNodeToTO
                        .setComponentNodeID(graphLinkNode.getComponentNodeTo().getComponentNodeID());
                componentNodeToTO.setHexID(graphLinkNode.getComponentNodeTo().getHexID());
                componentNodeToTO.setName(graphLinkNode.getComponentNodeTo().getName());

                ComponentTO componentToTO = new ComponentTO();
                componentToTO.setId(graphLinkNode.getComponentNodeTo().getComponent().getId());
                componentToTO.setName(graphLinkNode.getComponentNodeTo().getComponent().getName());
                componentToTO
                        .setHexID(graphLinkNode.getComponentNodeTo().getComponent().getHexID());
                componentToTO.setPublicComponent(
                        graphLinkNode.getComponentNodeTo().getComponent().getPublicComponent());
                componentNodeToTO.setComponent(componentToTO);

                graphLinkNodeTO.setComponentNodeTo(componentNodeToTO);

                graphLinkNodeTOs.add(graphLinkNodeTO);
            }

            applicationGraphTO.setGraphLinkNodes(graphLinkNodeTOs);
        } else {
            applicationGraphTO.setGraphLinkNodes(null);
        }

        return applicationGraphTO;
    }

    public static ProviderTO providerTOFromApplicationInstance(ApplicationInstance applicationInstance) {

        ProviderTO providerTO = new ProviderTO();
        providerTO.setProviderID(applicationInstance.getProvider().getProviderID());
        providerTO.setName(applicationInstance.getProvider().getName());
        providerTO.setDefaultProvider(applicationInstance.getProvider().getDefaultProvider());
        providerTO.setProviderType(
                new ProviderTypeTO(applicationInstance.getProvider().getProviderType().getId(),
                        applicationInstance.getProvider().getProviderType().getName(),
                        applicationInstance.getProvider().getProviderType().getFriendlyName()));

        return providerTO;
    }

    public static List<ConstraintTO> convertConstraintsToConstraintTO(List<Constraint> existingConstraints) {

        List<ConstraintTO> constraintTOs = new ArrayList<>();

        existingConstraints.forEach(exConstraint -> {

            ConstraintTO constraintTO = new ConstraintTO();
            BeanUtils.copyProperties(exConstraint, constraintTO);

            // Copy extra TO Fields RadioServiceTypeTO, CountryTO, QITO
            if (null != exConstraint.getRadioServiceType()) {
                RadioServiceTypeTO radioServiceTypeTO = new RadioServiceTypeTO();
                BeanUtils.copyProperties(exConstraint.getRadioServiceType(), radioServiceTypeTO);
                constraintTO.setRadioServiceType(radioServiceTypeTO);
            }

            if (null != exConstraint.getCountry()) {
                CountryTO countryTO = new CountryTO();
                BeanUtils.copyProperties(exConstraint.getCountry(), countryTO);
                constraintTO.setCountry(countryTO);
            }

            if (null != exConstraint.getQi()) {
                QITO qiTO = new QITO();
                qiTO.setId(exConstraint.getQi().getId());
                qiTO.setDateCreated(exConstraint.getQi().getDateCreated());
                qiTO.setQiValue(exConstraint.getQi().getQiValue());
                qiTO.setDefaultPriorityLevel(exConstraint.getQi().getDefaultPriorityLevel());
                qiTO.setResourceType(ResourceType.valueOf(exConstraint.getQi().getResourceType())
                        .getFriendlyName());
                qiTO.setPacketDelayBudget(
                        String.valueOf(exConstraint.getQi().getPacketDelayBudget()));
                qiTO.setPacketErrorRate(
                        String.valueOf(exConstraint.getQi().getPacketErrorRate()));
                constraintTO.setQi(qiTO);
            }

            constraintTOs.add(constraintTO);

        });

        return constraintTOs;

    }

    public static List<GraphLinkNodeInstanceTO> convertGraphLinkNodeInstancesToGraphLinkNodeInstanceTO(List<GraphLinkNodeInstance> exGraphLinkNodeInstances) {

        List<GraphLinkNodeInstanceTO> graphLinkNodeInstanceTOs = new ArrayList<>();

        exGraphLinkNodeInstances.stream().forEach(exGraphLinkNodeInstance -> {

            GraphLinkNodeInstanceTO graphLinkNodeInstanceTO = new GraphLinkNodeInstanceTO();
            graphLinkNodeInstanceTO.setGraphLinkNodeInstanceID(
                    exGraphLinkNodeInstance.getGraphLinkNodeInstanceID());

            // GraphLinkNode
            if (null != exGraphLinkNodeInstance.getGraphLinkNode()) {
                GraphLinkNodeTO graphLinkNodeTO = new GraphLinkNodeTO();
                graphLinkNodeTO.setGraphLinkNodeID(
                        exGraphLinkNodeInstance.getGraphLinkNode().getGraphLinkNodeID());

                GraphLinkTO graphLinkTO = new GraphLinkTO();
                graphLinkTO.setGraphLinkID(
                        exGraphLinkNodeInstance.getGraphLinkNode().getGraphLink().getGraphLinkID());
                graphLinkTO.setFriendlyName(
                        exGraphLinkNodeInstance.getGraphLinkNode().getGraphLink().getFriendlyName());

                InterfaceTO interfaceTO = new InterfaceTO();
                interfaceTO
                        .setInterfaceID(
                                exGraphLinkNodeInstance.getGraphLinkNode().getGraphLink().getInterfaceObj()
                                        .getInterfaceID());
                interfaceTO.setTransmissionProtocol(
                        exGraphLinkNodeInstance.getGraphLinkNode().getGraphLink().getInterfaceObj()
                                .getTransmissionProtocol());
                interfaceTO.setPort(
                        exGraphLinkNodeInstance.getGraphLinkNode().getGraphLink().getInterfaceObj()
                                .getPort());
                interfaceTO.setInterfaceType(
                        exGraphLinkNodeInstance.getGraphLinkNode().getGraphLink().getInterfaceObj()
                                .getInterfaceType());
                interfaceTO.setName(
                        exGraphLinkNodeInstance.getGraphLinkNode().getGraphLink().getInterfaceObj()
                                .getName());
                graphLinkTO.setInterfaceObj(interfaceTO);

                graphLinkNodeTO.setGraphLink(graphLinkTO);

                ComponentNodeTO componentNodeFromTO = new ComponentNodeTO();
                componentNodeFromTO
                        .setComponentNodeID(
                                exGraphLinkNodeInstance.getGraphLinkNode().getComponentNodeFrom()
                                        .getComponentNodeID());
                componentNodeFromTO.setHexID(
                        exGraphLinkNodeInstance.getGraphLinkNode().getComponentNodeFrom().getHexID());
                componentNodeFromTO.setName(
                        exGraphLinkNodeInstance.getGraphLinkNode().getComponentNodeFrom().getName());

                ComponentTO componentFromTO = new ComponentTO();
                componentFromTO.setId(
                        exGraphLinkNodeInstance.getGraphLinkNode().getComponentNodeFrom().getComponent()
                                .getId());
                componentFromTO
                        .setName(exGraphLinkNodeInstance.getGraphLinkNode().getComponentNodeFrom()
                                .getComponent().getName());
                componentFromTO
                        .setHexID(exGraphLinkNodeInstance.getGraphLinkNode().getComponentNodeFrom()
                                .getComponent().getHexID());
                componentFromTO.setPublicComponent(
                        exGraphLinkNodeInstance.getGraphLinkNode().getComponentNodeFrom().getComponent()
                                .getPublicComponent());
                componentNodeFromTO.setComponent(componentFromTO);

                graphLinkNodeTO.setComponentNodeFrom(componentNodeFromTO);

                ComponentNodeTO componentNodeToTO = new ComponentNodeTO();
                componentNodeToTO
                        .setComponentNodeID(
                                exGraphLinkNodeInstance.getGraphLinkNode().getComponentNodeTo()
                                        .getComponentNodeID());
                componentNodeToTO.setHexID(
                        exGraphLinkNodeInstance.getGraphLinkNode().getComponentNodeTo().getHexID());
                componentNodeToTO.setName(
                        exGraphLinkNodeInstance.getGraphLinkNode().getComponentNodeTo().getName());

                ComponentTO componentToTO = new ComponentTO();
                componentToTO.setId(
                        exGraphLinkNodeInstance.getGraphLinkNode().getComponentNodeTo().getComponent()
                                .getId());
                componentToTO.setName(
                        exGraphLinkNodeInstance.getGraphLinkNode().getComponentNodeTo().getComponent()
                                .getName());
                componentToTO.setHexID(
                        exGraphLinkNodeInstance.getGraphLinkNode().getComponentNodeTo().getComponent()
                                .getHexID());
                componentToTO.setPublicComponent(
                        exGraphLinkNodeInstance.getGraphLinkNode().getComponentNodeTo().getComponent()
                                .getPublicComponent());
                componentNodeToTO.setComponent(componentToTO);

                graphLinkNodeTO.setComponentNodeTo(componentNodeToTO);

                graphLinkNodeInstanceTO.setGraphLinkNode(graphLinkNodeTO);

            }

            ComponentNodeInstanceTO componentNodeInstanceFromTO = new ComponentNodeInstanceTO();
            componentNodeInstanceFromTO.setComponentNodeInstanceID(
                    exGraphLinkNodeInstance.getComponentNodeInstanceFrom()
                            .getComponentNodeInstanceID());
            graphLinkNodeInstanceTO.setComponentNodeInstanceFrom(componentNodeInstanceFromTO);
            ComponentNodeInstanceTO componentNodeInstanceToTO = new ComponentNodeInstanceTO();
            componentNodeInstanceToTO.setComponentNodeInstanceID(
                    exGraphLinkNodeInstance.getComponentNodeInstanceTo()
                            .getComponentNodeInstanceID());
            graphLinkNodeInstanceTO.setComponentNodeInstanceTo(componentNodeInstanceToTO);

            graphLinkNodeInstanceTOs.add(graphLinkNodeInstanceTO);

        });

        return graphLinkNodeInstanceTOs;
    }

    public static ComponentNodeInstanceTO convertComponentNodeInstanceTo(ComponentNodeInstance componentNodeInstance) {

        ComponentNodeInstanceTO componentNodeInstanceTO = new ComponentNodeInstanceTO();
        componentNodeInstanceTO.setComponentNodeInstanceID(
                componentNodeInstance.getComponentNodeInstanceID());
        componentNodeInstanceTO.setName(componentNodeInstance.getName());
        componentNodeInstanceTO.setHexID(componentNodeInstance.getHexID());

        return componentNodeInstanceTO;
    }

    public static ComponentNodeTO componentNodeTOFromComponentInstance(ComponentNodeInstance componentNodeInstance) {

        ComponentNodeTO componentNodeTO = new ComponentNodeTO();
        componentNodeTO.setComponentNodeID(
                componentNodeInstance.getComponentNode().getComponentNodeID());
        componentNodeTO.setHexID(componentNodeInstance.getComponentNode().getHexID());
        componentNodeTO.setName(componentNodeInstance.getComponentNode().getName());

        ComponentTO componentTO = new ComponentTO();
        componentTO
                .setId(componentNodeInstance.getComponentNode().getComponent().getId());
        componentTO
                .setName(componentNodeInstance.getComponentNode().getComponent().getName());
        componentTO
                .setHexID(componentNodeInstance.getComponentNode().getComponent().getHexID());
        componentTO.setPublicComponent(
                componentNodeInstance.getComponentNode().getComponent().getPublicComponent());
        componentTO
                .setElasticityController(componentNodeInstance.getComponentNode().getComponent().getElasticityController());

        List<InterfaceTO> exposedInterfacesTO;
        if (null != componentNodeInstance.getComponentNode().getComponent()
                .getExposedInterfaces() && !componentNodeInstance.getComponentNode()
                .getComponent().getExposedInterfaces().isEmpty()) {
            exposedInterfacesTO = new ArrayList<>();

            for (Interface expInterface : componentNodeInstance.getComponentNode()
                    .getComponent().getExposedInterfaces()) {

                InterfaceTO interfaceTO = new InterfaceTO();
                interfaceTO.setInterfaceID(expInterface.getInterfaceID());
                interfaceTO.setName(expInterface.getName());
                interfaceTO.setPort(expInterface.getPort());
                interfaceTO.setInterfaceType(expInterface.getInterfaceType());
                interfaceTO.setTransmissionProtocol(expInterface.getTransmissionProtocol());
                exposedInterfacesTO.add(interfaceTO);

            }

        } else {
            exposedInterfacesTO = null;
        }
        componentTO.setExposedInterfaces(exposedInterfacesTO);
        componentNodeTO.setComponent(componentTO);

        return componentNodeTO;
    }

    public static ProviderTO providerTOFromComponentNodeInstance(ComponentNodeInstance componentNodeInstance) {

        ProviderTO cniProviderTO = new ProviderTO();
        cniProviderTO
                .setProviderID(componentNodeInstance.getProvider().getProviderID());
        cniProviderTO.setName(componentNodeInstance.getProvider().getName());
        cniProviderTO.setDefaultProvider(
                componentNodeInstance.getProvider().getDefaultProvider());
        cniProviderTO.setProviderType(
                new ProviderTypeTO(
                        componentNodeInstance.getProvider().getProviderType().getId(),
                        componentNodeInstance.getProvider().getProviderType().getName(),
                        componentNodeInstance.getProvider().getProviderType()
                                .getFriendlyName()));

        return cniProviderTO;
    }

    public static List<InterfaceInstanceTO> convertInterfaceInstancesToInterfaceInstancesTO(List<InterfaceInstance> exInterfaceInstances) {

        List<InterfaceInstanceTO> interfaceInstanceTOs = new ArrayList<>();

        exInterfaceInstances.stream().forEach(exInterfaceInstance -> {

            InterfaceInstanceTO interfaceInstanceTO = new InterfaceInstanceTO();
            BeanUtils.copyProperties(exInterfaceInstance, interfaceInstanceTO);

            // Copy extra field InterfaceTO
            InterfaceTO interfaceTO = new InterfaceTO();
            interfaceTO
                    .setInterfaceID(exInterfaceInstance.getInterfaceObj().getInterfaceID());
            interfaceTO.setName(exInterfaceInstance.getInterfaceObj().getName());
            interfaceTO.setPort(exInterfaceInstance.getInterfaceObj().getPort());
            interfaceTO
                    .setInterfaceType(exInterfaceInstance.getInterfaceObj().getInterfaceType());
            interfaceTO.setTransmissionProtocol(
                    exInterfaceInstance.getInterfaceObj().getTransmissionProtocol());

            interfaceInstanceTO.setInterfaceObj(interfaceTO);

            interfaceInstanceTOs.add(interfaceInstanceTO);
        });

        return interfaceInstanceTOs;
    }

    public static List<IDRuleSetInstanceTO> idRuleSetInstancesTOFromComponentNodeInstance(ComponentNodeInstance componentNodeInstance) {

        List<IDRuleSetInstanceTO> idRuleSetInstanceTOs = new ArrayList<>();

        componentNodeInstance.getiDRuleSetInstances().stream().forEach(idRuleSetInstance -> {

            IDRuleSetInstanceTO idRuleSetInstanceTO = new IDRuleSetInstanceTO();
            idRuleSetInstanceTO.setRuleSetInstanceID(idRuleSetInstance.getRuleSetInstanceID());
            idRuleSetInstanceTO.setName(idRuleSetInstance.getName());
            idRuleSetInstanceTO.setIdRuleSetID(idRuleSetInstance.getIdRuleSet().getId());
            IDRuleSetTO idRuleSetTO = new IDRuleSetTO();
            idRuleSetTO.setId(idRuleSetInstance.getIdRuleSet().getId());
            idRuleSetTO.setName(idRuleSetInstance.getIdRuleSet().getName());
            idRuleSetTO.setPublicIDRuleSet(idRuleSetInstance.getIdRuleSet().getPublicIDRuleSet());
            idRuleSetInstanceTO.setIdRuleSet(idRuleSetTO);
            idRuleSetInstanceTOs.add(idRuleSetInstanceTO);

        });

        return idRuleSetInstanceTOs;
    }

    public static List<LocationInstanceTO> locationInstanceTOFromComponentNodeInstance(ComponentNodeInstance componentNodeInstance) {

        List<LocationInstanceTO> locationInstanceTOs = new ArrayList<>();

        componentNodeInstance.getLocationInstances().stream().forEach(exLocationInstance -> {

            LocationInstanceTO locationInstanceTO = new LocationInstanceTO();
            locationInstanceTO.setLocationInstanceID(exLocationInstance.getLocationInstanceID());
            locationInstanceTO.setRegion(exLocationInstance.getRegion());
            if (null != exLocationInstance.getCountry()) {
                CountryTO countryTO = new CountryTO();
                countryTO.setId(exLocationInstance.getCountry().getId());
                countryTO.setName(exLocationInstance.getCountry().getName());
                locationInstanceTO.setCountry(countryTO);
            }

            locationInstanceTOs.add(locationInstanceTO);

        });

        return locationInstanceTOs;
    }

    public static FlavorInstanceTO flavorInstanceTOFromComponentNodeInstance(ComponentNodeInstance componentNodeInstance) {

        FlavorInstanceTO flavorInstanceTO = new FlavorInstanceTO();
        flavorInstanceTO.setFlavorID(componentNodeInstance.getFlavorInstance().getFlavorID());
        flavorInstanceTO.setRam(componentNodeInstance.getFlavorInstance().getRam());
        flavorInstanceTO.setStorage(componentNodeInstance.getFlavorInstance().getStorage());
        flavorInstanceTO.setvCPUs(componentNodeInstance.getFlavorInstance().getvCPUs());
        flavorInstanceTO.setServerlessEnabled(componentNodeInstance.getFlavorInstance().getServerlessEnabled());

        return flavorInstanceTO;

    }

    public static HealthCheckInstanceTO healthCheckInstanceTOFromComponentNodeInstance(ComponentNodeInstance componentNodeInstance) {

        HealthCheckInstanceTO healthCheckInstanceTO = new HealthCheckInstanceTO();
        healthCheckInstanceTO.setHealthCheckInstanceID(
                componentNodeInstance.getHealthCheckInstance().getHealthCheckInstanceID());
        healthCheckInstanceTO.setArgs(
                null != componentNodeInstance.getHealthCheckInstance().getArgs()
                        && !componentNodeInstance.getHealthCheckInstance().getArgs().isEmpty() ?
                        componentNodeInstance.getHealthCheckInstance().getArgs() : null);
        healthCheckInstanceTO
                .setInterval(componentNodeInstance.getHealthCheckInstance().getInterval());
        healthCheckInstanceTO.setHttpURL(
                null != componentNodeInstance.getHealthCheckInstance().getHttpURL()
                        && !componentNodeInstance.getHealthCheckInstance().getHttpURL().isEmpty() ?
                        componentNodeInstance.getHealthCheckInstance().getHttpURL() : null);

        HealthCheckTO healthCheckTO = new HealthCheckTO();
        healthCheckTO.setHealthCheckID(
                componentNodeInstance.getHealthCheckInstance().getHealthCheck().getHealthCheckID());
        healthCheckTO.setInterval(
                componentNodeInstance.getHealthCheckInstance().getHealthCheck().getInterval());
        healthCheckTO.setArgs(
                null != componentNodeInstance.getHealthCheckInstance().getHealthCheck().getArgs()
                        && !componentNodeInstance.getHealthCheckInstance().getHealthCheck().getArgs()
                        .isEmpty() ?
                        componentNodeInstance.getHealthCheckInstance().getHealthCheck().getArgs()
                        : null);
        healthCheckTO.setHttpURL(
                null != componentNodeInstance.getHealthCheckInstance().getHealthCheck().getHttpURL()
                        && !componentNodeInstance.getHealthCheckInstance().getHealthCheck().getHttpURL()
                        .isEmpty() ?
                        componentNodeInstance.getHealthCheckInstance().getHealthCheck().getHttpURL()
                        : null);
        healthCheckInstanceTO.setHealthCheck(healthCheckTO);

        return healthCheckInstanceTO;
    }

    public static ServerlessPropertiesInstanceTO serverlessPropertiesInstanceInstanceTOFromComponentNodeInstance(ComponentNodeInstance componentNodeInstance) {

        ServerlessPropertiesInstanceTO serverlessPropertiesInstanceTO = new ServerlessPropertiesInstanceTO();

        serverlessPropertiesInstanceTO.setServerlessPropertiesInstanceID(componentNodeInstance.getServerlessPropertiesInstance().getServerlessPropertiesInstanceID());

        ServerlessPropertiesTO serverlessPropertiesTO = new ServerlessPropertiesTO();
        serverlessPropertiesTO.setServerlessPropertiesID(
                componentNodeInstance.getServerlessPropertiesInstance().getServerlessProperties().getServerlessPropertiesID());

        serverlessPropertiesTO.setAutoscaler(componentNodeInstance.getServerlessPropertiesInstance().getServerlessProperties().getAutoscaler());
        serverlessPropertiesTO.setMetric(componentNodeInstance.getServerlessPropertiesInstance().getServerlessProperties().getMetric());
        serverlessPropertiesTO.setTargetValue(componentNodeInstance.getServerlessPropertiesInstance().getServerlessProperties().getTargetValue());
        serverlessPropertiesTO.setWindowSize(componentNodeInstance.getServerlessPropertiesInstance().getServerlessProperties().getWindowSize());
        serverlessPropertiesTO.setMinScale(componentNodeInstance.getServerlessPropertiesInstance().getServerlessProperties().getMinScale());
        serverlessPropertiesTO.setMaxScale(componentNodeInstance.getServerlessPropertiesInstance().getServerlessProperties().getMaxScale());


        serverlessPropertiesInstanceTO.setServerlessProperties(serverlessPropertiesTO);

        serverlessPropertiesInstanceTO.setAutoscaler(componentNodeInstance.getServerlessPropertiesInstance().getAutoscaler());
        serverlessPropertiesInstanceTO.setMetric(componentNodeInstance.getServerlessPropertiesInstance().getMetric());
        serverlessPropertiesInstanceTO.setTargetValue(componentNodeInstance.getServerlessPropertiesInstance().getTargetValue());
        serverlessPropertiesInstanceTO.setWindowSize(componentNodeInstance.getServerlessPropertiesInstance().getWindowSize());
        serverlessPropertiesInstanceTO.setMinScale(componentNodeInstance.getServerlessPropertiesInstance().getMinScale());
        serverlessPropertiesInstanceTO.setMaxScale(componentNodeInstance.getServerlessPropertiesInstance().getMaxScale());

        return serverlessPropertiesInstanceTO;
    }

    public static List<PluginInstanceTO> pluginInstancesTOFromComponentNodeInstance(ComponentNodeInstance componentNodeInstance) {

        List<PluginInstanceTO> pluginInstanceTOs = new ArrayList<>();

        componentNodeInstance.getPluginInstances().stream()
                .filter(pluginInstance -> !pluginInstance.getImmutablePlugin().booleanValue())
                .forEach(pluginInstance -> {

                    PluginInstanceTO pluginInstanceTO = new PluginInstanceTO();
                    pluginInstanceTO.setPluginInstanceID(pluginInstance.getPluginInstanceID());
                    pluginInstanceTO.setDeletedPlugin(pluginInstance.getDeletedPlugin());
                    pluginInstanceTO.setImmutablePlugin(pluginInstance.getImmutablePlugin());
                    pluginInstanceTO.setName(pluginInstance.getName());
                    pluginInstanceTO.setModuleName(
                            null != pluginInstance.getModuleName() && !pluginInstance.getModuleName()
                                    .isEmpty() ? pluginInstance.getModuleName() : null);

                    PluginTO pluginTO = new PluginTO();
                    pluginTO.setPluginID(pluginInstance.getPlugin().getPluginID());

                    pluginInstanceTO.setPlugin(pluginTO);

                    pluginInstanceTOs.add(pluginInstanceTO);


                });

        return pluginInstanceTOs;
    }

    public static List<EnvironmentalVariableInstanceTO> environmentalVariableInstancesTOFromComponentNodeInstance(ComponentNodeInstance componentNodeInstance) {

        List<EnvironmentalVariableInstanceTO> environmentalVariableInstanceTOs = new ArrayList<>();

        componentNodeInstance.getEnvironmentalVariableInstances().stream()
                .forEach(environmentalVariableInstance -> {

                    EnvironmentalVariableInstanceTO environmentalVariableInstanceTO = new EnvironmentalVariableInstanceTO();
                    environmentalVariableInstanceTO.setEnvironmentalVariableInstanceID(
                            environmentalVariableInstance.getEnvironmentalVariableInstanceID());
                    environmentalVariableInstanceTO.setKey(environmentalVariableInstance.getKey());
                    environmentalVariableInstanceTO
                            .setValue(environmentalVariableInstance.getValue());

                    EnvironmentalVariableTO environmentalVariableTO = new EnvironmentalVariableTO();
                    environmentalVariableTO.setEnvironmentalVariableID(
                            environmentalVariableInstance.getEnvironmentalVariable()
                                    .getEnvironmentalVariableID());
                    environmentalVariableTO
                            .setKey(environmentalVariableInstance.getEnvironmentalVariable().getKey());
                    environmentalVariableTO.setValue(
                            environmentalVariableInstance.getEnvironmentalVariable().getValue());

                    environmentalVariableInstanceTO.setEnvironmentalVariable(environmentalVariableTO);

                    environmentalVariableInstanceTOs.add(environmentalVariableInstanceTO);
                });

        return environmentalVariableInstanceTOs;
    }

    public static List<DeviceInstanceTO> deviceInstanceTOFromComponentNodeInstance(ComponentNodeInstance componentNodeInstance) {

        List<DeviceInstanceTO> deviceInstanceTOs = new ArrayList<>();

        componentNodeInstance.getDeviceInstances().stream().forEach(deviceInstance -> {

            DeviceInstanceTO deviceInstanceTO = new DeviceInstanceTO();
            deviceInstanceTO.setDeviceInstanceID(deviceInstance.getDeviceInstanceID());
            deviceInstanceTO.setKey(deviceInstance.getKey());
            deviceInstanceTO.setValue(deviceInstance.getValue());

            DeviceTO deviceTO = new DeviceTO();
            deviceTO.setDeviceID(deviceInstance.getDevice().getDeviceID());
            deviceTO.setKey(deviceInstance.getDevice().getKey());
            deviceTO.setValue(deviceInstance.getDevice().getValue());
            deviceInstanceTO.setDevice(deviceTO);

            deviceInstanceTOs.add(deviceInstanceTO);
        });

        return deviceInstanceTOs;
    }

    public static List<VolumeInstanceTO> volumeInstanceTOFromComponentNodeInstance(ComponentNodeInstance componentNodeInstance, String rootPath) {

        List<VolumeInstanceTO> volumeInstanceTOs = new ArrayList<>();

        componentNodeInstance.getVolumeInstances().stream().forEach(volumeInstance -> {

            VolumeInstanceTO volumeInstanceTO = new VolumeInstanceTO();
            volumeInstanceTO.setVolumeInstanceID(volumeInstance.getVolumeInstanceID());

            String removePath = rootPath + "/" + volumeInstance.getComponentNodeInstance().getComponentNode().getComponent().getOrganization().getName();
            volumeInstanceTO.setHostPath(volumeInstance.getHostPath().replace(removePath, ""));
            volumeInstanceTO.setDockerPath(volumeInstance.getDockerPath());
            VolumeTO volumeTO = new VolumeTO();
            volumeTO.setVolumeID(volumeInstance.getVolume().getVolumeID());
            volumeTO.setDockerPath(volumeInstance.getVolume().getDockerPath());
            volumeInstanceTO.setVolume(volumeTO);

            volumeInstanceTOs.add(volumeInstanceTO);
        });

        return volumeInstanceTOs;
    }

    public static List<IPTO> componentNodeInstanceIPsToIpsTO(List<ComponentNodeInstanceIP> componentNodeInstanceIPs,
            ComponentNodeInstance componentNodeInstance) {

        List<IPTO> ipTOs = new ArrayList<>();

        componentNodeInstanceIPs.stream().forEach(componentNodeInstanceIP -> {

            IPTO ipTO = new IPTO();
            ipTO.setApplicationInstanceID(
                    componentNodeInstance.getApplicationInstance().getApplicationInstanceID());
            ipTO.setComponentNodeInstanceID(componentNodeInstance.getComponentNodeInstanceID());
            ipTO.setComponentNodeInstanceIPID(componentNodeInstanceIP.getComponentNodeInstanceIPID());
            ipTO.setDateCreated(componentNodeInstanceIP.getDateCreated());
            ipTO.setLastModified(componentNodeInstanceIP.getLastModified());
            ipTO.setGraphInstanceID(
                    componentNodeInstance.getApplicationInstance().getApplicationInstanceID() + "");
            ipTO.setComponentNodeInstanceName(componentNodeInstance.getName());
            ipTO.setIp(componentNodeInstanceIP.getIp());
            ipTO.setNetwork(componentNodeInstanceIP.getNetwork());
            ipTO.setType(componentNodeInstanceIP.getType());

            ipTOs.add(ipTO);

        });

        return ipTOs;
    }

    public static List<AlertTO> componentNodeInstanceAlertsToAlertsTO(List<ComponentNodeInstanceAlert> componentNodeInstanceAlerts,
            ComponentNodeInstance componentNodeInstance) {

        List<AlertTO> alertTOs = new ArrayList<>();

        componentNodeInstanceAlerts.stream().forEach(alert -> {

            AlertTO alertTO = new AlertTO();
            alertTO.setComponentNodeInstanceAlertID(alert.getComponentNodeInstanceAlertID());
            alertTO.setApplicationInstanceID(
                    componentNodeInstance.getApplicationInstance().getApplicationInstanceID());
            alertTO.setComponentNodeInstanceID(componentNodeInstance.getComponentNodeInstanceID());
            alertTO.setComponentNodeInstanceName(componentNodeInstance.getName());
            alertTO.setGraphInstanceID(
                    componentNodeInstance.getApplicationInstance().getApplicationInstanceID() + "");
            alertTO.setMessage(alert.getMessage());
            alertTO.setStatus(alert.getStatus());
            alertTO.setDateCreated(alert.getDateCreated());
            alertTO.setLastModified(alert.getLastModified());
            alertTOs.add(alertTO);

        });

        return alertTOs;
    }

    public static List<StatusTO> componentNodeInstanceStatusesToStatusesTO(List<ComponentNodeInstanceStatus> componentNodeInstanceStatuses,
            ComponentNodeInstance componentNodeInstance) {

        List<StatusTO> statusesTOs = new ArrayList<>();

        componentNodeInstanceStatuses.forEach(status -> {

            StatusTO statusTO = new StatusTO();
            statusTO.setComponentNodeInstanceStatusID(status.getComponentNodeInstanceStatusID());
            statusTO.setApplicationInstanceID(
                    componentNodeInstance.getApplicationInstance().getApplicationInstanceID());
            statusTO.setComponentNodeInstanceID(componentNodeInstance.getComponentNodeInstanceID());
            statusTO.setComponentNodeInstanceName(componentNodeInstance.getName());
            statusTO.setDateCreated(status.getDateCreated());
            statusTO.setLastModified(status.getLastModified());
            statusTO.setMessage(status.getMessage());
            statusTO.setReportedChange(status.getReportedChange());
            statusTO.setStatus(status.getStatus());
            statusesTOs.add(statusTO);

        });

        return statusesTOs;
    }

}
