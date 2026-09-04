package eu.orchestrator.elasticity.spi.adapter;

import eu.orchestrator.elasticity.spi.model.backend.ElasticityMetadata;
import eu.orchestrator.elasticity.spi.model.backend.ScalingObjects;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorApplicationInstance;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorScalingRequest;
import eu.orchestrator.elasticity.spi.model.backend.ConfigureProperties;
import eu.orchestrator.elasticity.spi.model.backend.ElasticityObjects;

public interface ElasticityFrameworkBackend {

    /**
     * @implNote
     * Add the elasticity controller component to the database.
     * You need to fill the ElasticityControllerComponent object and call
     * the method createElasticityControllerComponent of the ElasticityControllerService class.
     * Then the service will manage the translation to the appropriate objects as also the writing to the database.
     */
    void addComponentToDB();

    /**
     * @implNote
     * Fill the ElasticityMetadata object, where the name is the type of Elasticity (Horizontal, etc)
     * elasticityMode is the mode that will be passed to the elasticity controller (HTTP, GRPC)
     * elasticityImpl is the name of the implementation class of the elasticityFramework adapter
     * elasticityBackendImpl is the name of the implementation class of the ElasticityFrameworkBackend adapter
     * and the extraMetadata is other key-value information that are needed to be passed to the elasticity controller
     * before, during or after the deployment.
     *
     * @return Returns a ElasticityMetadata object filled with whatever is needed
     */
    ElasticityMetadata getElasticityType();

    /**
     * @implNote
     * Adds the elasticity controller to the graph schema.
     *
     * In the first step it must create the componentNode of the controller by using the method createElasticityControllerComponentNode
     * of the ElasticityControllerService class.
     *
     * The next step is to create the componentNodeInstance of the controller by using the method createElasticityControllerNodeInstance
     * of the ElasticityControllerService class by adding whatever extra information may be needed.
     *
     * The next two steps are optional, in the most case is either you have to do both or neither of them
     * and it depends on the type of the controller
     * The first of those two is to move the component interfaces to the controller by using the method
     * addComponentInterfacesToElasticityController of the ElasticityControllerService class.
     * The second of the two is to move the constraints (delay, jitter, etc) of the component to the controller
     * by using the method moveConstraintsToElasticityController of the ElasticityControllerService class.
     *
     * This step is mandatory and it just replicates the balanced component to X workers, in order to implement it
     * you must use the method addWorkers of the ElasticityControllerService class.
     *
     * The following step is optional and it depends if you have use the addComponentInterfacesToElasticityController method or not.
     * In this step you must call the linkWorkersWithElasticityController method ot the ElasticityControllerService class
     * where we take care the graphical representation of the application.
     *
     * The final mandatory step is to call the generateElasticityControllerService method of the ElasticityControllerService class
     * in order to generate the transfer object of the controller for the core-orchestrator
     *
     * @param elasticityObjects
     * @return ElasticityObjects filled with the in between objects.
     */
    ElasticityObjects configureElasticityController(ElasticityObjects elasticityObjects);

    //Have in mind the we may add it to different provider in the future
    /**
     * @implNote
     * Adds workers to the database and returns the
     *
     * In order to do that you need just to call increaseWorkers method from the ScalingService class
     *
     * @param scalingRequest
     * @return OrchestratorApplicationInstance that contains all the workers that are need to be deployed
     */
    ScalingObjects scaleOut(ScalingObjects scalingRequest);

    /**
     * @implNote
     * Deletes workers from the database
     *
     * In order to do that you need just to call decreaseWorkers method from the ScalingService class
     *
     * @param orchestratorScalingRequest
     * @return Boolean where true if success and false if error
     */
    Boolean scaleIn(OrchestratorScalingRequest orchestratorScalingRequest);

}
