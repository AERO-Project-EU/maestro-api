package eu.orchestrator.policy.engine.service.impl;

import eu.orchestrator.policy.engine.model.Drools;
import eu.orchestrator.policy.engine.model.DroolsFact;
import eu.orchestrator.policy.engine.service.DroolsService;
import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.EntryPoint;
import org.kie.internal.io.ResourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Panagiotis Parthenis
 */
@Component
public class DroolsServiceStreamImpl implements DroolsService {

  @Autowired
  private Drools drools;

  @Override
  public void initDroolsEngine(String ruleFilePath, String id) {
    KieServices kieServices = KieServices.Factory.get();

    // Get rules file
    Resource resourceFile = ResourceFactory.newFileResource(ruleFilePath);

    // Define drools resources like drools files (.drl) (Knowledge Base)
    KieFileSystem kieFileSystem = kieServices.newKieFileSystem().write(resourceFile);

    // Create a new Builder for current Knowledge Base
    KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
    kieBuilder.buildAll();

    // Create a new module
    KieModule kieModule = kieBuilder.getKieModule();

    // Create new unique container (contains kieSession, Kiebases etc)
    KieContainer kieContainer = kieServices.newKieContainer(kieModule.getReleaseId());

    //set configuration operation
    KieBaseConfiguration config = KieServices.Factory.get().newKieBaseConfiguration();
    config.setOption(EventProcessingOption.STREAM);

    KieBase kieBase = kieContainer.newKieBase(config);
    KieSession kieSession = kieBase.newKieSession();
    drools.addIntoKieSessionHashMap(id, kieSession);
  }

  @Override
  public void insertFactsIntoWorkingMemory(DroolsFact droolsFact, String id) {
    KieSession kieSession = drools.findKieSessionHashMap(id);
    EntryPoint entryPoint = kieSession.getEntryPoint("MonitoringStream");
    entryPoint.insert(droolsFact);
  }

  @Override
  public void fireRulesAll(String id) {
    KieSession kieSession = drools.findKieSessionHashMap(id);
    kieSession.fireAllRules();
  }

  @Override
  public void fireRuleByAgendaGroup(String groupName) {

  }

  @Override
  public void removeKieSessionWithId(String id) {
    drools.removeKieSessionHashMap(id);
  }

  @Override
  public boolean isKieSessionActive(String id) {
    if (drools.findKieSessionHashMap(id) == null) {
      return false;
    } else {
      return true;
    }

  }

  @Override
  public void fileUntilHalt(String id) {
    KieSession kieSession = drools.findKieSessionHashMap(id);
    kieSession.fireUntilHalt();
  }

  @Override
  public void Halt(String id) {
    KieSession kieSession = drools.findKieSessionHashMap(id);
    kieSession.halt();
  }

}
