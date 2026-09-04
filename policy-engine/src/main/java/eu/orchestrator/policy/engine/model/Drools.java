package eu.orchestrator.policy.engine.model;

import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * @author Panagiotis Parthenis
 * <p>
 * This class is the represent of a Session obj , hashed with graphHexID:graphInstanceHexID:policRuleHexID
 */
@Component
public class Drools {

  private HashMap<String, KieSession> kieSessionHashMap = new HashMap<>();

  public HashMap<String, KieSession> getKieSessionHashMap() {
    return kieSessionHashMap;
  }

  public void setKieSessionHashMap(HashMap<String, KieSession> kieSessionHashMap) {
    this.kieSessionHashMap = kieSessionHashMap;
  }

  public void addIntoKieSessionHashMap(String key, KieSession value) {
    kieSessionHashMap.put(key, value);
  }

  public KieSession findKieSessionHashMap(String key) {
    return kieSessionHashMap.get(key);
  }

  public boolean removeKieSessionHashMap(String key) {
    kieSessionHashMap.remove(key);
    if (findKieSessionHashMap(key) == null) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return "Drools{" +
        "kieSessionHashMap=" + kieSessionHashMap +
        '}';
  }
}
