package eu.orchestrator.elasticity.spi.model.orchestrator;

import eu.orchestrator.transfer.entities.orchestrator.OrchestratorApplicationInstance;
import eu.orchestrator.transfer.entities.orchestrator.internal.ComposeStatusObject;
import eu.orchestrator.transfer.entities.orchestrator.internal.ServiceStatus;
import eu.orchestrator.transfer.entities.policyEngine.TriggerActionModel;
import eu.orchestrator.collector.Collector;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 19/7/2019
 */
public class OrchestratorScalingTO {

    private OrchestratorApplicationInstance composeObject;
    private ComposeStatusObject composeStatusObject;
    private TriggerActionModel triggerActionModel;
    private ConsulConfigTO consulConfig;
    private String backendURL;
    private String backendPORT;
    private ServiceStatus elasticityControllerStatus;
    private Collector metricCollector;
    private String elasticityDimension;

    public OrchestratorScalingTO(OrchestratorApplicationInstance composeObject, ComposeStatusObject composeStatusObject, TriggerActionModel triggerActionModel,
                                 String backendURL, String backendPORT, ConsulConfigTO consulConfig, ServiceStatus elasticityControllerStatus, Collector metricCollector, String elasticityDimension) {
        this.composeObject = composeObject;
        this.composeStatusObject = composeStatusObject;
        this.triggerActionModel = triggerActionModel;
        this.backendURL = backendURL;
        this.backendPORT = backendPORT;
        this.consulConfig = consulConfig;
        this.elasticityControllerStatus = elasticityControllerStatus;
        this.metricCollector = metricCollector;
        this.elasticityDimension = elasticityDimension;
    }

    public OrchestratorApplicationInstance getComposeObject() {
        return composeObject;
    }

    public void setComposeObject(OrchestratorApplicationInstance composeObject) {
        this.composeObject = composeObject;
    }

    public ComposeStatusObject getComposeStatusObject() {
        return composeStatusObject;
    }

    public void setComposeStatusObject(ComposeStatusObject composeStatusObject) {
        this.composeStatusObject = composeStatusObject;
    }

    public TriggerActionModel getTriggerActionModel() {
        return triggerActionModel;
    }

    public void setTriggerActionModel(TriggerActionModel triggerActionModel) {
        this.triggerActionModel = triggerActionModel;
    }

    public ConsulConfigTO getConsulConfig() {
        return consulConfig;
    }

    public void setConsulConfig(ConsulConfigTO consulConfig) {
        this.consulConfig = consulConfig;
    }

    public String getBackendURL() {
        return backendURL;
    }

    public void setBackendURL(String backendURL) {
        this.backendURL = backendURL;
    }

    public String getBackendPORT() {
        return backendPORT;
    }

    public void setBackendPORT(String backendPORT) {
        this.backendPORT = backendPORT;
    }

    public ServiceStatus getElasticityControllerStatus() {
        return elasticityControllerStatus;
    }

    public void setElasticityControllerStatus(ServiceStatus elasticityControllerStatus) {
        this.elasticityControllerStatus = elasticityControllerStatus;
    }


    public Collector getMetricCollector() {
        return metricCollector;
    }

    public void setMetricCollector(Collector metricCollector) {
        this.metricCollector = metricCollector;
    }

    public String getElasticityDimension() {
        return elasticityDimension;
    }

    public void setElasticityDimension(String elasticityDimension) {
        this.elasticityDimension = elasticityDimension;
    }
}
