package eu.orchestrator.elasticity.spi.model.backend;

import eu.orchestrator.transfer.entities.orchestrator.OrchestratorComponentNodeInstance;
import eu.orchestrator.repository.domain.ApplicationInstance;
import eu.orchestrator.repository.domain.ComponentNodeInstance;
import eu.orchestrator.repository.domain.InterfaceInstance;

import java.util.List;
import java.util.Map;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 10/7/2019
 */
public class ElasticityObjects {

    private ApplicationInstance applicationInstance;
    private ComponentNodeInstance workerComponentNodeInstance;
    private Map<String, OrchestratorComponentNodeInstance> services;
    private Map<Long, ComponentNodeInstance> mapOfNeededChanges;
    private String vimID;
    private ComponentNodeInstance elasticityController;
    private Map<Long, ComponentNodeInstance> workers;
    private List<InterfaceInstance> elasticityControllerInterfaceInstaceList;
    private List<InterfaceInstance> elasticityControllerComponentInterfaceInstaceList;
    private String elasticityControllerOrchestratorAdapterImplementation;

    public ApplicationInstance getApplicationInstance() {
        return applicationInstance;
    }

    public void setApplicationInstance(ApplicationInstance applicationInstance) {
        this.applicationInstance = applicationInstance;
    }

    public ComponentNodeInstance getWorkerComponentNodeInstance() {
        return workerComponentNodeInstance;
    }

    public void setWorkerComponentNodeInstance(ComponentNodeInstance workerComponentNodeInstance) {
        this.workerComponentNodeInstance = workerComponentNodeInstance;
    }

    public Map<String, OrchestratorComponentNodeInstance> getServices() {
        return services;
    }

    public void setServices(Map<String, OrchestratorComponentNodeInstance> services) {
        this.services = services;
    }

    public Map<Long, ComponentNodeInstance> getMapOfNeededChanges() {
        return mapOfNeededChanges;
    }

    public void setMapOfNeededChanges(Map<Long, ComponentNodeInstance> mapOfNeededChanges) {
        this.mapOfNeededChanges = mapOfNeededChanges;
    }

    public String getVimID() {
        return vimID;
    }

    public void setVimID(String vimID) {
        this.vimID = vimID;
    }

    public ComponentNodeInstance getElasticityController() {
        return elasticityController;
    }

    public void setElasticityController(ComponentNodeInstance elasticityController) {
        this.elasticityController = elasticityController;
    }

    public Map<Long, ComponentNodeInstance> getWorkers() {
        return workers;
    }

    public void setWorkers(Map<Long, ComponentNodeInstance> workers) {
        this.workers = workers;
    }

    public List<InterfaceInstance> getElasticityControllerInterfaceInstaceList() {
        return elasticityControllerInterfaceInstaceList;
    }

    public void setElasticityControllerInterfaceInstaceList(List<InterfaceInstance> elasticityControllerInterfaceInstaceList) {
        this.elasticityControllerInterfaceInstaceList = elasticityControllerInterfaceInstaceList;
    }

    public List<InterfaceInstance> getElasticityControllerComponentInterfaceInstaceList() {
        return elasticityControllerComponentInterfaceInstaceList;
    }

    public void setElasticityControllerComponentInterfaceInstaceList(List<InterfaceInstance> elasticityControllerComponentInterfaceInstaceList) {
        this.elasticityControllerComponentInterfaceInstaceList = elasticityControllerComponentInterfaceInstaceList;
    }

    public String getElasticityControllerOrchestratorAdapterImplementation() {
        return elasticityControllerOrchestratorAdapterImplementation;
    }

    public void setElasticityControllerOrchestratorAdapterImplementation(String elasticityControllerOrchestratorAdapterImplementation) {
        this.elasticityControllerOrchestratorAdapterImplementation = elasticityControllerOrchestratorAdapterImplementation;
    }
}
