package eu.orchestrator.document.repository.dao;

import eu.orchestrator.transfer.entities.orchestrator.OrchestratorApplicationInstance;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrchestratorApplicationInstanceDAO extends MongoRepository<OrchestratorApplicationInstance, String> {
    OrchestratorApplicationInstance findByGraphInstanceID(String graphInstanceID);
}
