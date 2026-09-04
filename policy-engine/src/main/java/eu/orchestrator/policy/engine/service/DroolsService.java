package eu.orchestrator.policy.engine.service;

import eu.orchestrator.policy.engine.model.DroolsFact;
import org.springframework.stereotype.Service;

/**
 * @author Panagiotis Parthenis
 */
@Service
public interface DroolsService {

  void initDroolsEngine(String ruleFilePath, String id);

  void insertFactsIntoWorkingMemory(DroolsFact droolsFact, String id);

  void fireRulesAll(String id);

  void fireRuleByAgendaGroup(String groupName);

  void removeKieSessionWithId(String id);

  boolean isKieSessionActive(String id);

  void fileUntilHalt(String id);

  void Halt(String id);

}
