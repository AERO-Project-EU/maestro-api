package eu.orchestrator.elasticity.spi.adapter;


import eu.orchestrator.elasticity.spi.model.orchestrator.ScaleInTO;
import eu.orchestrator.elasticity.spi.model.orchestrator.ScaleOutTO;
import eu.orchestrator.elasticity.spi.model.orchestrator.OrchestratorScalingTO;
import eu.orchestrator.elasticity.spi.model.orchestrator.UpdateControllerTO;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorComponentNodeInstance;

import java.util.HashMap;
import java.util.List;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 19/7/2019
 */
public interface ElasticityFrameworkOrchestrator {

    /**
     * @implNote
     * Make whatever initialization are needed on the ControllerMetadata Object.
     * This method will be called before the deployment process is begun as also in the rare case
     * that a cold boot of the control loop is required and the metadata have been lost.
     *
     * @param componentList
     * @return Extended ControllerMetadata Object, at least return an initialized object
     */
    Object initControllerMetadata(List<OrchestratorComponentNodeInstance> componentList);

    /**
     * @implNote
     * Finds which workers are up and running as also the service of them is healthy.
     * Then updates the ControllerMetadata object appropriately.
     * Those metadata will be then given to the updateElasticityController method.
     *
     * @param scalingObject
     * @param component
     * @param hashMapServerName
     * @return Extended ControllerMetadata Object
     */
    Object getHealthyWorkers(OrchestratorScalingTO scalingObject, OrchestratorComponentNodeInstance component, HashMap hashMapServerName);

    /**
     * @implNote
     * Informs the elasticity controller with a new configuration if needed.
     * In case that the ControllerStatus is different than EMPTY_CONTROLLER_CONFIG and NO_CONTROLLER_UPDATE_NEEDED
     * update the metadata too, in order to let the control loop have them updated.
     * The metadata MUST contain the whole information that it will be needed in the next steps, as the control loop
     * will retain only this object of metadata and not the previous one.
     *
     * You can inform the controller either using the consul KV service, the kafka or even by a custom way.
     *
     * @param scalingObject
     * @param component
     * @param controllerNewMetadata those metadata are those that have been generated from the getHealthyWorkers method.
     * @return UpdateControllerTO with the appropriate status and updated metadata if needed
     */
    UpdateControllerTO updateElasticityController(OrchestratorScalingTO scalingObject, OrchestratorComponentNodeInstance component, Object controllerNewMetadata);


    /**
     * @implNote
     * Calculates the amount of workers that will be requested, based on the triggerActionModel and makes the call to the Backend for them.
     *
     * You can use the orchestratorInteractions.calculateRunningWorkers(), then orchestratorInteractions.calculateWorkersToRequest()
     * and then backendInteractions.scaleOutRequest()
     * If you want you can make a custom method of how you will calculate the workers to request.
     * As also, you can update your controller or your metadata before the return of the method.
     * The actual deployment will be done from the orchestrator using the return object.
     *
     * If you don't want to follow the implementation paradigm of the TraefikLoadBalancerAdapter and TraefikLambdaProxy it may be required
     * to know more for the Orchestrator's Objects.
     *
     * @param scalingObject
     * @return ScaleOutTO, all fields must be initialized
     */
    ScaleOutTO scaleOut(OrchestratorScalingTO scalingObject);

    /**
     * @implNote
     * Finds which workers will be removed at the scale in.
     *
     * By using the default methods orchestratorInteractions.calculateWorkersToRemove() and orchestratorInteractions.findWorkersTORemove()
     * It will remove all the workers that are not fully working first and the randomly will select from the working ones.
     * If you want, you can implement custom ways about which workers you want to remove.
     *
     * If you don't want to follow the implementation paradigm of the TraefikLoadBalancerAdapter and TraefikLambdaProxy it may be required
     * to know more for the Orchestrator's Objects.
     *
     * @param scalingObject
     * @return ScaleInTO, at least proceedWithScaling and qualifiedForRemoval MUST be initialized
     */
    ScaleInTO findWorkersForRemoval(OrchestratorScalingTO scalingObject);


    /**
     * @implNote
     * Informs the controller about which workers will be removed at the scale in.
     * After this the VMs will be deleted.
     *
     * You can inform the controller either using the consul KV service, the kafka or even by a custom way.
     *
     * @param scaleInTO
     * @param scalingObject
     * @return ScaleInTO, at least proceedWithScaling and qualifiedForRemoval MUST be initialized
     */
    ScaleInTO removeWorkersFromController(ScaleInTO scaleInTO, OrchestratorScalingTO scalingObject);

    /**
     * @implNote
     * Updates the controllerMetadata if needed and returns them.
     * If no update is required on the metadata, return the previous one.
     *
     * This method will be called after the deletion of the VMs
     *
     * @param scaleInTO
     * @param controllerMetadata last know metadata
     * @return Updated extended ControllerMetadata Object
     */
    Object postScaleInControllerMetadataClean(ScaleInTO scaleInTO, Object controllerMetadata);

}
