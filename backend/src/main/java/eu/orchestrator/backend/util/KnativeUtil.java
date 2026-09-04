package eu.orchestrator.backend.util;

import eu.orchestrator.backend.service.model.KubernetesGraph;
import eu.orchestrator.common.util.NullCheckUtil;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.repository.domain.ComponentNodeInstanceAffinity;

import eu.orchestrator.repository.domain.EnvironmentalVariableInstance;
import io.fabric8.knative.serving.v1.RevisionTemplateSpecBuilder;
import io.fabric8.knative.serving.v1.ServiceBuilder;

import io.fabric8.kubernetes.api.model.*;
import java.util.*;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static eu.orchestrator.backend.util.KubernetesUtil.*;


public class KnativeUtil {

    private static final Logger logger = Logger.getLogger(KnativeUtil.class.getName());

    private static final String LABELS_APP = "app";
    private static final String DEPLOYMENT_SUFFIX = "-deployment";
    private static final String SECRET_SUFFIX = "-secret";
    private static final String SERVICE_SUFFIX = "-service";
    private static final String KUBERNETES_SERVICE_SUFFIX = ".svc.cluster.local";

    private static List<String> componentNames = new ArrayList<>();
    private static String namespace_global = null;

    public static KubernetesGraph applicationInstanceToKnative(ApplicationInstance applicationInstance, String namespace, Boolean networkModeHost,
                                                               boolean k8sResourcesEnable, Map<Long, ComponentNodeInstanceAffinity> cniAffinityMap) {

        KubernetesGraph kubernetesGraph = new KubernetesGraph();
        componentNames = new ArrayList<>();
        if (NullCheckUtil.isNotEmpty(applicationInstance.getComponentNodeInstances())) {
            SortedSet<ComponentNodeInstance> componentNodeInstances = applicationInstance.getComponentNodeInstances();

            namespace_global = namespace;

            //for service discovery
            componentNodeInstances.forEach(x -> componentNames.add(componentInstanceNameProvider(x)));

            for (ComponentNodeInstance cni : componentNodeInstances) {
                io.fabric8.knative.serving.v1.Service cniKnativeServiceDeployment = knativeServiceDeployment(cni, networkModeHost, k8sResourcesEnable);
//                if (cniAffinityMap != null && cniAffinityMap.containsKey(cni.getComponentNodeInstanceID())) {
//                    setK8sDeploymentAffinity(cniDeployment, cniAffinityMap.get(cni.getComponentNodeInstanceID()));
//                }
                kubernetesGraph.getKnativeService().add(cniKnativeServiceDeployment);

                Service service = k8sService(cni);
                if (NullCheckUtil.isNotEmpty(service)) {
                    kubernetesGraph.getService().add(service);
                }
            }
        }

        return kubernetesGraph;
    }



    private static io.fabric8.knative.serving.v1.Service knativeServiceDeployment(ComponentNodeInstance componentNodeInstance, Boolean networkModeHost, boolean k8sResourcesEnable) {

        String name = componentInstanceNameProvider(componentNodeInstance);

        io.fabric8.knative.serving.v1.Service service = new ServiceBuilder()
                .withNewMetadata()
                .withName(name + DEPLOYMENT_SUFFIX)
                .addToLabels(LABELS_APP, name)
                .endMetadata()
                .withNewSpec()
                .withTemplate(new RevisionTemplateSpecBuilder()
                        .withNewMetadata()
                        .addToLabels(LABELS_APP, name)
                            .addToAnnotations(getServerlessRequirements(componentNodeInstance, k8sResourcesEnable))
                        .endMetadata()
                        .withNewSpec()
                        .addToContainers(new ContainerBuilder()
                                .withName(name)
                                .withImage(dockerImageNameProvider(componentNodeInstance))
//                                .withResources(getResourceRequirements(componentNodeInstance, k8sResourcesEnable))
                                .withEnv(environmentalVariableInstancesToEnv
                                        (componentNodeInstance.getEnvironmentalVariableInstances()))
                                .withCommand(getCommand(componentNodeInstance))
                                .withPorts(getContainerPorts(componentNodeInstance))
                                .withSecurityContext(getSecurityContext(componentNodeInstance))
                                .build())
                        .withImagePullSecrets(getImagePullSecrets(name))
                        .withHostNetwork(Boolean.TRUE.equals(networkModeHost) || componentNodeInstance.getComponentNode().getComponent().getNetworkModeHost())
                        .endSpec()
                        .build())
                .endSpec()
                .build();

        return service;

    }

    private static Map<String, String> getServerlessRequirements(ComponentNodeInstance componentNodeInstance, boolean k8sResourcesEnable) {

        Map<String, String> serverlessRequirementsAnnotations = new HashMap<>();

        if (k8sResourcesEnable && null != componentNodeInstance.getComponentNode().getComponent().getRequirement().getServerlessEnabled()
                && NullCheckUtil.isNotEmpty(componentNodeInstance.getComponentNode().getComponent().getRequirement().getServerlessEnabled())
                && false != componentNodeInstance.getComponentNode().getComponent().getRequirement().getServerlessEnabled()) {

            if (null != componentNodeInstance.getComponentNode().getComponent().getServerlessProperties()
                    && NullCheckUtil.isNotEmpty(componentNodeInstance.getComponentNode().getComponent().getServerlessProperties())) {

                String knativeScalingPrefix = "autoscaling.knative.dev/";

                // Autoscaler Class
                if (null != componentNodeInstance.getComponentNode().getComponent().getServerlessProperties().getAutoscaler()
                        && NullCheckUtil.isNotEmpty(componentNodeInstance.getComponentNode().getComponent().getServerlessProperties().getAutoscaler())) {

                    String autoscaler = componentNodeInstance.getComponentNode().getComponent().getServerlessProperties().getAutoscaler();
                    serverlessRequirementsAnnotations.put(knativeScalingPrefix + "class", autoscaler.toLowerCase() + ".autoscaling.knative.dev");

                }

                // Metric
                if (null != componentNodeInstance.getComponentNode().getComponent().getServerlessProperties().getMetric()
                        && NullCheckUtil.isNotEmpty(componentNodeInstance.getComponentNode().getComponent().getServerlessProperties().getMetric())) {

                    String metric = componentNodeInstance.getComponentNode().getComponent().getServerlessProperties().getMetric();
                    serverlessRequirementsAnnotations.put(knativeScalingPrefix + "metric", metric );
                }

                // Requests Per Second Metric
                if (null != componentNodeInstance.getComponentNode().getComponent().getServerlessProperties().getTargetValue()
                        && NullCheckUtil.isNotEmpty(componentNodeInstance.getComponentNode().getComponent().getServerlessProperties().getTargetValue())) {

                    String targetValue = componentNodeInstance.getComponentNode().getComponent().getServerlessProperties().getTargetValue();
                    serverlessRequirementsAnnotations.put(knativeScalingPrefix + "target", targetValue);
                }

                // Min Scale Bound
                if (null != componentNodeInstance.getComponentNode().getComponent().getServerlessProperties().getMinScale()
                        && NullCheckUtil.isNotEmpty(componentNodeInstance.getComponentNode().getComponent().getServerlessProperties().getMinScale())) {

                    String minScale = componentNodeInstance.getComponentNode().getComponent().getServerlessProperties().getMinScale();
                    serverlessRequirementsAnnotations.put(knativeScalingPrefix + "min-scale", minScale);
                }

                // Max Scale Bound
                if (null != componentNodeInstance.getComponentNode().getComponent().getServerlessProperties().getMaxScale()
                        && NullCheckUtil.isNotEmpty(componentNodeInstance.getComponentNode().getComponent().getServerlessProperties().getMaxScale())) {

                    String maxScale = componentNodeInstance.getComponentNode().getComponent().getServerlessProperties().getMaxScale();
                    serverlessRequirementsAnnotations.put(knativeScalingPrefix + "max-scale", maxScale);
                }

                // Window Size
                if (null != componentNodeInstance.getComponentNode().getComponent().getServerlessProperties().getWindowSize()
                        && NullCheckUtil.isNotEmpty(componentNodeInstance.getComponentNode().getComponent().getServerlessProperties().getWindowSize())) {

                    String windowSize = componentNodeInstance.getComponentNode().getComponent().getServerlessProperties().getWindowSize() + "s";
                    serverlessRequirementsAnnotations.put(knativeScalingPrefix + "window", windowSize);
                }
            }
        }

        return serverlessRequirementsAnnotations;

    }


    private static ResourceRequirements getResourceRequirements(ComponentNodeInstance componentNodeInstance, boolean k8sResourcesEnable) {
        ResourceRequirements resourceRequirements = new ResourceRequirements();

        if (k8sResourcesEnable && NullCheckUtil.isNotEmpty(componentNodeInstance.getFlavorInstance())) {
            Map<String, Quantity> limits = new HashMap<>();
            Map<String, Quantity> requests = new HashMap<>();

            if (NullCheckUtil.isNotEmpty(componentNodeInstance.getFlavorInstance().getvCPUs())) {
                Quantity quantity = new Quantity();
                quantity.setAmount(componentNodeInstance.getFlavorInstance().getvCPUs().toString());
                limits.put("cpu", quantity);
                requests.put("cpu", quantity);
            }

            if (NullCheckUtil.isNotEmpty(componentNodeInstance.getFlavorInstance().getRam())) {
                Quantity quantity = new Quantity();
                String ram = componentNodeInstance.getFlavorInstance().getRam().toString() + "Mi";
                quantity.setAmount(ram);
                limits.put("memory", quantity);
                requests.put("memory", quantity);
            }

            if (componentNodeInstance.getComponentNode().getComponent().getRequirement().getGpuRequired()) {
                Quantity quantity = new Quantity();
                quantity.setAmount("1");
                limits.put("nvidia.com/gpu", quantity);
                requests.put("nvidia.com/gpu", quantity);
            }

            if (NullCheckUtil.isNotEmpty(limits) || NullCheckUtil.isNotEmpty(requests)) {
                if (NullCheckUtil.isNotEmpty(limits)) {
                    resourceRequirements.setLimits(limits);
                }
                if (NullCheckUtil.isNotEmpty(requests)) {
                    resourceRequirements.setRequests(requests);
                }
//                container.setResources(resourceRequirements);
            }
        }

        return resourceRequirements;
    }

    private static List<String> getCommand(ComponentNodeInstance componentNodeInstance) {
        List<String> command = new ArrayList<>();

        if (componentNodeInstance.getCommand() != null && !componentNodeInstance.getCommand().isEmpty()) {
            command.add(componentNodeInstance.getCommand());
        }
        return command;
    }

    private static ContainerPort getContainerPorts(ComponentNodeInstance componentNodeInstance) {
        ContainerPort containerPort = new ContainerPort();

        if (NullCheckUtil.isNotEmpty(componentNodeInstance.getInterfaceInstances())) {
            containerPort.setContainerPort(Integer.parseInt(componentNodeInstance.getInterfaceInstances().get(0).getPort()));
        }

        return containerPort;
    }

    private static SecurityContext getSecurityContext(ComponentNodeInstance componentNodeInstance) {
        SecurityContext securityContext = new SecurityContext();

        if (NullCheckUtil.isNotEmpty(componentNodeInstance.getComponentNode())
                && NullCheckUtil.isNotEmpty(componentNodeInstance.getComponentNode().getComponent())
                && NullCheckUtil.isNotEmpty(componentNodeInstance.getComponentNode().getComponent().getPrivilege())) {
            securityContext.setPrivileged(componentNodeInstance.getComponentNode().getComponent().getPrivilege());
        }

        return securityContext;
    }

    private static List<LocalObjectReference> getImagePullSecrets(String name) {
        List<LocalObjectReference> imagePullSecrets = new ArrayList<>();
        LocalObjectReference imagePullSecret = new LocalObjectReference();
        imagePullSecret.setName(name + SECRET_SUFFIX);
        imagePullSecrets.add(imagePullSecret);
        return imagePullSecrets;
    }

    static List<EnvVar> environmentalVariableInstancesToEnv(List<EnvironmentalVariableInstance> environmentalVariableInstances) {

        List<EnvVar> envVars = new ArrayList<>();

        // Regex pattern for port identification
        String regex = ":(\\d+)$";

        for (EnvironmentalVariableInstance en : environmentalVariableInstances) {
            if (en.getValue().startsWith("@")) {
                String value = en.getValue().replaceFirst("@", "").toLowerCase(Locale.ROOT);

                // Check for port number existence (:portNumber)
                String portNumber = "";
                Pattern pattern = Pattern.compile(regex);
                Matcher portMatcher = pattern.matcher(value);
                if (portMatcher.find()) {
                    portNumber = portMatcher.group(1);
                    value = value.replaceFirst(":.*", "").toLowerCase(Locale.ROOT);
                }

                for (String nm : componentNames) {
                    if (nm.contains(value)) {
                        nm = "http://" + nm + DEPLOYMENT_SUFFIX + "." + namespace_global + KUBERNETES_SERVICE_SUFFIX;

                        EnvVar envVar = new EnvVar();
                        envVar.setName(en.getKey());
                        envVar.setValue(nm);
                        envVars.add(envVar);
                    }
                }

            } else {
                EnvVar envVar = new EnvVar();
                envVar.setName(en.getKey());
                envVar.setValue(en.getValue());
                envVars.add(envVar);
            }

        }

        return envVars;
    }


}
