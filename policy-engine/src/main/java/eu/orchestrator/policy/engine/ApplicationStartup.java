package eu.orchestrator.policy.engine;

import eu.orchestrator.policy.engine.configuration.DroolsConfiguration;
import eu.orchestrator.policy.engine.service.DroolsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Panagiotis Parthenis
 */
@Component
public class ApplicationStartup implements ApplicationListener<ApplicationReadyEvent> {

  private static final Logger LOGGER = Logger.getLogger(ApplicationStartup.class.getName());

  @Autowired
  private DroolsConfiguration droolsConfiguration;

  @Autowired
  private DroolsService droolsService;

  @Override
  public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {

    loadExistingPolicies();

  }

  private void loadExistingPolicies() {

    List<String> results = new ArrayList<>();

    File[] files = new File(droolsConfiguration.getFilePath()).listFiles();

    for (File file : files) {
      if (file.isFile()) {
        results.add(file.getName());
      }
    }

    for (int i = 0; i < results.size(); i++) {
      String filePath = results.get(i);
      int startAt = filePath.lastIndexOf('/') + 1;
      int endAt = filePath.length() - 4;
      String id = filePath.substring(startAt, endAt);

      if (!id.contains("_")) {
        continue;
      }

      id = id.replace("_", ":");
      LOGGER.log(Level.INFO, "Drool policy ID: {0} ", id);
      LOGGER.log(Level.INFO, "Path policy file: {0}", droolsConfiguration.getFilePath() + filePath);

      droolsService.initDroolsEngine(droolsConfiguration.getFilePath() + filePath, id);
    }

    LOGGER.log(Level.INFO, "Previous Drools rule applied!");
  }
}
