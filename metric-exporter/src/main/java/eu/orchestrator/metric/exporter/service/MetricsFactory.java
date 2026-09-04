package eu.orchestrator.metric.exporter.service;

import eu.orchestrator.metric.exporter.constant.MonitoringTypes;
import eu.orchestrator.metric.exporter.service.impl.KubernetesPrometheus;
import eu.orchestrator.metric.exporter.service.impl.RainbowKubernetes;
import eu.orchestrator.metric.exporter.service.impl.VmPrometheus;
import eu.orchestrator.metric.exporter.service.interfaces.MonitoringService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class MetricsFactory {

    private static final Logger logger = Logger.getLogger(MetricsFactory.class.getName());

    private final VmPrometheus vmPrometheus;
    private final KubernetesPrometheus kubernetesPrometheus;
    private final RainbowKubernetes rainbowKubernetes;

    @Autowired
    public MetricsFactory(VmPrometheus vmPrometheus, KubernetesPrometheus kubernetesPrometheus,
            RainbowKubernetes rainbowKubernetes) {
        this.vmPrometheus = vmPrometheus;
        this.kubernetesPrometheus = kubernetesPrometheus;
        this.rainbowKubernetes = rainbowKubernetes;
    }

    public MonitoringService getPrometheusInstance(String type) {
        logger.info("Metric Exporter - Metrics Factory - TYPE: " + type);
        if (MonitoringTypes.VM_PROMETHEUS.equalsIgnoreCase(type)) {
            return vmPrometheus;
        } else if (MonitoringTypes.K8S_PROMETHEUS.equalsIgnoreCase(type)) {
            return kubernetesPrometheus;
        } else if (MonitoringTypes.RAINBOW_K8S.equalsIgnoreCase(type)) {
            logger.info("Metric Exporter - Metrics Factory - Rainbow Metrics");
            return rainbowKubernetes;
        } else {
            return vmPrometheus;
        }
    }

}
