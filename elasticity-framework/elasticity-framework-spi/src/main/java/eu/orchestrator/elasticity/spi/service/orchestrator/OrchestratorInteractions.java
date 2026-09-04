package eu.orchestrator.elasticity.spi.service.orchestrator;

import eu.orchestrator.elasticity.spi.model.orchestrator.ScaleInTO;
import eu.orchestrator.elasticity.spi.model.orchestrator.ScaleOutTO;
import eu.orchestrator.elasticity.spi.model.orchestrator.OrchestratorScalingTO;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorComponentNodeInstance;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorDependency;
import eu.orchestrator.transfer.entities.orchestrator.internal.ServiceStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 19/7/2019
 */
public class OrchestratorInteractions {
    private static final Logger logger = LogManager.getLogger(OrchestratorInteractions.class);

    private static final int STATUS_COMPONENT_INSTANCE_UP = 8;


    public ScaleOutTO calculateRunningWorkers(OrchestratorScalingTO scalingObject){
        ScaleOutTO scaleOutTO = new ScaleOutTO();

        Integer runningWorkers = 0;
        for (OrchestratorComponentNodeInstance service : scalingObject.getComposeObject().getServices()) {
            if (service.getComponentNodeHexID().equals(scalingObject.getTriggerActionModel().getComponentNodeHexID())) {
                scaleOutTO.setServiceToScale(service);
                runningWorkers++;
            }
        }

        scaleOutTO.setRunningWorkers(runningWorkers);
        scaleOutTO.setProceedWithScaling(true);
        return scaleOutTO;
    }

    public ScaleOutTO calculateWorkersToRequest(ScaleOutTO scaleOutTO, OrchestratorScalingTO scalingObject){
        if (scaleOutTO.getServiceToScale() == null) {
            logger.warn("The componentNodeHexID: " + scalingObject.getTriggerActionModel().getComponentNodeHexID() + " doesn't exists");
            scaleOutTO.setProceedWithScaling(false);
            return scaleOutTO;
        }

        if(scaleOutTO.getRunningWorkers() == scaleOutTO.getServiceToScale().getMaximumWorkers()){
            scaleOutTO.setProceedWithScaling(false);
            return scaleOutTO;
        }else{
            int remainingWorkers = scaleOutTO.getServiceToScale().getMaximumWorkers() - scaleOutTO.getRunningWorkers();
            if(scalingObject.getTriggerActionModel().getActionAmount() > remainingWorkers){
                scaleOutTO.setWorkersToRequest(remainingWorkers);
            }else{
                scaleOutTO.setWorkersToRequest(scalingObject.getTriggerActionModel().getActionAmount());
            }
        }
        scaleOutTO.setProceedWithScaling(true);
        return scaleOutTO;
    }

    public ScaleInTO calculateWorkersToRemove(OrchestratorScalingTO scalingObject){
        ScaleInTO scaleInTO = new ScaleInTO();

        List<OrchestratorComponentNodeInstance> serviceList = new ArrayList<>();
        List<ServiceStatus> serviceStatusList = new ArrayList<>();

        //Get all the services with the specified componentNodeID / Name
        for(OrchestratorComponentNodeInstance service : scalingObject.getComposeObject().getServices()){
            if(service.getComponentNodeHexID().equals(scalingObject.getTriggerActionModel().getComponentNodeHexID())){
                logger.info("Add to the service list the instance: " + service.getComponentNodeInstanceHexID());
                serviceList.add(service);
            }
        }

        //Get all the serviceStatus with the specified componentNodeID / Name
        for(ServiceStatus serviceStatus : scalingObject.getComposeStatusObject().getServiceStatusList()){
            if(serviceStatus.getComponentNodeHexID().equals(scalingObject.getTriggerActionModel().getComponentNodeHexID())){
                logger.info("Add to the service Status list the instance: " + serviceStatus.getComponentNodeInstanceHexID());
                serviceStatusList.add(serviceStatus);
            }
        }

        if(serviceList==null || serviceList.isEmpty()){
            scaleInTO.setProceedWithScaling(false);
            return scaleInTO;
        }

        int minimumWorkers = serviceList.get(0).getMinimumWorkers();
        int numberOfRunningWorkers = serviceList.size();
        int workersToRemove = 0;
        // I check if we can remove this amount of workers
        // and if not, how much we can remove
        if(scalingObject.getTriggerActionModel().getActionAmount()>=(numberOfRunningWorkers-minimumWorkers)){
            workersToRemove = numberOfRunningWorkers - minimumWorkers;
        }else{
            workersToRemove = scalingObject.getTriggerActionModel().getActionAmount();
        }

        logger.info("Numbers of workers that will be removed: " + workersToRemove);

        if(workersToRemove<=0){
            scaleInTO.setProceedWithScaling(false);
            return scaleInTO;
        }

        scaleInTO.setWorkersToRemove(workersToRemove);
        scaleInTO.setServiceStatusList(serviceStatusList);
        scaleInTO.setProceedWithScaling(true);
        return scaleInTO;
    }

    public ScaleInTO findWorkersTORemove(ScaleInTO scaleInTO){
        List<ServiceStatus> possibleServiceStatusToRemove = new ArrayList<>();
        int counter = 0;

        for(ServiceStatus serviceStatus : scaleInTO.getServiceStatusList()){
            if(counter<scaleInTO.getWorkersToRemove()) {
                if (serviceStatus.getStatus() < STATUS_COMPONENT_INSTANCE_UP) {
                    possibleServiceStatusToRemove.add(serviceStatus);
                    counter++;
                }
            } else {
                break;
            }
        }

        if(counter<scaleInTO.getWorkersToRemove()){
            for(ServiceStatus serviceStatus : scaleInTO.getServiceStatusList()){
                if(counter<scaleInTO.getWorkersToRemove() &&  !possibleServiceStatusToRemove.contains(serviceStatus)) {
                    possibleServiceStatusToRemove.add(serviceStatus);
                    counter++;
                } else {
                    break;
                }
            }
        }

        List<ServiceStatus> qualifiedForRemoval = new ArrayList<>();

        for(ServiceStatus serviceStatus : possibleServiceStatusToRemove){
            logger.info("Qualified for removal: " + serviceStatus.getComponentNodeInstanceHexID());
            qualifiedForRemoval.add(serviceStatus);
        }

        if(qualifiedForRemoval == null || qualifiedForRemoval.isEmpty()){
            scaleInTO.setProceedWithScaling(false);
            return scaleInTO;
        }

        scaleInTO.setProceedWithScaling(true);
        scaleInTO.setQualifiedForRemoval(qualifiedForRemoval);
        return scaleInTO;
    }

    public Object findControllerMetadata(ScaleInTO scaleInTO, OrchestratorScalingTO scalingObject){
        String componentNodeHexID = scaleInTO.getQualifiedForRemoval().get(0).getComponentNodeHexID();
        Object controllerMetadata = null;

        for(OrchestratorComponentNodeInstance component: scalingObject.getComposeObject().getServices()) {
            //Check if this component is controller and if so check if it has dependency on the scalable component

            if(component.getController() && null != component.getDependsOn() && !component.getDependsOn().isEmpty()){
                for(OrchestratorDependency dependency : component.getDependsOn()){
                    if(dependency.getDependency().equals(componentNodeHexID)){
                        controllerMetadata = component.getControllerMetadata();
                        break;
                    }
                }
            }
        }

        return controllerMetadata;
    }
}
